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
	public Vector<String> words;
	public Vector<String> label;

	@SuppressWarnings("unchecked")
	public PosInstance(PosInstance inst)
	{
		this.words = inst.words;
		this.label = (Vector<String>) inst.label.clone();
	}

	public PosInstance(Vector<String> words, Vector<String> label)
	{
		this.words = words;
		this.label = label;
	}

	/**
	 * extract basic POS feature at position index of instance
	 * the features don't include features associated with dictionary
	 * 
	 * @param position
	 * @return feature vector which contains all features in position of the
	 *         words
	 */
	Vector<String> extractBasicFeatures(int position)
	{
		Vector<String> featVec = new Vector<String>();
		String pre2Word;
		if (position >= 2)
			pre2Word = words.elementAt(position - 2);
		else
			pre2Word = "_BEGIN_";

		String preWord;
		if (position >= 1)
			preWord = words.elementAt(position - 1);
		else
			preWord = "_BEGIN_";

		String curWord = words.elementAt(position);
		String nextWord;
		if (position <= words.size() - 2)
			nextWord = words.elementAt(position + 1);
		else
			nextWord = "_END_";

		String next2Word;
		if (position <= words.size() - 3)
			next2Word = words.elementAt(position + 2);
		else
			next2Word = "_END_";

		featVec.add("UWT[-2,0]=" + pre2Word);
		featVec.add("UWT[-1,0]=" + preWord);
		featVec.add("UWT[0,0]=" + curWord);
		featVec.add("UWT[1,0]=" + nextWord);
		featVec.add("UWT[2,0]=" + next2Word);

		// wiwi+1(i = − 1, 0)
		featVec.add("BWT[-1,0]=" + preWord + "/" + curWord);
		featVec.add("BWT[0,1]=" + curWord + "/" + nextWord);

		featVec.add("B2WT[-1,1]=" + preWord + "/" + nextWord);

		// last char(w−1)w0
		String lastChar;
		if (position >= 1)
			lastChar = "" + words.elementAt(position - 1).charAt(words.elementAt(position - 1).length() - 1);
		else
			lastChar = "_BOC_";
		featVec.add("CW[-1,0]=" + lastChar + "/" + curWord);

		// w0 ﬁrst_char(w1)
		String nextChar;
		if (position <= words.size() - 2)
			nextChar = "" + words.elementAt(position + 1).charAt(0);
		else
			nextChar = "_EOC_";
		featVec.add("CW[1,0]=" + curWord + "/" + nextChar);

		int length = curWord.length();

		// ﬁrstchar(w0)lastchar(w0)
		featVec.add("BE=" + curWord.charAt(0) + "/" + curWord.charAt(length - 1));

		// prefix
		featVec.add("prefix=" + curWord.substring(0, 1));

		if (length > 1)
			featVec.add("prefix=" + curWord.substring(0, 2));

		if (length > 2)
			featVec.add("prefix=" + curWord.substring(0, 3));

		// sufﬁx(w0, i)(i = 1, 2, 3)
		featVec.add("suffix=" + curWord.charAt(length - 1));

		if (length > 1)
			featVec.add("suffix=" + curWord.substring(length - 2));

		if (length > 2)
			featVec.add("suffix=" + curWord.substring(length - 3));

		// length
		if (length >= 5)
			featVec.add("length=" + 5);
		else
			featVec.add("length=" + length);

		// label feature
		String preLabel;
		if (position >= 1)
		{
			preLabel = label.elementAt(position - 1);
		}
		else
			preLabel = "_BL_";

		featVec.add("BiLabels=" + preLabel);

		for (int i = 0; i < curWord.length(); i++)
		{
			String prefix = curWord.substring(0, 1) + curWord.charAt(i) + "";
			featVec.add("pre2fix=" + prefix);
			String surfix = curWord.substring(curWord.length() - 1) + curWord.charAt(i) + "";
			featVec.add("sur2fix=" + surfix);

			if ((i < curWord.length() - 1) && (curWord.charAt(i) == curWord.charAt(i + 1)))
				featVec.add("dulChar=" + curWord.substring(i, i + 1));
			if ((i < curWord.length() - 2) && (curWord.charAt(i) == curWord.charAt(i + 2)))
				featVec.add("dul2Char=" + curWord.substring(i, i + 1));
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
			featVec.add("wordType=digit");

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
			featVec.add("wordType=punct");
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
			featVec.add("wordType=letter");

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
	public Vector<String> extractFeaturesFromInstance(int position, PosDic posDic)
	{
		Vector<String> featVec = extractBasicFeatures(position);

		String curWord = words.elementAt(position);
		// POS dictionary feature
		if (posDic.containsKey(curWord))
		{
			Vector<String> posSet = posDic.getPos(curWord);
			for (int i = 0; i < posSet.size(); i++)
			{
				featVec.add("dicPos=" + posSet.elementAt(i));
			}
		}

		return featVec;
	}

	public String toString()
	{
		String str = "";
		for (int i = 0; i < words.size(); i++)
			str += words.elementAt(i) + "/" + label.elementAt(i) + " ";

		return str.trim();
	}

}
