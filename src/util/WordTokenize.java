package util;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class WordTokenize {
	
	private static TreeSet<String> stopwords = new TreeSet<String>();
	
	static 
	{
		try {
			String tmp1 = FileUtil.read("stop_words_en.txt");
			String[] tmp2 = tmp1.split("\\s");
			for(String s:tmp2)
			{
				stopwords.add(s);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 给英文文本分词
	 * @param text 文本
	 * @return
	 */
	public static ArrayList<String> wordTokenize(String text, boolean splitStopWord)
	{
		ArrayList<String> ret = new ArrayList<String>();
		String regex = "([A-Za-z]\\.)+" + //分割
						"|\\-?\\d+(\\.\\d+)*%?" +
						"|\\w+([-']\\w+)*" +
						"|('|\")+" +
						"|[-\\.\\(\\[\\\\]+" +
						"|\\S\\w*";
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(text);
		String tmp = null;
		if(splitStopWord == true)
		{
			while(mat.find())
			{
				tmp = mat.group();
				if(!stopwords.contains(tmp))
				{
					ret.add(tmp);
				}
			}
		}
		else
		{
			while(mat.find())
			{
				ret.add(mat.group());
			}
		}
		return ret;
	}
	
}
