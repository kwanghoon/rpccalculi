package com.example.stacs;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		String ret = "Clo(" + f + ", {";
		int cnt = 0;
		
		for (StaValue v : vs) {
			if (v == null) 
				System.err.println("Clo " + f + " has something null inside");
			ret += v.toString();
			
			if (cnt != vs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		ret += "})";
		
		return ret;
	}
	
	public static final String Clo = "Clo";
	public static final String Fvs = "Fvs";
	
	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		
		// Function name
		jsonObject.put(Clo, f);
		
		// Free variables
		JSONArray jsonArray = new JSONArray();
		for(StaValue sv : vs) {
			jsonArray.add(sv.toJson());
		}
		
		jsonObject.put(Fvs, jsonArray);
	
		return jsonObject;
	}
	
}
