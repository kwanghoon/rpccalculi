package com.example.rpc;

import com.example.lib.ParserException;

public enum Token {
	END_OF_TOKEN("$"),
	OPENPAREN("("), CLOSEPAREN(")"),
	DOT("."), LOC("loc"),
	LAM("lam"), ID("id"), NUM("num");
	
	private String strToken;
	
	Token (String strToken) {
		this.strToken = strToken;
	}
	public String getStrToken() {
		return strToken;
	}
	
	public static Token findToken(String strToken) throws ParserException {
		for (Token t: Token.values()) {
			if (t.getStrToken().equals(strToken))
				return t;
		}
		throw new ParserException(strToken + " not expected.");
	}
}
