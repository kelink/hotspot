package util;

import java.io.*;

public class FileUtil {
	
	public static String read(String path) throws IOException
	{
		File file = new File(path);
		return read(file);
		
	}
	
	public static String read(File file) throws IOException
	{
		StringBuilder ret = new StringBuilder();
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while((line = br.readLine()) != null)
		{
			ret.append(line+" ");
		}
		fr.close();
		br.close();
		return ret.toString();
	}
	
	public static void write(String content, String path, String fileName) throws IOException
	{
		String savePath = path+File.pathSeparator+fileName;
		BufferedWriter bw = new BufferedWriter(new FileWriter(savePath));
		bw.write(content);
		bw.close();
	}
}
