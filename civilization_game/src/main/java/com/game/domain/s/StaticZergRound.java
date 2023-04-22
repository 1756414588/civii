package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticZergRound {

	private int id;
	private int type;
	private int wave;
	private long startTime;
	private long endTime;
    private int roundFinish;
    private int nextId;

}
