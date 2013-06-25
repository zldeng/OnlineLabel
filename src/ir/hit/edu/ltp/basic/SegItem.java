package ir.hit.edu.ltp.basic;

/**
 * class for SEG item
 * a SEG item contains a instance and the score of the instance
 * this class is used in decoding
 * 
 * @author dzl
 * 
 */
public class SegItem
{
	public double score;
	public SegInstance inst;

	public SegItem(double score, SegInstance inst)
	{
		this.score = score;
		this.inst = new SegInstance(inst);
	}

	public SegItem(SegItem item)
	{
		this.score = item.score;
		this.inst = new SegInstance(item.inst);
	}

}
