package com.example.typedrpc;

public class EquTy extends Equ {
	private Type ty1;
	private Type ty2;

	public EquTy(Type ty1, Type ty2) {
		super();
		this.ty1 = ty1;
		this.ty2 = ty2;
	}

	public Type getTy1() {
		return ty1;
	}

	public void setTy1(Type ty1) {
		this.ty1 = ty1;
	}

	public Type getTy2() {
		return ty2;
	}

	public void setTy2(Type ty2) {
		this.ty2 = ty2;
	}

	@Override
	public String toString() {
		return ty1.toString() + " = " + ty2.toString();
	}
	

}
