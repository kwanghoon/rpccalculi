package com.example.encrpc;

import java.util.ArrayList;

import com.example.rpc.Location;

public class Lam extends EncValue {
	private Location loc;
	private ArrayList<String> strArr;
	private EncTerm term;

	public Lam(Location loc, ArrayList<String> strArr, EncTerm term) {
		super();
		this.loc = loc;
		this.strArr = strArr;
		this.term = term;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public ArrayList<String> getStrArr() {
		return strArr;
	}

	public void setStrArr(ArrayList<String> strArr) {
		this.strArr = strArr;
	}

	public EncTerm getTerm() {
		return term;
	}

	public void setTerm(EncTerm term) {
		this.term = term;
	}

	@Override
	public String toString() {
		int cnt = 0;
		String ret = "lam^";

		if (loc == Location.Client)
			ret += "c";
		else
			ret += "s";

		ret += "(";
		for (String s : strArr) {
			ret += s;
			
			if (strArr.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}
		ret += ").";
		ret += term.toString();

		return ret;
	}
}
