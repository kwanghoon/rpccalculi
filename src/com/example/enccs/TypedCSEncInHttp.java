package com.example.enccs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

public class TypedCSEncInHttp {
	private static final String OPEN_SESSION = "OPEN_SESSION";
	private static final String CLOSE_SESSION = "CLOSE_SESSION";

	private static final String REQ = "REQ";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 8080;

	public static void main(String[] args) {
		Parser parser;
		String serverAddr = "127.0.0.1";
		HttpWas was = new HttpWas();

		Thread wasThread = new Thread(() -> {
			was.start();
		});

		wasThread.start();

		while (true) {
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
				com.example.encrpc.EncTerm rpcEncProgram = CompRPCEncTerm.compEncTerm(typedRPCProgram);

				TripleTup<com.example.enccs.EncTerm, com.example.enccs.FunStore, com.example.enccs.FunStore> csEncTerm = CompCSEncTerm
						.compCSEncTerm(rpcEncProgram);

				EncTerm mainExpr = csEncTerm.getFirst();
				FunStore clientFS = csEncTerm.getSecond();
				FunStore serverFS = csEncTerm.getThird();

				String programName = fileName.substring(0, fileName.indexOf("."));

				was.setServerFS(programName, serverFS);

				Thread clientThread = new Thread(() -> {
					Client client = new Client(serverAddr, programName, clientFS);
					EncValue result = client.evalClient(mainExpr);

					System.out.println("Result: " + result);
				});

				clientThread.start();
				clientThread.join();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LexerException e) {
				e.printStackTrace();
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static class HttpWas {
		private static HashMap<String, FunStore> programFSMap = new HashMap<>();
		private static HashMap<Integer, Server> sessionMap = new HashMap<>();
		private static int session = 0;

		private String protocol;

		public HttpWas() {

		}

		public void setServerFS(String programName, FunStore phi) {
			if (!programFSMap.keySet().contains(programName))
				programFSMap.put(programName, phi);
		}

		public void start() {
			try (ServerSocket srvSocket = new ServerSocket(PORT)) {
				while (true) {
					Socket client = srvSocket.accept();
					BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
					PrintWriter writer = new PrintWriter(client.getOutputStream(), true);

					String header = reader.readLine();

					int idxMethod = header.indexOf(" ");
					int idxVersion = header.indexOf(" ", idxMethod + 1);
					String url = header.substring(idxMethod + 1, idxVersion);

					// url은 /rpc/(programName)으로 정해짐
					String urlProgramName = url.substring(5, url.length());

					if (programFSMap.keySet().contains(urlProgramName)) {
						String line;
						while (!(line = reader.readLine()).equals(""))
							;

						String sessionState = reader.readLine();
						protocol = reader.readLine();

						Server server;

						if (sessionState.equals(OPEN_SESSION)) {
							session = session + 1;

							FunStore phi = programFSMap.get(urlProgramName);
							server = new Server(phi, session, client, reader, writer);

							sessionMap.put(session, server);

							Thread th = new Thread(() -> {
								server.start();
							});

							th.start();
						} else {
							session = Integer.parseInt(sessionState);
							server = sessionMap.get(session);
							server.connectClient(client, reader, writer);

							server.handleClient();
						}
					} else {
						System.err.println("Program Funstore not found");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		class Server {
			private FunStore phi;
			private int session;

			private JSONParser jsonParser;

			private Socket client;
			private BufferedReader reader;
			private PrintWriter writer;

			private String lock;

			Server(FunStore phi, int session, Socket client, BufferedReader reader, PrintWriter writer) {
				this.phi = phi;
				this.session = session;
				this.client = client;
				this.reader = reader;
				this.writer = writer;

				jsonParser = new JSONParser();

				lock = "";
			}

			public String getLock() {
				return lock;
			}

			public void start() {
				handleClient();
			}

			public void connectClient(Socket client, BufferedReader reader, PrintWriter writer) {
				this.client = client;
				this.reader = reader;
				this.writer = writer;
			}

			private void handleClient() {
				try {
					if (protocol.equals(REQ)) {
						String cloFnInStr = reader.readLine();
						JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
						EncValue cloFn = JSonUtil.fromJson(cloFnInJson);

						String numOfArgsInStr = reader.readLine();
						int numOfArgs = Integer.parseInt(numOfArgsInStr);
						ArrayList<EncValue> args = new ArrayList<>();

						for (int i = 0; i < numOfArgs; i++) {
							String argInStr = reader.readLine();
							JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
							EncValue arg = JSonUtil.fromJson(argInJson);

							args.add(arg);
						}

						EncTerm reqTerm = new App(cloFn, args);

						evalServer(reqTerm);
						
						reader.close();
						writer.close();
						
						sessionMap.remove(session);
					} else {
						System.err.println("Not expected: " + protocol);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			private void writeHeader(int code, String message) {
				writer.print("HTTP/1.1 " + code + " " + message + "\r\n");
				writer.print("Date: " + new Date() + "\r\n");
				writer.print("Server: " + "Apache 2.0\r\n\r\n");
			}

			private void evalServer(EncTerm m) {
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

							writeHeader(200, "OK");
							writer.println(CLOSE_SESSION);
							writer.println(CALL);
							writer.println(callClo.toJson());
							writer.println(args.size());
							for (EncValue v : args) {
								writer.println(v.toJson());
							}
						}

						return;
					} else if (m instanceof Clo) {
						Clo mClo = (Clo) m;

						writeHeader(200, "OK");
						writer.println(CLOSE_SESSION);
						writer.println(REPLY);
						writer.println(mClo.toJson());

						return;
					} else if (m instanceof Const) {
						Const mConst = (Const) m;

						writeHeader(200, "OK");
						writer.println(CLOSE_SESSION);
						writer.println(REPLY);
						writer.println(mConst.toJson());

						return;
					}
				}
			}
		}

	}

	public static class Client {
		private FunStore clientFS;
		private String programName;
		private String serverAddr;

		private Socket server;
		private BufferedReader reader;
		private PrintWriter writer;

		private JSONParser jsonParser;
		private Integer session;

		public Client(String serverAddr, String programName, FunStore clientFS) {
			this.serverAddr = serverAddr;
			this.programName = programName;
			this.clientFS = clientFS;
			
			jsonParser = new JSONParser();
		}

		private void connectServer() {
			if (server == null || server.isClosed()) {
				try {
					server = new Socket(serverAddr, PORT);

					reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
					writer = new PrintWriter(server.getOutputStream(), true);

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void writeHeader() {
			connectServer();
			writer.print("GET " + "/rpc/" + programName + " HTTP/1.1\r\n");
			writer.print("Host: " + server.getInetAddress().getHostAddress() + "\r\n");
			writer.print("\r\n");
		}

		private void disconnect() {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public EncValue evalClient(EncTerm m) {
			Function<Let, EncTerm> receiver = mLet -> {
				EncTerm retm;

				try {
					String statusLine = reader.readLine();

					int idxVersion = statusLine.indexOf(" ");
					int idxPhrase = statusLine.indexOf(" ", idxVersion + 1);

					int statusCode = Integer.parseInt(statusLine.substring(idxVersion + 1, idxPhrase));

					if (statusCode == 200) {
						String line;
						while(!(line = reader.readLine()).equals(""));
						
						String sessionState = reader.readLine();
						String protocol = reader.readLine(); // Protocol: REPLY or CALL
						
						if (sessionState.equals(CLOSE_SESSION))
							session = null;
						else
							session = Integer.parseInt(sessionState);

						if (REPLY.equals(protocol)) {
							String replyValInStr = reader.readLine(); // val in Json (REPLY val)
							JSONObject replyValInJson = (JSONObject) jsonParser.parse(replyValInStr);
							EncValue replyVal = JSonUtil.fromJson(replyValInJson);

							retm = new Let(mLet.getVal(), replyVal, mLet.getM2());
						} else if (CALL.equals(protocol)) {
							String cloInStr = reader.readLine();
							JSONObject cloInJson = (JSONObject) jsonParser.parse(cloInStr);
							EncValue clo = JSonUtil.fromJson(cloInJson);

							int n = Integer.parseInt(reader.readLine());

							ArrayList<EncValue> args = new ArrayList<>();
							for (int i = 0; i < n; i++) {
								String strArg = reader.readLine();
								JSONObject argInJson = (JSONObject) jsonParser.parse(strArg);
								EncValue arg = JSonUtil.fromJson(argInJson);
								
								args.add(arg);
							}

							retm = new Let(mLet.getVal(), new App(clo, args), mLet.getM2());
						} else {
							System.err.println("receiver: Neither REPLY or CALL: " + protocol);
							retm = null;
						}
					}
					else {
						System.err.println(statusCode);
						retm = null;
					}
				} catch (ParseException exn) {
					exn.printStackTrace();
					retm = null;
				} catch (IOException e) {
					e.printStackTrace();
					retm = null;
				}

				disconnect();

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

							writeHeader();
							if (session == null)
								writer.println(OPEN_SESSION);
							else
								writer.println(session);
							writer.println(REQ);
							writer.println(reqClo.toJson());
							writer.println(args.size());

							for (EncValue v : args) {
								writer.println(v.toJson());
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
