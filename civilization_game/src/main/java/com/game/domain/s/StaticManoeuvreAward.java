package com.game.domain.s;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticManoeuvreAward {

	private int id;
	private String name;
	private List<List<List<Integer>>> awards;

}
