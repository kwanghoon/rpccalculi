package com.example.enccs;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSonUtil {
	public static EncValue fromJson(JSONObject json) {
		Object obj;

		obj = json.get(Const.Const);
		if (obj instanceof Long) {
			Long i = (Long) obj;
			return new Const(i.intValue());
		}

		obj = json.get(Clo.Clo);
		if (obj instanceof String) {
			String f = (String) obj;
			JSONArray jsonArr = (JSONArray) json.get(Clo.Fvs);
			ArrayList<EncValue> args = new ArrayList<>();
			for (int i = 0; i < jsonArr.size(); i++) {
				EncValue arg = JSonUtil.fromJson((JSONObject) jsonArr.get(i));
				args.add(arg);
			}
			return new Clo(f, args);
		}

		System.err.println("JSonUtil: fromJson: Neither Const or Clo\n" + json);

		return null;
	}
}
