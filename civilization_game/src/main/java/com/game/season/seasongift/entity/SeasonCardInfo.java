package com.game.season.seasongift.entity;

import com.game.pb.SeasonActivityPb;
import com.game.season.BaseModule;
import com.game.season.SeasonAct;
import com.game.season.SeasonInfo;

/**
 * 赛季卡
 */
public class SeasonCardInfo extends BaseModule {
	private long endTime;// 月卡到期时间
	private long drawTime;

	@Override
	public SeasonAct getType() {
		return SeasonAct.ACT_8;
	}

	@Override
	public void load(SeasonInfo seasonInfo) {
		try {
			SeasonActivityPb.SeasonCardInfoPb seasonCardInfoPb = SeasonActivityPb.SeasonCardInfoPb.parseFrom(seasonInfo.getInfo());
			this.endTime = seasonCardInfoPb.getEndTime();
			this.drawTime = seasonCardInfoPb.getDrawTime();
		} catch (Exception e) {

		}
	}

	@Override
	public byte[] save() {
		SeasonActivityPb.SeasonCardInfoPb.Builder builder = SeasonActivityPb.SeasonCardInfoPb.newBuilder();
		builder.setEndTime(this.endTime);
		builder.setDrawTime(this.drawTime);
		return builder.build().toByteArray();
	}

	@Override
	public void clean() {

	}

	@Override
	public void clean(int actId) {

	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDrawTime() {
		return drawTime;
	}

	public void setDrawTime(long drawTime) {
		this.drawTime = drawTime;
	}
}
