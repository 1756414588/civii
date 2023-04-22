package com.game.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair<L, R> {

	private L left;
	private R right;

	public Pair() {
	}

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public String toString() {
		return left.toString() + ":" + right.toString();
	}
}
