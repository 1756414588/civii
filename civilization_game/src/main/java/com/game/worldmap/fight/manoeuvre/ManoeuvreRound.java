package com.game.worldmap.fight.manoeuvre;

import com.game.pb.SerializePb.ManRoundDetailPB;
import com.game.pb.SerializePb.ManRoundPB;
import com.game.util.Pair;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 回合信息
 */
@Setter
@Getter
public class ManoeuvreRound {

	private int line;
	private int stage;
	private int round;
	private int postLeft;
	private long playerIdLeft;
	private String nickLeft;
	private int bloodLeft;
	private int postRight;
	private long playerIdRight;
	private String nickRight;
	private int bloodRight;
	private List<ManoeuvreDetail> detailList = new ArrayList<>();

	public ManoeuvreRound() {
	}

	public ManoeuvreRound(ManRoundPB pb) {
		this.line = pb.getLine();
		this.stage = pb.getStage();
		this.round = pb.getRound();
		this.postLeft = pb.getPostLeft();
		this.playerIdLeft = pb.getPlayerIdLeft();
		this.nickLeft = pb.getNickLeft();
		this.bloodLeft = pb.getBloodLeft();
		this.postRight = pb.getPostRight();
		this.playerIdRight = pb.getPlayerIdRight();
		this.nickRight = pb.getNickRight();
		this.bloodRight = pb.getBloodRight();

		for (ManRoundDetailPB detailPB : pb.getLeftList()) {
			ManoeuvreDetail detail = new ManoeuvreDetail(stage, line, playerIdLeft, detailPB);
			detailList.add(detail);
		}
		for (ManRoundDetailPB detailPB : pb.getRightList()) {
			ManoeuvreDetail detail = new ManoeuvreDetail(stage, line, playerIdRight, detailPB);
			detailList.add(detail);
		}
	}

	public ManRoundPB wrap() {
		ManRoundPB.Builder builder = ManRoundPB.newBuilder();
		builder.setLine(line);
		builder.setStage(stage);
		builder.setRound(round);
		builder.setPostLeft(postLeft);
		builder.setPlayerIdLeft(playerIdLeft);
		builder.setNickLeft(nickLeft);
		builder.setBloodLeft(bloodLeft);
		builder.setPostRight(postRight);
		builder.setPlayerIdRight(playerIdRight);
		builder.setNickRight(nickRight);
		builder.setBloodRight(bloodRight);

		for (ManoeuvreDetail detail : detailList) {
			if (detail.getPlayerId() == playerIdLeft) {
				builder.addLeft(detail.wrap());
			} else {
				builder.addRight(detail.wrap());
			}
		}
		return builder.build();
	}

	public Pair<Integer, Integer> getKillSoiler() {
		int leftKillSoilder = 0;
		int rightKillSoilder = 0;
		for (ManoeuvreDetail detail : detailList) {
			if (detail.getPlayerId() == playerIdLeft) {
				leftKillSoilder += detail.getKillSoilder();
			} else {
				rightKillSoilder += detail.getKillSoilder();
			}
		}
		return new Pair<>(leftKillSoilder, rightKillSoilder);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(playerIdLeft).append(":").append(bloodLeft).append("|").append(playerIdRight).append(":").append(bloodRight);
		return sb.toString();
	}

}
