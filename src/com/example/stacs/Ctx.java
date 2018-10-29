package com.example.stacs;

public class Ctx {
	private String x;
	private StaTerm m;
	
	public Ctx(String x, StaTerm m) {
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

	public StaTerm getM() {
		return m;
	}

	public void setM(StaTerm m) {
		this.m = m;
	}
	
}
