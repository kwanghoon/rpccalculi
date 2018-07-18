package com.example.enccs;

public class ClientContext {
	private Ctx ctx;
	
	public ClientContext(Ctx ctx) {
		this.ctx = ctx;
	}

	public Ctx getCtx() {
		return ctx;
	}

	public void setCtx(Ctx ctx) {
		this.ctx = ctx;
	}

}
