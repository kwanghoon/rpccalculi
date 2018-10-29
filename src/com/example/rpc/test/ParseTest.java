package com.example.rpc.test;

import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.example.lib.LexerException;
import com.example.lib.ParserException;
import com.example.rpc.App;
import com.example.rpc.Lam;
import com.example.rpc.Location;
import com.example.rpc.Parser;
import com.example.rpc.Term;

public class ParseTest {

	@Test
	public void test1() throws ParserException, IOException, LexerException {
		StringReader ex = new StringReader("(lam^s f. (lam^s x. x) (f 1)) (lam^c y. (lam^s z. z) y)");

		Parser parser = new Parser();
		Term result = parser.Parsing(ex);

		assertTrue(result instanceof App && ((App) result).getFun() instanceof Lam
				&& ((Lam) ((App) result).getFun()).getLoc() == Location.Server);
	}

	@Test
	public void test2() throws IOException, LexerException, ParserException {
		StringReader ex = new StringReader("(lam^c x.x) (lam^s x.x)");

		Parser parser = new Parser();
		Term result = parser.Parsing(ex);

		assertTrue(result instanceof App && ((App) result).getFun() instanceof Lam
				&& ((App) result).getArg() instanceof Lam);
	}

	@Test
	public void test3() throws IOException, LexerException, ParserException {
		/*
		 * (lam^s f. (lam^s x.x) (f 1) (lam^c y. (lam^s z.z) y)
		 */
		StringReader ex = new StringReader("(lam^s f.\n\t(lam^s x.x) (f 1))\n(lam^c y.\n\t(lam^s z.z) y)");

		Parser parser = new Parser();
		Term result = parser.Parsing(ex);
	}

	@Test
	public void test4() throws IOException, LexerException, ParserException {
		String[] files = { "MultiParen01.txt", "ServerClient01.txt", "ServerClient02.txt", "VariableName01.txt",
				"VariableName02.txt", "WhiteSpace01.txt" };

		for (int i = 0; i < files.length; i++) {
			String directory = System.getProperty("user.dir");
			FileReader ex = new FileReader(directory + "/testcase/" + files[i]);
			Parser parser = new Parser();
			Term result = parser.Parsing(ex);
		}
	}

}
