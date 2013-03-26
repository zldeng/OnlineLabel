package ir.hit.edu.ltp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import ir.hit.edu.ltp.basic.PosInstance;
import ir.hit.edu.ltp.util.FullCharConverter;

/**
 * read POS instance from file
 * 
 * @author dzl
 * 
 */
public class PosIO
{
	/**
	 * read POS instance from a normal file
	 * the file format is each sentence in a line
	 * the line format is word1_pos1 word2_pos2 ...
	 * 
	 * @param filename
	 * @param allLabel
	 * @return
	 * @throws IOException
	 */
	public static Vector<PosInstance> getPosInstanceFromNormalFile(String filename, Vector<String> allLabel)
			throws IOException
	{
		Vector<PosInstance> instVec = new Vector<PosInstance>();
		InputStreamReader is = new InputStreamReader(new FileInputStream(filename), "UTF-8");
		BufferedReader br = new BufferedReader(is);

		String line;
		while ((line = br.readLine()) != null)
		{
			if (line.trim().equals(""))
				continue;

			String[] token = line.trim().split(" ");
			Vector<String> words = new Vector<String>();
			Vector<String> label = new Vector<String>();

			for (String str : token)
			{
				String[] tmp = str.split("_");
				words.add(FullCharConverter.half2Fullchange(tmp[0]));
				label.add(tmp[1]);

				if (!allLabel.contains(tmp[1]))
					allLabel.add(tmp[1]);
			}

			PosInstance inst = new PosInstance(words, label);
			instVec.add(inst);
		}

		return instVec;
	}
}
