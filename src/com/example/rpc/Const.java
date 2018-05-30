package com.example.rpc;

public class Const extends Value {
	private int i;

	
	public Const(int i) {
		super();
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}
	
	@Override
	public String toString() {
		return i + "";
	}
}
