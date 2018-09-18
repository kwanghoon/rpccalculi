package com.example.stacs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HttpServer {
	private static final String OPEN_SESSION = "OPEN_SESSION";
	private static final String CLOSE_SESSION = "CLOSE_SESSION";

	private static final String REQ = "REQ";
	private static final String RET = "RET";
	private static final String REPLY = "REPLY";
	private static final String CALL = "CALL";

	private static final int PORT = 8080;

	private static HashMap<String, FunStore> programFSMap = new HashMap<>();
	private static HashMap<Integer, CSServer> sessionMap = new HashMap<>();
	private static int count = 0;

	public String protocol;

	public HttpServer() {
		
	}
	
	public HttpServer(String programName, FunStore phi) {
		setServerFS(programName, phi);
	}
	
	public void setServerFS(String programName, FunStore phi) {
		if (!programFSMap.keySet().contains(programName)) {
			programFSMap.put(programName, phi);
		}
	}

	public void start() {
		try (ServerSocket srvSocket = new ServerSocket(PORT)) {
			while (true) {
				Socket conn = srvSocket.accept();
				InputStream input = conn.getInputStream();
				OutputStream output = conn.getOutputStream();

				// GET /rpc/programName HTTP/1.1\r\n
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
				String request = reader.readLine();

				// method와 version 사이에 있는 url(프로그램명)을 추출하기 위해 필요
				int idxMethod = request.indexOf(" ");
				int idxVersion = request.indexOf(" ", idxMethod + 1);
				String url = request.substring(idxMethod + 1, idxVersion);

				String urlProgramName = url.substring(5, url.length());

				if (programFSMap.keySet().contains(urlProgramName)) {
					// program에 대한 funstore가 정상적으로 등록되어 있는 경우
					// entity body까지 읽어들임
					String line;
					while (!(line = reader.readLine()).equals(""))
						;

					String sessionState = reader.readLine(); // sessionState -> OPEN_SESSION, sessionNum
					protocol = reader.readLine(); // protocol -> REQ, RET

					CSServer server;

					int session;

					if (sessionState.equals(OPEN_SESSION)) {
						session = newSession();

						FunStore phi = programFSMap.get(urlProgramName);
						server = new CSServer(phi, session, conn, reader, writer);

						sessionMap.put(count, server);

						Thread th = new Thread(() -> {
							server.run();
						});
						th.start();
					} else {
						session = Integer.parseInt(sessionState);

						server = sessionMap.get(session);

						server.connectClient(conn, reader, writer);

						synchronized (server.getLock()) {
							server.getLock().notify();
						}
					}
				} else {
					// program에 대한 funstore가 등록되지 않은 경우
					System.err.println("program funstore not found");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
	}

	private int newSession() {
		count = count + 1;

		return count;
	}

	class CSServer {
		private FunStore phi;
		private int sessionNum;

		private JSONParser jsonParser;

		private Socket conn;
		private BufferedReader reader;
		private BufferedWriter writer;

		private String lock;

		CSServer(FunStore phi, int sessionNum, Socket conn, BufferedReader reader, BufferedWriter writer) throws IOException {
			this.phi = phi;
			this.sessionNum = sessionNum;

			jsonParser = new JSONParser();

			this.conn = conn;
			this.reader = reader;
			this.writer = writer;

			lock = "";
		}

		public String getLock() {
			return lock;
		}

		public void run() {
			handleClient();
		}

		public void connectClient(Socket socket, BufferedReader reader, BufferedWriter writer) {
			conn = socket;

			this.reader = reader;
			this.writer = writer;
		}

		private void handleClient() {
			try {
				if (protocol.equals(REQ)) {
					String cloFnInStr = reader.readLine();
					JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
					StaValue cloFn = JSonUtil.fromJson(cloFnInJson);

					String numOfArgsInStr = reader.readLine();
					int numOfArgs = Integer.parseInt(numOfArgsInStr);
					ArrayList<StaValue> args = new ArrayList<>();

					for (int i = 0; i < numOfArgs; i++) {
						String argInStr = reader.readLine();
						JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
						StaValue arg = JSonUtil.fromJson(argInJson);

						args.add(arg);
					}

					String rStr = "r";
					Var rVar = new Var(rStr);

					StaTerm reqTerm = new Let(rStr, new App(cloFn, args), rVar);

					evalServer(reqTerm, 0);
					
					reader.close();
					writer.close();
					
					sessionMap.remove(sessionNum);
					
				} else {
					System.err.println("Unexpected protocol(" + protocol + ")");
					writeHeader(400, "Bad Request");
					writer.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public void writeHeader(int code, String message) {
			try {
				writer.write("HTTP/1.1 " + code + " " + message + "\r\n");
				writer.write("Date: " + new Date() + "\r\n");
				writer.write("Server: " + "Apache 2.0\r\n\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void evalServer(StaTerm m, int stackDepth) throws ParseException {
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
							try {
								Clo fClo = (Clo) mCall1.getF();
								ArrayList<StaValue> args = mCall1.getWs();
	
								// lock을 wait하기 전에 client에서 요청을 보내 wait하기 전에 notify를 할 수 있기 때문에
								// 하나의 연결되는 동작처럼 보내주고 wait을 해줘야됨
								synchronized(lock) { 
									// Client로 Call을 날리는 부분
									writeHeader(200, "OK");
									writer.write(sessionNum + "\n");
									writer.write(CALL + "\n");
									writer.write(fClo.toJson() + "\n");
									writer.write(args.size() + "\n");
									for (StaValue arg : args) {
										writer.write(arg.toJson() + "\n");
									}
									writer.flush();
	
									// object wait 시키기
									lock.wait();
								}

								while (true) {
									if (protocol.equals(RET)) {
										String retValInStr = reader.readLine();
										JSONObject retValInJson = (JSONObject) jsonParser.parse(retValInStr);
										StaValue retVal = JSonUtil.fromJson(retValInJson);

										m = new Let(mLet.getY(), retVal, mLet.getM2());

										break;
									} else if (protocol.equals(REQ)) {
										String cloFnInStr = reader.readLine();
										JSONObject cloFnInJson = (JSONObject) jsonParser.parse(cloFnInStr);
										StaValue cloFn = JSonUtil.fromJson(cloFnInJson);

										String numOfArgsInStr = reader.readLine();
										int numOfArgs = Integer.parseInt(numOfArgsInStr);
										ArrayList<StaValue> cloFnArgs = new ArrayList<>();

										for (int i = 0; i < numOfArgs; i++) {
											String argInStr = reader.readLine();
											JSONObject argInJson = (JSONObject) jsonParser.parse(argInStr);
											StaValue arg = JSonUtil.fromJson(argInJson);

											cloFnArgs.add(arg);
										}

										String rStr = "r";
										Var rVar = new Var(rStr);

										StaTerm reqTerm = new Let(rStr, new App(cloFn, args), rVar);

										evalServer(reqTerm, stackDepth + 1);
									} else {
										System.err.println("evalServer(Call) Must not reach here. " + protocol);
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
								writeHeader(500, "Internal Server Error");
							} catch (InterruptedException e) {
								e.printStackTrace();
								writeHeader(500, "Internal Server Error");
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

					try {
						// REPLY
						synchronized(lock) {
							writeHeader(200, "OK");
	
							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");
	
							writer.write(REPLY + "\n");
							writer.write(mClo.toJson() + "\n");
							writer.flush();
	
							if(stackDepth > 0)
								lock.wait();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					return;
				} else if (m instanceof Const) {
					Const mConst = (Const) m;

					try {
						// REPLY
						synchronized(lock) {
							writeHeader(200, "OK");
	
							if (stackDepth == 0)
								writer.write(CLOSE_SESSION + "\n");
							else
								writer.write(sessionNum + "\n");
	
							writer.write(REPLY + "\n");
							writer.write(mConst.toJson() + "\n");
							writer.flush();
	
							if(stackDepth > 0 ) 
								lock.wait();
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

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

}
