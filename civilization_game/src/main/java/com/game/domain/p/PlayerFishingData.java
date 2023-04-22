package com.game.domain.p;

import com.game.pb.CommonPb.*;
import com.game.pb.SerializePb.SerPlayerFishingDataPB;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter

public class PlayerFishingData {

	private int level;
	private int exp;
	private long points;
	private Map<Integer, PlayerBaits> baits = new ConcurrentHashMap<>();
	private Map<Integer, TeamQueue> teamQueue = new ConcurrentHashMap<>();
	private Map<Integer, ReachBaitRecord> reachBaitRecords = new ConcurrentHashMap<>();
	private Map<Integer, ReachFishRecord> reachFishRecords = new ConcurrentHashMap<>();
	private Map<Integer, FishRecord> fishRecords = new ConcurrentHashMap<>();
	private int shareCount;
	private Map<Integer, FishCostRecord> fishCostRecords = new ConcurrentHashMap<>();
	private int resetTime;


	public byte[] serPlayerFishingData() {

		SerPlayerFishingDataPB.Builder builder = SerPlayerFishingDataPB.newBuilder();

		builder.setLevel(level);
		builder.setExp(exp);
		builder.setPoints(points);

		for (PlayerBaits bait : baits.values()) {
			builder.addBaits(bait.encode());
		}

		for (TeamQueue team : teamQueue.values()) {
			builder.addTeamQueue(team.encode(null));
		}

		for (ReachBaitRecord record : reachBaitRecords.values()) {
			builder.addReachBaitRecord(record.encode());
		}

		for (ReachFishRecord record : reachFishRecords.values()) {
			builder.addReachFishRecord(record.encode());
		}

		for (FishRecord record : fishRecords.values()) {
			builder.addFishRecord(record.encode());
		}

		builder.setShareCount(shareCount);

		for (FishCostRecord record : fishCostRecords.values()) {
			builder.addFishCostRecord(record.encode());
		}

		builder.setResetTime(resetTime);

		return builder.build().toByteArray();
	}


	public void dserPlayerFishingData(SerPlayerFishingDataPB fishData) {

		this.level = fishData.getLevel();
		this.exp = fishData.getExp();
		this.points = fishData.getPoints();

		for (PlayerBaitsPB baitsPB : fishData.getBaitsList()) {
			PlayerBaits bait = new PlayerBaits();
			bait.decode(baitsPB);
			this.baits.put(bait.getBaitId(), bait);
		}

		for (TeamQueuePB teamPB : fishData.getTeamQueueList()) {
			TeamQueue team = new TeamQueue();
			team.decode(teamPB);
			this.teamQueue.put(team.getTeamId(), team);
		}

		for (ReachBaitRecordPB reachBaitRecordPB : fishData.getReachBaitRecordList()) {
			ReachBaitRecord baitRecord = new ReachBaitRecord();
			baitRecord.decode(reachBaitRecordPB);
			this.reachBaitRecords.put(baitRecord.getBaitId(), baitRecord);
		}

		for (ReachFishRecordPB reachFishRecordPB : fishData.getReachFishRecordList()) {
			ReachFishRecord fishRecord = new ReachFishRecord();
			fishRecord.decode(reachFishRecordPB);
			this.reachFishRecords.put(fishRecord.getFishId(), fishRecord);
		}

		for (FishRecordPB fishRecordPB : fishData.getFishRecordList()) {
			FishRecord record = new FishRecord();
			record.decode(fishRecordPB);
			this.fishRecords.put(record.getRecordId(), record);
		}

		this.shareCount = fishData.getShareCount();

		for (FishCostRecordPB costRecordPB : fishData.getFishCostRecordList()) {
			FishCostRecord costRecord = new FishCostRecord();
			costRecord.decode(costRecordPB);
			this.fishCostRecords.put(costRecord.getPropId(), costRecord);
		}

		this.resetTime = fishData.getResetTime();
	}

	// 重置次数限制
	public void resetCountLimit(int currentDay) {
		setShareCount(0);
		fishCostRecords.clear();
		// 重置已派遣的队列
		for (TeamQueue team : teamQueue.values()) {
			if (team.getStatus() == 3) {
				team.initData();
				teamQueue.put(team.getTeamId(), team);
			}
		}
		setResetTime(currentDay);
	}

}
