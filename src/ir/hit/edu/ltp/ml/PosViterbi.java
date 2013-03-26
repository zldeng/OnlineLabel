package ir.hit.edu.ltp.ml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import org.apache.log4j.Logger;

import ir.hit.edu.ltp.basic.*;
import ir.hit.edu.ltp.model.*;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.dic.*;
import ir.hit.edu.ltp.util.FullCharConverter;
import ir.hit.edu.ltp.util.InputStreams;
import ir.hit.edu.ltp.util.OutputStreams;

/**
 * a base class which using Viterbi algorithm to decode
 * 
 * @author dzl
 * 
 */
public class PosViterbi implements Runnable
{
	protected OnlineLabelModel model;
	protected PosDic posDic;
	protected Vector<String> allLabel;

	//the three variables are use for Multi-Thread
	private static InputStreams br;
	private static OutputStreams wr;
	private static int sentenceNum = 0;

	public PosViterbi(OnlineLabelModel model, PosDic posDic, Vector<String> allLabel)
	{
		this.model = model;
		this.posDic = posDic;
		this.allLabel = allLabel;
	}

	public PosViterbi()
	{}

	public double posViterbiDecode(Vector<String> originSentence, Vector<String> predLable) throws UnsupportedEncodingException
	{		
		Vector<String> sentence = new Vector<String>();
		for (String str: originSentence)
			sentence.add(FullCharConverter.half2Fullchange(str));
				
		final int wordsNum = sentence.size();
		Vector<Vector<PosItem>> itemVector = new Vector<Vector<PosItem>>();
		for (int i = 0; i < sentence.size(); i++)
			itemVector.add(new Vector<PosItem>());

		for (int i = 0; i < wordsNum; ++i)
		{
			String word = sentence.elementAt(i);

			// current word is the the first word in the sentence
			if (i == 0)
			{
				// if the word appears in POS dictionary, just give it the
				// POS in dic, or try all possible POS
				Vector<String> candidatePos = posDic.containsKey(word) ? posDic.getPos(word) : allLabel;

				for (int p = 0; p < candidatePos.size(); p++)
				{
					String pos = candidatePos.elementAt(p);

					Vector<String> tmpLabel = new Vector<String>();
					tmpLabel.add(pos);
					PosInstance inst = new PosInstance(sentence, tmpLabel);
					Vector<String> feature = inst.extractFeaturesFromInstanceInPosition(0, posDic);

					String curLabel = "/curLabel=" + pos;
					Vector<String> newFeat = new Vector<String>();
					for (int m = 0; m < feature.size(); m++)
					{
						newFeat.add(feature.elementAt(m) + curLabel);
					}

					Vector<Integer> featInt = model.featVec2IntVec(newFeat);
					double score = model.getScore(featInt);

					PosItem item = new PosItem(score, sentence, tmpLabel);
					itemVector.elementAt(0).add(item);
				}
			}
			else
			{
				Vector<String> candidatePos = posDic.containsKey(word) ? posDic.getPos(word) : allLabel;
				for (int j = 0; j < candidatePos.size(); j++)
				{
					String newPos = candidatePos.elementAt(j);
					double maxScore = Integer.MIN_VALUE;

					PosItem initItem = new PosItem(maxScore, sentence, new Vector<String>());
					itemVector.elementAt(i).add(initItem);

					int preNum = itemVector.elementAt(i - 1).size();
					for (int k = 0; k < preNum; k++)
					{
						@SuppressWarnings("unchecked")
						Vector<String> preLabel = (Vector<String>) itemVector.elementAt(i - 1).elementAt(k).inst.label
								.clone();
						preLabel.add(newPos);

						PosInstance tmpInst = new PosInstance(sentence, preLabel);

						Vector<String> featVec = tmpInst.extractFeaturesFromInstanceInPosition(i, posDic);

						String curLabel = "/curLabel=" + newPos;
						Vector<String> newFeat = new Vector<String>();
						for (int m = 0; m < featVec.size(); m++)
						{
							newFeat.add(featVec.elementAt(m) + curLabel);
						}

						Vector<Integer> intVec = model.featVec2IntVec(newFeat);

						double tmpScore = itemVector.elementAt(i - 1).get(k).score + model.getScore(intVec);

						if (maxScore < tmpScore)
						{
							maxScore = tmpScore;
							PosItem item = new PosItem(tmpScore, sentence, preLabel);
							itemVector.elementAt(i).set(j, item);
						}
					}
				}
			}
		}

		// get result
		int maxIndex = 0;
		double tmpMaxScore = itemVector.elementAt(wordsNum - 1).get(0).score;
		for (int i = 1; i < itemVector.elementAt(wordsNum - 1).size(); i++)
		{
			if (itemVector.elementAt(wordsNum - 1).get(i).score > tmpMaxScore)
			{
				tmpMaxScore = itemVector.elementAt(wordsNum - 1).get(i).score;
				maxIndex = i;
			}
		}

		Vector<String> result = itemVector.elementAt(wordsNum - 1).get(maxIndex).inst.label;
		predLable.clear();
		for (int i = 0; i < result.size(); i++)
			predLable.add(result.elementAt(i));

		return tmpMaxScore;
	}

