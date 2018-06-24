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

					retEither.setLeft(new Let(mLet.getVal(),
							EncMain.substs(fLam.getTerm(), fLam.getStrArr(), app1.getArgs()), mLet.getM2()));

					return retEither;
				}
			}
			else if (mLet.getM1() instanceof Req) {
				Req req1 = (Req) mLet.getM1();

				if (req1.getReq() instanceof Lam && ((Lam) req1.getReq()).getLoc() == Location.Server) {
					Lam rLam = (Lam) req1.getReq();

					retEither.setRight(new Pair<>(new ClientContext(new Ctx(mLet.getVal(), mLet.getM2())),
							new Let("r", new App(rLam, req1.getArgs()), new Var("r"))));

					return retEither;
				}
			}
			else if (mLet.getM1() instanceof Lam) {
				Lam lam1 = (Lam) mLet.getM1();

				retEither.setLeft(EncMain.subst(mLet.getM2(), mLet.getVal(), lam1));

				return retEither;
			}
			else if (mLet.getM1() instanceof Const) {
				Const const1 = (Const) mLet.getM1();

				retEither.setLeft(EncMain.subst(mLet.getM2(), mLet.getVal(), const1));

				return retEither;
			}
			else if (mLet.getM1() instanceof Let) {
				Let let1 = (Let) mLet.getM1();

				retEither.setLeft(
						new Let(let1.getVal(), let1.getM1(), new Let(mLet.getVal(), let1.getM2(), mLet.getM2())));

				return retEither;
			}
		}
		return null;
	}

	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalServer(ClientContext ctx, EncTerm m) {
		Either<EncTerm, Pair<ClientContext, EncTerm>> retEither = new Either<>();

		if (m instanceof Let) {
			Let mLet = (Let) m;

			if (mLet.getM1() instanceof App) {
				App mApp1 = (App) mLet.getM1();

				if (mApp1.getFun() instanceof Lam) {
					Lam fLam = (Lam) mApp1.getFun();

					if (fLam.getLoc() == Location.Server) {
						retEither.setRight(new Pair<>(ctx, new Let(mLet.getVal(),
								EncMain.substs(fLam.getTerm(), fLam.getStrArr(), mApp1.getArgs()), mLet.getM2())));
					}
				}
			}
			else if (mLet.getM1() instanceof Lam) {
				Lam mLam1 = (Lam) mLet.getM1();

				retEither.setRight(new Pair<>(ctx, EncMain.subst(mLet.getM2(), mLet.getVal(), mLam1)));
			}
			else if (mLet.getM1() instanceof Const) {
				Const mConst1 = (Const) mLet.getM1();

				retEither.setRight(new Pair<>(ctx, EncMain.subst(mLet.getM2(), mLet.getVal(), mConst1)));
			}
			else if (mLet.getM1() instanceof Let) {
				Let mLet1 = (Let) mLet.getM1();

				retEither.setRight(new Pair<>(ctx,
						new Let(mLet1.getVal(), mLet1.getM1(), new Let(mLet.getVal(), mLet1.getM2(), mLet.getM2()))));
			}
			else if (mLet.getM1() instanceof Call) {
				retEither = evalServerEta(ctx, mLet);
			}

			return retEither;
		}
		else {
			if (m instanceof Call) {
				Call mCall = (Call) m;

				if (mCall.getCall() instanceof Lam) {
					Lam cLam = (Lam) mCall.getCall();

					if (cLam.getLoc() == Location.Client)
						retEither.setLeft(new Let(ctx.getCtx().getX(), new App(cLam, mCall.getArgs()), ctx.getCtx().getM()));
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
	}

	public static Either<EncTerm, Pair<ClientContext, EncTerm>> evalServerEta(ClientContext ctx, EncTerm encm) {
		if (encm instanceof Let) {
			Let etLet = (Let) encm;

			if (etLet.getM1() instanceof Call && etLet.getM2() instanceof Var) {
				Call etCall = (Call) etLet.getM1();
				Var etVar = (Var) etLet.getM2();

				if (etVar.getX() == etLet.getVal())
					return evalServer(ctx, etCall);
			}
			else if (etLet.getM1() instanceof Call && etLet.getM2() instanceof Let) {
				Let etLet2 = (Let) etLet.getM2();

				if (etLet2.getM1() instanceof Var) {
					Var etVar1 = (Var) etLet2.getM1();

					if (etLet.getVal() == etVar1.getX())
						return evalServerEta(ctx, new Let(etLet2.getVal(), etLet.getM1(), etLet2.getM2()));
				}
			}
		}
		return null;
	}
}
