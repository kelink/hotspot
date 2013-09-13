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

       // �������ڵ� list;    
        Element root = new Element("news");    
       // ���ڵ���ӵ��ĵ��У�    
        Document Doc = new Document(root);   
        Connection conn = null;
        PreparedStatement pstat = null;
        ResultSet rs = null;
        conn = DBUtils.getConnection();
        String sql = "SELECT id, title, newstime, content FROM news_result";
        pstat = conn.prepareStatement(sql);
        rs = pstat.executeQuery();
       // �˴� for ѭ�����滻�� ���� ���ݿ��Ľ��������;    
        while(rs.next()){  
           // �����ڵ� user;    
           Element elements = new Element("new");    
           // �� new �ڵ�������� id;    
           elements.setAttribute("id", rs.getString(1));   
           // �� new �ڵ�����ӽڵ㲢��ֵ��     
           elements.addContent(new Element("title").setText(rs.getString(2)));   
           elements.addContent(new Element("newstime").setText(rs.getString(3)));   
           elements.addContent(new Element("content").setText(rs.getString(4)));   
           // �����ڵ�list���user�ӽڵ�;   
           root.addContent(elements);
       }   

       Format format = Format.getPrettyFormat();  // ��ʽ�����
       XMLOutputter XMLOut = new XMLOutputter(format); 
       // ��� user.xml �ļ���   
        XMLOut.output(Doc, new FileOutputStream("news.xml"));   

    }  

    public static void main(String[] args) {   

       try {   

           Java2XML j2x = new Java2XML();   

           System.out.println("���� mxl �ļ�...");   

           j2x.BuildXMLDoc();   

       } catch (Exception e) {   

           e.printStackTrace();   

       }   

    }   

}   


