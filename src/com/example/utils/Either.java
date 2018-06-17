package com.example.utils;

public class Either<T, V> {
	private T left;
	private V right;
	private boolean isLeft;

	public Either() {
		left = null;
		right = null;
	}

	public T getLeft() {
		return left;
	}

	public void setLeft(T left) {
		this.left = left;
		isLeft = true;
	}

	public V getRight() {
		return right;
	}

	public void setRight(V right) {
		this.right = right;
		isLeft = false;
	}

	public boolean isLeft() {
		return isLeft;
	}
}
