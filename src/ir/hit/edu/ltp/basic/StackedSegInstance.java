package ir.hit.edu.ltp.basic;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import ir.hit.edu.ltp.dic.SegDic;
import ir.hit.edu.ltp.ml.SegViterbi;

public class StackedSegInstance extends SegInstance
{
	public String[] stackedLabel;

	public StackedSegInstance(StackedSegInstance stackedInst)
	{
		super(stackedInst);
		this.stackedLabel = stackedInst.stackedLabel;
	}

	public StackedSegInstance(String rawSen, SegDic segDic, SegViterbi segger) throws UnsupportedEncodingException
	{
		super(rawSen, segDic);
		this.stackedLabel = new String[rawSen.length()];
		segger.segViterbiDecode(this, stackedLabel);
	}

	public StackedSegInstance(String[] sen, SegDic segDic, SegViterbi segger) throws UnsupportedEncodingException
	{
		super(sen, segDic);
		this.stackedLabel = new String[label.length];
		segger.segViterbiDecode(this, stackedLabel);
	}

	public Vector<String> extractFeaturesFromStackedInstanceInPosition(int position)
	{
		//get basic features
		Vector<String> featVec = extractFeaturesFromInstanceInPosition(position);

		//add stacked features
		String preChar = position > 0 ? sentence[position - 1] : "_B_";
		String nextChar = position < sentence.length - 1 ? sentence[position + 1] : "_E_";
		String curChar = sentence[position];
		String curStackLabel = stackedLabel[position];
		String preStackLabel = position > 0 ? stackedLabel[position - 1] : "_BL_";
		String nextStackLabel = position < sentence.length - 1 ? stackedLabel[position + 1] : "_EL_";

		StringBuffer bf = new StringBuffer();
		bf.append("sf1=").append(preChar).append("/").append(curChar).append("/sl=").append(curStackLabel);
		featVec.add(new String(bf));

		bf.delete(0, bf.length());
		bf.append("sf2=").append(curChar).append("/").append(nextChar).append("/sl=").append(curStackLabel);
		featVec.add(new String(bf));
		bf.delete(0, bf.length());

		bf.append("sf3=").append(preChar).append("/").append(nextChar).append("/sl=").append(curStackLabel);
		featVec.add(new String(bf));
		bf.delete(0, bf.length());

		bf.append("sf4=").append(preChar).append("/").append(curChar).append("/").append(nextChar).append("/sl=")
				.append(curStackLabel);
		featVec.add(new String(bf));
		bf.delete(0, bf.length());

		bf.append("sf5=").append(preChar).append("/").append(curChar).append("/slbi1=").append(preStackLabel)
				.append("/").append(curStackLabel);
		featVec.add(new String(bf));
		bf.delete(0, bf.length());

		bf.append("sf6=").append(curChar).append("/").append(nextChar).append("/slbi2=").append(curStackLabel)
				.append("/").append(nextStackLabel);
		featVec.add(new String(bf));
		bf.delete(0, bf.length());

		bf.append("sf7=").append(preChar).append("/").append(curChar).append("/").append(nextChar).append("/sltri=")
				.append(preStackLabel).append("/").append(curStackLabel).append("/").append(nextStackLabel);
		featVec.add(new String(bf));
		bf = null;

		return featVec;
	}

}
