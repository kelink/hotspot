import java.io.File;
import java.io.IOException;
import java.util.*;

import Text.TextProcess;

import util.FileUtil;
import util.WordTokenize;

public class HotSpot {
	
	
	
	public static void main(String[] args) throws IOException
	{
		TextProcess textProcess = new TextProcess("测试文本/");
		// 1 - 读取文件并分词，把所有分词存放到articleWords中
		textProcess.getWordTFDF();
		textProcess.sortWordByTF();
		textProcess.getFeatureWord();
		textProcess.makeTextToVector();
		textProcess.getR();
		textProcess.clustering();
		textProcess.getHotSpot();
		
		// 2 - 计算每个词的TF和DF值，按照TF和DF排序
		
		
		
		
	}


}
