package ir.hit.edu.ltp.util;

import java.lang.reflect.Array;

/**
 * 最小堆
 * 
 * @author Ayou
 * 
 * @param <T>
 */
public class MinHeap<T extends Comparable<T>>
{

	private int maxSize;
	private int curSize;
	public Node[] heapArray;

	@SuppressWarnings("unchecked")
	public MinHeap(int size)
	{
		maxSize = size;
		curSize = 0;
		heapArray = (Node[]) Array.newInstance(Node.class, maxSize);
	}

	public void push(T key)
	{
		if (curSize < maxSize)
		{
			heapArray[curSize] = new Node(key);
			filterUp(curSize);
			curSize++;
		}
		else
		{
			if (heapArray[0].key.compareTo(key) < 0)
			{
				heapArray[0].key = key;
				filterDown(0);
			}
		}
	}

	private void filterUp(int start)
	{
		Node cur = heapArray[start];
		int parentIdx = (start - 1) / 2;
		while (start > 0)
		{
			if (heapArray[parentIdx].key.compareTo(heapArray[start].key) < 0)
			{
				break;
			}
			else
			{
				heapArray[start] = heapArray[parentIdx];
				start = parentIdx;
				parentIdx = (start - 1) / 2;
				heapArray[start] = cur;
			}
		}
	}

	private void filterDown(int start)
	{
		Node cur = heapArray[start];
		int childIdx = start * 2 + 1;
		while (childIdx < curSize)
		{
			if (childIdx < curSize - 1 && heapArray[childIdx].key.compareTo(heapArray[childIdx + 1].key) > 0)
			{
				//childIdx is the index of lesser node
				childIdx++;
			}
			if (heapArray[childIdx].key.compareTo(heapArray[start].key) > 0)
			{
				break;
			}
			else
			{
				heapArray[start] = heapArray[childIdx];
				start = childIdx;
				childIdx = start * 2 + 1;
				heapArray[start] = cur;
			}
		}
	}

	public T pop()
	{
		Node cur = heapArray[0];
		heapArray[0] = heapArray[curSize - 1];
		heapArray[curSize - 1] = null;
		curSize--;
		filterDown(0);
		return cur == null ? null : cur.key;
	}

//	public void printer()
//	{
//		for (Node n : heapArray)
//		{
//			if (n == null)
//				break;
//			System.out.print(n.key + " ");
//		}
//		System.out.println("\n");
//	}

	class Node
	{
		private T key;

		Node(T e)
		{
			this.key = e;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		MinHeap<Hello> hp = new MinHeap<Hello>(5);

		//		hp.push(new Hello(-1, -2));
		//		hp.push(new Hello(2, 1));
		//		hp.push(new Hello(-1, 2));
		//		hp.push(new Hello(3, 1));
		//		hp.push(new Hello(3, 3));

//		hp.printer();
	}

}
