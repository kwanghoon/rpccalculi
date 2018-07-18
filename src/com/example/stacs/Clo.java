package com.example.stacs;

import java.util.ArrayList;

public class Clo extends StaValue {
	private String f;
	private ArrayList<StaValue> vs;
	
	public Clo(String f, ArrayList<StaValue> vs) {
		super();
		this.f = f;
		this.vs = vs;
	}

	public String getF() {
		return f;
	}

	public void setF(String f) {
		this.f = f;
	}

	public ArrayList<StaValue> getVs() {
		return vs;
	}

	public void setVs(ArrayList<StaValue> vs) {
		this.vs = vs;
	}

	@Override
	public String toString() {
		String ret = "Clo(" + f + ", ";
		int cnt = 0;
		
		for (StaValue v : vs) {
			ret += v.toString();
			
			if (cnt != vs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		ret += ")";
		
		return ret;
	}
	
}
