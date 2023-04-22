package com.game.domain.s;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticManoeuvreRankAward {

	private int id;
	private int type;
	private int param;
	private List<List<Integer>> awardList;

}
