package com.example.stacs;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.rpc.Parser;
import com.example.rpc.Term;
import com.example.starpc.CompRPCStaTerm;
import com.example.typedrpc.Infer;
import com.example.typedrpc.TypedTerm;
import com.example.utils.TripleTup;

public class TypedCSHttp {
	public static void main(String[] args) {
		Parser parser;

		try {
			parser = new Parser();
			System.out.print("Enter a file name: ");
			String fileName = new Scanner(System.in).next();

			FileReader fileReader = new FileReader("./testcase/" + fileName);
			Scanner scan = new Scanner(fileReader);

			while (scan.hasNext()) {
				System.out.println(scan.nextLine());
			}
			System.out.println();

			fileReader = new FileReader("./testcase/" + fileName);

			Term rpcProgram = parser.Parsing(fileReader);
			TypedTerm typedRPCProgram = Infer.infer(rpcProgram);
			com.example.starpc.StaTerm rpcStaProgram = CompRPCStaTerm.compStaTerm(typedRPCProgram);

			TripleTup<com.example.stacs.StaTerm, com.example.stacs.FunStore, com.example.stacs.FunStore> csStaTerm = CompCSStaTerm
					.compCSStaTerm(rpcStaProgram);

			StaTerm mainExpr = csStaTerm.getFirst();
			FunStore clientFS = csStaTerm.getSecond();
			FunStore serverFS = csStaTerm.getThird();

			CSClient client = new CSClient(clientFS);
			HttpServer server = new HttpServer(serverFS);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (LexerException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			e.printStackTrace();
		}

	}

	public static class CSClient {
		private FunStore clientFS;

		public CSClient(FunStore clientFS) {
			this.clientFS = clientFS;
		}

		public StaValue evalClient(StaTerm m) {
			while (true) {
				if (m instanceof Let) {
					Let mLet = (Let) m;
					StaTerm m1 = mLet.getM1();

					if (m1 instanceof App) {
						App mApp1 = (App) m1;

						if (mApp1.getF() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getF();

							ClosedFun closedFun = lookup(clientFS, fClo.getF());

							m = CSStaMain.substs(CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
									closedFun.getXs(), mApp1.getWs());
						}
					} else if (m1 instanceof Req) {
						Req mReq1 = (Req) m1;

						if (mReq1.getF() instanceof Clo) {
							Clo fClo = (Clo) mReq1.getF();
							ArrayList<StaValue> ws = mReq1.getWs();

						}
					} else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;

						m = CSStaMain.subst(mLet.getM2(), mLet.getY(), mClo1);
					} else if (m1 instanceof Const) {
						Const mConst1 = (Const) m1;

						m = CSStaMain.subst(mLet.getM2(), mLet.getY(), mConst1);
					} else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;

						Let let = new Let(mLet1.getY(), mLet1.getM1(),
								new Let(mLet.getY(), mLet1.getM2(), mLet.getM2()));

						m = let;
					} else if (m1 instanceof Ret) {
						Ret mRet1 = (Ret) m1;

					}
				} else if (m instanceof Clo || m instanceof Const) {
					return (StaValue) m;
				} else {
					System.err.println("TypedCSHttp.evalClient: Must not reach here");
				}
			}
		}

		public static ClosedFun lookup(FunStore fs, String f) {
			for (String p : fs.getFs().keySet()) {
				if (p.equals(f))
					return fs.getFs().get(p);
			}
			System.err.println("lookup: Not found: " + f + " in \n" + fs);
			return null;
		}
	}

}
