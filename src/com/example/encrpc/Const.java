package com.example.encrpc;

public class Const extends EncValue {
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
