package com.example.rpc.test;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.example.stacs.Clo;
import com.example.stacs.Const;
import com.example.stacs.StaValue;

public class StaValueToJSonTest {

	@Test
	public void testConst() {
		Const c = new Const(123);
		JSONObject jsonObject = c.toJson();
		System.out.println(jsonObject.toJSONString());
		assert("{\"Const\":123}".equals(jsonObject.toJSONString()));
	}
	
	@Test
	public void testCloWithNoFreeVariable() {
		Clo clo = new Clo("funName", new ArrayList<StaValue>());
		JSONObject jsonObject = clo.toJson();
		System.out.println(jsonObject.toJSONString());
		assert("{\"Fvs\":[],\"Clo\":\"funName\"}".equals(jsonObject.toJSONString()));
	}
	
	@Test
	public void testClo() {
		ArrayList<StaValue> vals = new ArrayList<StaValue>();
		vals.add(new Const(123));
		vals.add(new Const(456));
		Clo clo = new Clo("myfun", vals);
		
		JSONObject jsonObject = clo.toJson();
		System.out.println(jsonObject.toJSONString());
		assert("{\"Fvs\":[{\"Const\":123},{\"Const\":456}],\"Clo\":\"myfun\"}".equals(jsonObject.toJSONString()));
	}

	@Test
	public void testCloInClo() {
		ArrayList<StaValue> yourVals = new ArrayList<StaValue>();
		yourVals.add(new Const(789));
		Clo yourClo = new Clo("yourfun", yourVals);
		
		ArrayList<StaValue> vals = new ArrayList<StaValue>();
		vals.add(new Const(123));
		vals.add(new Const(456));
		vals.add(yourClo);
		Clo clo = new Clo("myfun", vals);
		
		JSONObject jsonObject = clo.toJson();
		System.out.println(jsonObject.toJSONString());
		assert("{\"Fvs\":[{\"Const\":123},{\"Const\":456},{\"Fvs\":[{\"Const\":789}],\"Clo\":\"yourfun\"}],\"Clo\":\"myfun\"}".equals(jsonObject.toJSONString()));	
	}
}
