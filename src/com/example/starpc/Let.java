package com.example.starpc;

public class Let extends StaTerm {
	private String y;
	private StaTerm m1;
	private StaTerm m2;
	
	public Let(String y, StaTerm m1, StaTerm m2) {
		super();
		this.y = y;
		this.m1 = m1;
		this.m2 = m2;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public StaTerm getM1() {
		return m1;
	}

	public void setM1(StaTerm m1) {
		this.m1 = m1;
	}

	public StaTerm getM2() {
		return m2;
	}

	public void setM2(StaTerm m2) {
		this.m2 = m2;
	}

	@Override
	public String toString() {
		String ret = "let " + y + " = " + m1.toString() + " in " + m2.toString();
		
		return ret;
	}
	
}
