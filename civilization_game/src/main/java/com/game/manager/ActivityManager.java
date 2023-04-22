package com.game.manager;

import com.game.Loading;
import com.game.activity.ActivityEventManager;
import com.game.activity.actor.CommonTipActor;
import com.game.activity.define.EventEnum;
import com.game.activity.actor.ActServerCensusActor;
import com.game.constant.*;
import com.game.dao.p.ActivityDao;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticFriendMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticTDTaskMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.LoadData;
import com.game.domain.ActivityData;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.consumer.EventManager;
import com.game.pb.ActivityPb;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ActivityCondState;
import com.game.server.GameServer;
import com.game.service.ActivityService;
import com.game.service.AutoService;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.game.worldmap.WarInfo;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;

@Component
@LoadData(name = "活动管理", type = Loading.LOAD_USER_DB, initSeq = 2500)
public class ActivityManager extends BaseManager{

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private StaticActivityMgr staticActivityMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    @Autowired
    private EquipManager equipManager;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private StaticLimitMgr limitMgr;

    @Autowired
    private StaticFriendMgr staticFriendMgr;

    @Autowired
    private WorldBoxManager worldBoxManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private AutoService autoService;
    @Autowired
    private ActivityService activityService;

    @Autowired
    private ServerManager serverManager;

    @Autowired
    ActivityEventManager activityEventManager;

    private Map<Integer, ActivityData> activityMap = new HashMap<Integer, ActivityData>();

    public Map<Integer, ActivityData> getActivityMap() {
        return activityMap;
    }


    @Override
    public void init() throws Exception {
        initActivity();
        activityService.activityRewardLogic();


        // checkSeven
        checkSeven();
        serverManager.updateBootStrap("activity");
    }

    public void initActivity() throws InvalidProtocolBufferException {
        List<Activity> list = activityDao.selectActivityList();
        for (Activity e : list) {
            ActivityData activityData = new ActivityData(e);
            activityMap.put(e.getActivityId(), activityData);
        }
    }

    public void update(Activity activity) {
        if (activityDao.updateActivity(activity) == 0) {
            activityDao.insertActivity(activity);
        }
    }

    public void activityTimer() {
        long time = System.currentTimeMillis();
        Iterator<ActivityData> it = activityMap.values().iterator();
        while (it.hasNext()) {
            ActivityData next = it.next();
            if (next.lastSaveTime <= time) {
                update(next.copyData());
                next.lastSaveTime = time + 5 * 60 + 1000L;
            }
        }
    }

