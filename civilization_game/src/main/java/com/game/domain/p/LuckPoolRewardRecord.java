package com.game.domain.p;


import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import com.game.util.StringUtil;
import com.game.util.TimeHelper;
import java.util.Date;

/**
 * @Description 辛运奖池抽奖记录
 * @Date 2021/2/18 14:34
 **/
public class LuckPoolRewardRecord {

	private String time;
	private String record;
	private Player player;
	private CommonPb.Award award;

	public LuckPoolRewardRecord() {

	}

	public LuckPoolRewardRecord(Player player, CommonPb.Award award) {
		this.player = player;
		this.award = award;
	}

	public LuckPoolRewardRecord(DataPb.RecordData e) {
		this.time = e.getTime();
		this.record = e.getRecord();
		this.player = SpringUtil.getBean(PlayerManager.class).getPlayer(e.getUid());
		this.award = e.getAward();
	}

	public CommonPb.ActLuckPoolRecord ser(int acId) {
		if (player != null && !StringUtil.isNullOrEmpty(player.getNick())) {
			return CommonPb.ActLuckPoolRecord.newBuilder().setNick(player.getNick()).setAward(award).setActivityId(acId).build();
		} else {
			return CommonPb.ActLuckPoolRecord.newBuilder().setNick("???").setAward(award).setActivityId(acId).build();
		}
	}

	public DataPb.RecordData serRecordData() {
		DataPb.RecordData.Builder builder = DataPb.RecordData.newBuilder();
		builder.setTime(this.time == null ? "" : this.time);
		builder.setRecord(this.getRecord());
		builder.setUid(this.getPlayer() != null ? this.getPlayer().getRoleId() : 0L);
		builder.setAward(this.getAward());
		return builder.build();
	}

	public String getTime() {
		return time == null ? "" : time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRecord() {
		return record == null ? "" : record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public CommonPb.Award getAward() {
		return award;
	}

	public void setAward(CommonPb.Award award) {
		this.award = award;
	}

	@Override
	public String toString() {
		return "LuckPoolRewardRecord{" +
			"time='" + time + '\'' +
			", record='" + record + '\'' +
			'}';
	}
}
