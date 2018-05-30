package com.example.rpc;

public class App extends Term {
	private Term fun;
	private Term arg;
	
	
	public App(Term fun, Term arg) {
		super();
		this.fun = fun;
		this.arg = arg;
	}
	public Term getFun() {
		return fun;
	}
	public void setFun(Term fun) {
		this.fun = fun;
	}
	public Term getArg() {
		return arg;
	}
	public void setArg(Term arg) {
		this.arg = arg;
	}
	
	@Override
	public String toString() {
		return "(" + fun.toString() + ") " + "(" + arg.toString() +")";
	}
}
