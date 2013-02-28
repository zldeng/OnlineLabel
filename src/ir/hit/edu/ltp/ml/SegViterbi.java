package ir.hit.edu.ltp.ml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.basic.SegItem;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.util.FullCharConverter;
import ir.hit.edu.ltp.util.InputStreams;
import ir.hit.edu.ltp.util.OutputStreams;

/**
 * class for segment using Viterbi algorithm for decoding
 * 
 * @author dzl
 * 
 */
public class SegViterbi implements Runnable
{
	protected static OnlineLabelModel model;
	protected static SegDic segDic;
	protected static Vector<String> allLabel;

	//the three variables are use for Multi-Thread
	private static InputStreams br;
	private static OutputStreams wr;
	private static int sentenceNum = 0;
	
	public SegViterbi(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel)
	{
		SegViterbi.model = model;
		SegViterbi.segDic = segDic;
		SegViterbi.allLabel = allLabel;
	}

	public SegViterbi()
	{

	}

	/**
	 * decoding for a SEG instance
	 * this function is used when training a model
	 * 
	 * @param inst
	 * @param predLabel
	 * @return
	 */
	protected double segViterbiDecode(SegInstance inst, Vector<String> predLabel)
	{
		final int senLength = inst.sentence.size();
		SegItem[][] itemMatrix = new SegItem[senLength][allLabel.size()];

		for (int i = 0; i < senLength; i++)
		{
			if (0 == i)
			{
				for (int j = 0; j < allLabel.size(); j++)
				{
					SegInstance tmpInstance = new SegInstance(inst);
					tmpInstance.label.clear();
					tmpInstance.label.add((String) model.featMap.int2Label.get(j));

					Vector<String> featVec = tmpInstance.extractFeaturesFromInstanceInPosition(0);

					Vector<String> newFeat = new Vector<String>();
					String curLabel = "/curLabel=" + model.featMap.int2Label.get(j);
					for (String str : featVec)
					{
						newFeat.add(str + curLabel);
					}

					Vector<Integer> intVec = model.featVec2IntVec(newFeat);
					double score = model.getScore(intVec);

					itemMatrix[0][j] = new SegItem(score, tmpInstance);
				}
			}
			else
			{
				for (int j = 0; j < allLabel.size(); j++)
				{
					String curLabelStr = (String) model.featMap.int2Label.get(j);
					String curLabel = "/curLabel=" + curLabelStr;

					double maxScore = Integer.MIN_VALUE;

					for (int k = 0; k < allLabel.size(); k++)
					{
						SegItem item = new SegItem(itemMatrix[i - 1][k]);
						item.inst.label.add(curLabelStr);

						Vector<String> feat = item.inst.extractFeaturesFromInstanceInPosition(i);

						Vector<String> newFeat = new Vector<String>();
						for (String str : feat)
						{
							newFeat.add(str + curLabel);
						}
						Vector<Integer> intVec = model.featVec2IntVec(newFeat);

						item.score += model.getScore(intVec);

						if (maxScore < item.score)
						{
							maxScore = item.score;
							itemMatrix[i][j] = item;
						}
					}
				}
			}
		}

		int maxIndex = 0;
		double maxScore = itemMatrix[senLength - 1][0].score;

		for (int index = 1; index < allLabel.size(); index++)
		{
			if (maxScore < itemMatrix[senLength - 1][index].score)
			{
				maxIndex = index;
				maxScore = itemMatrix[senLength - 1][index].score;
			}
		}

		predLabel.clear();
		for (int i = 0; i < senLength; i++)
			predLabel.add(itemMatrix[senLength - 1][maxIndex].inst.label.elementAt(i));

		return maxScore;
	}

	/**
	 * decoding for a raw sentence
	 * the function is used when testing for a sentence
	 * 
	 * @param raw_sen
	 * @param segResult
	 * @throws Exception
	 */
	public void segViterbiDecode(String raw_sen, Vector<String> segResult) throws Exception
	{
		String original_sen = new String(raw_sen);

		//when test a raw sentence, convert it to full-width characters 
		raw_sen = FullCharConverter.half2Fullchange(raw_sen);

		SegInstance inst = new SegInstance(raw_sen, segDic);
		Vector<String> resultLabel = new Vector<String>();
		segViterbiDecode(inst, resultLabel);

		//the content of result string is still the original sentence
		String[] result = rawSentence2SegSentence(original_sen, resultLabel).split(" ");

		segResult.clear();
		for (int i = 0; i < result.length; i++)
			segResult.add(result[i]);
	}

	/**
	 * segment for a file
	 * one raw sentence in each line in the file
	 * the function use only one thread
	 * the function is old and we don't use it
	 * 
	 * @param testFile
	 * @param resultFile
	 * @throws Exception
	 */
	public void segForFile(String testFile, String resultFile) throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		if (model == null || null == segDic)
		{
			logger.error("Model is null,you should firstly train a model");
			throw new Exception("Model is null,you should firstly train a model");
		}

		logger.info("begin to test...");
		long startTime = System.currentTimeMillis();

