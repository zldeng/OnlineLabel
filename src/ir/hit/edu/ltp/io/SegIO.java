package ir.hit.edu.ltp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.dic.SegDic;

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

			String[] sentence = line.trim().split(" ");
			SegInstance inst = new SegInstance(sentence, segDic);
			segVec.add(inst);
		}
		return segVec;
	}
}
