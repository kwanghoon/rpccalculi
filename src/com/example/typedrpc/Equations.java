package com.example.typedrpc;

import java.util.ArrayList;

public class Equations {
	private ArrayList<Equ> equs;

	public Equations() {
		equs = new ArrayList<>();
	}

	public Equations(ArrayList<Equ> equs) {
		this.equs = equs;
	}

	public ArrayList<Equ> getEqus() {
		return equs;
	}

	public void setEqus(ArrayList<Equ> equs) {
		this.equs = equs;
	}

}
