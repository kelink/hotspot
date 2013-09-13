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
 * һ�˾����㷨
 * @author Administrator
 *
 */
public class OnePass {

	private int size; //�ı���Ŀ
	private double r; //��ֵ
	private int FeatureWordSize = 0;
	private ArrayList<Article> articles  //�ı��еĴ�
							= new ArrayList<Article>();
	private TreeMap<String, TFDF> map_WordTFDF
							= new TreeMap<String, TFDF>(); // ������е��ʵ�TF��DF -- ��ʱ��
	private ArrayList<WordTFDF> list_WordTFDF
	 						= new ArrayList<WordTFDF>(); //��������  -- ��ʱ��
	private ArrayList<TextVector> list_TextVector
							= new ArrayList<TextVector>(); //����ı�����
	private ArrayList<CenterPoint> list_CenterPoint 
							= new ArrayList<CenterPoint>();
	private TreeMap<String, TFDF> map_HotSpot 
							= new TreeMap<String, TFDF>();
	
	public void doProcess(String dirPath)
	{
		// 1 - ��ȡ�����ļ���articles����
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
		// 2 - ��ȡtfdf
		System.out.println("2 ����tfdf");
		getWordTFDF();
		// 3 - 
		System.out.println("3 ��ȡ������");
		getFeatureWord();
		// 4 - 
		System.out.println("4 ��������");
		makeTextToVector();
		// 5 -
		System.out.println("5 �����ֵ");
		getR();
		// 6 
		System.out.println("6 ����");
		clustering();
		// 7
		System.out.println("7 ���ȵ�ʴ浽�ı� onepassresult.txt");
		getHotSpot();
	}
	
	public void getWordTFDF()
	{
		int flag = -1;
		for(int i=0; i<articles.size(); ++i)
		{
			ArrayList<String> list = articles.get(i).words;
			flag++; //���º��жϱ��
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
	 * �Ѵ��ﰴ��tfֵ������
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
		//����TF�Ӵ�С����
		Collections.sort(list_WordTFDF, new Comparator<WordTFDF>(){
			@Override
			public int compare(WordTFDF w1, WordTFDF w2)
			{
				return -(w1.getTF()-w2.getTF());
			}
		});
	}
	
	/**
	 * ��ȡ������
	 */
	public void getFeatureWord()
	{
		//map_WordTFDF.clear(); //���ԭ���Ķ���
		String word = null;
		TFDF tfdf = null;
		
		WordTFDF wordTFDF;
		Set<Map.Entry<String, TFDF>> set = map_WordTFDF.entrySet();
		for(Map.Entry<String, TFDF> entry : set)
		{
			tfdf = entry.getValue();
			if(tfdf.getTF()>=3){
				//ѡ���Ƶ>=3�Ĵ�
				wordTFDF = new WordTFDF(entry.getKey(), tfdf.getTF(), tfdf.getDF());
				list_WordTFDF.add(wordTFDF);
			}
		}
		FeatureWordSize = list_WordTFDF.size();
		
	}
	
	/**
	 * �����ı����������tfidf
	 * ���ƶȼ�������������ƶ�
	 */
	public void makeTextToVector()
	{
		TreeMap<String, TFDF> map_oneNewWords = new TreeMap<String, TFDF>();
		for(int i=0; i<size; ++i)
		{
			// 1 - �������ʷŽ�map_oneNewWords���棬��������������
			map_oneNewWords.clear();
			for(int j=0; j<list_WordTFDF.size(); ++j)
			{
				TFDF tfdf = new TFDF(0, list_WordTFDF.get(j).getDF(), -1);
				map_oneNewWords.put(list_WordTFDF.get(j).getWord(), tfdf);
			}
			
			// 2 - ͳ����������
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
			// 3 - ����tfidfֵ
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
	 * ������ֵ
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
		System.out.printf("��ֵr=%f%n", this.r);
	}
	
	
	public void createNewCluster(TextVector textVector, int num)
	{
		textVector.clusterNumber = num;
		CenterPoint point = new CenterPoint();
		point.clusterNumber  = num;
		point.newsNumber = 1;
		point.tfidf = new double[FeatureWordSize];
		//����
		for(int i=0; i<FeatureWordSize; ++i)
		{
			point.tfidf[i] = textVector.tfidf[i];
		}
		list_CenterPoint.add(point);
	}
	
	/**
	 *һ�˾���,�㷨���£�
	 *	1. ��ʼʱ�����༯��Ϊ�գ�����һ���µĶ��� 
	 *	2. �����������һ���µ��ࣻ 
	 *	3. ���ѵ�ĩβ����ת 6����������¶������ø��������ƶȶ��壬��������ÿ�������������ƶȣ���ѡ����
		   ������ƶȣ� 
	 *	4. ��������ƶ�С�ڸ�������ֵ r ��ת 2�� 
	 *	5. ���򽫸ö��������������ƶȵ����У������¸���ĸ���������ֵ��ͳ��Ƶ�ȣ�ת 3�� 
	 *	6. ������
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
		
		//��һ�����ų�Ϊһ����
		createNewCluster(list_TextVector.get(0), clusterNumber);
		clusterNumber++;
		//�ӵڶ������ŵ����һ������
		for(int i=1; i<list_TextVector.size(); ++i)
		{
			maxSim = 0.0;
			TextVector curTextVector = list_TextVector.get(i);
			//�������������������������ƶ�
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
				//���¼�������
				for(int j=0; j<FeatureWordSize; ++j)
				{
					maxSimPoint.tfidf[j] = 0.0;
				}
				//�Ѹ����tfidf��Ӧ���
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
				//���¼���
				for(int j=0; j<FeatureWordSize; ++j)
				{
					maxSimPoint.tfidf[j] /= maxSimPoint.newsNumber;
				}
			}
			else
			{
				//����һ������
				createNewCluster(curTextVector, clusterNumber);
				clusterNumber++;
			}
		}
		System.out.printf("�������%d%n", clusterNumber-1);
		
		//�洢������
		//����� �� ÿ����������Щ�ļ�
		
		
	}
	
	private void getClusterResult()
	{
		
	}
	
	
	/*
	 * 
	 */
	public void getHotSpot()
	{
		//�ҳ�ÿ�������д�Ƶ��ߵ�ǰ������
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
				index++;//�ж���һ�����ж���һ��		
			}
			sortWordByTF();
			
			//�ó��ȵ�ʣ�ȡ��ÿ������ȵ��
			try(BufferedWriter bw = new BufferedWriter(new FileWriter("corpus/onepassresult.txt", true))){
				Iterator<WordTFDF> iter = list_WordTFDF.iterator();
				bw.append("����ţ�"+i+"\n");
				bw.append("�ȵ�ʣ�\n");
				int tmp = 0;
				//���ȵ��������ļ�
				while(iter.hasNext() && tmp<noise)
				{
					WordTFDF wordTFDF = iter.next();
					String line = wordTFDF.getWord()+"   tf="+wordTFDF.getTF();
					bw.append(line+"\n");
					tmp++;
				}
				//����þ����е��ļ�
				for(int k=0; k<clusterTextList.size(); ++k)
				{
					bw.append(clusterTextList.get(k)+"  ");
				}
				bw.append("\n");
			} catch(IOException e)
			{
				e.printStackTrace();
			}
			
			
			//�������ó���������ȵ��
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
