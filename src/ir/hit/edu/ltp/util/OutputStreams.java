package ir.hit.edu.ltp.util;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class OutputStreams
{
	private PrintWriter wr;
	public OutputStreams(final String outFile) throws UnsupportedEncodingException, FileNotFoundException
	{
		wr = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
	}
	
	public synchronized void writerLine(final String str)
	{
		wr.write(str);
		wr.flush();
	}
}