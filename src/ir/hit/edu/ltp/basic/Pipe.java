package ir.hit.edu.ltp.basic;

import gnu.trove.map.hash.THashMap;
import ir.hit.edu.ltp.dic.PosDic;

import java.util.ArrayList;
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
	public int[] feature;
	public String[] label;

	public Pipe(int[] feature, String[] label)
	{
		this.feature = feature;
		this.label = label;
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
	public Pipe(PosInstance inst, THashMap<String, Integer> feature2Int, PosDic posDic) throws Exception
	{
		this.label = inst.label;

		ArrayList<Integer> featList = new ArrayList<Integer>();

		Vector<String> featureInPositionI = new Vector<String>();
		Vector<String> newFeat = new Vector<String>();
		StringBuffer curLabel = new StringBuffer();

		for (int i = 0; i < label.length; ++i)
		{
			featureInPositionI = null;
			featureInPositionI = inst.extractFeaturesFromInstanceInPosition(i, posDic);

			curLabel.delete(0, curLabel.length());
			curLabel.append("/cL=");
			curLabel.append(inst.label[i]);

			newFeat = new Vector<String>();
			StringBuffer bf = new StringBuffer();
			for (String feat : featureInPositionI)
			{
				bf.delete(0, bf.length());
				bf.append(feat).append(curLabel);
				newFeat.add(new String(bf));
			}

			for (int j = 0; j < newFeat.size(); ++j)
			{
				if (feature2Int.containsKey(newFeat.elementAt(j)))
					featList.add(feature2Int.get(newFeat.elementAt(j)));
				else
				{
					// in training, all features should appear in feature space
					Logger logger = Logger.getLogger("pos");
					logger.error("In training, features:" + newFeat.elementAt(j) + "dosen't appear in feature space");
					System.out.println("FEAT: " + newFeat.elementAt(j));
					throw new Exception("feature can't find in feature map!");
				}
			}

			featureInPositionI = null;
			newFeat = null;
		}

		this.feature = new int[featList.size()];
		for (int i = 0; i < featList.size(); i++)
			feature[i] = featList.get(i);

		featList = null;
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
	public Pipe(SegInstance inst, THashMap<String, Integer> feature2Int) throws Exception
	{
		this.label = inst.label;

		ArrayList<Integer> featList = new ArrayList<Integer>();

		Vector<String> featureInPositionI = new Vector<String>();
		Vector<String> newFeat = new Vector<String>();
		StringBuffer curLabel = new StringBuffer();

		for (int i = 0; i < label.length; ++i)
		{
			featureInPositionI = null;
			featureInPositionI = inst.extractFeaturesFromInstanceInPosition(i);

			curLabel.delete(0, curLabel.length());
			curLabel.append("/cL=").append(inst.label[i]);

			newFeat = new Vector<String>();
			StringBuffer bf = new StringBuffer();
			for (String str : featureInPositionI)
			{
				bf.delete(0, bf.length());
				bf.append(str).append(curLabel);
				newFeat.add(new String(bf));
			}

			for (int j = 0; j < newFeat.size(); ++j)
			{
				if (feature2Int.containsKey(newFeat.elementAt(j)))
					featList.add(feature2Int.get(newFeat.elementAt(j)));
				else
				{
					// in training, all features should appear in feature space
					Logger logger = Logger.getLogger("seg");
					logger.error("In training, features:" + newFeat.elementAt(j) + "dosen't appear in feature space");
					throw new Exception("feature " + newFeat.elementAt(j) + "can't find in feature map!");
				}
			}

			featureInPositionI = null;
			newFeat = null;
		}
		this.feature = new int[featList.size()];
		for (int i = 0; i < featList.size(); i++)
			this.feature[i] = featList.get(i);

		featList = null;
	}

	public Pipe(StackedSegInstance inst, THashMap<String, Integer> feature2Int) throws Exception
	{
		this.label = inst.label;

		ArrayList<Integer> featList = new ArrayList<Integer>();

		Vector<String> featureInPositionI = new Vector<String>();
		Vector<String> newFeat = new Vector<String>();
		StringBuffer curLabel = new StringBuffer();

		for (int i = 0; i < label.length; ++i)
		{
			featureInPositionI = null;
			featureInPositionI = inst.extractFeaturesFromStackedInstanceInPosition(i);

			curLabel.delete(0, curLabel.length());
			curLabel.append("/cL=").append(inst.label[i]);

			newFeat = new Vector<String>();
			StringBuffer bf = new StringBuffer();
			for (String str : featureInPositionI)
			{
				bf.delete(0, bf.length());
				bf.append(str).append(curLabel);
				newFeat.add(new String(bf));
			}

			for (int j = 0; j < newFeat.size(); ++j)
			{
				if (feature2Int.containsKey(newFeat.elementAt(j)))
					featList.add(feature2Int.get(newFeat.elementAt(j)));
				else
				{
					// in training, all features should appear in feature space
					Logger logger = Logger.getLogger("seg");
					logger.error("In training, features:" + newFeat.elementAt(j) + "dosen't appear in feature space");
					throw new Exception("feature " + newFeat.elementAt(j) + "can't find in feature map!");
				}
			}

			featureInPositionI = null;
			newFeat = null;
		}

		this.feature = new int[featList.size()];
		for (int i = 0; i < featList.size(); i++)
			this.feature[i] = featList.get(i);
	}
}
