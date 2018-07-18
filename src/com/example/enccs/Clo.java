package com.example.enccs;

import java.util.ArrayList;

public class Clo extends EncValue {
	private String f;
	private ArrayList<EncValue> vs;
	
	public Clo(String f, ArrayList<EncValue> vs) {
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

	public ArrayList<EncValue> getVs() {
		return vs;
	}

	public void setVs(ArrayList<EncValue> vs) {
		this.vs = vs;
	}

	@Override
	public String toString() {
		String ret = "Clo(" + f + ", ";
		int cnt = 0;
		
		for (EncValue v : vs) {
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
