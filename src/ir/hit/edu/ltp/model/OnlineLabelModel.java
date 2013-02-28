package ir.hit.edu.ltp.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Vector;

/**
 * class of online label model
 * a model contain a feature map and a parameter
 * 
 * @author dzl
 * 
 */
public class OnlineLabelModel
{
	public FeatureMap featMap;
	public double[] parameter;

	public OnlineLabelModel(FeatureMap fm, double[] parameter)
	{
		this.featMap = fm;
		this.parameter = parameter;
	}

	public OnlineLabelModel(FeatureMap fm)
	{
		this.featMap = fm;
		parameter = new double[fm.feature2Int.size()];
		for (int i = 0; i < parameter.length; i++)
			parameter[i] = 0;
	}

	// get score according feature vector
	public double getScore(Vector<Integer> featVect)
	{
		double score = 0;
		for (int i = 0; i < featVect.size(); ++i)
		{
			int index = featVect.elementAt(i);
			if (index == -1)
			{
				continue;
			}
			else if (index < -1 || index >= featMap.feature2Int.size())
			{
				throw new IllegalArgumentException("when get score, the feature index is an illegal index");
			}
			else
				score += parameter[index];
		}
		return score;
	}

	// add current parameter to total array
	public void addToTotal(double[] total)
	{
		for (int i = 0; i < total.length; ++i)
		{
			total[i] += parameter[i];
		}
	}

	// update the parameter according the predicted label and gold label
	public void update(Vector<Integer> goldIndex, Vector<Integer> predictIndex)
	{
		for (int i = 0; i < goldIndex.size(); ++i)
		{
			if (goldIndex.elementAt(i) == predictIndex.elementAt(i))
				continue;
			else
			{
				parameter[goldIndex.elementAt(i)]++;
				if (predictIndex.elementAt(i) >= 0 && predictIndex.elementAt(i) < parameter.length)
					parameter[predictIndex.elementAt(i)]--;
				else
				{
					throw new IllegalArgumentException("Illegal index when updata paramater");
				}
			}
		}
	}

	// map string feature vector to int vector
	public Vector<Integer> featVec2IntVec(Vector<String> featVec)
	{
		Vector<Integer> intVec = new Vector<Integer>();
		for (int m = 0; m < featVec.size(); m++)
		{
			String str = featVec.elementAt(m);
			if (featMap.feature2Int.containsKey(str))
				intVec.add(featMap.feature2Int.get(str));
			else
			{
				intVec.add(-1);
			}
		}

		return intVec;
	}

	/**
	 * write model to a file
	 * some feature's value in parameter is 0 and these feature is useless for
	 * test
	 * just discard them and this can decrease model size significantly
	 * 
	 * @param modelFile
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void writerModel(String modelFile) throws FileNotFoundException, UnsupportedEncodingException
	{
		FileOutputStream s = new FileOutputStream(modelFile);
		PrintWriter wr = new PrintWriter(new OutputStreamWriter(s, "UTF-8"));

		//get weight threshold, we use it to prune model
//		Vector<Double> weight = new Vector<Double>();
//		for (double w : parameter)
//		{
//			w = Math.abs(w);
//			if (w < 1e-5)
//				continue;
//			else
//				weight.add(w);
//		}
//		Collections.sort(weight);
//		double threshold = weight.elementAt((int) (weight.size() * 0.5));
		
		wr.write("#label\n");
		for (int i = 0; i < featMap.int2Label.size(); i++)
			wr.write(featMap.int2Label.get(i) + " " + i + "\n");
		wr.write("\n");

		wr.write("#feature\n");
		Object[] feats = featMap.feature2Int.keys();
		int num = 0;
		for (int i = 0; i < feats.length; i++)
		{
			int id = featMap.feature2Int.get(feats[i]);
			if (Math.abs(parameter[id]) < 1e-5)
				continue;
			wr.write(feats[i] + " " + num + " " + parameter[id] + "\n");
			num++;
		}

		wr.flush();
		wr.close();
	}

	/**
	 * load a model from model file
	 * the process is corresponding to writer function
	 * 
	 * @param modelFile
	 * @return
	 * @throws Exception
	 */
	public static OnlineLabelModel loadModel(String modelFile) throws Exception
	{
		InputStreamReader is = new InputStreamReader(new FileInputStream(modelFile), "UTF-8");
		BufferedReader br = new BufferedReader(is);

		gnu.trove.TObjectIntHashMap label2Int = new gnu.trove.TObjectIntHashMap();
		gnu.trove.TIntObjectHashMap int2Label = new gnu.trove.TIntObjectHashMap();

		String line;
		while ((line = br.readLine()) != null && !line.equals("#label"))
			continue;
		if (!line.equals("#label"))
		{
			throw new Exception("read model error when reading label from model file");
		}

		while ((line = br.readLine()) != null && !line.equals(""))
		{
			String[] token = line.trim().split(" ");
			if (token.length != 2)
			{
				throw new Exception("read model error when reading label from model file");
			}
			String labelStr = token[0].trim();
			int labelIndex = Integer.parseInt(token[1].trim());

			label2Int.put(labelStr, labelIndex);
			int2Label.put(labelIndex, labelStr);
		}

		line = br.readLine().trim();
		if (!line.equals("#feature"))
		{
			throw new Exception("read model error when reading features from model file");
		}

		gnu.trove.TObjectIntHashMap feat2Int = new gnu.trove.TObjectIntHashMap();
		gnu.trove.TDoubleArrayList paraArray = new gnu.trove.TDoubleArrayList();

		while ((line = br.readLine()) != null && !line.equals(""))
		{
			String[] token = line.split(" ");
			if (token.length != 3)
			{
				throw new Exception("read model error when reading features from model file");
			}
			String feat = token[0];
			int index = Integer.parseInt(token[1].trim());
			double value = Double.parseDouble(token[2].trim());
			feat2Int.put(feat, index);
			paraArray.add(value);
		}

		if (feat2Int.size() != paraArray.size())
		{
			throw new Exception("feature size is not same to parameter size when reading model");
		}

		FeatureMap featMap = new FeatureMap(feat2Int, label2Int, int2Label);
		double[] parameter = paraArray.toNativeArray();
		OnlineLabelModel model = new OnlineLabelModel(featMap, parameter);

		return model;
	}
}
