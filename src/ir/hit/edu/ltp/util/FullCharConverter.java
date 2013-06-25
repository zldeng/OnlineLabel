package ir.hit.edu.ltp.util;

import java.io.UnsupportedEncodingException;

public class FullCharConverter
{
	//full char to half char
	public static final String full2HalfChange(String fullStr) throws UnsupportedEncodingException
	{
		StringBuffer outStrBuf = new StringBuffer("");
		String Tstr = "";
		byte[] b = null;

		for (int i = 0; i < fullStr.length(); i++)
		{
			Tstr = fullStr.substring(i, i + 1);
			// change blank space 
			if (Tstr.equals("　"))
			{
				outStrBuf.append(" ");
				continue;
			}

			b = Tstr.getBytes("unicode");
			if (b[2] == -1)
			{
				b[3] = (byte) (b[3] + 32);
				b[2] = 0;
				outStrBuf.append(new String(b, "unicode"));
			}
			else
			{
				outStrBuf.append(Tstr);
			}
		}

		return outStrBuf.toString();
	}

	// half char to full char
	public static final String half2Fullchange(String halfStr) throws UnsupportedEncodingException
	{

		StringBuffer outStrBuf = new StringBuffer("");

		String Tstr = "";
		byte[] b = null;
		for (int i = 0; i < halfStr.length(); i++)
		{
			Tstr = halfStr.substring(i, i + 1);
			if (Tstr.equals(" "))
			{
				outStrBuf.append(Tstr);
				continue;
			}

			b = Tstr.getBytes("unicode");
			if (b[2] == 0)
			{
				b[3] = (byte) (b[3] - 32);
				b[2] = -1;
				outStrBuf.append(new String(b, "unicode"));
			}
			else
			{
				outStrBuf.append(Tstr);
			}
		}
		return outStrBuf.toString();
	}

	public static void main(String[] args) throws UnsupportedEncodingException
	{
		//full to half
		String QJstr = "hello!！ 全角转换，ＤＡＯ ５３２３２　";
		String result = full2HalfChange(QJstr);
		System.out.println(QJstr);
		System.out.println(result);
		System.out.println("------------------------------------");

		//half to full
		String str = "java 汽车 召回 2345";
		System.out.println(str);
		System.out.println(half2Fullchange(str));
	}

}
