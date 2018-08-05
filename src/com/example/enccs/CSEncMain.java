package com.example.enccs;

import java.util.ArrayList;

import javafx.util.Pair;

public class CSEncMain {
	public static EncTerm subst(EncTerm m, String x, EncValue v) {
		if (m instanceof Const) {
			Const mConst = (Const) m;
			
			return mConst;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;
			
			if (mVar.getX().equals(x))
				return v;
			else
				return mVar;
		}
		else if (m instanceof Clo) {
			Clo mClo = (Clo) m;
			
			ArrayList<EncValue> ws = new ArrayList<>();
			
			for (EncValue w: mClo.getVs()) {
				ws.add((EncValue) subst(w, x, v));
			}
			
			Clo retClo = new Clo(mClo.getF(), ws);
			
			return retClo;
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;
			
			EncValue f = (EncValue) subst(mCall.getCall(), x, v);
			ArrayList<EncValue> ws = new ArrayList<>();
			
			for (EncValue w: mCall.getArgs()) {
				ws.add((EncValue) subst(w, x, v));
			}
			
			Call retCall = new Call(f, ws);
			
			return retCall;
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;
			
			EncValue f = (EncValue) subst(mReq.getReq(), x, v);
			ArrayList<EncValue> ws = new ArrayList<>();
			
			for(EncValue w: mReq.getArgs()) {
				ws.add((EncValue) subst(w, x, v));
			}
			
			Req retReq = new Req(f, ws);
			
			return retReq;
		}
		else if (m instanceof App) {
			App mApp = (App) m;
			
			EncValue f = (EncValue) subst(mApp.getFun(), x, v);
			ArrayList<EncValue> ws = new ArrayList<>();
			
			for (EncValue w: mApp.getArgs()) {
				ws.add((EncValue) subst(w, x, v));
			}
			
			App retApp = new App(f, ws);
			
			return retApp;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;
			EncTerm m1 = subst(mLet.getM1(), x, v);
			EncTerm m2;
			
			if (mLet.getVal().equals(x))
				m2 = mLet.getM2();
			else
				m2 = subst(mLet.getM2(), x, v);
			
			Let retLet = new Let(mLet.getVal(), m1, m2);
			
			return retLet;
		}
		
		return null;
	}

	public static EncTerm substs(EncTerm m, ArrayList<String> xs, ArrayList<EncValue> vs) {
		ArrayList<Pair<String, EncValue>> pairList = new ArrayList<>();
		for (int i = 0; i < xs.size(); i++) {
			pairList.add(new Pair<>(xs.get(i), vs.get(i)));
		}
		
		EncTerm encTerm = m;
		
		for (Pair<String, EncValue> p:pairList) {
			String x = p.getKey();
			EncValue v = p.getValue();

			encTerm = subst(encTerm, x, v);
		}
		
		return encTerm;
	}
}
