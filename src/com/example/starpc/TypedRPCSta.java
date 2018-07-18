package com.example.starpc;

import com.example.rpc.Location;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class TypedRPCSta {
	public static StaValue eval(StaTerm m) {
		return (StaValue) repEvalClient(m, new ServerContext());
	}

	public static StaTerm repEvalClient(StaTerm m, ServerContext serverContext) {
		EitherSta either = evalClient(m, serverContext);

		if (either.getEither().isLeft()) {
			StaTerm v = either.getEither().getLeft().getKey();
			ServerContext serverCtx_ = either.getEither().getLeft().getValue();

			if (v instanceof Lam && serverCtx_.getServerContext().isEmpty()) {
				Lam vLam = (Lam) v;
				return vLam;
			}
			else if (v instanceof Const && serverCtx_.getServerContext().isEmpty()) {
				Const vConst = (Const) v;
				return vConst;
			}
			else {
				return repEvalClient(v, serverCtx_);
			}
		}
		else {
			ClientContext clientCtx_ = either.getEither().getRight().getFirst();
			ServerContext serverCtx_ = either.getEither().getRight().getSecond();
			StaTerm m_ = either.getEither().getRight().getThird();

			return repEvalServer(clientCtx_, serverCtx_, m_);
		}
	}

	public static StaTerm repEvalServer(ClientContext clientCtx, ServerContext serverCtx, StaTerm m) {
		EitherSta either = evalServer(clientCtx, serverCtx, m);

		if (either.getEither().isLeft()) {
			StaTerm v = either.getEither().getLeft().getKey();
			ServerContext serverCtx_ = either.getEither().getLeft().getValue();

			return repEvalClient(v, serverCtx_);
		}
		else {
			ClientContext clientCtx_ = either.getEither().getRight().getFirst();
			ServerContext serverCtx_ = either.getEither().getRight().getSecond();
			StaTerm v_ = either.getEither().getRight().getThird();

			return repEvalServer(clientCtx_, serverCtx_, v_);
		}
	}

	public static EitherSta evalClient(StaTerm m, ServerContext serverCtx) {
		EitherSta retEither = new EitherSta();
		
		if (m instanceof Let) {
			Let mLet = (Let) m;
			String x = mLet.getY();
			StaTerm m1 = mLet.getM1();
			StaTerm m2 = mLet.getM2();
			
			if (m1 instanceof App) {
				App mApp1 = (App) m1;
				
				if (mApp1.getF() instanceof Lam) {
					Lam fLam = (Lam) mApp1.getF();
					
					if (fLam.getLoc() == Location.Client) {
						Let retLet = new Let(x, RPCStaMain.substs(fLam.getM(), fLam.getXs(), mApp1.getWs()), m2);
						Pair<StaTerm, ServerContext> p = new Pair<>(retLet, serverCtx);
						
						retEither.getEither().setLeft(p);
						
						return retEither;
					}						
				}
			}
			else if (m1 instanceof Req) {
				Req mReq1 = (Req) m1;
				
				if (mReq1.getF() instanceof Lam) {
					Lam fLam = (Lam) mReq1.getF();
					
					if (fLam.getLoc() == Location.Server) {
						String rvar = "r";
						Var r = new Var(rvar);
						
						Let retLet = new Let(rvar, new App(fLam, mReq1.getWs()), r);
						TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(new ClientContext(new Context(new Ctx(x, m2))), serverCtx, retLet);
						
						retEither.getEither().setRight(t);
						
						return retEither;
					}
				}
			}
			else if (m1 instanceof Lam) {
				Lam mLam1 = (Lam) m1;
				
				Pair<StaTerm, ServerContext> p = new Pair<>(RPCStaMain.subst(m2, x, mLam1), serverCtx);
				retEither.getEither().setLeft(p);
				
				return retEither;
			}
			else if (m1 instanceof Const) {
				Const mConst1 = (Const) m1;
				
				Pair<StaTerm, ServerContext> p = new Pair<>(RPCStaMain.subst(m2, x, mConst1), serverCtx);
				retEither.getEither().setLeft(p);
				
				return retEither;
			}
			else if (m1 instanceof Let) {
				Let mLet1 = (Let) m1;
				
				Let retLet = new Let(mLet1.getY(), mLet1.getM1(), new Let(x, mLet1.getM2(), m2));
				Pair<StaTerm, ServerContext> p = new Pair<>(retLet, serverCtx);
				retEither.getEither().setLeft(p);
				
				return retEither;				
			}
			else if (m1 instanceof Ret) {
				Ret mRet1 = (Ret) m1;
				Context ctx = serverCtx.getServerContext().pop();
				
				ClientContext clientCtx_ = new ClientContext(new Context(new Ctx(x, m2)));
				Let retLet = new Let(ctx.getCtx().getX(), mRet1.getW(), ctx.getCtx().getM());
				
				TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(clientCtx_, serverCtx, retLet);
				retEither.getEither().setRight(t);
				
				return retEither;
			}
		}
		
		return null;
	}

	public static EitherSta evalServer(ClientContext clientCtx, ServerContext serverCtx, StaTerm m) {
		EitherSta retEither = new EitherSta();
		
		if (m instanceof Let) {
			Let mLet = (Let) m;
			String x = mLet.getY();
			StaTerm m1 = mLet.getM1();
			StaTerm m2 = mLet.getM2();
			
			if (m1 instanceof App) {
				App mApp1 = (App) m1;
				
				if (mApp1.getF() instanceof Lam) {
					Lam fLam = (Lam) mApp1.getF();
					
					if (fLam.getLoc() == Location.Server) {
						Let retLet = new Let(x, RPCStaMain.substs(fLam.getM(), fLam.getXs(), mApp1.getWs()), mLet.getM2());
						TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(clientCtx, serverCtx, retLet);
						
						retEither.getEither().setRight(t);
						
						return retEither;
					}
				}
			}
			else if (m1 instanceof Call) {
				Call mCall1 = (Call) m1;
				
				if (mCall1.getF() instanceof Lam) {
					Lam fLam = (Lam) mCall1.getF();
					
					if (fLam.getLoc() == Location.Client) {
						Ctx ctx = clientCtx.getContext().getCtx();
						serverCtx.getServerContext().push(new Context(new Ctx(x, m2)));
						Let retLet = new Let(ctx.getX(), new App(fLam, mCall1.getWs()), ctx.getM());
						
						Pair<StaTerm, ServerContext> p = new Pair<>(retLet, serverCtx);
						retEither.getEither().setLeft(p);
						
						return retEither;
					}
				}
			}
			else if (m1 instanceof Lam) {
				Lam mLam1 = (Lam) m1;
				StaTerm st = RPCStaMain.subst(m2, x, mLam1);
								
				TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(clientCtx, serverCtx, st);
				retEither.getEither().setRight(t);
				
				return retEither;
			}
			else if (m1 instanceof Const) {
				Const mConst1 = (Const) m1;
				StaTerm st = RPCStaMain.subst(m2, x, mConst1);
				
				TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(clientCtx, serverCtx, st);
				retEither.getEither().setRight(t);
				
				return retEither;
				
			}
			else if (m1 instanceof Let) {
				Let mLet1 = (Let) m1;
				StaTerm st = new Let(mLet1.getY(), mLet1.getM1(), new Let(x, mLet1.getM2(), m2));
				
				TripleTup<ClientContext, ServerContext, StaTerm> t = new TripleTup<>(clientCtx, serverCtx, st);
				retEither.getEither().setRight(t);
				
				return retEither;
			}
		}
		else if (m instanceof Lam) {
			Lam mLam = (Lam) m;
			Ctx ctx = clientCtx.getContext().getCtx();
			
			Pair<StaTerm, ServerContext> p = new Pair<>(new Let(ctx.getX(), mLam, ctx.getM()), serverCtx);
			retEither.getEither().setLeft(p);
			
			return retEither;
		}
		else if (m instanceof Const) {
			Const mConst = (Const) m;
			Ctx ctx = clientCtx.getContext().getCtx();
			
			Pair<StaTerm, ServerContext> p = new Pair<>(new Let(ctx.getX(), mConst, ctx.getM()), serverCtx);
			retEither.getEither().setLeft(p);
			
			return retEither;
		}
		
		return null;
	}
}
