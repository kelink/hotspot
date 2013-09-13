package com.gdufs.jy.Text;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.annotation.processing.FilerException;

import util.FileUtil;
import util.WordTokenize;

public class TextProcess 
{
	private int size; //�ı���Ŀ
	private double r; //��ֵ
	private ArrayList<Article> articles  //�ı��еĴ�
							= new ArrayList<Article>();
	private TreeMap<String, TFDF> map_WordTFDF
							= new TreeMap<String, TFDF>(); // ��ŵ��ʵ�TF��DF -- ��ʱ��
	private ArrayList<WordTFDF> list_WordTFDF
	 						= new ArrayList<WordTFDF>(); //��������  -- ��ʱ��
	private ArrayList<TextVector> list_TextVector
							= new ArrayList<TextVector>(); //����ı�����
	private ArrayList<CenterPoint> list_CenterPoint 
							= new ArrayList<CenterPoint>();
	private TreeMap<String, TFDF> map_HotSpot 
							= new TreeMap<String, TFDF>();
	public TextProcess(String folderPath)
	{
		/*
		 * 1 - ��ʼ������
		 */
		//1.1 - ����Ӣ���ı��ļ���
		File folder = new File(folderPath);
		//1.2 - ��ȡ�ļ����������ļ�
		File[] allfile = folder.listFiles();		
		size = allfile.length; //��ʼ���ı���Ŀ
		//1.3 - ���ļ��е��ı��ִ�,��ȥ��ͣ�ô�
		try{
			for(int i=0; i<allfile.length; ++i)
			{											
				ArrayList<String> tmp = WordTokenize.wordTokenize(FileUtil.read(allfile[i]), true);
				Article article = new Article();
				article.filename = allfile[i].getName();
				article.words = tmp;
				article.index =i;
				this.articles.add(article);
			}
			System.out.printf("size=%d%n", size);
		} catch(IOException e)
		{
			e.printStackTrace();
		}
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
					tmp.TF++;
					if(tmp.flag != flag)
					{
						tmp.DF ++;
						tmp.flag = flag;
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
	
	public void sortWordByTF()
	{
		WordTFDF wordTFDF;
		Set<Map.Entry<String, TFDF>> set = map_WordTFDF.entrySet();
		for(Map.Entry<String, TFDF> entry : set)
		{
			TFDF tfdf = entry.getValue();
			wordTFDF = new WordTFDF(entry.getKey(), tfdf.TF, tfdf.DF);
			list_WordTFDF.add(wordTFDF);
		}
		//����TF�Ӵ�С����
		Collections.sort(list_WordTFDF, new Comparator<WordTFDF>(){
			@Override
			public int compare(WordTFDF w1, WordTFDF w2)
			{
				return -(w1.TF-w2.TF);
			}
		});
	}
	
	public void getFeatureWord()
	{
		map_WordTFDF.clear(); //���ԭ���Ķ���
		String word = null;
		TFDF tfdf = null;
		for(int i=0; i<C.FEATUREWORDSIZE; ++i)
		{
			word = list_WordTFDF.get(i).word;
			int tf = list_WordTFDF.get(i).TF;
			int df = list_WordTFDF.get(i).DF;
			tfdf = new TFDF(tf, df, -1);
			map_WordTFDF.put(word, tfdf);
		}
	}
	
	public void makeTextToVector()
	{
		TreeMap<String, TFDF> map_oneNewWords = new TreeMap<String, TFDF>();
		for(int i=0; i<size; ++i)
		{
			// 1 - �������ʷŽ�map���棬��������������
			map_oneNewWords.clear();
			Set<Map.Entry<String, TFDF>> set = map_WordTFDF.entrySet();
			Iterator<Map.Entry<String, TFDF>> iter = set.iterator();
			for(int j=0; j<C.FEATUREWORDSIZE; ++j)
			{
				Map.Entry<String, TFDF> entry = iter.next();
				TFDF tfdf = new TFDF(0, entry.getValue().DF, -1);
				map_oneNewWords.put(entry.getKey(), tfdf);
			}
			
			// 2 - ͳ����������
			Article article = articles.get(i);
			ArrayList<String> words = article.words;
			for(String word : words)
			{
				if(map_oneNewWords.containsKey(word))
				{
					TFDF tfdf = map_oneNewWords.get(word);
					tfdf.TF++;
					map_oneNewWords.put(word, tfdf);
				}
			}
			// 3 - ����tfidfֵ
			set = map_oneNewWords.entrySet();
			iter = set.iterator();
			TextVector textVector = new TextVector();
			textVector.filename = article.filename;
			int k=0;
			double normalized = 0.0;
			while(iter.hasNext())
			{
				TFDF tfdf = iter.next().getValue();
				textVector.tfidf[k] = tfdf.TF * Math.log((double)size/tfdf.DF + 0.01);
				normalized += textVector.tfidf[k] * textVector.tfidf[k];
			}
			normalized = Math.sqrt(normalized);
			if(normalized!=0)
			{
				for(int j=0; j<C.FEATUREWORDSIZE; ++j)
				{
					textVector.tfidf[j] /= normalized;
				}
			}
			textVector.clusterNumber = 0;
			list_TextVector.add(textVector);
		}
	}
	
	public void getR()
	{
		int rand1;
		int rand2;
		double fenzi;
		double fenmu1;
		double fenmu2;
		this.r = 0.0;
		TextVector textVector1, textVector2;
		int i=(int)(size*C.PRECENT);
		while((i--)!=0)
		{
			rand1 = (int)(Math.random() * size);
			rand2 = (int)(Math.random() * size);
			fenzi = fenmu1 = fenmu2 = 0.0;
			textVector1 = list_TextVector.get(rand1);
			textVector2 = list_TextVector.get(rand2);
			for(int j=0; j<C.FEATUREWORDSIZE; ++j)
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
		
		//����
		for(int i=0; i<C.FEATUREWORDSIZE; ++i)
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
				for(int k=0; k<C.FEATUREWORDSIZE; ++k)
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
				for(int j=0; j<C.FEATUREWORDSIZE; ++j)
				{
					maxSimPoint.tfidf[j] = 0.0;
				}
				//�Ѹ����tfidf��Ӧ���
				for(int j=0; j<list_TextVector.size(); ++j)
				{
					if(list_TextVector.get(j).clusterNumber == curTextVector.clusterNumber)
					{
						for(int k=0; k<C.FEATUREWORDSIZE; ++k)
						{
							maxSimPoint.tfidf[k] += list_TextVector.get(j).tfidf[k];
						}
					}
				}
				//���¼���
				for(int j=0; j<C.FEATUREWORDSIZE; ++j)
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
		System.out.printf("�������%d%n", clusterNumber);
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
			int noise = (int)(C.HOTSPOTNUMBER * curPoint.newsNumber / size);
			sumNoise+=noise;
			
			
			for(int j=0; j<list_TextVector.size(); ++j)
			{
				int index = 0;
				if(list_TextVector.get(index).clusterNumber == curPoint.clusterNumber)
				{
					Article article = articles.get(index);
					ArrayList<String> a_words = article.words;
					for(int k=0; k<a_words.size(); ++k)
					{
						String word = a_words.get(k);
						if(map_WordTFDF.containsKey(word))
						{
							TFDF tfdf = map_WordTFDF.get(word);
							tfdf.TF++;
							map_WordTFDF.put(word, tfdf);
						}
						else
						{
							map_WordTFDF.put(word, new TFDF(1, 0, 0));
						}
					}
				}
				else
				{
					index++;
				}				
			}
			sortWordByTF();
			
			//�ó��ȵ��
			for(int j=0; j<noise && list_WordTFDF.size() > 0; ++j)
			{
				String word = list_WordTFDF.get(j).word;
				if(map_HotSpot.containsKey(word))
				{
					if(map_HotSpot.get(word).TF < list_WordTFDF.get(j).TF )
					{
						map_HotSpot.get(word).TF = list_WordTFDF.get(j).TF;
					}
				}
				else
				{
					int tf = list_WordTFDF.get(j).TF;
					TFDF tfdf = new TFDF(tf, 0, 0);
					map_HotSpot.put(word, tfdf);
				}
			}
		}
		
		ArrayList<WordTFDF> list_HotSpot = new ArrayList<WordTFDF>();
		Set<Map.Entry<String, TFDF>> set = map_HotSpot.entrySet();
		for(Map.Entry<String, TFDF> entry : set)
		{
			String word = entry.getKey();
			TFDF tfdf = entry.getValue();
			WordTFDF wtfdf = new WordTFDF();
			wtfdf.word = word;
			wtfdf.TF = tfdf.TF;
			list_HotSpot.add(wtfdf);
		}
		Collections.sort(list_HotSpot, new Comparator<WordTFDF>(){

			@Override
			public int compare(WordTFDF o1, WordTFDF o2) {
				// TODO Auto-generated method stub
				return o2.TF-o1.TF;
			}
		});
		
		for(int i=0; i<list_HotSpot.size(); ++i)
		{
			System.out.printf("%s%n", list_HotSpot.get(i).word);
		}
	}
	
}