		InputStreamReader is = new InputStreamReader(new FileInputStream(testFile), "UTF-8");
		BufferedReader br = new BufferedReader(is);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));

		String raw_sen;
		int count = 0;
		while ((raw_sen = br.readLine()) != null)
		{
			count++;
			if (count % 200 == 0)
				logger.info("sentence " + count);

			if (raw_sen.trim().equals(""))
				continue;

			Vector<String> resultVec = new Vector<String>();
			segViterbiDecode(raw_sen, resultVec);

			String result = "";
			for (String str : resultVec)
				result += str + " ";
			writer.write(result.trim() + "\n");
		}

		writer.flush();
		writer.close();

		logger.info("testing over!");
		long endTime = System.currentTimeMillis();
		logger.info("testing time: " + (endTime - startTime) / 1000 + " s" + "\n");

	}
	
	/**
	 * segment for a file
	 * one raw sentence in each line in the file
	 * 
	 * @param testFile
	 * @param resultFile
	 * @param threadNum thread number
	 * @throws Exception
	 */
	public void segForFile(final String testFile, final String resultFile,final int threadNum) throws Exception
	{
		br = new InputStreams(testFile);
		wr = new OutputStreams(resultFile);

		Logger logger = Logger.getLogger("seg");
		logger.info("begin to test...");
		if (model == null || br == null || null == wr)
		{
			logger.error("one of Model, br and wr is null,you should firstly train a model and the initialize br and wr");
			throw new Exception("Model is null,you should firstly train a model");
		}

		long startTime = System.currentTimeMillis();
		

		Thread[] threadVec = new Thread[threadNum];
		for (int i = 0; i < threadNum; i++)
		{
			threadVec[i] = new Thread(new SegViterbi());
			threadVec[i].start();
		}

		for (int i = 0; i < threadNum; i++)
			threadVec[i].join();
		
		logger.info("test finish!");
		long endTime = System.currentTimeMillis();
		logger.info("test time: " + (endTime - startTime) / 1000 + " s" + "\n");
	}

	/**
	 * get segmented string according raw sentence and decoding label
	 * 
	 * @param raw_sen
	 * @param label
	 * @return
	 * @throws Exception
	 */
	private String rawSentence2SegSentence(String raw_sen, Vector<String> label) throws Exception
	{
		if (raw_sen.length() != label.size())
		{
			throw new Exception("the raw sentence length is not same to label size!");
		}

		String result = raw_sen.charAt(0) + "";
		for (int i = 1; i < label.size(); i++)
		{
			if (label.elementAt(i).equals("B") || label.elementAt(i).equals("S"))
				result += " " + raw_sen.charAt(i);
			else
				result += raw_sen.charAt(i);
		}
		return result;
	}

	/**
	 * evaluate model with testGold file
	 * 
	 * @param testGold
	 * @param it
	 * @return an array which contains precision and recall of the model
	 * @throws Exception
	 */
	protected double[] evalSeg(String testGold, int it) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testGold), "UTF-8"));
		PrintWriter wr = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testGold + "-it" + it + ".result"), "UTF-8"));

		int goldTotal = 0, predTotal = 0, correct = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			if (line.trim().equals(""))
				continue;

			line = line.trim();
			String[] gold = line.split(" ");
			goldTotal += gold.length;

			String raw_sen = "";
			for (int i = 0; i < gold.length; i++)
				raw_sen += gold[i];

			Vector<String> predResult = new Vector<String>();
			segViterbiDecode(raw_sen, predResult);

			String segResult = "";
			for (String str : predResult)
				segResult += str + " ";
			wr.write(segResult.trim() + "\n");

			String[] result = segResult.split(" ");
			predTotal += result.length;

			int goldIndex = 0, predIndex = 0;
			int goldLen = 0, predLen = 0;

			while (goldIndex < gold.length && predIndex < result.length)
			{
				if (goldLen == predLen)
				{
					if (gold[goldIndex].equals(result[predIndex]))
					{
						correct++;
						goldLen += gold[goldIndex].length();
						predLen += gold[goldIndex].length();
						goldIndex++;
						predIndex++;
					}
					else
					{
						goldLen += gold[goldIndex].length();
						predLen += result[predIndex].length();
						goldIndex++;
						predIndex++;
					}
				}
				else if (goldLen < predLen)
				{
					goldLen += gold[goldIndex].length();
					goldIndex++;
				}
				else
				{
					predLen += result[predIndex].length();
					predIndex++;
				}
			}

		}

		wr.flush();
		wr.close();

		double precision = (correct * 100.0) / predTotal;
		double recall = (correct * 100.0) / goldTotal;
		double[] performance = new double[2];
		performance[0] = precision;
		performance[1] = recall;

		return performance;

	}

	/**
	 * load resources when testing
	 * 
	 * @param modelFile
	 * @param dicFile
	 * @throws Exception
	 */
	public void loadResource(String modelFile, String dicFile) throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("load resource...");
		long startTime = System.currentTimeMillis();

		segDic = new SegDic();
		segDic.loadSegDic(dicFile);
		CharType.loadCharType();

		model = OnlineLabelModel.loadModel(modelFile);
		allLabel = new Vector<String>();
		for (int i = 0; i < model.featMap.int2Label.size(); i++)
			allLabel.add((String) model.featMap.int2Label.get(i));

		logger.info("load resource over!");
		long endTime = System.currentTimeMillis();
		logger.info("loading source time: " + (endTime - startTime) / 1000 + " s\n");
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		String raw_sen;
		
		try
		{
			Logger logger = Logger.getLogger("seg");
			while ((raw_sen = br.readLine()) != null)
			{
				sentenceNum++;
				if (0 == sentenceNum % 300)
					logger.info("sentence " + sentenceNum);
				if (raw_sen.trim().equals(""))
					continue;
				
				Vector<String> resultVec = new Vector<String>();
				segViterbiDecode(raw_sen.trim(), resultVec);

				String result = "";
				for (String str : resultVec)
					result += str + " ";
				
				wr.writerLine(result.trim() + "\n");			
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
