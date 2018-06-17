package com.example.encrpc;

public class ClientContext {
	private Ctx ctx;
	private String str;
	private EncTerm m;

	public ClientContext(Ctx ctx, String str, EncTerm m) {
		super();
		this.ctx = ctx;
		this.str = str;
		this.m = m;
	}

	public Ctx getCtx() {
		return ctx;
	}

	public void setCtx(Ctx ctx) {
		this.ctx = ctx;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public EncTerm getM() {
		return m;
	}

	public void setM(EncTerm m) {
		this.m = m;
	}

}
