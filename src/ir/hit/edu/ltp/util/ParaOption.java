package ir.hit.edu.ltp.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * a class for parser command parameter
 * 
 * @author dzl
 * 
 */
public class ParaOption
{
	CommandLine cmd;
	public String trainFile, devFile, testFile, dicFile, resultFile, modelFile;
	public int iterator,threadNum;
	public boolean train = false, test = false;
	public double compressRatio = 0;

	public ParaOption(String[] args) throws ParseException
	{
		Options option = new Options();
		option.addOption("h", false, "show Usag information");
		option.addOption("train", false, "train model");
		option.addOption("test", false, "test model");
		option.addOption("trainFile", true, "train file");
		option.addOption("dicFile", true, "dictionary file");
		option.addOption("model", true, "model file");
		option.addOption("iterator", true, "iterator time");
		option.addOption("devFile", true, "dev file");
		option.addOption("testFile", true, "test file");
		option.addOption("result", true, "result file");
		option.addOption("thread",true,"thread number");
		option.addOption("compress",true,"the ratio of compress model,which is the proportion of features you want to remove");
		CommandLineParser parser = new PosixParser();
		cmd = parser.parse(option, args);
	}

	public boolean checkParameter()
	{
		boolean flag = true;
		if (cmd.hasOption("h"))
		{
			//			printUsag();
			return false;
		}
		else if (cmd.hasOption("train") && cmd.hasOption("test"))
		{
			flag = false;
		}
		else if (cmd.hasOption("train"))
		{
			if (!(cmd.hasOption("trainFile") && cmd.hasOption("model") && cmd.hasOption("iterator")
					&& cmd.hasOption("dicFile") && cmd.hasOption("devFile")))
			{
				//				printUsag();
				flag = false;
			}
			train = true;
			trainFile = cmd.getOptionValue("trainFile");
			modelFile = cmd.getOptionValue("model");
			dicFile = cmd.getOptionValue("dicFile");
			devFile = cmd.getOptionValue("devFile");
			iterator = Integer.parseInt(cmd.getOptionValue("iterator"));
			
			if (cmd.hasOption("thread"))
				threadNum = Integer.parseInt(cmd.getOptionValue("thread"));
			else
				threadNum = 1;
			
			if (cmd.hasOption("compress"))
				compressRatio = Double.parseDouble(cmd.getOptionValue("compress"));
			else
				compressRatio = 0;

			//			logger.info("Training file: " + trainFile);
			//			logger.info("Dev file: " + devFile);
			//			logger.info("Dictionary file: " + dicFile);
			//			logger.info("Model file: " + modelFile);
			//			logger.info("Iterator number: " + iterator + "\n");
		}
		else if (cmd.hasOption("test"))
		{
			if (!(cmd.hasOption("testFile") && cmd.hasOption("model") && cmd.hasOption("result") && cmd
					.hasOption("dicFile")))
			{
				//				printUsag();
				flag = false;
			}
			test = true;
			modelFile = cmd.getOptionValue("model");
			dicFile = cmd.getOptionValue("dicFile");
			testFile = cmd.getOptionValue("testFile");
			resultFile = cmd.getOptionValue("result");
			
			if (cmd.hasOption("thread"))
				threadNum = Integer.parseInt(cmd.getOptionValue("thread"));
			else
				threadNum = 1;
			

			//			logger.info("Model file: " + modelFile);
			//			logger.info("Dictionary file: " + dicFile);
			//			logger.info("Test file: " + testFile);
			//			logger.info("Result file: " + resultFile + "\n");
		}
		else
		{
			//			printUsag();
			flag = false;
		}

		return flag;
	}

	//	private void printUsag()
	//	{
	//		Logger logger = Logger.getRootLogger();
	//		logger.info("Usag:");
	//		logger.info("train: java -jar onlinePosTagger.jar -train -trainFile train_file -dicFile dic_file -model model_file -iterator iterator -devFile dev_file");
	//		logger.info("test:java -jar onlinePosTagger.jar -test -model model_file -dicFile dic_file -testFile test_file -result result_file\n");
	//	}
}
