package ir.hit.edu.ltp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import ir.hit.edu.ltp.basic.PosInstance;

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

			line = line.trim().replaceAll("\\t{1,}", " ");
			line = line.replaceAll("\\s{2,}", " ");

			String[] token = line.split(" ");
			String[] words = new String[token.length];
			String[] label = new String[token.length];

			for (int i = 0; i < token.length; i++)
			{
				String[] tmp = token[i].split("_");
				//				words[i] = FullCharConverter.half2Fullchange(tmp[0]);
				words[i] = tmp[0];
				label[i] = tmp[1];

				if (!allLabel.contains(tmp[1]))
					allLabel.add(tmp[1]);
			}

			PosInstance inst = new PosInstance(words, label);
			instVec.add(inst);
		}

		return instVec;
	}
}
