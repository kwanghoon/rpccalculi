package com.example.encrpc;

public class Let extends EncTerm {
	private String y;
	private EncTerm m1;
	private EncTerm m2;

	public Let(String y, EncTerm m1, EncTerm m2) {
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

	public EncTerm getM1() {
		return m1;
	}

	public void setM1(EncTerm m1) {
		this.m1 = m1;
	}

	public EncTerm getM2() {
		return m2;
	}

	public void setM2(EncTerm m2) {
		this.m2 = m2;
	}

	@Override
	public String toString() {
		String ret = "let ";

		ret += y + " = ";
		ret += m1.toString() + " in ";
		ret += m2.toString();

		return ret;
	}

}
