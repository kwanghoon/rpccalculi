package com.example.starpc;

import java.util.ArrayList;

public class Call extends StaTerm {
	private StaValue f;
	private ArrayList<StaValue> ws;
	
	public Call(StaValue f, ArrayList<StaValue> ws) {
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
		String ret = "Call(" + f.toString() + ") (";
		
		for(StaValue sv: ws)
			ret += sv.toString() + " ";
		ret += ")";
		
		return ret;
	}
	
}
