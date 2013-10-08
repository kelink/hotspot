import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.gdufs.jy.DBUtils.DBUtils;
import com.gdufs.jy.Text.Article;
import com.gdufs.jy.Text.News;
import com.gdufs.jy.Text.TextProcess;
import com.gdufs.jy.cluster.OnePass;


import util.FileUtil;
import util.WordTokenize;

/**
 * 
 * 程序入口
 *
 */
public class HotSpot {
	
	HashSet<String> stopwords = new HashSet<String>();
	
	public static void main(String[] args) throws IOException
	{
		HotSpot hot = new HotSpot("F:\\jy_1\\Hotspot\\stop_words_en.txt");
//		System.out.println(hot.strip("4,,,", ",.\""));
//		System.out.println("1234".substring(3,4));
		
//		HotSpot hot = new HotSpot();
		ArrayList<News> list = hot.selectCorpus("wsj", "2013-1-01", "2013-7-31");
		System.out.println(list.size());
		ArrayList<Article> articlelist = hot.preprocess(list);
		new OnePass().doProcess(articlelist);
//		
//		System.out.println(list.get(3));
		//对预处理的材料一趟聚类~~
		//new OnePass().doProcess("corpus/process_IncludeNotSpecial/original");
		
	}
	
	public HotSpot(String stoplist)
	{
		FileReader fr = null;
		BufferedReader br = null;
		String line = null;
		try{
			System.out.println("filename:"+stoplist);
			fr = new FileReader(stoplist);
			br = new BufferedReader(fr);
			while((line = br.readLine())!=null)
			{
				this.stopwords.add(line);
			}
		} catch(IOException e)
		{
			e.printStackTrace();
		} finally{
			try{
				if(br != null)
				{
					br.close();
				}
			} catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
	//从数据库中选择需要的新闻
	public ArrayList<News> selectCorpus(String source, String dateStart, String dateEnd)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<News> list = new ArrayList<News>();
		try {
			conn = DBUtils.getConnection();
			String sql = "select title, newstime, content from news_result where newstime between ? and ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dateStart);
			pstmt.setString(2, dateEnd);
			rs = pstmt.executeQuery();
			while(rs.next())
			{
				News news = new News();
				news.setSource(source);
				news.setTitle(rs.getString("title"));
				news.setContent(rs.getString("content"));
				news.setDate(rs.getDate("newstime"));
				list.add(news);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			DBUtils.close(pstmt);
			DBUtils.close(conn);
		}
		return list;
	}
	
	//新闻选出来，进行处理，分词，去掉枝干，去掉停用词
	public ArrayList<Article> preprocess(ArrayList<News> newslist)
	{
		ArrayList<Article> articleList = new ArrayList<Article>();
		int index = 0;
		for(News news : newslist)
		{
			String content = news.getContent();
			String[] words = content.split("\\s+");
			Article article = new Article();
			article.words = new ArrayList<String>();
			article.index = index;
			article.filename = news.getTitle();
			article.datetime = news.getDate();
			//System.out.println(article.datetime);
			for(String word : words)
			{
				word = strip(word, "()\'\".,?:-!~").toLowerCase();
				if(!word.isEmpty() && !this.stopwords.contains(word))
				{
					article.words.add(word);					
				}
			}
			articleList.add(article);
		}
		
		return articleList;
	}
	
	public String strip(String src, String seq)
	{
		int start = 0;
		int end = src.length();
		for(int i=0; i<src.length(); ++i)
		{
			if(seq.indexOf(src.charAt(i))!=-1)
			{
				start++;
			}
			else
			{
				break;
			}
		}
		for(int i=src.length()-1; i>=0 && start < end; --i)
		{
			if(seq.indexOf(src.charAt(i))!=-1)
			{
				end--;
			}
			else
			{
				break;
			}
		}
		return src.substring(start, end);
	}
}
