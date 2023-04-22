package com.game.worldmap.fight.war;

import com.game.pb.CommonPb;
import com.game.worldmap.WarInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 国家城战
 */
@Getter
@Setter
public class CountryCityWarInfo extends WarInfo {

	private int cityId;        //国战城池ID
	private String cityName;//禁卫军出动的时候 需要四方要塞的名字

	public CommonPb.WarInfo.Builder wrapCountryPb(boolean isJoin) {

		CommonPb.WarInfo.Builder builder = CommonPb.WarInfo.newBuilder();
		builder.setWarId(warId);
		builder.setEndTime(endTime);
		builder.setWarType(warType);
		builder.setCityWarType(cityWarType);

		builder.setAttackerCountry(attacker.getCountry());
		builder.setDefenceCountry(defender.getCountry());
		builder.setPos(defender.getPos().wrapPb());
		builder.setAttackerSoldier(getAttackSoldierNum());
		builder.setDefenceSoldier(getDefenceSoldierNum());
		builder.setHelpTime(attacker.getHelpTime());
		builder.setDefencerHelpTime(defender.getHelpTime());
		builder.setAttackPos(attacker.getPos().wrapPb());
		refreshWarInfo(builder, isJoin);
		return builder;
	}

}
