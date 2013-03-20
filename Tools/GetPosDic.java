import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

public class GetPosDic
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		InputStreamReader is = new InputStreamReader(new FileInputStream("./train.conll06.pos"), "UTF-8");
		BufferedReader br = new BufferedReader(is);

		PrintWriter posDicWriter = new PrintWriter(new FileWriter("./train.conll06.pos.dic"));

		HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
		HashMap<String, Vector<String>> wordPos = new HashMap<String, Vector<String>>();

		final int threshold = 3;

		int num = 0;
		String line;
		while ((line = br.readLine()) != null)
		{
			num++;
			System.out.println(num);
			if (line.trim().equals(""))
				continue;

			String[] sentence = line.trim().split(" ");
			for (int i = 0; i < sentence.length; i++)
			{
				String[] token = sentence[i].split("_");

				if (wordCount.containsKey(token[0]))
				{
					int count = wordCount.get(token[0]);
					wordCount.put(token[0], count + 1);
					if (!wordPos.get(token[0]).contains(token[1]))
					{
						Vector<String> newPos = wordPos.get(token[0]);
						newPos.add(token[1]);
						wordPos.put(token[0], newPos);
					}
				}
				else
				{
					wordCount.put(token[0], 1);
					Vector<String> pos = new Vector<String>();
					pos.add(token[1]);

					wordPos.put(token[0], pos);
				}
			}

		}

		for (java.util.Map.Entry<String, Integer> entry : wordCount.entrySet())
		{
			if (entry.getValue() >= threshold)
			{
				String word = entry.getKey();
				Vector<String> tmpPos = wordPos.get(word);
				String tmp = word;

				for (int i = 0; i < tmpPos.size(); i++)
					tmp += " " + tmpPos.elementAt(i);
				posDicWriter.write(tmp + "\n");
			}
		}

		posDicWriter.close();

	}

}
