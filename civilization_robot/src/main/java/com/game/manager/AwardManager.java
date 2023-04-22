package com.game.manager;

import com.game.constant.AwardType;
import com.game.domain.Robot;
import com.game.cache.UserBagCache;
import com.game.domain.World;
import com.game.pb.CommonPb.Award;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AwardManager {

	public void addAward(Robot robot, List<Award> awardList) {
		for (Award award : awardList) {
			addAward(robot, award);
		}
	}

	public void addAward(Robot robot, Award award) {
//		LogHelper.CHANNEL_LOGGER.info("addAward keyId:{} itemType:{} itemId:{} count:{}", award.getKeyId(), award.getType(), award.getId(), award.getCount());
		int type = award.getType();
		UserBagCache userBag = robot.getCache().getBagCache();
		if (type == AwardType.PROP) {
			userBag.addProp(award.getId(), award.getCount());
		} else if (type == AwardType.EQUIP) {
			userBag.addEquip(award.getKeyId(), award.getId(), award.getAllSkillList());
		} else if (type == AwardType.HERO) {
			robot.getCache().getHeroCache().putHero(award.getId());
		}
	}


}
