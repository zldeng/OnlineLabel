package ir.hit.edu.ltp.basic;

import java.util.Vector;

import ir.hit.edu.ltp.util.*;
import ir.hit.edu.ltp.dic.*;

/**
 * POS instance class
 * each POS instance contains two vectors
 * a vector of words and a vector of POS
 * 
 * @author dzl
 */
public class PosInstance
{
	public String[] words;
	public String[] cluster;
	public String[] label;

	public PosInstance(PosInstance inst)
	{
		this.words = inst.words;
		this.cluster = inst.cluster;
		this.label = inst.label.clone();
	}

	public PosInstance(String[] words, String[] cluster,String[] label)
	{
		this.words = words;
		this.cluster = cluster;
		this.label = label;
	}

	/**
	 * extract basic POS feature at position of instance
	 * the features don't include features relating to dictionary
	 * 
	 * @param position
	 * @return feature vector which contains all features in position
	 */
	private Vector<String> extractBasicFeatures(int position)
	{
		Vector<String> featVec = new Vector<String>();

		String pre2Word = position >= 2 ? words[position - 2] : "_B_";
		String preWord = position >= 1 ? words[position - 1] : "_B_";
		String curWord = words[position];

		//		System.out.println("cur: " + curWord);
		String nextWord = position <= words.length - 2 ? words[position + 1] : "_E_";
		String next2Word = position <= words.length - 3 ? words[position + 2] : "_E_";

		StringBuffer bf = new StringBuffer();
		bf.delete(0, bf.length());
		bf.append("U[-2,0]=").append(pre2Word);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[-1,0]=").append(preWord);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[0,0]=").append(curWord);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[1,0]=").append(nextWord);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[2,0]=").append(next2Word);
		featVec.add(new String(bf));

		// wiwi+1(i = − 1, 0)
		bf.delete(0, bf.length());
		bf.append("B[-1,0]=").append(preWord).append("/").append(curWord);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[0,1]=").append(curWord).append("/").append(nextWord);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[-1,1]=").append(preWord).append("/").append(nextWord);
		featVec.add(new String(bf));

		// last char(w−1)w0
		String lastChar = position >= 1 ? "" + words[position - 1].charAt(words[position - 1].length() - 1) : "_BC_";
		bf.delete(0, bf.length());
		bf.append("CW[-1,0]=").append(lastChar).append("/").append(curWord);
		featVec.add(new String(bf));

		// w0 ﬁrst_char(w1)
		String nextChar = position <= words.length - 2 ? "" + words[position + 1].charAt(0) : "_EC_";
		bf.delete(0, bf.length());
		bf.append("CW[1,0]=").append(curWord).append("/").append(nextChar);
		featVec.add(new String(bf));

		int length = curWord.length();

		// ﬁrstchar(w0)lastchar(w0)
		bf.delete(0, bf.length());
		bf.append("BE=").append(curWord.charAt(0)).append("/").append(curWord.charAt(length - 1));
		featVec.add(new String(bf));

		// prefix
		featVec.add("pf=" + curWord.substring(0, 1));

		if (length > 1)
			featVec.add("pf=" + curWord.substring(0, 2));

		if (length > 2)
			featVec.add("pf=" + curWord.substring(0, 3));

		// sufﬁx(w0, i)(i = 1, 2, 3)
		featVec.add("sf=" + curWord.charAt(length - 1));

		if (length > 1)
			featVec.add("sf=" + curWord.substring(length - 2));

		if (length > 2)
			featVec.add("sf=" + curWord.substring(length - 3));

		// length
		if (length >= 5)
			featVec.add("le=" + 5);
		else
			featVec.add("le=" + length);

		// label feature
		String preLabel;
		if (position >= 1)
		{
			preLabel = label[position - 1];
		}
		else
			preLabel = "_BL_";

		featVec.add("BL=" + preLabel);

		for (int i = 0; i < curWord.length(); i++)
		{
			String prefix = curWord.substring(0, 1) + curWord.charAt(i) + "";
			featVec.add("p2f=" + prefix);
			String surfix = curWord.substring(curWord.length() - 1) + curWord.charAt(i) + "";
			featVec.add("s2f=" + surfix);

			if ((i < curWord.length() - 1) && (curWord.charAt(i) == curWord.charAt(i + 1)))
				featVec.add("dulC=" + curWord.substring(i, i + 1));
			if ((i < curWord.length() - 2) && (curWord.charAt(i) == curWord.charAt(i + 2)))
				featVec.add("dul2C=" + curWord.substring(i, i + 1));
		}

		boolean isDigit = true;
		for (int i = 0; i < curWord.length(); i++)
		{
			if (!CharType.digitSet.contains(curWord.charAt(i) + ""))
			{
				isDigit = false;
				break;

			}
		}
		if (isDigit)
			featVec.add("wT=d");

		boolean isPunt = true;
		for (int i = 0; i < curWord.length(); i++)
		{
			if (!CharType.punctSet.contains(curWord.charAt(i) + ""))
			{
				isPunt = false;
				break;
			}
		}
		if (isPunt)
		{
			featVec.add("wT=p");
		}

		boolean isLetter = true;
		for (int i = 0; i < curWord.length(); i++)
		{
			if (!CharType.letterSet.contains(curWord.charAt(i) + ""))
			{
				isLetter = false;
				break;
			}
		}
		if (isLetter)
			featVec.add("wT=l");
		//word cluster feature
		String preCluster = position > 0 ? cluster[position-1] : "_BC_";
		String curCluster = cluster[position];
		String nextCluster = position < cluster.length -1 ? cluster[position + 1] : "_EC_";
		
		bf.delete(0, bf.length());
		bf.append("clt=").append(preCluster).append("/").append(curCluster).append("/").append(nextCluster);
		featVec.add(new String(bf));
		
		String sPreCluster = preCluster.length() >= 6 ? preCluster.substring(0, 6) : preCluster;
		String sCurCluster = curCluster.length() >= 6 ? curCluster.substring(0, 6) : curCluster;
		String sNextCluster = nextCluster.length() >= 6 ? nextCluster.substring(0, 6) : nextCluster;
		
		bf.delete(0, bf.length());
		bf.append("sclt=").append(sPreCluster).append("/").append(sCurCluster).append("/").append(sNextCluster);
		featVec.add(new String(bf));
		bf = null;

		return featVec;
	}

	/**
	 * extract all POS feature at position of instance including features
	 * associated with dictionary
	 * 
	 * @param position
	 * @param posDic
	 * @return
	 */
	public Vector<String> extractFeaturesFromInstanceInPosition(int position, PosDic posDic)
	{
		Vector<String> featVec = extractBasicFeatures(position);

		String curWord = words[position];
		// POS dictionary feature
		if (posDic.containsKey(curWord))
		{
			Vector<String> posSet = posDic.getPos(curWord);
			for (int i = 0; i < posSet.size(); i++)
			{
				featVec.add("dP=" + posSet.elementAt(i));
			}
		}

		return featVec;
	}
}
