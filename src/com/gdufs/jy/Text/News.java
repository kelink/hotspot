package com.gdufs.jy.Text;

import java.util.Date;

public class News {

	private String source;
	private String title;
	private Date date;
	private String content;

	public News()
	{
		//
	}
	
	public News(String source, String title, Date date, String content)
	{
		this.source = source;
		this.title = title;
		this.date = date;
		this.content = content;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "News [source=" + source + ", title=" + title + ", date=" + date
				+ "]";
	}
	
	
}
