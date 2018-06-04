package com.example.typerpc;

import java.util.ArrayList;

import com.example.rpc.Term;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class Infer {
	// genCst :: Int -> Term -> TyEnv -> (TypedTerm, Type, Equations, Int)
	public static TypedTerm infer(Term m) {
		GenCst genCst = new GenCst();
		genCst.genCst(1, m, new TyEnv());
		Equations equs1 = solve(genCst.getEqus());
		TypedTerm tym1 = substTerm(genCst.getTym(), equs1);

		return tym1;
	}

	public static Equations solve(Equations equs) {
		Pair<Equations, Boolean> unify = unifyEqus(equs);
		Pair<Equations, Boolean> merge = mergeAll(unify.getKey());
		Pair<Equations, Boolean> prop = propagate(merge.getKey());

		if (unify.getValue() || merge.getValue() || prop.getValue())
			return solve(prop.getKey());
		else
			return equs;
	}

	public static TypedTerm substTerm(TypedTerm tym, Equations equs) {
		if (tym instanceof Const) {
			Const tyConst = (Const) tym;

			return tyConst;
		} else if (tym instanceof Var) {
			Var var = (Var) tym;

			return var;
		} else if (tym instanceof Lam) {
			Lam lam = (Lam) tym;

			Type ty = substTyEqus(lam.getT(), equs);
			TypedTerm m = substTerm(lam.getTypedTerm(), equs);

			Lam ret = new Lam(lam.getLoc(), lam.getX(), ty, m);

			return ret;
		} else if (tym instanceof App) {
			App app = (App) tym;

			TypedLocation loc = substLocEqus(app.getLoc(), equs);
			TypedTerm fun = substTerm(app.getFun(), equs);
			TypedTerm arg = substTerm(app.getArg(), equs);

			App ret = new App(loc, fun, arg);

			return ret;
		} else
			return null;
	}

	public static Type substTyEqus(Type ty, Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty())
			return ty;
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					EquTy equTy = (EquTy) equ;
					Type ty1 = equTy.getTy1();

					if (ty1 instanceof VarType) {
						VarType varTy = (VarType) ty1;

						return substTyEqus(TypedRPCMain.subst(ty, varTy.getVar(), equTy.getTy2()), equs);
					}

				} else if (equ instanceof EquLoc) {
					EquLoc equLoc = (EquLoc) equ;
					TypedLocation tyloc1 = equLoc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locVarTy = (LocVarType) tyloc1;

						return substTyEqus(TypedRPCMain.substTyTyLoc(ty, locVarTy.getVar(), equLoc.getTyloc2()), equs);
					}
				}
			}
			return null;
		}
	}

	public static TypedLocation substLocEqus(TypedLocation tyloc, Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();

		if (equList == null || equList.isEmpty())
			return tyloc;
		else {
			for (Equ equ : equList) {
				if (equ instanceof EquTy) {
					return substLocEqus(tyloc, equs);
				} else if (equ instanceof EquLoc) {
					EquLoc equLoc = (EquLoc) equ;
					TypedLocation tyloc1 = equLoc.getTyloc1();

					if (tyloc1 instanceof LocVarType) {
						LocVarType locVarTy = (LocVarType) tyloc1;

						return substLocEqus(TypedRPCMain.substTyLoc(tyloc, locVarTy.getVar(), equLoc.getTyloc2()),
								equs);
					}
				}
			}

			return null;
		}
	}

	public static Pair<Equations, Boolean> unifyEqus(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> retList = new ArrayList<>();
		boolean retBool = false;
		if (equList == null || equList.isEmpty())
			return new Pair<>(equs, false);
		else {
			for (Equ equ : equList) {
				Pair<Equations, Boolean> p1 = unify(equ);
				Pair<Equations, Boolean> p2 = unifyEqus(equs);

				retList.addAll(p1.getKey().getEqus());
				retBool = retBool || p1.getValue() || p2.getValue();
			}
		}
		Equations ret = new Equations();
		ret.setEqus(retList);

		return new Pair<>(ret, retBool);
	}

	public static Pair<Equations, Boolean> unify(Equ equ) {
		if (equ instanceof EquTy) {
			EquTy equTy = (EquTy) equ;

			return unify_(equTy.getTy1(), equTy.getTy2());
		} else if (equ instanceof EquLoc) {
			EquLoc equLoc = (EquLoc) equ;

			return unifyLoc_(equLoc.getTyloc1(), equLoc.getTyloc2());
		}
		return null;
	}

	public static Pair<Equations, Boolean> unify_(Type ty1, Type ty2) {
		if (ty1 instanceof IntType) {
			IntType intTy1 = (IntType) ty1;

			if (ty2 instanceof IntType) {
				IntType intTy2 = (IntType) ty2;

				return new Pair<>(new Equations(), false);
			} else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				Equations retEqus = new Equations();
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, intTy1));
				retEqus.setEqus(equList);

				return new Pair<>(retEqus, true);
			} else if (ty2 instanceof FunType) {
				// unifyTyError(ty1, ty2);
			}
		} else if (ty1 instanceof VarType) {
			VarType varTy1 = (VarType) ty1;

			Equations retEqus = new Equations();
			ArrayList<Equ> equList = new ArrayList<>();
			equList.add(new EquTy(varTy1, ty2));
			retEqus.setEqus(equList);

			return new Pair<>(retEqus, true);
		} else if (ty1 instanceof FunType) {
			FunType funTy1 = (FunType) ty1;

			if (ty2 instanceof IntType) {
				// unifyTyError(ty1, ty2);
			} else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;

				Equations retEqus = new Equations();
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, funTy1));
				retEqus.setEqus(equList);

				return new Pair<>(retEqus, true);
			} else if (ty2 instanceof FunType) {
				FunType funTy2 = (FunType) ty2;

				Pair<Equations, Boolean> p1 = unify_(funTy1.getLeft(), funTy2.getLeft());
				Pair<Equations, Boolean> p2 = unifyLoc_(funTy1.getLoc(), funTy2.getLoc());
				Pair<Equations, Boolean> p3 = unify_(funTy1.getRight(), funTy2.getRight());

				Equations retEqus = new Equations();
				ArrayList<Equ> equList = new ArrayList<>();
				equList.addAll(p1.getKey().getEqus());
				equList.addAll(p2.getKey().getEqus());
				equList.addAll(p3.getKey().getEqus());
				retEqus.setEqus(equList);

				return new Pair<>(retEqus, p1.getValue() || p2.getValue() || p3.getValue());
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> unifyLoc_(TypedLocation tyloc1, TypedLocation tyloc2) {
		if (tyloc1 instanceof LocVarType) {
			LocVarType locVarTy1 = (LocVarType) tyloc1;

			Equations retEqus = new Equations();
			ArrayList<Equ> equList = new ArrayList<>();
			equList.add(new EquLoc(locVarTy1, tyloc2));
			retEqus.setEqus(equList);

			return new Pair<>(retEqus, false);
		} else if (tyloc1 instanceof LocType) {
			LocType locTy1 = (LocType) tyloc1;

			if (tyloc2 instanceof LocVarType) {
				LocVarType locVarTy2 = (LocVarType) tyloc2;

				Equations retEqus = new Equations();
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquLoc(locVarTy2, locTy1));
				retEqus.setEqus(equList);

				return new Pair<>(retEqus, true);
			} else if (tyloc2 instanceof LocType) {
				LocType locTy2 = (LocType) tyloc2;

				if (locTy1.getLoc() == locTy2.getLoc())
					return new Pair<>(new Equations(), true);
				else {
					//unifyLocError(locTy1, locTy2);
				}
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> mergeAll(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> retList = new ArrayList<>();
		boolean retBool = false;

		if (equList == null || equList.isEmpty()) {
			return new Pair<>(equs, false);
		} else {
			for (Equ equ : equList) {
				TripleTup<Equations, Equations, Boolean> mergeRest = mergeTheRest(equ, equs);
				Pair<Equations, Boolean> p = mergeAll(mergeRest.getSecond());
				
				retList.add(equ);
				retList.addAll(mergeRest.getFirst().getEqus());
				retList.addAll(p.getKey().getEqus());
				
				retBool = retBool || mergeRest.getThird() || p.getValue();
			}
			
			Equations retEqus = new Equations();
			retEqus.setEqus(retList);
			
			return new Pair<>(retEqus, retBool);
		}
	}
	
	public static TripleTup<Equations, Equations, Boolean> mergeTheRest(Equ equ, Equations equs) {
		for (Equ e: equs.getEqus()) {
			if (equ instanceof EquTy && e instanceof EquTy) {
				EquTy equTy1 = (EquTy) equ;
				EquTy equTy2 = (EquTy) e;
				
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equTy1, equs);
				
				if (equTy1.getTy1() == equTy2.getTy1()) {
					Pair<Equations, Boolean> p1 = unify(new EquTy(equTy1.getTy2(), equTy2.getTy2()));
					
					Equations retEqus = new Equations();
					ArrayList<Equ> equList = new ArrayList<>();
					equList.addAll(p1.getKey().getEqus());
					equList.addAll(merg.getFirst().getEqus());
					retEqus.setEqus(equList);
					
					return new TripleTup<>(retEqus, merg.getSecond(), p1.getValue() || merg.getThird());
				}
				else {
					Equations retSec = merg.getSecond();
					ArrayList<Equ> equList = retSec.getEqus();
					equList.add(equTy2);
					retSec.setEqus(equList);
					
					return new TripleTup<>(merg.getFirst(), retSec, merg.getThird());
				}
				
			}
			else if (equ instanceof EquLoc && e instanceof EquLoc) {
				EquLoc equloc1 = (EquLoc) equ;
				EquLoc equloc2 = (EquLoc) e;
				
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equloc1, equs);
				
				if (equloc1.getTyloc1() == equloc2.getTyloc1()) {
					Pair<Equations, Boolean> p1 = unify(new EquLoc(equloc1.getTyloc2(), equloc2.getTyloc2()));

					Equations retEqus = new Equations();
					ArrayList<Equ> equList = new ArrayList<>();
					equList.addAll(p1.getKey().getEqus());
					equList.addAll(merg.getFirst().getEqus());
					retEqus.setEqus(equList);
					
					return new TripleTup<>(retEqus, merg.getSecond(), p1.getValue() || merg.getThird());
				}
				else {
					Equations retSec = merg.getSecond();
					ArrayList<Equ> equList = retSec.getEqus();
					equList.add(equloc2);
					retSec.setEqus(equList);
					
					return new TripleTup<>(merg.getFirst(), retSec, merg.getThird());
				}
			}
			else {
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, equs);

				Equations retEqus = new Equations();
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(e);
				retEqus.setEqus(equList);
				
				return new TripleTup<>(merg.getFirst(), retEqus, merg.getThird());
			}
		}
		return null;
	}

	public static Pair<Equations, Boolean> propagate(Equations equs) {
		
	}
	
}
