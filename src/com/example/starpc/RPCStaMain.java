package com.example.starpc;

import java.util.ArrayList;

public class RPCStaMain {
	public static StaTerm subst(StaTerm m, String x, StaValue v) {
		if (m instanceof Const) {
			Const mconst = (Const) m;
			
			return mconst;
		}
		else if (m instanceof Var) {
			Var mvar = (Var) m;

			if (mvar.getX() == x)
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
			
			if (mlet.getY() == x)
				st2 = mlet.getM2();
			else
				st2 = subst(mlet.getM2(), x, v);
			
			return new Let(mlet.getY(), st1, st2);
		}

		return null;
	}

	public static StaTerm substs(StaTerm m, ArrayList<String> xs, ArrayList<StaValue> vs) {
		if ((xs == null || xs.isEmpty()) && (vs == null || vs.isEmpty()))
			return m;
		else {
			String x = xs.get(0);
			StaValue v = vs.get(0);
			xs.remove(x);
			vs.remove(v);

			return substs(subst(m, x, v), xs, vs);
		}
	}

}
