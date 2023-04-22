package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Manoeuvre {

	private long keyId;
	private long startTime;
	private int status;
	private int stage;
	private int winer;
	private int roundOne;
	private int roundTwo;
	private int roundThree;
	private byte[] apply;
	private byte[] fights;
	private byte[] roundInfo;
	private byte[] detail;
	private byte[] rank;

	public Manoeuvre() {

	}

	public Manoeuvre(Manoeuvre copy) {
		this.keyId = copy.keyId;
		this.startTime = copy.getStartTime();
		this.status = copy.getStatus();
		this.stage = copy.getStage();
		this.winer = copy.getWiner();
		this.roundOne = copy.getRoundOne();
		this.roundTwo = copy.getRoundTwo();
		this.roundThree = copy.getRoundThree();
		this.apply = copy.getApply();
		this.fights = copy.getFights();
		this.roundInfo = copy.getRoundInfo();
		this.detail = copy.getDetail();
		this.rank = copy.getRank();
	}

}
