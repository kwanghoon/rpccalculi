package com.example.utils;

public class TripleTup<E, V, T> {
	private E first;
	private V second;
	private T third;

	public TripleTup(E first, V second, T third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public E getFirst() {
		return first;
	}

	public void setFirst(E first) {
		this.first = first;
	}

	public V getSecond() {
		return second;
	}

	public void setSecond(V second) {
		this.second = second;
	}

	public T getThird() {
		return third;
	}

	public void setThird(T third) {
		this.third = third;
	}

}
