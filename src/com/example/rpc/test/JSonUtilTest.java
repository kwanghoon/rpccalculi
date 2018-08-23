package com.example.rpc.test;

import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.example.stacs.Const;
import com.example.stacs.JSonUtil;

public class JSonUtilTest {

	@Test
	public void test() {
		Const c = new Const(1);
		JSONObject jsonObject = c.toJson();
		
		Const c_from_json =(Const) JSonUtil.fromJson(jsonObject);
		System.out.println(jsonObject);
		
		assertTrue(c.getI() == c_from_json.getI());
	}

}
