package ir.hit.edu.ltp.model;

import java.util.Vector;
import gnu.trove.*;

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
public class FeatureMap
{
	public TObjectIntHashMap feature2Int;
	public TObjectIntHashMap label2Int;
	public TIntObjectHashMap int2Label;

	public FeatureMap(TObjectIntHashMap feature2Int, TObjectIntHashMap label2Int, TIntObjectHashMap int2Label)
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
	public FeatureMap(Vector<PosInstance> instanceVec, Vector<String> allLabel, PosDic posDic,
			Vector<Pipe> posPipeVec) throws Exception
	{
		this.feature2Int = new TObjectIntHashMap();
		this.label2Int = new TObjectIntHashMap();
		this.int2Label = new TIntObjectHashMap();

		for (int i = 0; i < allLabel.size(); i++)
		{
			if (lookUpLabelIndex(allLabel.elementAt(i)) == -1)
			{
				addLabel(allLabel.elementAt(i));
			}
		}

		//create all label features
		for (int i = 0; i < allLabel.size(); i++)
		{
			for (int j = 0; j < allLabel.size(); j++)
			{
				String biLabel = "BiLabels=" + allLabel.elementAt(i) + "/curLabel=" + allLabel.elementAt(j);
				if (lookUpFeatureIndex(biLabel) == -1)
					addFeature(biLabel);
			}
			String biLabel = "BiLabels=_BL_/curLabel=" + allLabel.elementAt(i);
			if (lookUpFeatureIndex(biLabel) == -1)
				addFeature(biLabel);
		}

		// PrintWriter writer = new PrintWriter(new FileWriter("feat.utf8"));

		for (int i = 0; i < instanceVec.size(); ++i)
		{
			Pipe tmpPosPipe = new Pipe();

			final PosInstance tmpInstance = instanceVec.elementAt(i);
			
			/**************/
//			System.out.println("sentence " + i);
			
			
			// writer.write("sentence: " + tmpInstance.words.toString() + "\n");
			// writer.write("label   : " + tmpInstance.label.toString() + "\n");
			for (int j = 0; j < tmpInstance.words.size(); j++)
			{
				tmpPosPipe.label.add(tmpInstance.label.elementAt(j));

				Vector<String> featVec = tmpInstance.extractFeaturesFromInstanceInPosition(j, posDic);

				String word = tmpInstance.words.elementAt(j);

				// create feature space
				// when the word appears in dictionary,
				// just use the candidate POS
				Vector<String> cadidatePos = posDic.containsKey(word) ? posDic.getPos(word) : allLabel;
				for (int w = 0; w < cadidatePos.size(); w++)
				{
					String curLabel = "/curLabel=" + cadidatePos.elementAt(w);
					for (int p = 0; p < featVec.size(); p++)
					{
						String feat = featVec.elementAt(p) + curLabel;
						if (lookUpFeatureIndex(feat) == -1)
							addFeature(feat);
					}

				}

				String curLabel = "/curLabel=" + tmpInstance.label.elementAt(j);

				// writer.write("\nposition: " + j + " feature:\n");
				// map the instance from string feature vector to PosPipe
				for (int k = 0; k < featVec.size(); k++)
				{
					String feat = featVec.elementAt(k) + curLabel;
					// writer.write(feat + "\n");
					if (feature2Int.containsKey(feat))
						tmpPosPipe.feature.add(feature2Int.get(feat));
					else
					{
						// when training,all features should appear in feature space
						throw new Exception("feature can't find in feature map!");

					}
				}
			}

			posPipeVec.add(tmpPosPipe);
		}

		// writer.close();
	}

	@SuppressWarnings("unchecked")
	public FeatureMap(Vector<SegInstance> instanceVec, Vector<String> allLabel, Vector<Pipe> segPipeVec)
			throws Exception
	{
		this.feature2Int = new TObjectIntHashMap();
		this.label2Int = new TObjectIntHashMap();
		this.int2Label = new TIntObjectHashMap();

		for (String str : allLabel)
		{
			if (lookUpLabelIndex(str) == -1)
			{
				addLabel(str);
			}
		}

		//create all bigram label features
		for (int i = 0; i < allLabel.size(); i++)
		{
			for (int j = 0; j < allLabel.size(); j++)
			{
				String biLabel = "BiLabels=" + allLabel.elementAt(i) + "/curLabel=" + allLabel.elementAt(j);
				if (lookUpFeatureIndex(biLabel) == -1)
					addFeature(biLabel);
			}
			String biLabel = "BiLabels=_BL_/curLabel=" + allLabel.elementAt(i);
			if (lookUpFeatureIndex(biLabel) == -1)
				addFeature(biLabel);
		}

		for (int i = 0; i < instanceVec.size(); i++)
		{
			SegInstance inst = instanceVec.elementAt(i);

			Pipe tmpSegPipe = new Pipe();

			//			System.out.println(inst.sentence);
			for (int j = 0; j < inst.sentence.size(); j++)
			{
				//				System.out.println("pos: " + j);
				tmpSegPipe.label.add(inst.label.elementAt(j));
				Vector<String> featVec = inst.extractFeaturesFromInstanceInPosition(j);

				//create all possible feature when training
				//contains all positive feature and all negative feature
				for (String str : allLabel)
				{
					String curLabel = "/curLabel=" + str;
					for (String feat : featVec)
					{
						//						System.out.println(feat + curLabel);

						if (!feature2Int.contains(feat + curLabel))
							addFeature(feat + curLabel);
					}
				}

				//map current SegInstance to SegPipe
				String curLabel = "/curLabel=" + inst.label.elementAt(j);
				for (String str : featVec)
				{
					String tmp = str + curLabel;
					if (feature2Int.contains(tmp))
						tmpSegPipe.feature.add(feature2Int.get(tmp));
					else
					{
						throw new Exception("feature can't find in feature map!");
					}
				}
			}

			segPipeVec.add(tmpSegPipe);
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
