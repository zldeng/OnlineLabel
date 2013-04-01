package ir.hit.edu.ltp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import gnu.trove.map.hash.THashMap;
import ir.hit.edu.ltp.basic.Pipe;
import ir.hit.edu.ltp.basic.SegInstance;
import ir.hit.edu.ltp.dic.PosDic;
import ir.hit.edu.ltp.basic.PosInstance;

/**
 * class for feature map
 * map string feature to int value
 * 
 * @author dzl
 * 
 */
@SuppressWarnings("serial")
public class FeatureMap implements Serializable
{
	public THashMap<String, Integer> feature2Int;
	public THashMap<String, Integer> label2Int;
	public THashMap<Integer, String> int2Label;

	public FeatureMap(THashMap<String, Integer> feature2Int, THashMap<String, Integer> label2Int,
			THashMap<Integer, String> int2Label)
	{
		this.feature2Int = feature2Int;
		this.label2Int = label2Int;
		this.int2Label = int2Label;
	}

	/**
	 * initialize feature map with POS training instances
	 * in training duration, create all possible feature and then store them
	 * in the same time, map instances to PosPipe
	 * 
	 * @param instanceVec
	 * @param allLabel
	 * @param posDic
	 * @param posPipeVec
	 * @throws Exception
	 */
	public FeatureMap(Vector<PosInstance> instanceVec, Vector<String> allLabel, PosDic posDic, Vector<Pipe> posPipeVec)
			throws Exception
	{
		this.feature2Int = new THashMap<String, Integer>();
		this.label2Int = new THashMap<String, Integer>();
		this.int2Label = new THashMap<Integer, String>();

		for (int i = 0; i < allLabel.size(); i++)
		{
			if (lookUpLabelIndex(allLabel.elementAt(i)) == -1)
			{
				addLabel(allLabel.elementAt(i));
			}
		}

		//create all label features
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < allLabel.size(); i++)
		{
			for (int j = 0; j < allLabel.size(); j++)
			{
				bf.delete(0, bf.length());
				bf.append("BL=").append(allLabel.elementAt(i)).append("/cL=").append(allLabel.elementAt(j));
				String biLabel = new String(bf);

				if (lookUpFeatureIndex(biLabel) == -1)
					addFeature(biLabel);
				biLabel = null;
			}
			String biLabel = "BL=_BL_/cL=" + allLabel.elementAt(i);
			if (lookUpFeatureIndex(biLabel) == -1)
				addFeature(biLabel);
			biLabel = null;
		}

		// PrintWriter writer = new PrintWriter(new FileWriter("feat.utf8"));

