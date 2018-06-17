package com.example.encrpc;

import java.util.ArrayList;

public class EncMain {
	public static EncTerm subst(EncTerm m, String x, EncValue v) {
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
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			EncTerm call = subst(mCall.getCall(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mCall.getArgs()) {
				EncTerm arg = subst(ev, x, v);
				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}

			Call retCall = new Call((EncValue) call, args);

			return retCall;
		}
		else if (m instanceof App) {
			App mApp = (App) m;

			EncTerm fun = subst(mApp.getFun(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mApp.getArgs()) {
				EncTerm arg = subst(ev, x, v);

				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}
			App retApp = new App((EncValue) fun, args);

			return retApp;
		}
		else if (m instanceof Req) {
			Req mReq = (Req) m;

			EncTerm req = subst(mReq.getReq(), x, v);
			ArrayList<EncValue> args = new ArrayList<>();

			for (EncValue ev : mReq.getArgs()) {
				EncTerm arg = subst(ev, x, v);

				if (arg instanceof EncValue)
					args.add((EncValue) arg);
			}
			Req retReq = new Req((EncValue) req, args);

			return retReq;
		}
		else if (m instanceof Let) {
			Let mLet = (Let) m;

			EncTerm m1 = subst(mLet.getM1(), x, v);
			EncTerm m2;

			if (mLet.getVal() == x)
				m2 = mLet.getM2();
			else
				m2 = subst(mLet.getM2(), x, v);

			Let retLet = new Let(mLet.getVal(), m1, m2);

			return retLet;
		}
		return null;
	}

	public static EncTerm substs(EncTerm m, ArrayList<String> xs, ArrayList<EncValue> vs) {
		if ((xs == null || xs.isEmpty()) && (vs == null || vs.isEmpty()))
			return m;
		else {
			if (xs.size() == vs.size()) {
				String x = xs.get(0);
				EncValue v = vs.get(0);

				xs.remove(x);
				vs.remove(v);

				EncTerm eTerm = subst(m, x, v);

				return substs(eTerm, xs, vs);
			}
			else
				System.out.println("Error in substs: the lengths of vars and vals are different");
		}
		return null;
	}
}