    /**
     * 活动
     *
     * @param activityId
     * @return
     */
    public ActivityBase getActivityBase(int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return null;
        }
        return activityBase;
    }

    /**
     * 获取个人活动记录结构体
     *
     * @param player
     * @param activityId
     * @return
     */
    public ActRecord getActivityInfo(Player player, int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return null;
        }
        return getActivityInfo(player, activityBase);
    }

    /**
     * 获取个人活动记录结构体
     *
     * @param player
     * @param activityBase
     * @return
     */
    public ActRecord getActivityInfo(Player player, ActivityBase activityBase) {
        int keyId = activityBase.getActivityId();
        Date beginTime = activityBase.getBeginTime();
        int begin = TimeHelper.getDay(beginTime);
        ActRecord activity = null;
        if (!player.activitys.containsKey(keyId)) {
            activity = new ActRecord(activityBase, begin);
            player.activitys.put(keyId, activity);
        } else {
            activity = player.activitys.get(keyId);
            boolean flag = true;
            if ((keyId == ActivityConst.ACT_PASS_PORT || keyId == ActivityConst.ACT_LOGIN_SEVEN) && player.getLord().getMergeServerStatus() == 1) {
                activity.setBeginTime(begin);
                activity.setCleanTime(begin);
            }
            if (activityBase.getActivityId() != ActivityConst.ACT_BUILD_QUE) {
                flag = activity.isReset(begin);// 是否重新设置活动
            }
            // 判定是否清理
            ActRecord record = activity.clone();
            boolean isClean = activity.autoDayClean(activityBase);
            if (isClean) {
                // 充值有礼需要发送下奖励
                if (activity.getActivityId() == ActivityConst.ACT_TOPUP_PERSON) {
                    activityService.sendActTopUpPerson(player, activityBase, record);
                }
            }
            if (flag || activity.getAwardId() != activityBase.getAwardId()) {// 新开启活动则有tips
                activity.setAwardId(activityBase.getAwardId());
            } else {// 再次点击活动
                if (activity.isNew()) {
                    activity.setNew(false);
                }
            }
        }
        return activity;
    }

    /**
     * 获取全服共同参与的活动结构体
     *
     * @param activityId
     * @return
     */
    public ActivityData getActivity(int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return null;
        }
        return getActivity(activityBase);
    }

    /**
     * 获取全服共同参与的活动结构体
     *
     * @param activityBase
     * @return
     */
    public ActivityData getActivity(ActivityBase activityBase) {
        int keyId = activityBase.getActivityId();
        Date beginTime = activityBase.getBeginTime();
        // 活动开启时间
        int begin = TimeHelper.getDay(beginTime);
        ActivityData activity = null;
        if (!activityMap.containsKey(keyId)) {
            activity = new ActivityData(activityBase, begin);
            activityMap.put(keyId, activity);
        } else {
            activity = activityMap.get(keyId);
            if (activity.isReset(begin)) {// 重置,重新开启活动
                activity.setAwardId(activityBase.getAwardId());
            }
        }
        return activity;
    }

    /**
     * 刷新个人活动记录数据
     *
     * @param actRecord
     */
    public void refreshDay(ActRecord actRecord) {
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        actRecord.autoDayClean(activityBase);
    }

    /**
     * 个人活动记录值更新
     *
     * @param player
     * @param activityId
     * @param schedule
     * @param sortId
     */
    public boolean updActPerson(Player player, int activityId, long schedule, int sortId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }
        return updActPerson(player, activityBase, schedule, sortId);
    }

    /**
     * 个人活动记录值更新
     *
     * @param player
     * @param activityBase
     * @param schedule
     * @param sortId
     */
    public boolean updActPerson(Player player, ActivityBase activityBase, long schedule, int sortId) {
        try {
            ActRecord activity = getActivityInfo(player, activityBase);
            // 物质搜寻特殊处理，按天存放 当天的活动完成度
            if (activityBase.getActivityId() == ActivityConst.ACT_SEARCH) {
                if (!DateHelper.isSameDate(new Date(), player.account.getLoginDate())) {
                    player.account.setLoginDays(player.account.getLoginDays() + 1);
                    player.account.setLoginDate(new Date());
                    reflushSeven(player.roleId, ActivityConst.ACT_SEARCH);
                }
                sortId = activity.getCount();
            }
            long state = activity.getStatus(sortId);
            state = state + schedule;
            activity.putState(sortId, state);
            if (state == 0) {
                return false;
            }

            // 幸运砸蛋,夺宝奇兵,好运转盘活动界面充值得次数刷新,需要SynActivity协议
            if (activityBase.getActivityId() == ActivityConst.ACT_RAIDERS || activityBase.getActivityId() == ActivityConst.ACT_LUCKLY_EGG || activityBase.getActivityId() == ActivityConst.RE_DIAL) {
                activityEventManager.activityTip(player, activity, activityBase);
                playerManager.synActivity(player, activityBase.getActivityId(), 0);
            }

            if (activityBase.getActivityId() == ActivityConst.ACT_SCENE_CITY || activityBase.getActivityId() == ActivityConst.LUCK_DIAL) {
                activityEventManager.activityTip(player, activity, activityBase);
            }

            /***** 排行类活动 *****/

            int staticRank = activityBase.getStaticActivity().getRank();
            if (staticRank == ActivityConst.RANK_1) {
                long lordId = player.getLord().getLordId();
                ActivityData activityData = getActivity(activityBase);
                activityData.addPlayerRank(lordId, state);

                // 记录排行在活动开启的当天7点钟之后
                // 当前排名小于历史排名,则替换历史排名
                int beginDay = TimeHelper.getDay(activityBase.getBeginTime());
                int today = TimeHelper.getDay(new Date());
                if (beginDay != today || TimeHelper.getCurrentHour() >= ActivityConst.RANK_HOUR) {
                    ActPlayerRank actRank = activityData.getLordRank(lordId);// 当前排名
                    long historyRank = activityData.getStatus(lordId);
                    if (actRank != null && (historyRank == 0 || historyRank > actRank.getRank())) {
                        activityData.putState(lordId, actRank.getRank());
                        activityEventManager.activityTip(player, activity, activityBase);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 更新个人排行
     *
     * @param player
     * @param activityId
     * @param max
     * @param schedule
     * @param sortId
     */
    public void updActPersonRank(Player player, int activityId, int max, long schedule, int sortId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }

        if (!player.isActive()) {
            return;
        }

        int staticRank = activityBase.getStaticActivity().getRank();
        if (staticRank != ActivityConst.RANK_2) {
            return;
        }
        ActivityData activityData = getActivity(activityBase);
        activityData.addPlayerRank(player.getLord().getLordId(), max, schedule);
    }

    /**
     * 全服共同参与活动累计值记录更新
     *
     * @param activityId
     * @param schedule
     * @param sortId
     */
    public void updActServer(int activityId, long schedule, int sortId) {
        try {

            ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
            if (activityBase == null) {
                return;
            }

            int step = activityBase.getStep();
            if (step != ActivityConst.ACTIVITY_BEGIN) {
                return;
            }

            if (activityBase.getStaticActivity().getAddUp() == 0) {
                return;
            }

            List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
            if (condList == null || condList.size() == 0) {
                return;
            }

            ActivityData actData = getActivity(activityBase);

            Map<Integer, Boolean> map = new HashMap<>();
            for (StaticActAward award : condList) {
                int status = (int) actData.getStatus(award.getSortId());
                if (status >= award.getCond()) {
                    map.put(award.getKeyId(), true);
                }
            }

            long state = actData.getStatus(sortId);
            actData.putState(sortId, state + schedule);

            // 判定是否开启了某个奖励
            boolean newOpen = false;
            for (StaticActAward award : condList) {
                int status = (int) actData.getStatus(award.getSortId());
                if (status >= award.getCond()) {
                    if (!map.containsKey(award.getKeyId())) {
                        newOpen = true;
                        break;
                    }
                }
            }
            if (newOpen) {
                activityEventManager.activityTip(EventEnum.ACT_SERVER, new ActServerCensusActor(activityBase, actData));
            }

        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 全服返利
     *
     * @param player
     * @param activityId
     * @param schedule
     * @param sortId
     */
    public void updActServerReMoney(Player player, int activityId, long schedule, int sortId) {
        try {

            ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
            if (activityBase == null) {
                return;
            }

            int step = activityBase.getStep();
            if (step != ActivityConst.ACTIVITY_BEGIN) {
                return;
            }

            if (activityBase.getStaticActivity().getAddUp() == 0) {
                return;
            }

            // 判定有没有新的红点需要推送
            List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
            if (condList == null || condList.size() == 0) {
                return;
            }

            ActivityData actData = getActivity(activityBase);

            Map<Integer, Boolean> map = new HashMap<>();
            for (StaticActAward award : condList) {
                int status = (int) actData.getStatus(award.getSortId());
                if (status >= award.getCond()) {
                    map.put(award.getKeyId(), true);
                }
            }

            long state = actData.getStatus(sortId);
            actData.putState(sortId, state + schedule);

            int country = player.getCountry();
            long v = actData.getAddtion(country);
            actData.putAddtion(country, v + schedule);

            // 判定是否开启了某个奖励
            boolean newOpen = false;
            for (StaticActAward award : condList) {
                int status = (int) actData.getStatus(award.getSortId());
                if (status >= award.getCond()) {
                    if (!map.containsKey(award.getKeyId())) {
                        newOpen = true;
                        break;
                    }
                }
            }
            if (newOpen) {
                activityEventManager.activityTip(EventEnum.ACT_SERVER, new ActServerCensusActor(activityBase, actData));
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }

    /***
     * 有人VIP状态发送改变 大v带队 判定是否需要重新推送获取活动
     */
    public void actHighVipPush() {
        ActivityData activityData = getActivity(ActivityConst.ACT_HIGH_VIP);
        if (activityData == null) {
            return;
        }
        // 获取奖励
        List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityData.getAwardId());
        if (condList == null || condList.size() == 0) {
            return;
        }
        Map<Integer, Boolean> map = new HashMap<>();
        for (StaticActAward award : condList) {
            int status = (int) activityData.getStatus(award.getSortId());
            if (status >= award.getCond()) {
                map.put(award.getKeyId(), true);
            }
        }
        // 刷新一下
        activityHighVip(activityData.getAwardId(), activityData);
        // 判定是否开启了某个奖励
        boolean newOpen = false;
        for (StaticActAward award : condList) {
            int status = (int) activityData.getStatus(award.getSortId());
            if (status >= award.getCond()) {
                if (!map.containsKey(award.getKeyId())) {
                    newOpen = true;
                    break;
                }
            }
        }
        // 有新开的
        if (newOpen) {
            // 全服统计类活动
            ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_HIGH_VIP);
            activityEventManager.activityTip(EventEnum.ACT_SERVER, new ActServerCensusActor(activityBase, activityData));
        }
    }

    /**
     * 大咖带队
     */
    public void activityHighVip(int awardId, ActivityData activityData) {
        // 建立索引
        long[][] status = new long[13][2];

        // 实例化索引
        List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(awardId);
        for (StaticActAward actAward : actAwardList) {
            String param = actAward.getParam();
            if (param == null || "".equals(param.trim())) {
                continue;
            }
            int vip = Integer.valueOf(param.trim());
            status[vip][0] = actAward.getSortId();
        }

        // 统计VIP人数
        Iterator<Player> it = playerManager.getPlayers().values().iterator();
        while (it.hasNext()) {
            Player next = it.next();
            int vip = next.getVip();
            if (vip < 2) {
                continue;
            }
            status[vip][1] += 1;
        }

        // 大咖带队v2-12
        int total = 0;
        for (int i = 12; i > 1; i--) {
            long sortId = status[i][0];
            total += status[i][1];
            long record = activityData.getStatus(sortId);
            if (total > record) {
                activityData.putState(sortId, total);
            }
        }
    }

    /**
     * 双倍活动
     *
     * @param activityId
     * @return
     */
    public float actDouble(int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return 0.0f;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return 0.0f;
        }
        int awardId = activityBase.getAwardId();

        StaticActDouble actDouble = staticActivityMgr.getActDouble(awardId);
        if (actDouble != null) {
            float a = actDouble.getTwice() / 100f;
            return a;
        }
        return 0.0f;
    }

    /**
     * 官员召唤
     *
     * @return
     */
    public int getActGovernCall() {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_GOVEN_CALL);
        if (activityBase == null) {
            return 0;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return 0;
        }
        return 2;
    }

    /**
     * 活动掉落
     *
     * @param activityId
     * @return
     */
    public List<List<Integer>> actItemDrop(int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return null;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return null;
        }

        StaticActDrop actDrop = staticActivityMgr.getActDrop(activityBase.getAwardId());
        if (actDrop == null) {
            return null;
        }

        List<List<Integer>> dropList = new ArrayList<List<Integer>>();

        int random = RandomHelper.randomInSize(actDrop.getProbability());
        for (List<Integer> e : actDrop.getDropList()) {
            if (e.size() < 4) {
                continue;
            }
            if (random < e.get(3)) {
                dropList.add(e);
            }
        }
        return dropList;
    }

    /**
     * 攻城掠地
     *
     * @param cityId
     * @param gameEntities
     * @param win
     */
    public void updActSceneCity(int cityId, List<BattleEntity> gameEntities, boolean win) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SCENE_CITY);
        if (activityBase == null) {
            return;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }

        // type:1为据点，2为镇，3为城，4为营地，5为市，6为首都，7为名城，8为世界要塞，9为世界要塞
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            return;
        }

        HashSet<Long> players = new HashSet<Long>();
        for (BattleEntity e : gameEntities) {
            if (e.getLordId() != 0) {
                players.add(e.getLordId());
            }
        }

        for (Long lordId : players) {
            Player player = playerManager.getPlayer(lordId);
            if (player == null) {
                continue;
            }
            if (win) {
                updActPerson(player, activityBase, 1, staticWorldCity.getType());
            }
            updActPerson(player, activityBase, 1, 0);
        }
    }

    public void updActCountryRank(WarInfo warInfo) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_COUNTRY_RANK);
        if (activityBase == null) {
            return;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }

        for (Long lordId : warInfo.getAttackerPlayers()) {
            Player player = playerManager.getPlayer(lordId);
            if (player == null) {
                continue;
            }
            updActPerson(player, activityBase, 1, 0);
        }
    }

    /**
     * 特价礼包
     *
     * @param player
     * @param giftId
     * @return
     */
    public boolean actPayGift(Player player, int giftId) {

        StaticActPayGift payGift = staticActivityMgr.getPayGift(giftId);
        if (payGift == null || payGift.getSellList().isEmpty()) {
            return false;
        }
        int actConst = ActivityConst.ACT_FLASH_GIFT;
        // 美女礼包
        int beautyGift = limitMgr.getNum(SimpleId.BEAUTY_GIFT);
        if (giftId == beautyGift) {
            return true;
        }
        switch (giftId) {
            case 8801: // 建造队列
                actConst = ActivityConst.ACT_BUILD_QUE;
                break;
            case 8401: // 双卡礼包
                actConst = ActivityConst.ACT_MONTH_GIFT;
                break;
            case 1281: // 限时礼包
                actConst = ActivityConst.ACT_FLASH_GIFT;
                break;
            default: // 特价礼包
                actConst = ActivityConst.ACT_PAY_GIFT;
                break;
        }

        ActivityBase activityBase = staticActivityMgr.getActivityById(actConst);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);

        long status = actRecord.getStatus(payGift.getPayGiftId());
        if (status >= payGift.getCount()) {
            return false;
        }

        actRecord.putState(payGift.getPayGiftId(), status + 1);
        List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();

        for (List<Integer> e : payGift.getSellList()) {
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            awardList.add(PbHelper.createAward(type, id, count).build());
        }

