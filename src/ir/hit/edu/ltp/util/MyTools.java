package ir.hit.edu.ltp.util;

import gnu.trove.map.hash.THashMap;
import ir.hit.edu.ltp.model.FeatureMap;
import ir.hit.edu.ltp.model.OnlineLabelModel;

import java.util.Vector;

public class MyTools
{
	//the method is used when training a model with Multi-thread
	public static float[] mixParameter(Vector<float[]> paramaterVec)
	{
		float[] result = new float[paramaterVec.elementAt(0).length];
		for (int i = 0; i < result.length; i++)
		{
			for (int j = 0; j < paramaterVec.size(); j++)
			{
				result[i] += paramaterVec.elementAt(j)[i];
			}
			result[i] /= paramaterVec.size();
		}

		return result;
	}

	public static long[] mixUseNum(Vector<long[]> useVec)
	{
		long[] result = new long[useVec.elementAt(0).length];
		for (int i = 0; i < result.length; i++)
		{
			for (int j = 0; j < useVec.size(); j++)
				result[i] += useVec.elementAt(j)[i];
		}

		return result;
	}

	//we merge feature weight using  this method when retraining a model
	public static OnlineLabelModel mixModel(OnlineLabelModel model1, OnlineLabelModel model2) throws Exception
	{
		THashMap<String, Integer> label2int = new THashMap<String, Integer>();
		THashMap<Integer, String> int2label = new THashMap<Integer, String>();
		int num = 0;
		for (String feat : model1.featMap.label2Int.keySet())
		{
			label2int.put(feat, num);
			int2label.put(num, feat);
			num++;
		}
		for (String feat : model2.featMap.label2Int.keySet())
		{
			if (!label2int.keySet().contains(feat))
			{
				label2int.put(feat, num);
				int2label.put(num, feat);
				num++;
			}
		}

		THashMap<String, Integer> feat2int = new THashMap<String, Integer>();
		num = 0;
		for (String feat : model1.featMap.feature2Int.keySet())
		{
			feat2int.put(feat, num);
			num++;
		}
		for (String feat : model2.featMap.feature2Int.keySet())
		{
			if (!feat2int.containsKey(feat))
			{
				feat2int.put(feat, num);
				num++;
			}
		}

		FeatureMap featMap = new FeatureMap(feat2int, label2int, int2label);
		float[] parameter = new float[feat2int.size()];
		for (String feat : featMap.feature2Int.keySet())
		{
			int id = feat2int.get(feat);
			//how to merge weight value?
			float value1 = model1.featMap.feature2Int.keySet().contains(feat) ? model1.parameter[model1.featMap.feature2Int
					.get(feat)] : 0;
			float value2 = model2.featMap.feature2Int.keySet().contains(feat) ? model2.parameter[model2.featMap.feature2Int
					.get(feat)] : 0;

			//method 1
			//			parameter[id] = (value1 + value2) / 2;

			//method 2
			//this method is work well than method 1 
			if (Math.abs(value1) < 0.01)
				parameter[id] = value2;
			else if (Math.abs(value2) < 0.01)
				parameter[id] = value1;
			else
				parameter[id] = (value1 + value2) / 2;
		}

		OnlineLabelModel model = new OnlineLabelModel(featMap, parameter);
		return model;
	}

	/**
	 * 
	 * @param model1
	 *            model trained using new instances
	 * @param newPre
	 *            performance of model1 for dev file
	 * @param model2
	 *            base model
	 * @param basePre
	 *            performance of model2 for dev file
	 * @return
	 * @throws Exception
	 */
	public static OnlineLabelModel mixModel(OnlineLabelModel model1, double newPre, OnlineLabelModel model2, double basePre)
			throws Exception
	{
		THashMap<String, Integer> label2int = new THashMap<String, Integer>();
		THashMap<Integer, String> int2label = new THashMap<Integer, String>();
		int num = 0;
		for (String feat : model1.featMap.label2Int.keySet())
		{
			label2int.put(feat, num);
			int2label.put(num, feat);
			num++;
		}
		for (String feat : model2.featMap.label2Int.keySet())
		{
			if (!label2int.keySet().contains(feat))
			{
				label2int.put(feat, num);
				int2label.put(num, feat);
				num++;
			}
		}

		THashMap<String, Integer> feat2int = new THashMap<String, Integer>();
		num = 0;
		for (String feat : model1.featMap.feature2Int.keySet())
		{
			feat2int.put(feat, num);
			num++;
		}
		for (String feat : model2.featMap.feature2Int.keySet())
		{
			if (!feat2int.containsKey(feat))
			{
				feat2int.put(feat, num);
				num++;
			}
		}

		FeatureMap featMap = new FeatureMap(feat2int, label2int, int2label);
		float[] parameter = new float[feat2int.size()];

		//set model weight according to model precision for dev file
		double weight1 = newPre / (newPre + basePre);
		double weight2 = basePre / (newPre + basePre);

		for (String feat : featMap.feature2Int.keySet())
		{
			int id = feat2int.get(feat);
			//how to merge weight value?
			float value1 = model1.featMap.feature2Int.keySet().contains(feat) ? model1.parameter[model1.featMap.feature2Int
					.get(feat)] : 0;
			float value2 = model2.featMap.feature2Int.keySet().contains(feat) ? model2.parameter[model2.featMap.feature2Int
					.get(feat)] : 0;

			//method 1
			//			parameter[id] = (value1 + value2) / 2;

			//method 2
			//this method is work well than method 1 
			if (Math.abs(value1) < 0.01)
				parameter[id] = value2;
			else if (Math.abs(value2) < 0.01)
				parameter[id] = value1;
			else
			{
				parameter[id] = (float) (weight1 * value1 + weight2 * value2);
			}
		}

		OnlineLabelModel model = new OnlineLabelModel(featMap, parameter);
		return model;
	}

	/**
	 * get segmented string according raw sentence and decoding label
	 * 
	 * @param raw_sen
	 * @param label
	 * @return
	 * @throws Exception
	 */
	public static String rawSentence2SegSentence(String rawSen, String[] label) throws Exception
	{
		if (rawSen.length() != label.length)
		{
			throw new Exception("the raw sentence length is not same to label size!");
		}

		StringBuffer result = new StringBuffer();
		result.append(rawSen.charAt(0));

		for (int i = 1; i < label.length; i++)
		{
			if (label[i].equals("B") || label[i].equals("S"))
				result.append(" ").append(rawSen.charAt(i));
			else
				result.append(rawSen.charAt(i));
		}

		String resultStr = new String(result);
		result = null;

		return resultStr;
	}
}
