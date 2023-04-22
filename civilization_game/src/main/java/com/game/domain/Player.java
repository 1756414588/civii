package com.game.domain;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.*;
import com.game.dataMgr.StaticCountryMgr;
import com.game.dataMgr.StaticFriendMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.p.*;
import com.game.domain.s.StaticApprenticeAward;
import com.game.domain.s.StaticCountryTitle;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Good;
import com.game.pb.CommonPb.MasterShopAward;
import com.game.pb.CommonPb.SerEndlessTDInfo;
import com.game.pb.DataPb;
import com.game.pb.DataPb.LevelAwards;
import com.game.pb.RolePb;
import com.game.pb.SerializePb;
import com.game.pb.SerializePb.*;
import com.game.season.BaseModule;
import com.game.server.GameServer;
import com.game.util.DateHelper;
import com.game.util.GameHelper;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.util.internal.StringUtil;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Getter
@Setter
public class Player {

    // 角色基础数据
    private Lord lord;

    // 角色账号
    public Account account;

    // 是否登录过
    private boolean entering;

    // 是否是整体加载
    private AtomicBoolean fullLoad = new AtomicBoolean(false);

    // 所有建筑
    public Building buildings = new Building();

    // 角色资源数据
    private Resource resource = new Resource();

    private PWorldBox pWorldBox = new PWorldBox();

    // 道具背包
    private HashMap<Integer, Item> itemMap = new HashMap<Integer, Item>();

    // 将领集合
    private HashMap<Integer, Hero> heros = new HashMap<Integer, Hero>();

    // 邮件列表:战报,系统,私聊
    private ConcurrentLinkedDeque<Mail> mails = new ConcurrentLinkedDeque<Mail>();
    private ConcurrentLinkedQueue<Integer> repots = new ConcurrentLinkedQueue<Integer>();
    private ConcurrentLinkedQueue<Integer> sysmail = new ConcurrentLinkedQueue<Integer>();
    private ConcurrentLinkedQueue<Integer> pmails = new ConcurrentLinkedQueue<Integer>();

    private ConcurrentHashMap<Long, PersonChatRoom> personChatRoom = new ConcurrentHashMap<Long, PersonChatRoom>();

    // 装备背包
    private Map<Integer, Equip> equips = new ConcurrentHashMap<Integer, Equip>();

    // 兵书背包
    private Map<Integer, WarBook> warBooks = new ConcurrentHashMap<Integer, WarBook>();

    // 兵书商城
    private Map<Integer, CommonPb.WarBookShopItem> warBookShops = new ConcurrentHashMap<Integer, CommonPb.WarBookShopItem>();

    // 上阵武将List Id
    private List<Integer> embattleList = new ArrayList<Integer>();

    // 采集武将列表
    private List<Integer> miningList = new ArrayList(4);

    // 扫荡武将列表
    private List<Integer> sweepHeroList = new ArrayList<>();

    // 城防武将列表
    private List<WarDefenseHero> defenseArmyList = new ArrayList<>(4);

    // 士兵信息
    private Map<Integer, Soldier> soldiers = new ConcurrentHashMap<Integer, Soldier>(); // 1,2,3=> 主基地兵营,
    // 4, 民兵营

    // 关卡信息:key1 : mapId, key2: missionId
    private Map<Integer, Map<Integer, Mission>> missions = new ConcurrentHashMap<Integer, Map<Integer, Mission>>();

    // 雇工信息
    private EmployInfo employInfo = new EmployInfo();

    // 聚宝盆补给站
    private List<Depot> depots = new LinkedList<>();

    // vip礼包
    private List<Integer> vipGifts = new LinkedList<Integer>();

    // vip特价商品
    private Map<Integer, Shop> shops = new ConcurrentHashMap<>();

    // 任务
    private Map<Integer, Task> taskMap = new ConcurrentHashMap<>();

    // 国器
    private Map<Integer, KillEquip> killEquipMap = new ConcurrentHashMap<>();

    // 活动
    public Map<Integer, ActRecord> activitys = new ConcurrentHashMap<>();

    // 行军信息
    private ConcurrentLinkedDeque<March> marchList = new ConcurrentLinkedDeque<March>();

    // 国家信息
    private Nation nation = new Nation();

    // 商店的相关信息
    private ShopInfo shopInfo = new ShopInfo();

    private int maxKey;

    // 个性签名
    private String personalSignature;

    // 上次发送聊天时间
    public int chatTime = 0;
    public long flameChatTime;

    private Map<Integer, MapStatus> mapStatusMap = new HashMap<>();

    // buff
    private Map<Integer, Effect> effects = new ConcurrentHashMap<Integer, Effect>();

    // 玩家世界目标的领奖状态
    private Map<Integer, WorldTargetAward> worldTargetAwardMap = new ConcurrentHashMap<Integer, WorldTargetAward>();

    // 黑名单
    private List<Long> blackList = new ArrayList<Long>();

    // 美女信息
    private Map<Integer, BeautyData> beautys = new ConcurrentHashMap<>();

    // 配饰背包信息
    private Map<Integer, Omament> omaments = new ConcurrentHashMap<>();

    // 玩家穿戴配饰信息
    private Map<Integer, PlayerOmament> playerOmaments = new ConcurrentHashMap<>();

    private Map<Integer, Frame> frameMap = new ConcurrentHashMap<>();

    // 推送坐标
    private Map<String, Boolean> pushPos = new ConcurrentHashMap<>();

    private PlayerDailyTask playerDailyTask = new PlayerDailyTask();

    // 已经完成
    public int maxKey() {
        return ++maxKey;
    }

    public int getMaxKey() {
        return maxKey;
    }

    public void setMaxKey(int maxKey) {
        this.maxKey = maxKey;
    }

    public Long roleId;
    public long lastSaveTime;
    public long removeOnlineTime;

    public boolean isLogin = false;

    public boolean immediateSave = false;
//	public ChannelHandlerContext ctx;

    // 行军信息
    private LostRes lostRes = new LostRes();

    // 已经触发过的新手引导
    private HashSet<Integer> newStateDone = new HashSet<Integer>();

    // 升级奖励
    private Map<Integer, LevelAward> levelAwardsMap = new HashMap<Integer, LevelAward>();

    // 玩家特殊数据存盘
    private SimpleData simpleData = new SimpleData();

    private BroodWarInfo broodWarInfo = new BroodWarInfo();

    // 玩家的oldPos
    private Pos oldPos = new Pos();

    // 英雄集结信息
    private HashSet<Integer> massHeroes = new HashSet<Integer>();

    // 血战参战英雄[战斗开始前清除]
    private HashMap<Integer, PvpHero> pvpHeroMap = new HashMap<Integer, PvpHero>();

    // 已经完成的任务
    private TreeSet<Integer> finishedTask = new TreeSet<Integer>();

    // 星级奖励
    private TreeMap<Integer, TreeMap<Integer, Integer>> missionStar = new TreeMap<Integer, TreeMap<Integer, Integer>>();

    // 世界目标的个人任务目标
    private Map<Integer, WorldPersonalGoal> personalGoals = new HashMap<>();

    // 市场资源打包信息
    private Map<Integer, ResourcePacket> resPackets = new HashMap<>();

    /**
     * 当前作战研究室的任务
     */
    private MeetingTask meetingTask = new MeetingTask();

    private Map<Integer, Map<Long, Friend>> friends = new HashMap<>();// 好友列表

    private Set<Long> stranger = new HashSet<>();// 陌生人列表

    private MasterShop masterShop = new MasterShop();// 好友积分商店列表

    private Map<Integer, Integer> apprenticeAwardMap = new HashMap<>();// 师徒个数奖励列表
    private Map<Long, Map<Integer, Integer>> friAward = new HashMap<>();// 记录领取过的徒弟奖励
    /**
     * 玩家td通关信息
     */
    private Map<Integer, TD> tdMap = new HashMap<>();
    /**
     * 玩家无尽塔防信息
     */
    private EndlessTDInfo endlessTDInfo = new EndlessTDInfo();
    /**
     * 玩家塔防战力开启信息
     */
    private Map<Integer, Integer> tdBouns = new HashMap<>();
    /**
     * ui打开记录
     */
    private List<Integer> recordList = new ArrayList<>();
    /**
     * 主城小游戏
     */
    private SmallCityGame smallCityGame;

    // 道具购买临时数据
    private int price;

    private int loginNum;

    private WeekCard weekCard = new WeekCard();

    // 记录一分钟内玩家的邮件内容
    private List<Integer> mailIds = new ArrayList<>();

    private long score;

    /**
     * 母巢职位信息
     */
    private BroodWarPosition broodWarPosition;

    private CommonPb.OnlineMessage.Builder onlineMessage = CommonPb.OnlineMessage.newBuilder();
    @Setter
    @Getter
    private WarAssemble warInfos = new WarAssemble();

    public CommonPb.OnlineMessage.Builder getOnlineMessage() {
        onlineMessage.setOffLineTimes(System.currentTimeMillis() - this.getLord().getOffTime());
        return onlineMessage;
    }

    @Setter
    @Getter
    private boolean flag = true;

    /**
     * 玩家主城皮肤
     */
    private Map<Integer, CommandSkin> commandSkins = new ConcurrentHashMap<>();

    private long lastHelpTime;

    /**
     * 充值的消息
     */
    private List<RolePb.SynGoldRq> payMsg = new ArrayList<>();

    @Getter
    @Setter
    private Nation fortessNation = new Nation();// 要塞建设信息

    /**
     * 玩家钓鱼相关数据
     */
    private PlayerFishingData playerFishingData = new PlayerFishingData();

    /**
     * 登陆时间戳
     */
    @Getter
    @Setter
    public long loginTime;
    @Getter
    @Setter
    private long bookTime;// type23被打了之后 曾加一小时保护罩 一本书每天只增加一次
    @Getter
    @Setter
    private int bookFlush = 1;// 1.有气泡 2.无气泡

    private TitleAward titleAward = new TitleAward();

    private BulletWarInfo bulletWarInfo = new BulletWarInfo();

    // 网关ID
    @Getter
    @Setter
    private String gateId;
    @Getter
    @Setter
    private long channelId = -1;

    private Map<Class<?>, BaseModule> seasonActivity = new ConcurrentHashMap<>();
    private Map<Integer, BaseModule> seasonAct = new ConcurrentHashMap<>();

    public <T> T getModule(Class<T> clazz) {
        T t = (T) seasonActivity.get(clazz);
        try {
            if (t == null) {
                BaseModule baseModule = (BaseModule) clazz.newInstance();
                baseModule.setPlayer(this);
                seasonActivity.put(clazz, baseModule);
                return (T) baseModule;
            }
        } catch (Exception e) {

        }
        return t;
    }


    public Player(Lord lord, int nowTime) {
        this.roleId = lord.getLordId();
        this.setLord(lord);
        oldPos.init(lord.getPosX(), lord.getPosY());
        lastSaveTime = System.currentTimeMillis();
    }

    /**
     * 离线
     */
    public void logOut() {
        isLogin = false;
//		ctx = null;
        immediateSave = true;
        lord.setOffTime(System.currentTimeMillis());
        lord.setOlTime(onLineTime(false));
        removeOnlineTime = System.currentTimeMillis() + 600000;// 十分钟之后移出onlineMap
        pushPos.clear();
        payMsg.clear();
        flag = true;
        gateId = "";
        channelId = -1;
    }

    public void offTime() {
        lord.setOffTime(System.currentTimeMillis());
        lord.setOlTime(onLineTime(false));
    }

    /**
     * 踢出游戏
     */
//    public void tickOut() {
//        if (isLogin) {
//            getLord().setOffTime(System.currentTimeMillis());
//            getLord().setOlTime(onLineTime(false));
//            //LogHelper.logLoginOut(account, lord);
//        }
//
//        isLogin = false;
//        ctx = null;
//        immediateSave = true;
//    }

    /**
     * 玩家登录
     */
    public boolean login() {
        // 当前的凌晨时间
        // 最后一次登录的凌晨时间
        boolean flag = false;
        long todayZeroTime = TimeHelper.getZeroOfDay();
        long lastZeroTime = TimeHelper.getTimeZero(getLord().getOnTime());
        if (todayZeroTime != lastZeroTime) {
            lord.setOlTime(0);
//            lord.setLoginDays(getLord().getLoginDays() + 1);
            flag = true;
        }
        lord.setOnTime(System.currentTimeMillis());
        removeOnlineTime = 0;
        isLogin = true;
        this.loginTime = System.currentTimeMillis();

        return flag;
    }

    /**
     * 玩家在线时长
     *
     * @return
     */
    public int onLineTime(boolean flag) {
        if (lord.getOnTime() == 0L) {
            return 0;
        }
        // 当前系统时间
        // 当天的凌晨时间
        // 最后一次登录的凌晨时间
        long currentTime = System.currentTimeMillis();
        long todayZeroTime = TimeHelper.getTimeZero(currentTime);
        long lastZeroTime = TimeHelper.getTimeZero(lord.getOnTime());

        // 登录时间为当天
        if (todayZeroTime == lastZeroTime) {
            return lord.getOlTime() + (int) ((currentTime - lord.getOnTime()) / 1000);
        }
        if (flag) {
            return (int) ((System.currentTimeMillis() - TimeHelper.getZeroOfDay()) / 1000);
        } else {
            int olTime = (int) ((currentTime - TimeHelper.getZeroOfDay()) / 1000);
            lord.setOnTime(currentTime);
            lord.setOlTime(olTime);
            return olTime;
        }
    }

    public boolean isActive() {
        if (account == null) {
            // 说明lord存在，但account不存在其不在smallid表中。出现这种情况是因为手动关联了lord产生的多余数据没有处理
            // 将其加入到smallId中即可
            return false;
        }

        return account.getCreated() == 1 && getLord().getLevel() > 2;
    }

