package ir.hit.edu.ltp.ml;

import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.basic.StackedSegInstance;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.io.SegIO;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

public class StackedSegAP extends StackedSegViterbi
{
	private Vector<StackedSegInstance> instanceList;
	private Vector<Pipe> segPipeList;

	public StackedSegAP(OnlineLabelModel finalmodel, SegDic segDic, Vector<String> allLabel, SegViterbi baseSegger)
	{
		super(finalmodel, segDic, allLabel, baseSegger);
	}

	public StackedSegAP()
	{

	}

	public void loadAndInit(final String trainingFile, final String stackedDicFile, final String baseModelFile, final String baseDicFile)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");

		logger.info("Start to load training instance and initialize model...");

		logger.info("start to get seg dictionary from stacked dictionary file...");
		segDic = new SegDic();
		segDic.loadSegDic(stackedDicFile);
		logger.info("finish geting stacked dictionary!");
		logger.info("There are total " + segDic.size() + " words in stacked dictionary\n");

		logger.info("load char type...");
		CharType.loadCharType();
		logger.info("load char type over!\n");

		allLabel = new Vector<String>();
		allLabel.add("B");
		allLabel.add("M");
		allLabel.add("E");
		allLabel.add("S");

		logger.info("start to get base resource...");
		SegDic baseDic = new SegDic();
		baseDic.loadSegDic(baseDicFile);
		OnlineLabelModel baseModel = OnlineLabelModel.loadModel(baseModelFile);
		baseSegger = new SegViterbi(baseModel, baseDic, allLabel);
		logger.info("load base resource over!");

		logger.info("load instance from training file...");
		instanceList = SegIO.getStackedSegInstanceFromNormalFile(trainingFile, segDic, baseSegger);
		logger.info("instance number: " + instanceList.size());

		FeatureMap featMap = new FeatureMap();
		segPipeList = new Vector<Pipe>();
		logger.info("extract features from instance and map SegInstance to SegPipe...");
		featMap.initWithStackedInstance(instanceList, allLabel, segPipeList);
		logger.info("there are total " + featMap.feature2Int.size() + " features");
		logger.info("there are total " + featMap.label2Int.size() + " labels\n");

		model = new OnlineLabelModel(featMap);
		logger.info("load instance and initialize model over!");
	}

	public void stackedSegAPTrain(String trainingFile, String stackedModelFile, String baseModelFile, String stackedDicFile, String baseDicFile, int iterator, String devFile, final double compressRatio)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("training start...");
		long startTime = System.currentTimeMillis();

		loadAndInit(trainingFile, stackedDicFile, baseModelFile, baseDicFile);

		double[] total = new double[model.parameter.length];

		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int p = 0; p < instanceList.size(); p++)
		{
			intList.add(p);
		}

		for (int it = 0; it < iterator; it++)
		{
			logger.info("start iterator " + it + "...");

			logger.info("Shuffle instance order...");

			// in each iterator, use training instance in different order
			// in order to reproduce the experiment result
			// we don't use real shuffle
			final int listSize = intList.size();
			for (int i = 0; i < listSize / 2; i++)
			{
				int tmp = intList.indexOf(i);
				intList.set(i, intList.indexOf(listSize - 1 - i));
				intList.set(listSize - 1 - i, tmp);
			}

			for (int q = 0; q < intList.size(); q++)
			{
				if ((q + 1) % 500 == 0)
				{
					logger.info("instance " + (q + 1));
				}

				int inst = intList.indexOf(q);

				StackedSegInstance tmpInst = instanceList.elementAt(inst);
				String[] predLabel = new String[tmpInst.label.length];

				segStackedViterbiDecode(tmpInst, predLabel);

				if (predLabel.equals(instanceList.elementAt(inst).label))
				{
					tmpInst = null;
					predLabel = null;
					continue;
				}
				StackedSegInstance predInstance = new StackedSegInstance(tmpInst);
				predInstance.label = predLabel;

				tmpInst = null;
				predLabel = null;

				Pipe predPipe = new Pipe(predInstance, model.featMap.feature2Int);

				if (predPipe.feature.length != segPipeList.elementAt(inst).feature.length)
				{
					throw new Exception(
							"When decoding, the feature number of result label is not the same to the gold number!");
				}

				model.update(segPipeList.elementAt(inst).feature, predPipe.feature);

				predPipe = null;
				predInstance = null;

				model.addToTotal(total);
			}

			logger.info("finish iterator " + it + "\n");

			OnlineLabelModel tmpModel = new OnlineLabelModel(model.featMap);
			for (int i = 0; i < tmpModel.parameter.length; i++)
			{
				tmpModel.parameter[i] = (float) (total[i] / (instanceList.size() * (it + 1)));
				tmpModel.useNum[i] = model.useNum[i];
			}

			StackedSegAP stackedSegger = new StackedSegAP(tmpModel, segDic, allLabel, baseSegger);
			logger.info("evaluate uncompressed model with dev file...");

			double[] performance = stackedSegger.evalSeg(devFile, it);
			double precision = performance[0];
			double recall = performance[1];
			logger.info("the evaluation result of uncompressed P: " + precision + " R: " + recall + " F: "
					+ (2 * precision * recall) / (precision + recall) + "\n");

			logger.info("writer model to file...\n");
			tmpModel.writerModel(stackedModelFile + "-it-" + it, compressRatio);

			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(stackedModelFile + "-it-" + it);
				stackedSegger = new StackedSegAP(compressedModel, segDic, allLabel, baseSegger);
				logger.info("evaluate compressed model with dev file...");
				performance = stackedSegger.evalSeg(devFile, it);
				precision = performance[0];
				recall = performance[1];
				logger.info("the evaluation result of conpressed model P: " + precision + " R: " + recall + " F: "
						+ (2 * precision * recall) / (precision + recall) + "\n");
			}

			tmpModel = null;
			stackedSegger = null;
		}

		logger.info("training over!");
		long endTime = System.currentTimeMillis();
		logger.info("training time: " + (endTime - startTime) / 1000 + " S");
	}
}
