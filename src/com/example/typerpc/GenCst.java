package com.example.typerpc;

import java.util.ArrayList;

import com.example.rpc.Term;

import javafx.util.Pair;

public class GenCst {
	private TypedTerm tym;
	private Type ty;
	private Equations equs;
	private int n;	
	
	public GenCst() {
		this.equs = new Equations();
	}
	
	public TypedTerm getTym() {
		return tym;
	}
	public void setTym(TypedTerm tym) {
		this.tym = tym;
	}
	public Type getTy() {
		return ty;
	}
	public void setTy(Type ty) {
		this.ty = ty;
	}
	public Equations getEqus() {
		return equs;
	}
	public void setEqus(Equations equs) {
		this.equs = equs;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	
	public void genCst(int i, Term t, TyEnv tyenv) {
		if (t instanceof com.example.rpc.Const) {
			com.example.rpc.Const cst = (com.example.rpc.Const) t;
			this.tym = new Const(cst.getI());
			this.ty = new IntType();
			this.n= i;
		}
		else if (t instanceof com.example.rpc.Var) {
			com.example.rpc.Var var = (com.example.rpc.Var) t;
			this.ty = tylookup(var.getVar(), tyenv);
			this.tym = new Var(var.getVar());
			this.n = i;
		}
		else if (t instanceof com.example.rpc.App) {
			com.example.rpc.App app = (com.example.rpc.App) t;
			
			GenCst gen1 = new GenCst();
			GenCst gen2 = new GenCst();
			gen1.genCst(i, app.getFun(), tyenv);
			gen2.genCst(gen1.getN(), app.getArg(), tyenv);
			
			TypedLocation locvar = new LocVarType(gen2.getN());
			Type retty = new VarType(gen2.getN() + 1);
			
			this.equs = gen1.getEqus();
			ArrayList<Equ> tmp = this.equs.getEqus();
			tmp.addAll(gen2.getEqus().getEqus());
			tmp.add(new EquTy(gen1.getTy(), new FunType(gen2.getTy(), locvar, retty)));
			
			this.tym = new App(locvar, gen1.getTym(), gen2.getTym());
			this.ty = retty;
			this.equs.setEqus(tmp);
			this.n = gen2.getN() + 2;
		}
		else if (t instanceof com.example.rpc.Lam) {
			com.example.rpc.Lam lam = (com.example.rpc.Lam) t;

			Type argty = new VarType(i + 1);
			TyEnv tyenv1 = new TyEnv();
			ArrayList<Pair<String, Type>> tmp = tyenv1.getPairList();
			tmp.add(new Pair<>(lam.getX(), argty));
			tyenv1.setPairList(tmp);
			
			GenCst gen1 = new GenCst();
			gen1.genCst(i, lam.getM(), tyenv1);
			
			Type funty = new FunType(argty, new LocType(lam.getLoc()), gen1.getTy());
			
			this.tym = new Lam(lam.getLoc(), lam.getX(), argty, gen1.getTym());
			this.ty = funty;
			this.equs = gen1.getEqus();
			this.n = gen1.getN() + 1;
		}
	}
	
	public Type tylookup(String str, TyEnv tyenv) {
		for (Pair<String, Type> p: tyenv.getPairList()) {
			if (p.getKey() == str) {
				return p.getValue();
			}
		}
		System.out.println("lookup error: " + str);
		return null;
	}
}
