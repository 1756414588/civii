package com.game.manager;

import java.util.List;

import org.springframework.stereotype.Component;

import com.game.constant.ConditionType;
import com.game.constant.GameError;
import com.game.domain.Player;

// 通用的条件-完成系统[一般的条件、任务、活动等]
@Component
public class CondMgr {
	// 条件判断
	public GameError onCondition(Player player, int conditionType, List<Long> param) {
		if (conditionType == ConditionType.FREE_BUILD_TEAM) {
			return player.hasBuildTeam(param);
		} else if (conditionType == ConditionType.COMMAND_LEVEL) {
			return player.reachCommandLevel(param);
		} else if (conditionType == ConditionType.LORD_LEVEL) {
			return player.reachLordLevel(param);
		} else if (conditionType == ConditionType.RESOURCE) {
			return player.hasResource(param);
		} else if (conditionType == ConditionType.TECH_BUILDING_LEVEL) {
			return player.reachTechLevel(param);
		} else if (conditionType == ConditionType.TECH_RESEARCH_LEVEL) {
			return player.reachTechResearchLv(param);
		}

		return GameError.PARAM_ERROR;
	}

}
