package com.game.domain.s;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StaticEquip {

	private int equipId;
	private String equipName;
	private int quality;
	private int attack;
	private int defence;
	private int soldierCount;
	private List<Integer> skillId;
	private int equipType;
	private List<List<Long>> compose;
	private int lordLv;
	private int canCompose;
	private int period;  // 秒
	private List<List<Long>> decompose;
	private int secretSkill;


}
