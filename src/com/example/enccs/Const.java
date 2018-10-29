package com.example.enccs;

import org.json.simple.JSONObject;

public class Const extends EncValue {
	private int i;

	public Const(int i) {
		super();
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	@Override
	public String toString() {
		return i + "";
	}

	public static final String Const = "Const";

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(Const, new Long(i));
		return jsonObject;
	}
}
