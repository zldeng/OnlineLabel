package ir.hit.edu.ltp.basic;

public class StackedSegItem
{
	public double score;
	public StackedSegInstance inst;

	public StackedSegItem(double score, StackedSegInstance inst)
	{
		this.score = score;
		this.inst = new StackedSegInstance(inst);
	}

	public StackedSegItem(StackedSegItem item)
	{
		this.score = item.score;
		this.inst = new StackedSegInstance(item.inst);
	}
}
