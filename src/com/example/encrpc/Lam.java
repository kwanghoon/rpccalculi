package com.example.encrpc;

import java.util.ArrayList;

import com.example.rpc.Location;

public class Lam extends EncValue {
	private Location loc;
	private ArrayList<String> xs;
	private EncTerm m;

	public Lam(Location loc, ArrayList<String> xs, EncTerm m) {
		super();
		this.loc = loc;
		this.xs = xs;
		this.m = m;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public ArrayList<String> getXs() {
		return xs;
	}

	public void setXs(ArrayList<String> xs) {
		this.xs = xs;
	}

	public EncTerm getM() {
		return m;
	}

	public void setM(EncTerm m) {
		this.m = m;
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
		for (String s : xs) {
			ret += s;
			
			if (xs.size() - 1 != cnt) {
				ret += " ";
				cnt++;
			}
		}
		ret += ").";
		ret += m.toString();

		return ret;
	}
}
