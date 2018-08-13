package com.example.rpc.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.example.rpc.App;
import com.example.rpc.Lam;
import com.example.rpc.Location;
import com.example.rpc.Term;
import com.rpc.parser.LexerException;
import com.rpc.parser.LexicalAnalyzer;
import com.rpc.parser.Parser;
import com.rpc.parser.ParserException;

public class ParseTest {

	@Test
	public void test1() throws ParserException, IOException, LexerException {
		StringReader ex = new StringReader("(lam^s f. (lam^s x. x) (f 1)) (lam^c y. (lam^s z. z) y)");
		LexicalAnalyzer lexer = new LexicalAnalyzer(ex);
		
		Parser parser = new Parser(lexer);
		Term result = parser.Parsing();
		
		assertTrue(result instanceof App
					&& ((App) result).getFun() instanceof Lam
					&& ((Lam) ((App) result).getFun()).getLoc() == Location.Server);
	}
	
	@Test
	public void test2() throws IOException, LexerException, ParserException {
		StringReader ex = new StringReader("(lam^c x.x) (lam^s x.x)");
		LexicalAnalyzer lexer = new LexicalAnalyzer(ex);
		
		Parser parser = new Parser(lexer);
		Term result = parser.Parsing();
		
		assertTrue(result instanceof App
					&& ((App) result).getFun() instanceof Lam
					&& ((App) result).getArg() instanceof Lam);
	}
	
	@Test
	public void test3() throws IOException, LexerException, ParserException {
		/*	(lam^s f.
		 * 		(lam^s x.x) (f 1)
		 * 	(lam^c y.
		 * 		(lam^s z.z) y)
		 */
		StringReader ex = new StringReader("(lam^s f.\n\t(lam^s x.x) (f 1))\n(lam^c y.\n\t(lam^s z.z) y)");
		LexicalAnalyzer lexer = new LexicalAnalyzer(ex);
		
		Parser parser = new Parser(lexer);
		Term result = parser.Parsing();
	}

}
