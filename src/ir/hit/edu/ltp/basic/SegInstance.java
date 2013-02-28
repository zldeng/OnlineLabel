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
	public Vector<String> sentence;
	public Vector<String> charType;
	public Vector<Integer> begin;
	public Vector<Integer> middle;
	public Vector<Integer> end;
	public Vector<String> label;

	//we restrict max word length in dictionary to  8 
	final static int MAX_LENGTH = 8;

	@SuppressWarnings("unchecked")
	public SegInstance(SegInstance inst)
	{
		this.sentence = inst.sentence;
		this.charType = inst.charType;
		this.begin = inst.begin;
		this.middle = inst.middle;
		this.end = inst.end;
		this.label = (Vector<String>) inst.label.clone();
	}

	/**
	 * map a raw sentence to a SEG instance
	 * for a raw sentence, the label vector is just a empty vector
	 * 
	 * @param raw_sen
	 * @param segDic
	 * @throws UnsupportedEncodingException
	 */
	public SegInstance(String raw_sen, SegDic segDic) throws UnsupportedEncodingException
	{
		this.sentence = new Vector<String>();
		this.charType = new Vector<String>();
		this.begin = new Vector<Integer>();
		this.middle = new Vector<Integer>();
		this.end = new Vector<Integer>();
		this.label = new Vector<String>();

		raw_sen = FullCharConverter.half2Fullchange(raw_sen);
		getInfor(raw_sen, segDic);

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
		this.sentence = new Vector<String>();
		this.charType = new Vector<String>();
		this.begin = new Vector<Integer>();
		this.middle = new Vector<Integer>();
		this.end = new Vector<Integer>();
		this.label = new Vector<String>();

		String raw_sen = "";
		for (String str : sen)
		{
			raw_sen += str;
		}

		//change half-width characters to full-width characters
		raw_sen = FullCharConverter.half2Fullchange(raw_sen);

		//initialize basic information
		getInfor(raw_sen, segDic);

		for (int i = 0; i < sen.length; i++)
		{
			if (sen[i].trim().length() == 1)
				label.add("S");
			else
			{
				label.add("B");
				for (int j = 1; j < sen[i].length() - 1; j++)
					label.add("M");
				label.add("E");
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
	private void getInfor(String raw_sen, SegDic segDic)
	{
		for (int i = 0; i < raw_sen.length(); i++)
		{
			begin.add(0);
			middle.add(0);
			end.add(0);
		}

		for (int i = 0; i < raw_sen.length(); i++)
		{
			String ch = raw_sen.charAt(i) + "";
			sentence.add(ch);

			//1 letter
			//2 digit
			//3 punctuation
			//4 other
			String type = "";
			if (CharType.letterSet.contains(ch))
				type = "1";
			else if (CharType.digitSet.contains(ch))
				type = "2";
			else if (CharType.punctSet.contains(ch))
				type = "3";
			else
				type = "4";
			charType.add(type);

			int maxPre = 0;
			for (int len = 1; i + len <= raw_sen.length() && len < MAX_LENGTH; len++)
			{
				String subStr = raw_sen.substring(i, i + len);
				
				if (segDic.containsKey(subStr) && (maxPre < len))
				{
					maxPre = len;
				}
			}
			begin.set(i, maxPre);

			if (maxPre > 0 && end.elementAt(i + maxPre - 1) < maxPre)
				end.set(i + maxPre - 1, maxPre);
			for (int k = i + 1; k < i + maxPre - 1; k++)
				if (middle.elementAt(k) < maxPre)
					middle.set(k, maxPre);
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

		String pre2Char = position >= 2 ? sentence.elementAt(position - 2) : "_BOC_";
		String preChar = position >= 1 ? sentence.elementAt(position - 1) : "_BOC_";
		String curChar = sentence.elementAt(position);
		String nextChar = position < sentence.size() - 1 ? sentence.elementAt(position + 1) : "_EOC_";
		String next2Char = position < sentence.size() - 2 ? sentence.elementAt(position + 2) : "_EOC_";

		//char unigram feature
		featVec.add("UCT[-2,0]=" + pre2Char);
		featVec.add("UCT[-1,0]=" + preChar);
		featVec.add("UCT[0,0]=" + curChar);
		featVec.add("UCT[1,0]=" + nextChar);
		featVec.add("UCT[2,0]=" + next2Char);

		//char bigram feature
		featVec.add("BCT[-2,-1]=" + pre2Char + "/" + preChar);
		featVec.add("BCT[-1,0]=" + preChar + "/" + curChar);
		featVec.add("BCT[0,1]=" + curChar + "/" + nextChar);
		featVec.add("BCT[1,2]=" + nextChar + "/" + next2Char);

		featVec.add("BCT[-2,0]=" + pre2Char + "/" + curChar);
		featVec.add("BCT[-1,1]=" + preChar + "/" + nextChar);
		featVec.add("BCT[0,2]=" + curChar + "/" + next2Char);

		//char trigram feature
		featVec.add("TCT[-1,0]=" + preChar + "/" + curChar + "/" + nextChar);

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

		//char type feature
		featVec.add("charType=" + charType.elementAt(position));

		//dictionary feature
		featVec.add("begin=" + begin.elementAt(position));
		featVec.add("middle=" + middle.elementAt(position));
		featVec.add("end=" + end.elementAt(position));

		//label bigram feature
		String preLabel = position > 0 ? label.elementAt(position - 1) : "_BL_";

		featVec.add("BiLabels=" + preLabel);

		return featVec;
	}
}
