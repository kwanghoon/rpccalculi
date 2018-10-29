package com.example.enccs;

import java.util.ArrayList;

public class App extends EncTerm {
	private EncValue fun;
	private ArrayList<EncValue> args;

	public App(EncValue fun, ArrayList<EncValue> args) {
		super();
		this.fun = fun;
		this.args = args;
	}

	public EncValue getFun() {
		return fun;
	}

	public void setFun(EncValue fun) {
		this.fun = fun;
	}

	public ArrayList<EncValue> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<EncValue> args) {
		this.args = args;
	}

	@Override
	public String toString() {
		int cnt = 0;
		String ret = "(" + fun.toString() + ") (";

		for (EncValue ev : args) {
			ret += ev.toString();
			
			if (args.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}
		ret += ")";

		return ret;
	}

}
