package com.example.starpc;

import java.util.ArrayList;

import com.example.rpc.Location;

public class Lam extends StaValue {
	private Location loc;
	private ArrayList<String> xs;
	private StaTerm m;
	
	public Lam(Location loc, ArrayList<String> xs, StaTerm m) {
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

	public StaTerm getM() {
		return m;
	}

	public void setM(StaTerm m) {
		this.m = m;
	}

	@Override
	public String toString() {
		String ret = "lam^";
		
		if (loc == Location.Client)
			ret += "c";
		else
			ret += "s";
		
		for(String s: xs)
			ret += s + " ";
		
		ret += ")." + m.toString();
		
		return ret;
	}	
	
}
