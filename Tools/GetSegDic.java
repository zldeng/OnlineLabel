import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

public class GetSegDic
{
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				"./pku/pku-only-one-of-ten.train.utf8"), "UTF-8"));
		PrintWriter uniWr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				"./pku/pku-only-one-of-ten.dic.utf8"), "UTF-8"));
		HashMap<String, Integer> unigramCount = new HashMap<String, Integer>();
		final int threshold = 3;
		String line;
		int sen = 0;
		while ((line = br.readLine()) != null)
		{
			sen++;
			if (sen % 100 == 0)
				System.out.println(sen);

			if (line.trim().equals(""))
				continue;

			String[] token = line.trim().split(" ");

			for (int i = 0; i < token.length; i++)
			{
				String str = token[i];
				if (unigramCount.keySet().contains(str))
				{
					int count = unigramCount.get(str) + 1;
					unigramCount.put(str, count);
				}
				else
					unigramCount.put(str, 1);
			}
		}

		for (java.util.Map.Entry<String, Integer> entry : unigramCount.entrySet())
		{
			if (entry.getValue() >= threshold)
			{
				String word = entry.getKey();
				uniWr.write(word + "\n");
			}
		}
		uniWr.close();
	}
}
