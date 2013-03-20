import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PosEval
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub

		String goldFile = "./postagged-test";

		String predFile = "./seg-test.result";

		InputStreamReader goldIs = new InputStreamReader(new FileInputStream(goldFile), "UTF-8");
		BufferedReader goldBr = new BufferedReader(goldIs);

		InputStreamReader predIis = new InputStreamReader(new FileInputStream(predFile), "UTF-8");
		BufferedReader predBr = new BufferedReader(predIis);

		String str1, str2;
		int num = 0;
		int total = 0;
		int correct = 0;
		while ((str1 = goldBr.readLine()) != null)
		{
			num++;
			str2 = predBr.readLine();

			String[] goldArray = str1.trim().split(" ");
			String[] predArray = str2.trim().split(" ");

			if (goldArray.length != predArray.length)
			{
				System.out.println("sentence " + num + " is not same length");
			}

			total += goldArray.length;
			for (int i = 0; i < goldArray.length; i++)
			{
				String[] tmp1 = goldArray[i].split("_");
				String[] tmp2 = predArray[i].split("_");

				if (!tmp1[0].equals(tmp2[0]))
				{
					System.out.println("sentence " + num + " word " + i + "is not same");
				}
				if (tmp1[1].trim().equals(tmp2[1].trim()))
				{
					correct++;
				}
			}
		}
		System.out.println("POS precision: " + (correct * 100.0) / total);

	}
}
