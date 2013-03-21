import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class WSEval
{

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		// TODO Auto-generated method stub
		if (2 != args.length)
		{
			System.out.println("Usag: java WSEval gold_file[in] predict_file[out] ");
			return;
		}

		BufferedReader brGold = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));
		BufferedReader brPred = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), "UTF-8"));

		int goldTotal = 0, predTotal = 0, correct = 0;
		String goldStr, predStr;
		while ((goldStr = brGold.readLine()) != null && !goldStr.trim().equals(""))
		{
			predStr = brPred.readLine().trim();

			String[] gold = goldStr.trim().split(" ");
			String[] pred = predStr.trim().split(" ");

			goldTotal += gold.length;
			predTotal += pred.length;

			int goldIndex = 0, predIndex = 0;
			int goldLen = 0, predLen = 0;
			while (goldIndex < gold.length && predIndex < pred.length)
			{
				if (goldLen == predLen)
				{
					if (gold[goldIndex].equals(pred[predIndex]))
					{
						correct++;
						goldLen += gold[goldIndex].length();
						predLen += gold[goldIndex].length();
						goldIndex++;
						predIndex++;
					}
					else
					{
						goldLen += gold[goldIndex].length();
						predLen += pred[predIndex].length();
						goldIndex++;
						predIndex++;
					}
				}
				else if (goldLen < predLen)
				{
					goldLen += gold[goldIndex].length();
					goldIndex++;
				}
				else
				{
					predLen += pred[predIndex].length();
					predIndex++;
				}
			}

		}

		double precisin = (correct * 100.0) / predTotal;
		double recall = (correct * 100.0) / goldTotal;
		System.out.println("P: " + precisin + " R: " + recall + " F: " + (2 * precisin * recall) / (precisin + recall));
	}

}
