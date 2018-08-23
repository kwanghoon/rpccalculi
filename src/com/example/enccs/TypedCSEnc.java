package com.example.enccs;

import com.example.utils.Either;

import javafx.util.Pair;

public class TypedCSEnc {
	public static ClosedFun lookup(FunStore fs, String f) {
		for (Pair<String, ClosedFun> p: fs.getFs()) {
			if (p.getKey().equals(f))
				return p.getValue();
		}
		return null;
	}
	
	public static EncValue eval(FunStore clientFS, FunStore serverFS, EncTerm m) {
		return (EncValue) repEvalClient(clientFS, serverFS, m);
	}
	
	public static EncTerm repEvalClient(FunStore clientFS, FunStore serverFS, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> either = evalClient(clientFS, m);

		if (either.isLeft()) {
			EncTerm left = either.getLeft();
			
			if (left instanceof Clo) {
				return (Clo) left;
			}
			else if (left instanceof Const) {
				return (Const) left;
			}
			else {
				return repEvalClient(clientFS, serverFS, left);
			}
		}
		else {
			Pair<ClientContext, EncTerm> right = either.getRight();
			
			return repEvalServer(clientFS, serverFS, right.getKey(), right.getValue());
		}
	}
	
	public static EncTerm repEvalServer(FunStore clientFS, FunStore serverFS, ClientContext ctx, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> either = evalServer(serverFS, ctx, m);

		if (either.isLeft()) {
			EncTerm left = either.getLeft();
			
			return repEvalClient(clientFS, serverFS, left);
		}
		else {
			Pair<ClientContext, EncTerm> right = either.getRight();
			return repEvalServer(clientFS, serverFS, right.getKey(), right.getValue());
		}
	}
	
	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalClient(FunStore phi, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> either = new Either<>();
		if (m instanceof Let) {
			Let mLet = (Let) m;
			EncTerm m1 = mLet.getM1();

			if (m1 instanceof App) {
				App mApp1 = (App) m1;

				if (mApp1.getFun() instanceof Clo) {
					Clo funClo = (Clo) mApp1.getFun();
					ClosedFun closedFun = lookup(phi, funClo.getF());
					
					EncTerm left = new Let(mLet.getVal(),
									CSEncMain.substs(
											CSEncMain.substs(closedFun.getM(), closedFun.getZs(), funClo.getVs()),
											closedFun.getXs(), mApp1.getArgs()),
									mLet.getM2());
					either.setLeft(left);
					
					return either;
				}
			}
			else if (m1 instanceof Req) {
				Req mReq1 = (Req) m1;
				
				if (mReq1.getReq() instanceof Clo) {
					Pair<ClientContext, EncTerm> right = new Pair<>(new ClientContext(new Ctx(mLet.getVal(), mLet.getM2())),
																	new App(mReq1.getReq(), mReq1.getArgs()));
					either.setRight(right);
					
					return either;
				}
			}
			else if (m1 instanceof Let) {
				Let mLet1 = (Let) m1;
				
				EncTerm left = new Let(mLet1.getVal(), mLet1.getM1(), new Let(mLet.getVal(), mLet1.getM2(), mLet.getM2()));
				either.setLeft(left);
				
				return either;
			}
			else if (m1 instanceof Clo) {
				Clo mClo1 = (Clo) m1;
				
				EncTerm left = CSEncMain.subst(mLet.getM2(), mLet.getVal(), mClo1);
				either.setLeft(left);
				
				return either;
			}
			else if (m1 instanceof Const) {
				Const mConst1 = (Const) m1;
				
				EncTerm left = CSEncMain.subst(mLet.getM2(), mLet.getVal(), mConst1);
				either.setLeft(left);
				
				return either;
			}
		}
		
		return null;
	}
	
	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalServer(FunStore phi, ClientContext clientCtx, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> either = new Either<>();
		
		if (m instanceof App) {
			App mApp = (App) m;
			
			if (mApp.getFun() instanceof Clo) {
				Clo funClo = (Clo) mApp.getFun();
				
				ClosedFun closedFun = lookup(phi, funClo.getF());
				
				Pair<ClientContext, EncTerm> right = new Pair<>(clientCtx, CSEncMain.substs(
																CSEncMain.substs(closedFun.getM(), closedFun.getZs(), funClo.getVs()),
																closedFun.getXs(), mApp.getArgs()));
				either.setRight(right);
				return either;
			}
		}
		else if (m instanceof Call) {
			Call mCall = (Call) m;

			if (mCall.getCall() instanceof Clo) {
				Clo callClo = (Clo) mCall.getCall();
				Ctx ctx = clientCtx.getCtx();
				
				EncTerm left = new Let(ctx.getX(), new App(callClo, mCall.getArgs()), ctx.getM());
				either.setLeft(left);
				
				return either;
			}
		}
		else if (m instanceof Clo) {
			Clo mClo = (Clo) m;
			Ctx ctx = clientCtx.getCtx();
			
			EncTerm left = new Let(ctx.getX(), mClo, ctx.getM());
			either.setLeft(left);
			
			return either;
		}
		else if (m instanceof Const) {
			Const mConst = (Const) m;
			Ctx ctx = clientCtx.getCtx();
			
			EncTerm left = new Let(ctx.getX(), mConst, ctx.getM());
			either.setLeft(left);
			
			return either;
		}
		
		return null;
	}
}


















