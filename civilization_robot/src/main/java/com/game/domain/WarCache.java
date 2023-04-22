package com.game.domain;

import com.game.pb.CommonPb.WarInfo;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.game.util.RandomUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/12/11 17:07
 **/

@Getter
@Setter
public class WarCache {

	private long warId;
	// 战斗结束时间
	private long endTime;

	private int attackCountry;

	private int defendCountry;

	private int x;
	private int y;

	private int cityId;
	private int mapId;

	// 参战时间,是否已参战
	private List<Pair<Long, Boolean>> attackerList = new ArrayList<>();

	public WarCache(WarInfo warInfo) {
		this.warId = warInfo.getWarId();
		this.endTime = warInfo.getEndTime();
		this.attackCountry = warInfo.getAttackerCountry();
		this.defendCountry = warInfo.getDefenceCountry();
		this.x = warInfo.getPos().getX();
		this.y = warInfo.getPos().getY();
		this.cityId = warInfo.getCityId();

	}

	public void initAttackTime() {
		int count = RandomUtil.randomBetween(1, 10);
		long curTime = System.currentTimeMillis();
		long millS = endTime - curTime;
		int maxTime = (int) (millS - 300000);
		for (int i = 0; i < count; i++) {
			int ran = RandomUtil.getRandomNumber(maxTime);
			long attackTime = curTime + ran;
			attackerList.add(new Pair<>(attackTime, false));
		}

		LogHelper.CHANNEL_LOGGER.info("warId:{} attackerList:{}", warId, attackerList);
	}
}
