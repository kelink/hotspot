package com.jy.gdufs.cluster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import util.FileUtil;
import util.WordTokenize;

import com.gdufs.jy.Text.Article;
import com.gdufs.jy.Text.C;
import com.gdufs.jy.Text.CenterPoint;
import com.gdufs.jy.Text.TFDF;
import com.gdufs.jy.Text.TextVector;
import com.gdufs.jy.Text.WordTFDF;


/**
 * 一趟聚类算法
 * @author Administrator
 *
 */
public class OnePass {

	private int size; //文本数目
	private double r; //阈值
	private int FeatureWordSize = 0;
	private ArrayList<Article> articles  //文本中的词
							= new ArrayList<Article>();
	private TreeMap<String, TFDF> map_WordTFDF
							= new TreeMap<String, TFDF>(); // 存放所有单词的TF与DF -- 临时用
	private ArrayList<WordTFDF> list_WordTFDF
	 						= new ArrayList<WordTFDF>(); //用于排序  -- 临时用
	private ArrayList<TextVector> list_TextVector
							= new ArrayList<TextVector>(); //存放文本向量
	private ArrayList<CenterPoint> list_CenterPoint 
							= new ArrayList<CenterPoint>();
	private TreeMap<String, TFDF> map_HotSpot 
							= new TreeMap<String, TFDF>();
	
