package com.example.typerpc;

public class EquLoc extends Equ {
	private TypedLocation tyloc1;
	private TypedLocation tyloc2;
	
	public EquLoc(TypedLocation tyloc1, TypedLocation tyloc2) {
		super();
		this.tyloc1 = tyloc1;
		this.tyloc2 = tyloc2;
	}

	public TypedLocation getTyloc1() {
		return tyloc1;
	}

	public void setTyloc1(TypedLocation tyloc1) {
		this.tyloc1 = tyloc1;
	}

	public TypedLocation getTyloc2() {
		return tyloc2;
	}

	public void setTyloc2(TypedLocation tyloc2) {
		this.tyloc2 = tyloc2;
	}
	
}
