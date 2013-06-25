import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JointEval
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
			System.out.println("Usag: java JointEval gold_file[in] predict_file[out] ");
			return;
		}
		//gold file
		BufferedReader brGold = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), "UTF-8"));

		//predicted file
		BufferedReader brPred = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]), "UTF-8"));

		int goldTotal = 0, predTotal = 0, correctWord = 0, correctBoth = 0;
		String goldStr, predStr;
		int num = 0;
		while ((goldStr = brGold.readLine()) != null && !goldStr.trim().equals(""))
		{
			num++;
			System.out.println(num);
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
					if (gold[goldIndex].split("_")[0].equals(pred[predIndex].split("_")[0]))
					{
						correctWord++;

						if (gold[goldIndex].split("_")[1].equals(pred[predIndex].split("_")[1]))
							correctBoth++;

						goldLen += gold[goldIndex].length();
						predLen += gold[goldIndex].length();
						goldIndex++;
						predIndex++;

					}
					else
					{
						goldLen += gold[goldIndex].split("_")[0].length();
						predLen += pred[predIndex].split("_")[0].length();
						goldIndex++;
						predIndex++;
					}
				}
				else if (goldLen < predLen)
				{
					goldLen += gold[goldIndex].split("_")[0].length();
					goldIndex++;
				}
				else
				{
					predLen += pred[predIndex].split("_")[0].length();
					predIndex++;
				}
			}

		}

		double wordPrecision = (correctWord * 100.0) / predTotal;
		double wordRecall = (correctWord * 100.0) / goldTotal;
		System.out.println("word: P: " + wordPrecision + " R: " + wordRecall + " F: "
				+ (2 * wordPrecision * wordRecall) / (wordPrecision + wordRecall));

		double bothPrecision = (correctBoth * 100) / predTotal;
		double bothRecall = (correctBoth * 100) / goldTotal;
		System.out.println("joint: P: " + bothPrecision + " R: " + bothRecall + " F: "
				+ (2 * bothPrecision * bothRecall) / (bothPrecision + bothRecall));

	}

}
