import java.io.File;
import java.io.IOException;
import java.util.*;

import com.gdufs.jy.Text.TextProcess;
import com.jy.gdufs.cluster.OnePass;


import util.FileUtil;
import util.WordTokenize;

/**
 * 
 * 程序入口
 *
 */
public class HotSpot {
	
	
	
	public static void main(String[] args) throws IOException
	{
		//对预处理的材料一趟聚类~~
		new OnePass().doProcess("corpus/process_IncludeNotSpecial/original");
		
		
	}


}
