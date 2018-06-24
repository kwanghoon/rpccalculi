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

			if (v instanceof Lam) {
				Lam vLam = (Lam) v;
				return vLam;
			}
			else if (v instanceof Const) {
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
						Let retLet = new Let(x, (StaValue) StaMain.substs(fLam.getM(), fLam.getXs(), mApp1.getWs()), m2);
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
				
			}
			else if (m1 instanceof Const) {
				
			}
			else if (m1 instanceof Let) {
				
			}
			else if (m1 instanceof Ret) {
				
			}
			else {
				if (m2 instanceof Let) {
					
				}
			}
		}
	}

	public static EitherSta evalServer(ClientContext clientCtx, ServerContext serverCtx, StaTerm m) {

	}
}
