package com.example.stacs;

import com.example.utils.TripleTup;

import javafx.util.Pair;

public class TypedCSSta {
	public static ClosedFun lookup(FunStore fs, String f) {
		for (String p: fs.getFs().keySet()) {
			if (p.equals(f))
				return fs.getFs().get(p);
		}
		return null;
	}
	
	public static StaValue eval(FunStore clientFS, FunStore serverFS, StaTerm m) {
		return (StaValue) repEvalClient(clientFS, serverFS, m, new ServerContext());
	}
	
	public static StaTerm repEvalClient(FunStore clientFS, FunStore serverFS, StaTerm m, ServerContext serverCtx) {
		EitherSta either = evalClient(clientFS, m, serverCtx);
		
		if (either.getEither().isLeft()) {
			Pair<StaTerm, ServerContext> p = either.getEither().getLeft();
			
			if (p.getKey() instanceof Clo) {
				Clo pClo = (Clo) p.getKey();
				
				return pClo;
			}
			else if (p.getKey() instanceof Const) {
				Const pConst = (Const) p.getKey();
				
				return pConst;
			}
			else {
				return repEvalClient(clientFS, serverFS, p.getKey(), p.getValue());
			}
		}
		else {
			TripleTup<ClientContext, ServerContext, StaTerm> triple = either.getEither().getRight();
			
			return repEvalServer(clientFS, serverFS, triple.getFirst(), triple.getSecond(), triple.getThird());
		}
	}
	
	public static StaTerm repEvalServer(FunStore clientFS, FunStore serverFS, ClientContext clientCtx, ServerContext serverCtx, StaTerm m) {
		EitherSta either = evalServer(serverFS, clientCtx, serverCtx, m);
		
		if (either.getEither().isLeft()) {
			Pair<StaTerm, ServerContext> p = either.getEither().getLeft();
			
			return repEvalClient(clientFS, serverFS, p.getKey(), p.getValue());
		}
		else {
			TripleTup<ClientContext, ServerContext, StaTerm> triple = either.getEither().getRight();
			
			return repEvalServer(clientFS, serverFS, triple.getFirst(), triple.getSecond(), triple.getThird());
		}
	}
	
	public static EitherSta evalClient(FunStore clientFS, StaTerm m, ServerContext serverCtx) {
		EitherSta either = new EitherSta();
		if (m instanceof Let) {
			Let mLet = (Let) m;
			StaTerm m1 = mLet.getM1();
			
			if (m1 instanceof App) {
				App mApp1 = (App) m1;
				
				if (mApp1.getF() instanceof Clo) {
					Clo fClo = (Clo) mApp1.getF();
					
					ClosedFun closedFun = lookup(clientFS, fClo.getF());
					
					Pair<StaTerm, ServerContext> left =
							new Pair<>(CSStaMain.substs(
										CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
										closedFun.getXs(), mApp1.getWs()),
									serverCtx);
					either.getEither().setLeft(left);
					
					return either;
				}
			}
			else if (m1 instanceof Req) {
				Req mReq1 = (Req) m1;
				
				if (mReq1.getF() instanceof Clo) {
					Clo fClo = (Clo) mReq1.getF();
					String rvar = "r";
					Var r = new Var(rvar);
					
					TripleTup<ClientContext, ServerContext, StaTerm> right =
							new TripleTup<>(new ClientContext(new Context(new Ctx(mLet.getY(), mLet.getM2()))),
																serverCtx,
																new Let(rvar, new App(fClo, mReq1.getWs()), r));
					either.getEither().setRight(right);
					
					return either;
				}
			}
			else if (m1 instanceof Clo) {
				Clo mClo1 = (Clo) m1;
				
				Pair<StaTerm, ServerContext> left = new Pair<>(CSStaMain.subst(mLet.getM2(), mLet.getY(), mClo1), serverCtx);
				either.getEither().setLeft(left);
				
				return either;
			}
			else if (m1 instanceof Const) {
				Const mConst1 = (Const) m1;
				
				Pair<StaTerm, ServerContext> left = new Pair<>(CSStaMain.subst(mLet.getM2(), mLet.getY(), mConst1), serverCtx);
				either.getEither().setLeft(left);
				
				return either;
			}
			else if (m1 instanceof Let) {
				Let mLet1 = (Let) m1;
				
				Let let = new Let(mLet1.getY(), mLet1.getM1(), new Let(mLet.getY(), mLet1.getM2(), mLet.getM2()));
				Pair<StaTerm, ServerContext> left = new Pair<>(let, serverCtx);
				
				either.getEither().setLeft(left);
				
				return either;
			}
			else if (m1 instanceof Ret) {
				Ret mRet1 = (Ret) m1;

				Ctx ctx = serverCtx.getServerContext().pop().getCtx();
				TripleTup<ClientContext, ServerContext, StaTerm> right =
						new TripleTup<>(new ClientContext(new Context(new Ctx(mLet.getY(), mLet.getM2()))),
															serverCtx,
															new Let(ctx.getX(), mRet1.getW(), ctx.getM()));
				either.getEither().setRight(right);
				
				return either;
			}
		}
		
		return null;
	}
	
