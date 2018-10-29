package com.example.stacs;

import java.util.ArrayList;

import com.example.rpc.Location;

public class ClosedFun {
	private ArrayList<String> zs;
	private Location loc;
	private ArrayList<String> xs;
	private StaTerm m;
	
	public ClosedFun(ArrayList<String> zs, Location loc, ArrayList<String> xs, StaTerm m) {
		super();
		this.zs = zs;
		this.loc = loc;
		this.xs = xs;
		this.m = m;
	}

	public ArrayList<String> getZs() {
		return zs;
	}

	public void setZs(ArrayList<String> zs) {
		this.zs = zs;
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
		String ret = "{";
		int cnt = 0;
		
		for (String z: zs) {
			ret += z;
			
			if (cnt < zs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		
		ret += "} lam^";
		
		if (loc == Location.Client)
			ret += "c";
		else
			ret += "s";
		
		ret += "(";
		cnt = 0;
		
		for (String x: xs) {
			ret += x;
			
			if (cnt < xs.size() - 1) {
				ret += " ";
				cnt++;
			}
		}
		
		ret += ")." + m.toString();
		
		return ret;
	}
	
	
}
