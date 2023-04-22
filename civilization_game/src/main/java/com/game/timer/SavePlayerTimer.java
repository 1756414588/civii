package com.game.timer;

import com.game.constant.BuildingType;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.Role;
import com.game.domain.p.Account;
import com.game.domain.p.Lord;
import com.game.manager.PlayerManager;
import com.game.manager.PublicDataManager;
import com.game.server.datafacede.SavePlayerServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 每隔五分钟存储下登陆过的用户数据
 */
public class SavePlayerTimer extends TimerEvent {

    public SavePlayerTimer() {
        super(-1, 30 * TimeHelper.SECOND_MS);
    }

    @Override
    public void action() {
        saveTimerLogic();
        cleanExpirePlayer();
        publilcDataUpdate();
    }


    // 定时保存玩家数据
    public void saveTimerLogic() {
        SavePlayerServer savePlayerServer = SpringUtil.getBean(SavePlayerServer.class);
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);

        long now = System.currentTimeMillis();
        long updateTime = now - 3 * 60 * 1000;

        List<Long> list = Lists.newArrayList(playerManager.getLoginCache().keySet());
        Iterator<Long> it = list.iterator();
        while (it.hasNext()) {
            long roleId = it.next();
            Player player = playerManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }

            if (player.lastSaveTime > updateTime) {
                continue;
            }

            try {
                player.lastSaveTime = now;
                savePlayerServer.saveData(new Role(player));

                //离线玩家移除出更新队列
                if (!player.isLogin) {
                    it.remove();
                    //从队列中移除
                    playerManager.getLoginCache().remove(roleId);
                }
            } catch (Exception e) {
                LogHelper.ERROR_LOGGER.error("SavePlayerTimer cause:{}", e.getMessage(), e);
            }
        }
    }

    public void cleanExpirePlayer() {
        StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        long now = System.currentTimeMillis();
        long cleanAccountBegin = TimeHelper.getTimeOfDay(PlayerManager.CLEAN_ACCOUNT_BEGIN);
        long cleanAccountEnd = TimeHelper.getTimeOfDay(PlayerManager.CLEAN_ACCOUNT_END);
        //清理账号配置规则
        List<List<Integer>> cleanAccount = staticLimitMgr.getCleanAccount();
        //到了清理时间阶段
        if (null != cleanAccount && now <= cleanAccountEnd && now >= cleanAccountBegin) {
            Map<Long, Player> players = playerManager.getPlayers();
            Iterator<Player> iterator = players.values().iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                boolean clean = isClean(cleanAccount, player);
                if (clean) {
                    playerManager.cleanPlayer(player);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 根据规则判定改账号是否清理
     *
     * @param cleanAccount
     * @param player
     * @return
     */
    private boolean isClean(List<List<Integer>> cleanAccount, Player player) {
        Lord lord = player.getLord();
        Account account = player.account;
        if (lord == null || account == null) {
            return false;
        }
        for (List<Integer> clean : cleanAccount) {
            if (clean.size() >= 3) {
                int vip = lord.getVip();// VIP等级
                int commandLv = player.getBuildingLv(BuildingType.COMMAND);// 主城等级
                Date loginDate = account.getLoginDate();// 登录日期
                if (loginDate == null) {
                    break;
                }
                //上次登录时间距离当前时间的小时
                int dacey = TimeHelper.getDifferHours(loginDate.getTime(), System.currentTimeMillis());
                //清理账号条件设置(addtion配置解释:主城等级(等于),离线时长(大于等于),vip等级(小于)]
                if (vip < clean.get(2) && commandLv == clean.get(0) && dacey >= clean.get(1)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 每个小时 数据入库
    public void publilcDataUpdate() {
        PublicDataManager dataManager = SpringUtil.getBean(PublicDataManager.class);
        long now = System.currentTimeMillis();
        if ((dataManager.getPublicData().getLastSaveTime() - now) > TimeHelper.HOUR_MS) {
            dataManager.update();
        }
    }


}
