package com.example.stacs;

import java.util.ArrayList;

import com.example.rpc.Location;
import com.example.starpc.RPCStaMain;
import com.example.utils.TripleTup;

import javafx.util.Pair;

public class CompCSStaTerm {
	public static TripleTup<StaTerm, FunStore, FunStore> compCSStaTerm(com.example.starpc.StaTerm rt) {
		Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p = comp(1, rt, new ArrayList<String>());
		
		TripleTup<StaTerm, FunStore, FunStore> triple = p.getValue();
		
		return triple;
	}
	
	public static Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> comp(int i, com.example.starpc.StaTerm rt, ArrayList<String> zs) {
		Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> pair;
		TripleTup<StaTerm, FunStore, FunStore> triple;
		
		if (rt instanceof com.example.starpc.Const) {
			com.example.starpc.Const rtConst = (com.example.starpc.Const) rt;
			
			triple = new TripleTup<>(new Const(rtConst.getI()), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Var) {
			com.example.starpc.Var rtVar = (com.example.starpc.Var) rt;
			
			triple = new TripleTup<>(new Var(rtVar.getX()), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Lam) {
			com.example.starpc.Lam rtLam = (com.example.starpc.Lam) rt;
			
			ArrayList<String> strs = new ArrayList<>();
			strs.addAll(zs);
			strs.addAll(rtLam.getXs());
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtLam.getM(), strs);
			
			ArrayList<String> fvs = RPCStaMain.fv(rtLam);
			ClosedFun closedFun = new ClosedFun(fvs, rtLam.getLoc(), rtLam.getXs(), p1.getValue().getFirst());
			
			String f = "_gf" + p1.getKey();
			
			FunStore clientFS = p1.getValue().getSecond();
			FunStore serverFS = p1.getValue().getThird();
			
			if (rtLam.getLoc() == Location.Client) {
				clientFS.getFs().put(f, closedFun);
			}
			else {
				serverFS.getFs().put(f, closedFun);
			}
			
			ArrayList<StaValue> cloVs = new ArrayList<>();
			
			for (String z: fvs) {
				cloVs.add(new Var(z));
			}
			
			triple = new TripleTup<>(new Clo(f, cloVs), clientFS, serverFS);
			pair = new Pair<>(p1.getKey() + 1, triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.App) {
			com.example.starpc.App rtApp = (com.example.starpc.App) rt;
			ArrayList<com.example.starpc.StaValue> rtAppArgs = (ArrayList<com.example.starpc.StaValue>) rtApp.getWs().clone();
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtApp.getF(), zs);
			Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, rtAppArgs, zs);

			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new App((StaValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Let) {
			com.example.starpc.Let rtLet = (com.example.starpc.Let) rt;
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtLet.getM1(), zs);
			
			ArrayList<String> strArr = (ArrayList<String>) zs.clone();
			strArr.add(rtLet.getY());
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p2 = comp(p1.getKey(), rtLet.getM2(), strArr);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Let(rtLet.getY(), p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Req) {
			com.example.starpc.Req rtReq = (com.example.starpc.Req) rt;
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtReq.getF(), zs);
			Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, rtReq.getWs(), zs);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Req((StaValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Call) {
			com.example.starpc.Call rtCall = (com.example.starpc.Call) rt;
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtCall.getF(), zs);
			Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> p2 = compList(p1.getKey(), 0, rtCall.getWs(), zs);
			
			FunStore clientFS = p1.getValue().getSecond();
			clientFS.getFs().putAll(p2.getValue().getSecond().getFs());
			
			FunStore serverFS = p1.getValue().getThird();
			serverFS.getFs().putAll(p2.getValue().getThird().getFs());
			
			triple = new TripleTup<>(new Call((StaValue) p1.getValue().getFirst(), p2.getValue().getFirst()), clientFS, serverFS);
			pair = new Pair<>(p2.getKey(), triple);

			return pair;
		}
		else if (rt instanceof com.example.starpc.Ret) {
			com.example.starpc.Ret rtRet = (com.example.starpc.Ret) rt;
			
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p1 = comp(i, rtRet.getW(), zs);
			
			triple = new TripleTup<>(new Ret((StaValue) p1.getValue().getFirst()), p1.getValue().getSecond(), p1.getValue().getThird());
			pair = new Pair<>(p1.getKey(), triple);

			return pair;
		}
		
		return null;
	}
	
	public static Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> compList(int i, int idx, ArrayList<com.example.starpc.StaValue> ms, ArrayList<String> zs) {
		TripleTup<ArrayList<StaValue>, FunStore, FunStore> triple;
		Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> pair;
		
		if (idx == ms.size()) {
			triple = new TripleTup<>(new ArrayList<StaValue>(), new FunStore(), new FunStore());
			pair = new Pair<>(i, triple);
			
			return pair;
		}
		else {
			com.example.starpc.StaTerm m = ms.get(idx);
			
			Pair<Integer, TripleTup<ArrayList<StaValue>, FunStore, FunStore>> p1 = compList(i, idx + 1, ms, zs);
			Pair<Integer, TripleTup<StaTerm, FunStore, FunStore>> p2 = comp(p1.getKey(), m, zs);
			
			ArrayList<StaValue> svArr = new ArrayList<>();
			svArr.add((StaValue) p2.getValue().getFirst());
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
