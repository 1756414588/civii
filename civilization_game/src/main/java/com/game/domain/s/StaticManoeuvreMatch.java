package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticManoeuvreMatch {

	private int id;
	private int sort;
	private int countryA;
	private int countryB;


	@Override
	public String toString() {
		return "id:" + id + "【" + countryA + " VS " + countryB + "】";
	}
}
