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
	public String trainFile, devFile, testFile, dicFile, resultFile, modelFile, clusterFile;
	public int iterator, threadNum;
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
		option.addOption("clusterFile", true, "word cluster file");
		option.addOption("model", true, "model file");
		option.addOption("iterator", true, "iterator time");
		option.addOption("devFile", true, "dev file");
		option.addOption("testFile", true, "test file");
		option.addOption("result", true, "result file");
		option.addOption("thread", true, "thread number");
		option.addOption("compress", true,
				"the ratio of compress model,which is the proportion of features you want to remove");
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

			if (cmd.hasOption("clusterFile"))
				clusterFile = cmd.getOptionValue("clusterFile");
		}
		else if (cmd.hasOption("test"))
		{
			if (!(cmd.hasOption("testFile") && cmd.hasOption("model") && cmd.hasOption("result") && cmd
					.hasOption("dicFile")))
			{
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

			if (cmd.hasOption("clusterFile"))
				clusterFile = cmd.getOptionValue("clusterFile");

		}
		else
		{
			flag = false;
		}

		return flag;
	}
}
