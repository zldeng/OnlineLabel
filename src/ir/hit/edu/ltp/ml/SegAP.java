package ir.hit.edu.ltp.ml;

import ir.hit.edu.ltp.basic.PosInstance;
import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.dic.PosDic;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.io.PosIO;
import ir.hit.edu.ltp.io.SegIO;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

public class SegAP extends SegViterbi
{
	Vector<SegInstance> instList;
	Vector<Pipe> segPipeList;
	
	public SegAP(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel)
	{
		super(model, segDic, allLabel);
	}

	public SegAP()
	{

	}

	@SuppressWarnings("unchecked")
	public void segAPTrain(String trainingFile, String modelFile, String dicFile, int iterator, String devFile,final double compressRatio)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("training start...");
		long startTime = System.currentTimeMillis();
	
		loadInstanceAndInit(trainingFile, dicFile);
		
		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int p = 0; p < instList.size(); p++)
		{
			intList.add(p);
		}

		double[] total = new double[model.parameter.length];
		for (int i = 0; i < total.length; i++)
			total[i] = 0;

		for (int it = 0; it < iterator; ++it)
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

				SegInstance tmpInst = instList.elementAt(inst);
				Vector<String> predLabel = new Vector<String>();

				segViterbiDecode(tmpInst, predLabel);

				if (predLabel.size() != tmpInst.sentence.size())
				{
					logger.error("gold size: " + tmpInst.sentence.size() + " pred size: " + predLabel.size());
					throw new Exception("When decoding " + (inst + 1)
							+ " instance , the number of result label is not the same to the number of word!");
				}

				if (predLabel.equals(instList.elementAt(inst).label))
					continue;

				SegInstance predInstance = new SegInstance(tmpInst);
				predInstance.label.clear();
				for (int i = 0; i < predLabel.size(); i++)
					predInstance.label.add(predLabel.elementAt(i));

				Pipe predPipe = new Pipe(predInstance, model.featMap.feature2Int);

				if (predPipe.feature.size() != segPipeList.elementAt(inst).feature.size())
				{
					throw new Exception(
							"When decoding, the feature number of result label is not the same to the gold number!");
				}

				model.update(segPipeList.elementAt(inst).feature, predPipe.feature);
				model.addToTotal(total);

			}

			logger.info("finish iterator " + it + "\n");

			OnlineLabelModel tmpModel = new OnlineLabelModel(model.featMap);
			for (int i = 0; i < tmpModel.parameter.length; i++)
			{
				tmpModel.parameter[i] = total[i] / (instList.size() * (it + 1));
			}

			SegAP tmpSeg = new SegAP(tmpModel, segDic, allLabel);

			logger.info("evaluate uncompressed model with dev file...");
			double[] performance = tmpSeg.evalSeg(devFile, it);
			double precision = performance[0];
			double recall = performance[1];
			logger.info("the evaluation result of uncompressed P: " + precision + " R: " + recall + " F: " + (2 * precision * recall)
					/ (precision + recall) + "\n");

			logger.info("writer model to file...\n");
			tmpSeg.model.writerModel(modelFile + "-it-" + it,compressRatio);
			
			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpSeg = new SegAP(compressedModel, segDic, allLabel);
				logger.info("evaluate compressed model with dev file...");
				performance = tmpSeg.evalSeg(devFile, it);
				precision = performance[0];
				recall = performance[1];
				logger.info("the evaluation result of conpressed model P: " + precision + " R: " + recall + " F: " + (2 * precision * recall)
						/ (precision + recall) + "\n");
			}
			
		}

		logger.info("training over!");
		long endTime = System.currentTimeMillis();

		logger.info("training time: " + (endTime - startTime) / 1000 + " s" + "\n");

	}
	
	private void loadInstanceAndInit(final String trainingFile, final String dicFile) throws Exception
	{
		Logger logger = Logger.getLogger("seg");

		logger.info("Start to load training instance and initialize model...");

		logger.info("start to get seg dictionary from dictionary file...");
		segDic = new SegDic();
		segDic.loadSegDic(dicFile);

		logger.info("finish geting seg dictionary!");
		logger.info("There are total " + segDic.size() + " words in segmentation dictionary\n");

		logger.info("load char type...");
		CharType.loadCharType();
		logger.info("load char type over!\n");

		allLabel = new Vector<String>();
		allLabel.add("B");
		allLabel.add("M");
		allLabel.add("E");
		allLabel.add("S");

		logger.info("load instance from training file...");
		instList = SegIO.getSegInstanceFromNormalFile(trainingFile, segDic);
		logger.info("instance number: " + instList.size());

		segPipeList = new Vector<Pipe>();
		logger.info("extract features from instance and map SegInstance to SegPipe...");
		FeatureMap featMap = new FeatureMap(instList, allLabel, segPipeList);
		logger.info("there are total " + featMap.feature2Int.size() + " features");
		logger.info("there are total " + featMap.label2Int.size() + " labels\n");

		model = new OnlineLabelModel(featMap);
		logger.info("load instance and initialize model over!");

	}
}
