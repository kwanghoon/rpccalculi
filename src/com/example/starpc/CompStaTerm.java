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
		}

		return null;
	}
}
