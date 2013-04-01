package ir.hit.edu.ltp.basic;

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

	public PosItem(double score, String[] sentence, String[] label)
	{
		this.score = score;
		this.inst = new PosInstance(sentence, label);
	}

	public PosItem(PosItem item)
	{
		this.score = item.score;
		this.inst = new PosInstance(item.inst);
	}

	public PosItem()
	{
		this.score = Integer.MIN_VALUE;
		this.inst = null;
	}
}
