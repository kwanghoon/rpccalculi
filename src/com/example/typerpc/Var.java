package com.example.typerpc;

public class Var extends TypedTerm {
	private String var;

	public Var(String var) {
		super();
		this.var = var;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}
	
	@Override
	public String toString() {
		return var;
	}
	
}
