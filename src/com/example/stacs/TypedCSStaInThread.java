package com.example.stacs;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.rpc.Parser;
import com.example.rpc.Term;
import com.example.starpc.CompRPCStaTerm;
import com.example.typedrpc.Infer;
import com.example.utils.TripleTup;

public class TypedCSStaInThread {
	// Protocol headers
	private static final String REQ = "REQ";
	private static final String RET = "RET";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 7777;

	public static void main(String[] args) {
		try {
			Parser parser = new Parser();

			System.out.println("Enter a file name: ");

			String fileName = new Scanner(System.in).next();

			FileReader fileReader = new FileReader("./testcase/" + fileName);
			System.out.println("./testcase/" + fileName);

			Scanner scan = new Scanner(fileReader);
			while (scan.hasNext()) {
				System.out.println(scan.nextLine());
			}
			System.out.println();

			fileReader = new FileReader("./testcase/" + fileName);

			Term rpc_program = parser.Parsing(fileReader);

			com.example.typedrpc.TypedTerm typed_rpc_program = Infer.infer(rpc_program);

			com.example.starpc.StaTerm staTerm = CompRPCStaTerm.compStaTerm(typed_rpc_program);

			TripleTup<com.example.stacs.StaTerm, com.example.stacs.FunStore, com.example.stacs.FunStore> csStaTerm = CompCSStaTerm
					.compCSStaTerm(staTerm);

			FunStore clientFS = csStaTerm.getSecond();
			FunStore serverFS = csStaTerm.getThird();
			StaTerm main_expr = csStaTerm.getFirst();

			System.out.println("Main expression:");
			System.out.println(main_expr);
			System.out.println();

			System.out.println("Client Functions:");
			System.out.println(clientFS);

			System.out.println("Server Functions:");
			System.out.println(serverFS);
			
			Thread was_thread = new Thread(() -> {
				SocketWas was = new SocketWas();
				was.setServerFS(serverFS);
				was.start();
			});
			
			was_thread.start();

			Thread client_thread = new Thread(() -> {
				Client client;
				try {
					InetAddress localAddr = InetAddress.getLocalHost();
					client = new Client(localAddr, clientFS);
					StaValue v = client.start(main_expr);
					System.out.println("Result: " + v);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			});

			client_thread.start();

		} catch (ParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LexerException e) {
			e.printStackTrace();
		}
	}

	public static class SocketWas {
		private FunStore phi;
		private static HashMap<String, FunStore> programFSMap = new HashMap<>();

		public SocketWas() {

		}

		public void setServerFS(FunStore phi) {
			this.phi = phi;
		}

		public void start() {
			try (ServerSocket servSock = new ServerSocket(PORT)) {
				while (true) {
					Socket client = servSock.accept();
					Scanner input = new Scanner(client.getInputStream());
					PrintWriter output = new PrintWriter(client.getOutputStream(), true);

					Server server = new Server(phi, client, input, output);

					Thread th = new Thread(() -> {
						server.start();
					});

					th.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static class Server {
		private FunStore phi;

		private JSONParser jsonParser;

		private Socket client;
		private Scanner input;
		private PrintWriter output;

		public Server(FunStore phi) {
			this.phi = phi;

			jsonParser = new JSONParser();
		}

		public Server(FunStore phi, Socket client, Scanner input, PrintWriter output) {
			this.phi = phi;
			this.client = client;
			this.input = input;
			this.output = output;

			jsonParser = new JSONParser();
		}

		public void start() {
			handleClient();
		}

		private void handleClient() {
			try {
				// Repeat a service on each new outermost request
				while (true) {
					String line = input.nextLine();

					if (REQ.equals(line)) {
						String cloFnInStr = input.nextLine();
						JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
						StaValue cloFn = JSonUtil.fromJson(cloFnInJson);

						String numOfArgsInStr = input.nextLine();
						int numOfArgs = Integer.parseInt(numOfArgsInStr);
						ArrayList<StaValue> args = new ArrayList<>();

						for (int i = 0; i < numOfArgs; i++) {
							String argInStr = input.nextLine();
							JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
							StaValue arg = JSonUtil.fromJson(argInJson);

							args.add(arg);
						}

						String rStr = "r";
						Var rVar = new Var(rStr);

						StaTerm reqTerm = new Let(rStr, new App(cloFn, args), rVar);

						evalServer(reqTerm);

						// After evalServer is terminated,
						// it waits to receive a new request from clients.
					} else {
						// Must not reach here.
						System.err.println("No REQ. Unexpected " + line);
					}
				}

			} catch (ParseException e) {
				// jsonParser.parse()
			}
		}

		public void evalServer(StaTerm m) throws ParseException {

			// Repeat on each new nested request
			while (true) {
				System.out.println("SERVER: " + m);

				if (m instanceof Let) {
					Let mLet = (Let) m;
					StaTerm m1 = mLet.getM1();

					if (m1 instanceof App) {
						App mApp1 = (App) m1;

						if (mApp1.getF() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getF();

							ClosedFun closedFun = lookup(phi, fClo.getF());
							Let let = new Let(mLet.getY(),
									CSStaMain.substs(
											CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
											closedFun.getXs(), mApp1.getWs()),
									mLet.getM2());

							m = let;
						}
					} else if (m1 instanceof Call) {
						Call mCall1 = (Call) m1;

						if (mCall1.getF() instanceof Clo) {
							Clo fClo = (Clo) mCall1.getF();
							ArrayList<StaValue> args = mCall1.getWs();

							output.println(CALL);
							output.println(fClo.toJson());
							output.println(args.size());
							for (StaValue x : args) {
								output.println(x.toJson());
							}

							// Protocols:
							// - Wait to receive REQ or RET
							// - If it receives RET then continue to while-loop
							// with the current let with the returned value.
							// - Otherwise, v = evalServer(let r = f(ws) in r)
							// and then continue with the current let with v.

							while (true) {
								String line = input.nextLine();

								if (RET.equals(line)) {
									String retValInStr = input.nextLine();
									JSONObject retValInJson = (JSONObject) jsonParser.parse(retValInStr);
									StaValue retVal = JSonUtil.fromJson(retValInJson);

									m = new Let(mLet.getY(), retVal, mLet.getM2());
									break;
								} else if (REQ.equals(line)) {
									String cloFnInStr = input.nextLine();
									JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
									StaValue cloFn = JSonUtil.fromJson(cloFnInJson);

									String numOfArgsInStr = input.nextLine();
									int numOfArgs = Integer.parseInt(numOfArgsInStr);
									ArrayList<StaValue> cloFnArgs = new ArrayList<>();

									for (int i = 0; i < numOfArgs; i++) {
										String argInStr = input.nextLine();
										JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
										StaValue arg = JSonUtil.fromJson(argInJson);

										cloFnArgs.add(arg);
									}

									String rStr = "r";
									Var rVar = new Var(rStr);

									StaTerm reqTerm = new Let(rStr, new App(cloFn, args), rVar);

									evalServer(reqTerm);

									// After evalServer is terminated,
									// it waits to receive a new request from clients.
								} else {
									System.err.println("evalServer(Call) Must not reach here.");
								}
							}
						}
					} else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;

						StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(), mClo1);
						m = st;
					} else if (m1 instanceof Const) {
						Const mConst1 = (Const) m1;

						StaTerm st = CSStaMain.subst(mLet.getM2(), mLet.getY(), mConst1);
						m = st;
					} else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;

						Let let = new Let(mLet1.getY(), mLet1.getM1(),
								new Let(mLet.getY(), mLet1.getM2(), mLet.getM2()));
						m = let;
					}

				} else if (m instanceof Clo) {
					Clo mClo = (Clo) m;

					output.println(REPLY);
					output.println(mClo.toJson());

//					// Protocols:
//					//  - If the server context is empty then stop serverEval().
//					//  - Otherwise, wait to receive Req or Ret.

					return;

				} else if (m instanceof Const) {
					Const mConst = (Const) m;

					output.println(REPLY);
					output.println(mConst.toJson());

//					// Protocols:
//					//  - The same as above.

					return;
				}

			} // While
		}
	}

	public static class Client {
		private FunStore clientFS;

		private InetAddress serverAddress;
		private JSONParser jsonParser;

		public Client(InetAddress serverAddress, FunStore clientFS) {
			this.serverAddress = serverAddress;
			this.clientFS = clientFS;

			jsonParser = new JSONParser();
		}

		class ServerConn {
			private Socket server;
			private Scanner input;
			private PrintWriter output;

			ServerConn() {
				this.server = null;
				this.input = null;
				this.output = null;
			}

			Scanner input() {
				if (this.input == null)
					connect();
				return this.input;
			}

			PrintWriter output() {
				if (this.output == null)
					connect();
				return this.output;
			}

			private void connect() {
				try {
					server = new Socket(serverAddress, PORT);
					input = new Scanner(server.getInputStream());
					output = new PrintWriter(server.getOutputStream(), true);
				} catch (UnknownHostException e) {
					// InetAddress.getLocalHost()
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public StaValue start(StaTerm m) {
			System.out.println("The client begins ...");

			return evalClient(m, new ServerConn());
		}

		public StaValue evalClient(StaTerm m, ServerConn srvConn) {

			Function<Let, StaTerm> receiver = mLet -> {
				StaTerm retm;

				String line = srvConn.input().nextLine(); // Protocol: REPLY or CALL

				try {
					if (REPLY.equals(line)) {
						String replyValInStr = srvConn.input().nextLine(); // val in Json (REPLY val)
						JSONObject replyValInJson = (JSONObject) jsonParser.parse(replyValInStr);
						StaValue replyVal = JSonUtil.fromJson(replyValInJson);

						retm = new Let(mLet.getY(), replyVal, mLet.getM2());
					} else if (CALL.equals(line)) {
						String cloInStr = srvConn.input().nextLine(); // clo in Json (CALL clo n arg1 arg2 ... argn

						String nInStr = srvConn.input().nextLine();
						int n = Integer.parseInt(nInStr);

						ArrayList<String> argsInStr = new ArrayList<>();
						for (int i = 0; i < n; i++) {
							String argInStr = srvConn.input().nextLine();
							argsInStr.add(argInStr);
						}

						JSONObject cloInJson = (JSONObject) jsonParser.parse(cloInStr);
						StaValue clo = JSonUtil.fromJson(cloInJson);

						ArrayList<StaValue> args = new ArrayList<>();
						for (String argInStr : argsInStr) {
							JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
							StaValue arg = JSonUtil.fromJson(argInJson);
							args.add(arg);
						}

						retm = new Let(mLet.getY(), new App(clo, args), mLet.getM2());
					} else {
						// Must not reach here!!
						System.err.println("receiver: Neither REPLY or CALL: " + line);
						retm = null;
					}
				} catch (ParseException exn) {
					exn.printStackTrace();
					retm = null;
				}
				return retm;
			};

			while (true) {
				System.out.println("CLIENT: " + m);

				if (m instanceof Let) {
					Let mLet = (Let) m;
					StaTerm m1 = mLet.getM1();

					if (m1 instanceof App) {
						App mApp1 = (App) m1;

						if (mApp1.getF() instanceof Clo) {
							Clo fClo = (Clo) mApp1.getF();

							ClosedFun closedFun = lookup(clientFS, fClo.getF());

							m = new Let(mLet.getY(),
									CSStaMain.substs(
											CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
											closedFun.getXs(), mApp1.getWs()),
									mLet.getM2());
						}
					} else if (m1 instanceof Req) {
						Req mReq1 = (Req) m1;

						if (mReq1.getF() instanceof Clo) {
							Clo fClo = (Clo) mReq1.getF();
							ArrayList<StaValue> ws = mReq1.getWs();

							srvConn.output().println("REQ"); // Protocol: REQ
							srvConn.output().println(fClo.toJson()); // clo in Json
							srvConn.output().println(ws.size()); // n
							for (StaValue w : ws) {
								srvConn.output().println(w.toJson()); // arg1 ... argn
							}

							m = receiver.apply(mLet);
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

						StaValue retVal = mRet1.getW();
						srvConn.output().println("RET"); // Protocol: RET
						srvConn.output().println(retVal.toJson()); // val in Json

						m = receiver.apply(mLet);
					}
				} else if (m instanceof Clo || m instanceof Const) {
					return (StaValue) m;
				} else {
					System.err.println("TypedCSStaInThread.evalClient: Must not reach here");
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
}
