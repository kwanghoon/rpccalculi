package com.example.stacs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpServer {
	private FunStore phi;

	private static HashMap<Integer, CSServer> sessionMap = new HashMap<>();
	private static int count = 0;

	public HttpServer(FunStore phi) {
		this.phi = phi;
	}

	public void start() {
		try {
			ServerSocket s = new ServerSocket(8081, 10, InetAddress.getByName("127.0.0.1"));

			while (true) {
				Socket conn = s.accept();
				InputStream input = conn.getInputStream();
				OutputStream output = conn.getOutputStream();

				Request request = new Request(input);
				HashMap<String, String> paramMap = request.parse();

				String sessionState = paramMap.get("sessionState");
				CSServer server = null;

				if (sessionState.equals("OPEN_SESSION")) {
					count = count + 1;
					server = new CSServer(phi);
				} else if (sessionState.equals("SESSION")) {
					Integer sessionNum = Integer.parseInt(paramMap.get("sessionNum"));
					server = sessionMap.get(sessionNum);
				}
				else {
					System.err.println("HttpServer.start(): Unexpected session state " + sessionState); 
				}

				String protocol = paramMap.get("protocol");
				JSONParser jsonParser = new JSONParser();

				if (protocol.equals("REQ")) {
					String fun = paramMap.get("fun");
					JSONObject funJson = (JSONObject) jsonParser.parse(fun);
					StaValue clo = JSonUtil.fromJson(funJson);

					int num = Integer.parseInt(paramMap.get("num"));

					ArrayList<StaValue> args = new ArrayList<>();

					for (int i = 1; i <= num; i++) {
						String strArg = paramMap.get("args" + i);
						JSONObject argJson = (JSONObject) jsonParser.parse(strArg);
						StaValue arg = JSonUtil.fromJson(argJson);

						args.add(arg);
					}
					String rStr = "r";
					Var rVar = new Var(rStr);

					StaTerm reqLet = new Let(rStr, new App(clo, args), rVar);

					server.evalServer(reqLet);

				} else if (protocol.equals("RET")) {
					String ret = paramMap.get("ret");
					JSONObject retJson = (JSONObject) jsonParser.parse(ret);
					StaValue retVal = JSonUtil.fromJson(retJson);

					// server의 evalServer가 yield된 시점으로 전환해주는 부분
					// server.resume();
				}

				Response response = new Response(output);

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	class CSServer {
		private FunStore phi;

		CSServer(FunStore phi) {
			this.phi = phi;
		}

		public void evalServer(StaTerm m) throws ParseException {
			while (true) {
				if (m instanceof Let) {
					Let mLet = (Let) m;
					StaTerm m1 = mLet.getM1();
					
					if (m1 instanceof App) {
						App mApp1 = (App) m1;
						
						if (mApp1.getF() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getF();
							
							ClosedFun closedFun = lookup(phi, fClo.getF());
							Let let = 
								new Let(mLet.getY(), 
									CSStaMain.substs(
										CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
											closedFun.getXs(), mApp1.getWs()), mLet.getM2());
							
							m = let;
						}
					}
					else if (m1 instanceof Call) {
						Call mCall1 = (Call) m1;
						
						if (mCall1.getF() instanceof Clo) {
							Clo fClo = (Clo) mCall1.getF();
							ArrayList<StaValue> args = mCall1.getWs();
							
							// Client로 Call을 날리는 부분
							// this.yield();
						}
					}
					else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;
						
						StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(),  mClo1);
						m = st;
					}
					else if (m1 instanceof Const) {
						Const mConst1 = (Const) m1;
						
						StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(), mConst1);
						m = st;
					}
					else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;
						
						Let let = new Let(mLet1.getY(), mLet1.getM1(), new Let(mLet.getY(), mLet1.getM2(), mLet.getM2()));
						m = let;
					}
					
				}
				else if (m instanceof Clo) {
					Clo mClo = (Clo) m;
					
					// REPLY
					
					return;
					
					
				}
				else if (m instanceof Const) {
					Const mConst = (Const) m;
					
					// REPLY
					
					return;
				}		
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

	class Request {
		private InputStream input;
		private String url;

		Request(InputStream input) {
			this.input = input;
		}

		public HashMap<String, String> parse() {
			try {
				StringBuffer requestBuffer = new StringBuffer();
				String line;
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));

				while ((line = reader.readLine()) != null) {
					requestBuffer.append(line);
				}

				String request = requestBuffer.toString();
				System.out.println(request);

				int idxMethod = request.indexOf(" ");
				int idxVersion = request.indexOf(" ", idxMethod + 1);

				url = request.substring(idxMethod + 1, idxVersion); // /protocol=REQ&sessionState=OPEN_SESSION&fun=Clo&num=n&args1=args1
																	// ... &argsn=argsn
																	// /protocol=RET&sessionState=SESSION&sessionNum=num&ret=retVal.toJson()

				HashMap<String, String> paramMap = new HashMap<>();
				int idx = 0;

				while (idx < url.length()) {
					int idxAnd = url.indexOf("&", idx + 1);
					int idxEqual = url.indexOf("=", idx + 1);
					if (idxAnd != -1) {
						String data = url.substring(idx + 1, idxAnd);
						int idxDataEqual = data.indexOf("=");

						String param = data.substring(0, idxDataEqual);
						String paramVal = data.substring(idxDataEqual + 1, data.length());

						paramMap.put(param, paramVal);

						idx = idxAnd;
					} else {
						String data = url.substring(idx + 1, url.length());
						int idxDataEqual = data.indexOf("=");

						String param = data.substring(0, idxDataEqual);
						String paramVal = data.substring(idxDataEqual + 1, data.length());

						paramMap.put(param, paramVal);

						idx = idxAnd;
						break;
					}
				}

				return paramMap;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	class Response {
		private OutputStream output;

		Response(OutputStream output) {
			this.output = output;
		}

		public void sendResult(String message) {

		}
	}
}
