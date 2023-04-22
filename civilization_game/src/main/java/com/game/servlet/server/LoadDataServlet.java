package com.game.servlet.server;

import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.game.dataMgr.StaticSensitiveWordMgr;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.game.constant.MarchReason;
import com.game.constant.UcCodeEnum;
import com.game.dataMgr.BaseDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.WorldData;
import com.game.domain.p.WorldActPlan;
import com.game.manager.PlayerManager;
import com.game.manager.WarManager;
import com.game.manager.WorldManager;
import com.game.server.GameServer;
import com.game.service.ActivityService;
import com.game.service.CountryService;
import com.game.service.MissionService;
import com.game.service.WorldActPlanService;
import com.game.uc.Message;
import com.game.util.TimeHelper;
import com.game.worldmap.*;

/**
 * @author cpz
 * @date 2020/10/21 14:28
 * @description
 */
@Controller
@RequestMapping("load")
public class LoadDataServlet {

    /**
     * 重新加载所有配置
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/reloadAll.do", method = RequestMethod.POST)
    public Message reloadAll() {
        Map<String, BaseDataMgr> res = SpringUtil.getApplicationContext().getBeansOfType(BaseDataMgr.class);
        List<Object> errorList = new ArrayList<>();
        for (Map.Entry<String, BaseDataMgr> mgr : res.entrySet()) {
            try {
                mgr.getValue().init();
            } catch (Exception e) {
                errorList.add(mgr.getKey());
            }
        }
        //重新加载下活动缓存
        SpringUtil.getBean(ActivityService.class).activityRewardLogic();
        if (errorList.size() > 0) {
            Message message = new Message("error", new JSONArray(errorList).toJSONString());
            message.setCode(1);
            return message;
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    /**
     * 加载指定配置
     *
     * @param table
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/reloadTable.do", method = RequestMethod.GET)
    public Message reloadTable(String table) {

        Map<String, BaseDataMgr> res = SpringUtil.getApplicationContext().getBeansOfType(BaseDataMgr.class);
        BaseDataMgr mgr = res.get(table);
        List<Object> errorList = new ArrayList<>();
        if (mgr != null) {
            try {
                mgr.init();
            } catch (Exception e) {
                errorList.add(table);
            }
        }
        if (errorList.size() > 0) {
            return new Message("error", new JSONArray(errorList).toJSONString());
        }
        return new Message(UcCodeEnum.SUCCESS);
    }


    /**
     * 加载指定配置
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/reloadCommand.do", method = RequestMethod.POST)
    public Message reloadCommand(int id, int cmd, String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return new Message(UcCodeEnum.ERROR);
        }
        GameServer.getInstance().messagePool.registerC(id, cmd, clazz);
        return new Message(UcCodeEnum.SUCCESS);
    }


    /**
     * 刷新活动时间
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refushWorldActivity.do", method = RequestMethod.POST)
    public Message refushWorldActivity(int activityId, String endTime, int state) {
        WorldActPlanService service = SpringUtil.getBean(WorldActPlanService.class);
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(activityId);
        if (worldActPlan != null) {
            long time = TimeHelper.dateToStampY(endTime);
            worldActPlan.setEndTime(time);
            worldActPlan.setState(state);
            service.activityEnd(worldActPlan);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    /**
     * 刷新活动时间
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refushMission", method = RequestMethod.POST)
    public Message refushMission(int missionId, int preMissionId) {
        if (missionId == 0) {
            return new Message(UcCodeEnum.ERROR);
        }
        MissionService service = SpringUtil.getBean(MissionService.class);
        service.refushAllPlayerMission(missionId, preMissionId);
        return new Message(UcCodeEnum.SUCCESS);
    }

    @Deprecated
    @ResponseBody
    @RequestMapping(value = "/sendTitalReward", method = RequestMethod.POST)
    public Message sendTitalReward(String title, String content, String text) {
        CountryService service = SpringUtil.getBean(CountryService.class);
        SpringUtil.getBean(PlayerManager.class).getAllPlayer().values().forEach(e -> {
            service.sendTitalReward(e, title, content, text);
        });
        return new Message(UcCodeEnum.SUCCESS);
    }

    /**
     * 刷新活动时间
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/refushResource", method = RequestMethod.POST)
    public Message refushResource() {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        playerManager.getAllPlayer().values().forEach(e -> {
            playerManager.checkResource(e);
        });
        return new Message(UcCodeEnum.SUCCESS);
    }

    /**
     * 刷新活动时间
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/clearMarch", method = RequestMethod.POST)
    public Message clearMarch() {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        WarManager warManager = SpringUtil.getBean(WarManager.class);
        MapInfo mmm = worldManager.getMapInfo(1);

//        Iterator itt = mmm.getBigMonsterWarMap().column(3).values().iterator();
//        while (itt.hasNext()) {
//            itt.remove();
//        }
//
//        playerManager.getAllPlayer().values().forEach(e -> {
//            Iterator<March> it = e.getMarchList().iterator();
//            while (it.hasNext()) {
//                March m = it.next();
//                if (m.getMarchType() == MarchType.BigWar) {
//                    int mapId = worldManager.getMapId(m.getEndPos());
//                    MapInfo mapInfo = worldManager.getMapInfo(mapId);
//                    BigMonster bigMonster = mapInfo.getBigMonsterMap().get(m.getEndPos());
//                    if (bigMonster == null) {
//                        warManager.handleMarchReturn(m, MarchReason.CANCEL_BIGMONSTER_BACK);
//                        worldManager.synMarch(mapId, m);
//                        WarInfo warInfo = mapInfo.getBigMonsterWarMap().get(m.getStartPos(), e.getCountry());
//                        if (warInfo != null) {
//                            Iterator<March> wit = warInfo.getAttackMarches().iterator();
//                            while (wit.hasNext()) {
//                                March mm = it.next();
//                                if (mm.getKeyId() == m.getKeyId()) {
//                                    wit.remove();
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        });

		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/reloadGroup.do", method = RequestMethod.POST)
	public Message reloadGroup() {
		StaticLimitMgr bean = SpringUtil.getBean(StaticLimitMgr.class);
		bean.initGroup();
		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/reloadSensitiveWord.do", method = RequestMethod.POST)
	public Message reloadSensitiveWord() {
		StaticSensitiveWordMgr bean = SpringUtil.getBean(StaticSensitiveWordMgr.class);
		bean.initSenstiveWord();
		return new Message(UcCodeEnum.SUCCESS);
	}
}
