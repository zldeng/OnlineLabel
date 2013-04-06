package ir.hit.edu.ltp.label;

import org.apache.log4j.*;

import ir.hit.edu.ltp.ml.PosAP;
import ir.hit.edu.ltp.ml.PosViterbi;
import ir.hit.edu.ltp.util.ParaOption;

public class OnlinePos
{
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("./config/log4j.properties");

		Logger logger = Logger.getLogger("pos");

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
			logger.info("cluster file: " + option.clusterFile);
			logger.info("Model file: " + option.modelFile);
			logger.info("Iterator number: " + option.iterator);
			logger.info("thread number: " + option.threadNum);
			logger.info("compress ratio: " + option.compressRatio + "\n");

			logger.info("training start....\n");
			PosAP posTagger = new PosAP();
			posTagger.PosAPTrain(option.trainFile, option.modelFile, option.dicFile, option.clusterFile,
					option.iterator, option.devFile, option.compressRatio, option.threadNum);
		}
		else if (option.test)
		{
			logger.info("Model file: " + option.modelFile);
			logger.info("Dictionary file: " + option.dicFile);
			logger.info("cluster file: " + option.clusterFile);
			logger.info("Test file: " + option.testFile);
			logger.info("Result file: " + option.resultFile);
			logger.info("thread number: " + option.threadNum + "\n");

			logger.info("testing start...");
			PosViterbi posTagger = new PosViterbi();
			posTagger.loadResource(option.modelFile, option.dicFile, option.clusterFile);
			posTagger.PosForFile(option.testFile, option.resultFile, option.threadNum);
		}
	}

	private static void printUsag()
	{
		Logger logger = Logger.getLogger("pos");
		logger.info("Usag:");
		logger.info("train: java -cp onlineLabel.jar ir.hit.edu.ltp.parser.OnlinePos -train -trainFile train_file -dicFile dic_file -model model_file -iterator iterator -devFile dev_file [opt]-compress compressRetio [opt]-thread threadNum -clusterFile cluster_file");
		logger.info("test:  java -cp onlineLabel.jar ir.hit.edu.ltp.parser.OnlinePos -test -model model_file -dicFile dic_file -testFile test_file -result result_file [opt]-thread threadNum -clusterFile cluster_file\n");
	}
}
