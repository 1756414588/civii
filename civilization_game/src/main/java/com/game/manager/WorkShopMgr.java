package com.game.manager;

import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.game.constant.SimpleId;
import com.game.dataMgr.StaticLimitMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.ItemQuality;
import com.game.dataMgr.StaticWorkShopMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BuildQue;
import com.game.domain.p.Building;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.WorkShop;
import com.game.domain.p.WsWaitQue;
import com.game.domain.p.WsWorkQue;
import com.game.domain.s.StaticWorkShop;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.util.TimeHelper;

// 作坊管理器
@Component
public class WorkShopMgr {
    @Autowired
    private StaticWorkShopMgr staticWorkShopMgr;

    @Autowired
    private ItemManager itemManager;

    @Autowired
    private CityManager cityManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private TechManager techManager;

    public StaticWorkShop getStaticWorkShop(int workShopLv) {
        return staticWorkShopMgr.getWorkShop(workShopLv);
    }

    public int getLevel(int quality) {
        Map<Integer, Integer> qualityMap = staticWorkShopMgr.getQualityMap();
        Integer level = qualityMap.get(quality);
        if (level == null) {
            LogHelper.CONFIG_LOGGER.error("quality = " + quality + " not exists!");
            return 0;
        }

        return level;
    }

    public boolean isQualityOk(int quality) {
        int level = getLevel(quality);
        if (level == 0) {
            LogHelper.CONFIG_LOGGER.error("isQualityOk : level is 0.");
            return false;
        }

        return true;
    }

    // 检查图纸是否合法: 图纸品质为当前材料品质-1
    public boolean isQualityOk(int itemId, int quality) {
        int paperQuality = quality - 1;
        int itemQuality = itemManager.getQuality(itemId);
        if (itemManager.getQuality(itemId) != paperQuality) {
            LogHelper.CONFIG_LOGGER.error("itemQuality = " + itemQuality + ", paperQuality = " + paperQuality);
            return false;
        }

        return true;
    }

    // 检查物品数量对不对
    public boolean isItemNumOk(Player player, int itemId) {
        Item item = player.getItem(itemId);
        if (item == null) {
            return false;
        }

        return item.getItemNum() >= 1;
    }

    // 检查司令部等级和是否充足
    public boolean isCommandLvOk(Player player, StaticWorkShop workShop) {
        return player.getCommandLv() >= workShop.getCommandLv();
    }

    // 检查玩家等级是否充足
    public boolean isLordLvOk(Player player, StaticWorkShop workShop) {
        return player.getLevel() >= workShop.getLevel();
    }

    // 从lootList里面随机掉落一个物品，在从数量里面随机掉落一个数量
    public List<Award> lootAwardList(Player player, StaticWorkShop workShop) {
        List<Award> awards = new ArrayList<Award>();
        List<List<Integer>> lootList = workShop.getLootList();
        if (lootList == null) {
            LogHelper.CONFIG_LOGGER.error("lootList is null");
            return awards;
        }

        if (lootList.size() <= 0) {
            LogHelper.CONFIG_LOGGER.error("lootList.size() <= 0");
            return awards;
        }

        Random random = new Random(System.currentTimeMillis());
        int randNum = RandomHelper.randomInSize(1000);
        int total = 0;
        int lootNum;
        for (List<Integer> item : lootList) {
            if (item == null || item.size() != 4) {
                LogHelper.CONFIG_LOGGER.error("item is null or item.size() != 4");
                return awards;
            }

            total += item.get(3);
            // 掉落成功
            if (randNum < total) {
                lootNum = lootNum(workShop.getLootRate(), random);
                awards.add(new Award(player.maxKey(), AwardType.PROP, item.get(0), lootNum));
                break;
            }
        }

        return awards;

    }

