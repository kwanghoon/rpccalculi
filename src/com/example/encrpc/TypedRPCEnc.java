package com.example.encrpc;

import com.example.rpc.Location;
import com.example.utils.Either;

import javafx.util.Pair;

public class TypedRPCEnc {
	public static EncValue eval(EncTerm m) {
		EncTerm eTerm = repEvalClient(m);

		return (EncValue) eTerm;
	}

	public static EncTerm repEvalClient(EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> eClient = evalClient(m);

		if (eClient.isLeft()) {
			EncTerm left = eClient.getLeft();

			if (left instanceof Lam) {
				Lam lLam = (Lam) left;

				return lLam;
			}
			else if (left instanceof Const) {
				Const lConst = (Const) left;

				return lConst;
			}
			else {
				return repEvalClient(left);
			}
		}
		else
			return repEvalServer(eClient.getRight().getKey(), eClient.getRight().getValue());
	}

	public static EncTerm repEvalServer(ClientContext ctx, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> eServer = evalServer(ctx, m);

		if (eServer.isLeft())
			return repEvalClient(eServer.getLeft());
		else
			return repEvalServer(eServer.getRight().getKey(), eServer.getRight().getValue());
	}

	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalClient(EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> retEither = new Either<>();

		if (m instanceof Let) {
			Let mLet = (Let) m;

			if (mLet.getM1() instanceof App) {
				App app1 = (App) mLet.getM1();

				if (app1.getFun() instanceof Lam && ((Lam) app1.getFun()).getLoc() == Location.Client) {
					Lam fLam = (Lam) app1.getFun();

					retEither.setLeft(new Let(mLet.getY(),
							RPCEncMain.substs(fLam.getM(), fLam.getXs(), app1.getArgs()), mLet.getM2()));

					return retEither;
				}
			}
			else if (mLet.getM1() instanceof Req) {
				Req req1 = (Req) mLet.getM1();

				if (req1.getV() instanceof Lam && ((Lam) req1.getV()).getLoc() == Location.Server) {
					Lam rLam = (Lam) req1.getV();

					retEither.setRight(new Pair<>(new ClientContext(new Ctx(mLet.getY(), mLet.getM2())),
							new App(rLam, req1.getWs())));

					return retEither;
				}
			}
			else if (mLet.getM1() instanceof Lam) {
				Lam lam1 = (Lam) mLet.getM1();

				retEither.setLeft(RPCEncMain.subst(mLet.getM2(), mLet.getY(), lam1));

				return retEither;
			}
			else if (mLet.getM1() instanceof Const) {
				Const const1 = (Const) mLet.getM1();

				retEither.setLeft(RPCEncMain.subst(mLet.getM2(), mLet.getY(), const1));

				return retEither;
			}
			else if (mLet.getM1() instanceof Let) {
				Let let1 = (Let) mLet.getM1();

				retEither.setLeft(
						new Let(let1.getY(), let1.getM1(), new Let(mLet.getY(), let1.getM2(), mLet.getM2())));

				return retEither;
			}
		}
		return null;
	}

	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalServer(ClientContext ctx, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> retEither = new Either<>();

		if (m instanceof App) {
			App mApp = (App) m;
			
			if (mApp.getFun() instanceof Lam && ((Lam) mApp.getFun()).getLoc() == Location.Server) {
				Lam fLam = (Lam) mApp.getFun();
				
				retEither.setRight(new Pair<>(ctx, RPCEncMain.substs(fLam.getM(), fLam.getXs(), mApp.getArgs())));
				
				return retEither;
			}
		}
		else {
			if (m instanceof Call) {
				Call mCall = (Call) m;

				if (mCall.getV() instanceof Lam) {
					Lam cLam = (Lam) mCall.getV();

					if (cLam.getLoc() == Location.Client)
						retEither.setLeft(
								new Let(ctx.getCtx().getX(), new App(cLam, mCall.getWs()), ctx.getCtx().getM()));
				}
			}
			else if (m instanceof Lam) {
				Lam mLam = (Lam) m;
				retEither.setLeft(new Let(ctx.getCtx().getX(), mLam, ctx.getCtx().getM()));
			}
			else if (m instanceof Const) {
				Const mConst = (Const) m;

				retEither.setLeft(new Let(ctx.getCtx().getX(), mConst, ctx.getCtx().getM()));
			}

			return retEither;
		}
		
		return null;
	}
}