//		LogHelper.MESSAGE_LOGGER.info("actPayGift");
        activityEventManager.activityTip(EventEnum.ACT_BUY_GIFT, new CommonTipActor(player, actRecord, activityBase));

        SpringUtil.getBean(EventManager.class).join_activity(player, actConst, activityBase.getStaticActivity().getName(), payGift.getPayGiftId());
        SpringUtil.getBean(EventManager.class).complete_activity(player, actConst, activityBase.getStaticActivity().getName(), payGift.getPayGiftId(), activityBase.getBeginTime(), payGift.getSellList());
        return true;
    }

    /**
     * 每日优惠(道具直购)
     *
     * @param player
     * @param giftId
     * @return
     */
    public boolean actPayMoney(Player player, int giftId) {

        StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(giftId);
        if (payMoney == null || payMoney.getSellList().isEmpty()) {
            return false;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAY_PAY);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }

        ActRecord actRecord = getActivityInfo(player, activityBase);
        Map<Integer, Integer> received = actRecord.getReceived();
        if (null != received) {
            if (received.containsKey(giftId)) {
                return false;
            }
        }
        int limit = actRecord.getReceived(giftId);
        actRecord.getReceived().put(giftId, limit + 1);
        playerManager.synActivity(player, ActivityConst.ACT_DAY_PAY, payMoney.getPayMoneyId());
        SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_DAY_PAY, activityBase.getStaticActivity().getName(), giftId);
        SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_DAY_PAY, activityBase.getStaticActivity().getName(), giftId, new Date(), payMoney.getSellList());
        return true;
    }

    /**
     * 特价尊享
     *
     * @param player
     * @param giftId
     * @return
     */
    public boolean actPaySpecial(Player player, int giftId) {

        StaticActPayMoney payMoney = staticActivityMgr.getPayMoney(giftId);
        if (payMoney == null || payMoney.getSellList().isEmpty()) {
            return false;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SPECIAL_GIFT);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }

        ActRecord actRecord = getActivityInfo(player, activityBase);
        if (actRecord == null) {
            return false;
        }
        int limit = actRecord.getReceived(giftId);
        if (limit >= payMoney.getLimit()) {
            return false;
        }
        actRecord.getReceived().put(giftId, limit + 1);
        playerManager.synActivity(player, ActivityConst.ACT_SPECIAL_GIFT, payMoney.getPayMoneyId());
        SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_SPECIAL_GIFT, activityBase.getStaticActivity().getName(), giftId);
        SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_SPECIAL_GIFT, activityBase.getStaticActivity().getName(), giftId, new Date(), payMoney.getSellList());
        return true;
    }

    /**
     * 月卡,季卡
     *
     * @param player
     * @param cardId
     * @return
     */
    public boolean actPayCard(Player player, int cardId) {

        StaticActPayCard payCard = staticActivityMgr.getPayCard(cardId);
        if (payCard == null) {
            return false;
        }
        // 月卡
        Lord lord = player.getLord();
        long endTimeOfDay = TimeHelper.getEndTimeOfDay();

        // List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
        if (cardId == ActMonthlyCard.MONTHLY_CARD.getKey()) {
            if (lord.getMonthCard() == 0 || lord.getMonthCard() < endTimeOfDay) {// 添加30天月卡
                lord.setMonthCard(endTimeOfDay + TimeHelper.MONTH_MS - TimeHelper.DAY_MS);
            } else {// 已有月卡,继续往后延迟30天
                lord.setMonthCard(lord.getMonthCard() + TimeHelper.MONTH_MS);
            }

            playerManager.sendAttachMail(player, Lists.newArrayList(new Award(AwardType.GOLD, 0, payCard.getDiamond())), MailId.MONTH_CARD);
        } else if (cardId == ActMonthlyCard.SEASON_CARD.getKey()) {
            if (lord.getSeasonCard() == 0 || lord.getSeasonCard() < endTimeOfDay) {// 添加90天季卡
                lord.setSeasonCard(endTimeOfDay + TimeHelper.SEASON_MS - TimeHelper.DAY_MS);
            } else {// 已有季卡,继续往后延迟90天
                lord.setSeasonCard(lord.getSeasonCard() + TimeHelper.SEASON_MS);
            }

            /*
             * for (List<Integer> e : payCard.getSellList()) { int type = e.get(0); int id = e.get(1); int count = e.get(2); awardList.add(PbHelper.createAward(type, id, count).build()); }
             */
            playerManager.sendAttachMail(player, Lists.newArrayList(new Award(AwardType.GOLD, 0, payCard.getDiamond())), MailId.SEASON_CARD);
        } else if (cardId == ActMonthlyCard.IRON_CARD.getKey() || cardId == ActMonthlyCard.COPPER_CARD.getKey() || cardId == ActMonthlyCard.OIL_CARD.getKey() || cardId == ActMonthlyCard.STONE_CARD.getKey()) {
            long cardExpireTime = player.getWeekCard().getExpireTime(payCard.getAwardId());
            if (cardExpireTime == 0 || cardExpireTime < endTimeOfDay) {// 添加周卡
                player.getWeekCard().setExpireTime(payCard.getAwardId(), endTimeOfDay + TimeHelper.WEEK_MS - TimeHelper.DAY_MS);
            } else {// 已有周卡,继续往后延迟7天
                player.getWeekCard().setExpireTime(payCard.getAwardId(), cardExpireTime + TimeHelper.WEEK_MS);
            }
            List<Award> awards = new ArrayList<>();
            for (List<Integer> list : payCard.getSellList()) {
                awards.add(new Award(list.get(0), list.get(1), list.get(2)));
            }

            // 立即发送钻石邮件

            playerManager.sendAttachMail(player, Lists.newArrayList(new Award(AwardType.GOLD, 0, payCard.getDiamond())), MailId.WEEK_CARD_AWARD, String.valueOf(payCard.getPayCardId()), String.valueOf(payCard.getPayCardId()));
            // liji发送当日奖励
            playerManager.sendAttachMail(player, awards, MailId.WEEK_CARD_SEASON, String.valueOf(payCard.getPayCardId()), String.valueOf(payCard.getPayCardId()), String.valueOf(payCard.getPeriod()));
        } else if (cardId == ActMonthlyCard.AUTO_CARD.getKey()) {
            long periodTime = payCard.getPeriod() * TimeHelper.DAY_MS;
            playerManager.addEffect(player, LordPropertyType.AUTO_KILL_MONSTGER, 0, periodTime / TimeHelper.SECOND_MS);
            autoService.pushAutoKillMsg(player);
            playerManager.sendAttachMail(player, Lists.newArrayList(new Award(AwardType.GOLD, 0, payCard.getDiamond())), MailId.WEEK_CARD_AWARD, String.valueOf(payCard.getPayCardId()), String.valueOf(payCard.getPayCardId()));
        }

        playerManager.synActivity(player, ActivityConst.ACT_MONTH_CARD, payCard.getPayCardId());
        String name = payCard.getName();
        SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_MONTH_CARD, payCard.getName(), cardId);
        SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_MONTH_CARD, name, cardId, new Date(), Lists.newArrayList());
        return true;
    }

    /**
     * 每日连续充值
     *
     * @param player
     * @param topup
     * @return
     */
    public boolean actPayEveryDay(Player player, int topup) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_CONTINUOUS_RECHARGE);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);

        int sortId = DateHelper.dayiy(activityBase.getBeginTime(), new Date());
        long status = actRecord.getStatus(sortId);
        actRecord.putState(sortId, status + topup);
        List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
        if (null == condList || condList.size() == 0) {
            return false;
        }
        for (StaticActAward e : condList) {
            int keyId = e.getKeyId();
            int staticSortId = e.getSortId();
            if (staticSortId == sortId) {
                int state = (int) status + topup;
                if (!actRecord.getReceived().containsKey(keyId) && e.getCond() <= state) {// 已领取奖励
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 七日狂欢活动
     *
     * @param player
     * @param sortId
     * @param state
     * @param param
     */
    public void updActSeven(Player player, int type, int sortId, int param, int state) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SEVEN);
        if (activityBase == null) {
            return;
        }
        Date createDate = player.account.getCreateDate();
        int dayiy = DateHelper.dayiy(createDate, new Date());
        if (dayiy > 7) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);
        if (actRecord == null) {
            return;
        }
        if (type == ActivityConst.TYPE_SET) {
            actRecord.putState(sortId, state);
        } else if (type == ActivityConst.TYPE_ADD) {
            long status = actRecord.getStatus(sortId);
            actRecord.putState(sortId, state + status);
        }
        activityEventManager.activityTip(player, actRecord, activityBase);
    }

    /**
     * 玩家登陆时
     *
     * @param player
     * @param type
     * @param sortId
     * @param param
     * @param state
     */
    public void enterActSeven(Player player, int type, int sortId, int param, int state) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SEVEN);
        if (activityBase == null) {
            return;
        }
        Date createDate = player.account.getCreateDate();
        int dayiy = DateHelper.dayiy(createDate, new Date());
        if (dayiy > 7) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);
        if (actRecord == null) {
            return;
        }
        if (type == ActivityConst.TYPE_SET) {
            actRecord.putState(sortId, state);
        } else if (type == ActivityConst.TYPE_ADD) {
            long status = actRecord.getStatus(sortId);
            actRecord.putState(sortId, state + status);
        }
        activityEventManager.activityTip(player, actRecord, activityBase);
    }

    /**
     * 世界征战活动
     *
     * @param player
     * @param sortId
     * @param state
     * @param param
     */
    public void updActWorldBattle(Player player, int type, int sortId, int param, int state) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_WORLD_BATTLE);
        if (activityBase == null) {
            return;
        }
        Date now = new Date();
        if (now.before(activityBase.getBeginTime())) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);
        if (actRecord == null) {
            return;
        }
