package ir.hit.edu.ltp.basic;

import java.util.Vector;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.dic.SegDic;

/**
 * class for SEG Pipe
 * a SEG contains a int vector and a label vector
 * the int vector contains all features of the instance
 * all features have been mapped into int number according the feature map
 * the label vector contains the SEG label of the instance
 * 
 * @author dzl
 * 
 */
public class SegPipe
{
	public Vector<Integer> feature;
	public Vector<String> label;

	@SuppressWarnings("unchecked")
	public SegPipe(SegInstance inst, gnu.trove.TObjectIntHashMap feature2Int) throws Exception
	{
		this.label = (Vector<String>) inst.label.clone();
		this.feature = new Vector<Integer>();

		for (int i = 0; i < label.size(); ++i)
		{
			Vector<String> featureInPositionI = inst.extractFeaturesFromInstanceInPosition(i);

			String curLabel = "/curLabel=" + inst.label.elementAt(i);

			Vector<String> newFeat = new Vector<String>();
			for (String str : featureInPositionI)
				newFeat.add(str + curLabel);

			for (int j = 0; j < newFeat.size(); ++j)
			{
				if (feature2Int.containsKey(newFeat.elementAt(j)))
					feature.add(feature2Int.get(newFeat.elementAt(j)));
				else
				{
					// in training, all features should appear in feature space
					Logger logger = Logger.getLogger("seg");
					logger.error("In training, features:" + newFeat.elementAt(j) + "dosen't appear in feature space");
					throw new Exception("feature " + newFeat.elementAt(j) + "can't find in feature map!");
				}
			}
		}
	}

	public SegPipe()
	{
		feature = new Vector<Integer>();
		label = new Vector<String>();
	}

}
