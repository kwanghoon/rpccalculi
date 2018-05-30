package com.example.typerpc;

import com.example.rpc.Location;

public class Lam extends TypedTerm {
	private Location loc;
	private String x;
	private Type t;
	private TypedTerm typedTerm;
	
	public Lam(Location loc, String x, Type t, TypedTerm typedTerm) {
		super();
		this.loc = loc;
		this.x = x;
		this.t = t;
		this.typedTerm = typedTerm;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public Type getT() {
		return t;
	}

	public void setT(Type t) {
		this.t = t;
	}

	public TypedTerm getTypedTerm() {
		return typedTerm;
	}

	public void setTypedTerm(TypedTerm typedTerm) {
		this.typedTerm = typedTerm;
	}
	
	@Override
	public String toString() {
		String ret = "lam^";
		
		if (loc == Location.Client)
			ret += "c";
		else if (loc == Location.Server)
			ret += "s";
		
		ret += " (" + x + ": " + t.toString() + ")." + typedTerm.toString();
		
		return ret;
	}
}