//        playerManager.synActivity(player, 0, 0);
        if (type == ActivityConst.TYPE_SET) {
            actRecord.putState(sortId, state);
        } else if (type == ActivityConst.TYPE_ADD) {
            long status = actRecord.getStatus(sortId);
            actRecord.putState(sortId, state + status);
        }
    }

    public void updActWashEquip(Player player, int type, long sortId, int state, int activityId) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_WASH_EQUIP);
        if (actRecord != null) {
            long timeKey = 500;
            Date now = new Date();
            Date pre = new Date();
            pre.setTime(actRecord.getStatus(timeKey));
            if (TimeHelper.isNextDay(0, now, pre)) {
                actRecord.putState(timeKey, now.getTime());
                updActData(player, ActivityConst.TYPE_ADD, StaticActEquipUpdate.PAY_CONUT, 1, ActivityConst.ACT_WASH_EQUIP);
            }
        }
    }

    /**
     * 更新活动数据
     *
     * @param player
     * @param state
     */
    public void updActData(Player player, int type, long sortId, int state, int activityId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);
        if (actRecord == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        if (type == ActivityConst.TYPE_SET) {
            actRecord.putState(sortId, state);
        } else if (type == ActivityConst.TYPE_ADD) {
            long status = actRecord.getStatus(sortId);
            actRecord.putState(sortId, state + status);

            // 更新活动推送红点
            activityEventManager.activityTip(player, actRecord, activityBase);
        }

    }

    public List<Award> getRedDialAwards(List<List<Integer>> dropList) {
        List<Award> awards = new ArrayList<Award>();
        if (dropList != null && dropList.size() >= 1) {
            // 先计算总权重
            int total = 0;
            for (List<Integer> itemLoot : dropList) {
                if (itemLoot == null || itemLoot.size() != 4) {
                    continue;
                }
                total += itemLoot.get(3);
            }

            int randNum = RandomHelper.threadSafeRand(1, total);
            int checkNum = 0;
            for (List<Integer> itemLoot : dropList) {
                if (itemLoot == null || itemLoot.size() != 4) {
                    continue;
                }
                int type = itemLoot.get(0);
                int id = itemLoot.get(1);
                int count = itemLoot.get(2);
                checkNum += itemLoot.get(3);
                if (randNum <= checkNum) {
                    if (type == AwardType.EMPTY_LOOT) {
                        return awards;
                    }
                    // 空掉落不加入
                    awards.add(new Award(0, type, id, count));
                    break;
                }
            }
        }

        return awards;
    }

    // 检查七日狂欢有些活动
    public void checkSeven() {
        Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null) {
                continue;
            }

            // 检查装备洗练和获得
            equipManager.checkWashEquip(player);
            // 检查英雄洗满
            heroManager.chechHeroWashMax(player);
            // 检查战力
            Lord lord = player.getLord();
            if (lord != null) {
                if (player.getMaxScore() < lord.getBattleScore()) {
                    player.setMaxScore(lord.getAllScore());
                }
            }
        }
    }

    /**
     * 紫装装盘排行榜记录值更新
     *
     * @param player
     * @param activityId
     * @param schedule
     * @param sortId
     */
    public boolean updActPersonWithoutRefresh(Player player, int activityId, int schedule, int sortId) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }
        return updActPersonWithoutRefresh(player, activityBase, schedule, sortId);
    }

    /**
     * 紫装装盘排行榜
     *
     * @param player
     * @param activityBase
     * @param schedule
     * @param sortId
     */
    public boolean updActPersonWithoutRefresh(Player player, ActivityBase activityBase, int schedule, int sortId) {
        try {

            ActRecord activity = getActivityInfo(player, activityBase);
            int record = activity.getRecord(sortId);
            record = record + schedule;
            activity.putRecord(sortId, record);
            int onePrice = activityBase.getActivityId() == ActivityConst.ACT_CRYSTAL_DIAL ? limitMgr.getNum(230) : limitMgr.getNum(246);
            int dialCount = activity.getRecord(1);
            int state = activity.getRecord(0);
            int freeCount = state / onePrice - dialCount;
            freeCount = freeCount < 0 ? 0 : freeCount;
            return freeCount > 0;
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 紫装装盘排行榜记录值更新
     *
     * @param player
     * @param activityId
     * @param schedule
     * @param sortId
     */
    public void updActPersonPurp(Player player, int activityId, int schedule, int sortId, int min) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        updActPersonPurp(player, activityBase, schedule, sortId, min);
    }

    /**
     * 紫装装盘排行榜
     *
     * @param player
     * @param activityBase
     * @param schedule
     * @param sortId
     */
    public void updActPersonPurp(Player player, ActivityBase activityBase, int schedule, int sortId, int min) {
        try {
            ActRecord activity = getActivityInfo(player, activityBase);

            int state = activity.getRecord(sortId);
            state = state + schedule;
            activity.putRecord(sortId, state);

            /***** 排行类活动 *****/
            int staticRank = activityBase.getStaticActivity().getRank();
            if (staticRank == ActivityConst.RANK_2) {
                long lordId = player.getLord().getLordId();
                ActivityData activityData = getActivity(activityBase);
                if (state >= min)// 上榜最低积分
                {
                    activityData.addPlayerRank(lordId, state);
                    int beginDay = TimeHelper.getDay(activityBase.getBeginTime());
                    int today = TimeHelper.getDay(new Date());
                    if (beginDay != today || TimeHelper.getCurrentHour() >= ActivityConst.RANK_HOUR) {
                        ActPlayerRank actRank = activityData.getLordRank(lordId);// 当前排名
                        long historyRank = activityData.getStatus(lordId);
                        if (actRank != null && (historyRank == 0 || historyRank > actRank.getRank())) {
                            activityData.putState(lordId, actRank.getRank());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }

    public void updActPayEveryDay(Player player, int activityId, int schedule) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        updActPayEveryDay(player, activityBase, schedule);
    }

    public void updActPayEveryDay(Player player, ActivityBase activityBase, int schedule) {
        try {
            ActRecord actRecord = getActivityInfo(player, activityBase);
            int day = 1;
            long state = actRecord.getStatus(day);
            state = state + schedule;
            actRecord.putState(day, state);
            Date date = activityBase.getBeginTime();
            if (DateHelper.dayiy(date, new Date()) == day) {
                day++;
            }

        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 开启建造大礼包
     *
     * @param player
     */
    public void openBuildGift(Player player) {
        List<Integer> ations = limitMgr.getAddtion(254);
        if (ations == null || ations.size() == 0 || ations.size() < 3) {
            LogHelper.CONFIG_LOGGER.info("建造礼包开启bug:{}", ations);
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_BUILD_QUE);
        if (activityBase == null) {
            LogHelper.CONFIG_LOGGER.info("no activity 88 config");
            return;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase);
        actRecord.setShow(true);
        actRecord.setBeginTime(TimeHelper.getCurrentSecond());
        activityEventManager.activityTip(player, actRecord, activityBase);
        activityEventManager.activityTip(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, new CommonTipActor(player, actRecord, activityBase));
    }

    /**
     * 刷新英雄令任务
     *
     * @param player
     * @return
     */
    public List<CommonPb.ActPassPortTask.Builder> getPassPortTask(Player player) {
        List<CommonPb.ActPassPortTask.Builder> builders = new ArrayList<>();

        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_PASS_PORT);
        if (null == activityBase) {
            return null;
        }
        ActivityData activityData = getActivity(ActivityConst.ACT_PASS_PORT);
        if (null == activityData) {
            return null;
        }
        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == activityInfo) {
            return null;
        }

        long dayTaskEndTime = activityData.getAddtion(1);// 类型为1的任务的结束时间
        long weekTaskEndTime = activityData.getAddtion(2);// 类型为2的任务的结束时间
        long monthTaskEndTime = activityData.getAddtion(3);// 类型为3的任务的结束时间
        long weeNum = activityData.getAddtion(4) == 0 ? 1 : activityData.getAddtion(4);// 活动进行到第几周

        Long dayTaskEndTimeInfo = activityInfo.getStatus(1);// 个人类型为1的任务的结束时间
        Long weekTaskEndTimeInfo = activityInfo.getStatus(2);// 个人类型为1的任务的结束时间
        Long monthTaskEndTimeInfo = activityInfo.getStatus(3);// 个人类型为2的任务的结束时间

        for (int i = 1; i <= 3; i++) {
            CommonPb.ActPassPortTask.Builder actPassPortTask = CommonPb.ActPassPortTask.newBuilder();
            actPassPortTask.setType(i);
            switch (i) {
                case 1:
                    if (dayTaskEndTimeInfo == 0 || dayTaskEndTime > dayTaskEndTimeInfo || getPassPortTaskCount(player, 1) == 0) {
                        refreshPassPortTask(player, 1, 0);
                        activityInfo.putState(1, dayTaskEndTime);
                    }
                    actPassPortTask.setEndTime(activityData.getAddtion(1));
                    break;
                case 2:
                    if (weekTaskEndTimeInfo == 0 || weekTaskEndTime > weekTaskEndTimeInfo || getPassPortTaskCount(player, 2) == 0) {
                        refreshPassPortTask(player, 2, (int) weeNum);
                        activityInfo.putState(2, weekTaskEndTime);
                    }
                    actPassPortTask.setEndTime(activityData.getAddtion(2));
                    break;
                case 3:
                    if (monthTaskEndTimeInfo == 0 || monthTaskEndTime > monthTaskEndTimeInfo || getPassPortTaskCount(player, 3) == 0) {
                        refreshPassPortTask(player, 3, 0);
                        activityInfo.putState(3, monthTaskEndTime);
                    }
                    actPassPortTask.setEndTime(activityBase.getEndTime().getTime());
                    break;
                default:
                    break;
            }

            Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();
            Iterator<ActPassPortTask> iterator = tasks.values().iterator();
            while (iterator.hasNext()) {
                ActPassPortTask task = iterator.next();
                if (task.getType() == i) {
                    CommonPb.ActPassPortTaskItem.Builder actPassPortTaskItem = CommonPb.ActPassPortTaskItem.newBuilder();
                    StaticPassPortTask staticPassPortTask = staticActivityMgr.getPassPortTask(task.getId());
                    if (null != staticPassPortTask) {
                        actPassPortTaskItem.setId(task.getId());
                        actPassPortTaskItem.setType(task.getTaskType());
                        actPassPortTaskItem.setCond(staticPassPortTask.getCond());
                        actPassPortTaskItem.setScore(staticPassPortTask.getScore());
                        actPassPortTaskItem.setProcess(task.getProcess());
                        actPassPortTaskItem.setIsAward(task.getIsAward());
                        actPassPortTaskItem.setDesc(staticPassPortTask.getContent());

                        actPassPortTask.addItem(actPassPortTaskItem);
                    }
                }
            }
            builders.add(actPassPortTask);
        }
        return builders;
    }

    /**
     * 刷新任务时间
     */
    public void refreshPassPortTaskTime() {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_PASS_PORT);
        if (null == activityBase) {
            return;
        }
        ActivityData activityData = getActivity(ActivityConst.ACT_PASS_PORT);
        if (null == activityData) {
            return;
        }

        if (null == activityData || activityBase == null) {
            return;
        }

        long dayTaskEndTime = activityData.getAddtion(1);// 类型为1的任务的结束时间
        long weekTaskEndTime = activityData.getAddtion(2);// 类型为2的任务的结束时间
        long monthTaskEndTime = activityData.getAddtion(3);// 类型为3的任务的结束时间
        long weekNum = activityData.getAddtion(4);// 当前任务的周数
        long now = System.currentTimeMillis();

        if (dayTaskEndTime == 0 || dayTaskEndTime < now) {
            activityData.putAddtion(1, TimeHelper.getEndTimeOfDay());
        }
        if (weekTaskEndTime == 0 || weekTaskEndTime < now) {
            int state = DateHelper.dayiy(activityBase.getBeginTime(), new Date());
            weekNum = state / 7 + 1;
            activityData.putAddtion(2, activityBase.getBeginTime().getTime() + 7 * weekNum * TimeHelper.DAY_MS - 1);
            activityData.putAddtion(4, weekNum);
        }
        if (monthTaskEndTime == 0) {
            activityData.putAddtion(3, activityBase.getEndTime().getTime());
        }
    }

    /**
     * 刷新个人通行证任务
     *
     * @param player
     * @param type
     * @param week
     */
    public void refreshPassPortTask(Player player, int type, int week) {
        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();

        Iterator<Map.Entry<Integer, ActPassPortTask>> iterator = tasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ActPassPortTask> next = iterator.next();
            int ty = next.getValue().getType();
            if (ty == type) {
                iterator.remove();
            }
        }

        List<StaticPassPortTask> staticPassPortTaskByType = staticActivityMgr.getStaticPassPortTaskByType(type);
        for (StaticPassPortTask task : staticPassPortTaskByType) {
            if (null != task && week == task.getWeekNum()) {
                tasks.put(task.getId(), new ActPassPortTask(task));
            }
        }
    }

    /**
     * 刷新个人通行证任务的进度
     *
     * @param player
     * @param taskType
     * @param process
     */
    public boolean updatePassPortTaskCond(Player player, int taskType, int process) {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_PASS_PORT);
        if (null == activityBase) {
            return false;
        }
        if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }

        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == activityInfo) {
            return false;
        }
        Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();
        getPassPortTask(player);
        boolean flag = false;
        Set<Map.Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
        for (Map.Entry<Integer, ActPassPortTask> entry : entries) {
            ActPassPortTask value = entry.getValue();
            if (taskType == value.getTaskType()) {
                StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(value.getId());
                if (null == passPortTask) {
                    continue;
                }
                if (taskType == ActPassPortTaskType.EVERY_DAY_LOGIN && value.getProcess() >= passPortTask.getCond()) {
                    continue;
                }
                int totalProcess = value.getProcess() + process;
                value.setProcess(totalProcess);
                if (value.getIsAward() == 0 && passPortTask != null && totalProcess >= passPortTask.getCond()) {
                    flag = true;
                }
            }
        }
        if (flag) {
            playerManager.synActivity(player, 0, 0);
        }
        return flag;
    }

    public int getPassPortTaskCount(Player player, int type) {
        int i = 0;
        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == activityInfo) {
            return i;
        }
        Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();
        Set<Map.Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
        for (Map.Entry<Integer, ActPassPortTask> entry : entries) {
            ActPassPortTask value = entry.getValue();
            if (type == value.getType()) {
                i++;
            }
        }
        return i;
    }

    /**
     * 购买通行证
     *
     * @param player
     * @param giftId
     * @return
     */
    public boolean actPayPassPort(Player player, int giftId) {
        StaticPayPassPort staticPayPassPort = staticActivityMgr.getStaticPayPassPort(giftId);
        if (staticPayPassPort == null || staticPayPassPort.getSellList().isEmpty()) {
            return false;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_PASS_PORT);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }

        ActRecord actRecord = getActivityInfo(player, activityBase);
        Map<Integer, Integer> actRecords = actRecord.getRecord();
        if (null != actRecords) {
            if (actRecords.get(1) != null && actRecords.get(1) == 1) {
                return false;
            }
        }

        actRecord.getRecord().put(1, 1);
