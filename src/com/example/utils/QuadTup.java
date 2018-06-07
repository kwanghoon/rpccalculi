package com.example.utils;

public class QuadTup<E, T, V, M> {
	private E first;
	private T second;
	private V third;
	private M fourth;
	
	public QuadTup(E first, T second, V third, M fourth) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
		this.fourth = fourth;
	}

	public E getFirst() {
		return first;
	}

	public void setFirst(E first) {
		this.first = first;
	}

	public T getSecond() {
		return second;
	}

	public void setSecond(T second) {
		this.second = second;
	}

	public V getThird() {
		return third;
	}

	public void setThird(V third) {
		this.third = third;
	}

	public M getFourth() {
		return fourth;
	}

	public void setFourth(M fourth) {
		this.fourth = fourth;
	}
		
}
