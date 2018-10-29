package com.example.enccs;

import java.util.ArrayList;

import com.example.encrpc.RPCEncMain;
import com.example.rpc.Location;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class CompCSEncTerm {
	public static TripleTup<EncTerm, FunStore, FunStore> compCSEncTerm(com.example.encrpc.EncTerm tt) {
		Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p = comp(1, tt, new ArrayList<String>());
		
		return p.getValue();
	}
	
	public static Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> comp(int i, com.example.encrpc.EncTerm tt, ArrayList<String> zs) {
		Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> pair;
		TripleTup<EncTerm, FunStore, FunStore> triple;
		
		if (tt instanceof com.example.encrpc.Const) {
			com.example.encrpc.Const ttConst = (com.example.encrpc.Const) tt;
			
			triple = new TripleTup<>(new Const(ttConst.getI()), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.Var) {
			com.example.encrpc.Var ttVar = (com.example.encrpc.Var) tt;
			
			triple = new TripleTup<>(new Var(ttVar.getX()), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.Lam) {
			com.example.encrpc.Lam ttLam = (com.example.encrpc.Lam) tt;
			
			ArrayList<String> strs = new ArrayList<>();
			strs.addAll(zs);
			strs.addAll(ttLam.getXs());
			
			com.example.encrpc.EncTerm lamM = ttLam.getM();
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p1 = comp(i, lamM, strs);
			
			ArrayList<String> fvs = RPCEncMain.fv(ttLam);
			ClosedFun closedFun = new ClosedFun(fvs, ttLam.getLoc(), ttLam.getXs(), p1.getValue().getFirst());
			
			String f = "_gf" + p1.getKey();
			
			FunStore clientFS = p1.getValue().getSecond();
			FunStore serverFS = p1.getValue().getThird();
			
			if (ttLam.getLoc() == Location.Client) {
				clientFS.getFs().put(f, closedFun);
			}
			else {
				serverFS.getFs().put(f, closedFun);
			}
			
			ArrayList<EncValue> cloVs = new ArrayList<>();
			
			for (String z: fvs) {
				cloVs.add(new Var(z));
			}
			
			triple = new TripleTup<>(new Clo(f, cloVs), clientFS, serverFS);
			pair = new Pair<>(p1.getKey() + 1, triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.App) {
			com.example.encrpc.App ttApp = (com.example.encrpc.App) tt;
			ArrayList<com.example.encrpc.EncValue> ttAppArgs = (ArrayList<com.example.encrpc.EncValue>) ttApp.getArgs().clone();
			
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p1 = comp(i, ttApp.getFun(), zs);
			Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, ttAppArgs, zs);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new App((EncValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.Let) {
			com.example.encrpc.Let ttLet = (com.example.encrpc.Let) tt;
			
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p1 = comp(i, ttLet.getM1(), zs);
			
			ArrayList<String> strArr = (ArrayList<String>) zs.clone();
			strArr.add(ttLet.getY());
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p2 = comp(p1.getKey(), ttLet.getM2(), strArr);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Let(ttLet.getY(), p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.Req) {
			com.example.encrpc.Req ttReq = (com.example.encrpc.Req) tt;
			
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p1 = comp(i, ttReq.getV(), zs);
			Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, ttReq.getWs(), zs);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Req((EncValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (tt instanceof com.example.encrpc.Call) {
			com.example.encrpc.Call ttCall = (com.example.encrpc.Call) tt;
			
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p1 = comp(i, ttCall.getV(), zs);
			Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, ttCall.getWs(), zs);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Call((EncValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}		
		return null;
	}
	
	public static Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> compList(int i, int idx, ArrayList<com.example.encrpc.EncValue> ms, ArrayList<String> zs) {
		TripleTup<ArrayList<EncValue>, FunStore, FunStore> triple;
		Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> pair;
		
		if (idx == ms.size()) {
			triple = new TripleTup<>(new ArrayList<EncValue>(), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);
			
			return pair;
		}
		else {
			com.example.encrpc.EncTerm m = ms.get(idx);
			
			Pair<Integer, TripleTup<ArrayList<EncValue>, FunStore, FunStore>> p1 = compList(i, idx + 1, ms, zs);
			Pair<Integer, TripleTup<EncTerm, FunStore, FunStore>> p2 = comp(p1.getKey(), m, zs);
			
			ArrayList<EncValue> svArr = new ArrayList<>();
			svArr.add((EncValue) p2.getValue().getFirst());
			svArr.addAll(p1.getValue().getFirst());
			
			FunStore clientFS = p2.getValue().getSecond();
			clientFS.getFs().putAll(p1.getValue().getSecond().getFs());
			
			FunStore serverFS = p2.getValue().getThird();
			serverFS.getFs().putAll(p1.getValue().getThird().getFs());
			
			triple = new TripleTup<>(svArr, clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);
			
			return pair;
		}
	}
}