//		playerManager.synActivity(player, ActivityConst.ACT_PASS_PORT, 0);
//		LogHelper.MESSAGE_LOGGER.info("购买通行证 通知客户端:{}", player.getRoleId());
        SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_PASS_PORT, activityBase.getStaticActivity().getName(), giftId);
        SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_PASS_PORT, activityBase.getStaticActivity().getName(), giftId, new Date(), staticPayPassPort.getSellList());
        return true;
    }

    public void addPassPortLv(Player player, int id) {
        if (id <= 0) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == actRecord) {
            return;
        }
        Map<Integer, Integer> record = actRecord.getRecord();
        Integer score = record.get(0) == null ? 0 : record.get(0);
        int lv = staticActivityMgr.getPassPortLv(score);
        if (lv >= id) {
            return;
        }

        Map<Integer, StaticPassPortLv> passPortLvMap = staticActivityMgr.getPassPortLvMap();
        StaticPassPortLv staticPassPortLv = passPortLvMap.get(id);
        if (staticPassPortLv == null) {
            return;
        }
        record.put(0, staticPassPortLv.getScore());

    }

    /**
     * 增加通行证积分
     *
     * @param player
     * @param count
     */
    public void addPassPortScore(Player player, long count) {
        if (count <= 0) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == actRecord) {
            return;
        }
        Map<Integer, Integer> record = actRecord.getRecord();
        Integer score = record.get(0) == null ? 0 : record.get(0);
        int maxPassPortScore = staticActivityMgr.getMaxPassPortScore();
        score = score + (int) count;
        if (score >= maxPassPortScore) {
            record.put(0, maxPassPortScore);
            return;
        }
        record.put(0, score);
    }

    /**
     * 计算大杀四方
     *
     * @param attack
     * @param defence
     */
    public void calcuKillAll(WarInfo warInfo, Team attack, Team defence) {
        if (!limitMgr.getAddtion(SimpleId.ACT_KILL_ALL).contains(warInfo.getWarType())) {
            return;
        }
        calcuKillAll(attack, defence);
    }

    /**
     * 计算大杀四方
     *
     * @param attack
     * @param defence
     */
    public void calcuKillAll(Team attack, Team defence) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_KILL_ALL);
        if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }

        Map<Long, Integer> playerKill = new HashMap<>();
        for (BattleEntity entity : attack.getAllEnities()) {
            Integer kill = playerKill.get(entity.getLordId());
            if (kill == null) {
                kill = 0;
            }
            kill += entity.getKillNum();
            playerKill.put(entity.getLordId(), kill);
        }
        for (BattleEntity entity : defence.getAllEnities()) {
            Integer kill = playerKill.get(entity.getLordId());
            if (kill == null) {
                kill = 0;
            }
            kill += entity.getKillNum();
            playerKill.put(entity.getLordId(), kill);
        }
        playerKill.forEach((lordId, killNum) -> {
            Player player = playerManager.getPlayer(lordId);
            if (player != null) {
                ActRecord actRecord = getActivityInfo(player, activityBase);
                if (actRecord == null) {
                    return;
                }
                Integer totalKill = actRecord.getRecord(1);
                if (totalKill == null) {
                    totalKill = 0;
                }
                totalKill += killNum;
                actRecord.putRecord(1, totalKill);

                activityEventManager.updateActivityHandler(EventEnum.KILL_PLAYER_SOIDLER, new CommonTipActor(player, actRecord, activityBase));
            }
        });
    }

    /**
     * 计算每日远征
     */
    public void calculDailyExpedition(Player player, int count) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAYLY_EXPEDITION);
        if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        if (player != null) {
            ActRecord actRecord = getActivityInfo(player, activityBase);
            if (actRecord == null) {
                return;
            }
            long key = -1L;
            Long total = actRecord.getStatus(key);
            if (total == null) {
                total = 0L;
            }
            total += count;
            actRecord.getStatus().put(key, total);
            activityEventManager.activityTip(player, actRecord, activityBase);
        }
    }

    /**
     * 刷新每日充值金额
     *
     * @param player
     * @param taskType
     * @param process
     */
    public void updateActDailyRecharge(Player player, int taskType, int topUp, int process) {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_DAYLY_RECHARGE);
        if (null == activityBase) {
            return;
        }
        if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_DAYLY_RECHARGE);
        if (null == actRecord) {
            return;
        }
        // 当天
        int day = GameServer.getInstance().currentDay;
        Integer totalUp = actRecord.getRecord(day);
        if (totalUp == null) {
            totalUp = 0;
        }
        totalUp += topUp;
        actRecord.putRecord(day, totalUp);
    }

    /**
     * 导师排行榜记录值更新
     *
     * @param player
     */
    public void updActMentorScore(Player player) {
        /***** 排行类活动 *****/
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_MENTOR_SCORE);
        if (activityBase == null || activityBase.getStaticActivity().getRank() != ActivityConst.RANK_3 || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN

                || getActivity(activityBase) == null) {
            return;
        }
        Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
        Date now = new Date();
        if (now.after(rewardTime)) {
            return;
        }

        getActivity(activityBase).addPlayerRank(player.getLord().getLordId(), calculateMentorScore(player));
    }

    /**
     * 充值排行 消费排行
     *
     * @param player
     * @param amount
     */
    public boolean updActRecharScore(Player player, int amount, int activityType) {
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityType);
        if (activityBase == null || activityBase.getStaticActivity().getRank() != ActivityConst.RANK_3 || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN

                || getActivity(activityBase) == null) {
            return false;
        }
        Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
        Date now = new Date();
        if (now.after(rewardTime)) {
            return false;
        }

        getActivity(activityBase).addRank(player.getLord().getLordId(), amount);
        return true;
    }

    /**
     * 计算分数
     *
     * @param player
     * @return
     */
    public long calculateMentorScore(Player player) {
        long score = 0;
        Map<Long, Friend> apprenticeMap = player.getFriends().get(FriendType.APPRENTICE);
        if (apprenticeMap == null || apprenticeMap.size() == 0) {
            return 0;
        }
        Map<Integer, StaticApprenticeRank> rankMap = staticFriendMgr.getApprenticeRankMap();
        for (Friend value : apprenticeMap.values()) {
            Player apprentice = playerManager.getPlayer(value.getRolaId());
            if (apprentice == null) {
                continue;
            }
            int level = apprentice.getLevel();
            int count = 0;
            for (Entry<Integer, StaticApprenticeRank> entry : rankMap.entrySet()) {
                if (level >= entry.getKey()) {
                    int num = entry.getValue().getScore();
                    count = count > num ? count : num;
                }
            }
            score += count;
        }
        return score;
    }

    public void actDoubleEggEnd(int prop, Player player) {
        player.getLord().setClothes(0);
        Item item = player.getItem(prop);
        if (item != null && item.getItemNum() > 0) {
            int num = limitMgr.getNum(SimpleId.PROP_CHANGE_GOLD);
            List<Award> resourceAward = new ArrayList<>();
            Award award = new Award(AwardType.RESOURCE, ResourceType.IRON, item.getItemNum() * num);
            Award award2 = new Award(AwardType.RESOURCE, ResourceType.COPPER, item.getItemNum() * num);
            resourceAward.add(award);
            resourceAward.add(award2);
            // 发送兑换资源邮件
            playerManager.sendAttachMail(player, resourceAward, MailId.ACT_DOUBLE_EGG_END);
            playerManager.subItem(player, item.getItemId(), item.getItemNum(), Reason.ACT_DOUBLE_EGG);
        }
    }

    public void actChrismasRewardEnd(Player player, ActRecord actRecord, int cost, ActivityBase activityBase) {
        // 奖励的
        List<CommonPb.Award> tawards = new ArrayList<>();
        staticActivityMgr.getChrismasAwardMap().forEach((keyId, statiAward) -> {
            if (cost >= statiAward.getCost() && activityBase.getAwardId() == statiAward.getAwardId()) { // 满足奖励领取需求
                if (!actRecord.getReceived().containsKey(keyId)) {// 没有领取过
                    statiAward.getAward().forEach(e -> {
                        tawards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
                    });
                }
            }
        });
        // 已达成条件,未领取奖励,补发奖励邮件
        if (tawards.size() > 0) {
            playerManager.sendAttachMail(player, PbHelper.finilAward(tawards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
        }
    }

    public void actWorldBoxEnd(Player player, ActivityBase activityBase) {
        PWorldBox pWorldBox = player.getPWorldBox();
        if (pWorldBox.getWorldBoxList().size() > 0) {
            List<CommonPb.Award> tawards = new ArrayList<>();
            for (WorldBox worldBox : pWorldBox.getWorldBoxList()) {
                List<Award> awards = worldBoxManager.openWorldBox(worldBox);
                awards.forEach(e -> {
                    tawards.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
                });
            }
            if (tawards.size() > 0) {
                playerManager.sendAttachMail(player, PbHelper.finilAward(tawards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
            }
        }
        pWorldBox.reset();
    }

    /***
     * 王牌球手
     *
     * @param player
     * @return
     */
    public void checkHeroKowtow(Player player, int taskType, int process) {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_TASK_HERO);
        if (null == activityBase) {
            return;
        }
        if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_TASK_HERO);
        if (null == activityInfo) {
            return;
        }
        Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();
        List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
        if (condList == null || condList.size() == 0) {
            return;
        }
        if (tasks.size() == 0) {
            condList.forEach(e -> {
                activityInfo.getTasks().put(e.getId(), new ActPassPortTask(e));
            });
        }

        Set<Map.Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
        for (Map.Entry<Integer, ActPassPortTask> entry : entries) {
            ActPassPortTask value = entry.getValue();
            if (value.getIsAward() == 1) {
                continue;
            }
            StaticHeroTask staticHeroTask = staticActivityMgr.getStaticHeroTaskMap().get(value.getId());
            if (null == staticHeroTask) {
                continue;
            }
            switch (value.getType()) {
                case TaskType.BUILDING_LEVELUP: {
                    int totalProcess = player.getBuildingLv(staticHeroTask.getParam().get(0));
                    value.setProcess(totalProcess);
                }
                break;
                case TaskType.FINISH_TECH: {
                    Tech tech = player.getTech();
                    if (tech != null) {
                        TechInfo info = tech.getTechInfo(staticHeroTask.getParam().get(0));
                        if (info != null) {
                            int totalProcess = info.getLevel();
                            value.setProcess(totalProcess);
                        }
                    }
                }
                break;
                case TaskType.KILL_REBEL: {
                    if (staticHeroTask.getParam().contains(process)) {
                        int totalProcess = value.getProcess() + 1;
                        value.setProcess(totalProcess);
                    }
                }
                break;
                default: {
                    if (taskType == value.getTaskType()) {
                        int totalProcess = value.getProcess() + process;
                        value.setProcess(totalProcess);
                    }
                }
                break;
            }
        }
    }

    public boolean updSurpriseGift(Player player, int giftId) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_SURIPRISE_GIFT);
        if (null == actRecord) {
            return false;
        }
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_SURIPRISE_GIFT);
        if (activityBase == null) {
            return false;
        }
        Iterator<ActivityRecord> it = actRecord.getActivityRecords().iterator();
        while (it.hasNext()) {
            ActivityRecord record = it.next();
            if (record.getKey() == giftId) {
                StaticLimitGift limitGift = staticActivityMgr.getLimitGiftByKeyId(record.getKey());
                record.setBuyCount(record.getBuyCount() + 1);
                if (record.getBuyCount() >= limitGift.getCount()) {
                    if (SuripriseId.MarkerFlop.get() == limitGift.getLimit().get(0)) {
                        it.remove();
                    } else {
                        //
                        actRecord.getReceived().put(record.getKey(), 1);
                        LogHelper.CONFIG_LOGGER.info("pay receive:{}", record.getKey());
                    }
                }
            }
        }

        // 购买惊喜礼包
        activityEventManager.activityTip(EventEnum.ACT_BUY_GIFT, new CommonTipActor(player, actRecord, activityBase));
        return true;
    }

    public void actMyEnd(Player player) {
        int itemNum = player.getItemNum(ItemId.HERO_GOLD);
        if (itemNum > 0) {
            List<Award> resourceAward = new ArrayList<>();
            Award award = new Award(AwardType.PROP, 82, itemNum * 100);
            resourceAward.add(award);
            // 发送兑换资源邮件
            playerManager.sendAttachMail(player, resourceAward, MailId.MY_RECEIVED);
            playerManager.subItem(player, ItemId.HERO_GOLD, itemNum, Reason.ACT_HERO_DIAL);
        }
    }

    /**
     * @Description 更新日常训练活动
     * @Date 2021/3/16 16:33
     * @Param [player, actId, money]
     * @Return
     **/
    public void updateActDailyTrain(Player player, int soidierCount) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.DAILY_TRAINRS);
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        actRecord.addRecord(1, soidierCount);

        activityEventManager.updateActivityHandler(EventEnum.TRAINR, new CommonTipActor(player, actRecord, activityBase));

