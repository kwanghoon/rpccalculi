package com.example.starpc;

import java.util.Stack;

public class ServerContext {
	private Stack<Context> serverContext;

	
	public ServerContext() {
		serverContext = new Stack<>();
	}

	public ServerContext(Stack<Context> serverContext) {
		super();
		this.serverContext = serverContext;
	}

	public Stack<Context> getServerContext() {
		return serverContext;
	}

	public void setServerContext(Stack<Context> serverContext) {
		this.serverContext = serverContext;
	}
	
}
