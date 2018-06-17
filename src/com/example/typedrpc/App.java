package com.example.typedrpc;

public class App extends TypedTerm {
	private TypedLocation loc;
	private TypedTerm fun;
	private TypedTerm arg;

	public App(TypedLocation loc, TypedTerm fun, TypedTerm arg) {
		this.loc = loc;
		this.fun = fun;
		this.arg = arg;
	}

	public TypedLocation getLoc() {
		return loc;
	}

	public void setLoc(TypedLocation loc) {
		this.loc = loc;
	}

	public TypedTerm getFun() {
		return fun;
	}

	public void setFun(TypedTerm fun) {
		this.fun = fun;
	}

	public TypedTerm getArg() {
		return arg;
	}

	public void setArg(TypedTerm arg) {
		this.arg = arg;
	}

	@Override
	public String toString() {
		String ret = "(" + fun.toString() + ")^" + loc.toString() + "^(" + arg.toString() + ")";

		return ret;
	}

}
