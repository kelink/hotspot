package com.gdufs.jy.Text;

public class TFDF {
	//Ϊ�˷�����Public
	public int TF;
	public int DF;
	public int flag; //�����жϱ��
	public TFDF(){}
	public TFDF(int tf, int df, int flag)
	{
		this.TF = tf;
		this.DF = df;
		this.flag = flag;
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
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	
}
