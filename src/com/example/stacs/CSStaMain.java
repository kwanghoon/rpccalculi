package com.example.stacs;

import java.util.ArrayList;

import javafx.util.Pair;

public class CSStaMain {
	public static StaTerm subst(StaTerm m, String x, StaValue v) {
		if (m instanceof Const) {
			Const mConst = (Const) m;
			return mConst;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;
			
			if (mVar.getX() == x)
				return v;
			else
				return mVar;
		}
		else if (m instanceof Clo) {
			Clo mClo = (Clo) m;
			
			ArrayList<StaValue> ws = new ArrayList<>();
			
			for (StaValue w: mClo.getVs()) {
				ws.add((StaValue) subst(w, x, v));
			}
			
			Clo retClo = new Clo(mClo.getF(), ws);
			
			return retClo;
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;
			
			StaValue f = (StaValue) subst(mCall.getF(), x, v);
			
			ArrayList<StaValue> ws = new ArrayList<>();
			
			for (StaValue w: mCall.getWs()) {
				ws.add((StaValue) subst(w, x, v));
			}
			
			Call retCall = new Call(f, ws);
			
			return retCall;
		}
		else if (m instanceof Ret) {
			Ret mRet = (Ret) m;
			
			StaValue w = (StaValue) subst(mRet.getW(), x, v);
			
			Ret retRet = new Ret(w);
			
			return retRet;
		}
		else if (m instanceof App) {
			App mApp = (App) m;
			
			StaValue f = (StaValue) subst(mApp.getF(), x, v);
			
			ArrayList<StaValue> ws = new ArrayList<>();
			
			for (StaValue w: mApp.getWs()) {
				ws.add((StaValue) subst(w, x, v));
			}
			
			App retApp = new App(f, ws);
			
			return retApp;
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;
			
			StaValue f = (StaValue) subst(mReq.getF(), x, v);
			
			ArrayList<StaValue> ws = new ArrayList<>();
			
			for (StaValue w: mReq.getWs()) {
				ws.add((StaValue) subst(w, x, v));
			}
			
			Req retReq = new Req(f, ws);
			
			return retReq;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;
			
			StaTerm m1 = subst(mLet.getM1(), x, v);
			StaTerm m2;
			
			if (x == mLet.getY()) {
				m2 = mLet.getM2();
			}
			else
				m2 = subst(mLet.getM2(), x, v);
			
			Let retLet = new Let(mLet.getY(), m1, m2);
			
			return retLet;
		}
		
		return null;
	}
	
	public static StaTerm substs(StaTerm m, ArrayList<String> xs, ArrayList<StaValue> vs) {
		ArrayList<Pair<String, StaValue>> pairList = new ArrayList<>();
		for (int i = 0; i < xs.size(); i++) {
			pairList.add(new Pair<>(xs.get(i), vs.get(i)));
		}
		
		StaTerm staTerm = m;
		
		for (Pair<String, StaValue> p:pairList) {
			String x = p.getKey();
			StaValue v = p.getValue();

			staTerm = subst(staTerm, x, v);
		}
		
		return staTerm;
	}
}
