import java.io.File;
import java.io.IOException;
import java.util.*;

import com.gdufs.jy.Text.TextProcess;
import com.jy.gdufs.cluster.OnePass;


import util.FileUtil;
import util.WordTokenize;

/**
 * 
 * �������
 *
 */
public class HotSpot {
	
	
	
	public static void main(String[] args) throws IOException
	{
		//��Ԥ����Ĳ���һ�˾���~~
		new OnePass().doProcess("corpus/process_IncludeNotSpecial/original");
		
		
	}


}
