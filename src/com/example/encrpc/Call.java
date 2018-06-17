package com.example.encrpc;

import java.util.ArrayList;

public class Call extends EncTerm {
	private EncValue call;
	private ArrayList<EncValue> args;

	public Call(EncValue call, ArrayList<EncValue> args) {
		super();
		this.call = call;
		this.args = args;
	}

	public EncValue getCall() {
		return call;
	}

	public void setCall(EncValue call) {
		this.call = call;
	}

	public ArrayList<EncValue> getArgs() {
		return args;
	}

	public void setArgs(ArrayList<EncValue> args) {
		this.args = args;
	}

	@Override
	public String toString() {
		String ret = "Call(" + call.toString() + ") (";

		for (EncValue ev : args) {
			ret += ev.toString() + " ";
		}

		ret += ")";

		return ret;
	}

}