	public static EitherSta evalServer(FunStore phi, ClientContext clientCtx, ServerContext serverCtx, StaTerm m) {
		EitherSta either = new EitherSta();
		
		if (m instanceof Let) {
			Let mLet = (Let) m;
			StaTerm m1 = mLet.getM1();
			
			if (m1 instanceof App) {
				App mApp1 = (App) m1;
				
				if (mApp1.getF() instanceof Clo) {
					Clo fClo = (Clo) mApp1.getF();
					
					ClosedFun closedFun = lookup(phi, fClo.getF());
					Let let = new Let(mLet.getY(), CSStaMain.substs(
													CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
												closedFun.getXs(), mApp1.getWs()), mLet.getM2());
					
					TripleTup<ClientContext, ServerContext, StaTerm> right = new TripleTup<>(clientCtx, serverCtx, let);
					either.getEither().setRight(right);
					
					return either;
				}
			}
			else if (m1 instanceof Call) {
				Call mCall1 = (Call) m1;
				
				if (mCall1.getF() instanceof Clo) {
					Clo fClo = (Clo) mCall1.getF();
					
					Ctx ctx = clientCtx.getContext().getCtx();
					Let let = new Let(ctx.getX(), new App(fClo, mCall1.getWs()), ctx.getM());
					serverCtx.getServerContext().add(new Context(new Ctx(mLet.getY(), mLet.getM2())));
					Pair<StaTerm, ServerContext> left = new Pair<>(let, serverCtx);
					
					either.getEither().setLeft(left);
					
					return either;
				}
			}
			else if (m1 instanceof Clo) {
				Clo mClo1 = (Clo) m1;
				
				StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(),  mClo1);
				TripleTup<ClientContext, ServerContext, StaTerm> right = new TripleTup<>(clientCtx, serverCtx, st);
				
				either.getEither().setRight(right);
				
				return either;
			}
			else if (m1 instanceof Const) {
				Const mConst1 = (Const) m1;
				
				StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(), mConst1);
				TripleTup<ClientContext, ServerContext, StaTerm> right = new TripleTup<>(clientCtx, serverCtx, st);
				
				either.getEither().setRight(right);
				
				return either;
			}
			else if (m1 instanceof Let) {
				Let mLet1 = (Let) m1;
				
				Let let = new Let(mLet1.getY(), mLet1.getM1(), new Let(mLet.getY(), mLet1.getM2(), mLet.getM2()));
				TripleTup<ClientContext, ServerContext, StaTerm> right = new TripleTup<>(clientCtx, serverCtx, let);
				
				either.getEither().setRight(right);
				
				return either;
			}
			
		}
		else if (m instanceof Clo) {
			Clo mClo = (Clo) m;
			
			Ctx ctx = clientCtx.getContext().getCtx();
			Let let = new Let(ctx.getX(), mClo, ctx.getM());
			Pair<StaTerm, ServerContext> left = new Pair<>(let, serverCtx);
			
			either.getEither().setLeft(left);
			
			return either;
		}
		else if (m instanceof Const) {
			Const mConst = (Const) m;
			
			Ctx ctx = clientCtx.getContext().getCtx();
			Let let = new Let(ctx.getX(), mConst, ctx.getM());
			Pair<StaTerm, ServerContext> left = new Pair<>(let, serverCtx);
			
			either.getEither().setLeft(left);
			
			return either;
		}
		
		return null;
	}
}
