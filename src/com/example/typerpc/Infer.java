package com.example.typerpc;

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
		} else if (t instanceof com.example.rpc.Var) {
			com.example.rpc.Var tyVar = (com.example.rpc.Var) t;
			Type ty = tylookup(tyVar.getVar(), tyenv);
			ret = new QuadTup<>(new Var(tyVar.getVar()), ty, new Equations(), n);
			
			return ret; 
		} else if (t instanceof com.example.rpc.App) {
			com.example.rpc.App tyApp = (com.example.rpc.App) t;
			
			QuadTup<TypedTerm, Type, Equations, Integer> retFun = genCst(n, tyApp.getFun(), tyenv);
			QuadTup<TypedTerm, Type, Equations, Integer> retArg = genCst(retFun.getFourth(), tyApp.getArg(), tyenv);
			
			int n2 = retArg.getFourth();
			TypedLocation locvar = new LocVarType(n2);
			Type retty = new VarType(n2 + 1);
			Equations equ = new Equations();
			
			ArrayList<Equ> equList = new ArrayList<>();
			equList.addAll(retFun.getThird().getEqus());
			equList.addAll(retArg.getThird().getEqus());
			equList.add(new EquTy(retFun.getSecond(), new FunType(retArg.getSecond(), locvar, retty)));
			equ.setEqus(equList);
			
			ret = new QuadTup<>(new App(locvar, retFun.getFirst(), retArg.getFirst()), retty, equ, n2 + 2);
			
			return ret;
		} else if (t instanceof com.example.rpc.Lam) {
			com.example.rpc.Lam tyLam = (com.example.rpc.Lam) t;
			
			Type argty = new VarType(n);
			TyEnv tyenv1 = new TyEnv();
			
			ArrayList<Pair<String, Type>> pairList = tyenv.getPairList();
			pairList.add(new Pair<>(tyLam.getX(), argty));
			tyenv1.setPairList(pairList);
			
			QuadTup<TypedTerm, Type, Equations, Integer> quad = genCst(n + 1, tyLam.getM(), tyenv1);
			Type funty = new FunType(argty, new LocType(tyLam.getLoc()), quad.getSecond());
			
			ret = new QuadTup<>(new Lam(tyLam.getLoc(), tyLam.getX(), argty, quad.getFirst()), funty, quad.getThird(), quad.getFourth());
			
			return ret;
		}
		else
			return null;
	}

	public static Type tylookup(String x, TyEnv tyenv) {
		for (Pair<String, Type> p : tyenv.getPairList()) {
			if (p.getKey() == x)
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
		Pair<Equations, Boolean> p1 = unifyEqus(equs);
		Pair<Equations, Boolean> p2 = mergeAll(p1.getKey());
		Pair<Equations, Boolean> p3 = propagate(p2.getKey());
		
		if (p1.getValue() || p2.getValue() || p3.getValue())
			solve(p3.getKey());
		else
			return equs;
		
		return null;
	}
	
	public static Pair<Equations, Boolean> unifyEqus(Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> cloneList = (ArrayList<Equ>) equs.getEqus().clone();
		ArrayList<Equ> retList = new ArrayList<>();
		boolean retBool = false;
		
		if (equList == null || equList.isEmpty())
			return new Pair<>(equs, retBool);
		else {
			for(Equ equ: equList) {
				cloneList.remove(equ);
				Pair<Equations, Boolean> p1 = unify(equ);
				Equations restEqus = new Equations();
				restEqus.setEqus(cloneList);
				Pair<Equations, Boolean> p2 = unifyEqus(restEqus);
				
				retList.addAll(p1.getKey().getEqus());
				retList.addAll(p2.getKey().getEqus());
				retBool = retBool || p1.getValue().booleanValue() || p2.getValue().booleanValue();
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
		}
		else if (equ instanceof EquLoc) {
			EquLoc equLoc = (EquLoc) equ;
			
			return unifyLoc_(equLoc.getTyloc1(), equLoc.getTyloc2());
		}
		return null;
	}
	
	public static Pair<Equations, Boolean> unify_(Type ty1, Type ty2) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		
		if (ty1 instanceof IntType) {
			IntType intTy1 = (IntType) ty1;
			
			if (ty2 instanceof IntType) {
				IntType intTy2 = (IntType) ty2;
				
				retPair = new Pair<>(retEqus, false);
				return retPair;
			}
			else if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;
				
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, intTy1));
				retEqus.setEqus(equList);
				
				retPair = new Pair<>(retEqus, true);
				return retPair;
			}
		}
		else if (ty1 instanceof VarType) {
			VarType varTy1 = (VarType) ty1;
			
			ArrayList<Equ> equList = new ArrayList<>();
			equList.add(new EquTy(varTy1, ty2));
			retEqus.setEqus(equList);
			
			retPair = new Pair<>(retEqus, true);
			
			return retPair;
		}
		else if (ty1 instanceof FunType) {
			FunType funTy1 = (FunType) ty1;
			
			if (ty2 instanceof VarType) {
				VarType varTy2 = (VarType) ty2;
				
				ArrayList<Equ> equList = new ArrayList<>();
				equList.add(new EquTy(varTy2, funTy1));
				retEqus.setEqus(equList);
				
				retPair = new Pair<>(retEqus, true);
				
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
				retEqus.setEqus(equList);
				
				retPair = new Pair<>(retEqus, p1.getValue() || p2.getValue() || p3.getValue());
				
				return retPair;
			}
		}
		return null;
	}
	
	public static Pair<Equations, Boolean> unifyLoc_(TypedLocation tyloc1, TypedLocation tyloc2) {
		Equations retEqus = new Equations();
		ArrayList<Equ> equList = new ArrayList<>();
		Pair<Equations, Boolean> retPair;
		
		if (tyloc1 instanceof LocVarType) {
			LocVarType locvarty1 = (LocVarType) tyloc1;
			
			equList.add(new EquLoc(locvarty1, tyloc2));
			retEqus.setEqus(equList);
			
			retPair = new Pair<>(retEqus, false);
			
			return retPair;
		}
		else if (tyloc1 instanceof LocType) {
			LocType locty1 = (LocType) tyloc1;
			
			if (tyloc2 instanceof LocVarType) {
				LocVarType locvarty2 = (LocVarType) tyloc2;
				
				equList.add(new EquLoc(locvarty2, locty1));
				retEqus.setEqus(equList);
				
				retPair = new Pair<>(retEqus, true);
				
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
		ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
		
		Equations retEqus = new Equations();
		ArrayList<Equ> retList = new ArrayList<>();
		Pair<Equations, Boolean> retPair;
		
		boolean retBool = false;
		
		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, false);
			
			return retPair;
		}
		else {
			for (Equ equ: equList) {
				cloneList.remove(equ);
				Equations restEqus = new Equations();
				restEqus.setEqus(cloneList);
				TripleTup<Equations, Equations, Boolean> mergeRest = mergeTheRest(equ, restEqus);
				Pair<Equations, Boolean> p = mergeAll(mergeRest.getSecond());
				
				retList.add(equ);
				retList.addAll(mergeRest.getFirst().getEqus());
				retList.addAll(p.getKey().getEqus());
				
				retBool = retBool || mergeRest.getThird() || p.getValue();
			}
			
			retEqus.setEqus(retList);
			retPair = new Pair<>(retEqus, retBool);
			
			return retPair;			
		}			
	}
	
	public static TripleTup<Equations, Equations, Boolean> mergeTheRest(Equ equ, Equations equs) {
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
		
		Equations retEqus = new Equations();
		ArrayList<Equ> retList = new ArrayList<>();
		TripleTup<Equations, Equations, Boolean> retTrip;
		
		Equations restEqus = new Equations();
		
		if (equList == null || equList.isEmpty()) {
			retTrip = new TripleTup<>(new Equations(), new Equations(), false);
			return retTrip;
		}
			
		
		for (Equ e: equList) {
			cloneList.remove(e);
			restEqus.setEqus(cloneList);
			
			if (equ instanceof EquTy && e instanceof EquTy) {
				EquTy equty1 = (EquTy) equ;
				EquTy equty2 = (EquTy) e;

				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equty1, restEqus);
				
				if (equty1.getTy1() == equty2.getTy2()) {
					Pair<Equations, Boolean> p = unify(new EquTy(equty1.getTy2(), equty2.getTy2()));
					
					retList.addAll(p.getKey().getEqus());
					retList.addAll(merg.getFirst().getEqus());
					retEqus.setEqus(retList);
					
					retTrip = new TripleTup<>(retEqus, merg.getSecond(), p.getValue() || merg.getThird());
					
					return retTrip;
				}
				else {
					equList.addAll(merg.getSecond().getEqus());
					equList.add(equty2);
					
					retEqus.setEqus(retList);
					retTrip = new TripleTup<>(merg.getFirst(), retEqus, merg.getThird());
					
					return retTrip;
				}
			}
			else if (equ instanceof EquLoc && e instanceof EquLoc) {
				EquLoc equloc1 = (EquLoc) equ;
				EquLoc equloc2 = (EquLoc) e;
				
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equloc1, restEqus);
				
				if (equloc1.getTyloc1() == equloc2.getTyloc1()) {
					Pair<Equations, Boolean> p = unify(new EquLoc(equloc1.getTyloc2(), equloc2.getTyloc2()));
					
					retList.addAll(p.getKey().getEqus());
					retList.addAll(merg.getFirst().getEqus());
					retEqus.setEqus(retList);
					
					retTrip = new TripleTup<>(retEqus, merg.getSecond(), p.getValue() || merg.getThird());
					
					return retTrip;
				}
				else {
					retList.addAll(merg.getSecond().getEqus());
					retList.add(equloc2);
					retEqus.setEqus(retList);
					
					retTrip = new TripleTup<>(merg.getFirst(), retEqus, merg.getThird());
					
					return retTrip;
				}
			}
			else {
				TripleTup<Equations, Equations, Boolean> merg = mergeTheRest(equ, restEqus);
				
				retList.add(e);
				retEqus.setEqus(retList);
				
				retTrip = new TripleTup<>(merg.getFirst(), retEqus, merg.getThird());
				
				return retTrip;
			}
		}
		
		return null;
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
			ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
			Equations restEqus = new Equations();
			
			for (Equ equ: equList) {
				cloneList.remove(equ);
				
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					Type ty1 = equty.getTy1();
					
					if (ty1 instanceof VarType) {
						VarType varty = (VarType) ty1;
						restEqus.setEqus(cloneList);
						return substTyEqus(TypedRPCMain.subst(ty, varty.getVar(), equty.getTy2()), restEqus);
					}
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();
					
					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						restEqus.setEqus(cloneList);
						return substTyEqus(TypedRPCMain.substTyTyLoc(ty, locvarty.getVar(), equloc.getTyloc2()), restEqus);
					}
				}
			}
		}
		
		return null;
	}
	
	public static TypedLocation substLocEqus(TypedLocation tyloc, Equations equs) {		
		if (equs == null || equs.getEqus().isEmpty())
			return tyloc;
		else {
			ArrayList<Equ> equList = equs.getEqus();
			ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
			Equations restEqus = new Equations();
			
			for (Equ equ: equList) {
				cloneList.remove(equ);
				restEqus.setEqus(cloneList);
				
				if (equ instanceof EquTy) {
					return substLocEqus(tyloc, restEqus);
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					TypedLocation tyloc1 = equloc.getTyloc1();
					
					if (tyloc1 instanceof LocVarType) {
						LocVarType locvarty = (LocVarType) tyloc1;
						return substLocEqus(TypedRPCMain.substTyLoc(tyloc, locvarty.getVar(), equloc.getTyloc2()), restEqus);
					}
				}
			}
		}
		return null;
	}
	
	public static Pair<Equations, Boolean> propagate_(Equations equs1, Equations equs2) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		
		ArrayList<Equ> equList1 = equs1.getEqus();
		ArrayList<Equ> cloneList1 = (ArrayList<Equ>) equList1.clone();
		Equations restEqus = new Equations();
		
		if (equList1 == null || equList1.isEmpty()) {
			retPair = new Pair<>(retEqus, false);
			
			return retPair;
		}
		else {
			for (Equ equ: equList1) {
				cloneList1.remove(equ);
				restEqus.setEqus(cloneList1);
				
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					
					if (equty.getTy1() instanceof VarType) {
						VarType varty1 = (VarType) equty.getTy1();
						Pair<Equations, Boolean> p1 = propagateTy(varty1.getVar(), equty.getTy2(), equs2);
						Pair<Equations, Boolean> p2 = propagate_(restEqus, p1.getKey());
						
						retPair = new Pair<>(p2.getKey(), p1.getValue() || p2.getValue());
						
						return retPair;
					}
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					
					if (equloc.getTyloc1() instanceof LocVarType) {
						LocVarType locvarty1 = (LocVarType) equloc.getTyloc1();
						
						Pair<Equations, Boolean> p1 = propagateLoc(locvarty1.getVar(), equloc.getTyloc2(), equs2);
						Pair<Equations, Boolean> p2 = propagate_(restEqus, p1.getKey());
						
						retPair = new Pair<>(p2.getKey(), p1.getValue() || p2.getValue());
						
						return retPair;
					}
				}
			}
		}
		return null;
	}
	
	public static Pair<Equations, Boolean> propagateTy(int i, Type ity, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		ArrayList<Equ> retList;
		
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
		Equations restEqus = new Equations();
		
		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, false);
			
			return retPair;
		}
		else {
			for (Equ equ: equList) {
				cloneList.remove(equ);
				restEqus.setEqus(cloneList);
				
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					
					Type ty1 = TypedRPCMain.subst(equty.getTy2(), i, ity);
					Pair<Equations, Boolean> p1 = propagateTy(i, ity, restEqus);
					
					retList = p1.getKey().getEqus();
					retList.add(new EquTy(equty.getTy1(), ty1));
					retEqus.setEqus(retList);
					
					retPair = new Pair<>(retEqus, /*ty1 /= ty ||*/ p1.getValue());
					return retPair;
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					
					Pair<Equations, Boolean> p = propagateTy(i, ity, restEqus);
					
					retList = p.getKey().getEqus();
					retList.add(new EquLoc(equloc.getTyloc1(), equloc.getTyloc2()));
					retEqus.setEqus(retList);
					retPair = new Pair<>(retEqus, p.getValue());
					
					return retPair;
				}
			}
		}
		return null;
	}
	
	public static Pair<Equations, Boolean> propagateLoc(int i, TypedLocation ilocty, Equations equs) {
		Pair<Equations, Boolean> retPair;
		Equations retEqus = new Equations();
		
		ArrayList<Equ> equList = equs.getEqus();
		ArrayList<Equ> cloneList = (ArrayList<Equ>) equList.clone();
		Equations restEqus = new Equations();
		
		if (equList == null || equList.isEmpty()) {
			retPair = new Pair<>(retEqus, false);
			return retPair;
		}
		else {
			for (Equ equ: equList) {
				cloneList.remove(equ);
				restEqus.setEqus(cloneList);
				
				if (equ instanceof EquTy) {
					EquTy equty = (EquTy) equ;
					
					Pair<Equations, Boolean> p1 = propagateLoc(i, ilocty, restEqus);
					ArrayList<Equ> retList = p1.getKey().getEqus();
					retList.add(equ);
					retEqus.setEqus(retList);
					
					retPair = new Pair<>(retEqus, p1.getValue());
					
					return retPair;
				}
				else if (equ instanceof EquLoc) {
					EquLoc equloc = (EquLoc) equ;
					
					TypedLocation tyloc1 = TypedRPCMain.substTyLoc(equloc.getTyloc2(), i, ilocty);
					Pair<Equations, Boolean> p1 = propagateLoc(i, ilocty, restEqus);
					
					ArrayList<Equ> retList = p1.getKey().getEqus();
					retList.add(new EquLoc(equloc.getTyloc1(), tyloc1));
					retEqus.setEqus(retList);
					
					retPair = new Pair<>(retEqus, /*equloc.getTyloc2() / tyloc1 ||*/ p1.getValue());
					
					return retPair;
				}
			}
		}
		return null;
	}
}
 