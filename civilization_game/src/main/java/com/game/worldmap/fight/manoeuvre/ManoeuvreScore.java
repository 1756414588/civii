package com.game.worldmap.fight.manoeuvre;

import com.game.pb.SerializePb.ManScorePB;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManoeuvreScore {

	private int country;         // 国家
	private int score;           // 积分
	private int captureFlag;     // 夺旗次数
	private int killSoidler;     // 杀敌数

	public ManoeuvreScore() {
	}

	public ManoeuvreScore(int country, int score, int captureFlag) {
		this.country = country;
		this.score = score;
		this.captureFlag = captureFlag;
	}

	public ManoeuvreScore(ManScorePB pb) {
		this.country = pb.getCountry();
		this.score = pb.getScore();
		this.captureFlag = pb.getCaptureFlag();
		this.killSoidler = pb.getKillSoidler();
	}

	public ManScorePB warp() {
		ManScorePB.Builder builder = ManScorePB.newBuilder();
		builder.setCountry(country);
		builder.setScore(score);
		builder.setCaptureFlag(captureFlag);
		builder.setKillSoidler(killSoidler);
		return builder.build();
	}

	@Override
	public String toString() {
		return "【country:" + country + " score:" + score + " captureFlag:" + captureFlag + " killSoilder:" + killSoidler + "】";
	}

}