//        int record = actRecord.getRecord(1);
//        Map<Integer, Integer> received = actRecord.getReceived();
//        List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
//        if (condList.size() < 1) {
//            return;
//        }
//        for (StaticActAward staticActAward : condList) {
//            if (record >= staticActAward.getCond() && !received.containsKey(staticActAward.getKeyId())) {
//                playerManager.synActivity(player, ActivityConst.DAILY_TRAINRS);
//                break;
//            }
//        }
    }

    /**
     * 更新幸运奖池活动
     *
     * @Description
     * @Date 2021/3/16 16:34
     * @Param [player, actId, money]
     * @Return
     **/
//    public void updateActLuckPool(Player player, int actId, int money) {
//        ActRecord actRecord = getActivityInfo(player, actId);
//        if (actRecord == null) {
//            return;
//        }
//
//        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
//        if (activityBase == null) {
//            return;
//        }
//
//        int step = activityBase.getStep();
//        if (step != ActivityConst.ACTIVITY_BEGIN) {
//            return;
//        }
//
//        StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
//
//        //增加充值金额   0:累计充值金额   1:剩余抽奖次数   2:奖池剩余钻石数量   3:已抽奖次数
//        actRecord.addRecord(ActivityLuckPool.RECHARGE_AMOUNT, money);
//
//        //增加抽奖次数
//        int moneyNum = actRecord.getRecordNum(ActivityLuckPool.RECHARGE_AMOUNT);
//        int count = moneyNum / (staticLimitMgr.getNum(SimpleId.LUCK_POOL_ONE_GOLD) / 10);
//        int surplusCount = count - actRecord.getRecordNum(ActivityLuckPool.CONSUME_COUNT);
//        actRecord.putRecord(ActivityLuckPool.SURPLUS_COUNT, surplusCount);
//
//        //增加奖池钻石数量
//        int recordNum = actRecord.getRecordNum(ActivityLuckPool.DIAMONDS_NUM);
//        //初始化奖池钻石数量
//        recordNum = recordNum == 0 ? staticLimitMgr.getNum(SimpleId.LUCK_POOL_INI_GOLD) : recordNum;
//        recordNum = recordNum < staticLimitMgr.getNum(SimpleId.LUCK_POOL_MIN_GOLD) ? staticLimitMgr.getNum(SimpleId.LUCK_POOL_MIN_GOLD) : recordNum;
//        recordNum = recordNum + (money * 10);
//        actRecord.putRecord(ActivityLuckPool.DIAMONDS_NUM, recordNum);
//    }
    public void updateOrder(Player player, int count, StaticProp staticProp) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_ORDER);
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(actRecord.getAwardId());
        if (actAwardById.isEmpty()) {
            return;
        }
        String param = actAwardById.get(0).getParam();
        if (param == null || param.isEmpty()) {
            return;
        }
        List<Integer> list = StringUtil.stringToList(param);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (staticProp.getColor() < list.get(0).intValue()) {
            return;
        }
        actRecord.addRecord(1, count);
        activityEventManager.activityTip(player, actRecord, activityBase);
