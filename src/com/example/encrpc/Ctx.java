package com.example.encrpc;

public class Ctx {
	private String x;
	private EncTerm m;
	
	public Ctx(String x, EncTerm m) {
		super();
		this.x = x;
		this.m = m;
	}
	
	public String getX() {
		return x;
	}
	
	public void setX(String x) {
		this.x = x;
	}

	public EncTerm getM() {
		return m;
	}

	public void setM(EncTerm m) {
		this.m = m;
	}
	
}
