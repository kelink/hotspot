import java.io.File;
import java.io.IOException;
import java.util.*;

import Text.TextProcess;

import util.FileUtil;
import util.WordTokenize;

public class HotSpot {
	
	
	
	public static void main(String[] args) throws IOException
	{
		TextProcess textProcess = new TextProcess("�����ı�/");
		// 1 - ��ȡ�ļ����ִʣ������зִʴ�ŵ�articleWords��
		textProcess.getWordTFDF();
		textProcess.sortWordByTF();
		textProcess.getFeatureWord();
		textProcess.makeTextToVector();
		textProcess.getR();
		textProcess.clustering();
		textProcess.getHotSpot();
		
		// 2 - ����ÿ���ʵ�TF��DFֵ������TF��DF����
		
		
		
		
	}


}
