package com.example.typedrpc;

import java.util.ArrayList;

import com.example.rpc.Term;
import com.example.utils.QuadTup;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class Infer {
	public static QuadTup<TypedTerm, Type, Equations, Integer> genCst(int n, Term t, TyEnv tyenv) {
		QuadTup<TypedTerm, Type, Equations, Integer> ret;

		if (t instanceof com.example.rpc.Const) {
			com.example.rpc.Const tyConst = (com.example.rpc.Const) t;

			ret = new QuadTup<>(new Const(tyConst.getI()), new IntType(), new Equations(), n);

			return ret;
		}
		else if (t instanceof com.example.rpc.Var) {
			com.example.rpc.Var tyVar = (com.example.rpc.Var) t;
			Type ty = tylookup(tyVar.getVar(), tyenv);
			ret = new QuadTup<>(new Var(tyVar.getVar()), ty, new Equations(), n);

			return ret;
		}
		else if (t instanceof com.example.rpc.App) {
			com.example.rpc.App tyApp = (com.example.rpc.App) t;

			QuadTup<TypedTerm, Type, Equations, Integer> retFun = genCst(n, tyApp.getFun(), tyenv);
			QuadTup<TypedTerm, Type, Equations, Integer> retArg = genCst(retFun.getFourth(), tyApp.getArg(), tyenv);

			int n2 = retArg.getFourth();
			TypedLocation locvar = new LocVarType(n2);
			Type retty = new VarType(n2 + 1);

			ArrayList<Equ> equList = new ArrayList<>();
			equList.addAll(retFun.getThird().getEqus());
			equList.addAll(retArg.getThird().getEqus());
			equList.add(new EquTy(retFun.getSecond(), new FunType(retArg.getSecond(), locvar, retty)));

			ret = new QuadTup<>(new App(locvar, retFun.getFirst(), retArg.getFirst()), retty, new Equations(equList),
					n2 + 2);

			return ret;
		}
		else if (t instanceof com.example.rpc.Lam) {
			com.example.rpc.Lam tyLam = (com.example.rpc.Lam) t;

			Type argty = new VarType(n);
			TyEnv tyenv1 = new TyEnv();

			ArrayList<Pair<String, Type>> pairList = tyenv.getPairList();
			pairList.add(new Pair<>(tyLam.getX(), argty));
			tyenv1.setPairList(pairList);

			QuadTup<TypedTerm, Type, Equations, Integer> quad = genCst(n + 1, tyLam.getM(), tyenv1);
			Type funty = new FunType(argty, new LocType(tyLam.getLoc()), quad.getSecond());

			ret = new QuadTup<>(new Lam(tyLam.getLoc(), tyLam.getX(), argty, quad.getFirst()), funty, quad.getThird(),
					quad.getFourth());

			return ret;
		}
		else
			return null;
	}

	public static Type tylookup(String x, TyEnv tyenv) {
		for (Pair<String, Type> p : tyenv.getPairList()) {
			if (p.getKey().equals(x))
				return p.getValue();
		}
		return null;
	}

	public static TypedTerm infer(Term m) {
		QuadTup<TypedTerm, Type, Equations, Integer> quadGenCst = genCst(1, m, new TyEnv());
		Equations equs1 = solve(quadGenCst.getThird());
		TypedTerm tym1 = substTerm(quadGenCst.getFirst(), equs1);

		return tym1;
	}

	public static Equations solve(Equations equs) {
		while (true) {
			Pair<Equations, Boolean> p1 = unifyEqus(equs);
			Pair<Equations, Boolean> p2 = mergeAll(p1.getKey());
			Pair<Equations, Boolean> p3 = propagate(p2.getKey());

			if (p1.getValue() || p2.getValue() || p3.getValue()) {
				equs = p3.getKey();
			}
			else
				return equs;
		}
	}

	public static Pair<Equations, Boolean> unifyEqus(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> retList = new ArrayList<>();
		boolean changed = false;

		if (equList == null || equList.isEmpty())
			return new Pair<>(new Equations(retList), changed);
		else {
			for (Equ equ : equList) {
				Pair<Equations, Boolean> p1 = unify(equ);
				changed = changed || p1.getValue();

				retList.addAll(p1.getKey().getEqus());
			}
			return new Pair<>(new Equations(retList), changed);
		}
	}

	public static Pair<Equations, Boolean> unify(Equ equ) {
		if (equ instanceof EquTy) {
			EquTy equTy = (EquTy) equ;

			return unify_(equTy.getTy1(), equTy.getTy2());
		}
		else if (equ instanceof EquLoc) {
			EquLoc equLoc = (EquLoc) equ;

			return unifyLoc_(equLoc.getTyloc1(), equLoc.getTyloc2());
		}
		return null;
	}

	public static Pair<Equations, Boolean> unify_(Type ty1, Type ty2) {
		Pair<Equations, Boolean> retPair;

		if (ty1 instanceof IntType) {
			IntType intTy1 = (IntType) ty1;

			if (ty2 instanceof IntType) {
				IntType intTy2 = (IntType) ty2;

				retPair = new Pair<>(new Equations(), false);
				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, intTy1));

				retPair = new Pair<>(new Equations(equList), true);
				return retPair;
			}
		}
		else if (ty1 instanceof VarType) {
			VarType varTy1 = (VarType) ty1;

			ArrayList<Equ> equList = new ArrayList<>();
			equList.add(new EquTy(varTy1, ty2));

			retPair = new Pair<>(new Equations(equList), false);

			return retPair;
		}
		else if (ty1 instanceof FunType) {
			FunType funTy1 = (FunType) ty1;

			if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, funTy1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
			else if (ty2 instanceof FunType) {
				FunType funTy2 = (FunType) ty2;

				Pair<Equations, Boolean> p1 = unify_(funTy1.getLeft(), funTy2.getLeft());
				Pair<Equations, Boolean> p2 = unifyLoc_(funTy1.getLoc(), funTy2.getLoc());
				Pair<Equations, Boolean> p3 = unify_(funTy1.getRight(), funTy2.getRight());

				ArrayList<Equ> equList = new ArrayList<>();
				equList.addAll(p1.getKey().getEqus());
				equList.addAll(p2.getKey().getEqus());
				equList.addAll(p3.getKey().getEqus());

				retPair = new Pair<>(new Equations(equList), p1.getValue() || p2.getValue() || p3.getValue());

				return retPair;
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> unifyLoc_(TypedLocation tyloc1, TypedLocation tyloc2) {
		ArrayList<Equ> equList = new ArrayList<>();
		Pair<Equations, Boolean> retPair;

		if (tyloc1 instanceof LocVarType) {
			LocVarType locvarty1 = (LocVarType) tyloc1;

			equList.add(new EquLoc(locvarty1, tyloc2));

			retPair = new Pair<>(new Equations(equList), false);

			return retPair;
		}
		else if (tyloc1 instanceof LocType) {
			LocType locty1 = (LocType) tyloc1;

			if (tyloc2 instanceof LocVarType) {
				LocVarType locvarty2 = (LocVarType) tyloc2;

				equList.add(new EquLoc(locvarty2, locty1));

				retPair = new Pair<>(new Equations(equList), true);

				return retPair;
			}
			else if (tyloc2 instanceof LocType) {
				LocType locty2 = (LocType) tyloc2;

				if (locty1.getLoc() == locty2.getLoc()) {
					retPair = new Pair<>(new Equations(), true);

					return retPair;
				}
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> mergeAll(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		Equations retEqus = new Equations();
		Pair<Equations, Boolean> retPair;

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, false);

			return retPair;
		}
		else {
			Equ equ = equList.get(0);
			equList.remove(equ);

			TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, new Equations(equList));
			Pair<Equations, Boolean> p = mergeAll(merg.getSecond());

			retEqus.getEqus().add(equ);
			retEqus.getEqus().addAll(merg.getFirst().getEqus());
			retEqus.getEqus().addAll(p.getKey().getEqus());

			retPair = new Pair<>(retEqus, merg.getThird() || p.getValue());

			return retPair;
		}
	}

	public static TripleTup<Equations, Equations, Boolean> mergeTheRest(Equ equ, Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		ArrayList<Equ> retList = new ArrayList<>();
		TripleTup<Equations, Equations, Boolean> retTrip;

		if (equList == null || equList.isEmpty()) {
			retTrip = new TripleTup<>(new Equations(), new Equations(), false);
			return retTrip;
		}
		else {
			Equ e = equList.get(0);
			equList.remove(e);

			if (equ instanceof EquTy && e instanceof EquTy) {
				EquTy equty1 = (EquTy) equ;
				EquTy equty2 = (EquTy) e;

				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equty1, new Equations(equList));

				if (equty1.getTy1() == equty2.getTy1()) {
					Pair<Equations, Boolean> p = unify(new EquTy(equty1.getTy2(), equty2.getTy2()));
					retList = new ArrayList<>();
					retList.addAll(p.getKey().getEqus());

					retTrip = new TripleTup<>(new Equations(retList), merg.getSecond(),
							p.getValue() || merg.getThird());

					return retTrip;
				}
				else {
					retList = new ArrayList<>();
					retList.add(equty2);
					retList.addAll(merg.getSecond().getEqus());

					retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

					return retTrip;
				}
			}
			else if (equ instanceof EquLoc && e instanceof EquLoc) {
				EquLoc equloc1 = (EquLoc) equ;
				EquLoc equloc2 = (EquLoc) e;

				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equloc1, new Equations(equList));

				if (equloc1.getTyloc1() == equloc2.getTyloc1()) {
					Pair<Equations, Boolean> p = unify(new EquLoc(equloc1.getTyloc2(), equloc2.getTyloc2()));

					retList = new ArrayList<>();
					retList.addAll(p.getKey().getEqus());
					retList.addAll(merg.getFirst().getEqus());

					retTrip = new TripleTup<>(new Equations(retList), merg.getSecond(),
							p.getValue() || merg.getThird());

					return retTrip;
				}
				else {
					retList = new ArrayList<>();
					retList.addAll(merg.getSecond().getEqus());
					retList.add(equloc2);

					retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

					return retTrip;
				}
			}
			else {
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, new Equations(equList));

				retList = new ArrayList<>();
				retList.add(e);
				retList.addAll(merg.getSecond().getEqus());

				retTrip = new TripleTup<>(merg.getFirst(), new Equations(retList), merg.getThird());

				return retTrip;
			}
		}
	}

	public static Pair<Equations, Boolean> propagate(Equations equs) {
		Pair<Equations, Boolean> prop = propagate_(equs, equs);

		return prop;
	}

	public static TypedTerm substTerm(TypedTerm tt, Equations equs) {
		if (tt instanceof Const) {
			Const ttConst = (Const) tt;

			return ttConst;
		}
		else if (tt instanceof Var) {
			Var ttVar = (Var) tt;

			return ttVar;
		}
		else if (tt instanceof Lam) {
			Lam ttLam = (Lam) tt;

			Type ty = substTyEqus(ttLam.getT(), equs);
			TypedTerm m = substTerm(ttLam.getTypedTerm(), equs);

			Lam retLam = new Lam(ttLam.getLoc(), ttLam.getX(), ty, m);

			return retLam;
		}
		else if (tt instanceof App) {
			App ttApp = (App) tt;

			TypedLocation loc = substLocEqus(ttApp.getLoc(), equs);
			TypedTerm fun = substTerm(ttApp.getFun(), equs);
			TypedTerm arg = substTerm(ttApp.getArg(), equs);

			App retApp = new App(loc, fun, arg);

			return retApp;
		}
		return null;
	}

	public static Type substTyEqus(Type ty, Equations equs) {
		if (equs == null || equs.getEqus().isEmpty())
			return ty;
		else {
			ArrayList<Equ> equList = equs.getEqus();

			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					Type ty1 = equty.getTy1();

					if (ty1 instanceof VarType) {
						VarType varty = (VarType) ty1;
						ty = TypedRPCMain.subst(ty, varty.getVar(), equty.getTy2());
					}
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						ty = TypedRPCMain.substTyTyLoc(ty, locvarty.getVar(), equloc.getTyloc2());
					}
				}
			}
			return ty;
		}
	}

	public static TypedLocation substLocEqus(TypedLocation tyloc, Equations equs) {
		if (equs == null || equs.getEqus().isEmpty())
			return tyloc;
		else {
			ArrayList<Equ> equList = equs.getEqus();

			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					// do nothing
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						tyloc = TypedRPCMain.substTyLoc(tyloc, locvarty.getVar(), equloc.getTyloc2());
					}
				}
			}

			return tyloc;
		}
	}

	public static Pair<Equations, Boolean> propagate_(Equations equs1, Equations equs2) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		Equations cloneEqus = new Equations((ArrayList<Equ>) equs1.getEqus().clone());
		boolean changed = false;

		ArrayList<Equ> equList1 = equs1.getEqus();

		if (equList1 == null || equList1.isEmpty()) {
			retEqus.getEqus().addAll(equs2.getEqus());
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
		else {
			Equ equ = cloneEqus.getEqus().get(0);
			cloneEqus.getEqus().remove(equ);

			if (equ instanceof EquTy) {
				EquTy equty = (EquTy) equ;

				if (equty.getTy1() instanceof VarType) {
					VarType varty1 = (VarType) equty.getTy1();
					Pair<Equations, Boolean> p1 = propagateTy(varty1.getVar(), equty.getTy2(), equs2);
					Pair<Equations, Boolean> p2 = propagate_(cloneEqus, p1.getKey());
					retEqus.getEqus().addAll(p2.getKey().getEqus());
					changed = changed || p1.getValue() || p2.getValue();
				}
			}
			else if (equ instanceof EquLoc) {
				EquLoc equloc = (EquLoc) equ;

				if (equloc.getTyloc1() instanceof LocVarType) {
					LocVarType locvarty1 = (LocVarType) equloc.getTyloc1();

					Pair<Equations, Boolean> p1 = propagateLoc(locvarty1.getVar(), equloc.getTyloc2(), equs2);
					Pair<Equations, Boolean> p2 = propagate_(cloneEqus, p1.getKey());
					retEqus.getEqus().addAll(p2.getKey().getEqus());
					changed = changed || p1.getValue() || p2.getValue();
				}
			}

			retPair = new Pair<>(retEqus, changed);
			return retPair;
		}
	}

	public static Pair<Equations, Boolean> propagateTy(int i, Type ity, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		boolean changed = false;

		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;

					Type ty1 = TypedRPCMain.subst(equty.getTy2(), i, ity);
					changed = changed || !ty1.equals(equty.getTy2());
					retEqus.getEqus().add(new EquTy(equty.getTy1(), ty1));
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;

					retEqus.getEqus().add(equloc);
				}
			}
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
	}

	public static Pair<Equations, Boolean> propagateLoc(int i, TypedLocation ilocty, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		boolean changed = false;

		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, changed);
			return retPair;
		}
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;

					retEqus.getEqus().add(equty);
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;

					TypedLocation tyloc1 = TypedRPCMain.substTyLoc(equloc.getTyloc2(), i, ilocty);
					changed = changed || !equloc.getTyloc2().equals(tyloc1);
					retEqus.getEqus().add(new EquLoc(equloc.getTyloc1(), tyloc1));
				}
			}
			retPair = new Pair<>(retEqus, changed);

			return retPair;
		}
	}

}
