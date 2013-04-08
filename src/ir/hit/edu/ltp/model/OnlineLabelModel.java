package ir.hit.edu.ltp.model;

import gnu.trove.map.hash.THashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
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
	public float[] parameter;
	
	public int iteratorNum;
	public int instanceNum;

	public long[] useNum;

	public OnlineLabelModel(FeatureMap fm, float[] parameter,final int itNum,final int instNum)
	{
		this.featMap = fm;
		this.parameter = parameter;
		this.iteratorNum = itNum;
		this.instanceNum = instNum;
	}

	public OnlineLabelModel(FeatureMap fm, float[] parameter, long[] useNum,final int itNum,final int instNum)
	{
		this.featMap = fm;
		this.parameter = parameter;
		this.useNum = useNum;
		this.iteratorNum = itNum;
		this.instanceNum = instNum;
	}

	public OnlineLabelModel(FeatureMap fm,final int itNum,final int instNum)
	{
		this.featMap = fm;
		parameter = new float[fm.feature2Int.size()];
		useNum = new long[fm.feature2Int.size()];
		this.iteratorNum = itNum;
		this.instanceNum = instNum;
	}
	
	public OnlineLabelModel(FeatureMap fm)
	{
		this.featMap = fm;
		parameter = new float[fm.feature2Int.size()];
		useNum = new long[fm.feature2Int.size()];
		this.iteratorNum = 0;
		this.instanceNum = 0;
	}

	// get score according feature vector
	public double getScore(int[] featVect)
	{
		double score = 0;
		for (int index : featVect)
		{
			if (index == -1)
			{
				continue;
			}
			else if (index < -1 || index >= featMap.feature2Int.size())
			{
				throw new IllegalArgumentException("when get score, the feature index is an illegal index");
			}
			else
			{
				score += parameter[index];

				//only used in training
				//useNum is null when testing
				if (useNum != null)
					useNum[index]++;
			}
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
	public void update(int[] goldIndex, int[] predictIndex)
	{
		for (int i = 0; i < goldIndex.length; ++i)
		{
			if (goldIndex[i] == predictIndex[i])
				continue;
			else
			{
				parameter[goldIndex[i]]++;
				if (predictIndex[i] >= 0 && predictIndex[i] < parameter.length)
					parameter[predictIndex[i]]--;
				else
				{
					throw new IllegalArgumentException("Illegal index when updata paramater");
				}
			}
		}
	}

	// map string feature vector to int vector
	public int[] featVec2IntVec(String[] featVec)
	{
		int[] intVec = new int[featVec.length];
		for (int m = 0; m < featVec.length; m++)
		{
			String str = featVec[m];
			if (featMap.feature2Int.containsKey(str))
				intVec[m] = featMap.feature2Int.get(str);
			else
			{
				intVec[m] = -1;
			}
		}

		return intVec;
	}

	/**
	 * write model to a file
	 * sort features by used number
	 * some feature's value in parameter is 0 and these feature is useless for
	 * test
	 * just discard them and this can decrease model size significantly
	 * 
	 * @param modelFile
	 * @throws Exception
	 */
	public void writerModel(String modelFile, final double ratio) throws Exception
	{
		if (ratio < 0 || ratio >= 1)
		{
			throw new Exception("the Rompression ratio should more than 0 and less than 1");
		}

		DataOutputStream wr = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(modelFile)));

		double threshold = 1e-3;

		//if need compression, calculate the compression threshold
		if (ratio > 0)
		{
			//get weight threshold, we use it to compress model
			Vector<Float> weight = new Vector<Float>();
			for (float w : parameter)
			{
				w = Math.abs(w);
				if (w < 1e-3)
					continue;
				else
					weight.add(w);
			}
			Collections.sort(weight);
			threshold = weight.elementAt((int) (weight.size() * ratio));
		}

		wr.writeInt(instanceNum);
		wr.writeInt(iteratorNum);
		
		wr.writeUTF("#label");
		wr.writeInt(featMap.int2Label.size());

		for (int i = 0; i < featMap.int2Label.size(); i++)
			wr.writeUTF(featMap.int2Label.get(i));

		Vector<Feature> featVec = new Vector<OnlineLabelModel.Feature>();

		Set<String> feats = featMap.feature2Int.keySet();
		for (String str : feats)
		{
			int id = featMap.feature2Int.get(str);
			float value = parameter[id];
			if (Math.abs(value) < threshold)
				continue;
			Feature feat = new Feature(str, value, useNum[id]);
			featVec.add(feat);
		}

		Collections.sort(featVec, new Comparator<Feature>()
		{
			public int compare(Feature a, Feature b)
			{
				if (a.useNum > b.useNum)
					return -1;
				else if (a.useNum < b.useNum)
					return 1;
				else
					return 0;
			}
		});

		wr.writeUTF("#feature");
		wr.writeInt(featVec.size());

		String feature;
		float value;
		for (int i = 0; i < featVec.size(); i++)
		{
			feature = featVec.elementAt(i).feat;
			value = featVec.elementAt(i).value;
			wr.writeUTF(feature);
			wr.writeFloat(value);
		}
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
		DataInputStream br = new DataInputStream(new BufferedInputStream(new FileInputStream(modelFile)));

		THashMap<String, Integer> label2Int = new THashMap<String, Integer>();
		THashMap<Integer, String> int2Label = new THashMap<Integer, String>();

		final int instNum = br.readInt();
		final int itNum = br.readInt();
		
		String line;
		while ((line = br.readUTF()) != null && !line.equals("#label"))
			continue;
		if (!line.equals("#label"))
		{
			throw new Exception("read model error when reading label from model file");
		}

		final int labelNumber = br.readInt();
		for (int i = 0; i < labelNumber; i++)
		{
			line = br.readUTF();
			label2Int.put(line, i);
			int2Label.put(i, line);
		}

		line = br.readUTF();
		if (!line.equals("#feature"))
		{
			throw new Exception("read model error when reading features from model file");
		}
		final int parameterNumber = br.readInt();

		THashMap<String, Integer> feat2Int = new THashMap<String, Integer>();
		float[] tmpParameter = new float[parameterNumber];

		String feat;
		float value;
		for (int i = 0; i < parameterNumber; i++)
		{
			feat = br.readUTF();
			value = br.readFloat();

			feat2Int.put(feat, i);
			tmpParameter[i] = value;
		}

		FeatureMap featMap = new FeatureMap(feat2Int, label2Int, int2Label);
		OnlineLabelModel model = new OnlineLabelModel(featMap, tmpParameter,itNum,instNum);

		br.close();
		return model;
	}

	class Feature
	{
		public String feat;
		public float value;
		public long useNum;

		public Feature(String feat, float value, long useNum)
		{
			this.feat = feat;
			this.value = value;
			this.useNum = useNum;
		}
	}
}
