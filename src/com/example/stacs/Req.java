package com.example.stacs;

import java.util.ArrayList;

public class Req extends StaTerm {
	private StaValue f;
	private ArrayList<StaValue> ws;
	
	public Req(StaValue f, ArrayList<StaValue> ws) {
		super();
		this.f = f;
		this.ws = ws;
	}

	public StaValue getF() {
		return f;
	}

	public void setF(StaValue f) {
		this.f = f;
	}

	public ArrayList<StaValue> getWs() {
		return ws;
	}

	public void setWs(ArrayList<StaValue> ws) {
		this.ws = ws;
	}

	@Override
	public String toString() {
		int cnt = 0;
		String ret = "Req(" + f.toString() + ") (";
		
		for (StaValue sv: ws) {
			ret += sv.toString();
			
			if (ws.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}
		ret += ")";
		
		return ret;
	}

}
