package com.example.starpc;

import java.util.ArrayList;

import javafx.util.Pair;

public class RPCStaMain {
	public static StaTerm subst(StaTerm m, String x, StaValue v) {
		if (m instanceof Const) {
			Const mconst = (Const) m;
			
			return mconst;
		}
		else if (m instanceof Var) {
			Var mvar = (Var) m;

			if (mvar.getX().equals(x))
				return v;
			else
				return mvar;
		}
		else if (m instanceof Lam) {
			Lam mlam = (Lam) m;

			if (mlam.getXs().contains(x))
				return new Lam(mlam.getLoc(), mlam.getXs(), mlam.getM());
			else
				return new Lam(mlam.getLoc(), mlam.getXs(), subst(mlam.getM(), x, v));
		}
		else if (m instanceof App) {
			App mapp = (App) m;

			ArrayList<StaValue> ws = new ArrayList<>();

			for (StaValue sv : mapp.getWs()) {
				StaTerm st = subst(sv, x, v);
				ws.add((StaValue) st);
			}

			return new App((StaValue) subst(mapp.getF(), x, v), ws);
		}
		else if (m instanceof Call) {
			Call mcall = (Call) m;

			ArrayList<StaValue> ws = new ArrayList<>();

			for (StaValue sv : mcall.getWs()) {
				StaTerm st = subst(sv, x, v);

				ws.add((StaValue) st);
			}

			return new Call((StaValue) subst(mcall.getF(), x, v), ws);
		}
		else if (m instanceof Ret) {
			Ret mret = (Ret) m;

			return new Ret((StaValue) subst(mret.getW(), x, v));
		}
		else if (m instanceof Req) {
			Req mreq = (Req) m;

			ArrayList<StaValue> ws = new ArrayList<>();

			for (StaValue sv : mreq.getWs()) {
				StaTerm st = subst(sv, x, v);

				ws.add((StaValue) st);
			}

			return new Req((StaValue) subst(mreq.getF(), x, v), ws);
		}
		else if (m instanceof Let) {
			Let mlet = (Let) m;
			
			StaTerm st1 = subst(mlet.getM1(), x, v);
			StaTerm st2;
			
			if (mlet.getY().equals(x))
				st2 = mlet.getM2();
			else
				st2 = subst(mlet.getM2(), x, v);
			
			return new Let(mlet.getY(), st1, st2);
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
	
	public static ArrayList<String> fv(StaTerm m) {
		ArrayList<String> retList = new ArrayList<>();
		
		if (m instanceof Const) {
			return retList;
		}
		else if (m instanceof Var) {
			Var mVar = (Var) m;
			
			retList.add(mVar.getX());
			
			return retList;
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;
			
			retList.addAll(fv(mLam.getM()));
			retList.removeAll(mLam.getXs());
			
			return retList;
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;
			
			retList.addAll(fv(mCall.getF()));
			
			for (StaValue v: mCall.getWs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof Ret) {
			Ret mRet = (Ret) m;
			
			return fv(mRet.getW());
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;
			
			retList.addAll(fv(mReq.getF()));
			
			for (StaValue v: mReq.getWs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof App) {
			App mApp = (App) m;
			
			retList.addAll(fv(mApp.getF()));
			
			for (StaValue v: mApp.getWs()) {
				retList.addAll(fv(v));
			}
			
			return retList;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;
			
			retList.addAll(fv(mLet.getM2()));
			retList.remove(mLet.getY());
			retList.addAll(fv(mLet.getM1()));
			
			return retList;
		}
		else
			return null;
	}

}
