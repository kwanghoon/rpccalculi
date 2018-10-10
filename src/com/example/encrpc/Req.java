package com.example.encrpc;

import java.util.ArrayList;

public class Req extends EncTerm {
	private EncValue v;
	private ArrayList<EncValue> ws;

	public Req(EncValue v, ArrayList<EncValue> ws) {
		super();
		this.v = v;
		this.ws = ws;
	}

	public EncValue getV() {
		return v;
	}

	public void setV(EncValue v) {
		this.v = v;
	}

	public ArrayList<EncValue> getWs() {
		return ws;
	}

	public void setWs(ArrayList<EncValue> ws) {
		this.ws = ws;
	}

	@Override
	public String toString() {
		int cnt = 0;
		String ret = "Req(";
		ret += v.toString();
		ret += ") (";

		for (EncValue ev : ws) {
			ret += ev.toString();
			
			if (ws.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}

		ret += ")";

		return ret;
	}

}
