package ir.hit.edu.ltp.basic;

import ir.hit.edu.ltp.dic.PosDic;

import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * class for a Pipe
 * a Pipe is a representation of a instance by mapping string features to 
 * int features
 * 
 * @author dzl
 * 
 */
public class Pipe
{
	public Vector<Integer> feature;
	public Vector<String> label;

	public Pipe()
	{
		feature = new Vector<Integer>();
		label = new Vector<String>();
	}

	/**
	 * Initialize a Pipe with a POS instance
	 * 
	 * @param inst
	 *            POS instance
	 * @param feature2Int
	 *            feature map
	 * @param posDic
	 *            POS dictionary
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Pipe(PosInstance inst, gnu.trove.TObjectIntHashMap feature2Int, PosDic posDic) throws Exception
	{
		this.label = (Vector<String>) inst.label.clone();
		this.feature = new Vector<Integer>();

		for (int i = 0; i < label.size(); ++i)
		{
			Vector<String> featureInPositionI = inst.extractFeaturesFromInstanceInPosition(i, posDic);

			String curLabel = "/curLabel=" + inst.label.elementAt(i);
			Vector<String> newFeat = new Vector<String>();
			for (int m = 0; m < featureInPositionI.size(); m++)
			{
				newFeat.add(featureInPositionI.elementAt(m) + curLabel);
			}

			for (int j = 0; j < newFeat.size(); ++j)
			{
				if (feature2Int.containsKey(newFeat.elementAt(j)))
					feature.add(feature2Int.get(newFeat.elementAt(j)));
				else
				{
					// in training, all features should appear in feature space
					Logger logger = Logger.getLogger("pos");
					logger.error("In training, features:" + newFeat.elementAt(j) + "dosen't appear in feature space");
					throw new Exception("feature can't find in feature map!");
				}
			}
		}
	}

	/**
	 * Initialize a Pipe with a SEG instance
	 * 
	 * @param inst
	 *            SEG instance
	 * @param feature2Int
	 *            feature map
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Pipe(SegInstance inst, gnu.trove.TObjectIntHashMap feature2Int) throws Exception
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
}
