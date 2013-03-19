package ir.hit.edu.ltp.util;

import java.util.Vector;

public class MyTools
{
	public static double[] mixParameter(Vector<double[]> paramaterVec)
	{
		double[] result = new double[paramaterVec.elementAt(0).length];
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
}
