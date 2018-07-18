package com.example.stacs;

import java.util.ArrayList;

import javafx.util.Pair;

public class FunStore {
	private ArrayList<Pair<String, ClosedFun>> fs;

	public FunStore() {
		super();
		fs = new ArrayList<>();
	}
	
	public FunStore(ArrayList<Pair<String, ClosedFun>> fs) {
		super();
		this.fs = fs;
	}

	public ArrayList<Pair<String, ClosedFun>> getFs() {
		return fs;
	}

	public void setFs(ArrayList<Pair<String, ClosedFun>> fs) {
		this.fs = fs;
	}

	@Override
	public String toString() {
		String ret = "";
		for (Pair<String, ClosedFun> p: fs) {
			ret += p.getKey() + " = ";
			ret += p.getValue() + "\n";
		}
		
		return ret;
	}	
	
}
