package com.game.acion.facotry;

import com.game.acion.IAction;
import com.game.acion.message.AddSoldierAction;
import com.game.acion.message.AttackRebelAction;
import com.game.acion.message.BuyBuildQueCdAction;
import com.game.acion.message.CommonAction;
import com.game.acion.message.DoActTaskHeroRewardAction;
import com.game.acion.message.GetLevelAwardAction;
import com.game.acion.message.GetMapAction;
import com.game.acion.message.MissionDoneAction;
import com.game.acion.message.RecoverBuildAction;
import com.game.acion.message.IgnoreResultAction;
import com.game.acion.message.RecruitSoldierAction;
import com.game.acion.message.RecruitWorkQueCdAction;
import com.game.acion.message.TaskAwardAction;
import com.game.acion.message.TechKillCdAction;
import com.game.acion.message.TechLevelupAction;
import com.game.acion.message.UpBuildingAction;
import com.game.acion.message.UpdateGuideAction;
import com.game.acion.message.WearEquipAction;
import com.game.acion.message.WearOmamentAction;
import com.game.domain.p.RobotMessage;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/9/14 15:48
 **/
@Component
public class ActionFactory {

	public IAction createAction(RobotMessage robotMessage) {
		switch (robotMessage.getType()) {
			case 0:
				return new CommonAction(robotMessage);
			case 1:// 攻打野怪
				return new AttackRebelAction(robotMessage);
			case 2:// 穿带装备
				return new WearEquipAction(robotMessage);
			case 3:// 新手引导
			case 7:// 在线时长奖励
			case 12:// 招募完成
			case 13:// 半价特训武将
			case 26:// 穿戴饰品
				return new IgnoreResultAction(robotMessage);
			case 4:// 新手引导
				return new UpdateGuideAction(robotMessage);
			case 5:// 任务奖励
				return new TaskAwardAction(robotMessage);
			case 6:// 英雄补兵
				return new AddSoldierAction(robotMessage);
			case 8:// 请求地图信息
				return new GetMapAction(robotMessage);
			case 9:// 收复建筑
				return new RecoverBuildAction(robotMessage);
			case 10:// 通关关卡
				return new MissionDoneAction(robotMessage);
			case 11:// 领取等级奖励
				return new GetLevelAwardAction(robotMessage);
			case 14:// 秒招募士兵CD
				return new RecruitWorkQueCdAction(robotMessage);
			case 15:// 科技秒CD
				return new TechKillCdAction(robotMessage);
			case 16:// 科技完成
				return new TechLevelupAction(robotMessage);
			case 17:// 日常任务完成
			case 18:// 签到奖励
			case 19:// 塔防活动奖励
				return new IgnoreResultAction(robotMessage);
			case 20:// 秒建筑CD
				return new BuyBuildQueCdAction(robotMessage);
			case 21:// 无畏尖兵
				return new DoActTaskHeroRewardAction(robotMessage);
			case 22:// 穿戴配饰
				return new WearOmamentAction(robotMessage);
			case 23:// 训练士兵
				return new RecruitSoldierAction(robotMessage);
			case 24:// 建筑升级
				return new UpBuildingAction(robotMessage);
			default:
				return new CommonAction(robotMessage);
		}
	}

}
