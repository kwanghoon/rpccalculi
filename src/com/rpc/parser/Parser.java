package com.rpc.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.example.rpc.App;
import com.example.rpc.Const;
import com.example.rpc.Lam;
import com.example.rpc.Location;
import com.example.rpc.Term;
import com.example.rpc.Var;

public class Parser {
	private ArrayList<Terminal> lexer;
	private CommonParserUtil pu;
	
	public Parser(LexicalAnalyzer lexicalAnalyzer) throws IOException, LexerException {
		lexer = lexicalAnalyzer.Lexing();
		
		pu = new CommonParserUtil(lexer);
		
		pu.rule("L' -> L", () -> { return pu.get(1); });
		pu.rule("L -> E", () -> { return pu.get(1); });
		pu.rule("L -> lam loc id . L", () -> {
			Object tree = pu.get(5);
			return new Lam(getLoc(pu.getText(2)), pu.getText(3), (Term) tree); });
		pu.rule("E -> E T", () -> { return new App((Term) pu.get(1), (Term) pu.get(2)); });
		pu.rule("E -> T", () -> { return pu.get(1); });
		pu.rule("T -> id", () -> { return new Var(pu.getText(1)); });
		pu.rule("T -> num", () -> { return new Const(Integer.parseInt(pu.getText(1))); });
		pu.rule("T -> ( L )", () -> { return pu.get(2); });
	}
	
	public Term Parsing() throws FileNotFoundException, ParserException {
		return (Term) pu.Parsing();
	}
	
	private Location getLoc(String loc) {
		if (loc.equals("s"))
			return Location.Server;
		else
			return Location.Client;
	}
}
