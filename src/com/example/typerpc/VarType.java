package com.example.typerpc;

public class VarType extends Type {
	private int var;

	public VarType(int var) {
		super();
		this.var = var;
	}

	public int getVar() {
		return var;
	}

	public void setVar(int var) {
		this.var = var;
	}
	
	@Override
	public String toString() {
		return "a " + var;
	}
	
}
