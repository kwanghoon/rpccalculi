package com.example.stacs;

import java.util.HashMap;

public class FunStore {
	private HashMap<String, ClosedFun> fs;

	public FunStore() {
		super();
		fs = new HashMap<>();
	}
	
	public FunStore(HashMap<String, ClosedFun> fs) {
		super();
		this.fs = fs;
	}

	public HashMap<String, ClosedFun> getFs() {
		return fs;
	}

	public void setFs(HashMap<String, ClosedFun> fs) {
		this.fs = fs;
	}

	@Override
	public String toString() {
		String ret = "";
		for (String p: fs.keySet()) {
			ret += p + " = ";
			ret += fs.get(p) + "\n";
		}
		
		return ret;
	}	
	
}
