package com.example.enccs;

import java.util.ArrayList;

public class Req extends EncTerm {
	private EncValue req;
	private ArrayList<EncValue> args;

	public Req(EncValue req, ArrayList<EncValue> args) {
		super();
		this.req = req;
		this.args = args;
	}

	public EncValue getReq() {
		return req;
	}

	public void setReq(EncValue req) {
		this.req = req;
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
		String ret = "Req(";
		ret += req.toString();
		ret += ") (";

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
