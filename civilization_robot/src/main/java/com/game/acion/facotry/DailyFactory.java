package com.game.acion.facotry;

import com.game.acion.IAction;
import com.game.acion.daily.ActPowerDaily;
import com.game.acion.daily.AttackRebelDaily;
import com.game.acion.daily.AutoDaily;
import com.game.acion.daily.BuyUpBuildQueDaily;
import com.game.acion.daily.GetActPowerDaily;
import com.game.acion.daily.GetAllJourneyDaily;
import com.game.acion.daily.GetMapDaily;
import com.game.acion.daily.JourneyDoneDaily;
import com.game.acion.daily.GetAllMissionDaily;
import com.game.acion.daily.MissionDoneDaily;
import com.game.acion.daily.RecruitDoneDaily;
import com.game.acion.daily.RecruitSoldierDaily;
import com.game.acion.daily.RecruitWorkQueCdDaily;
import com.game.acion.daily.SweepMissionDaily;
import com.game.acion.daily.TechKillCdDaily;
import com.game.acion.daily.UpBuildingDaily;
import com.game.acion.daily.UpTechDaily;
import com.game.domain.p.DailyMessage;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/9/14 15:48
 **/
@Component
public class DailyFactory {

	public IAction createAction(DailyMessage message) {
		switch (message.getType()) {
			case 1:// 攻打野怪31411
				return new AttackRebelDaily(message);
			case 2:// 关卡信息30701
				return new GetAllMissionDaily(message);
			case 3:// 通关副本30705
				return new MissionDoneDaily(message);
			case 4:// 关卡扫荡30707
				return new SweepMissionDaily(message);
			case 5:// 远征关卡信息60400
				return new GetAllJourneyDaily(message);
			case 6:// 通关远征60402
				return new JourneyDoneDaily(message);
			case 7:// 升级建筑30203
				return new UpBuildingDaily(message);
			case 8:// 科技升级31301
				return new UpTechDaily(message);
			case 9:// 科技秒CD31307
				return new TechKillCdDaily(message);
			case 10:// 购买建造队列30217
				return new BuyUpBuildQueDaily(message);
			case 11:// 体力赠送31900
				return new ActPowerDaily(message);
			case 12:// 领取体力40070
				return new GetActPowerDaily(message);
			case 13:// 兵营招募请求30311
				return new RecruitSoldierDaily(message);
			case 14:// 兵营招募秒CD30317
				return new RecruitWorkQueCdDaily(message);
			case 15:// 兵营招募完成30313
				return new RecruitDoneDaily(message);
			case 16:// 地图场景31401
				return new GetMapDaily(message);
			default:// 自动日常
				return new AutoDaily(message);
		}
	}

}