    public int lootNum(List<List<Integer>> lootRate, Random random) {
        if (lootRate == null) {
            LogHelper.CONFIG_LOGGER.error("lootRate == null");
            return 0;
        }

        if (lootRate.size() != 5) {
            LogHelper.CONFIG_LOGGER.error("lootRate.size() != 5");
            return 0;
        }

        int randNum = RandomHelper.randomInSize(1000);
        int total = 0;
        for (List<Integer> lootNum : lootRate) {
            if (lootNum == null || lootNum.size() != 2) {
                LogHelper.CONFIG_LOGGER.error("lootNum is null or lootNum.size() != 4");
                return 0;
            }

            total += lootNum.get(1);
            if (randNum < total) {
                return lootNum.get(0);
            }
        }
        return 0;
    }

    public int getTotalQue(Player player) {
        Building building = player.buildings;
        if (building == null) {
            LogHelper.CONFIG_LOGGER.error("building is null");
            return 0;
        }

        WorkShop workShop = building.getWorkShop();
        if (workShop == null) {
            LogHelper.CONFIG_LOGGER.error("work shop is null!");
            return 0;
        }

        int workShopLv = workShop.getLv();
        int buyTimes = player.getLord().getBuyWorkShopQue();

        return workShopLv + buyTimes + SpringUtil.getBean(StaticLimitMgr.class).getNum(SimpleId.WORK_SHOP_ADDITIONAL_COUNT);
    }

    // 获取当前减少的时间(单位毫秒)
    public long getTimeDelta(Player player, int workQueNum) {
        workQueNum = Math.max(1, workQueNum); // 最小应该是1
        int people = player.getPeople();
        int max = staticWorkShopMgr.getLimitPeople(player.getCommandLv());
        int currentPeople = Math.min(people, max);

        // 国家人口
        int countryPeople = cityManager.getCountryPeople(player.getCountry());

        int total = currentPeople + countryPeople;
        return (total / workQueNum) * TimeHelper.SECOND_MS;
    }

    // 获取当前时间
    public long getTime(StaticWorkShop workShop, int quality) {
        int index = 0;
        if (quality == ItemQuality.GREEN) {
            index = 0;
        } else if (quality == ItemQuality.GOLD) {
            index = 1;
        } else if (quality == ItemQuality.RED) {
            index = 2;
        } else {
            LogHelper.ERROR_LOGGER.error("quality is error, quality = " + quality);
            index = 0;
        }

        List<Integer> times = workShop.getTime();
        if (times == null || times.size() != workShop.getLevel()) {
            LogHelper.ERROR_LOGGER.error("times == null || times.size() != workShopLv!");
            return 0L;
        }

        if (index >= times.size()) {
            LogHelper.ERROR_LOGGER.error("index >= times.size() - 1, index = " + index + ", size " + times.size() + ",quality = " + quality);
            return 0L;
        }

        Integer time = times.get(index);
        if (time == null) {
            LogHelper.ERROR_LOGGER.error("time == null getTime!");
            return 0L;
        }

        return (long) time * TimeHelper.SECOND_MS;
    }

    public int getPeople(Player player) {
        Lord lord = player.getLord();
        if (lord == null) {
            return 0;
        }

        return lord.getPeople();
    }

    public int getWorkQueNum(Map<Integer, WsWorkQue> workQues) {
        if (workQues == null) {
            return 0;
        }
        int workQueNum = 0;
        long now = System.currentTimeMillis();
        for (WsWorkQue wsWorkQue : workQues.values()) {
            if (wsWorkQue == null) {
                continue;
            }

            if (wsWorkQue.getEndTime() > now) {
                ++workQueNum;
            }

        }

        return workQueNum;
    }

    public Integer getMinWorkShop(WorkShop workShop, int maxIndex) {
        Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
        for (int i = 1; i <= maxIndex; i++) {
            int k = i;
            boolean isHave = workQues.values().stream().anyMatch(e -> e.getIndex() == k);
            if (!isHave) {
                return i;
            }
        }
        return 0;
    }


    public WsWorkQue getWorkShop(WorkShop workShop, int index) {
        Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
        for (WsWorkQue wsWorkQue : workQues.values()) {
            if (wsWorkQue == null) {
                continue;
            }

            if (wsWorkQue.getIndex() == index) {
                return wsWorkQue;
            }
        }

        return null;
    }