//        StaticActAward staticActAward = actAwardById.stream().filter(x -> x.getCond() < i && !actRecord.getReceived().containsKey(x.getKeyId()))
//            .findAny().orElse(null);
//        if (staticActAward != null) {
//            playerManager.synActivity(player, ActivityConst.ACT_ORDER);
//        }
    }

    /**
     * 七日豪礼/物质搜寻
     *
     * @param roleId
     * @param actId
     */
    public void reflushSeven(long roleId, int actId) {
        Player player = playerManager.getPlayer(roleId);
        if (player == null) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, actId);
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        if (actId == ActivityConst.ACT_SEARCH) {
            int count = actRecord.getCount();
            if (count > 0) {
                // 判定前一天是否完成 如果完成就加一天 开启一个新宝箱
                List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(activityBase.getAwardId(), count);
                if (actAwardById != null && !actAwardById.isEmpty()) {
                    if (actRecord.getStatus(count) >= actAwardById.get(0).getCond()) {
                        actRecord.addCount();
                        return;
                    }
                }
                actRecord.putState(count, 0);
                return;
            }
            actRecord.addCount();
        }
        if (actId == ActivityConst.ACT_LOGIN_SEVEN) {
            int i = actRecord.getCount() + 1;
            if (i > player.account.getTimeFromCreat()) {
                return;
            }
            actRecord.addCount();
        }
    }

    /**
     * @Description 根据用户排名获取奖励档位
     * @Date 2021/3/18 10:53
     * @Param [playerRank, displayList]
     * @Return
     **/
    public int obtainAwardGear(int playerRank, List<StaticActRankDisplay> displayList) {
        ArrayList<Integer> list = new ArrayList<>();
        int tempRank = -1;
        for (StaticActRankDisplay display : displayList) {
            int rank = display.getRank();
            if (playerRank <= rank) {
                list.add(rank);
            }
        }
        Collections.sort(list);
        if (!list.isEmpty()) {
            tempRank = list.get(0);
        }
        return tempRank;
    }

    /**
     * 勇冠三军活动更新
     *
     * @param player
     * @param activityId
     * @param schedule
     */
    public boolean updActWellCrownThreeArmy(Player player, int activityId, int schedule) {
        if (player.getLevel() < 48) {
            return false;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
        if (activityBase == null) {
            return false;
        }

        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }
        ActRecord activity = getActivityInfo(player, activityBase);
        ActivityData activityData = getActivity(activityBase);
        int selfGold = activity.getRecord(0);
        selfGold += schedule;
        activity.putRecord(0, selfGold);

        int countryGold = activityData.getRecord(player.getCountry());
        countryGold += schedule;
        activityData.putRecord(player.getCountry(), countryGold);
        List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
        for (StaticActAward award : actAwardList) {
            List<Integer> param = StringUtil.stringToList(award.getParam());
            if (param.size() < 2) {
                continue;
            }
            if (selfGold >= param.get(0) && countryGold >= param.get(1)) {// 个人达标,且阵营达标
                if (!activity.getReceived().containsKey(award.getKeyId())) {// 没有领取记录
                    playerManager.synActivity(player, activityId);
//					LogHelper.MESSAGE_LOGGER.info("updActWellCrownThreeArmy");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @Description 更新资源采集活动
     * @Date 2021/3/26 14:32
     * @Param
     * @Return
     **/
    public void updCollectionResource(Player player, Award award) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_COLLECTION_RESOURCE);
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
        List<Integer> paramList = null;
        if (actAwardList != null && actAwardList.size() > 0) {
            StaticActAward staticActAward = actAwardList.get(0);
            String param = staticActAward.getParam();
            if (param == null || param.isEmpty()) {
                return;
            }
            paramList = StringUtil.stringToList(param);
        }
        if (paramList == null || !paramList.contains(award.getId())) {
            return;
        }
        actRecord.putState(0, actRecord.getStatus(0) + award.getCount());
        long record = actRecord.getStatus(0);

        // 活动红点推送
        activityEventManager.activityTip(player, actRecord, activityBase);
    }

    public void updateActMonster(Player player) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_MONSTER);
        if (actRecord == null) {
            return;
        }
        ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
        if (activityBase == null) {
            return;
        }
        int step = activityBase.getStep();
        if (step != ActivityConst.ACTIVITY_BEGIN) {
            return;
        }
        actRecord.addCount();
        activityEventManager.updateActivityHandler(EventEnum.KILL_MONSTER, new CommonTipActor(player, actRecord, activityBase));
    }

    /**
     * @Description 刷新成长基金红点
     * @Param [player] @Return void
     * @Date 2021/4/27 12:45
     **/
    public void updateActInvestRq(Player player) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_MONSTER);
        if (actRecord == null) {
            return;
        }
        List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
        if (condList == null) {
            return;
        }
        int state = SpringUtil.getBean(ActivityService.class).currentActivity(player, actRecord, 0);// 是否已参与投资计划

        // 已参与则领奖
        if (state != 0) {
            activityEventManager.activityTip(player, actRecord, getActivityBase(ActivityConst.ACT_MONSTER));
        }
    }

    /**
     * 第一次打开秘书界面改变是否显示秘书购买礼包
     *
     * @param player
     * @param id
     */
    public void flushShowBeauty(Player player, int id) {
        if (id != 284) {
            return;
        }
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_BEAUTY_GIFT);
        if (activityBase == null) {
            return;
        }
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_BEAUTY_GIFT);
        if (actRecord == null) {
            return;
        }
        actRecord.setShow(true);

        activityEventManager.activityTip(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, new CommonTipActor(player, actRecord, activityBase));
