package ir.hit.edu.ltp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.basic.StackedSegInstance;
import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.ml.SegViterbi;

public class SegIO
{
	/**
	 * get training instance from file and convert the segmented sentences to
	 * SEG instances at the same time
	 * 
	 * @param fileName
	 * @param segDic
	 * @return
	 * @throws IOException
	 */
	public static Vector<SegInstance> getSegInstanceFromNormalFile(String fileName, SegDic segDic) throws IOException
	{
		Vector<SegInstance> segVec = new Vector<SegInstance>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

		String line;
		while ((line = br.readLine()) != null)
		{
			if (line.trim().equals(""))
				continue;

			line = line.trim().replaceAll("\\t{1,}", " ");
			line = line.replaceAll("\\s{2,}", " ");
			String[] sentence = line.split(" ");
			SegInstance inst = new SegInstance(sentence, segDic);
			segVec.add(inst);
		}
		return segVec;
	}

	public static Vector<StackedSegInstance> getStackedSegInstanceFromNormalFile(String fileName, SegDic stackedDic, SegViterbi baseSegger)
			throws IOException
	{
		Logger logger = Logger.getLogger("seg");
		Vector<StackedSegInstance> segVec = new Vector<StackedSegInstance>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));

		int lineNum = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			lineNum++;
			if (lineNum % 500 == 0)
				logger.info("instance " + lineNum);

			if (line.trim().equals(""))
				continue;

			line = line.trim().replaceAll("\\t{1,}", " ");
			line = line.replaceAll("\\s{2,}", " ");
			String[] sentence = line.split(" ");
			StackedSegInstance inst = new StackedSegInstance(sentence, stackedDic, baseSegger);
			segVec.add(inst);
		}
		return segVec;
	}
}
