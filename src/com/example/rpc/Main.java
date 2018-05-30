package com.example.rpc;

public class Main {
	public static Value eval(Term m, Location loc) {
		if (m instanceof Lam) {
			Lam lam = (Lam) m;
			return lam;
		} else if (m instanceof App) {
			App app = (App) m;
			Lam lam = (Lam) eval(app.getFun(), loc);
			Value w = eval(app.getArg(), loc);
			Value v = eval(subst(lam.getM(), lam.getX(), w), lam.getLoc());

			return v;
		} else if (m instanceof Const) {
			Const con = (Const) m;

			return con;
		} else {
			return null;
		}
	}

	public static Term subst(Term m, String x, Value v) {
		if (m instanceof Var) {
			Var var = (Var) m;

			if (var.getVar() == x) {
				return v;
			} else {
				return var;
			}
		} else if (m instanceof Lam) {
			Lam lam = (Lam) m;

			if (lam.getX() == x) {
				return lam;
			} else {
				Lam l = lam;
				l.setM(subst(lam.getM(), x, v));

				return l;
			}
		} else if (m instanceof App) {
			App app = (App) m;

			Term left = subst(app.getFun(), x, v);
			Term right = subst(app.getArg(), x, v);
			App ret = new App(left, right);

			return ret;
		} else if (m instanceof Const) {
			Const con = (Const) m;

			return con;
		} else
			return null;
	}

	public static void main(String[] args) {
		Term leftApp = new App(
				new Lam(Location.Server, "x", new Var("x")),
				new App(new Var("f"), new Const(1)));
		
		Term left = new Lam(Location.Server, "f", leftApp);
		
		Term right = new Lam(Location.Client, "y",
				new App(new Lam(Location.Server, "z", new Var("z")),
						new Var("y")));
		
		Term ex1 = new App(left, right);
		System.out.println(ex1.toString());
		System.out.println(eval(ex1, Location.Client).toString());
	}
}
