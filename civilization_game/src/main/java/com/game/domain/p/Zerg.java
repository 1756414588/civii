package com.game.domain.p;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Zerg {

	private int keyId;
	private int cityId;
	private int mapId;
	private int x;
	private int y;
	private int openDate;
	private int step;
	private int wave;
	private long stepEndTime;
	private long preHotTime;
	private long openTime;
	private long endTime;
	private List<Long> attacks = new ArrayList<>();

}
