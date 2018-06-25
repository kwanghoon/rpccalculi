package com.example.encrpc;

import java.util.ArrayList;

import com.example.rpc.Location;
import com.example.typedrpc.LocType;
import com.example.typedrpc.TypedTerm;

import javafx.util.Pair;

public class CompEncTerm {
	public static EncTerm compEncTerm(TypedTerm tt) {
		EncTerm et = compClient(1, tt).getValue();

		return et;
	}

	public static Pair<Integer, EncTerm> compClient(int i, TypedTerm tt) {
		Pair<Integer, EncTerm> retPair;

		if (tt instanceof com.example.typedrpc.Const) {
			com.example.typedrpc.Const ttConst = (com.example.typedrpc.Const) tt;

			retPair = new Pair<>(i, new Const(ttConst.getI()));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Var) {
			com.example.typedrpc.Var ttVar = (com.example.typedrpc.Var) tt;

			retPair = new Pair<>(i, new Var(ttVar.getVar()));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Lam) {
			com.example.typedrpc.Lam ttLam = (com.example.typedrpc.Lam) tt;

			if (ttLam.getLoc() == Location.Client) {
				Pair<Integer, EncTerm> p = compClient(i, ttLam.getTypedTerm());
				ArrayList<String> lamXs = new ArrayList<>();
				lamXs.add(ttLam.getX());

				retPair = new Pair<>(p.getKey(), new Lam(Location.Client, lamXs, p.getValue()));
			}
			else {
				String kvar = "k" + i;
				Var k = new Var(kvar);
				Pair<Integer, EncTerm> p = compServer(i + 1, ttLam.getTypedTerm(), k);

				ArrayList<String> lamXs = new ArrayList<>();
				lamXs.add(ttLam.getX());
				lamXs.add(kvar);

				retPair = new Pair<>(p.getKey(), new Lam(Location.Server, lamXs, p.getValue()));
			}

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.App) {
			com.example.typedrpc.App ttApp = (com.example.typedrpc.App) tt;

			if (ttApp.getLoc() instanceof com.example.typedrpc.LocType) {
				LocType appLoc = (LocType) ttApp.getLoc();

				String fvar = "f" + i;
				String xvar = "x" + (i + 1);
				String rvar = "r" + (i + 2);

				Var f = new Var(fvar);
				Var x = new Var(xvar);
				Var r = new Var(rvar);

				if (appLoc.getLoc() == Location.Client) {
					Pair<Integer, EncTerm> p1 = compClient(i + 3, ttApp.getFun());
					Pair<Integer, EncTerm> p2 = compClient(p1.getKey(), ttApp.getArg());

					ArrayList<EncValue> appArgs = new ArrayList<>();
					appArgs.add(x);

					retPair = new Pair<>(p2.getKey(), new Let(fvar, p1.getValue(),
							new Let(xvar, p2.getValue(), new Let(rvar, new App(f, appArgs), r))));

				}
				else {
					Pair<Integer, EncTerm> p1 = compClient(i + 3, ttApp.getFun());
					Pair<Integer, EncTerm> p2 = compClient(p1.getKey(), ttApp.getArg());

					String contxvar = "x" + p2.getKey();
					Var contx = new Var(contxvar);
					ArrayList<String> lamXs = new ArrayList<>();
					lamXs.add(contxvar);

					Lam idcont = new Lam(Location.Server, lamXs, contx);
					int j = p2.getKey() + 1;

					ArrayList<EncValue> reqXs = new ArrayList<>();
					reqXs.add(x);
					reqXs.add(idcont);

					retPair = new Pair<>(j, new Let(fvar, p1.getValue(),
							new Let(xvar, p2.getValue(), new Let(rvar, new Req(f, reqXs), r))));
				}
				
				return retPair;
			}
		}

		return null;
	}

