package com.game.worldmap.fight.manoeuvre;

import com.game.pb.SerializePb.ManRoundDetailPB;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManoeuvreDetail {

	private int round;
	private int stage;
	private long playerId;
	private int heroId;
	private int diviNum;
	private int killSoilder;
	private int lostSoilder;
	private int maxSoilder;

	public ManoeuvreDetail() {
	}

	public ManoeuvreDetail(int stage, int round, long playerId, ManRoundDetailPB detailPB) {
		this.round = round;
		this.stage = stage;
		this.playerId = playerId;
		this.heroId = detailPB.getHeroId();
		this.killSoilder = detailPB.getKillSoilder();
		this.lostSoilder = detailPB.getLostSoilder();
		this.diviNum = detailPB.getDiviNum();
		this.maxSoilder = detailPB.getMaxSoilder();
	}

	public ManRoundDetailPB wrap() {
		ManRoundDetailPB.Builder builder = ManRoundDetailPB.newBuilder();
		builder.setHeroId(heroId);
		builder.setDiviNum(diviNum);
		builder.setKillSoilder(killSoilder);
		builder.setLostSoilder(lostSoilder);
		builder.setMaxSoilder(maxSoilder);
		return builder.build();
	}
}
