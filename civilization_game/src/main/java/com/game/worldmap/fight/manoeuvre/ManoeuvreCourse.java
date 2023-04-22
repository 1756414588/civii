package com.game.worldmap.fight.manoeuvre;

import com.game.pb.CommonPb.FourInt;
import com.game.pb.CommonPb.ManoeuverReport;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManoeuvreCourse {

	private int stage;
	private long time;
	private int round;
	private int countryLeft;// 左侧国家
	private int scoreLeft;// 左侧积分
	private int countryRight;// 右侧国家
	private int scoreRight;// 右侧积分
	private int status;// 0未开打 1.已开打 2.结束

	private List<ManoeuvreRound> roundList = new ArrayList<>();//玩家对战回合信息
	// 国家线路报名人数
	private HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fights = HashBasedTable.create();


	public ManoeuvreCourse(int stage, long time, int round) {
		this.stage = stage;
		this.time = time;
		this.round = round;
		this.countryLeft = round / 1000;
		this.countryRight = round % 1000 / 100;
		this.scoreLeft = round % 100 / 10;
		this.scoreRight = round % 10;
	}

	public int getRound() {
		return round + scoreLeft * 10 + scoreRight;
	}

	public Pair<Integer, Integer> getKillSoilder() {
		int leftKill = 0;
		int rightKill = 0;
		for (ManoeuvreRound round : roundList) {
			Pair<Integer, Integer> r = round.getKillSoiler();
			leftKill += r.getLeft();
			rightKill += r.getRight();
		}
		return new Pair<>(leftKill, rightKill);
	}

	public boolean isComplete() {
		return !roundList.isEmpty();
	}

	public int getBattleResult() {
		if (scoreLeft == scoreRight) {
			return 0;
		}
		if (scoreLeft > scoreRight) {
			return 1;
		} else {
			return 2;
		}
	}

	public ManoeuverReport getReportPb() {
		ManoeuverReport.Builder builder = ManoeuverReport.newBuilder();
		builder.setLeftCountry(countryLeft);
		builder.setLeftScore(scoreLeft);
		builder.setRightCountry(countryRight);
		builder.setRightScore(scoreRight);
		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			FourInt.Builder four = FourInt.newBuilder();
			List<ManoeuvreFighter> leftAttends = fights.get(countryLeft, line);
			long leftAlive = leftAttends.stream().filter(e -> e.alive()).count();
			four.setV1((int) leftAlive);
			four.setV2(leftAttends.size());

			List<ManoeuvreFighter> rightAttends = fights.get(countryRight, line);
			long rightAlive = rightAttends.stream().filter(e -> e.alive()).count();
			four.setV3((int) rightAlive);
			four.setV4(rightAttends.size());
			builder.addLine(four);
		}
		return builder.build();
	}

	public Map<Long, Integer> getBeatMap() {
		Map<Long, Integer> result = new HashMap<>();
		for (ManoeuvreRound round : roundList) {
			int booldLeft = round.getBloodLeft();
			int booldRight = round.getBloodRight();
			long roldIdLeft = round.getPlayerIdLeft();
			long roleIdRight = round.getPlayerIdRight();

			if (!result.containsKey(roldIdLeft)) {
				result.put(roldIdLeft, 0);
			}
			if (!result.containsKey(roleIdRight)) {
				result.put(roleIdRight, 0);
			}

			if (booldLeft > 0 && booldRight <= 0) {
				int count = result.get(roldIdLeft);
				count += 1;
				result.put(roldIdLeft, count);
			}
			if (booldLeft <= 0 && booldRight > 0) {
				int count = result.get(roleIdRight);
				count += 1;
				result.put(roleIdRight, count);
			}
			LogHelper.GAME_LOGGER.info("manoeuvreRound:{}", round);
		}
		LogHelper.GAME_LOGGER.info("result:{}", result);
		return result;
	}
}
