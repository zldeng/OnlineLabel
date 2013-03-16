package ir.hit.edu.ltp.util;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

//in this class, we must use synchronized to avoid different threads read
//content from the input stream at the same time
public class InputStreams
{
	private BufferedReader br ;
	
	public InputStreams (String inputFile) throws UnsupportedEncodingException, FileNotFoundException
	{
		this.br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile),"UTF-8"));  
	}
	
	public synchronized String readLine() throws IOException
	{
//		System.out.println("add lock");
		String line = br.readLine();
//		System.out.println("cancle lock");
		return line;
	}
}
