package de.liebki.utils;

public class Pair<A, B> {

	public A getOne() {
		return one;
	}

	public void setOne(A one) {
		this.one = one;
	}

	public B getTwo() {
		return two;
	}

	public void setTwo(B two) {
		this.two = two;
	}

	public Pair(A one, B two) {
		super();
		this.one = one;
		this.two = two;
	}

	private A one;
	private B two;

}