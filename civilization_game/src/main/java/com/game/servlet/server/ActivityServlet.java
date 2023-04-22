package com.game.servlet.server;

import com.game.server.datafacede.SaveActivityServer;
import com.game.spring.SpringUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.ActivityConst;
import com.game.domain.ActivityData;
import com.game.domain.RecordReissueAwards;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.domain.s.StaticActFoot;
import com.game.domain.s.StaticActFreeBuy;
import com.game.log.LogUser;
import com.game.service.ActivityService;
import com.game.util.TimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.game.constant.UcCodeEnum;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.manager.ActivityManager;
import com.game.manager.PlayerManager;
import com.game.uc.Message;

/**
 * 2020年5月8日
 *
 * @CaoBing halo_game ActivityServlet.java
 * <p>
 * 活动计划接口
 **/

@RequestMapping("act")
@Controller
public class ActivityServlet {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 活动计划
	 *
	 * @param
	 * @param
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/actPlan", method = RequestMethod.POST)
	public Message actPlan() {
		logger.info("ActivityServlet actPlan");

		try {
			/**
			 * 保存活动数据
			 */
			SpringUtil.getBean(SaveActivityServer.class).saveAll();

			/**
			 * 重新加载活动
			 */
			StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
			ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);

			staticActivityMgr.init();
			activityManager.init();

			/**
			 * 向客户端推送拉取活动列表的消息
			 */
			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
			playerManager.getOnlinePlayer().forEach(e -> {
				playerManager.synActivity(e, 0, 0);
			});

		} catch (Exception e) {
			logger.error("ActivityServlet actPlan : desc{}", UcCodeEnum.SYS_ERROR.getDesc());
			e.printStackTrace();
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/recordReissueAwards", method = RequestMethod.POST)
	public Message recordReissueAwards() {
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityService activityService = SpringUtil.getBean(ActivityService.class);
		Map<Long, Player> players = playerManager.getPlayers();
		Iterator<Player> iterator = null;
		if (players.size() > 0) {
			iterator = players.values().iterator();
		} else {
			return new Message(UcCodeEnum.SYS_ERROR);
		}
		List<ActivityBase> list = staticActivityMgr.getActivityList();
		ArrayList<RecordReissueAwards> RecordReissueAwardsList = new ArrayList<>();
		for (ActivityBase activityBase : list) {
			int step = activityBase.getStep();
			int activityId = activityBase.getActivityId();
			if (activityId == ActivityConst.ACT_GROW_FOOT) {
				if (step == ActivityConst.ACTIVITY_BEGIN || step == ActivityConst.ACTIVITY_DISPLAY) {
					while (iterator.hasNext()) {
						Player player = iterator.next();
						ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
						if (actRecord == null) {
							continue;
						}

						ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
						if (activityData == null) {
							continue;
						}

						int awardId = actRecord.getAwardId();
						List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
						if (condList == null) {
							continue;
						}

						List<StaticActFoot> footList = staticActivityMgr.getActFoots(awardId);
						if (footList == null) {
							continue;
						}
						for (StaticActFoot foot : footList) {
							if (null == foot) {
								continue;
							}
							int sortId = foot.getSortId();

							// 0.未购买 1-N购买后的第几天
							int state = activityService.currentActivity(player, actRecord, sortId);
							if (state > 0) {
								RecordReissueAwards recordReissueAwards = new RecordReissueAwards(
									player.getLord().getLordId(),
									player.account.getAccountKey(),
									player.getNick(),
									activityBase.getActivityId(),
									activityBase.getStaticActivity().getName(),
									foot.getFootId(),
									awardId,
									foot.getType(),
									foot.getName(),
									state
								);

								RecordReissueAwardsList.add(recordReissueAwards);
							}
						}
					}
				}
			}
			if (activityId == ActivityConst.ACT_ZERO_GIFT) {
				if (step == ActivityConst.ACTIVITY_BEGIN || step == ActivityConst.ACTIVITY_DISPLAY) {
					while (iterator.hasNext()) {
						Player player = iterator.next();
						ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ZERO_GIFT);
						if (actRecord == null) {
							continue;
						}

						ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
						if (activityData == null) {
							continue;
						}

						int awardId = actRecord.getAwardId();
						List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
						if (condList == null) {
							continue;
						}

						List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(awardId);
						if (footList == null) {
							continue;
						}

						for (StaticActFreeBuy foot : footList) {
							if (null == foot) {
								continue;
							}
							int sortId = foot.getSortId();

							// 0.未购买 1-N购买后的第几天
							int state = activityService.currentActivity(player, actRecord, sortId);
							if (state > 0) {
								RecordReissueAwards recordReissueAwards = new RecordReissueAwards(
									player.getLord().getLordId(),
									player.account.getAccountKey(),
									player.getNick(),
									activityBase.getActivityId(),
									activityBase.getStaticActivity().getName(),
									foot.getFootId(),
									awardId,
									foot.getType(),
									foot.getName(),
									state
								);

								RecordReissueAwardsList.add(recordReissueAwards);
							}
						}
					}
				}
			}
		}
		String recordReissueAwardsStr = "";
		if (RecordReissueAwardsList.size() != 0) {
			recordReissueAwardsStr = JSONObject.toJSONString(RecordReissueAwardsList);
			File file = new File(System.getProperty("user.dir") + "\\" + TimeHelper.getDay(new Date()) + "活动未领取记录ServerId_" + playerManager.getServerId() + ".txt");
			FileOutputStream outputStream;
			try {
				outputStream = new FileOutputStream(file);
				outputStream.write(recordReissueAwardsStr.getBytes());
				outputStream.close();
				for (RecordReissueAwards award : RecordReissueAwardsList) {
					//日志埋点
					SpringUtil.getBean(LogUser.class).recordReissueAwardsLog(award);
				}

			} catch (Exception e) {
				e.printStackTrace();
				return new Message(UcCodeEnum.SYS_ERROR);
			}
			return new Message(UcCodeEnum.SUCCESS, recordReissueAwardsStr);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}
}
