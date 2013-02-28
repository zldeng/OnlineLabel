package ir.hit.edu.ltp.basic;

import java.util.Vector;

/**
 * class for PosInstance
 * which also contains the score of the instance
 * the class is used when decoding in Viterbi algorithm
 * 
 * @author dzl
 * 
 */
public class PosItem implements Comparable<PosItem>
{
	public double score;
	public PosInstance inst;
//	public Vector<Integer> intFeat;

	public PosItem(double score, Vector<String> sentence, Vector<String> label)
	{
		this.score = score;
		this.inst = new PosInstance(sentence, label);
//		this.intFeat = intFeat;
	}

	public PosItem(PosItem item)
	{
		this.score = item.score;
		this.inst = new PosInstance(item.inst);
	}
	
	public PosItem()
	{
		this.score = Double.MIN_VALUE;
		this.inst = null;
	}

	@Override
	public int compareTo(PosItem o)
	{
		// TODO Auto-generated method stub
		if (this.score > o.score)
			return 1;
		else if (Math.abs(this.score - o.score) < 1e-4)
			return 0;
		else
			return -1;
	}
}
