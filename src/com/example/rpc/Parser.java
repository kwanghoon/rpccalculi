package com.example.rpc;

import java.io.IOException;
import java.io.Reader;

import com.example.lib.CommonParserUtil;
import com.example.lib.LexerException;
import com.example.lib.ParserException;

public class Parser {
	private CommonParserUtil pu;
	
	public Parser() throws IOException, LexerException {
		pu = new CommonParserUtil();
//		pu = new CommonParserUtil("grammar_rules.txt", "action_table.txt", "goto_table.txt");

		pu.lex("[ \t\n]", text -> { return null; });
		pu.lex("[0-9]+", text -> { return Token.NUM; });
		pu.lex("[a-zA-Z]+[0-9]*", text -> {
			if (text.equalsIgnoreCase("lam"))
					return Token.LAM;
			else
					return Token.ID; });
		pu.lex("\\^[cs]", text -> { return Token.LOC; });
		pu.lex("\\(", text -> { return Token.OPENPAREN; });
		pu.lex("\\)", text -> { return Token.CLOSEPAREN; });
		pu.lex("\\.", text -> { return Token.DOT; });
		pu.lexEndToken("$", Token.END_OF_TOKEN);
		
		pu.ruleStartSymbol("L'");
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
	
	public Term Parsing(Reader r) throws ParserException, IOException, LexerException {
		return (Term) pu.Parsing(r);
	}
	
	private Location getLoc(String loc) {
		if (loc.equals("^s"))
			return Location.Server;
		else
			return Location.Client;
	}
}