	/**
	 * POS for a segmented file
	 * each sentence is in a line words are separated with blank space
	 * the function can use only one thread
	 * 
	 * @param testFile
	 * @param resultFile
	 * @throws Exception
	 */
	public void PosForFile(String testFile, String resultFile) throws Exception
	{
		Logger logger = Logger.getLogger("pos");
		if (model == null)
		{
			logger.error("Model is null,you should firstly train a model");
			throw new Exception("Model is null,you should firstly train a model");
		}

		logger.info("begin to test...");
		long startTime = System.currentTimeMillis();

		InputStreamReader is = new InputStreamReader(new FileInputStream(testFile), "UTF-8");
		BufferedReader br = new BufferedReader(is);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));

		int num = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			num++;
			if ((num + 1) % 200 == 0)
			{
				logger.info("sentence " + num);
			}

			if (line.trim().equals(""))
				continue;
			String[] sentence = line.trim().split(" ");
			Vector<String> words = new Vector<String>();
			for (int i = 0; i < sentence.length; ++i)
				words.add(sentence[i].trim());

			Vector<String> result = new Vector<String>();

			posViterbiDecode(words, result);
			String resultStr = "";
			for (int i = 0; i < words.size(); i++)
				resultStr += words.elementAt(i) + "_" + result.elementAt(i) + " ";

			writer.write(resultStr.trim() + "\n");
		}

		writer.flush();
		writer.close();

		logger.info("testing over!");
		long endTime = System.currentTimeMillis();
		logger.info("testing time: " + (endTime - startTime) / 1000 + " s" + "\n");
	}

	/**
	 * POSTagger for a segmented file
	 * each sentence is in a line words are separated with blank space
	 * we can use Multi-Thread by setting threadNum
	 * 
	 * @param testFile
	 * @param resultFile
	 * @param threadNum
	 *            thread number
	 * @throws Exception
	 */
	public void PosForFile(final String testFile, final String resultFile, final int threadNum) throws Exception
	{
		br = new InputStreams(testFile);
		wr = new OutputStreams(resultFile);

		Logger logger = Logger.getLogger("pos");
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
			threadVec[i] = new Thread(new PosViterbi(model, posDic, allLabel));
			threadVec[i].start();
		}

		for (int i = 0; i < threadNum; i++)
			threadVec[i].join();

		logger.info("test finish!");
		long endTime = System.currentTimeMillis();
		logger.info("test time: " + (endTime - startTime) / 1000 + " s" + "\n");
	}

	/**
	 * evaluate model with test file
	 * only used in training
	 * the test file format is: word1_POS1 word2_POS2
	 * 
	 * @param testFile
	 * @param it
	 * @return
	 * @throws Exception
	 */
	protected double evalPos(String testFile, int it) throws Exception
	{
		InputStreamReader is = new InputStreamReader(new FileInputStream(testFile), "UTF-8");
		BufferedReader br = new BufferedReader(is);

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(testFile + "-" + it
				+ ".result"), "UTF-8"));

		int total = 0, correct = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			if (line.trim().equals(""))
				continue;

			String[] token = line.trim().split(" ");
			Vector<String> sentence = new Vector<String>();
			Vector<String> goldLabel = new Vector<String>();

			for (int i = 0; i < token.length; i++)
			{
				String[] pair = token[i].split("_");
				sentence.add(pair[0]);
				goldLabel.add(pair[1]);
			}

			Vector<String> predLabel = new Vector<String>();
			posViterbiDecode(sentence, predLabel);

			String str = "";
			for (int i = 0; i < sentence.size(); i++)
			{
				str += sentence.elementAt(i) + "_" + predLabel.elementAt(i) + " ";
			}
			writer.write(str.trim() + "\n");

			if (goldLabel.size() != predLabel.size())
			{
				throw new Exception("When test, the size of goldLabel is not equal to predLabel!");
			}

			total += goldLabel.size();

			for (int i = 0; i < predLabel.size(); i++)
			{
				if (goldLabel.elementAt(i).trim().equals(predLabel.elementAt(i).trim()))
					correct++;
			}
		}

		writer.flush();
		writer.close();

		double precision = (correct * 100.0) / total;
		return precision;
	}

	/**
	 * load resource
	 * when testing,we need load model, POS dictionary and initialize charType
	 * 
	 * @param modelFile
	 * @param dicFile
	 * @throws Exception
	 */
	public void loadResource(String modelFile, String dicFile) throws Exception
	{
		Logger logger = Logger.getLogger("pos");
		logger.info("load resource...");
		long startTime = System.currentTimeMillis();

		model = OnlineLabelModel.loadModel(modelFile);
		allLabel = new Vector<String>();
		for (int i = 0; i < model.featMap.int2Label.size(); i++)
			allLabel.add((String) model.featMap.int2Label.get(i));
		posDic = new PosDic();
		posDic.loadDic(dicFile);
		CharType.loadCharType();

		logger.info("load resource over!");
		long endTime = System.currentTimeMillis();
		logger.info("loading source time: " + (endTime - startTime) / 1000 + " s\n");
	}

	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		String line;
		try
		{
			Logger logger = Logger.getLogger("pos");
			while ((line = br.readLine()) != null)
			{
				sentenceNum++;

				if (0 == sentenceNum % 300)
					logger.info("sentence " + sentenceNum);

				if (line.trim().equals(""))
					continue;

				String[] sentence = line.trim().split(" ");
				Vector<String> words = new Vector<String>();
				for (int i = 0; i < sentence.length; ++i)
					words.add(sentence[i].trim());

				Vector<String> result = new Vector<String>();

				posViterbiDecode(words, result);
				String resultStr = "";
				for (int i = 0; i < words.size(); i++)
					resultStr += words.elementAt(i) + "_" + result.elementAt(i) + " ";

				wr.writerLine(resultStr.trim() + "\n");
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