	public void doProcess(String dirPath)
	{
		// 1 - 读取所有文件到articles里面
		System.out.println("1");
		FileReader fr = null;
		BufferedReader br = null;
		File dirfile = null;
		try
		{
			dirfile = new File(dirPath);
			if(!dirfile.isDirectory())
			{
				System.out.println("not a directory");
				System.exit(0);
			}
			File[] fileList = dirfile.listFiles();
			int countFileNumber = 0;
			for (int i = 0; i < fileList.length; i++) {
				String fileTrueName = fileList[i].getName();
				if(!fileTrueName.contains("stemed"))
					continue;
				countFileNumber++;
				fr = new FileReader(fileList[i]);
				br = new BufferedReader(fr);
				Article article = new Article();
				ArrayList<String> words = new ArrayList<String>();
				String line = "";
				while((line = br.readLine()) != null)
				{
					words.add(line);
				}
				article.words = words;
				article.filename = fileList[i].getName();
				article.index = i;
				this.articles.add(article);
			}
			size = countFileNumber;
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		// 2 - 获取tfdf
		System.out.println("2 计算tfdf");
		getWordTFDF();
		// 3 - 
		System.out.println("3 获取特征词");
		getFeatureWord();
		// 4 - 
		System.out.println("4 生成向量");
		makeTextToVector();
		// 5 -
		System.out.println("5 获得阈值");
		getR();
		// 6 
		System.out.println("6 聚类");
		clustering();
		// 7
		System.out.println("7 把热点词存到文本 onepassresult.txt");
		getHotSpot();
	}
	
	public void getWordTFDF()
	{
		int flag = -1;
		for(int i=0; i<articles.size(); ++i)
		{
			ArrayList<String> list = articles.get(i).words;
			flag++; //文章号判断标记
			for(String word : list)
			{
				if(map_WordTFDF.containsKey(word))
				{
					TFDF tmp = map_WordTFDF.get(word);
					tmp.setTF(tmp.getTF()+1);
					if(tmp.getFlag() != flag)
					{
						tmp.setDF(tmp.getDF()+1);
						tmp.setFlag(flag);
					}
				}
				else
				{
					TFDF tmp = new TFDF(1, 1, flag);
					map_WordTFDF.put(word, tmp);
				}
			}
		}
	}
	
	/**
	 * 把词语按照tf值来排序
	 */
	public void sortWordByTF()
	{
		WordTFDF wordTFDF;
		Set<Map.Entry<String, TFDF>> set = map_WordTFDF.entrySet();
		for(Map.Entry<String, TFDF> entry : set)
		{
			TFDF tfdf = entry.getValue();
			wordTFDF = new WordTFDF(entry.getKey(), tfdf.getTF(), tfdf.getDF());
			list_WordTFDF.add(wordTFDF);
		}
		//按照TF从大到小排序
		Collections.sort(list_WordTFDF, new Comparator<WordTFDF>(){
			@Override
			public int compare(WordTFDF w1, WordTFDF w2)
			{
				return -(w1.getTF()-w2.getTF());
			}
		});
	}
	
	/**
	 * 获取特征词
	 */
	public void getFeatureWord()
	{
		//map_WordTFDF.clear(); //清空原来的东西
		String word = null;
		TFDF tfdf = null;
		
		WordTFDF wordTFDF;
		Set<Map.Entry<String, TFDF>> set = map_WordTFDF.entrySet();
		for(Map.Entry<String, TFDF> entry : set)
		{
			tfdf = entry.getValue();
			if(tfdf.getTF()>=3){
				//选择词频>=3的词
				wordTFDF = new WordTFDF(entry.getKey(), tfdf.getTF(), tfdf.getDF());
				list_WordTFDF.add(wordTFDF);
			}
		}
		FeatureWordSize = list_WordTFDF.size();
		
	}
	
	/**
	 * 构造文本向量，算出tfidf
	 * 相似度计算采用余弦相似度
	 */
	public void makeTextToVector()
	{
		TreeMap<String, TFDF> map_oneNewWords = new TreeMap<String, TFDF>();
		for(int i=0; i<size; ++i)
		{
			// 1 - 把特征词放进map_oneNewWords里面，利用来构造向量
			map_oneNewWords.clear();
			for(int j=0; j<list_WordTFDF.size(); ++j)
			{
				TFDF tfdf = new TFDF(0, list_WordTFDF.get(j).getDF(), -1);
				map_oneNewWords.put(list_WordTFDF.get(j).getWord(), tfdf);
			}
			
			// 2 - 统计特征词数
			Article article = articles.get(i);
			ArrayList<String> words = article.words;
			for(String word : words)
			{
				if(map_oneNewWords.containsKey(word))
				{
					TFDF tfdf = map_oneNewWords.get(word);
					tfdf.setTF(tfdf.getTF()+1);
					map_oneNewWords.put(word, tfdf);
				}
			}
			// 3 - 计算tfidf值
			Set<Map.Entry<String, TFDF>> set = map_oneNewWords.entrySet();
			Iterator<Map.Entry<String, TFDF>> iter = set.iterator();
			TextVector textVector = new TextVector();
			textVector.filename = article.filename;
			textVector.tfidf = new double[FeatureWordSize];
			int k=0;
			double normalized = 0.0;
			double tmp = 0;
			double tmp1 = 0;
			while(iter.hasNext())
			{
				TFDF tfdf = iter.next().getValue();
				textVector.tfidf[k] = tfdf.getTF() * Math.log((double)size/tfdf.getDF() + 0.01);
				normalized += textVector.tfidf[k] * textVector.tfidf[k];
				
				tmp1 += textVector.tfidf[k];
				k++;
			}
			System.out.println("i="+i+" before normalized tfidf_sum="+tmp1);
			System.out.printf("i=%d before sqrt  normalized=%f%n", i,normalized);
			normalized = Math.sqrt(normalized);
			System.out.printf("i=%d after  sqrt  normalized=%f%n", i,normalized);
			if(normalized!=0)
			{
				for(int j=0; j<FeatureWordSize; ++j)
				{
					if(textVector.tfidf[j] > 0)
					{
						textVector.tfidf[j] /= normalized;
						tmp+=textVector.tfidf[j];
					}
					
				}
			}
			System.out.println("i="+i+" after tfidf_sum="+tmp);
			textVector.clusterNumber = 0;
			list_TextVector.add(textVector);
		}
	}
	
	/**
	 * 生成阈值
	 */
	public void getR()
	{
		int rand1;
		int rand2;
		double fenzi;
		double fenmu1;
		double fenmu2;
		this.r = 0.0;
		TextVector textVector1, textVector2;
		int i=(int)(size);
		while((i--)!=0)
		{
			rand1 = (int)(Math.random() * size);
			rand2 = (int)(Math.random() * size);
			fenzi = fenmu1 = fenmu2 = 0.0;
			textVector1 = list_TextVector.get(rand1);
			textVector2 = list_TextVector.get(rand2);
			for(int j=0; j<FeatureWordSize; ++j)
			{
				fenzi += textVector1.tfidf[j] * textVector2.tfidf[j];
				fenmu1 += textVector1.tfidf[j] * textVector1.tfidf[j];
				fenmu2 += textVector2.tfidf[j] * textVector2.tfidf[j];
 			}
			if(fenmu1 !=0 && fenmu2 != 0)
			{
				r += fenzi/ Math.sqrt(fenmu1 * fenmu2);
			}
		}
		r = C.MULTI * r / (double)(size * C.PRECENT);
		System.out.printf("阈值r=%f%n", this.r);
	}
	
	
	public void createNewCluster(TextVector textVector, int num)
	{
		textVector.clusterNumber = num;
		CenterPoint point = new CenterPoint();
		point.clusterNumber  = num;
		point.newsNumber = 1;
		point.tfidf = new double[FeatureWordSize];
		//质心
		for(int i=0; i<FeatureWordSize; ++i)
		{
			point.tfidf[i] = textVector.tfidf[i];
		}
		list_CenterPoint.add(point);
	}
	
	/**
	 *一趟聚类,算法如下：
	 *	1. 初始时，聚类集合为空，读入一个新的对象； 
	 *	2. 以这个对象构造一个新的类； 
	 *	3. 若已到末尾，则转 6，否则读入新对象，利用给定的相似度定义，计算它与每个已有类间的相似度，并选择最
		   大的相似度； 
	 *	4. 若最大相似度小于给定的阈值 r ，转 2； 
	 *	5. 否则将该对象并入具有最大相似度的类中，并更新该类的各分类属性值的统计频度，转 3； 
	 *	6. 结束。
	 */
	public void clustering()
	{
		CenterPoint maxSimPoint = new CenterPoint();
		int clusterNumber = 1;
		int maxSimClusterNum = -1;
		double maxSim;
		double sim;
		double fenzi;
		double fenmu1;
		double fenmu2;
		
		//第一个新闻成为一个类
		createNewCluster(list_TextVector.get(0), clusterNumber);
		clusterNumber++;
		//从第二个新闻到最后一个新闻
		for(int i=1; i<list_TextVector.size(); ++i)
		{
			maxSim = 0.0;
			TextVector curTextVector = list_TextVector.get(i);
			//求该新闻与所有类质心最大相似度
			for(int j=0; j<list_CenterPoint.size(); ++j)
			{
				fenzi = 0.0;
				fenmu1 = 0.0;
				fenmu2 = 0.0;
				CenterPoint center = list_CenterPoint.get(j);
				for(int k=0; k<FeatureWordSize; ++k)
				{
					fenzi += curTextVector.tfidf[k] * center.tfidf[k];
					fenmu1 += curTextVector.tfidf[k] * curTextVector.tfidf[k];
					fenmu2 += center.tfidf[k] * center.tfidf[k];
				}
				
				sim = fenzi/ Math.sqrt(fenmu1 * fenmu2);
				if(sim > maxSim)
				{
					maxSim = sim;
					maxSimClusterNum = center.clusterNumber;
					maxSimPoint = center;
				}								
			}
			
			if(maxSim > r)
			{
				curTextVector.clusterNumber = maxSimClusterNum;
				maxSimPoint.newsNumber++;
				//重新计算质心
				for(int j=0; j<FeatureWordSize; ++j)
				{
					maxSimPoint.tfidf[j] = 0.0;
				}
				//把该类的tfidf对应求和
				for(int j=0; j<list_TextVector.size(); ++j)
				{
					if(list_TextVector.get(j).clusterNumber == curTextVector.clusterNumber)
					{
						for(int k=0; k<FeatureWordSize; ++k)
						{
							maxSimPoint.tfidf[k] += list_TextVector.get(j).tfidf[k];
						}
					}
				}
				//重新计算
				for(int j=0; j<FeatureWordSize; ++j)
				{
					maxSimPoint.tfidf[j] /= maxSimPoint.newsNumber;
				}
			}
			else
			{
				//创建一个新类
				createNewCluster(curTextVector, clusterNumber);
				clusterNumber++;
			}
		}
		System.out.printf("聚类个数%d%n", clusterNumber-1);
		
		//存储聚类结果
		//聚类号 ， 每个聚类有哪些文件
		
		
	}
	
	private void getClusterResult()
	{
		
	}
	
	
	/*
	 * 
	 */
	public void getHotSpot()
	{
		//找出每个聚类中词频最高的前几个词
		int sumNoise = 0;
		System.out.println(list_CenterPoint.size());
		for(int i=0; i<list_CenterPoint.size(); ++i)
		{
			map_WordTFDF.clear();
			list_WordTFDF.clear();
			CenterPoint curPoint = list_CenterPoint.get(i);
			//int noise = (int)(C.HOTSPOTNUMBER * curPoint.newsNumber / size);
			int noise = 40;
			sumNoise+=noise;
			ArrayList<Integer> clusterTextList = new ArrayList<Integer>();
			int index = 0;
			for(int j=0; j<list_TextVector.size(); ++j)
			{
				if(list_TextVector.get(index).clusterNumber == curPoint.clusterNumber)
				{
					clusterTextList.add(index);
					Article article = articles.get(index);
					ArrayList<String> a_words = article.words;
					for(int k=0; k<a_words.size(); ++k)
					{
						String word = a_words.get(k);
						if(map_WordTFDF.containsKey(word))
						{
							TFDF tfdf = map_WordTFDF.get(word);
							tfdf.setTF(tfdf.getTF()+1);
							map_WordTFDF.put(word, tfdf);
						}
						else
						{
							map_WordTFDF.put(word, new TFDF(1, 0, 0));
						}
					}
				}
				index++;//判断完一个，判断下一个		
			}
			sortWordByTF();
			
			//拿出热点词，取出每个类的热点词
			try(BufferedWriter bw = new BufferedWriter(new FileWriter("corpus/onepassresult.txt", true))){
				Iterator<WordTFDF> iter = list_WordTFDF.iterator();
				bw.append("聚类号："+i+"\n");
				bw.append("热点词：\n");
				int tmp = 0;
				//把热点词输出到文件
				while(iter.hasNext() && tmp<noise)
				{
					WordTFDF wordTFDF = iter.next();
					String line = wordTFDF.getWord()+"   tf="+wordTFDF.getTF();
					bw.append(line+"\n");
					tmp++;
				}
				//输出该剧类中的文件
				for(int k=0; k<clusterTextList.size(); ++k)
				{
					bw.append(clusterTextList.get(k)+"  ");
				}
				bw.append("\n");
			} catch(IOException e)
			{
				e.printStackTrace();
			}
			
			
			//以下是拿出所有类的热点词
//			for(int j=0; j<noise && list_WordTFDF.size() > 0; ++j)
//			{
//				String word = list_WordTFDF.get(j).getWord();
//				if(map_HotSpot.containsKey(word))
//				{
//					if(map_HotSpot.get(word).getTF() < list_WordTFDF.get(j).getTF() )
//					{
//						map_HotSpot.get(word).setTF(list_WordTFDF.get(j).getTF());
//					}
//				}
//				else
//				{
//					int tf = list_WordTFDF.get(j).getTF();
//					TFDF tfdf = new TFDF(tf, 0, 0);
//					map_HotSpot.put(word, tfdf);
//				}
//			}
		}
		
//		ArrayList<WordTFDF> list_HotSpot = new ArrayList<WordTFDF>();
//		Set<Map.Entry<String, TFDF>> set = map_HotSpot.entrySet();
//		for(Map.Entry<String, TFDF> entry : set)
//		{
//			String word = entry.getKey();
//			TFDF tfdf = entry.getValue();
//			WordTFDF wtfdf = new WordTFDF();
//			wtfdf.setWord(word);
//			wtfdf.setTF(tfdf.getTF());
//			list_HotSpot.add(wtfdf);
//		}
//		Collections.sort(list_HotSpot, new Comparator<WordTFDF>(){
//
//			@Override
//			public int compare(WordTFDF o1, WordTFDF o2) {
//				// TODO Auto-generated method stub
//				return o2.getTF()-o1.getTF();
//			}
//		});
//		
//		for(int i=0; i<list_HotSpot.size(); ++i)
//		{
//			System.out.printf("%s%n", list_HotSpot.get(i).getWord());
//		}
	}

	
	public static void main(String[] args) {
		new OnePass().doProcess("corpus/process_IncludeNotSpecial/original");
	}
}
