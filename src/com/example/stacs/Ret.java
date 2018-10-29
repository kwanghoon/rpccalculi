package com.example.stacs;

public class Ret extends StaTerm {
	private StaValue w;

	public Ret(StaValue w) {
		super();
		this.w = w;
	}

	public StaValue getW() {
		return w;
	}

	public void setW(StaValue w) {
		this.w = w;
	}

	@Override
	public String toString() {
		String ret = "Ret(" + w.toString() + ")";
		
		return ret;
	}

}
