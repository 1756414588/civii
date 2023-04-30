package com.game.manager;

import com.game.util.PbHelper;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.game.constant.ActivityConst;
import com.game.constant.BeautySkillType;
import com.game.constant.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dataMgr.StaticJourneyMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.s.StaticJourney;
import com.game.domain.s.StaticVip;
import com.game.pb.CommonPb;
import com.game.util.BasePbHelper;
import com.game.util.TimeHelper;
import com.game.util.random.WeightRandom;

/**
 * 2020年8月17日
 * 
 *    halo_game JourneyManager.java
 **/
@Component
public class JourneyManager {
	@Autowired
	private StaticJourneyMgr staticJourneyMgr;
	
	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private ActivityManager activityManager;

	public CommonPb.Journey.Builder getNowLastJourney(Player player) {
		int lastJourney = player.getLord().getLastJourney();

		CommonPb.Journey.Builder builder = CommonPb.Journey.newBuilder();
		if (lastJourney != 0) {
			StaticJourney staticJourney = staticJourneyMgr.getStaticJourney(lastJourney);

			builder.setJourneyId(lastJourney);
			builder.setJourneyType(staticJourney.getJourneyType());
			builder.setState(2);
		}
		return builder;
	}
	
	public void getJourneyTimes(Player player) {
		StaticVip staticVip = staticVipMgr.getStaticVip(player.getLord().getVip());
		//int journeyBuyTime = staticVip.getJourneyBuyTime();
		long freeJourneyEndTime = player.getLord().getFreeJourneyEndTime();
		long buyJourneyEndTime = player.getLord().getBuyJourneyEndTime();
		
		long zeroOfDay = TimeHelper.getZeroOfDay();//24点重置小游戏次数
		if (freeJourneyEndTime < zeroOfDay) {
			player.getLord().setFreeJourneyEndTime(System.currentTimeMillis());
			int journeyTimes = Math.max(10,player.getLord().getJourneyTimes());
			player.getLord().setJourneyTimes(journeyTimes);
		}
		
		if (buyJourneyEndTime < zeroOfDay) {
			player.getLord().setBuyJourneyEndTime(System.currentTimeMillis()); 
			player.getLord().setBuyJourneyTimes(staticVip.getJourneyBuyTime());
			player.getLord().setBuyJourneyTimes(0);
		}
	}
	
	public CommonPb.Award getStableAwards(int journeyId){
		StaticJourney staticJourney = staticJourneyMgr.getStaticJourney(journeyId);
		if(staticJourney == null) {
			return null;
		}
		
		List<Integer> stableAwards = staticJourney.getStableAwards();
		if(stableAwards == null || stableAwards.size()!=3) {
			return null;
		}

		double pre = 1;
		double actAdd = activityManager.actDouble(ActivityConst.ACT_JOURNEY_DOUBLE);
		int addFactor = (int) (actAdd + pre);

		CommonPb.Award.Builder award = PbHelper.createAward(stableAwards.get(0), stableAwards.get(1), stableAwards.get(2)*addFactor);
		return award.build();
	}
	
	
	public CommonPb.Award getRandomAwards(int journeyId){
		StaticJourney staticJourney = staticJourneyMgr.getStaticJourney(journeyId);
		if(staticJourney == null) {
			return null;
		}
		
		if(staticJourney.getRandomAwardId() == null || staticJourney.getRandomAwardId().size() == 0) {
			return null;
		}
		
		if(staticJourney.getRandomAwards() == null) {
			return null;
		}
		
		Random random = new Random();
		int type = random.nextInt(staticJourney.getRandomAwardId().size());
		List<List<Integer>> randomAwardType = staticJourney.getRandomAwardId();
		List<Integer> randomAward = randomAwardType.get(type);
		
		int count = WeightRandom.initData(staticJourney.getRandomAwards());
		CommonPb.Award.Builder award = PbHelper.createAward(randomAward.get(0), randomAward.get(1), count);
		
		return award.build();
	}
	
}