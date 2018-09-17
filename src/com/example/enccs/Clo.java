package com.example.enccs;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
		for(EncValue sv : vs) {
			jsonArray.add(sv.toJson());
		}
		
		jsonObject.put(Fvs, jsonArray);
	
		return jsonObject;
	}
}
