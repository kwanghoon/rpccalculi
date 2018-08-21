package com.example.rpc.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class MatcherTest {

	@Test
	public void test() {
		Pattern p = Pattern.compile("\\^[cs]");
		Matcher m1 = p.matcher("^c x.x");
		Matcher m2 = p.matcher("^");
		
		System.out.println(m1.matches());
		System.out.println(m2.matches());
		
		System.out.println(m1.find());
		System.out.println(m1.lookingAt() + ", " + m1.start() + ", " + m1.end());
		
		Pattern p2 = Pattern.compile("[a-zA-Z]+");
		Matcher m3 = p2.matcher("^c x.x");
		System.out.println(m3.lookingAt() +", " +m3.region(3, 5).lookingAt());
	}

}
