package com.example.starpc;

import java.util.ArrayList;

import com.example.rpc.Location;
import com.example.typedrpc.TypedTerm;

import javafx.util.Pair;

public class CompStaTerm {
	public static StaTerm compStaTerm(TypedTerm tt) {
		Pair<Integer, StaTerm> p = compClient(1, tt);

		return p.getValue();
	}

	public static Pair<Integer, StaTerm> compClient(int i, TypedTerm tt) {
		Pair<Integer, StaTerm> retPair;

		if (tt instanceof com.example.typedrpc.Const) {
			com.example.typedrpc.Const ttConst = (com.example.typedrpc.Const) tt;

			retPair = new Pair<>(i, new Const(ttConst.getI()));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Var) {
			com.example.typedrpc.Var ttVar = (com.example.typedrpc.Var) tt;

			retPair = new Pair<>(i, new Var(ttVar.getVar()));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Lam) {
			com.example.typedrpc.Lam ttLam = (com.example.typedrpc.Lam) tt;
			
			ArrayList<String> xs = new ArrayList<>();
			xs.add(ttLam.getX());
			
			if (ttLam.getLoc() == Location.Client) {
				Pair<Integer, StaTerm> p = compClient(i, ttLam.getTypedTerm());
				
				Lam retLam = new Lam(ttLam.getLoc(), xs, p.getValue());
				retPair = new Pair<>(p.getKey(), retLam);
			}
			else {
				Pair<Integer, StaTerm> p = compServer(i, ttLam.getTypedTerm());
				
				Lam retLam = new Lam(ttLam.getLoc(), xs, p.getValue());
				retPair = new Pair<>(p.getKey(), retLam);
			}
			

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.App) {
			com.example.typedrpc.App ttApp = (com.example.typedrpc.App) tt;
			
			if (ttApp.getLoc() instanceof com.example.typedrpc.LocType) {
				com.example.typedrpc.LocType loc = (com.example.typedrpc.LocType) ttApp.getLoc();
				
				if (loc.getLoc() == Location.Client) {
					String fvar = "f" + i;
					String xvar = "x" + (i + 1);
					String rvar = "r" + (i + 2);
					Var f = new Var(fvar);
					Var x = new Var(xvar);
					Var r = new Var(rvar);
					
					Pair<Integer, StaTerm> p1 = compClient(i + 3, ttApp.getFun());
					Pair<Integer, StaTerm> p2 = compClient(p1.getKey(), ttApp.getArg());
					
					ArrayList<StaValue> xs = new ArrayList<>();
					xs.add(x);
					
					Let retLet = new Let(fvar, p1.getValue(),
										new Let(xvar, p2.getValue(),
												new Let(rvar, new App(f, xs), r)));
					
					retPair = new Pair<>(p2.getKey(), retLet);
				}
				else {
					String fvar = "f" + i;
					String xvar = "x" + (i + 1);
					String rvar = "r" + (i + 2);
					Var f = new Var(fvar);
					Var x = new Var(xvar);
					Var r = new Var(rvar);
					
					Pair<Integer, StaTerm> p1 = compClient(i + 3, ttApp.getFun());
					Pair<Integer, StaTerm> p2 = compClient(p1.getKey(), ttApp.getArg());
					
					ArrayList<StaValue> xs = new ArrayList<>();
					xs.add(x);
					
					Let retLet = new Let(fvar, p1.getValue(),
										new Let(rvar, p2.getValue(),
												new Let(rvar, new Req(f, xs), r)));
					retPair = new Pair<>(p2.getKey(), retLet);
				}
				
				return retPair;
			}
		}

		return null;
	}

	public static Pair<Integer, StaTerm> compServer(int i, TypedTerm tt) {
		Pair<Integer, StaTerm> retPair;
		
		if (tt instanceof com.example.typedrpc.Const) {
			com.example.typedrpc.Const ttConst = (com.example.typedrpc.Const) tt;
			
			retPair = new Pair<>(i, new Const(ttConst.getI()));
			
			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Var) {
			com.example.typedrpc.Var ttVar = (com.example.typedrpc.Var) tt;
			
			retPair = new Pair<>(i, new Var(ttVar.getVar()));
			
			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Lam) {
			com.example.typedrpc.Lam ttLam = (com.example.typedrpc.Lam) tt;
			
			ArrayList<String> xs = new ArrayList<>();
			xs.add(ttLam.getX());
			
			if (ttLam.getLoc() == Location.Client) {
				Pair<Integer, StaTerm> p = compClient(i, ttLam.getTypedTerm());
				
				retPair = new Pair<>(p.getKey(), new Lam(Location.Client, xs, p.getValue()));
			}
			else {
				Pair<Integer, StaTerm> p = compServer(i, ttLam.getTypedTerm());
				
				retPair = new Pair<>(p.getKey(), new Lam(Location.Server, xs, p.getValue()));
			}
			
			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.App) {
			com.example.typedrpc.App ttApp = (com.example.typedrpc.App) tt;
			
			if (ttApp.getLoc() instanceof com.example.typedrpc.LocType) {
				com.example.typedrpc.LocType loc = (com.example.typedrpc.LocType) ttApp.getLoc();
				
				if (loc.getLoc() == Location.Client) {
					String fvar = "f" + i;
					String xvar = "x" + (i + 1);
					String yvar = "y" + (i + 2);
					String zvar = "z" + (i + 3);
					String rvar = "r" + (i + 4);
					Var f = new Var(fvar);
					Var x = new Var(xvar);
					Var y = new Var(yvar);
					Var z = new Var(zvar);
					Var r = new Var(rvar);
					
					ArrayList<String> lamXs = new ArrayList<>();
					ArrayList<StaValue> appXs = new ArrayList<>();
					lamXs.add(zvar);
					appXs.add(z);
					
					Lam commuteFun = new Lam(Location.Client, lamXs,
											new Let(yvar, new App(f, appXs), new Ret(y)));
					
					Pair<Integer, StaTerm> p1 = compServer(i + 5, ttApp.getFun());
					Pair<Integer, StaTerm> p2 = compServer(p1.getKey(), ttApp.getArg());
					
					ArrayList<StaValue> callXs = new ArrayList<>();
					
					Let retLet = new Let(fvar, p1.getValue(), 
										new Let(xvar, p2.getValue(),
												new Let(rvar, new Call(commuteFun, callXs), r)));
					retPair = new Pair<>(p2.getKey(), retLet);
					
					}
				else {
					String fvar = "f" + i;
					String xvar = "x" + (i + 1);
					String rvar = "r" + (i + 2);
					Var f = new Var(fvar);
					Var x = new Var(xvar);
					Var r = new Var(rvar);
					
					Pair<Integer, StaTerm> p1 = compServer(i + 3, ttApp.getFun());
					Pair<Integer, StaTerm> p2 = compServer(p1.getKey(), ttApp.getArg());
					
					ArrayList<StaValue> appXs = new ArrayList<>();
					appXs.add(x);
					
					Let retLet = new Let(fvar, p1.getValue(),
										new Let(xvar, p2.getValue(),
												new Let(rvar, new App(f, appXs), r)));
					
					retPair = new Pair<>(p2.getKey(), retLet);
				}
			}
		}

		return null;
	}
}
