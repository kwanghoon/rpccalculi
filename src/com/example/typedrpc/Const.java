package com.example.typedrpc;

public class Const extends TypedTerm {
	private int i;

	public Const(int i) {
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
