package com.game.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Triple<L, M, R> {

	private L left;
	private M middle;
	private R right;

	public Triple() {
	}

	public Triple(L left, M middle, R right) {
		this.left = left;
		this.middle = middle;
		this.right = right;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		return sb.append("[").append(left).append(":").append(middle).append(":").append(right).append("]").toString();
	}
}
