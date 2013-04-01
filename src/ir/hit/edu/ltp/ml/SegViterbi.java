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
	public OnlineLabelModel model;
	protected SegDic segDic;
	protected Vector<String> allLabel;

	//the three variables are use for Multi-Thread
	private static InputStreams br;
	private static OutputStreams wr;
	private static int sentenceNum = 0;

	public SegViterbi(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel)
	{
		this.model = model;
		this.segDic = segDic;
		this.allLabel = allLabel;
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
	protected double segViterbiDecode(SegInstance inst, String[] predLabel)
	{
		final int senLength = inst.sentence.length;
		final int labelSize = allLabel.size();
		SegItem[][] itemMatrix = new SegItem[senLength][labelSize];

		StringBuffer bf = new StringBuffer();
		StringBuffer curLabel = new StringBuffer();
		for (int i = 0; i < senLength; i++)
		{
			if (0 == i)
			{
				for (int j = 0; j < allLabel.size(); j++)
				{
					SegInstance tmpInstance = new SegInstance(inst);

					tmpInstance.label[i] = (String) model.featMap.int2Label.get(j);

					Vector<String> featVec = tmpInstance.extractFeaturesFromInstanceInPosition(i);

					String[] newFeat = new String[featVec.size()];

					curLabel.delete(0, curLabel.length());
					curLabel.append("/cL=").append(model.featMap.int2Label.get(j));
					//					String curLabel = "/cL=" + model.featMap.int2Label.get(j);

					int index = 0;
					for (String str : featVec)
					{
						bf.delete(0, bf.length());
						bf.append(str).append(curLabel);
						String feat = new String(bf);
						newFeat[index++] = feat;
						feat = null;
					}

					int[] intVec = model.featVec2IntVec(newFeat);
					double score = model.getScore(intVec);

					featVec = null;
					newFeat = null;
					intVec = null;

					itemMatrix[0][j] = new SegItem(score, tmpInstance);
				}
			}
			else
			{
				for (int j = 0; j < allLabel.size(); j++)
				{
					String curLabelStr = (String) model.featMap.int2Label.get(j);

					curLabel.delete(0, curLabel.length());
					curLabel.append("/cL=").append(curLabelStr);

					double maxScore = Integer.MIN_VALUE;

					for (int k = 0; k < allLabel.size(); k++)
					{
						SegItem item = new SegItem(itemMatrix[i - 1][k]);
						item.inst.label[i] = curLabelStr;

						Vector<String> feat = item.inst.extractFeaturesFromInstanceInPosition(i);

						String[] newFeat = new String[feat.size()];
						int index = 0;
						for (String str : feat)
						{
							bf.delete(0, bf.length());
							bf.append(str).append(curLabel);
							String featStr = new String(bf);
							newFeat[index++] = featStr;
							featStr = null;
						}

						int[] intVec = model.featVec2IntVec(newFeat);

						item.score += model.getScore(intVec);

						feat = null;
						newFeat = null;
						intVec = null;

						if (maxScore < item.score)
						{
							maxScore = item.score;
							itemMatrix[i][j] = item;
						}
					}
				}

				//set some objects which won't be used  to be null and release some memory
				for (int k = 0; k < itemMatrix[i - 1].length; k++)
				{
					itemMatrix[i - 1][k] = null;
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

		for (int i = 0; i < senLength; i++)
			predLabel[i] = itemMatrix[senLength - 1][maxIndex].inst.label[i];

		//release memory
		for (int i = 0; i < itemMatrix[senLength - 1].length; i++)
			itemMatrix[senLength - 1][i] = null;

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
	public void seg(String rawSen, Vector<String> segResult) throws Exception
	{
		rawSen = rawSen.trim();
		String originalSen = new String(rawSen);

		//when test a raw sentence, convert it to full-width characters 
		rawSen = FullCharConverter.half2Fullchange(rawSen);

		SegInstance inst = new SegInstance(rawSen, segDic);
		String[] resultLabel = new String[inst.sentence.length];
		segViterbiDecode(inst, resultLabel);

		inst = null;

		//the content of result string is still the original sentence
		String[] result = rawSentence2SegSentence(originalSen, resultLabel).split(" ");
		resultLabel = null;

		segResult.clear();
		for (int i = 0; i < result.length; i++)
			segResult.add(result[i]);
		result = null;
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

		String rawSen;
		int count = 0;
		Vector<String> resultVec = new Vector<String>();
		while ((rawSen = br.readLine()) != null)
		{
			count++;
			if (count % 500 == 0)
				logger.info("sentence " + count);

			if (rawSen.trim().equals(""))
				continue;

			resultVec.clear();
			seg(rawSen, resultVec);

			String result = "";
			for (String str : resultVec)
				result += str + " ";
			writer.write(result.trim() + "\n");
		}

		br.close();
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
	 * @param threadNum
	 *            thread number
	 * @throws Exception
	 */
	public void segForFile(final String testFile, final String resultFile, final int threadNum) throws Exception
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
			threadVec[i] = new Thread(new SegViterbi(model, segDic, allLabel));
			threadVec[i].start();
		}

		for (int i = 0; i < threadNum; i++)
			threadVec[i].join();

		br.close();
		wr.close();

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
	private String rawSentence2SegSentence(String rawSen, String[] label) throws Exception
	{
		if (rawSen.length() != label.length)
		{
			throw new Exception("the raw sentence length is not same to label size!");
		}

		StringBuffer result = new StringBuffer();
		result.append(rawSen.charAt(0));

		for (int i = 1; i < label.length; i++)
		{
			if (label[i].equals("B") || label[i].equals("S"))
				result.append(" ").append(rawSen.charAt(i));
			else
				result.append(rawSen.charAt(i));
		}

		String resultStr = new String(result);
		result = null;

		return resultStr;
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

			StringBuffer rawSen = new StringBuffer();
			for (int i = 0; i < gold.length; i++)
				rawSen.append(gold[i]);
			String rawSenStr = new String(rawSen);
			rawSen = null;

			Vector<String> predResult = new Vector<String>();
			seg(rawSenStr, predResult);

			StringBuffer segResult = new StringBuffer();
			for (String str : predResult)
				segResult.append(str).append(" ");

			String resultStr = new String(segResult);
			segResult = null;

			wr.write(resultStr.trim() + "\n");

			String[] result = resultStr.split(" ");
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

			result = null;
			gold = null;

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
		logger.info("load dictionary time: " + (System.currentTimeMillis() - startTime) / 1000 + " s");

		CharType.loadCharType();

		long modelStartTime = System.currentTimeMillis();
		model = OnlineLabelModel.loadModel(modelFile);
		logger.info("load model time: " + (System.currentTimeMillis() - modelStartTime) / 1000 + " s");

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
		String rawSen;

		try
		{
			Logger logger = Logger.getLogger("seg");
			while ((rawSen = br.readLine()) != null)
			{
				sentenceNum++;
				if (0 == sentenceNum % 500)
					logger.info("sentence " + sentenceNum);
				if (rawSen.trim().equals(""))
					continue;

				Vector<String> resultVec = new Vector<String>();
				seg(rawSen.trim(), resultVec);

				StringBuffer result = new StringBuffer();
				for (String str : resultVec)
					result.append(str).append(" ");

				String str = new String(result);
				result = null;
				wr.writerLine(str.trim() + "\n");
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
