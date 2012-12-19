package ir.hit.edu.ltp.basic;

import java.util.Vector;

import org.apache.log4j.Logger;

import ir.hit.edu.ltp.dic.*;

public class PosPipe
{
	public Vector<Integer> feature;
	public Vector<String> label;

	/**
	 * map a training instance to a feature vector
	 * if a feature dose not appear in feature space, using -1 indicate
	 * 
	 * @param inst
	 *            training instance
	 * @param feature2Int
	 *            a map which map feature to int
	 * @param posDic
	 *            POS dictionary
	 */
	@SuppressWarnings("unchecked")
	public PosPipe(PosInstance inst, gnu.trove.TObjectIntHashMap feature2Int, PosDic posDic) throws Exception
	{
		this.label = (Vector<String>) inst.label.clone();
		this.feature = new Vector<Integer>();

		for (int i = 0; i < label.size(); ++i)
		{
			Vector<String> featureInPositionI = inst.extractFeaturesFromInstance(i, posDic);

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

	public PosPipe()
	{
		this.label = new Vector<String>();
		this.feature = new Vector<Integer>();
	}
}
