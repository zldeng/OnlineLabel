package ir.hit.edu.ltp.ml;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.basic.PosInstance;
import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.dic.PosDic;
import ir.hit.edu.ltp.io.PosIO;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;

/**
 * a class for training POS model using Average Perceptron
 * 
 * @author dzl
 * 
 */
public class PosAP extends PosViterbi
{
	public PosAP(OnlineLabelModel model, PosDic posDic, Vector<String> allLabel)
	{
		super(model, posDic, allLabel);
	}

	public PosAP()
	{

	}

	/**
	 * train a model using Average Perceptron
	 * In each iterator, evaluate the model with dev file and writer the model
	 * to a file
	 * 
	 * @param trainingFile
	 * @param modelFile
	 * @param dicFile
	 * @param iterator
	 * @param devFile
	 * @throws Exception
	 */
	public void PosAPTrain(String trainingFile, String modelFile, String dicFile, int iterator, String devFile,final double compressRatio)
			throws Exception
	{
		Logger logger = Logger.getLogger("pos");

		logger.info("Start to train....");
		long startTime = System.currentTimeMillis();
		allLabel = new Vector<String>();

		logger.info("start to get instances from triang file...");
		Vector<PosInstance> instanceList = PosIO.getPosInstanceFromNormalFile(trainingFile, allLabel);
		logger.info("finish getting instances from traing file!");
		logger.info("There are total " + instanceList.size() + " training instances!\n");

		logger.info("start to get POS dictionary from dictionary file...");
		posDic = new PosDic();
		posDic.loadDic(dicFile);
		logger.info("finish geting POS dictionary!");
		logger.info("There are total " + posDic.size() + " POS pairs in POS dictionary\n");

		logger.info("load char type...");
		CharType.loadCharType();
		logger.info("load char type over!\n");

		Vector<Pipe> posPipeList = new Vector<Pipe>();
		logger.info("start to extract feature from training file...");
		FeatureMap featMap = new FeatureMap(instanceList, allLabel, posDic, posPipeList);
		logger.info("finishing extract feature from training file!\n");

		logger.info("There are total " + featMap.feature2Int.size() + " features!");
		logger.info("There are total " + featMap.label2Int.size() + " labels!\n");

		model = new OnlineLabelModel(featMap);
		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int p = 0; p < instanceList.size(); p++)
		{
			intList.add(p);
		}

		double[] total = new double[featMap.feature2Int.size()];
		for (int i = 0; i < total.length; i++)
			total[i] = 0;

		for (int it = 0; it < iterator; ++it)
		{
			logger.info("start iterator " + it + "...");

			logger.info("Shuffle instance order...");
			final int listSize = intList.size();

			// in each iterator, use training instance in different order
			// in order to reproduce the experiment
			// we can't use real shuffle
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

				Vector<String> sentence = instanceList.elementAt(inst).words;
				Vector<String> predLabel = new Vector<String>();
				posViterbiDecode(sentence, predLabel);

				if (predLabel.size() != sentence.size())
				{
					logger.error("predicted size is not same to gold size, pre size: " + predLabel.size()
							+ " sen size: " + sentence.size());
					throw new Exception("When decoding " + (inst + 1)
							+ " instance , the number of result POS is not the same to the number of words!");
				}

				// if the prediction is correct, next instance
				if (predLabel.equals(instanceList.elementAt(inst).label))
					continue;

				PosInstance preInstance = new PosInstance(sentence, predLabel);
				
				// get feature vector of predicted result
				Pipe predPipe = new Pipe(preInstance, model.featMap.feature2Int, posDic);

				if (posPipeList.elementAt(inst).feature.size() != predPipe.feature.size())
				{
					throw new Exception(
							"When decoding, the feature number of result POS is not the same to the gold number!");
				}

				// update model parameter and total parameter according to
				// current instance
				model.update(posPipeList.elementAt(inst).feature, predPipe.feature);
				model.addToTotal(total);
			}
			logger.info("finish iterator " + it + "\n");

			// output model in each iterator
			// user can get the best model according the evaluation result
			// get current model
			OnlineLabelModel tmpModel = new OnlineLabelModel(featMap);
			for (int i = 0; i < featMap.feature2Int.size(); i++)
			{
				tmpModel.parameter[i] = total[i] / (instanceList.size() * (it + 1));
			}		
			
			PosAP tmpPosTagger = new PosAP(tmpModel, posDic, allLabel);
			
			// evaluate current model with dev file
			logger.info("Evaluate model for dev file with uncompressed model...");
			double devPre = tmpPosTagger.evalPos(devFile, it);
			logger.info("the POS precision of uncompressed model for dev file is: " + devPre + "\n");
			
			logger.info("writer model to file...\n");
			tmpModel.writerModel(modelFile + "-it-" + it,compressRatio);
			
			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpPosTagger = new PosAP(compressedModel, posDic, allLabel);
				logger.info("Evaluate compressed model for dev file with compressed model...");
				devPre = tmpPosTagger.evalPos(devFile, it);
				logger.info("the POS precision of compressed model for dev file is: " + devPre + "\n");
			}
			
		}

		logger.info("train over!");
		long endTime = System.currentTimeMillis();

		logger.info("training time: " + (endTime - startTime) / 1000 + " s" + "\n");
	}
}
