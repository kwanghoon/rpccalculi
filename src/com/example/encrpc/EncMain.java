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
			
		}
		else if (m instanceof App) {
			
		}
		else if (m instanceof Req) {
			
		}
		else if (m instanceof Let) {
			
		}
		return null;
	}
	
	public static EncTerm substs(EncTerm m, ArrayList<String> xs, ArrayList<EncValue> vs) {
		
	}	
}
