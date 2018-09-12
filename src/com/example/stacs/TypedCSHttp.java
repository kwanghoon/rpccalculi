package com.example.stacs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
import com.example.typedrpc.TypedTerm;
import com.example.utils.TripleTup;

public class TypedCSHttp {
	public static void main(String[] args) {
		Parser parser;
		String serverAddr = "127.0.0.1";

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

			String programName = fileName.substring(0, fileName.indexOf("."));

			Thread serverThread = new Thread(() -> {
				HttpServer server = new HttpServer(programName, serverFS);
				server.start();
			});
			Thread clientThread = new Thread(() -> {
				CSClient client = new CSClient(serverAddr, programName, clientFS);
				StaValue result = client.evalClient(mainExpr);

				System.out.println("result: " + result);
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

	public static class CSClient {
		private FunStore clientFS;
		private String programName;
		private String serverAddr;

		private Socket socket;
		private BufferedReader reader;
		private PrintWriter writer;

		private JSONParser jsonParser;

		private Integer sessionNum;

		public CSClient(String serverAddr, String programName, FunStore clientFS) {
			this.programName = programName;
			this.clientFS = clientFS;
			sessionNum = null;

			jsonParser = new JSONParser();

			try {
				this.serverAddr = serverAddr;
				socket = new Socket(serverAddr, 8080);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				writer = new PrintWriter(socket.getOutputStream(), true);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void connectServer() {
			if (socket == null) {
				try {
					socket = new Socket(serverAddr, 8080);

					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream(), true);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void writeHeader() {
			connectServer();
			writer.print("GET " + "/rpc/" + programName + " HTTP/1.1.\r\n");
			writer.print("Host: " + socket.getInetAddress().getHostAddress() + "\r\n");
			writer.print("\r\n");
		}

		public StaValue evalClient(StaTerm m) {

			Function<Let, StaTerm> receiver = mLet -> {
				StaTerm retM;
				try {
					// HTTP_VERSION STATUS_CODE PHRASE
					String statusLine = reader.readLine();

					// 잘 왔는지를 확인하기 위해서 필요!
					int idxVersion = statusLine.indexOf(" ");
					int idxPhrase = statusLine.indexOf(" ", idxVersion + 1);

					int statusCode = Integer.parseInt(statusLine.substring(idxVersion + 1, idxPhrase));

					if (statusCode == 200) {
						String line;
						// entity body 이전인 \r\n에 도달할 때까지 읽기
						while (!(line = reader.readLine()).equals(""))
							;

						String sessionState = reader.readLine(); // sessionState -> CLOSE_SESSION, sessionNum
						line = reader.readLine(); // protocol

						try {
							if (sessionState.equals("CLOSE_SESSION")) {
								sessionNum = null;
							} else {
								sessionNum = Integer.parseInt(sessionState);
							}

							if (line.equals("REPLY")) {
								String strReply = reader.readLine();
								JSONObject replyJson = (JSONObject) jsonParser.parse(strReply);
								StaValue replyVal = JSonUtil.fromJson(replyJson);

								retM = new Let(mLet.getY(), replyVal, mLet.getM2());
							} else if (line.equals("CALL")) {
								String strClo = reader.readLine();
								JSONObject cloJson = (JSONObject) jsonParser.parse(strClo);
								StaValue clo = JSonUtil.fromJson(cloJson);

								int n = Integer.parseInt(reader.readLine());

								ArrayList<StaValue> args = new ArrayList<>();
								for (int i = 0; i < n; i++) {
									String strArg = reader.readLine();
									JSONObject argJson = (JSONObject) jsonParser.parse(strArg);
									StaValue arg = JSonUtil.fromJson(argJson);

									args.add(arg);
								}

								retM = new Let(mLet.getY(), new App(clo, args), mLet.getM2());
							} else {
								System.err.println("receiver: Unexpected protocol(" + line + ")");
								retM = null;
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
							retM = null;
						} catch (ParseException e) {
							e.printStackTrace();
							retM = null;
						}
					} else {
						System.err.println(statusCode);
						retM = null;
					}

				} catch (IOException e) {
					e.printStackTrace();
					retM = null;
				}

				try {
					socket.close();
					socket = null;
				} catch (IOException e) {
					e.printStackTrace();
				}

				return retM;
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

							m = CSStaMain.substs(CSStaMain.substs(closedFun.getM(), closedFun.getZs(), fClo.getVs()),
									closedFun.getXs(), mApp1.getWs());
						}
					} else if (m1 instanceof Req) {
						Req mReq1 = (Req) m1;

						if (mReq1.getF() instanceof Clo) {
							Clo fClo = (Clo) mReq1.getF();
							ArrayList<StaValue> ws = mReq1.getWs();

							writeHeader();
							if (sessionNum != null)
								writer.println(sessionNum);
							else
								writer.println("OPEN_SESSION");
							writer.println("REQ");
							writer.println(fClo.toJson());
							writer.println(ws.size());
							for (StaValue w : ws) {
								writer.println(w.toJson());
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

						writeHeader();
						writer.println(sessionNum); // RET의 경우 sessionNum가 null인 상태는 있을 수가 없음
						writer.println("RET");
						writer.println(retVal.toJson());

						m = receiver.apply(mLet);
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
