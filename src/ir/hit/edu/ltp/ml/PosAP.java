package ir.hit.edu.ltp.ml;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.basic.PosInstance;
import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.dic.PosDic;
import ir.hit.edu.ltp.io.PosIO;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.util.MyTools;

/**
 * a class for training POS model using Average Perceptron
 * 
 * @author dzl
 * 
 */
public class PosAP extends PosViterbi implements Callable<Object[]>
{
	private Vector<PosInstance> instanceList;
	private Vector<Pipe> posPipeList;
	private static int threadNum;
	private int id;
	private CountDownLatch finishSigle;

	public PosAP(OnlineLabelModel model, PosDic posDic, Vector<String> allLabel)
	{
		super(model, posDic, allLabel);
	}

	public PosAP()
	{

	}

	public PosAP(OnlineLabelModel model, PosDic posDic, Vector<String> allLabel, int id,
			Vector<PosInstance> instanceList, Vector<Pipe> posPipeList, CountDownLatch finishSigle)
	{
		this.model = new OnlineLabelModel(model.featMap);
		for (int i = 0; i < model.parameter.length; i++)
			this.model.parameter[i] = model.parameter[i];

		this.posDic = posDic;
		this.allLabel = allLabel;
		this.instanceList = instanceList;
		this.posPipeList = posPipeList;
		this.id = id;
		this.finishSigle = finishSigle;
	}

	//if the thread number is only one,just use old training function. we can save some time by avoiding managing thread
	//if the thread number is more than one, then use another training function
	public void PosAPTrain(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio, final int threadNum)
			throws Exception
	{
		if (threadNum <= 0)
		{
			throw new Exception("thread number must be a int value and it must be more than 0");
		}

		if (1 == threadNum)
			PosAPTrainWithOneThread(trainingFile, modelFile, dicFile, iterator, devFile, compressRatio);
		else
			PosAPTrainWithMultiThreads(trainingFile, modelFile, dicFile, iterator, devFile, compressRatio, threadNum);

	}

	/**
	 * train a model using Average Perceptron in one thread
	 * In each iterator, evaluate the model with development file and writer the
	 * model to a file
	 * 
	 * @param trainingFile
	 * @param modelFile
	 * @param dicFile
	 * @param iterator
	 * @param devFile
	 * @throws Exception
	 */
	private void PosAPTrainWithOneThread(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio)
			throws Exception
	{
		Logger logger = Logger.getLogger("pos");

		logger.info("Start to train....");
		long startTime = System.currentTimeMillis();

		//load training instance and transform them to PosInstance, then initialize dictionary, model
		loadInstanceAndInit(trainingFile, dicFile);

		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int p = 0; p < instanceList.size(); p++)
		{
			intList.add(p);
		}

		//the variable is used to keep each model
		double[] total = new double[model.featMap.feature2Int.size()];

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

				String[] sentence = instanceList.elementAt(inst).words;
				Vector<String> predLabel = new Vector<String>();
				pos(sentence, predLabel);

				if (predLabel.size() != sentence.length)
				{
					logger.error("predicted size is not same to gold size, pre size: " + predLabel.size()
							+ " sen size: " + sentence.length);
					throw new Exception("When decoding " + (inst + 1)
							+ " instance , the number of result POS is not the same to the number of words!");
				}

				String[] predArray = new String[predLabel.size()];
				for (int m = 0; m < predArray.length; m++)
					predArray[m] = predLabel.elementAt(m);

				// if the prediction is correct, next instance
				if (predArray.equals(instanceList.elementAt(inst).label))
				{
					sentence = null;
					predLabel = null;
					predArray = null;
					continue;
				}
				PosInstance preInstance = new PosInstance(sentence, predArray);

				// get feature vector of predicted result
				Pipe predPipe = new Pipe(preInstance, model.featMap.feature2Int, posDic);

				if (posPipeList.elementAt(inst).feature.length != predPipe.feature.length)
				{
					throw new Exception(
							"When decoding, the feature number of result POS is not the same to the gold number!");
				}

				// update model parameter and total parameter according to
				// current instance
				model.update(posPipeList.elementAt(inst).feature, predPipe.feature);

