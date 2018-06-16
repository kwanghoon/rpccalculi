package com.example.typedrpc;

import com.example.rpc.Location;

public class LocType extends TypedLocation {
	Location loc;
	
	public LocType(Location loc) {
		super();
		this.loc = loc;
	}

	public Location getLoc() {
		return loc;
	}

	public void setLoc(Location loc) {
		this.loc = loc;
	}
	
	@Override
	public String toString() {
		if (loc == Location.Client)
			return "c";
		else
			return "s";
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof LocType) {
			LocType locTy = (LocType) arg0;
			
			return locTy.getLoc().equals(this.loc);
		}
		return false;
	}
	
}
