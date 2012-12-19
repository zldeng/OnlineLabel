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
public class PosItem
{
	public double score;
	public PosInstance inst;

	public PosItem(double score, Vector<String> sentence, Vector<String> label)
	{
		this.score = score;
		this.inst = new PosInstance(sentence, label);
	}

	public PosItem(PosItem item)
	{
		this.score = item.score;
		this.inst = new PosInstance(item.inst);
	}
}