				predArray = null;
				preInstance = null;
				predLabel = null;
				predPipe = null;

				model.addToTotal(total);
			}
			logger.info("finish iterator " + it + "\n");

			// output model in each iterator
			// user can get the best model according the evaluation result
			// get current model
			OnlineLabelModel tmpModel = new OnlineLabelModel(model.featMap);
			for (int i = 0; i < model.featMap.feature2Int.size(); i++)
			{
				tmpModel.parameter[i] = (float) (total[i] / (instanceList.size() * (it + 1)));
				tmpModel.useNum[i] = model.useNum[i];
			}

			PosAP tmpPosTagger = new PosAP(tmpModel, posDic, allLabel);

			// evaluate current model with dev file
			logger.info("Evaluate model for dev file with uncompressed model...");
			double devPre = tmpPosTagger.evalPos(devFile, it);
			logger.info("the POS precision of uncompressed model for dev file is: " + devPre + "\n");

			logger.info("writer model to file...\n");
			tmpModel.writerModel(modelFile + "-it-" + it, compressRatio);
			tmpModel = null;

			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpPosTagger = new PosAP(compressedModel, posDic, allLabel);
				logger.info("Evaluate compressed model for dev file with compressed model...");
				devPre = tmpPosTagger.evalPos(devFile, it);
				logger.info("the POS precision of compressed model for dev file is: " + devPre + "\n");
				compressedModel = null;
			}

			tmpPosTagger = null;

		}

		logger.info("train over!");
		long endTime = System.currentTimeMillis();

		logger.info("training time: " + (endTime - startTime) / 1000 + " s" + "\n");
	}

	private void loadInstanceAndInit(final String trainingFile, final String dicFile) throws Exception
	{
		Logger logger = Logger.getLogger("pos");

		logger.info("Start to load training instance and initialize model...");

		instanceList = new Vector<PosInstance>();
		posPipeList = new Vector<Pipe>();

		allLabel = new Vector<String>();

		logger.info("start to get instances from triang file...");
		instanceList = PosIO.getPosInstanceFromNormalFile(trainingFile, allLabel);

		System.out.println(allLabel);

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

		posPipeList = new Vector<Pipe>();
		logger.info("start to extract feature from training file...");
		FeatureMap featMap = new FeatureMap(instanceList, allLabel, posDic, posPipeList);
		logger.info("finishing extract feature from training file!\n");

		logger.info("There are total " + featMap.feature2Int.size() + " features!");
		logger.info("There are total " + featMap.label2Int.size() + " labels!\n");

		model = new OnlineLabelModel(featMap);

		logger.info("load instance and initialize model over!");
	}

	/**
	 * training model using Structure Perceptron in Multiple threads
	 * when using multiple threads, just use Structure Perceptron
	 * 
	 * @param trainingFile
	 * @param modelFile
	 * @param dicFile
	 * @param iterator
	 * @param devFile
	 * @param compressRatio
	 * @param threadNum
	 * @throws Exception
	 */
	private void PosAPTrainWithMultiThreads(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio, final int threadNum)
			throws Exception
	{
		Logger logger = Logger.getLogger("pos");

		logger.info("Start to train....");
		long startTime = System.currentTimeMillis();
		//load training instance and transform them to PosInstance, then initialize dictionary, allLabel and model
		loadInstanceAndInit(trainingFile, dicFile);

		//set thread number, this is a static variable
		PosAP.threadNum = threadNum;
		logger.info("each thread almost uses " + instanceList.size() / threadNum + " training instances");

		for (int it = 0; it < iterator; it++)
		{
			logger.info("start iterator " + it + " ...");

			ExecutorService exec = Executors.newCachedThreadPool();
			ArrayList<Future<Object[]>> results = new ArrayList<Future<Object[]>>();

			CountDownLatch finishSigle = new CountDownLatch(threadNum);
			//create threaNum threads and submit them
			for (int id = 0; id < threadNum; id++)
			{
				//because the PosAP extends from PosViterbi and PosViterbi has implemented Runnable interface
				//in ExecutorService, there are two submit methods, one uses a Runnable task as parameter and another uses a Callable task as parameter
				//so we must use a cast to tell submit that we want use the submit which use a Callable parameter
				results.add(exec.submit((Callable<Object[]>) new PosAP(model, posDic, allLabel, id, instanceList,
						posPipeList, finishSigle)));
			}

			//wait until all sub-thread are finished
			finishSigle.await();
			logger.info("sub-threads are all finished!");
			logger.info("merge parameter and use number...");
			Vector<float[]> paraVec = new Vector<float[]>();
			Vector<long[]> useVec = new Vector<long[]>();

			for (Future<Object[]> fs : results)
			{
				Object[] tmp = fs.get();
				paraVec.add((float[]) tmp[0]);
				useVec.add((long[]) tmp[1]);

			}
			float[] tmpPara = MyTools.mixParameter(paraVec);
			long[] useNum = MyTools.mixUseNum(useVec);
			logger.info("finish merging parameter and useNum!");
			
			for (int i = 0;i < useNum.length;i++)
				model.useNum[i] += useNum[i];

			OnlineLabelModel tmpModel = new OnlineLabelModel(model.featMap, tmpPara, model.useNum);
			PosAP tmpPosTagger = new PosAP(tmpModel, posDic, allLabel);

			// evaluate current model with development file
			logger.info("Evaluate model for dev file with uncompressed model...");
			double devPre = tmpPosTagger.evalPos(devFile, it);
			logger.info("the POS precision of uncompressed model for dev file is: " + devPre + "\n");

			logger.info("writer model to file...\n");
			tmpModel.writerModel(modelFile + "-it-" + it, compressRatio);

			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpPosTagger = new PosAP(compressedModel, posDic, allLabel);
				logger.info("Evaluate compressed model for dev file with compressed model...");
				devPre = tmpPosTagger.evalPos(devFile, it);
				logger.info("the POS precision of compressed model for dev file is: " + devPre + "\n");
			}

			model.parameter = tmpPara;
			logger.info("iterator " + it + " over!");
		}

		long endTime = System.currentTimeMillis();
		logger.info("training over!");
		logger.info("training time: " + (endTime - startTime) / 1000 + " s\n");
	}

	private Object[] trainWithSubInstance() throws Exception
	{
		Logger logger = Logger.getLogger("pos");
		logger.info("training in thread " + id + " start...");

		int num = instanceList.size() / threadNum;
		int startIndex, endIndex;

		//for each thread, it only uses instance from startIndex to endIndex
		if (id < threadNum - 1)
		{
			startIndex = num * id;
			endIndex = startIndex + num - 1;
		}
		else
		{
			startIndex = num * (threadNum - 1);
			endIndex = instanceList.size() - 1;
		}

		for (int instIndex = startIndex; instIndex <= endIndex; instIndex++)
		{
			if (((instIndex - startIndex + 1)) % 500 == 0)
				logger.info("thread " + id + ": " + (instIndex - startIndex + 1));

			String[] sentence = instanceList.elementAt(instIndex).words;
			Vector<String> predLabel = new Vector<String>();
			pos(sentence, predLabel);

			if (predLabel.size() != sentence.length)
			{
				throw new Exception("When decoding " + (instIndex + 1)
						+ " instance , the number of result POS is not the same to the number of words!");
			}

			// if the prediction is correct, next instance
			if (predLabel.equals(instanceList.elementAt(instIndex).label))
				continue;

			String[] predArray = new String[predLabel.size()];
			for (int m = 0; m < predArray.length; m++)
				predArray[m] = predLabel.elementAt(m);

			PosInstance preInstance = new PosInstance(sentence, predArray);

			// get feature vector of predicted result
			Pipe predPipe = new Pipe(preInstance, model.featMap.feature2Int, posDic);

			//update parameter according current instance
			model.update(posPipeList.elementAt(instIndex).feature, predPipe.feature);
			//			model.addToTotal(tmpTotal);
		}

		logger.info("thread " + id + " finish!");
		Object[] result = new Object[2];
		result[0] = model.parameter;
		result[1] = model.useNum;
		//tell the thread manager, this thread has finished it's work
		finishSigle.countDown();

		return result;
	}

	@Override
	public Object[] call() throws Exception
	{
		return trainWithSubInstance();
	}
}
