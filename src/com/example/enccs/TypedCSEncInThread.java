package com.example.enccs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Function;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.encrpc.CompRPCEncTerm;
import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.rpc.Parser;
import com.example.rpc.Term;
import com.example.typedrpc.Infer;
import com.example.typedrpc.TypedTerm;
import com.example.utils.TripleTup;

public class TypedCSEncInThread {
	private static final String REQ = "REQ";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 8080;

	public static void main(String[] args) {
		try {
			Parser parser = new Parser();

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
			TypedTerm typedRpcProgram = Infer.infer(rpcProgram);

			com.example.encrpc.EncTerm encTerm = CompRPCEncTerm.compEncTerm(typedRpcProgram);

			TripleTup<com.example.enccs.EncTerm, com.example.enccs.FunStore, com.example.enccs.FunStore> csEncTerm = CompCSEncTerm
					.compCSEncTerm(encTerm);

			EncTerm mainExpr = csEncTerm.getFirst();
			FunStore clientFS = csEncTerm.getSecond();
			FunStore serverFS = csEncTerm.getThird();

			Thread serverThread = new Thread(() -> {
				Server server = new Server(serverFS);
				server.start();
			});

			Thread clientThread = new Thread(() -> {
				InetAddress localAddr;
				try {
					localAddr = InetAddress.getLocalHost();
					Client client = new Client(localAddr, clientFS);
					EncValue result = client.start(mainExpr);
					System.out.println("Result: " + result);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			});

			serverThread.start();
			clientThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LexerException e) {
			e.printStackTrace();
		} catch (ParserException e) {
			e.printStackTrace();
		}

	}

	public static class Server {
		private FunStore phi;

		private JSONParser jsonParser;
		private ServerSocket serverSock;
		private BufferedReader input;
		private PrintWriter output;

		public Server(FunStore phi) {
			this.phi = phi;

			jsonParser = new JSONParser();
		}

		public void start() {
			try {
				serverSock = new ServerSocket(PORT);

				// WAS와 비슷한 역할을 하는 부분
				while (true) {
					handleClient();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void handleClient() {
			Socket client;

			try {
				client = serverSock.accept();
				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				output = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);

				String protocol = input.readLine();

				if (REQ.equals(protocol)) {
					String cloFnInStr = input.readLine();
					JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
					EncValue cloFn = JSonUtil.fromJson(cloFnInJson);

					String numOfArgsInStr = input.readLine();
					int numOfArgs = Integer.parseInt(numOfArgsInStr);
					ArrayList<EncValue> args = new ArrayList<>();

					for (int i = 0; i < numOfArgs; i++) {
						String argInStr = input.readLine();
						JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
						EncValue arg = JSonUtil.fromJson(argInJson);

						args.add(arg);
					}

					EncTerm reqTerm = new App(cloFn, args);

					evalServer(reqTerm);
				} else {
					System.err.println("Not expected: " + protocol);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public void evalServer(EncTerm m) {
			while (true) {
				System.out.println("SERVER: " + m);

				if (m instanceof App) {
					App mApp = (App) m;

					if (mApp.getFun() instanceof Clo) {
						Clo funClo = (Clo) mApp.getFun();
						ClosedFun closedFun = lookup(phi, funClo.getF());

						m = CSEncMain.substs(CSEncMain.substs(closedFun.getM(), closedFun.getZs(), funClo.getVs()),
								closedFun.getXs(), mApp.getArgs());
					}
				} else if (m instanceof Call) {
					Call mCall = (Call) m;

					if (mCall.getCall() instanceof Clo) {
						Clo callClo = (Clo) mCall.getCall();
						ArrayList<EncValue> args = mCall.getArgs();

						output.println(CALL);
						output.println(callClo.toJson());
						output.println(args.size());
						for (EncValue v : args) {
							output.println(v.toJson());
						}
					}

					return;
				} else if (m instanceof Clo) {
					Clo mClo = (Clo) m;

					output.println(REPLY);
					output.println(mClo.toJson());

					return;
				} else if (m instanceof Const) {
					Const mConst = (Const) m;

					output.println(REPLY);
					output.println(mConst.toJson());

					return;
				}
			}
		}
	}

	public static class Client {
		private FunStore clientFS;

		private InetAddress serverAddr;
		private JSONParser jsonParser;

		public Client(InetAddress serverAddr, FunStore clientFS) {
			this.serverAddr = serverAddr;
			this.clientFS = clientFS;

			jsonParser = new JSONParser();
		}

		class ServerConn {
			private Socket server;
			private BufferedReader input;
			private PrintWriter output;

			ServerConn() {
				this.server = null;
				this.input = null;
				this.output = null;
			}

			BufferedReader input() {
				if (server == null || server.isClosed())
					connect();
				return this.input;
			}

			PrintWriter output() {
				if (server == null || server.isClosed())
					connect();
				return this.output;
			}

			private void connect() {
				try {
					server = new Socket(serverAddr, PORT);
					input = new BufferedReader(new InputStreamReader(server.getInputStream()));
					output = new PrintWriter(new OutputStreamWriter(server.getOutputStream()), true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public void disconnect() {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public EncValue start(EncTerm m) {
			return evalClient(m, new ServerConn());
		}

		public EncValue evalClient(EncTerm m, ServerConn conn) {
			Function<Let, EncTerm> receiver = mLet -> {
				EncTerm retm;

				try {
					String protocol = conn.input().readLine(); // Protocol: REPLY or CALL

					if (REPLY.equals(protocol)) {
						String replyValInStr = conn.input().readLine(); // val in Json (REPLY val)
						JSONObject replyValInJson = (JSONObject) jsonParser.parse(replyValInStr);
						EncValue replyVal = JSonUtil.fromJson(replyValInJson);

						retm = new Let(mLet.getVal(), replyVal, mLet.getM2());
					} else if (CALL.equals(protocol)) {
						String cloInStr = conn.input().readLine(); // clo in Json (CALL clo n arg1 arg2 ... argn

						String nInStr = conn.input().readLine();
						int n = Integer.parseInt(nInStr);

						ArrayList<String> argsInStr = new ArrayList<>();
						for (int i = 0; i < n; i++) {
							String argInStr = conn.input().readLine();
							argsInStr.add(argInStr);
						}

						JSONObject cloInJson = (JSONObject) jsonParser.parse(cloInStr);
						EncValue clo = JSonUtil.fromJson(cloInJson);

						ArrayList<EncValue> args = new ArrayList<>();
						for (String argInStr : argsInStr) {
							JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
							EncValue arg = JSonUtil.fromJson(argInJson);
							args.add(arg);
						}

						retm = new Let(mLet.getVal(), new App(clo, args), mLet.getM2());
					} else {
						System.err.println("receiver: Neither REPLY or CALL: " + protocol);
						retm = null;
					}
				} catch (ParseException exn) {
					exn.printStackTrace();
					retm = null;
				} catch (IOException e) {
					e.printStackTrace();
					retm = null;
				}

				conn.disconnect();

				return retm;
			};

			while (true) {
				System.out.println("CLIENT: " + m);
				if (m instanceof Let) {
					Let mLet = (Let) m;
					EncTerm m1 = mLet.getM1();

					if (m1 instanceof App) {
						App mApp1 = (App) m1;

						if (mApp1.getFun() instanceof Clo) {
							Clo funClo = (Clo) mApp1.getFun();
							ClosedFun closedFun = lookup(clientFS, funClo.getF());

							m = new Let(mLet.getVal(),
									CSEncMain.substs(
											CSEncMain.substs(closedFun.getM(), closedFun.getZs(), funClo.getVs()),
											closedFun.getXs(), mApp1.getArgs()),
									mLet.getM2());
						}
					} else if (m1 instanceof Req) {
						Req mReq1 = (Req) m1;

						if (mReq1.getReq() instanceof Clo) {
							Clo reqClo = (Clo) mReq1.getReq();
							ArrayList<EncValue> args = mReq1.getArgs();

							conn.output().println(REQ);
							conn.output().println(reqClo.toJson());
							conn.output().println(args.size());

							for (EncValue v : args) {
								conn.output().println(v.toJson());
							}

							m = receiver.apply(mLet);
						}
					} else if (m1 instanceof Let) {
						Let mLet1 = (Let) m1;

						m = new Let(mLet1.getVal(), mLet1.getM1(), new Let(mLet.getVal(), mLet1.getM2(), mLet.getM2()));
					} else if (m1 instanceof Clo) {
						Clo mClo1 = (Clo) m1;

						m = CSEncMain.subst(mLet.getM2(), mLet.getVal(), mClo1);
					} else if (m1 instanceof Const) {
						Const mConst1 = (Const) m1;

						m = CSEncMain.subst(mLet.getM2(), mLet.getVal(), mConst1);
					}
				} else if (m instanceof Clo || m instanceof Const) {
					return (EncValue) m;
				}
			}
		}
	}

	public static ClosedFun lookup(FunStore fs, String f) {
		for (String p : fs.getFs().keySet()) {
			if (p.equals(f))
				return fs.getFs().get(p);
		}
		return null;
	}
}
