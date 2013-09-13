package com.gdufs.jy.xml;

import java.io.FileOutputStream;    

import java.io.IOException;    
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jdom.Document;    
import org.jdom.Element;    
import org.jdom.JDOMException;    
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;    

import com.gdufs.jy.DBUtils.DBUtils;

public class Java2XML {   

    public void BuildXMLDoc() throws IOException, JDOMException, SQLException {    

       // 创建根节点 list;    
        Element root = new Element("news");    
       // 根节点添加到文档中；    
        Document Doc = new Document(root);   
        Connection conn = null;
        PreparedStatement pstat = null;
        ResultSet rs = null;
        conn = DBUtils.getConnection();
        String sql = "SELECT id, title, newstime, content FROM news_result";
        pstat = conn.prepareStatement(sql);
        rs = pstat.executeQuery();
       // 此处 for 循环可替换成 遍历 数据库表的结果集操作;    
        while(rs.next()){  
           // 创建节点 user;    
           Element elements = new Element("new");    
           // 给 new 节点添加属性 id;    
           elements.setAttribute("id", rs.getString(1));   
           // 给 new 节点添加子节点并赋值；     
           elements.addContent(new Element("title").setText(rs.getString(2)));   
           elements.addContent(new Element("newstime").setText(rs.getString(3)));   
           elements.addContent(new Element("content").setText(rs.getString(4)));   
           // 给父节点list添加user子节点;   
           root.addContent(elements);
       }   

       Format format = Format.getPrettyFormat();  // 格式化输出
       XMLOutputter XMLOut = new XMLOutputter(format); 
       // 输出 user.xml 文件；   
        XMLOut.output(Doc, new FileOutputStream("news.xml"));   

    }  

    public static void main(String[] args) {   

       try {   

           Java2XML j2x = new Java2XML();   

           System.out.println("生成 mxl 文件...");   

           j2x.BuildXMLDoc();   

       } catch (Exception e) {   

           e.printStackTrace();   

       }   

    }   

}   


