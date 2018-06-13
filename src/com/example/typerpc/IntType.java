package com.example.typerpc;

public class IntType extends Type {
	@Override
	public String toString() {
		return "int";
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof IntType)
			return true;
		else
			return false;
	}
}
