package com.gdufs.jy.Text;

public class WordTFDF {
	//为了方便用Public
	public String word;
	public int TF;
	public int DF;
	public WordTFDF(){};
	public WordTFDF(String word, int tf, int df)
	{
		this.word = word;
		this.TF = tf;
		this.DF = df;
	}
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public int getTF() {
		return TF;
	}
	public void setTF(int tF) {
		TF = tF;
	}
	public int getDF() {
		return DF;
	}
	public void setDF(int dF) {
		DF = dF;
	}
	
	
	
}
