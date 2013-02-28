package ir.hit.edu.ltp.util;

import java.util.Collections;
import java.util.Vector;

public class Hello implements Comparable<Hello>
{
	int i,j;
	public Hello(int i,int j)
	{
		this.i = i;
		this.j = j;
	}
//	public int compareTo(Object o)
//	{
//		// TODO Auto-generated method stub
//		if (this.i > ((Hello)o).i)
//			return 1;
//		else if (this.i < ((Hello)o).i)
//			return -1;
//		else if (this.j >= ((Hello)o).j)
//			return 1;
//		else
//			return -1;
//	}
	
	public String toString()
	{
		return "i: " + i +" j: " + j;
	}
	
	public static void main(String[] args)
	{
		Vector<Hello> vec = new Vector<Hello>();
		vec.add(new Hello(1,2));
		vec.add(new Hello(-1,2));
		vec.add(new Hello(1,1));
		vec.add(new Hello(0,3));
		
		System.out.println(vec);
		Collections.sort(vec);
		System.out.println(vec);
	}
	@Override
	public int compareTo(Hello o)
	{
		// TODO Auto-generated method stub
		if (this.j > o.j)
			return 1;
		else if (this.j == o.j)
			return 0;
		else
			return -1;
	}
}
