package com.game.domain.s;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticManoeuvreShop {

	private int id;
	private int type;
	private List<Integer> award;
	private int price;
	private int limitCount;
	private int beautyId;

}
