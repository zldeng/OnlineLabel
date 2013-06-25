package ir.hit.edu.ltp.basic;

import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.util.CharType;
import ir.hit.edu.ltp.util.FullCharConverter;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * class for SEG instance
 * a SEG instance contains six vectors: sentence, charType,
 * begin, middle, end and label
 * 
 * @author dzl
 * 
 */
public class SegInstance
{
	public String[] sentence;
	public int[] charType;
	public int[] begin;
	public int[] middle;
	public int[] end;
	public String[] label;

	public SegInstance(SegInstance inst)
	{
		this.sentence = inst.sentence;
		this.charType = inst.charType;
		this.begin = inst.begin;
		this.middle = inst.middle;
		this.end = inst.end;
		this.label = inst.label.clone();
	}

	/**
	 * map a raw sentence to a SEG instance
	 * for a raw sentence, the label vector is just a empty vector
	 * 
	 * @param raw_sen
	 * @param segDic
	 * @throws UnsupportedEncodingException
	 */
	public SegInstance(String rawSen, SegDic segDic) throws UnsupportedEncodingException
	{
		final int length = rawSen.trim().length();
		this.sentence = new String[length];
		this.charType = new int[length];
		this.begin = new int[length];
		this.middle = new int[length];
		this.end = new int[length];
		this.label = new String[length];

		rawSen = FullCharConverter.half2Fullchange(rawSen);
		getInfor(rawSen, segDic);

	}

	/**
	 * map a segmented sentence to a SEG instance
	 * for a segmented sentence, besides the basic vectors we also initialize
	 * the label vector with B, M, E, S
	 * 
	 * @param sen
	 * @param segDic
	 * @throws UnsupportedEncodingException
	 */
	public SegInstance(String[] sen, SegDic segDic) throws UnsupportedEncodingException
	{

		StringBuffer rawSen = new StringBuffer();
		for (String str : sen)
		{
			rawSen.append(str);
		}

		final int length = rawSen.length();
		this.sentence = new String[length];
		this.charType = new int[length];
		this.begin = new int[length];
		this.middle = new int[length];
		this.end = new int[length];
		this.label = new String[length];

		//change half-width characters to full-width characters
		String fullrawSen = FullCharConverter.half2Fullchange(new String(rawSen));

		//initialize basic information
		getInfor(fullrawSen, segDic);

		//		System.out.println("lable size: " + label.length);
		for (int i = 0, j = 0; i < sen.length; i++)
		{
			//			System.out.println(sen[i] + " " + j);
			if (sen[i].trim().length() == 1)
				label[j++] = "S";
			else
			{
				label[j++] = "B";
				for (int k = 1; k < sen[i].length() - 1; k++)
					label[j++] = "M";
				label[j++] = "E";
			}
		}
	}

	/**
	 * initialize basic vectors of a SEG instance
	 * the basic vectors contain begin, end, middle, charType
	 * 
	 * @param raw_sen
	 * @param segDic
	 */
	private void getInfor(String rawSen, SegDic segDic)
	{
		for (int i = 0; i < rawSen.length(); i++)
		{
			String ch = rawSen.charAt(i) + "";
			sentence[i] = ch;

			//1 letter
			//2 digit
			//3 punctuation
			//4 other
			int type = 4;
			if (CharType.letterSet.contains(ch))
				type = 1;
			else if (CharType.digitSet.contains(ch))
				type = 2;
			else if (CharType.punctSet.contains(ch))
				type = 3;
			charType[i] = type;

			int maxPre = 0;
			for (int len = 1; i + len <= rawSen.length() && len < segDic.maxWordLengtn; len++)
			{
				String subStr = rawSen.substring(i, i + len);

				if (segDic.containsKey(subStr) && (maxPre < len))
				{
					maxPre = len;
				}
			}
			begin[i] = maxPre;

			if (maxPre > 0 && end[i + maxPre - 1] < maxPre)
				end[i + maxPre - 1] = maxPre;
			for (int k = i + 1; k < i + maxPre - 1; k++)
				if (middle[k] < maxPre)
					middle[k] = maxPre;
		}
	}

	/**
	 * extract features in position of a SEG instance
	 * 
	 * @param position
	 * @return
	 */
	public Vector<String> extractFeaturesFromInstanceInPosition(int position)
	{
		Vector<String> featVec = new Vector<String>();

		String pre2Char = position >= 2 ? sentence[position - 2] : "_B_";
		String preChar = position >= 1 ? sentence[position - 1] : "_B_";
		String curChar = sentence[position];
		String nextChar = position < sentence.length - 1 ? sentence[position + 1] : "_E_";
		String next2Char = position < sentence.length - 2 ? sentence[position + 2] : "_E_";

		StringBuffer bf = new StringBuffer();
		//char unigram feature
		bf.delete(0, bf.length());
		bf.append("U[-2,0]=").append(pre2Char);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[-1,0]=").append(preChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[0,0]=").append(curChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[1,0]=").append(nextChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("U[2,0]=").append(next2Char);
		featVec.add(new String(bf));

		//char bigram feature
		bf.delete(0, bf.length());
		bf.append("B[-2,-1]=").append(pre2Char).append("/").append(preChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[-1,0]=").append(preChar).append("/").append(curChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[0,1]=").append(curChar).append("/").append(nextChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[1,2]=").append(nextChar).append("/").append(next2Char);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[-2,0]=").append(pre2Char).append("/").append(curChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[-1,1]=").append(preChar).append("/").append(nextChar);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("B[0,2]=").append(curChar).append("/").append(next2Char);
		featVec.add(new String(bf));

		//char trigram feature
		bf.delete(0, bf.length());
		bf.append("T[-1,0]=").append(preChar).append("/").append(curChar).append("/").append(nextChar);
		featVec.add(new String(bf));
		bf = null;

		if (preChar.equals(curChar))
			featVec.add("-1AABBT");
		if (curChar.equals(nextChar))
			featVec.add("0AABBT");

		if (pre2Char.equals(curChar))
			featVec.add("-2ABABT");
		if (preChar.equals(nextChar))
			featVec.add("-1ABABT");
		if (curChar.equals(next2Char))
			featVec.add("0ABABT");

		//char type unigram feature
		featVec.add("cT=" + charType[position]);

		//char type trigram feature
		StringBuffer trigram = new StringBuffer();

		if (position > 0)
			trigram.append(charType[position - 1]);
		else
			trigram.append("_BT_");

		trigram.append("/" + charType[position]);

		if (position < sentence.length - 1)
			trigram.append("/" + charType[position + 1]);
		else
			trigram.append("/_EL_");

		featVec.add("cTT=" + trigram);

		//dictionary feature
		featVec.add("b=" + begin[position]);
		featVec.add("m=" + middle[position]);
		featVec.add("e=" + end[position]);

		//label bigram feature
		String preLabel = position > 0 ? label[position - 1] : "_BL_";

		featVec.add("BL=" + preLabel);

		return featVec;
	}
}
