package com.game.worldmap.fight.manoeuvre;

import com.game.domain.Player;
import com.game.domain.p.Manoeuvre;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ManoeuvreLinePB;
import com.game.pb.SerializePb.ManApplyPB;
import com.game.pb.SerializePb.ManRoundPB;
import com.game.pb.SerializePb.ManScorePB;
import com.game.pb.SerializePb.SerManoeuvreApply;
import com.game.pb.SerializePb.SerManoeuvreRound;
import com.game.pb.SerializePb.SerManoeuvreScore;
import com.game.spring.SpringUtil;
import com.google.common.collect.HashBasedTable;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ManoeuvreData {

	// 第几届
	private long keyId;
	// 开启时间
	private long startTime;
	// 0.未开启 1.报名阶段 2.准备阶段 3.开始 4.结算
	private int status;
	// 1.第几回合
	private int stage;
	// 该次比赛胜利国家
	private int winer;
	// 国家赛程信息stage:ManoeuvreCourse
	private Map<Integer, ManoeuvreCourse> courseMap = new HashMap<>();
	// 报名信息.用户ID:报名信息
	private Map<Long, ManoeuvreFighter> applyMap = new ConcurrentHashMap<>();
	// 国家ID,线路,报名数据
	private HashBasedTable<Integer, Integer, List<ManoeuvreFighter>> fights = HashBasedTable.create();
	// 积分排行
	private List<ManoeuvreScore> scoreList = new ArrayList<>();

	public ManoeuvreData() {
	}

	public ManoeuvreData(Manoeuvre manoeuvre) {
		this.keyId = manoeuvre.getKeyId();
		this.startTime = manoeuvre.getStartTime();
		this.status = manoeuvre.getStatus();
		this.stage = manoeuvre.getStage();
		this.winer = manoeuvre.getWiner();

		// 赛程信息
		initCourse(1, manoeuvre.getStartTime() + 20 * 60 * 1000, manoeuvre.getRoundOne());
		initCourse(2, manoeuvre.getStartTime() + 40 * 60 * 1000, manoeuvre.getRoundTwo());
		initCourse(3, manoeuvre.getStartTime() + 60 * 60 * 1000, manoeuvre.getRoundThree());

		initCountryLineFightMap();

		dserApply(manoeuvre.getApply());// 报名信息
		dserFights(manoeuvre.getFights());// 参战阵容信息
		dserScore(manoeuvre.getRank());// 积分排行信息
		dserRoundInfo(manoeuvre.getRoundInfo());// 战斗回合信息
	}

	public Manoeuvre createManoeuvre() {
		Manoeuvre manoeuvre = new Manoeuvre();
		manoeuvre.setKeyId(keyId);
		manoeuvre.setStartTime(startTime);
		manoeuvre.setStatus(status);
		manoeuvre.setStage(stage);
		manoeuvre.setWiner(winer);
		manoeuvre.setRoundOne(courseMap.get(1).getRound());
		manoeuvre.setRoundTwo(courseMap.get(2).getRound());
		manoeuvre.setRoundThree(courseMap.get(3).getRound());
		manoeuvre.setApply(serApply());
		manoeuvre.setFights(serFights());
		manoeuvre.setRank(serScore());
		manoeuvre.setRoundInfo(serRoundInfo());
		return manoeuvre;
	}

	public void initCountryLineFightMap() {
		for (int county = 1; county <= 3; county++) {
			for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
				fights.put(county, line, new ArrayList<>());
			}
		}
	}

	/**
	 * 赛程信息
	 *
	 * @param stage
	 * @param time
	 * @param round
	 */
	private void initCourse(int stage, long time, int round) {
		ManoeuvreCourse course = new ManoeuvreCourse(stage, time, round);
		courseMap.put(stage, course);
	}

	/**
	 * 申请信息
	 *
	 * @param bytes
	 */
	private void dserApply(byte[] bytes) {
		try {
			if (bytes == null) {
				return;
			}
			SerManoeuvreApply apply = SerManoeuvreApply.parseFrom(bytes);
			for (ManApplyPB pb : apply.getApplyList()) {
				ManoeuvreFighter fighter = new ManoeuvreFighter(pb);
				applyMap.put(fighter.getPlayerId(), fighter);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	private byte[] serApply() {
		SerManoeuvreApply.Builder builder = SerManoeuvreApply.newBuilder();
		applyMap.forEach((e, f) -> {
			builder.addApply(f.wrap());
		});
		return builder.build().toByteArray();
	}

	/**
	 * 线路参战信息
	 *
	 * @param bytes
	 */
	private void dserFights(byte[] bytes) {
		try {

			if (bytes == null) {
				return;
			}
			SerManoeuvreApply apply = SerManoeuvreApply.parseFrom(bytes);
			for (ManApplyPB pb : apply.getApplyList()) {
				ManoeuvreFighter fighter = new ManoeuvreFighter(pb);
				List<ManoeuvreFighter> list = fights.get(pb.getCountry(), pb.getLine());
				list.add(fighter);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	public byte[] serFights() {
		SerManoeuvreApply.Builder builder = SerManoeuvreApply.newBuilder();
		fights.values().stream().forEach(list -> {
			if (list == null || list.isEmpty()) {
				return;
			}
			list.forEach(e -> {
				builder.addApply(e.wrap());
			});
		});
		return builder.build().toByteArray();
	}

	/**
	 * 积分排行信息
	 *
	 * @param bytes
	 */
	private void dserScore(byte[] bytes) {
		try {
			if (bytes == null) {
				return;
			}
			SerManoeuvreScore serManoeuvreScore = SerManoeuvreScore.parseFrom(bytes);
			for (ManScorePB pb : serManoeuvreScore.getScoreList()) {
				ManoeuvreScore manoeuvreScore = new ManoeuvreScore(pb);
				scoreList.add(manoeuvreScore);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	private byte[] serScore() {
		SerManoeuvreScore.Builder builder = SerManoeuvreScore.newBuilder();
		scoreList.forEach(e -> {
			builder.addScore(e.warp());
		});
		return builder.build().toByteArray();
	}

	/**
	 * 战斗回合信息
	 *
	 * @param bytes
	 */
	private void dserRoundInfo(byte[] bytes) {
		try {
			if (bytes == null) {
				return;
			}
			SerManoeuvreRound serManoeuvreRound = SerManoeuvreRound.parseFrom(bytes);
			for (ManRoundPB pb : serManoeuvreRound.getRoundsList()) {
				ManoeuvreCourse manoeuvreCourse = courseMap.get(pb.getStage());
				ManoeuvreRound round = new ManoeuvreRound(pb);
				manoeuvreCourse.getRoundList().add(round);
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	private byte[] serRoundInfo() {
		SerManoeuvreRound.Builder builder = SerManoeuvreRound.newBuilder();
		courseMap.forEach((e, f) -> {
			f.getRoundList().forEach(round -> {
				builder.addRounds(round.wrap());
			});
		});
		return builder.build().toByteArray();
	}

	public List<ManoeuvreLinePB> getApplyLinePb(int country,int stage) {
		List<ManoeuvreLinePB> result = new ArrayList<>();
		if (applyMap.isEmpty()) {
			return result;
		}
		Map<Integer, List<ManoeuvreFighter>> group = null;
		if (stage == 0){
			group = applyMap.values().stream().filter(e -> e.getCountry() == country).collect(Collectors.groupingBy(ManoeuvreFighter::getLine));
		}else if(stage > 0){
			group = fights.row(country);
		}

		for (int line = ManoeuvreConst.LINE_ONE; line <= ManoeuvreConst.LINE_THREE; line++) {
			if (group.containsKey(line)) {
				List<ManoeuvreFighter> list = group.get(line);
				ManoeuvreLinePB.Builder builder = ManoeuvreLinePB.newBuilder();
				builder.setLine(line);
				builder.setNumber(list.size());
				builder.setTotalNumber(25);
				long totalFight = list.stream().mapToLong(ManoeuvreFighter::getPower).sum();
				builder.setTotalFight((int) totalFight);
				result.add(builder.build());
			} else {
				ManoeuvreLinePB.Builder builder = ManoeuvreLinePB.newBuilder();
				builder.setLine(line);
				builder.setNumber(0);
				builder.setTotalNumber(25);
				builder.setTotalFight(0);
				result.add(builder.build());
			}
		}
		return result;
	}

	public List<ManoeuvreFighter> getArmys(int country, int line) {
		if (status <= ManoeuvreConst.STATUS_APPLY) {
			List<ManoeuvreFighter> list = applyMap.values().stream().filter(e -> e.getCountry() == country && e.getLine() == line).sorted(Comparator.comparing(ManoeuvreFighter::getPower).reversed().thenComparing(ManoeuvreFighter::getApplyTime)).collect(Collectors.toList());
			if (list != null && list.size() > 25) {
				return list.subList(0, 25);
			} else {
				return list;
			}
		} else {
			return fights.get(country, line);
		}
	}

	public Map<Integer, List<CommonPb.ManoeuverApply>> getFighterPb() {
		Map<Integer, List<CommonPb.ManoeuverApply>> map = new HashMap<>();
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		for (List<ManoeuvreFighter> list : fights.values()) {
			for (ManoeuvreFighter fighter : list) {
				List<CommonPb.ManoeuverApply> applyList = map.get(fighter.getCountry());
				if (applyList == null) {
					applyList = new ArrayList<>();
					map.put(fighter.getCountry(), applyList);
				}
				Player target = playerManager.getPlayer(fighter.getPlayerId());
				CommonPb.ManoeuverApply.Builder pb = CommonPb.ManoeuverApply.newBuilder();
				pb.setLordId(target.getRoleId());
				pb.setPortrait(target.getPortrait());
				pb.setNick(target.getNick());
				pb.setPower((int) fighter.getPower());
				pb.setTitle(target.getTitle());
				pb.setOffice(target.getOfficerId());
				applyList.add(pb.build());
			}
		}
		return map;
	}
}