    // 返回毫秒
    public long getWorkTime(long time, Player player) {
        // LogHelper.GAME_DEBUG.error("生产队列时间的时间为:" + time);
        // 活动减半
        float actFactor = activityManager.actDouble(ActivityConst.ACT_WORK_SPEED);
        float leftFactor = 1 - actFactor;
        //活动减半后的时间
        float resTime = time * leftFactor;

        // 科技时间
        float techAdd = techManager.getWorkTime(player);
        leftFactor = 1 - techAdd;
        //科技减半后的时间
        resTime = resTime * leftFactor;

        resTime = Math.max(0L, resTime);

        return Float.valueOf(resTime).longValue();
    }

    public float getWorkFactor(Player player) {
        // 活动减半
        float actFactor = activityManager.actDouble(ActivityConst.ACT_WORK_SPEED);
        // LogHelper.GAME_DEBUG.error("活动减少的耗时加成: " + actFactor);

        // 科技时间
        float techAdd = techManager.getWorkTime(player);

        return actFactor + techAdd;
    }

    // 重新整理时间
    public void checkWorkQue(WorkShop workShop, long deltaTime, Player player) {
        Map<Integer, WsWaitQue> WsWaitQueMap = workShop.getWaitQues();
        Map<Integer, WsWorkQue> workQues = workShop.getWorkQues();
        // 更新作坊减少的人口时间
        long now = System.currentTimeMillis();
        for (Map.Entry<Integer, WsWorkQue> elem : workQues.entrySet()) {
            if (elem == null) {
                continue;
            }

            WsWorkQue workQueElem = elem.getValue();
            if (workQueElem == null) {
                continue;
            }

            // 去掉已经完成的队列
            if (workQueElem.getIndex() == 0) {
                continue;
            }

            // 上次人口减少时间 - 当前人口减少时间
            float removeFactor = 1 - getWorkFactor(player);
            removeFactor = Math.max(0.0f, removeFactor);
            removeFactor = Math.min(1.0f, removeFactor);
            long leftTime = workQueElem.getEndTime() - now;
            leftTime = Math.max(0, leftTime);
            long reducePeople = workQueElem.getReduceTime() - deltaTime;
            reducePeople = Math.max(0, reducePeople);
            long resTime = (long) (leftTime + reducePeople * removeFactor);
            if (resTime <= 0) {
                resTime = 0;
            }
            resTime = Math.max(0, resTime);
            workQueElem.setEndTime(now + resTime);
//            workQueElem.setPeriod(resTime);
            workQueElem.setReduceTime(deltaTime);

            WsWaitQue wsWaitQue = WsWaitQueMap.get(workQueElem.getIndex());
            if (wsWaitQue != null) {
                wsWaitQue.setStartTime(workQueElem.getEndTime());
            }
        }
    }

    public int getWorkShopLv(Player player) {
        Building building = player.buildings;
        if (building == null) {
            return 0;
        }
        WorkShop workShop = building.getWorkShop();
        if (workShop == null) {
            return 0;
        }

        return workShop.getLv();
    }

    // 生产的时候能升级建筑
    public boolean workShopCanUp(Player player) {
        // 检查是否在生产
        Building building = player.buildings;
        if (building == null) {
            return false;
        }

        WorkShop workShop = building.getWorkShop();
        if (workShop == null) {
            return false;
        }

        Map<Integer, WsWorkQue> wsWorkQueMap = workShop.getWorkQues();
        if (wsWorkQueMap.isEmpty()) {
            return true;
        }

        return false;
    }

    // 升级建筑的时候能生产
    public boolean workShopcanMake(Player player) {
        // 检查是否在升级建筑
        Building building = player.buildings;
        if (building == null) {
            return false;
        }

        WorkShop workShop = building.getWorkShop();
        if (workShop == null) {
            return false;
        }

        int buildingId = workShop.getBuildingId();

        ConcurrentLinkedDeque<BuildQue> buildQues = building.getBuildQues();
        for (BuildQue buildQue : buildQues) {
            if (buildQue.getBuildingId() == buildingId) {
                return false;
            }
        }

        return true;
    }

}
