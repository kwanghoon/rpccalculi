package com.example.encrpc;

public class Var extends EncTerm {
	private String x;

	public Var(String x) {
		super();
		this.x = x;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	@Override
	public String toString() {
		return x;
	}
}
