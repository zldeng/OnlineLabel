package ir.hit.edu.ltp.ml;

import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.io.SegIO;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.util.MyTools;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

public class SegAP extends SegViterbi implements Callable<Object[]>
{
	private Vector<SegInstance> instanceList;
	private Vector<Pipe> segPipeList;
	private static int threadNum;
	private int id;
	private CountDownLatch finishSigle;

	public SegAP(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel)
	{
		super(model, segDic, allLabel);
	}

	public SegAP()
	{

	}

	public SegAP(OnlineLabelModel model, SegDic segDic, Vector<String> allLabel, int id,
			Vector<SegInstance> instanceList, Vector<Pipe> segPipeList, CountDownLatch finishSigle)
	{
		this.model = new OnlineLabelModel(model.featMap);
		for (int i = 0; i < model.parameter.length; i++)
			this.model.parameter[i] = model.parameter[i];

		this.segDic = segDic;
		this.allLabel = allLabel;
		this.instanceList = instanceList;
		this.segPipeList = segPipeList;
		this.id = id;
		this.finishSigle = finishSigle;
	}

	//we decide to use which function to train a model by the thread number
	//when the thread number is only one,just use old function and we can save some time by avoiding  import thread manager
	public void segAPTrain(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio, final int threadNum)
			throws Exception
	{
		if (threadNum <= 0)
		{
			throw new Exception("thread number must be a int and it must more than zero");
		}
		if (1 == threadNum)
			segAPTrainWithOneThread(trainingFile, modelFile, dicFile, iterator, devFile, compressRatio);
		else
			segAPTrainWithMultiThreads(trainingFile, modelFile, dicFile, iterator, devFile, compressRatio, threadNum);
	}

	/**
	 * if the thread number is only one, we use Average Perceptron
	 * 
	 * @param trainingFile
	 * @param modelFile
	 * @param dicFile
	 * @param iterator
	 * @param devFile
	 * @param compressRatio
	 * @throws Exception
	 */
	private void segAPTrainWithOneThread(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("training start...");
		long startTime = System.currentTimeMillis();

		loadInstanceAndInit(trainingFile, dicFile);

		ArrayList<Integer> intList = new ArrayList<Integer>();
		for (int p = 0; p < instanceList.size(); p++)
		{
			intList.add(p);
		}

		double[] total = new double[model.parameter.length];

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

				SegInstance tmpInst = instanceList.elementAt(inst);
				String[] predLabel = new String[tmpInst.label.length];

				segViterbiDecode(tmpInst, predLabel);

				if (predLabel.equals(instanceList.elementAt(inst).label))
				{
					tmpInst = null;
					predLabel = null;
					continue;
				}
				SegInstance predInstance = new SegInstance(tmpInst);
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

			SegAP tmpSeg = new SegAP(tmpModel, segDic, allLabel);

			logger.info("evaluate uncompressed model with dev file...");
			double[] performance = tmpSeg.evalSeg(devFile, it);
			double precision = performance[0];
			double recall = performance[1];
			logger.info("the evaluation result of uncompressed P: " + precision + " R: " + recall + " F: "
					+ (2 * precision * recall) / (precision + recall) + "\n");

			logger.info("writer model to file...\n");
			tmpSeg.model.writerModel(modelFile + "-it-" + it, compressRatio);

			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpSeg = new SegAP(compressedModel, segDic, allLabel);
				logger.info("evaluate compressed model with dev file...");
				performance = tmpSeg.evalSeg(devFile, it);
				precision = performance[0];
				recall = performance[1];
				logger.info("the evaluation result of conpressed model P: " + precision + " R: " + recall + " F: "
						+ (2 * precision * recall) / (precision + recall) + "\n");
			}

			tmpModel = null;
			tmpSeg = null;

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
		instanceList = SegIO.getSegInstanceFromNormalFile(trainingFile, segDic);
		logger.info("instance number: " + instanceList.size());

		segPipeList = new Vector<Pipe>();
		logger.info("extract features from instance and map SegInstance to SegPipe...");
		FeatureMap featMap = new FeatureMap(instanceList, allLabel, segPipeList);
		logger.info("there are total " + featMap.feature2Int.size() + " features");
		logger.info("there are total " + featMap.label2Int.size() + " labels\n");