    /**
     * 是否是城防军
     *
     * @param hero
     * @return
     */
    public boolean isDefenseHero(Hero hero) {
        for (WarDefenseHero warDefenseHero : getDefenseArmyList()) {
            if (hero.getHeroId() == warDefenseHero.getHeroId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 拿到城防军
     *
     * @param heroId
     * @return
     */
    public WarDefenseHero getDefenseHero(int heroId) {
        for (WarDefenseHero warDefenseHero : getDefenseArmyList()) {
            if (warDefenseHero.getHeroId() == heroId) {
                return warDefenseHero;
            }
        }
        return null;
    }

    /**
     * data序列化
     *
     * @return
     */
    private byte[] serRoleData() {
        SerData.Builder ser = SerData.newBuilder();
        serHero(ser);
        serEquip(ser);
        serSoldier(ser);
        serDepot(ser);
        serShop(ser);
        serGift(ser);
        serBlack(ser);
        serKillEquip(ser);
        serMapStatus(ser);
        serWorldAward(ser);
        serNewState(ser);
        serLevelAwards(ser);
        serPvpHero(ser);
        serOmament(ser);
        serPlayerOmament(ser);
        serFriendList(ser);
        serMasterShop(ser);
        serApprenticeAward(ser);
        serSmallGame(ser);
        serBook(ser);
        serWarBookShop(ser);
        serCommanSkin(ser);
        serMessage(ser);
        seBullet(ser);
        seFriAward(ser);
        ser.setBookTime(this.bookTime);
        ser.setScore(this.score);
        serTitleAward(ser);
        return ser.build().toByteArray();
    }

    /**
     * 反序列化
     *
     * @param ser
     */
    private void dserRoleData(SerData ser) {
        dserHero(ser);
        dserEquip(ser);
        dserSoldier(ser);
        dserDepot(ser);
        dserShop(ser);
        dserGift(ser);
        dserBlack(ser);
        dserKillEquip(ser);
        dserMapStatus(ser);
        dserWorldAward(ser);
        dserNewState(ser);
        dserLevelAwards(ser);
        dserPvpHero(ser);
        dserFriAward(ser);
        dserFriendList(ser);
        dserOmament(ser);
        dserPlayerOmament(ser);
        dserMasterShop(ser);
        dserApprenticeAward(ser);
        dserSmallGame(ser);
        dserBook(ser);
        dserWarBookShop(ser);
        dserCommanSkin(ser);
        dserMessage(ser);
        dserBullet(ser);
        this.bookTime = ser.getBookTime();
        this.score = ser.getScore();
        dserTitleAward(ser);
    }

    public Detail serDetail() {
        Detail detail = new Detail();
        detail.setLordId(roleId);
        detail.setMaxKey(maxKey);
        detail.setPersonalSignature(personalSignature);
        detail.setRoleData(serRoleData());
        detail.setEmbattleData(serEmbattleList());
        detail.setMiningData(serMiningList());
        detail.setDefenseArmyData(serDefenseArmyInfo());
        detail.setMeetingTaskData(serMeetingTask());
        detail.setMissionData(writeMissionData());
        detail.setMail(serMailData());
        detail.setEmployData(serEmployeeData());
        detail.setBuildingData(serBuildingData());
        detail.setResourceData(serResourceData());
        detail.setAdvanceHero(serAdvanceHero());
        detail.setTaskData(serTaskData());
        detail.setMarchData(serMarchData());
        detail.setLostResData(serLostResData());
        detail.setActivityData(serActivityData());
        detail.setSimpleData(serSimpleData());
        detail.setNationData(serNationData());
        detail.setWorldPersonalGoal(serWorldPersonalGoal());
        detail.setResPacketData(serResPackets());
        detail.setSweepHeroData(serSweepHeroData());
        detail.setEffectData(serEffectData());
        detail.setBeautyData(serBeautyData());
        detail.setTdData(serTDData());
        detail.setRecordData(serRecordData());
        detail.setWorldBox(pWorldBox.toWorldBoxData());
        detail.setFrame(JSONObject.toJSON(frameMap).toString());
        detail.setPersonChat(serPersonChatData());
        detail.setDailyTask(playerDailyTask.toData());
        detail.setWeekCard(weekCard.toData());
        detail.setBroodInfo(broodWarInfo.toData());
        detail.setBuildFortress(fortessNation.serNationData());
        detail.setEndlessTDInfo(this.endlessTDInfo.serEndlessTDInfo().toByteArray());
        detail.setFishingData(this.playerFishingData.serPlayerFishingData()); // 玩家钓鱼相关数据
        return detail;
    }

    public void dserDetail(Detail detail) throws InvalidProtocolBufferException {
        roleId = detail.getLordId();
        maxKey = detail.getMaxKey();
        personalSignature = detail.getPersonalSignature();
        if (detail.getRoleData() != null) {
            SerData ser = SerData.parseFrom(detail.getRoleData());
            dserRoleData(ser);
        }

        // 布阵信息
        if (detail.getEmbattleData() != null) {
            SerEmbattleInfo serEmbattle = SerEmbattleInfo.parseFrom(detail.getEmbattleData());
            derEmbattleList(serEmbattle);
        }

        // 采集部队信息
        if (detail.getMiningData() != null) {
            SerMiningInfo serMiningInfo = SerMiningInfo.parseFrom(detail.getMiningData());
            derMiningList(serMiningInfo);
        }

        // 城防部队信息
        if (detail.getDefenseArmyData() != null) {
            SerializePb.SerDefenseArmyInfo serDefenseArmyInfo = SerializePb.SerDefenseArmyInfo.parseFrom(detail.getDefenseArmyData());
            derDefenseArmyInf(serDefenseArmyInfo);
        }

        // 作战研究室任务
        if (detail.getMeetingTaskData() != null) {
            SerializePb.SerMeetingTaskData serMeetingTaskData = SerializePb.SerMeetingTaskData.parseFrom(detail.getMeetingTaskData());
            derMeetingTaskData(serMeetingTaskData);
        }

        // 关卡信息
        if (detail.getMissionData() != null) {
            SerMission serMission = SerMission.parseFrom(detail.getMissionData());
            readMissionData(serMission);
        }

        // 建筑信息
        if (detail.getBuildingData() != null) {
            SerBuilding buildings = SerBuilding.parseFrom(detail.getBuildingData());
            dserBuildingData(buildings);
        }

        // 资源信息
        if (detail.getResourceData() != null) {
            SerResource resource = SerResource.parseFrom(detail.getResourceData());
            dserResourceData(resource);
        }

        // 雇工信息
        if (detail.getEmployData() != null) {
            SerEmployee resource = SerEmployee.parseFrom(detail.getEmployData());
            dserEmployData(resource);
        }

        // 武将突破日期
        if (detail.getAdvanceHero() != null) {
            SerAdvanceHero advanceHero = SerAdvanceHero.parseFrom(detail.getAdvanceHero());
            dserAdvanceHero(advanceHero);
        }

        // 任务系统
        if (detail.getTaskData() != null) {
            SerTask taskInfo = SerTask.parseFrom(detail.getTaskData());
            dserTaskData(taskInfo);
        }

        // 行军信息
        if (detail.getMarchData() != null) {
            SerMarchData serMarchData = SerMarchData.parseFrom(detail.getMarchData());
            dserMarchData(serMarchData);
        }

        if (detail.getMail() != null) {
            SerMailData ser = SerMailData.parseFrom(detail.getMail());
            dserMailData(ser);
        }

        if (detail.getLostResData() != null) {
            SerLostRes lostResPb = SerLostRes.parseFrom(detail.getLostResData());
            dserLostResData(lostResPb);
        }

        if (detail.getActivityData() != null) {
            SerActRecord actRecord = SerActRecord.parseFrom(detail.getActivityData());
            dserActivityData(actRecord);
        }

        if (detail.getSimpleData() != null) {
            SerSimpleData simpleData = SerSimpleData.parseFrom(detail.getSimpleData());
            dserSimpleData(simpleData);
        }

        if (detail.getNationData() != null) {
            SerNation nationData = SerNation.parseFrom(detail.getNationData());
            dserNationData(nationData);
        }
        if (detail.getWorldPersonalGoal() != null) {
            SerializePb.SerWorldPersonalGoal serWorldPersonalGoal = SerializePb.SerWorldPersonalGoal.parseFrom(detail.getWorldPersonalGoal());
            dserWorldPersonalGoal(serWorldPersonalGoal);
        }
        if (detail.getResPacketData() != null) {
            SerializePb.SerResPacket serResPacket = SerializePb.SerResPacket.parseFrom(detail.getResPacketData());
            derResPackets(serResPacket);
        }
        if (detail.getSweepHeroData() != null) {
            SerializePb.SerSweepHeroData serSweepHero = SerializePb.SerSweepHeroData.parseFrom(detail.getSweepHeroData());
            derSweepHeros(serSweepHero);
        }
        if (detail.getEffectData() != null) {
            SerializePb.SerEffectData serEffectData = SerializePb.SerEffectData.parseFrom(detail.getEffectData());
            derEffectData(serEffectData);
        }
        if (detail.getBeautyData() != null) {
            SerializePb.SerNewBeautyRecord serBeautyData = SerializePb.SerNewBeautyRecord.parseFrom(detail.getBeautyData());
            dserBeautyData(serBeautyData);
        }
        if (detail.getTdData() != null) {
            SerializePb.SerTDRecord tdData = SerializePb.SerTDRecord.parseFrom(detail.getTdData());
            dserTDData(tdData);
        }
        if (detail.getRecordData() != null) {
            SerializePb.SerMiningInfo data = SerializePb.SerMiningInfo.parseFrom(detail.getRecordData());
            dserRecordData(data);
        }
        // 世界宝箱
        if (!StringUtil.isNullOrEmpty(detail.getWorldBox())) {
            PWorldBox pWorldBox = JSONObject.parseObject(detail.getWorldBox(), PWorldBox.class);
            pWorldBox.setLordId(getLord().getLordId());
            setPWorldBox(pWorldBox);
        }
        if (!StringUtil.isNullOrEmpty(detail.getFrame())) {
            Map map = JSONObject.parseObject(detail.getFrame(), HashMap.class);
            Map<Integer, Frame> result = new HashMap<>();
            map.forEach((e, f) -> {
                Frame frame = JSONObject.parseObject(f.toString(), Frame.class);
                result.put(Integer.parseInt(e.toString()), frame);
            });
            setFrameMap(result);
        }

        if (detail.getPersonChat() != null) {
            SerializePb.SerPersonChatRoom serPersonChatRoom = SerializePb.SerPersonChatRoom.parseFrom(detail.getPersonChat());
            dserPersonChatData(serPersonChatRoom);
        }

        if (!StringUtil.isNullOrEmpty(detail.getDailyTask())) {
            playerDailyTask.serData(detail.getDailyTask());
        }

        if (!StringUtil.isNullOrEmpty(detail.getWeekCard())) {
            WeekCard data = new WeekCard();
            data.serData(detail.getWeekCard());
            weekCard = data;
        }
        if (detail.getBroodInfo() != null) {
            BroodWarInfo info = new BroodWarInfo();
            info.reloadData(detail.getBroodInfo());
            this.broodWarInfo = info;
        }

        if (detail.getBuildFortress() != null) {
            SerNation nationData = SerNation.parseFrom(detail.getBuildFortress());
            fortessNation.dserNationData(nationData);
        }
        // 无尽塔防
        if (detail.getEndlessTDInfo() != null) {
            SerEndlessTDInfo serEndlessTDInfo = SerEndlessTDInfo.parseFrom(detail.getEndlessTDInfo());
            this.endlessTDInfo.dserEndlessTDInfo(serEndlessTDInfo);
        }

        // 钓鱼
        if (detail.getFishingData() != null) {
            SerPlayerFishingDataPB serPlayerFishingData = SerPlayerFishingDataPB.parseFrom(detail.getFishingData());
            this.playerFishingData.dserPlayerFishingData(serPlayerFishingData);
        }
    }

    /**
     * 序列化将领
     */
    private void serHero(SerData.Builder ser) {
        Iterator<Hero> it = getHeros().values().iterator();
        while (it.hasNext()) {
            Hero hero = it.next();

            ser.addHero(hero.writeData());
        }
    }

    /**
     * 反序列化将领
     */
    private void dserHero(SerData ser) {
        List<DataPb.HeroData> list = ser.getHeroList();
        for (DataPb.HeroData heroPb : list) {
            Hero hero = new Hero();
            hero.readData(heroPb);
            hero.fixDb();
            heros.put(hero.getHeroId(), hero);
        }
    }

    /**
     * 序列化武器背包
     */
    private void serEquip(SerData.Builder ser) {
        Iterator<Equip> it = getEquips().values().iterator();
        while (it.hasNext()) {
            Equip equipment = it.next();
            ser.addEquip(equipment.wrapPb());
        }
    }

    /**
     * 反序列化背包
     */
    private void dserEquip(SerData ser) {
        List<CommonPb.Equip> list = ser.getEquipList();
        for (CommonPb.Equip equipPb : list) {
            Equip equip = new Equip();
            equip.unwrapPb(equipPb);
            equips.put(equip.getKeyId(), equip);
        }
    }

    /**
     * 序列化兵书背包
     */
    private void serBook(SerData.Builder ser) {
        Iterator<WarBook> it = getWarBooks().values().iterator();
        while (it.hasNext()) {
            WarBook bookment = it.next();
            ser.addBook(bookment.wrapPb());
        }
    }

    /**
     * 反序列化兵书背包
     */
    private void dserBook(SerData ser) {
        List<CommonPb.WarBook> list = ser.getBookList();
        for (CommonPb.WarBook bookPb : list) {
            WarBook book = new WarBook();
            book.unwrapPb(bookPb);
            warBooks.put(book.getKeyId(), book);
        }
    }

    /**
     * 序列化兵书商城物品
     */
    private void serWarBookShop(SerData.Builder ser) {
        Iterator<CommonPb.WarBookShopItem> it = getWarBookShops().values().iterator();
        while (it.hasNext()) {
            CommonPb.WarBookShopItem next = it.next();
            ser.addItem(next);
        }
    }

    /**
     * 反序列化兵书商城物品
     */
    private void dserWarBookShop(SerData ser) {
        List<CommonPb.WarBookShopItem> itemList = ser.getItemList();
        for (CommonPb.WarBookShopItem warBookShopItem : itemList) {
            warBookShops.put(warBookShopItem.getPos(), warBookShopItem);
        }
    }

    // 序列化配饰
    private void serOmament(SerData.Builder ser) {
        for (Omament omament : omaments.values()) {
            if (omament == null) {
                continue;
            }
            ser.addOmament(omament.writeData());
        }
    }

    // 反序列化配饰
    public void dserOmament(SerData ser) {
        omaments.clear();
        for (CommonPb.Omament data : ser.getOmamentList()) {
            if (data == null) {
                continue;
            }

            Omament omament = new Omament();
            omament.readData(data);
            omaments.put(data.getOmamentId(), omament);
        }
    }

    // 序列化配饰穿戴
    private void serPlayerOmament(SerData.Builder ser) {
        for (PlayerOmament playerOmament : playerOmaments.values()) {
            if (playerOmament == null) {
                continue;
            }
            ser.addPlayerOmament(playerOmament.writeData());
        }
    }

    // 反序列化配饰穿戴
    public void dserPlayerOmament(SerData ser) {
        playerOmaments.clear();
        for (CommonPb.PlayerOmament data : ser.getPlayerOmamentList()) {
            if (data == null) {
                continue;
            }

            PlayerOmament playerOmament = new PlayerOmament();
            playerOmament.readData(data);
            playerOmaments.put(data.getPos(), playerOmament);
        }
    }

    // 序列化主城皮肤
    private void serCommanSkin(SerData.Builder ser) {
        for (CommandSkin commandSkin : commandSkins.values()) {
            if (commandSkin == null) {
                continue;
            }
            ser.addSkinData(commandSkin.writeData());
        }
    }

    // 反序列化主城皮肤
    public void dserCommanSkin(SerData ser) {
        commandSkins.clear();
        for (DataPb.SkinData skinData : ser.getSkinDataList()) {
            if (skinData == null) {
                continue;
            }
            CommandSkin commandSkin = new CommandSkin();
            commandSkin.readData(skinData);
            commandSkins.put(skinData.getKeyId(), commandSkin);
        }
    }

    private void serMessage(SerData.Builder ser) {
        ser.setOnMessage(getOnlineMessage());
    }

    public void dserMessage(SerData ser) {
        this.setOnlineMessage(ser.getOnMessage().toBuilder());
    }

    private void seFriAward(SerData.Builder ser) {
        Map<Long, Map<Integer, Integer>> friAward = getFriAward();
        Iterator<Entry<Long, Map<Integer, Integer>>> iterator = friAward.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Long, Map<Integer, Integer>> next = iterator.next();
            Long key = next.getKey();
            SerFriAwardPB.Builder builder = SerFriAwardPB.newBuilder();
            builder.setRoleId(key);
            Map<Integer, Integer> value = next.getValue();
            value.entrySet().forEach(x -> {
                CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
                builder1.setV1(x.getKey());
                builder1.setV2(x.getValue());
                builder.addTwoInt(builder1);
            });
            ser.addFriAward(builder);
        }
    }

    public void dserFriAward(SerData ser) {
        List<SerFriAwardPB> friAwardList = ser.getFriAwardList();
        friAwardList.forEach(x -> {
            long roleId = x.getRoleId();
            Map<Integer, Integer> integerIntegerMap = this.friAward.computeIfAbsent(roleId, y -> new HashMap<>());
            List<CommonPb.TwoInt> twoIntList = x.getTwoIntList();
            for (CommonPb.TwoInt twoInt : twoIntList) {
                integerIntegerMap.put(twoInt.getV1(), twoInt.getV2());
            }
        });
    }

    /**
     * 序列化布阵[军事学院]
     */
    private byte[] serEmbattleList() {
        SerEmbattleInfo.Builder ser = SerEmbattleInfo.newBuilder();
        for (Integer item : getEmbattleList()) {
            ser.addEmbattleHero(item);
        }

        for (Integer heroId : massHeroes) {
            ser.addMassHero(heroId);
        }

        return ser.build().toByteArray();
    }

    /**
     * 反序列化布阵、武将集结[军事学院]
     */
    private void derEmbattleList(SerEmbattleInfo ser) {
        getEmbattleList().clear();
        List<Integer> list = ser.getEmbattleHeroList();
        for (Integer item : list) {
            getEmbattleList().add(item);
        }

        massHeroes.clear();
        List<Integer> massList = ser.getMassHeroList();
        for (Integer heroId : massList) {
            massHeroes.add(heroId);
        }
    }

    /**
     * 序列化采集部队布阵
     */
    private byte[] serMiningList() {
        SerMiningInfo.Builder ser = SerMiningInfo.newBuilder();
        for (Integer item : getMiningList()) {
            ser.addMiningHero(item);
        }
        return ser.build().toByteArray();
    }

    /**
     * 反序列化采集部队布阵
     */
    private void derMiningList(SerMiningInfo ser) {
        getMiningList().clear();
        List<Integer> list = ser.getMiningHeroList();
        for (Integer item : list) {
            getMiningList().add(item);
        }
    }

    /**
     * 序列化城防部队
     */
    private byte[] serDefenseArmyInfo() {
        SerializePb.SerDefenseArmyInfo.Builder ser = SerializePb.SerDefenseArmyInfo.newBuilder();
        getDefenseArmyList().forEach(hero -> {
            CommonPb.DefenseHeroSoldier.Builder defenseHeroSoldier = CommonPb.DefenseHeroSoldier.newBuilder();
            defenseHeroSoldier.setHeroId(hero.getHeroId());
            defenseHeroSoldier.setLastRefreshTime(hero.getLastRefreshTime());
            defenseHeroSoldier.setIsAddSoldier(hero.isAddSoldier());
            Hero h = getHero(hero.getHeroId());
            int num = 0;
            if (h != null) {
                num = h.getCurrentSoliderNum();
            }
            defenseHeroSoldier.setSoldierNum(num);
            ser.addDefenseHeroSoldiers(defenseHeroSoldier);

        });
        return ser.build().toByteArray();

    }

    // TODO jyb 世界目标个人目标序列化
    private byte[] serWorldPersonalGoal() {
        SerializePb.SerWorldPersonalGoal.Builder ser = SerializePb.SerWorldPersonalGoal.newBuilder();
        personalGoals.forEach((key, value) -> ser.addWorldPersonalGoal(value.writeData()));
        return ser.build().toByteArray();
    }

    // TODO jyb 世界目标个人进度反序列化
    private void dserWorldPersonalGoal(SerializePb.SerWorldPersonalGoal goal) {
        for (CommonPb.WorldPersonalGoal worldPersonalGoaPb : goal.getWorldPersonalGoalList()) {
            WorldPersonalGoal worldPersonalGoal = new WorldPersonalGoal();
            worldPersonalGoal.setProcess(worldPersonalGoaPb.getProcess());
            worldPersonalGoal.setTaskId(worldPersonalGoaPb.getTaskId());
            worldPersonalGoal.setWorldState(worldPersonalGoaPb.getWorldState());
            worldPersonalGoal.setState(worldPersonalGoaPb.getState());
            worldPersonalGoal.setChallengeNumber(worldPersonalGoaPb.getChallengeNumber());
            worldPersonalGoal.setLastAttackBossTime(worldPersonalGoaPb.getLastAttackBossTime());
            personalGoals.put(worldPersonalGoal.getTaskId(), worldPersonalGoal);
        }
    }

    public void derResPackets(SerializePb.SerResPacket serResPacket) {
        resPackets.clear();
        serResPacket.getResPacketsList().forEach(p -> {
            ResourcePacket packet = new ResourcePacket();
            packet.setResId(p.getResPacketInfo().getResId());
            packet.setPacketNum(p.getResPacketInfo().getPacketNum());
            packet.setPacketTime(p.getResPacketTime());
            resPackets.put(packet.getResId(), packet);
        });
    }

    public void derSweepHeros(SerializePb.SerSweepHeroData serSweepHeroData) {
        sweepHeroList.clear();
        sweepHeroList.addAll(serSweepHeroData.getSweepHeroList());
    }

    public void derEffectData(SerializePb.SerEffectData serEffectData) {
        effects.clear();
        serEffectData.getEffectsList().forEach(effectPb -> {
            Effect effect = new Effect();
            effect.setEffectId(effectPb.getEffectId());
            effect.setEffect(effectPb.getEffect());
            effect.setBeginTime(effectPb.getBeginTime());
            effect.setEndTime(effectPb.getEndTime());
            effects.put(effect.getEffectId(), effect);
        });

    }

    public byte[] serResPackets() {
        SerializePb.SerResPacket.Builder builder = SerializePb.SerResPacket.newBuilder();
        for (Entry<Integer, ResourcePacket> entry : resPackets.entrySet()) {
            builder.addResPackets(entry.getValue().wrapPb());
        }
        return builder.build().toByteArray();
    }

    public byte[] serSweepHeroData() {
        SerializePb.SerSweepHeroData.Builder builder = SerializePb.SerSweepHeroData.newBuilder();
        builder.addAllSweepHero(sweepHeroList);
        return builder.build().toByteArray();
    }

    public byte[] serEffectData() {
        SerializePb.SerEffectData.Builder builder = SerializePb.SerEffectData.newBuilder();
        for (Map.Entry<Integer, Effect> effectEntry : effects.entrySet()) {
            Effect effect = effectEntry.getValue();
            CommonPb.Effect.Builder effectPb = CommonPb.Effect.newBuilder();
            effectPb.setEffectId(effect.getEffectId());
            effectPb.setEffect(effect.getEffect());
            effectPb.setBeginTime(effect.getBeginTime());
            effectPb.setEndTime(effect.getEndTime());
            builder.addEffects(effectPb);

        }
        return builder.build().toByteArray();
    }

    /**
     * 序列化战斗
     *
     * @return
     */
    private byte[] serMeetingTask() {
        SerializePb.SerMeetingTaskData.Builder ser = SerializePb.SerMeetingTaskData.newBuilder();
        ser.setId(meetingTask.getId());
        ser.setProcess(meetingTask.getProcess());
        ser.setState(meetingTask.getState());
        ser.setStartTime(meetingTask.getStartTime());
        return ser.build().toByteArray();
    }

    /**
     * 反序列化城防部队
     *
     * @param ser
     */
    private void derDefenseArmyInf(SerializePb.SerDefenseArmyInfo ser) {
        getDefenseArmyList().clear();
        ser.getDefenseHeroSoldiersList().forEach((hero -> {
            WarDefenseHero warDefenseHero = new WarDefenseHero();
            warDefenseHero.setHeroId(hero.getHeroId());
            warDefenseHero.setLastRefreshTime(hero.getLastRefreshTime());
            warDefenseHero.setAddSoldier(hero.getIsAddSoldier());
            getDefenseArmyList().add(warDefenseHero);
        }));
    }

    /**
     * 反序列化作战研究站任务
     *
     * @param ser
     */
    private void derMeetingTaskData(SerializePb.SerMeetingTaskData ser) {
        meetingTask = new MeetingTask();
        meetingTask.setId(ser.getId());
        meetingTask.setProcess(ser.getProcess());
        meetingTask.setState(ser.getState());
        meetingTask.setStartTime(ser.getStartTime());
    }

    /**
     * 序列化士兵
     */
    private void serSoldier(SerData.Builder ser) {
        Iterator<Soldier> it = getSoldiers().values().iterator();
        while (it.hasNext()) {
            Soldier solider = it.next();
            ser.addSoldier(solider.wrapPb());
        }
    }

    private void serDepot(SerData.Builder ser) {
        Iterator<Depot> it = getDepots().iterator();
        while (it.hasNext()) {
            Depot depot = it.next();
            ser.addDeport(depot.serDb());
        }
    }

    private void serShop(SerData.Builder ser) {
        Iterator<Shop> it = shops.values().iterator();
        while (it.hasNext()) {
            Shop shop = it.next();
            ser.addShopData(shop.serDb());
        }
    }

    private void serGift(SerData.Builder ser) {
        for (Integer giftId : vipGifts) {
            ser.addGiftId(giftId);
        }
    }

    private void serBlack(SerData.Builder ser) {
        for (Long blackId : blackList) {
            ser.addBlackId(blackId);
        }
    }

    // 写数据库
    public void serKillEquip(SerData.Builder ser) {
        for (KillEquip killEquip : killEquipMap.values()) {
            if (killEquip == null) {
                continue;
            }
            ser.addKillEquip(killEquip.writeData());
        }
    }

    // 写地图状态
    public void serMapStatus(SerData.Builder ser) {
        for (MapStatus mapStatus : getMapStatusMap().values()) {
            if (mapStatus == null) {
                LogHelper.CONFIG_LOGGER.info("mapStatus is null");
                continue;
            }

            if (mapStatus.getMapId() == 0) {
                continue;
            }
            ser.addMapStatus(mapStatus.writeData());
        }
    }

    // 写世界奖励
    public void serWorldAward(SerData.Builder ser) {
        for (WorldTargetAward award : worldTargetAwardMap.values()) {
            if (award == null) {
                LogHelper.CONFIG_LOGGER.info("mapStatus is null");
                continue;
            }
            ser.addWorldAward(award.writeData());
        }
    }

    // 新手引导写数据库
    public void serNewState(SerData.Builder ser) {
        for (Integer state : newStateDone) {
            ser.addNewState(state);
        }
    }

    // 升级奖励
    public void serLevelAwards(SerData.Builder ser) {
        for (Map.Entry<Integer, LevelAward> awardEntry : getLevelAwardsMap().entrySet()) {
            if (awardEntry == null) {
                continue;
            }

            LevelAward levelAward = awardEntry.getValue();
            if (levelAward == null) {
                continue;
            }
            ser.addLevelAwards(levelAward.writeData());
        }
    }

    public void serPvpHero(SerData.Builder ser) {
        for (PvpHero pvpHero : pvpHeroMap.values()) {
            if (pvpHero == null) {
                continue;
            }

            ser.addPvpHero(pvpHero.writeData());
        }
    }

    // 新手引导读取数据库
    public void dserNewState(SerData ser) {
        newStateDone.clear();
        for (Integer newState : ser.getNewStateList()) {
            newStateDone.add(newState);
        }
    }

    // 升级奖励存盘
    public void dserLevelAwards(SerData ser) {
        getLevelAwardsMap().clear();
        for (LevelAwards data : ser.getLevelAwardsList()) {
            if (data == null) {
                continue;
            }
            int level = data.getLevel();
            LevelAward award = new LevelAward();
            award.readData(data);
            getLevelAwardsMap().put(level, award);
        }

    }

    // 血战英雄
    public void dserPvpHero(SerData ser) {
        pvpHeroMap.clear();
        for (DataPb.PvpHeroData data : ser.getPvpHeroList()) {
            if (data == null) {
                continue;
            }

            PvpHero pvpHero = new PvpHero();
            pvpHero.readData(data);
            pvpHeroMap.put(data.getHeroId(), pvpHero);
        }
    }

    // 读数据库
    public void dserKillEquip(SerData ser) {
        killEquipMap.clear();
        List<DataPb.KillEquipData> list = ser.getKillEquipList();
        for (DataPb.KillEquipData data : list) {
            KillEquip killEquip = new KillEquip();
            killEquip.readData(data);
            killEquipMap.put(killEquip.getEquipId(), killEquip);
        }
    }

    // 读地图状态
    public void dserMapStatus(SerData ser) {
        getMapStatusMap().clear();
        List<DataPb.MapStatusData> list = ser.getMapStatusList();
        if (list == null) {
            LogHelper.CONFIG_LOGGER.info("dserMapStatus list is null!");
            return;
        }

        for (DataPb.MapStatusData data : list) {
            MapStatus mapStatus = new MapStatus();
            mapStatus.readData(data);
            getMapStatusMap().put(mapStatus.getMapId(), mapStatus);
        }

    }

    // 读世界奖励
    public void dserWorldAward(SerData ser) {
        worldTargetAwardMap.clear();
        List<DataPb.WorldTargetAwardData> list = ser.getWorldAwardList();
        if (list == null) {
            LogHelper.CONFIG_LOGGER.info("dserWorldAward list is null!");
            return;
        }

        for (DataPb.WorldTargetAwardData data : list) {
            WorldTargetAward award = new WorldTargetAward();
            award.readData(data);
            worldTargetAwardMap.put(award.getTargetId(), award);
        }
    }

    /**
     * 反序列化士兵
     */
    private void dserSoldier(SerData der) {
        soldiers.clear();
        List<CommonPb.Soldier> serSoldiers = der.getSoldierList();
        for (CommonPb.Soldier item : serSoldiers) {
            if (item == null) {
                continue;
            }
            Soldier soldier = new Soldier();
            soldier.unWrapPb(item);
            if (soldier.getSoldierIndex() == 0) {
                soldier.setSoldierIndex(soldier.getSoldierType());
            }
            soldiers.put(soldier.getSoldierIndex(), soldier);
        }
    }

    /**
     * 反序列化聚宝盆
     */
    private void dserDepot(SerData der) {
        depots.clear();
        List<CommonPb.Depot> serDepots = der.getDeportList();
        for (CommonPb.Depot pbDepot : serDepots) {
            if (pbDepot == null) {
                continue;
            }
            depots.add(new Depot(pbDepot));
        }
    }

    private void dserShop(SerData der) {
        shops.clear();
        List<DataPb.ShopData> serShops = der.getShopDataList();
        for (DataPb.ShopData pbShop : serShops) {
            if (pbShop == null) {
                continue;
            }
            shops.put(pbShop.getPropId(), new Shop(pbShop));
        }
    }

    private void dserGift(SerData der) {
        vipGifts.clear();
        List<Integer> glist = der.getGiftIdList();
        for (Integer giftId : glist) {
            if (giftId == null) {
                continue;
            }
            vipGifts.add(giftId);
        }
    }

    public void setDefenseArmyList(List<WarDefenseHero> defenseArmyList) {
        if (this.defenseArmyList.size() < 1) {
            this.defenseArmyList.addAll(defenseArmyList);
            return;
        }
        Iterator<WarDefenseHero> iterator = this.defenseArmyList.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            WarDefenseHero warDefenseHero = iterator.next();
            if (warDefenseHero.getHeroId() > 0) {
                i++;
                continue;
            }
            this.defenseArmyList.set(i, defenseArmyList.get(i));
            i++;
        }
    }

    private void dserBlack(SerData der) {
        blackList.clear();
        List<Long> blist = der.getBlackIdList();
        for (Long blackId : blist) {
            if (blackId == null) {
                continue;
            }
            blackList.add(blackId);
        }
    }

    private void serFriendList(SerData.Builder ser) {

        for (Map.Entry<Integer, Map<Long, Friend>> friendMap : friends.entrySet()) {
            if (friendMap == null) {
                continue;
            }

            Map<Long, Friend> friendList = friendMap.getValue();
            if (friendList == null) {
                continue;
            }
            List<Good.Builder> goodList = new ArrayList<>();

            for (Friend friend : friendList.values()) {
                Good.Builder builder = Good.newBuilder();
                if (friend == null) {
                    continue;
                }
                builder.setLordId(friend.getRolaId());
                builder.setType(friend.getType());
                builder.setTime(friend.getApplyTime());
                builder.setOnceApprenticeLv(friend.getOnceApprenticeLv());
                builder.setLevel(friend.getLevel());
                goodList.add(builder);
            }
            for (Good.Builder builder : goodList) {
                ser.addFriendList(builder);
            }
        }
    }

    private void dserFriendList(SerData der) {
        friends.clear();
        friends.put(FriendType.APPLY, new ConcurrentHashMap<>());
        friends.put(FriendType.APPRENTICE, new ConcurrentHashMap<>());
        friends.put(FriendType.FRIEND, new ConcurrentHashMap<>());
        friends.put(FriendType.MASTER, new ConcurrentHashMap<>());
        friends.put(FriendType.ONCE_APPRENTICE, new ConcurrentHashMap<>());
        friends.put(FriendType.APPLY_MASTER, new ConcurrentHashMap<>());
        List<Good> friendList = der.getFriendListList();
        StaticFriendMgr staticFriendMgr = SpringUtil.getBean(StaticFriendMgr.class);
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        Map<Integer, StaticApprenticeAward> apprenticeAward = staticFriendMgr.getApprenticeAward();// 配置积分商城数据
        boolean flag = false;
        if (this.friAward.isEmpty()) {
            flag = true;
        }
        for (Good good : friendList) {
            if (good == null) {
                continue;
            }
            if (friends.get(good.getType()) != null) {
                friends.get(good.getType()).put(good.getLordId(), new Friend(good.getType(), good.getLordId(), good.getTime(), good.getOnceApprenticeLv(), good.getLevel()));
                if (flag && good.getType() == FriendType.APPRENTICE) {
                    Map<Integer, Integer> integerIntegerMap = friAward.computeIfAbsent(good.getLordId(), x -> new HashMap<>());
                    if (apprenticeAward != null) {
                        Player player1 = playerManager.getPlayer(good.getLordId());// 徒弟
                        if (player1 != null) {
                            apprenticeAward.values().forEach(x -> {
                                if (integerIntegerMap.get(x.getCond()) == null && player1.getLevel() >= x.getCond()) {
                                    integerIntegerMap.put(x.getCond(), 1);
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void serApprenticeAward(SerData.Builder ser) {
        if (apprenticeAwardMap == null) {
            apprenticeAwardMap = new HashMap<>();
        }
        for (Map.Entry<Integer, Integer> apprenticeAward : apprenticeAwardMap.entrySet()) {
            if (apprenticeAward == null) {
                continue;
            }

            CommonPb.ApprenticeAward.Builder builder = CommonPb.ApprenticeAward.newBuilder();
            Integer id = apprenticeAward.getKey();
            Integer count = apprenticeAward.getValue();
            if (id == null || count == null) {
                continue;
            }
            builder.setId(id);
            builder.setCount(count);
            ser.addApprenticeAward(builder);
        }
    }

    private void serSmallGame(SerData.Builder ser) {
        if (smallCityGame != null) {
            SerializePb.SerSmallGame.Builder builder = SerializePb.SerSmallGame.newBuilder();
            builder.setTotal(smallCityGame.getTotal());
            builder.setLastRefushTime(smallCityGame.getLastRefushTime());
            smallCityGame.getWorms().forEach((e, f) -> {
                builder.addWorms(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
            });
            smallCityGame.getRewards().forEach((e, f) -> {
                builder.addRewards(CommonPb.TwoInt.newBuilder().setV1(e).setV2(f).build());
            });
            ser.setSmallGame(builder);
        }
    }

    private void dserApprenticeAward(SerData der) {
        apprenticeAwardMap.clear();
        List<CommonPb.ApprenticeAward> awards = der.getApprenticeAwardList();
        for (CommonPb.ApprenticeAward award : awards) {
            apprenticeAwardMap.put(award.getId(), award.getCount());
        }
    }

    private void dserSmallGame(SerData der) {
        smallCityGame = new SmallCityGame(der.getSmallGame());
    }

    private void serMasterShop(SerData.Builder ser) {
        if (masterShop.getMasterShopAward() == null) {
            masterShop.setMasterShopAward(new HashMap<>());
        }
        CommonPb.MasterShop.Builder builder = CommonPb.MasterShop.newBuilder();
        for (Map.Entry<Integer, Integer> masterShop : masterShop.getMasterShopAward().entrySet()) {
            if (masterShop == null) {
                continue;
            }

            Integer id = masterShop.getKey();
            Integer count = masterShop.getValue();
            if (id == null || count == null) {
                continue;
            }
            MasterShopAward.Builder award = MasterShopAward.newBuilder();
            award.setId(id);
            award.setCount(count);
            builder.addMasterShopAward(award);
        }
        builder.setScore(masterShop.getScore());
        ser.setMasterShop(builder);
    }

    private void dserMasterShop(SerData der) {
        CommonPb.MasterShop shop = der.getMasterShop();
        List<MasterShopAward> shopAwardList = shop.getMasterShopAwardList();
        masterShop.setScore(shop.getScore());
        for (MasterShopAward m : shopAwardList) {
            masterShop.getMasterShopAward().put(m.getId(), m.getCount());
        }
    }

    // read data
    private void readMissionData(SerMission ser) {
        missions.clear();
        List<DataPb.MissionData> sermissions = ser.getMissionList();
        for (DataPb.MissionData item : sermissions) {
            if (item == null) {
                continue;
            }

            Mission missionData = new Mission();
            missionData.readData(item);
            int mapId = missionData.getMapId();

            Map<Integer, Mission> missionMap = missions.get(mapId);
            if (missionMap == null) {
                missionMap = new HashMap<Integer, Mission>();
                missionMap.put(missionData.getMissionId(), missionData);
                missions.put(missionData.getMapId(), missionMap);
            } else {
                missionMap.put(missionData.getMissionId(), missionData);
            }
        }
        getMissionStar().clear();
        List<DataPb.MissionStarData> serMissionStar = ser.getStarList();
        for (DataPb.MissionStarData data : serMissionStar) {
            if (data == null) {
                continue;
            }
            TreeMap<Integer, Integer> state = getMissionStar().get(data.getId());
            if (state == null) {
                state = new TreeMap<Integer, Integer>();
                for (int i = 1; i <= 3; i++) {
                    state.put(i, MissionStar.MISSION_STAR_CLOSE);
                }
            }

            List<Integer> stateList = data.getStateList();
            for (int i = 0; i < stateList.size(); i++) {
                state.put(i + 1, stateList.get(i));
            }
            getMissionStar().put(data.getId(), state);
        }
    }

    // wirte data
    private byte[] writeMissionData() {
        SerMission.Builder ser = SerMission.newBuilder();
        for (Map.Entry<Integer, Map<Integer, Mission>> mapItem : missions.entrySet()) {
            if (mapItem == null) {
                continue;
            }

            Map<Integer, Mission> missionMap = mapItem.getValue();
            if (missionMap == null) {
                continue;
            }

            for (Map.Entry<Integer, Mission> missionItem : missionMap.entrySet()) {
                if (missionItem == null || missionItem.getValue() == null) {
                    continue;
                }
                Mission mission = missionItem.getValue();
                ser.addMission(mission.writeData());
            }
        }

        for (Map.Entry<Integer, TreeMap<Integer, Integer>> starItem : getMissionStar().entrySet()) {
            if (starItem == null) {
                continue;
            }
            Map<Integer, Integer> stateMap = starItem.getValue();
            if (stateMap == null) {
                continue;
            }

            DataPb.MissionStarData.Builder starData = DataPb.MissionStarData.newBuilder();
            starData.setId(starItem.getKey());
            for (Map.Entry<Integer, Integer> stateItem : stateMap.entrySet()) {
                if (stateItem == null || starItem.getValue() == null) {
                    continue;
                }
                int state = stateItem.getValue();
                starData.addState(state);
            }
            ser.addStar(starData.build());
        }

        return ser.build().toByteArray();
    }

    // read data
    private void dserMailData(SerMailData ser) {
        mails.clear();
        repots.clear();
        sysmail.clear();
        pmails.clear();
        List<DataPb.MailData> mailDatas = ser.getMailDataList();
        long now = System.currentTimeMillis();
        for (DataPb.MailData item : mailDatas) {
            if (item == null) {
                continue;
            }

            if (item.getCreateTime() + TimeHelper.DAY_MS * 7 <= now) {
                continue;
            }

            Mail mail = new Mail(item);
            mails.add(mail);
        }
    }

    private byte[] serMailData() {
        SerMailData.Builder ser = SerMailData.newBuilder();
        if (!mails.isEmpty()) {
            Iterator<Mail> it = mails.iterator();
            while (it.hasNext()) {
                Mail next = it.next();
                if (next == null) {
                    continue;
                }
                ser.addMailData(next.serMailData());
            }
        }

        return ser.build().toByteArray();
    }

    // ser building,雇佣需要提前序列化
    private byte[] serBuildingData() {
        SerBuilding.Builder builder = SerBuilding.newBuilder();
        Command command = buildings.getCommand();
        builder.setCommandInfo(command.wrapPb());
        builder.setBuildTeams(buildings.getBuildingTeams());
        // 兵营
        Camp camp = buildings.getCamp();
        builder.setCamp(camp.wrapPb());
        // 资源建筑
        ResBuildings resBuildings = buildings.getResBuildings();
        builder.setResBuilding(resBuildings.wrapPb());
        // 科技
        Tech tech = buildings.getTech();
        builder.setTech(tech.wrapPb());
        // wall
        Wall wall = buildings.getWall();
        builder.setWall(wall.wrapPb());
        // ware
        Ware ware = buildings.getWare();
        builder.setWare(ware.wrapPb());
        // workshop
        WorkShop workShop = buildings.getWorkShop();
        builder.setWorkShop(workShop.wrapPb());
        // 建造队列
        ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
        for (BuildQue buildQue : buildQues) {
            builder.addBuildQue(buildQue.wrapPb());
        }

        // 士兵募兵队列
        for (Map.Entry<Integer, Soldier> soldierElem : soldiers.entrySet()) {
            Soldier soldier = soldierElem.getValue();
            if (soldier == null) {
                continue;
            }
            LinkedList<WorkQue> workQues = soldier.getWorkQues();
            // 士兵建造队列
            for (WorkQue workQue : workQues) {
                builder.addSoldierWorkQue(workQue.wrapPb());
            }
        }

        // 序列化科技等级
        Map<Integer, TechInfo> techLevelInfoMap = tech.getTechInfoMap();
        for (Map.Entry<Integer, TechInfo> entry : techLevelInfoMap.entrySet()) {
            TechInfo techInfo = entry.getValue();
            if (techInfo == null) {
                continue;
            }

            builder.addTechData(techInfo.wrapData());
        }

        // 序列化科技进度
        LinkedList<TechQue> techQues = tech.getTechQues();
        for (TechQue techQue : techQues) {
            builder.addTechQueData(techQue.wrapData());
        }

        // 写生产队列到数据库
        Map<Integer, WsWorkQue> wsWorkQueMap = workShop.getWorkQues();
        for (WsWorkQue wsWorkQue : wsWorkQueMap.values()) {
            if (wsWorkQue != null) {
                builder.addWsWorkQueData(wsWorkQue.writeData());
            }
        }

        // 写预设队列到数据库
        Map<Integer, WsWaitQue> wsWaitQueMap = workShop.getWaitQues();
        for (WsWaitQue wsWaitQue : wsWaitQueMap.values()) {
            if (wsWaitQue != null) {
                builder.addWsWaitQueData(wsWaitQue.writeData());
            }
        }

        // 装备建造队列
        LinkedList<WorkQue> equipWorkQues = buildings.getEquipWorkQue();
        if (equipWorkQues != null && !equipWorkQues.isEmpty()) {
            try {
                for (WorkQue workQue : equipWorkQues) {
                    builder.addEquipWorkQue(workQue.wrapPb());
                }
            } catch (Exception e) {
                System.out.println("异常," + equipWorkQues.size());
                equipWorkQues.clear();
            }
        }

        // 参谋部
        Staff staff = buildings.getStaff();
        builder.setStaff(staff.wrapPb());

        // 市场
        Market market = buildings.getMarket();
        builder.setMarket(market.wrapPb());
        builder.addAllRecoverData(buildings.getRecoverBuilds());
        return builder.build().toByteArray();
    }

    // dser building, 一定要注意building 存盘大小不要超过64k
    private void dserBuildingData(SerBuilding serBuilding) {
        Command command = buildings.getCommand();
        if (command != null) {
            command.unwrapPb(serBuilding.getCommandInfo());
        }

        if (serBuilding.hasBuildTeams()) {
            buildings.setBuildingTeams(serBuilding.getBuildTeams());
        }

        // 兵营
        Camp camp = buildings.getCamp();
        if (serBuilding.hasCamp()) {
            camp.unwrapPb(serBuilding.getCamp());
        }

        // 资源建筑
        ResBuildings resBuildings = buildings.getResBuildings();
        if (serBuilding.hasResBuilding()) {
            resBuildings.unwrapPb(serBuilding.getResBuilding());
        }

        // 科技
        Tech tech = buildings.getTech();
        if (serBuilding.hasTech()) {
            tech.unwrapPb(serBuilding.getTech());
        }

        // wall
        Wall wall = buildings.getWall();
        if (serBuilding.hasWall()) {
            wall.unwrapPb(serBuilding.getWall());
        }

        // ware
        Ware ware = buildings.getWare();
        if (serBuilding.hasWare()) {
            ware.unwrapPb(serBuilding.getWare());
        }

        // workShop
        WorkShop workShop = buildings.getWorkShop();
        if (serBuilding.hasWorkShop()) {
            workShop.unwrapPb(serBuilding.getWorkShop());
        }

        // 建造队列
        ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
        List<CommonPb.BuildQue> buildQuesListPb = serBuilding.getBuildQueList();
        for (CommonPb.BuildQue buildQuePb : buildQuesListPb) {
            BuildQue buildQue = new BuildQue();
            buildQue.unwrapPb(buildQuePb);
            buildQues.add(buildQue);
        }

        // 募兵队列
        List<CommonPb.WorkQue> workQueListPb = serBuilding.getSoldierWorkQueList();
        for (CommonPb.WorkQue workQuePb : workQueListPb) {
            WorkQue workQue = new WorkQue();
            workQue.unwrapPb(workQuePb);
            int buildingId = workQue.getBuildingId();
            // 兵营Id和类型一致
            if (buildingId < BuildingType.ROCKET_CAMP || buildingId > BuildingType.MILITIA_CAMP) {
                continue;
            }
            int soldierIndex = GameHelper.getSoldierIndexByBuildingId(buildingId);
            if (soldierIndex == -1) {
                LogHelper.CONFIG_LOGGER.info("soldierIndex error!!!");
                continue;
            }
            Soldier soldier = soldiers.get(soldierIndex);
            if (soldier == null) {
                LogHelper.CONFIG_LOGGER.info("soldier is null soldierIndex:{}", soldierIndex);
                continue;
            }
            LinkedList<WorkQue> workQues = soldier.getWorkQues();
            workQues.add(workQue);
        }

        // 反序列化科技等级信息
        Map<Integer, TechInfo> techLevelInfoMap = tech.getTechInfoMap();
        List<DataPb.TechData> techLevels = serBuilding.getTechDataList();
        techLevelInfoMap.clear();
        for (DataPb.TechData techInfoPb : techLevels) {
            TechInfo techInfo = new TechInfo();
            techInfo.unwrapData(techInfoPb);
            techLevelInfoMap.put(techInfoPb.getTechType(), techInfo);
        }

        // 反序列化科技研发进度
        LinkedList<TechQue> techQues = tech.getTechQues();
        List<DataPb.TechQueData> techProcesses = serBuilding.getTechQueDataList();
        techQues.clear();
        for (DataPb.TechQueData techProcessInfo : techProcesses) {
            TechQue elem = new TechQue();
            elem.unwrapData(techProcessInfo);
            techQues.add(elem);
        }

        // 读取生产队列到内存
        Map<Integer, WsWorkQue> wsWorkQueMap = workShop.getWorkQues();
        wsWorkQueMap.clear();
        for (DataPb.WsWorkQueData wsWorkQueData : serBuilding.getWsWorkQueDataList()) {
            if (wsWorkQueData == null) {
                continue;
            }

            WsWorkQue wsWorkQue = new WsWorkQue();
            wsWorkQue.readData(wsWorkQueData);
            wsWorkQueMap.put(wsWorkQue.getKeyId(), wsWorkQue);
        }

        // 读预设队列到内存
        Map<Integer, WsWaitQue> wsWaitQueMap = workShop.getWaitQues();
        wsWaitQueMap.clear();
        for (DataPb.WsWaitQueData wsWaitQueData : serBuilding.getWsWaitQueDataList()) {
            if (wsWaitQueData == null) {
                continue;
            }
            WsWaitQue wsWaitQue = new WsWaitQue();
            wsWaitQue.readData(wsWaitQueData);
            wsWaitQueMap.put(wsWaitQue.getIndex(), wsWaitQue);
        }

        // 装备建造队列
        LinkedList<WorkQue> equipWorkQues = buildings.getEquipWorkQue();
        for (CommonPb.WorkQue workQuePb : serBuilding.getEquipWorkQueList()) {
            if (workQuePb == null) {
                continue;
            }
            WorkQue workQue = new WorkQue();
            workQue.unwrapPb(workQuePb);
            equipWorkQues.add(workQue);
        }

        // 参谋部
        Staff staff = buildings.getStaff();
        if (staff != null && serBuilding.hasStaff()) {
            staff.unwrapPb(serBuilding.getStaff());
        }

        Market market = buildings.getMarket();
        if (market != null && serBuilding.hasMarket()) {
            market.unwrapPb(serBuilding.getMarket());
        }

        if (serBuilding.getRecoverDataList() != null && serBuilding.getRecoverDataList().size() > 0) {
            buildings.getRecoverBuilds().addAll(serBuilding.getRecoverDataList());
        }
    }

    // ser resource
    public byte[] serResourceData() {
        SerResource.Builder builder = SerResource.newBuilder();
        builder.setResource(resource.wrapPb());
        return builder.build().toByteArray();
    }

    // dser resource
    public void dserResourceData(SerResource serResource) {
        if (serResource.hasResource()) {
            CommonPb.Resource resourcePb = serResource.getResource();
            resource.unwrapPb(resourcePb);
        }
    }

    // ser employee
    public byte[] serEmployeeData() {
        SerEmployee.Builder ser = SerEmployee.newBuilder();
        Map<Integer, Employee> employeeMap = getEmployInfo().getEmployeeMap();
        for (Map.Entry<Integer, Employee> elem : employeeMap.entrySet()) {
            if (elem == null || elem.getValue() == null) {
                continue;
            }
            ser.addEmployee(elem.getValue().serEmployee());
        }

        ser.setOfficerId(employInfo.getOfficerId());
        ser.setBlackSmithId(employInfo.getBlackSmithId());
        ser.setResearcherId(employInfo.getResearcherId());

        return ser.build().toByteArray();
    }

    // dser employee
    public void dserEmployData(SerEmployee serEmployee) {
        List<CommonPb.EmployeeInfo> employeeList = serEmployee.getEmployeeList();
        for (CommonPb.EmployeeInfo elem : employeeList) {
            getEmployInfo().addEmplyee(elem.getEmployeeId(), elem);
        }

        employInfo.setOfficerId(serEmployee.getOfficerId());
        employInfo.setBlackSmithId(serEmployee.getBlackSmithId());
        employInfo.setResearcherId(serEmployee.getResearcherId());
    }

    public byte[] serAdvanceHero() {
        SerAdvanceHero.Builder builder = SerAdvanceHero.newBuilder();
        for (Map.Entry<Integer, Hero> heroEntry : heros.entrySet()) {
            if (heroEntry == null) {
                continue;
            }
            Hero hero = heroEntry.getValue();
            if (hero == null) {
                continue;
            }
            HeroAdvance.Builder heroAdvance = HeroAdvance.newBuilder();
            heroAdvance.setHeroId(hero.getHeroId());
            heroAdvance.setAdvanceTime(hero.getAdvanceTime());
            builder.addHeroAdvance(heroAdvance);
        }

        return builder.build().toByteArray();
    }

    public byte[] serTaskData() {
        SerTask.Builder builder = SerTask.newBuilder();
        for (Map.Entry<Integer, Task> elem : taskMap.entrySet()) {
            if (elem == null) {
                continue;
            }

            Task task = elem.getValue();
            if (task == null) {
                continue;
            }
            builder.addTask(task.wrapPb());
        }

        if (!finishedTask.isEmpty()) {
            builder.addAllFinishedId(finishedTask);
        }

        return builder.build().toByteArray();
    }

    public void dserTaskData(SerTask builder) {
        taskMap.clear();
        List<DataPb.TaskData> taskDataList = builder.getTaskList();
        for (DataPb.TaskData taskData : taskDataList) {
            Task task = new Task();
            task.unwrapPb(taskData);
            if (task.isProcessing()) {
                taskMap.put(task.getTaskId(), task);
            }
        }

        finishedTask.clear();
        for (Integer taskId : builder.getFinishedIdList()) {
            finishedTask.add(taskId);
        }

    }

    public void dserAdvanceHero(SerAdvanceHero builder) {
        for (HeroAdvance heroAdvance : builder.getHeroAdvanceList()) {
            int heroId = heroAdvance.getHeroId();
            Hero hero = heros.get(heroId);
            if (hero == null) {
                continue;
            }
            hero.setAdvanceTime(heroAdvance.getAdvanceTime());
        }
    }

    public int getAutoWallTimes() {
        return lord.getAutoWallTimes();
    }

    public void setAutoWallTimes(int autoWallTimes) {
        lord.setAutoBuildTimes(autoWallTimes);
    }

    public int getMaxMonsterLv() {
        return lord.getMaxMonsterLv();
    }

    public void setMaxMonsterLv(int level) {
        if (getMaxMonsterLv() < level) {
            lord.setMaxMonsterLv(level);
        }
    }

    public boolean isInMarch(Hero hero) {
        for (March march : marchList) {
            if (march.hasHero(hero.getHeroId())) {
                return true;
            }
        }

        return false;
    }

    public boolean isEmbattle(int heroId) {
        for (Integer elem : embattleList) {
            if (elem == heroId) {
                return true;
            }
        }
        return false;
    }

    public boolean isInMass(int heroId) {
        return massHeroes.contains(heroId);
    }

    public void incrementKillMonsterNum() {
        lord.setKillMonsterNum(lord.getKillMonsterNum() + 1);
    }

    public int getKillMonsterNum() {
        return lord.getKillMonsterNum();
    }

    public int getWorldKillMonsterStatus() {
        return lord.getWorldKillMonsterStatus();
    }

    public void setWorldKillMonsterStatus(int status) {
        lord.setWorldKillMonsterStatus(status);
    }

    public Soldier getSoldier(int soldierIndex) {
        return soldiers.get(soldierIndex);
    }

    // 获得玩家等级
    public int getLevel() {
        return lord.getLevel();
    }

    // 获得玩家经验
    public int getExp() {
        return lord.getExp();
    }

    // 玩家金币
    public int getGold() {
        if (lord == null) {
            return 0;
        }

        return lord.getGold();
    }

    // 请用:playerManager.subGold
    public boolean subGold(int sub) {
        if (sub <= 0) {
            return false;
        }

        if (lord.getGold() < sub) {
            return false;
        }

        lord.setGold(lord.getGold() - sub);
        lord.setGoldCost(lord.getGoldCost() + sub);

        return true;
    }

    // 请用:playerManager.addGold
    public boolean addGold(int add) {
        if (add <= 0) {
            return false;
        }
        lord.setGold(lord.getGold() + add);
        lord.setGoldGive(lord.getGoldGive() + add);

        return true;
    }

    // 请用:playerManager.addGold
    public boolean addSystemGold(int add) {
        if (add <= 0) {
            return false;
        }
        lord.addSystemGold(add);
        return true;
    }

    public boolean addRechargeGold(int add) {
        if (add <= 0) {
            return false;
        }
        lord.addRechargeGold(add);
        return true;
    }

    public Map<Integer, Long> getAllRes() {
        return resource.getResource();
    }

    public int getCommandLv() {
        return buildings.getCommandLv();
    }

    public Command getCommand() {
        return buildings.getCommand();
    }

    public long getIron() {
        return resource.getIron();
    }

    public long getCopper() {
        return resource.getCopper();
    }

    public long getStone() {
        return resource.getStone();
    }

    public long getResource(int resourceType) {
        return resource.getResource(resourceType);
    }

    public int getOfficerId() {
        return employInfo.getOfficerId();
    }

    public long getOfficerTime() {
        return employInfo.getOfficerTime();
    }

    public CommonPb.Resource.Builder wrapResourcePb() {
        return resource.wrapPb();
    }

    public int getFreeTeam() {
        // 检查免费队列
        int busyTeam1 = 0;
        int busyTeam2 = 0;
        ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
        long now = System.currentTimeMillis();
        for (BuildQue buildQue : buildQues) {
            if (buildQue.getBuildQueType() == 1) {
                busyTeam1 = 1;
            } else if (buildQue.getBuildQueType() == 2) {
                busyTeam2 = 1;
            }
        }

        int freeTeam1 = 1 - busyTeam1;
        int freeTeam2 = 0;
        if (lord.getBuildTeamTime() > now) {
            freeTeam2 = 1 - busyTeam2;
        } else {
            freeTeam2 = 0;
        }

        int freeTeam = freeTeam1 + freeTeam2;

        freeTeam = Math.max(0, freeTeam);
        freeTeam = Math.min(2, freeTeam);
        return freeTeam;
    }

    public GameError hasBuildTeam(List<Long> param) {
        if (param.size() != 1) {
            return GameError.BUILD_TEAM_PARAM_ERROR;
        }
        int needTeam = param.get(0).intValue();
        if (getFreeTeam() < needTeam) {
            return GameError.NO_MORE_BUILDING_TEAM;
        }

        return GameError.OK;
    }

    public GameError reachCommandLevel(List<Long> param) {
        if (param.size() != 1) {
            return GameError.COMMAND_LEVEL_PARAM_ERROR;
        }

        int needLevel = param.get(0).intValue();
        if (getCommandLv() < needLevel) {
            return GameError.COMMAND_LV_NOT_ENOUGH;
        }
        return GameError.OK;
    }

    public GameError reachLordLevel(List<Long> param) {
        if (param.size() != 1) {
            return GameError.LORD_LEVEL_PARAM_ERROR;
        }
        int needLevel = param.get(0).intValue();
        if (lord.getLevel() < needLevel) {
            return GameError.LORD_LV_NOT_ENOUGH;
        }
        return GameError.OK;
    }

    public GameError hasResource(List<Long> param) {
        if (param.size() != 3) {
            return GameError.RESOURCE_PARAM_ERROR;
        }
        int awardType = param.get(0).intValue();
        if (awardType != AwardType.RESOURCE) {
            return GameError.AWARD_RESOURCE_TYPE_ERROR;
        }

        int resType = param.get(1).intValue();
        if (resType < ResourceType.IRON || resType > ResourceType.STONE) {
            return GameError.RESOURCE_TYPE_ERROR;
        }
        Long needResource = param.get(2);
        long owned = getResource(resType);
        if (owned < needResource) {
            return GameError.RESOURCE_NOT_ENOUGH;
        }

        return GameError.OK;

    }

    // 达到科技院等级
    public GameError reachTechLevel(List<Long> param) {
        if (param.size() != 1) {
            return GameError.TECH_LEVEL_PARAM_ERROR;
        }

        int level = param.get(0).intValue();
        int techLevel = getTechLv();
        if (techLevel < level) {
            return GameError.NOT_ENOUGH_TECH_LEVEL;
        }

        return GameError.OK;
    }

    // 科技达到多少级
    public GameError reachTechResearchLv(List<Long> param) {
        if (param.size() != 2) {
            return GameError.TECH_RESEARCH_PARAM_ERROR;
        }
        int techType = param.get(0).intValue();
        int techLevel = param.get(1).intValue();
        Tech tech = getTech();
        TechInfo info = tech.getTechInfo(techType);
        if (info.getLevel() < techLevel) {
            return GameError.TECH_RESEARCH_NOT_ENOUGH;
        }

        return GameError.OK;
    }

    public ConcurrentLinkedDeque<BuildQue> getBuildQues() {
        return buildings.getBuildQues();
    }

    public Hero getHero(int heroId) {
        if (heros.containsKey(heroId)) {
            return heros.get(heroId);
        }
        return null;
    }

    public long getHonor() {
        return lord.getHonor();
    }

    public int getEnergy() {
        return lord.getEnergy();
    }

    public int getBuyEnergy() {
        return lord.getBuyEnergy();
    }

    public BuildQue getBuildQue(int buildingId) {
        return buildings.getBuildQue(buildingId);
    }

    public void removeBuildQue(int buildingId) {
        buildings.removeBuildQue(buildingId);
    }

    public Item getItem(int itemId) {
        if (itemId == 0) {
            return null;
        }
        if (itemMap.containsKey(itemId)) {
            return itemMap.get(itemId);
        }
        return null;
    }

    public int getItemNum(int itemId) {
        Item item = getItem(itemId);
        if (item == null) {
            return 0;
        }

        return item.getItemNum();
    }

    public long getOil() {
        return resource.getOil();
    }

    public int getVip() {
        return lord.getVip();
    }

    public int getVipExp() {
        return lord.getVipExp();
    }

    // 开启建筑
    public BuildingBase openResBuilding(int buildingId) {
        return buildings.openResBuilding(buildingId);
    }

    public BuildingBase getResBuilding(int buildingId) {
        ResBuildings resBuildings = buildings.getResBuildings();
        return resBuildings.getBuilding(buildingId);
    }

    public Tech getTech() {
        return buildings.getTech();
    }

    public int getTechLv() {
        Tech tech = buildings.getTech();
        if (tech != null) {
            return tech.getLv();
        }
        return Integer.MAX_VALUE;
    }

    public Task getTask(int taskId) {
        return taskMap.get(taskId);
    }

    public int getSoldierLines() {
        return lord.getSoliderLines();
    }

    public int getBuildingLv(int buildingId) {
        return buildings.getBuildingLv(buildingId);
    }

    public int getPosX() {
        return lord.getPosX();
    }

    public Pos getPos() {
        return new Pos(lord.getPosX(), lord.getPosY());
    }

    private void setPosX(int posX) {
        lord.setPosX(posX);
    }

    public int getPosY() {
        return lord.getPosY();
    }

    private void setPosY(int posY) {
        lord.setPosY(posY);
    }

    public String getNick() {
        return lord.getNick();
    }

    public int getTitle() {
        return lord.getTitle();
    }

    public int getCountry() {
        return lord.getCountry();
    }

    public int getPeople() {
        return lord.getPeople();
    }

    public int getPortrait() {
        return lord.getPortrait();
    }

    public void setMiningList(List<Integer> miningList) {
        Iterator<Integer> iterator = this.miningList.iterator();
        if (this.miningList.size() < 1) {
            this.miningList.addAll(miningList);
            return;
        }
        int i = 0;
        while (iterator.hasNext()) {
            int heroId = iterator.next();
            if (heroId > 0) {
                i++;
                continue;
            }
            this.miningList.set(i, miningList.get(i));
            i++;
        }
    }

    public List<Integer> getMeetingArmy(int type) {
        if (type == CastleConsts.MINING) {
            return refreshMingHeros(getMiningList());
        } else {
            List<Integer> list = new ArrayList<>();
            List<WarDefenseHero> defenseHeroes = refreshDefenseArmyHeros(getDefenseArmyList());
            defenseHeroes.forEach(warDefenseHero -> list.add(warDefenseHero.getHeroId()));
            return list;
        }
    }

    private List<Integer> refreshMingHeros(List<Integer> heros) {
        Iterator<Integer> iterator = heros.iterator();
        while (iterator.hasNext()) {
            Integer hero = iterator.next();
            if (hero < 1) {
                continue;
            }
            if (this.heros.get(hero) == null) {
                iterator.remove();
            }
        }
        return heros;
    }

    private List<WarDefenseHero> refreshDefenseArmyHeros(List<WarDefenseHero> heros) {
        Iterator<WarDefenseHero> iterator = heros.iterator();
        while (iterator.hasNext()) {
            WarDefenseHero hero = iterator.next();
            if (hero.getHeroId() < 1) {
                continue;
            }
            if (this.heros.get(hero.getHeroId()) == null) {
                iterator.remove();
            }
        }
        return heros;
    }

    public void updateMeetingHero(int index, int heroId, int type) {
        if (type == CastleConsts.MINING) {
            getMiningList().set(index, heroId);
        } else {
            WarDefenseHero warDefenseHero = new WarDefenseHero();
            warDefenseHero.setHeroId(heroId);
            warDefenseHero.setLastRefreshTime(System.currentTimeMillis());
            Hero hero = heros.get(heroId);
            if (hero != null) {
                hero.setCurrentSoliderNum(0);
            }
            getDefenseArmyList().set(index, warDefenseHero);
        }

    }

    public byte[] serMarchData() {
        SerMarchData.Builder builder = SerMarchData.newBuilder();
//        for (March march : marchList) {
//            builder.addMarchData(march.writeMarch());
//        }

        return builder.build().toByteArray();
    }

    public byte[] serLostResData() {
        SerLostRes.Builder builder = SerLostRes.newBuilder();
        builder.setLostRes(getLostRes().writeData());
        return builder.build().toByteArray();
    }

    public void dserLostResData(SerLostRes data) {
        getLostRes().clear();
        getLostRes().readData(data.getLostRes());
    }

    public byte[] serActivityData() {
        SerActRecord.Builder builder = SerActRecord.newBuilder();
        Iterator<ActRecord> it = activitys.values().iterator();
        while (it.hasNext()) {
            ActRecord actRecord = it.next();
            DataPb.ActRecord.Builder actPb = DataPb.ActRecord.newBuilder();
            actPb.setActivityId(actRecord.getActivityId());
            actPb.setAwardId(actRecord.getAwardId());
            actPb.setBeginTime(actRecord.getBeginTime());
            actPb.setCleanTime(actRecord.getCleanTime());
            actPb.setClose(actRecord.isClose());
            actPb.setIsnew(actRecord.isNew());
            actPb.setIsShow(actRecord.isShow());
            actPb.setCount(actRecord.getCount());
            actPb.setBeforeReceiveDay(actRecord.getBeforeReceiveDay());
            Iterator<Entry<Long, Long>> st = actRecord.getStatus().entrySet().iterator();
            while (st.hasNext()) {
                Entry<Long, Long> entry = st.next();
                DataPb.Status.Builder statusPb = DataPb.Status.newBuilder();
                statusPb.setK(entry.getKey());
                statusPb.setV(entry.getValue());
                actPb.addStatus(statusPb.build());
            }

            Iterator<Entry<Integer, Integer>> rt = actRecord.getReceived().entrySet().iterator();
            while (rt.hasNext()) {
                Entry<Integer, Integer> entry = rt.next();
                DataPb.TowInt.Builder rPb = DataPb.TowInt.newBuilder();
                rPb.setK(entry.getKey());
                rPb.setV(entry.getValue());
                actPb.addReceived(rPb.build());
            }

            Iterator<ActShopProp> spt = actRecord.getShops().values().iterator();
            while (spt.hasNext()) {
                ActShopProp next = spt.next();
                DataPb.ActShopProp.Builder spPb = DataPb.ActShopProp.newBuilder();
                spPb.setGrid(next.getGrid());
                spPb.setPropId(next.getPropId());
                spPb.setPropNum(next.getPropNum());
                spPb.setPrice(next.getPrice());
                spPb.setIsBuy(next.getIsBuy());
                actPb.addActShopProp(spPb.build());
            }

            Iterator<Entry<Integer, Integer>> ret = actRecord.getRecord().entrySet().iterator();
            while (ret.hasNext()) {
                Entry<Integer, Integer> entry = ret.next();
                DataPb.TowInt.Builder rPb = DataPb.TowInt.newBuilder();
                rPb.setK(entry.getKey());
                rPb.setV(entry.getValue());
                actPb.addRecord(rPb.build());
            }

            Iterator<Entry<Integer, ActPassPortTask>> actPassPortTask = actRecord.getTasks().entrySet().iterator();
            while (actPassPortTask.hasNext()) {
                Entry<Integer, ActPassPortTask> entry = actPassPortTask.next();
                DataPb.ActPassPortTaskData.Builder rPb = DataPb.ActPassPortTaskData.newBuilder();
                rPb.setId(entry.getValue().getId());
                rPb.setType(entry.getValue().getType());
                rPb.setTaskType(entry.getValue().getTaskType());
                rPb.setProcess(entry.getValue().getProcess());
                rPb.setIsAward(entry.getValue().getIsAward());
                actPb.addActPassPortTaskData(rPb.build());
            }

            Iterator<Entry<Integer, Integer>> dailGuarantee = actRecord.getDailGuarantee().entrySet().iterator();
            while (dailGuarantee.hasNext()) {
                Entry<Integer, Integer> entry = dailGuarantee.next();
                DataPb.TowInt.Builder rPb = DataPb.TowInt.newBuilder();
                rPb.setK(entry.getKey());
                rPb.setV(entry.getValue());
                actPb.addDailGuarantee(rPb.build());
            }

            actRecord.getActivityRecords().forEach(e -> {
                actPb.addPairIntLong(CommonPb.PairIntLong.newBuilder().setV1(e.getKey()).setV2(e.getBuyCount()).setV3(e.getExpireTime()).build());
            });

            builder.addActRecord(actPb.build());
        }
        return builder.build().toByteArray();
    }

    public void dserActivityData(SerActRecord data) {
        activitys.clear();
        if (data.getActRecordCount() <= 0) {
            return;
        }
        for (DataPb.ActRecord actRecordPb : data.getActRecordList()) {
            ActRecord actRecord = new ActRecord(actRecordPb);
            activitys.put(actRecord.getActivityId(), actRecord);
        }
    }

    public void dserMarchData(SerMarchData data) {
        marchList.clear();
        for (DataPb.MarchData marchData : data.getMarchDataList()) {
            March march = new March();
            march.readMarch(marchData);
//            LogHelper.GAME_LOGGER.info("【玩家.行军.初始化】开始序列化！playerId:{} marchId:{} marchType:{} state:{} endTime:{}", getRoleId(), march.getKeyId(), march.getMarchType(), march.getState(), DateHelper.getDate(march.getEndTime()));
            marchList.add(march);
        }
    }

    public void addLostRes(LostRes rhs) {
        getLostRes().add(rhs);
    }

    public ConcurrentLinkedDeque<March> getMarchList() {
        return marchList;
    }

    public void addMarch(March march) {
//        LogHelper.GAME_LOGGER.info("【玩家.行军.添加】 playerId:{} marchId:{} marchType:{} state:{} endTime:{}", getRoleId(), march.getKeyId(), march.getMarchType(), march.getState(), DateHelper.getDate(march.getEndTime()));
        marchList.add(march);
    }

    public void clearLostRes() {
        lostRes.clear();
    }

    public void setMarchList(ConcurrentLinkedDeque<March> marchList) {
        this.marchList = marchList;
    }

    public Wall getWall() {
        return buildings.getWall();
    }

    public int getWallLv() {
        Wall wall = getWall();
        if (wall == null) {
            return 0;
        }
        return wall.getLv();
    }

    public Ware getWare() {
        return buildings.getWare();
    }

    public Nation getNation() {
        return nation;
    }

    public String getPosStr() {
        return String.valueOf(lord.getPosX()) + "," + String.valueOf(lord.getPosY());
    }

    public String getOldPosStr() {
        return String.valueOf(oldPos.getX()) + "," + String.valueOf(oldPos.getY());
    }

    public ShopInfo getShopInfo() {
        return shopInfo;
    }

    public void setShopInfo(ShopInfo shopInfo) {
        this.shopInfo = shopInfo;
    }

    public ConcurrentLinkedDeque<Mail> getMails() {
        return mails;
    }

    public Queue<Integer> getPmails() {
        return pmails;
    }

    public Queue<Integer> getRepots() {
        return repots;
    }

    public Queue<Integer> getSysmail() {
        return sysmail;
    }

    public MapStatus findMapStatus(int mapId) {
        return mapStatusMap.get(mapId);
    }

    public Map<Integer, WorldTargetAward> getWorldTargetAwardMap() {
        return worldTargetAwardMap;
    }

    public void setWorldTargetAwardMap(Map<Integer, WorldTargetAward> worldTargetAwardMap) {
        this.worldTargetAwardMap = worldTargetAwardMap;
    }

    public March getMarch(int keyId) {
        for (March march : marchList) {
            if (keyId == march.getKeyId()) {
                return march;
            }
        }
        return null;
    }

    public List<March> getMarch(Pos pos) {
        return marchList.stream().filter(x -> x.getEndPos().equals(pos)).collect(Collectors.toList());
    }

    public HashMap<Integer, March> getMarchList(Pos pos) {
        HashMap<Integer, March> map = new HashMap<>();
        for (March march : marchList) {
            if (march == null) {
                continue;
            }
            Pos endPos = march.getEndPos();
            if (endPos == null) {
                continue;
            }
            if (pos.getX() == endPos.getX() && pos.getY() == endPos.getY()) {
                map.computeIfAbsent(march.getKeyId(), value -> march);
            }
        }
        return map;
    }

    public LostRes getLostRes() {
        return lostRes;
    }

    public void setLostRes(LostRes lostRes) {
        this.lostRes = lostRes;
    }

    public BuildingBase getBuilding(int buildingId) {
        return buildings.getBuilding(buildingId);
    }

    public TreeSet<Integer> getBuildingIds() {
        return buildings.getBuildingIds();
    }

    public void setAutoBuildTimes(int buildingTimes) {
        lord.setAutoBuildTimes(buildingTimes);
    }

    public int getAutoBuildTimes() {
        return lord.getAutoBuildTimes();
    }

    public boolean isOpenBuild() {
        if (lord == null) {
            return false;
        }
        return lord.getOnBuild() == 1;
    }

    public void setOnBuild(int state) {
        lord.setOnBuild(state);
    }

    public int getOnBuild() {
        return lord.getOnBuild();
    }

    public void setCityId(int cityId) {
        lord.setCityId(cityId);
    }

    public int getCityId() {
        return lord.getCityId();
    }

    public long getProectedTime() {
        return lord.getProtectedTime();
    }

    public void setProtectedTime(long time) {
        lord.setProtectedTime(time);
    }

    public Map<Integer, Effect> getEffects() {
        return effects;
    }

    public void setNewStateDone(HashSet<Integer> newStateDone) {
        this.newStateDone = newStateDone;
    }

    public HashSet<Integer> getNewStateDone() {
        return newStateDone;
    }

    public void addNewState(int newState) {
        newStateDone.add(newState);
    }

    public boolean isNewStateDone(int newState) {
        return newStateDone.contains(newState);
    }

    public void removeTask(Task task) {
        if (!task.isFinished()) {
            return;
        }
        taskMap.remove(task.getTaskId());
    }

    public Map<Integer, LevelAward> getLevelAwardsMap() {
        return levelAwardsMap;
    }

    public void setLevelAwardsMap(Map<Integer, LevelAward> levelAwardsMap) {
        this.levelAwardsMap = levelAwardsMap;
    }

    public void setMinCountry(int country) {
        if (lord.getMinCountry() == -1) {
            return;
        }
        lord.setMinCountry(country);
    }

    public int getMinCountry() {
        return lord.getMinCountry();
    }

    public int getHeroState(int heroId) {
        for (March march : marchList) {
            if (march.hasHero(heroId)) {
                return march.getState();
            }
        }
        return 0;
    }

    public March getHeroMarch(int heroId) {
        for (March march : marchList) {
            if (march.hasHero(heroId)) {
                return march;
            }
        }
        return null;
    }

    public int getWallAuto() {
        return lord.getOnWall();
    }

    public int getSoldierAuto() {
        return lord.getSoldierAuto();
    }

    public void setSoldierAuto(int soldierAuto) {
        lord.setSoldierAuto(soldierAuto);
    }

    public void updateMapStatuses(List<MapStatus> mapStatuses) {
        for (MapStatus mapStatus : mapStatuses) {
            MapStatus status = mapStatusMap.get(mapStatus.getMapId());
            if (status != null) {
                status.setStatus(mapStatus.getStatus());
            }
        }
    }

    public Map<Integer, MapStatus> getMapStatusMap() {
        return mapStatusMap;
    }

    public void setMapStatusMap(Map<Integer, MapStatus> mapStatusMap) {
        this.mapStatusMap = mapStatusMap;
    }

    public List<Long> getBlackList() {
        return blackList;
    }

    public void setBlackList(List<Long> blackList) {
        this.blackList = blackList;
    }

    public boolean isMapCanMove(int mapId) {
        MapStatus mapStatus = mapStatusMap.get(mapId);
        if (mapStatus == null) {
            return false;
        }
        // 如果要飞的区域为母巢，但是玩家的等级不足75 不让飞
        int num = SpringUtil.getBean(StaticLimitMgr.class).getNum(292);
        if (mapId == 20 && lord.getLevel() < num) {
            return false;
        }
        return mapStatus.getStatus() >= 2;
    }

    public int isMapCanMoves(int mapId) {
        if (mapId == MapId.FIRE_MAP) {
            return 3;
        }
        MapStatus mapStatus = mapStatusMap.get(mapId);
        if (mapStatus == null) {
            return 1;
        }
        // 如果要飞的区域为母巢，但是玩家的等级不足75 不让飞
        int num = SpringUtil.getBean(StaticLimitMgr.class).getNum(292);
        if (mapId == 20 && lord.getLevel() < num) {
            return 2;
        }
        return mapStatus.getStatus() >= 2 ? 3 : 1;
    }

    public void setBattleScore(int battleScore) {
        lord.setBattleScore(battleScore);
    }

    public void setBuildingScore(int battleScore) {
        lord.setBuildingScore(battleScore);
    }

    public boolean isSoldierTraining(int soldierType) {
        Soldier soldier = soldiers.get(soldierType);
        if (soldier == null) {
            return false;
        }

        return soldier.isTraining();
    }

    public SimpleData getSimpleData() {
        return simpleData;
    }

    public void setSimpleData(SimpleData simpleData) {
        this.simpleData = simpleData;
    }

    public byte[] serSimpleData() {
        return simpleData.serSimpleData();
    }

    public void dserSimpleData(SerSimpleData data) {
        simpleData.dserSimpleData(data);
    }

    public Pos initNewPos(Pos newPos) {
        oldPos.init(lord.getPosX(), lord.getPosY());
        setPosX(newPos.getX());
        setPosY(newPos.getY());
        return oldPos;
    }

    public byte[] serNationData() {
        return nation.serNationData();
    }

    public void dserNationData(SerNation serNation) {
        nation.dserNationData(serNation);
    }

    public int getRankValue(int type) {
        return nation.getRankValue(type);
    }

    public int getBattleScore() {
        return lord.getAllScore();
    }

    public int getBuildingScore() {
        return lord.getBuildingScore();
    }

    public int getHeroScore() {
        return lord.getBattleScore();
    }

    public boolean hasMarch() {
        return !marchList.isEmpty();
    }

    public boolean isHeroInMarch(int heroId) {
        for (March march : marchList) {
            for (Integer id : march.getHeroIds()) {
                if (heroId == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public HashSet<Integer> getMassHeroes() {
        return massHeroes;
    }

    public void clearMassHeroes() {
        massHeroes.clear();
    }

    public void clearPvpHeroes() {
        pvpHeroMap.clear();
    }

    public void setMassHeroes(HashSet<Integer> massHeroes) {
        this.massHeroes = massHeroes;
    }

    public HashMap<Integer, PvpHero> getPvpHeroMap() {
        return pvpHeroMap;
    }

    public void setPvpHeroMap(HashMap<Integer, PvpHero> pvpHeroMap) {
        this.pvpHeroMap = pvpHeroMap;
    }

    public PvpHero createPvpHero(int heroId) {
        PvpHero pvpHero = new PvpHero();
        pvpHero.setHeroId(heroId);
        pvpHero.setLordId(roleId);
        pvpHero.setRebornTimes(0);
        pvpHero.setMutilKill(0);
        pvpHero.setCountry(getCountry());
        pvpHero.setDefenceTimes(0);
        pvpHero.setAttackTimes(0);

        return pvpHero;
    }

    public boolean hasPvpHero(int heroId) {
        PvpHero hero = pvpHeroMap.get(heroId);
        return hero != null;
    }

    public PvpHero getPvpHero(int heroId) {
        return pvpHeroMap.get(heroId);
    }

    // you must check first
    public PvpHero addPvpHero(int heroId) {
        PvpHero pvpHero = createPvpHero(heroId);
        pvpHeroMap.put(heroId, pvpHero);

        return pvpHero;
    }

    public void removePvpHero(PvpHero pvpHero) {
        pvpHeroMap.remove(pvpHero.getHeroId());
    }

    public void removeMassHero(int heroId) {
        massHeroes.remove(heroId);
    }

    public void setCountry(int country) {
        lord.setCountry(country);
    }

    public int getTotalKillNum() {
        return simpleData.getTotalKillNum();
    }

    public void setTotalKillNum(int mutilKillNum) {
        simpleData.setTotalKillNum(mutilKillNum);
    }

    public PvpHero getMutilKillHero() {
        if (pvpHeroMap.isEmpty()) {
            return null;
        }
        if (pvpHeroMap.values().size() <= 1) {
            List<PvpHero> list = new ArrayList<>(pvpHeroMap.values());
            return list.get(0);
        }
        List<PvpHero> list = pvpHeroMap.values().stream().sorted(Comparator.comparingInt(PvpHero::getMutilKill).reversed()).collect(Collectors.toList());
        return list.get(0);
//
//        int kill = 0;
//        int heroId = 0;
//        for (PvpHero pvpHero : pvpHeroMap.values()) {
//            if (kill <= pvpHero.getMutilKill()) {
//                heroId = pvpHero.getHeroId();
//                kill = pvpHero.getMutilKill();
//            }
//        }
//
//        if (heroId != 0) {
//            return pvpHeroMap.get(heroId);
//        }
//
//        return null;
    }

    public void addPvpScore(int score) {
        if (score < 0) {
            LogHelper.CONFIG_LOGGER.info("addPvpScore score is 0");
            return;
        }

        simpleData.addPvpScore(score);
    }

    public int getPvpScore() {
        return simpleData.getPvpScore();
    }

    public void clearPvpBattle() {
        setTotalKillNum(0);
        massHeroes.clear();
        pvpHeroMap.clear();
    }

    public List<CommonPb.Buff> wrapBuffs() {
        HashMap<Integer, Buff> buffs = simpleData.getBuffMap();
        List<CommonPb.Buff> buffPb = new ArrayList<CommonPb.Buff>();
        long now = System.currentTimeMillis();
        for (Buff buff : buffs.values()) {
            if (buff.getEndTime() <= now) {
                continue;
            }
            buffPb.add(buff.wrapPb());
        }
        return buffPb;
    }

    public List<CommonPb.Effect> wrapEffects() {
        long currentTimeMillis = System.currentTimeMillis();
        List<CommonPb.Effect> effectsPb = new ArrayList<>();
        for (Effect effect : effects.values()) {
            if (effect.getBeginTime() <= currentTimeMillis && currentTimeMillis < effect.getEndTime()) {
                CommonPb.Effect.Builder builder = CommonPb.Effect.newBuilder();
                builder.setEffectId(effect.getEffectId());
                builder.setEffect(effect.getEffect());
                builder.setEndTime(effect.getEndTime());
                builder.setBeginTime(effect.getBeginTime());
                effectsPb.add(builder.build());
            }
        }
        return effectsPb;
    }

    public boolean isOk() {
        return isLogin && channelId != -1;
    }

    public void setCreateState(int createState) {
        lord.setCreateState(createState);
    }

    public void removeMail(int keyId) {
        Iterator<Mail> it = mails.iterator();
        while (it.hasNext()) {
            Mail mail = it.next();
            if (mail == null) {
                continue;
            }

            if (mail.getKeyId() == keyId) {
                it.remove();
            }
        }
    }

    public Mail recallMail(int keyId) {
        Iterator<Mail> it = mails.iterator();
        Mail mail = null;
        while (it.hasNext()) {
            mail = it.next();
            if (mail == null) {
                continue;
            }

            if (mail.getMailKey() == keyId) {
                it.remove();
            }
            break;
        }
        return mail;
    }

    public void addMail(Mail mail) {
        mails.offerFirst(mail);
    }

    public Mail getMail(int keyId) {
        for (Mail mail : mails) {
            if (mail == null) {
                continue;
            }

            if (mail.getKeyId() == keyId) {
                return mail;
            }
        }

        return null;
    }

    public boolean hasIcon(int iconId) {
        return simpleData.hasIcon(iconId);
    }

    public List<CommonPb.LordIcons> wrapIcons() {
        return simpleData.wrapIcons();
    }

    public void updateWashHeroNum(int heroId, int quality) {
        simpleData.updateWashHeroNum(heroId, quality);
    }

    public void updateMakeEquipNum(int quality, int equipType) {
        simpleData.updateMakeEquipNum(quality, equipType);
    }

    public void updateWashEquipNum(int keyId, int quality) {
        simpleData.updateWashEquipNum(keyId, quality);
    }

    public int getMaxScore() {
        return simpleData.getMaxScore();
    }

    public void setMaxScore(int maxScore) {
        if (maxScore != simpleData.getMaxScore()) {
            SpringUtil.getBean(EventManager.class).record_userInfo(this, EventName.add_capacity);
        }
        simpleData.setMaxScore(maxScore);
    }

    public int getWashEquipMax(int quality) {
        return simpleData.getWashEquipMax(quality);
    }

    public int getWashHeroMax(int quality) {
        return simpleData.getWashHeroMax(quality);
    }

    public int getEquipMake(int quality, int equipType) {
        return simpleData.getEquipMake(quality, equipType);
    }

    public ArmyEnum getArmyEnumByHeroId(int heroId) {
        if (embattleList.contains(heroId)) {
            return ArmyEnum.ARMY_ONE;
        }
        if (miningList.contains(heroId)) {
            return ArmyEnum.ARMY_TWO;
        }
        if (defenseArmyList.stream().anyMatch(e -> e.getHeroId() == heroId)) {
            return ArmyEnum.ARMY_THREE;
        }
        return ArmyEnum.ARMY_ONE;
    }

    public boolean isOnline() {
        return isLogin && channelId != -1;
    }

    public Map<Integer, WorldPersonalGoal> getPersonalGoals() {
        return personalGoals;
    }

    public List<Integer> getSweepHeroList() {
        return sweepHeroList;
    }

    ///////////////////// 美女系统数据/////////////////////
    public byte[] serBeautyData() {
        SerNewBeautyRecord.Builder builder = SerNewBeautyRecord.newBuilder();
        Iterator<BeautyData> it = beautys.values().iterator();
        while (it.hasNext()) {
            BeautyData beautyData = it.next();
            DataPb.BeautyData.Builder beauPb = DataPb.BeautyData.newBuilder();
            beauPb.setKeyId(beautyData.getKeyId());
            beauPb.setIntimacyValue(beautyData.getIntimacyValue());
            beauPb.setStar(beautyData.getStar());
            beauPb.setKillId(beautyData.getKillId());
            beauPb.setSeekingTimes(beautyData.getSeekingTimes());
            beauPb.setFreeSeekingEndTime(beautyData.getFreeSeekingEndTime());
            beauPb.setIsUnlock(beautyData.getIsUnlock());
            beauPb.setClickCount(beautyData.getClickCount());

            builder.addBeautyData(beauPb.build());
        }
        return builder.build().toByteArray();
    }

    public void dserBeautyData(SerNewBeautyRecord data) {
        beautys.clear();
        for (DataPb.BeautyData beautyData : data.getBeautyDataList()) {
            BeautyData beauty = new BeautyData(beautyData);
            beautys.put(beauty.getKeyId(), beauty);
        }
    }

    public byte[] serTDData() {
        SerializePb.SerTDRecord.Builder builder = SerializePb.SerTDRecord.newBuilder();
        getTdMap().values().forEach(td -> {
            builder.addRecords(DataPb.TDRecord.newBuilder().setLevelId(td.getLevelId()).setStar(td.getStar()).setState(td.getState()).addAllStarRewardStatus(td.getStarRewardStatus().values()));
        });
        return builder.build().toByteArray();
    }

    public byte[] serRecordData() {
        SerMiningInfo.Builder b = SerMiningInfo.newBuilder();
        for (Integer e : getRecordList()) {
            if (e != null) {
                b.addMiningHero(e);
            }
        }
        return b.build().toByteArray();
    }

    // 私人聊天数据
    public byte[] serPersonChatData() {
        SerializePb.SerPersonChatRoom.Builder builder = SerializePb.SerPersonChatRoom.newBuilder();
        Iterator<PersonChatRoom> iterator = personChatRoom.values().iterator();
        while (iterator.hasNext()) {
            PersonChatRoom next = iterator.next();
            if (null != next) {
                builder.addChatRoom(next.writeData());
            }
        }
        return builder.build().toByteArray();
    }

    public void dserTDData(SerializePb.SerTDRecord data) {
        tdMap.clear();
        for (DataPb.TDRecord record : data.getRecordsList()) {
            TD td = new TD(record);
            tdMap.put(td.getLevelId(), td);
        }
    }

    public void dserRecordData(SerializePb.SerMiningInfo data) {
        recordList.clear();
        recordList.addAll(data.getMiningHeroList());
    }

    public void dserPersonChatData(SerializePb.SerPersonChatRoom data) {
        personChatRoom.clear();
        for (DataPb.PersonChatRoomData personChatRoomData : data.getChatRoomList()) {
            PersonChatRoom chatRoom = new PersonChatRoom();
            chatRoom.readData(personChatRoomData);
            personChatRoom.put(chatRoom.getRoomId(), chatRoom);
        }
    }

    /**
     * 采集中的次数
     *
     * @return
     */
    public boolean getColectNum() {
//        int i = 0;
//        for (March march : marchList) {
//            if (march.getMarchType() == MarchType.CollectResource) {
//                ++i;
//            }
//        }
//        return i;
        return marchList.stream().filter(x -> x.getMarchType() == MarchType.CollectResource || x.getMarchType() == MarchType.SUPER_COLLECT).count() >= 4;

    }

    public void addKillRebel() {
        this.lord.setKillRebel(this.lord.getKillRebel() + 1);
    }

    public void addCallRebel() {
        this.lord.setRebelCall(this.lord.getRebelCall() + 1);
    }

    public void addAccackPlayerCity() {
        this.lord.setAttackPlayerNum(this.lord.getAttackPlayerNum() + 1);
    }

    public void addAccackCityNum() {
        this.lord.setAttackCityNum(this.lord.getAttackCityNum() + 1);
    }

    public String getPersonalSignature() {
        return personalSignature == null ? "" : personalSignature;
    }

    public Equip getEquipItem(int keyId) {
        return equips.get(keyId);
    }

    public List<CommonPb.Buff> wrapBroodWarBuff() {
//        Map<Integer, Buff> buffs = simpleData.getBroodWarBuff();
//        List<CommonPb.Buff> buffPb = new ArrayList<CommonPb.Buff>();
//        for (Buff buff : buffs.values()) {
//            buffPb.add(buff.wrapPb());
//        }
//        return buffPb;
        return null;
    }

    public CommonPb.Companion warCompanion(int status) {
        CommonPb.Companion.Builder builder = CommonPb.Companion.newBuilder();
        builder.setLordId(roleId).setHeadImg(lord.getPortrait()).setFight(getBattleScore()).setNick(lord.getNick()).setStatus(status).setTitle(lord.getTitle()).setLevel(lord.getLevel());
        return builder.build();
    }

    public List<Hero> getAllHeroList() {
        return new ArrayList<>(heros.values());
    }

    public int getTdMoney() {
        return getLord().getTdMoney();
    }

    public void addScore(long score) {
        this.score += score;
    }

    private void serTitleAward(SerData.Builder ser) {
        // CommonPb.TitleAwardInfo.Builder builder = CommonPb.TitleAwardInfo.newBuilder();
        // builder.setRecv(this.titleAward.getRecv());
        // Map<Integer, Integer> hisRecv = this.titleAward.getHisRecv();
        // hisRecv.entrySet().forEach(x->{
        // CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
        // builder1.setV1(x.getKey());
        // builder1.setV2(x.getValue());
        // builder.addHisRecv(builder1);
        // });
        ser.setTitleAward(this.titleAward.encode());
    }

    private void dserTitleAward(SerData ser) {
        CommonPb.TitleAwardInfo ctitleAward = ser.getTitleAward();
        TitleAward titleAward = getTitleAward();
        if (ctitleAward == null) {
            titleAward.setRecv(0);// 未领取
            int title = getLord().getTitle();
            StaticCountryMgr staticCountryMgr = SpringUtil.getBean(StaticCountryMgr.class);
            for (int i = 1; i <= title; i++) {
                StaticCountryTitle countryTitle = staticCountryMgr.getCountryTitle(i);
                titleAward.getHisRecv().put(countryTitle.getTitleId(), 1);
                if (countryTitle != null && countryTitle.getPromotionAward() != null) {
                    titleAward.getHisRecv().put(countryTitle.getTitleId(), 0);
                }
            }
        } else {
            titleAward.decode(ctitleAward);
        }
    }

    public void dserBullet(SerData ser) {
        this.bulletWarInfo.decode(ser.getBulletWarInfoPb());
    }

    private void seBullet(SerData.Builder ser) {
        ser.setBulletWarInfoPb(this.bulletWarInfo.encode());
    }


}
