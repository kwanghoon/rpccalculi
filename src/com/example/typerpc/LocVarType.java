package com.example.typerpc;

public class LocVarType extends TypedLocation {
	private int var;

	
	public LocVarType(int var) {
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
		return "l" + var;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof LocVarType) {
			LocVarType locVarTy = (LocVarType) arg0;
			return locVarTy.getVar() == this.var;
		}
		return false;
	}
	
	
}
