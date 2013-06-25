package ir.hit.edu.ltp.label;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ir.hit.edu.ltp.ml.SegAP;
import ir.hit.edu.ltp.ml.SegViterbi;
import ir.hit.edu.ltp.ml.StackedSegAP;
import ir.hit.edu.ltp.ml.StackedSegViterbi;
import ir.hit.edu.ltp.util.ParaOption;

public class OnlineSeg
{
	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("./config/log4j.properties");

		Logger logger = Logger.getLogger("seg");

		logger.info("\n\n\n*********** welcome ******************\n");

		ParaOption option = new ParaOption(args);
		if (!option.checkParameter())
		{
			printUsag();
			return;
		}
		else if (option.train)
		{
			logger.info("Training file: " + option.trainFile);
			logger.info("Dev file: " + option.devFile);
			logger.info("Dictionary file: " + option.dicFile);
			logger.info("Model file: " + option.modelFile);
			logger.info("Iterator number: " + option.iterator);
			logger.info("thread number: " + option.threadNum);
			logger.info("compress ratio: " + option.compressRatio + "\n");

			logger.info("start to train...");
			SegAP segger = new SegAP();
			segger.segAPTrain(option.trainFile, option.modelFile, option.dicFile, option.iterator, option.devFile,
					option.compressRatio, option.threadNum);
		}
		else if (option.retrain)
		{
			logger.info("Training file: " + option.trainFile);
			logger.info("Dev file: " + option.devFile);
			logger.info("Dictionary file: " + option.dicFile);
			logger.info("Model file: " + option.modelFile);
			logger.info("old model file: " + option.oldModelFile);
			logger.info("Iterator number: " + option.iterator);
			logger.info("thread number: " + option.threadNum);
			logger.info("compress ratio: " + option.compressRatio + "\n");

			logger.info("start to retrain...");
			SegAP segger = new SegAP();
			segger.segAPRetrain(option.trainFile, option.oldModelFile, option.modelFile, option.dicFile,
					option.iterator, option.devFile, option.threadNum, option.compressRatio);
		}
		else if (option.stackedTrain)
		{
			logger.info("Training file: " + option.trainFile);
			logger.info("Dev file: " + option.devFile);
			logger.info("base dictionary file: " + option.baseDicFile);
			logger.info("base model file: " + option.baseModelFile);
			logger.info("stacked model file: " + option.modelFile);
			logger.info("stacked dictionary file: " + option.dicFile);
			logger.info("Iterator number: " + option.iterator);
			logger.info("compression ratio: " + option.compressRatio);

			logger.info("start to train...");
			StackedSegAP stackedSegger = new StackedSegAP();
			stackedSegger.stackedSegAPTrain(option.trainFile, option.modelFile, option.baseModelFile, option.dicFile,
					option.baseDicFile, option.iterator, option.devFile, option.compressRatio);
		}
		else if (option.test)
		{
			logger.info("Model file: " + option.modelFile);
			logger.info("Dictionary file: " + option.dicFile);
			logger.info("Test file: " + option.testFile);
			logger.info("Result file: " + option.resultFile);
			logger.info("thread number: " + option.threadNum + "\n");

			logger.info("testing start...");
			SegViterbi segger = new SegViterbi();
			segger.loadResource(option.modelFile, option.dicFile);
			segger.segForFile(option.testFile, option.resultFile, option.threadNum);
		}
		else if (option.stackedTest)
		{
			logger.info("base model file: " + option.baseModelFile);
			logger.info("base dic file: " + option.baseDicFile);
			logger.info("stacked model file: " + option.modelFile);
			logger.info("Dictionary file: " + option.dicFile);
			logger.info("Test file: " + option.testFile);
			logger.info("Result file: " + option.resultFile);

			logger.info("testing start...");
			StackedSegViterbi stackedSegger = new StackedSegViterbi();
			stackedSegger.loadResource(option.modelFile, option.baseModelFile, option.dicFile, option.baseDicFile);
			stackedSegger.segForFile(option.testFile, option.resultFile);
		}
	}

	private static void printUsag()
	{
		Logger logger = Logger.getLogger("seg");
		logger.info("Usag:");
		logger.info("train: java -cp onlineLabel.jar ir.hit.edu.ltp.parser.OnlineSeg -train -trainFile train_file -dicFile dic_file -model model_file -iterator iterator -devFile dev_file [opt]-compress compressRetio [opt]-thread threadNum");
		logger.info("test:  java -cp onlineLabel.jar ir.hit.edu.ltp.parser.OnlineSeg -test -model model_file -dicFile dic_file -testFile test_file -result result_file [opt]-thread threadNum \n");
	}

}
