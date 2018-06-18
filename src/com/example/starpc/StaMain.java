package com.example.starpc;

import java.util.ArrayList;

public class StaMain {
	public static StaTerm subst(StaTerm m, String x, StaValue v) {
		if (m instanceof Const) {
			Const mconst = (Const) m;
			return m;
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
			
			
		}
		else if (m instanceof Call) {
			
		}
		else if (m instanceof Ret) {
			
		}
		else if (m instanceof Req) {
			
		}
		else if (m instanceof Let) {
			
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