		for (PosInstance tmpInstance : instanceVec)
		{
			ArrayList<Integer> featList = new ArrayList<Integer>();
			/**************/
			//			System.out.println("sentence " + i);

			// writer.write("sentence: " + tmpInstance.words.toString() + "\n");
			// writer.write("label   : " + tmpInstance.label.toString() + "\n");

			StringBuffer curLabel = new StringBuffer();
			for (int j = 0; j < tmpInstance.words.length; j++)
			{
				Vector<String> featVec = tmpInstance.extractFeaturesFromInstanceInPosition(j, posDic);

				String word = tmpInstance.words[j];

				// create feature space
				// when the word appears in dictionary
				// just use the candidate POS
				Vector<String> cadidatePos = posDic.containsKey(word) ? posDic.getPos(word) : allLabel;
				for (String pos : cadidatePos)
				{
					curLabel.delete(0, curLabel.length());
					curLabel.append("/cL=").append(pos);
					//					String curLabel = "/cL=" + cadidatePos.elementAt(w);
					for (String featOld : featVec)
					{
						bf.delete(0, bf.length());
						bf.append(featOld).append(curLabel);
						String feat = new String(bf);
						if (lookUpFeatureIndex(feat) == -1)
							addFeature(feat);
						feat = null;
					}
				}

				curLabel.delete(0, curLabel.length());
				curLabel.append("/cL=").append(tmpInstance.label[j]);

				// writer.write("\nposition: " + j + " feature:\n");
				// map the instance from string feature vector to PosPipe
				for (String featOld : featVec)
				{
					bf.delete(0, bf.length());
					bf.append(featOld).append(curLabel);
					String feat = new String(bf);
					// writer.write(feat + "\n");
					if (feature2Int.containsKey(feat))
						featList.add(feature2Int.get(feat));
					else
					{
						// when training,all features should appear in feature space
						System.out.println("word: " + word);
						System.out.println("cad pos:");
						System.out.println(cadidatePos);

						System.out.println("curL: " + curLabel);
						System.out.println("feat: " + feat);
						throw new Exception("feature can't find in feature map!");
					}
				}
			}

			String[] label = tmpInstance.label;
			int[] feature = new int[featList.size()];
			for (int k = 0; k < feature.length; k++)
				feature[k] = featList.get(k);

			posPipeVec.add(new Pipe(feature, label));
		}
		// writer.close();
	}

	public FeatureMap(Vector<SegInstance> instanceVec, Vector<String> allLabel, Vector<Pipe> segPipeVec)
			throws Exception
	{
		this.feature2Int = new THashMap<String, Integer>();
		this.label2Int = new THashMap<String, Integer>();
		this.int2Label = new THashMap<Integer, String>();

		for (String str : allLabel)
		{
			if (lookUpLabelIndex(str) == -1)
			{
				addLabel(str);
			}
		}

		//create all bigram label features
		StringBuffer bf = new StringBuffer();
		for (String curPos : allLabel)
		{
			for (String prePos : allLabel)
			{
				bf.delete(0, bf.length());
				bf.append("BL=").append(prePos).append("/cL=").append(curPos);
				String biLabel = new String(bf);
				if (lookUpFeatureIndex(biLabel) == -1)
					addFeature(biLabel);
				biLabel = null;
			}
			String biLabel = "BL=_BL_/cL=" + curPos;
			if (lookUpFeatureIndex(biLabel) == -1)
				addFeature(biLabel);
		}

		StringBuffer curLabel = new StringBuffer();
		for (SegInstance inst : instanceVec)
		{
			ArrayList<Integer> featList = new ArrayList<Integer>();
			//			System.out.println(inst.sentence);
			for (int j = 0; j < inst.sentence.length; j++)
			{
				//				System.out.println("pos: " + j);

				Vector<String> featVec = inst.extractFeaturesFromInstanceInPosition(j);

				//create all possible feature when training
				//contains all positive feature and all negative feature
				for (String str : allLabel)
				{
					curLabel.delete(0, curLabel.length());
					curLabel.append("/cL=").append(str);
					for (String feat : featVec)
					{
						//						System.out.println(feat + curLabel);
						bf.delete(0, bf.length());
						bf.append(feat).append(curLabel);
						String featStr = new String(bf);
						if (!feature2Int.contains(featStr))
							addFeature(featStr);

						featStr = null;
					}
				}

				//map current SegInstance to SegPipe
				curLabel.delete(0, curLabel.length());
				curLabel.append("/cL=").append(inst.label[j]);
				for (String str : featVec)
				{
					bf.delete(0, bf.length());
					bf.append(str).append(curLabel);
					String tmp = new String(bf);
					if (feature2Int.contains(tmp))
						featList.add(feature2Int.get(tmp));
					else
					{
						throw new Exception("feature can't find in feature map!");
					}

					tmp = null;
				}
			}

			String[] label = inst.label;
			int[] feature = new int[featList.size()];
			for (int k = 0; k < feature.length; k++)
				feature[k] = featList.get(k);

			segPipeVec.add(new Pipe(feature, label));
		}
	}

	public int lookUpFeatureIndex(String feature)
	{
		if (feature2Int.containsKey(feature))
			return feature2Int.get(feature);
		else
			return -1;
	}

	public int lookUpLabelIndex(String label)
	{
		if (label2Int.containsKey(label))
			return label2Int.get(label);
		else
			return -1;
	}

	public String lookUpLabel(int index)
	{
		if (int2Label.containsKey(index))
			return (String) int2Label.get(index);
		else
			return null;
	}

	public void addFeature(String feature)
	{
		if (!feature2Int.containsKey(feature))
		{
			int count = feature2Int.size();
			feature2Int.put(feature, count);
		}
	}

	public void addLabel(String label)
	{
		if (!label2Int.containsKey(label))
		{
			int count = label2Int.size();
			label2Int.put(label, count);
			int2Label.put(count, label);
		}
	}
}