//		LogHelper.MESSAGE_LOGGER.info("flushShowBeauty");
    }

    /**
     * 活动消失
     *
     * @param player
     * @param activityId
     */
    public void synActivityDisappear(Player player, int activityId) {
        ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
        builder.addParam(activityId);
        SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
    }

    /**
     * 刷新个人通行证任务的进度(玩家登陆时)
     *
     * @param player
     * @param taskType
     * @param process
     */
    public boolean enterPassPortTaskCond(Player player, int taskType, int process) {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_PASS_PORT);
        if (null == activityBase) {
            return false;
        }
        if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
            return false;
        }

        ActRecord activityInfo = getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
        if (null == activityInfo) {
            return false;
        }
        Map<Integer, ActPassPortTask> tasks = activityInfo.getTasks();
        if (tasks.size() == 0) {
            getPassPortTask(player);
        }

        boolean flag = false;
        Set<Map.Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
        for (Map.Entry<Integer, ActPassPortTask> entry : entries) {
            ActPassPortTask value = entry.getValue();
            if (taskType == value.getTaskType()) {
                StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(value.getId());
                if (null == passPortTask) {
                    continue;
                }
                if (taskType == ActPassPortTaskType.EVERY_DAY_LOGIN && value.getProcess() >= passPortTask.getCond()) {
                    continue;
                }
                int totalProcess = value.getProcess() + process;
                value.setProcess(totalProcess);
                if (value.getIsAward() == 0 && passPortTask != null && totalProcess >= passPortTask.getCond()) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public Map<Integer, StaticResourceGift> getResourceGift() {
        return staticActivityMgr.getStaticResourceGiftMap();
    }

    public boolean buySpringGift(Player player, StaticLimitGift springGift) {
        if (springGift == null) {
            return false;
        }
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_SPRING_FESTIVAL_GIFT);
        if (activityBase == null) {
            activityEventManager.updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
            return false;
        }
        ActRecord actRecord = getActivityInfo(player, activityBase.getActivityId());
        if (actRecord == null) {
            activityEventManager.updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
            return false;
        }
        int recordNum = actRecord.getRecordNum(springGift.getKeyId());
        if (recordNum >= springGift.getCount()) {
            return false;
        }
        actRecord.addRecord(springGift.getKeyId(), 1);
        activityEventManager.activityTip(player, actRecord, activityBase);
        return true;
    }

    // 推送春节活动红点
    public void pushSpringTips() {
        ActivityBase activityBase = getActivityBase(ActivityConst.ACT_SPRING_FESTIVAL_GIFT);
        if (activityBase == null) {
            return;
        }
        ActivityData activityData = getActivity(activityBase.getActivityId());
        if (activityData == null) {
            return;
        }
        activityService.synActivity(activityBase, activityData, EventEnum.GET_ACTIVITY_AWARD_TIP);
    }

    // 刷新塔防活动红点
    public boolean refreshTDTaskTips(Player player, ActRecord actRecord) {
        TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
        StaticTDTaskMgr staticTDTaskMgr = SpringUtil.getBean(StaticTDTaskMgr.class);
        int integral = actRecord.getRecordNum(0);
        Map<Integer, Integer> received = actRecord.getReceived();
        for (StaticTDSevenBoxAward value : staticTDTaskMgr.getStaticTDSevenBoxAwardMap().values()) {
            if (integral >= value.getCond() && !received.containsKey(value.getKeyId())) {
                return true;
            }
        }
        Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType = staticTDTaskMgr.getTdSevenTaskByType();
        for (Entry<Integer, Map<Integer, StaticTDSevenTask>> entry : tdSevenTaskByType.entrySet()) {
            if (entry.getKey() == ActTDSevenType.tdTaskType_1) {
                for (StaticTDSevenTask value : entry.getValue().values()) {
                    if ((int) actRecord.getStatus(value.getTaskId()) != GameServer.getInstance().currentDay) {
                        actRecord.getStatus().remove(Long.valueOf(value.getTaskId()));
                        actRecord.getReceived().remove(value.getTaskId());
                    }
                    ActivityCondState.Builder warp = value.warp(player, actRecord);
                    if (warp.getState() >= warp.getActivityCond().getCond() && warp.getActivityCond().getIsAward() == 0) {
                        return true;
                    }
                }
                continue;
            }
            StaticTDSevenTask staticTDSevenTask = entry.getValue().values().stream().filter(e -> !actRecord.getReceived().containsKey(e.getTaskId())).sorted(Comparator.comparing(e -> e.getTaskId())).findFirst().orElse(null);
            if (staticTDSevenTask != null) {
                ActivityCondState.Builder warp = staticTDSevenTask.warp(player, actRecord);
                if (warp.getState() >= warp.getActivityCond().getCond() && warp.getActivityCond().getIsAward() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 塔防活动在活动结束的时候 发放奖励
     **/
    public void sendTDTaskAward() {
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_TD_SEVEN_TASK);
        if (activityBase == null) {
            return;
        }
        TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
        StaticTDTaskMgr staticTDTaskMgr = SpringUtil.getBean(StaticTDTaskMgr.class);
        Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType = staticTDTaskMgr.getTdSevenTaskByType();
        Map<Integer, StaticTDSevenBoxAward> staticTDSevenBoxAwardMap = staticTDTaskMgr.getStaticTDSevenBoxAwardMap();
        if (tdSevenTaskByType == null || tdSevenTaskByType.isEmpty() || staticTDSevenBoxAwardMap == null || staticTDSevenBoxAwardMap.isEmpty()) {
            return;
        }
        int currentDay = GameServer.getInstance().currentDay;
        Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.account == null) {
                continue;
            }
            SimpleData simpleData = player.getSimpleData();
            Date createDate = player.account.getCreateDate();
            int less = activityBase.getStaticActivity().getLess();
            less = less == 0 ? 7 : less;
            Date endTime = DateHelper.addDate(createDate, less);
            if (new Date().before(endTime) || simpleData.isReissueTDTaskAward()) {
                continue;
            }
            ActRecord actRecord = getActivityInfo(player, activityBase);
            Map<Integer, Integer> received = actRecord.getReceived();
            if (actRecord == null) {
                continue;
            }
            for (Map<Integer, StaticTDSevenTask> value : tdSevenTaskByType.values()) {
                if (value == null || value.isEmpty()) {
                    continue;
                }
                value.values().forEach(e -> {
                    ActivityCondState.Builder warp = e.warp(player, actRecord);
                    if (warp.getState() >= warp.getActivityCond().getCond() && warp.getActivityCond().getIsAward() == 0) {
                        received.put(e.getTaskId(), currentDay);
                        List<Integer> awardList = e.getAwardList();
                        actRecord.addRecord(0, awardList.get(2));
                    }
                });
            }
            ArrayList<Award> awards = Lists.newArrayList();
            for (StaticTDSevenBoxAward value : staticTDSevenBoxAwardMap.values()) {
                if (actRecord.getRecordNum(0) >= value.getCond() && !received.containsKey(value.getKeyId())) {
                    received.put(value.getKeyId(), currentDay);
                    value.getAwardList().forEach(e -> {
                        if (e.size() == 3) {
                            Award award = new Award(e.get(0), e.get(1), e.get(2));
                            awards.add(award);
                        }
                    });
                }
            }
            simpleData.setReissueTDTaskAward(true);
            // 已达成条件,未领取奖励,补发奖励邮件
            if (awards.size() > 0) {
                playerManager.sendAttachMail(player, awards, MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
            }
        }
    }

    public void roleLoginActivity(Player player) {
        ActRecord actRecord = getActivityInfo(player, ActivityConst.ACT_DAILY_CHECKIN);
        Map<Integer, StaticDailyCheckin> condList = staticActivityMgr.getDailyCheckinAwards();
        ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAILY_CHECKIN);
        if (activityBase != null && actRecord != null && condList != null && condList.size() != 0) {
            long currentTime = TimeHelper.curentTime();
            long status = actRecord.getStatus(0L);
            if (status == 0 || !TimeHelper.isSameDay(status)) {
                actRecord.addCount();
                actRecord.putState(0L, currentTime);
                if (actRecord.getCount() > condList.size()) {
                    actRecord.setCount(1);
                    actRecord.getStatus().clear();
                    actRecord.getRecord().clear();
                    actRecord.getReceived().clear();
                }
            }
            int day = actRecord.getCount();
            actRecord.putRecord(day, day);//记录第几天 只要这天登录了就记录一下
        }
    }
}