		model = new OnlineLabelModel(featMap);
		logger.info("load instance and initialize model over!");

	}

	/**
	 * If thread number is more than one, just use Structure Perceptron
	 * We don't need to use average
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
	private void segAPTrainWithMultiThreads(String trainingFile, String modelFile, String dicFile, int iterator, String devFile, final double compressRatio, final int threadNum)
			throws Exception
	{
		Logger logger = Logger.getLogger("seg");

		logger.info("Start to train....");
		long startTime = System.currentTimeMillis();
		//load training instance and transform them to PosInstance, then initialize dictionary, allLabel and model
		loadInstanceAndInit(trainingFile, dicFile);

		//set thread number, this is a static variable
		SegAP.threadNum = threadNum;
		logger.info("each thread almost uses " + instanceList.size() / threadNum + " training instances");

		for (int it = 0; it < iterator; it++)
		{
			logger.info("start iterator " + it + " ...");

			ExecutorService exec = Executors.newCachedThreadPool();
			ArrayList<Future<Object[]>> results = new ArrayList<Future<Object[]>>();

			CountDownLatch finishSigle = new CountDownLatch(threadNum);
			//create threadNum threads and submit them
			for (int id = 0; id < threadNum; id++)
			{
				//because the PosAP extends from PosViterbi and PosViterbi has implemented Runnable interface
				//in ExecutorService, there are two submit methods, one uses a Runnable task as parameter and another uses a Callable task as parameter
				//so we must use a cast to tell submit that we want use the submit which use a Callable parameter
				results.add(exec.submit((Callable<Object[]>) new SegAP(model, segDic, allLabel, id, instanceList,
						segPipeList, finishSigle)));
			}

			//wait until all sub-thread are finished
			finishSigle.await();
			logger.info("sub-threads are all finished!");
			logger.info("merge parameter and useNum...");
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
			
			logger.info("finiash merging parameter and useNum!");
			
			for (int i = 0;i < useNum.length;i++)
				model.useNum[i] += useNum[i];

			OnlineLabelModel tmpModel = new OnlineLabelModel(model.featMap, tmpPara,model.useNum);
			SegAP tmpSegger = new SegAP(tmpModel, segDic, allLabel);

			// evaluate current model with dev file
			logger.info("Evaluate for dev file with uncompressed model...");
			double[] performance = tmpSegger.evalSeg(devFile, it);
			double precision = performance[0];
			double recall = performance[1];
			logger.info("the evaluation result of uncompressed model P: " + precision + " R: " + recall + " F: "
					+ (2 * precision * recall) / (precision + recall) + "\n");

			logger.info("writer model to file...\n");
			tmpModel.writerModel(modelFile + "-it-" + it, compressRatio);

			if (compressRatio > 0)
			{
				OnlineLabelModel compressedModel = OnlineLabelModel.loadModel(modelFile + "-it-" + it);
				tmpSegger = new SegAP(compressedModel, segDic, allLabel);
				logger.info("Evaluate for dev file with compressed model...");
				performance = tmpSegger.evalSeg(devFile, it);
				precision = performance[0];
				recall = performance[1];
				logger.info("the evaluation result of compressed model P: " + precision + " R: " + recall + " F: "
						+ (2 * precision * recall) / (precision + recall) + "\n");
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
		Logger logger = Logger.getLogger("seg");
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

			SegInstance tmpInst = instanceList.elementAt(instIndex);
			String[] predLabel = new String[tmpInst.label.length];

			segViterbiDecode(tmpInst, predLabel);

			if (predLabel.equals(instanceList.elementAt(instIndex).label))
				continue;

			SegInstance predInstance = new SegInstance(tmpInst);
			predInstance.label = predLabel;

			Pipe predPipe = new Pipe(predInstance, model.featMap.feature2Int);

			model.update(segPipeList.elementAt(instIndex).feature, predPipe.feature);
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
		// TODO Auto-generated method stub
		return trainWithSubInstance();
	}
}
