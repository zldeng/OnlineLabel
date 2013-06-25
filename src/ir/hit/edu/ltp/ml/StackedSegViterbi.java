package ir.hit.edu.ltp.ml;

import ir.hit.edu.ltp.basic.StackedSegInstance;
import ir.hit.edu.ltp.basic.StackedSegItem;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.util.FullCharConverter;
import ir.hit.edu.ltp.util.MyTools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

import org.apache.log4j.Logger;

public class StackedSegViterbi
{
	public OnlineLabelModel model;
	protected SegDic segDic;
	protected Vector<String> allLabel;
	public SegViterbi baseSegger;

	public StackedSegViterbi(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel, SegViterbi baseSegger)
	{
		this.model = model;
		this.segDic = segDic;
		this.allLabel = allLabel;
		this.baseSegger = baseSegger;
	}

	public StackedSegViterbi()
	{

	}

	public void segStackedViterbiDecode(StackedSegInstance inst, String[] predLabel)
	{
		final int senLength = inst.sentence.length;
		final int labelSize = allLabel.size();
		StackedSegItem[][] itemMatrix = new StackedSegItem[senLength][labelSize];

		StringBuffer bf = new StringBuffer();
		StringBuffer curLabel = new StringBuffer();
		for (int i = 0; i < senLength; i++)
		{
			if (0 == i)
			{
				for (int j = 0; j < allLabel.size(); j++)
				{
					StackedSegInstance tmpInstance = new StackedSegInstance(inst);

					tmpInstance.label[i] = (String) model.featMap.int2Label.get(j);

					Vector<String> featVec = tmpInstance.extractFeaturesFromStackedInstanceInPosition(i);

					String[] newFeat = new String[featVec.size()];

					curLabel.delete(0, curLabel.length());
					curLabel.append("/cL=").append(tmpInstance.label[i]);

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

					itemMatrix[0][j] = new StackedSegItem(score, tmpInstance);
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
						StackedSegItem item = new StackedSegItem(itemMatrix[i - 1][k]);
						item.inst.label[i] = curLabelStr;

						Vector<String> feat = item.inst.extractFeaturesFromStackedInstanceInPosition(i);

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
	}

	public void stackedSeg(String rawSen, Vector<String> segResult) throws Exception
	{
		rawSen = rawSen.trim();
		String originalSen = new String(rawSen);

		//when test a raw sentence, convert it to full-width characters 
		rawSen = FullCharConverter.half2Fullchange(rawSen);

		StackedSegInstance inst = new StackedSegInstance(rawSen, segDic, baseSegger);
		String[] resultLabel = new String[inst.sentence.length];
		segStackedViterbiDecode(inst, resultLabel);

		inst = null;

		//the content of result string is still the original sentence
		String[] result = MyTools.rawSentence2SegSentence(originalSen, resultLabel).split(" ");
		resultLabel = null;

		segResult.clear();
		for (int i = 0; i < result.length; i++)
			segResult.add(result[i]);
		result = null;
	}

	public void loadResource(String stackedModelFile, String baseModelFile, String stackedDicFile, String baseDicFile)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("load resource...");
		long startTime = System.currentTimeMillis();

		segDic = new SegDic();
		segDic.loadSegDic(stackedDicFile);
		logger.info("load dictionary time: " + (System.currentTimeMillis() - startTime) / 1000 + " s");

		CharType.loadCharType();

		//		long modelStartTime = System.currentTimeMillis();
		model = OnlineLabelModel.loadModel(stackedModelFile);
		//		logger.info("load model time: " + (System.currentTimeMillis() - modelStartTime) / 1000 + " s");

		allLabel = new Vector<String>();
		for (int i = 0; i < model.featMap.int2Label.size(); i++)
			allLabel.add((String) model.featMap.int2Label.get(i));

		//load base model
		SegDic baseDic = new SegDic();
		baseDic.loadSegDic(baseDicFile);
		OnlineLabelModel baseModel = OnlineLabelModel.loadModel(baseModelFile);

		baseSegger = new SegViterbi(baseModel, baseDic, allLabel);

		logger.info("load resource over!");
		long endTime = System.currentTimeMillis();
		logger.info("loading source time: " + (endTime - startTime) / 1000 + " s\n");
	}

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
			stackedSeg(rawSenStr, predResult);

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

	public void segForFile(final String testFile, final String resultFile) throws Exception
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testFile), "UTF-8"));
		PrintWriter wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "UTF-8"));

		Logger logger = Logger.getLogger("seg");
		logger.info("begin to test...");
		long startTime = System.currentTimeMillis();
		String line;
		int lineNum = 0;
		while ((line = br.readLine()) != null)
		{
			lineNum++;
			if (lineNum % 500 == 0)
				logger.info("sentence " + lineNum);

			if (line.trim().equals(""))
				continue;
			Vector<String> resultVec = new Vector<String>();
			stackedSeg(line.trim(), resultVec);

			StringBuffer result = new StringBuffer();
			for (String str : resultVec)
				result.append(str).append(" ");

			String str = new String(result);
			result = null;
			wr.write(str.trim() + "\n");
		}
		br.close();
		wr.close();
		long endTime = System.currentTimeMillis();
		logger.info("finish testing!");
		logger.info("test time : " + (endTime - startTime) / 1000 + " s");
	}
}