	public static Pair<Integer, EncTerm> compServer(int i, TypedTerm tt, EncTerm k) {
		Pair<Integer, EncTerm> retPair;

		if (tt instanceof com.example.typedrpc.Const) {
			com.example.typedrpc.Const ttConst = (com.example.typedrpc.Const) tt;

			String rvar = "r" + i;
			Var r = new Var(rvar);
			int j = i + 1;
			ArrayList<EncValue> appXs = new ArrayList<>();
			appXs.add(new Const(ttConst.getI()));

			retPair = new Pair<>(j, new Let(rvar, new App((EncValue) k, appXs), r));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Var) {
			com.example.typedrpc.Var ttVar = (com.example.typedrpc.Var) tt;

			String rvar = "r" + i;
			Var r = new Var(rvar);
			int j = i + 1;
			ArrayList<EncValue> appXs = new ArrayList<>();
			appXs.add(new Var(ttVar.getVar()));

			retPair = new Pair<>(j, new Let(rvar, new App((EncValue) k, appXs), r));

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.Lam) {
			com.example.typedrpc.Lam ttLam = (com.example.typedrpc.Lam) tt;

			if (ttLam.getLoc() == Location.Client) {
				String rvar = "r" + i;
				Var r = new Var(rvar);

				Pair<Integer, EncTerm> p = compClient(i + 1, ttLam.getTypedTerm());

				ArrayList<EncValue> appXs = new ArrayList<>();
				ArrayList<String> lamXs = new ArrayList<>();

				lamXs.add(ttLam.getX());
				appXs.add(new Lam(Location.Client, lamXs, p.getValue()));

				retPair = new Pair<>(p.getKey(), new Let(rvar, new App((EncValue) k, appXs), r));
			}
			else {
				String rvar = "r" + i;
				String contkvar = "k" + (i + 1);
				Var r = new Var(rvar);
				Var contk = new Var(contkvar);

				Pair<Integer, EncTerm> p = compServer(i + 3, ttLam.getTypedTerm(), contk);

				ArrayList<EncValue> appXs = new ArrayList<>();
				ArrayList<String> lamXs = new ArrayList<>();

				lamXs.add(ttLam.getX());
				lamXs.add(contkvar);
				appXs.add(new Lam(Location.Server, lamXs, p.getValue()));

				retPair = new Pair<>(p.getKey(), new Let(rvar, new App((EncValue) k, appXs), r));
			}

			return retPair;
		}
		else if (tt instanceof com.example.typedrpc.App) {
			com.example.typedrpc.App ttApp = (com.example.typedrpc.App) tt;

			if (ttApp.getLoc() instanceof com.example.typedrpc.LocType) {
				String fvar = "f" + i;
				String xvar = "x" + (i + 1);
				Var f = new Var(fvar);
				Var x = new Var(xvar);

				com.example.typedrpc.LocType appLoc = (com.example.typedrpc.LocType) ttApp.getLoc();

				if (appLoc.getLoc() == Location.Client) {
					String zvar = "z" + (i + 2);
					String rvar = "r" + (i + 3);
					Var z = new Var(zvar);
					Var r = new Var(rvar);

					ArrayList<String> cLamXs = new ArrayList<>();
					ArrayList<EncValue> cLetXs = new ArrayList<>();
					ArrayList<EncValue> cReqXs = new ArrayList<>();
					cLamXs.add(zvar);
					cLetXs.add(z);
					cReqXs.add(r);

					Lam commuteFun = new Lam(Location.Client, cLamXs,
							new Let(rvar, new App(f, cLetXs), new Req((EncValue) k, cReqXs)));

					ArrayList<String> aLamXs = new ArrayList<>();
					ArrayList<EncValue> aCallXs = new ArrayList<>();
					aLamXs.add(xvar);
					aCallXs.add(x);

					Lam argcont = new Lam(Location.Server, aLamXs, new Call(commuteFun, aCallXs));

					Pair<Integer, EncTerm> p = compServer(i + 4, ttApp.getArg(), argcont);
					ArrayList<String> fLamXs = new ArrayList<>();
					fLamXs.add(fvar);

					Lam fncont = new Lam(Location.Server, fLamXs, p.getValue());

					retPair = compServer(p.getKey(), ttApp.getFun(), fncont);
				}
				else {
					String rvar = "r" + (i + 2);
					Var r = new Var(rvar);

					ArrayList<String> aLamXs = new ArrayList<>();
					ArrayList<EncValue> aAppXs = new ArrayList<>();
					aLamXs.add(xvar);
					aAppXs.add(x);
					aAppXs.add((EncValue) k);

					Lam argcont = new Lam(Location.Server, aLamXs, new Let(rvar, new App(f, aAppXs), r));
					Pair<Integer, EncTerm> p = compServer(i + 3, ttApp.getArg(), argcont);

					ArrayList<String> fLamXs = new ArrayList<>();
					fLamXs.add(fvar);

					Lam fncont = new Lam(Location.Server, fLamXs, p.getValue());
					retPair = compServer(p.getKey(), ttApp.getFun(), fncont);
				}

				return retPair;
			}
		}

		return null;
	}
}
