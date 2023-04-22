package com.game.domain;

import com.game.constant.Country;
import com.game.constant.PvpPlaceId;
import com.game.constant.SimpleId;
import com.game.constant.WorldBossId;
import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.p.BroodWarDictater;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.ChatShow;
import com.game.domain.p.CityRemark;
import com.game.domain.p.DigInfo;
import com.game.domain.p.PvpBattle;
import com.game.domain.p.PvpHero;
import com.game.domain.p.RankPvpHero;
import com.game.domain.p.Team;
import com.game.domain.p.World;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldTarget;
import com.game.domain.p.WorldTargetTask;
import com.game.manager.BroodWarManager;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.TwoInt;
import com.game.pb.DataPb;
import com.game.pb.DataPb.MarchData;
import com.game.pb.SerializePb;
import com.game.pb.SerializePb.SerChatShowData;
import com.game.pb.SerializePb.SerPvpBattleData;
import com.game.pb.SerializePb.SerWorldBossData;
import com.game.pb.SerializePb.SerWorldTargetData;
import com.game.pb.SerializePb.SerZergRecord;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.worldmap.March;
import com.game.worldmap.WorldBoss;
import com.game.worldmap.fight.zerg.ZergData;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;


// 世界数据
public class WorldData {

    private int keyId;
    private ConcurrentHashMap<Integer, WorldBoss> bossMap = new ConcurrentHashMap<Integer, WorldBoss>();  // 张角是每个国家一个Boss, countryId, boss
    private WorldBoss shareBoss = null;  // 剩余两个是所有的国家共享
    private ConcurrentHashMap<Integer, WorldTarget> worldTargets = new ConcurrentHashMap<Integer, WorldTarget>();  // 只存放城池被攻打的状态
    private long lastSaveTime;     // 上次存盘时间
    private int season;     // 当前季节
    private long seasonEndTime;    // 下次季节的结束时间
    private int seasonUp;//默认为1，新服开启或者老服维护后此值为2（用于判断是否取第一个星期几）
    private int effect;     // 效果铁铜油石兵
    private Map<Integer, ChatShow> chatShowMap = new HashMap<Integer, ChatShow>();
    private int worldPvpState;                                                       // 血战状态
    private LinkedList<PvpBattle> pvpBattles = new LinkedList<PvpBattle>();                   // 处于4个区域的战斗
    private LinkedList<RankPvpHero> rankList = new LinkedList<RankPvpHero>();                 // 连杀排行榜: 武将Id, 玩家
    private HashSet<Long> attenders = new HashSet<Long>();                               // 参与血战
    private LinkedList<PvpHero> deadHeroes = new LinkedList<PvpHero>();                     // 死亡中的英雄, 时间到死亡的英雄从这里删除, 同时删除玩家的英雄
    private HashMap<Integer, HashMap<Integer, DigInfo>> digPapers = new HashMap<Integer, HashMap<Integer, DigInfo>>(); // 挖到的图纸
    private long banquetEndTime;                                                     // 国宴结束时间
    private int pvpCountry;                                                          // 当前占领世界要塞的国家
    private long pvpPeriod;                                                          // 防守累计时长
    private long pvpEndTime;                                                         // 防守倒计时
    private long pvpBattleScore;                                                     // 血战经验值[战斗结束后需要清除, 记住需要存盘]
    private Map<Integer, HashSet<Integer>> roitRecord = new HashMap<Integer, HashSet<Integer>>();             // 记录暴乱完成的状态, key:活动Id value:阶段Id
    private Map<Integer, WorldTargetTask> tasks = new ConcurrentHashMap<>();                    //世界目标任务
    private Map<Integer, WorldActPlan> worldActPlans = new ConcurrentHashMap<>();        //世界活动活动的信息
    private int totalMaxOnLineNum;//总最高在线
    private int todayMaxOnLineNum;//今日最高在线
    private Date refreshTime; //刷新世界
    private String stealCity;//抢夺名城数据
    private long activityEndTime;   //活动结束时间
    private int riotLevel;  //虫族入侵怪物等级
    private int target;//当前进程阶段
    @Getter
    @Setter
    private String bigMonster;

    /**
     * 母巢职位信息
     */
    @Getter
    private Map<Integer, BroodWarPosition> appoints = new ConcurrentHashMap<>();
    /**
     * 历届独裁者信息
     */
    @Getter
    private LinkedList<BroodWarDictater> hofs = new LinkedList<>();
	@Getter
	@Setter
	private ZergData zergData;

    @Getter
    private Map<Integer, AtomicInteger> remarkMap = new ConcurrentHashMap<>();

    private Map<Integer, CityRemark> cityRemarkMap = new ConcurrentHashMap<>();


    public long getActivityEndTime() {
        return activityEndTime;
    }

    public void setActivityEndTime(long activityEndTime) {
        this.activityEndTime = activityEndTime;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public long getLastSaveTime() {
        return lastSaveTime;
    }

    public void setLastSaveTime(long lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    public Map<Integer, WorldBoss> getBossMap() {
        return bossMap;
    }

    public void setBossMap(ConcurrentHashMap<Integer, WorldBoss> bossMap) {
        this.bossMap = bossMap;
    }


    public byte[] serBossData() {
        SerWorldBossData.Builder builder = SerWorldBossData.newBuilder();
        for (WorldBoss worldBoss : bossMap.values()) {
            if (worldBoss == null) {
                continue;
            }
            builder.addWorldBoss(worldBoss.writeData());
        }

        if (shareBoss != null) {
            builder.setShareBoss(shareBoss.writeData());
        }

        return builder.build().toByteArray();
    }

    public byte[] serTargetData() {
        SerWorldTargetData.Builder builder = SerWorldTargetData.newBuilder();
        for (WorldTarget worldTarget : worldTargets.values()) {
            if (worldTarget == null) {
                continue;
            }
            builder.addWorldTarget(worldTarget.writeData());
        }


        return builder.build().toByteArray();
    }

    public byte[] serWorldActPlanData() {
        SerializePb.SerWorldActPlanData.Builder builder = SerializePb.SerWorldActPlanData.newBuilder();
        for (WorldActPlan worldActPlan : worldActPlans.values()) {
            SerializePb.SerWorldActPlan.Builder serWorldActPlan = SerializePb.SerWorldActPlan.newBuilder();
            CommonPb.WorldActPlan.Builder worldActPlanPb = CommonPb.WorldActPlan.newBuilder();
            worldActPlanPb.setId(worldActPlan.getId());
            worldActPlanPb.setOpenTime(worldActPlan.getOpenTime());
            worldActPlanPb.setState(worldActPlan.getState());
            worldActPlanPb.setPreheatTime(worldActPlan.getPreheatTime());
            worldActPlanPb.setEndTime(worldActPlan.getEndTime());
            worldActPlanPb.setEnterTime(worldActPlan.getEnterTime());
            worldActPlanPb.setExhibitionTime(worldActPlan.getExhibitionTime());
            serWorldActPlan.setWorldActPlan(worldActPlanPb);
            serWorldActPlan.setTargetSuccessTime(worldActPlan.getTargetSuccessTime());
            builder.addSerWorldActPlan(serWorldActPlan);
        }
        return builder.build().toByteArray();

    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public long getSeasonEndTime() {
        return seasonEndTime;
    }

    public void setSeasonEndTime(long seasonEndTime) {
        this.seasonEndTime = seasonEndTime;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public WorldBoss getShareBoss() {
        return shareBoss;
    }

    public void setShareBoss(WorldBoss param) {
        this.shareBoss = param;
    }

    public ConcurrentHashMap<Integer, WorldTarget> getWorldTargets() {
        return worldTargets;
    }

    public void setWorldTargets(ConcurrentHashMap<Integer, WorldTarget> worldTargets) {
        this.worldTargets = worldTargets;
    }

    public WorldBoss getWorldBoss(int country) {
        for (WorldBoss elem : bossMap.values()) {
            if (elem.getMonsterId() == WorldBossId.WORLD_BOSS_1 &&
                    elem.getCountry() == 1) {
                return elem;
            }
        }

        return null;
    }

    public Map<Integer, ChatShow> getChatShowMap() {
        return chatShowMap;
    }

    public void setChatShowMap(Map<Integer, ChatShow> chatShowMap) {
        this.chatShowMap = chatShowMap;
    }

    public byte[] serChatShowData() {
        SerChatShowData.Builder builder = SerChatShowData.newBuilder();
        for (ChatShow chatShow : chatShowMap.values()) {
            if (chatShow == null) {
                continue;
            }
            builder.addChatShowData(chatShow.writeData());
        }
        return builder.build().toByteArray();
    }

    public byte[] serPvpBattleData() {
        SerPvpBattleData.Builder builder = SerPvpBattleData.newBuilder();
        builder.setWorldPvpState(worldPvpState);
        for (PvpBattle pvpBattle : pvpBattles) {
            if (pvpBattle != null) {
                builder.addPvpBattles(pvpBattle.writeData());
            }
        }

        for (RankPvpHero rankPvpHero : SpringUtil.getBean(BroodWarManager.class).getRanks()) {
            if (rankPvpHero != null) {
                builder.addRankList(rankPvpHero.writeData());
            }
        }

        for (Long lordId : attenders) {
            builder.addAttenders(lordId);
        }

        for (PvpHero pvpHero : deadHeroes) {
            if (pvpHero != null) {
                builder.addDeadHeros(pvpHero.writeData());
            }
        }

        for (Map.Entry<Integer, HashMap<Integer, DigInfo>> entryset : digPapers.entrySet()) {
            if (entryset == null) {
                continue;
            }

            DataPb.CountryDigInfo.Builder digInfo = DataPb.CountryDigInfo.newBuilder();
            int country = entryset.getKey();
            HashMap<Integer, DigInfo> digInfoMap = entryset.getValue();
            digInfo.setCountry(country);
            for (DigInfo digInfoElem : digInfoMap.values()) {
                if (digInfoElem != null) {
                    digInfo.addInfo(digInfoElem.writeData());
                }
            }
            builder.addCountryDigInfo(digInfo);
        }
        builder.setBanquetEndTime(banquetEndTime);
        builder.setPvpCountry(pvpCountry);
        builder.setPvpEndTime(pvpEndTime);
        builder.setPvpPeriod(pvpPeriod);
        builder.setActivityEndTime(activityEndTime);

        return builder.build().toByteArray();
    }


    public byte[] serWorldTarget() {
        CommonPb.WorldTaget.Builder builder = CommonPb.WorldTaget.newBuilder();
        for (Map.Entry<Integer, WorldTargetTask> targetTaskEntry : tasks.entrySet()) {
            builder.addWorldTargetTask(worldTargetTask(targetTaskEntry.getValue()));
        }
        return builder.build().toByteArray();
    }

    public CommonPb.WorldTargetTask.Builder worldTargetTask(WorldTargetTask worldTargetTask) {
        CommonPb.WorldTargetTask.Builder builder = CommonPb.WorldTargetTask.newBuilder();
        builder.setTaskId(worldTargetTask.getTaskId());
        builder.setCurHp(worldTargetTask.getCurHp());
        worldTargetTask.getCountryTaskProcess().forEach(value -> {
            CommonPb.CountryTaskProcess.Builder process = CommonPb.CountryTaskProcess.newBuilder();
            process.setCountryId(value.getCountryId());
            process.setLastRefreshTime(value.getLastRefreshTime());
            process.setLossSoldier(value.getLossSoldier());
            process.setProcess(value.getPoints());
            process.setArea(value.getArea());
            builder.addCountryProcess(process);
        });
        worldTargetTask.getHitRanks().forEach(x -> {
            CommonPb.WorldRankInfo.Builder builder1 = CommonPb.WorldRankInfo.newBuilder();
            builder1.setRoleId(x.getPlayer().roleId);
            builder1.setHit(x.getHit());
            builder1.setTotalHit(x.getTotalHit());
            builder1.setTime(x.getTime());
            builder.addRankInfo(builder1);
        });
        builder.setOpenTime(worldTargetTask.getOpenTime());
        builder.setNum(worldTargetTask.getNum());
        builder.setCount(worldTargetTask.getCount());
        builder.setComplete(worldTargetTask.getComplete());
        return builder;
    }

	public byte[] serZergData() {
		SerZergRecord.Builder builder = SerZergRecord.newBuilder();
		if (zergData == null) {
			return builder.build().toByteArray();
		}
		builder.setRecordDate(zergData.getRecordDate());
		builder.setStartTime(zergData.getStartTime());
		builder.setEndTime(zergData.getEndTime());
		builder.setMonsterId(zergData.getMonsterId());
		builder.setStatus(zergData.getStatus());
		builder.setStep(zergData.getStep());
		builder.setStepStartTime(zergData.getStepStartTime());
		builder.setStepEndTime(zergData.getStepEndTime());
		builder.addAllStepParam(zergData.getStepParam() == null ? new ArrayList<>() : zergData.getStepParam());
		builder.setCityId(zergData.getCityId());
		builder.setAwardTime(zergData.getAwardTime());
		builder.setOpenTimes(zergData.getOpenTimes());
		if (zergData.getTeam() != null) {
			builder.setTeam(zergData.getTeam().wrapPb());
		}
		if (zergData.getMarches() != null) {
			for (March march : zergData.getMarches()) {
				builder.addMarchData(march.writeMarch().build());
			}
		}
		builder.setStepFinish(zergData.getStepFinish());
		return builder.build().toByteArray();
	}
	public void dserZergData(byte[] zergBytes) {
		if (zergBytes == null) {
			return;
		}
		try {
			SerZergRecord ser = SerZergRecord.parseFrom(zergBytes);
			ZergData zergData = new ZergData();
			zergData.setRecordDate(ser.getRecordDate());
			zergData.setStartTime(ser.getStartTime());
			zergData.setEndTime(ser.getEndTime());
			zergData.setMonsterId(ser.getMonsterId());
			zergData.setStatus(ser.getStatus());
			zergData.setStep(ser.getStep());
			zergData.setStepStartTime(ser.getStepStartTime());
			zergData.setStepEndTime(ser.getStepEndTime());
			zergData.setCityId(ser.getCityId());
			zergData.setAwardTime(ser.getAwardTime());
			zergData.setOpenTimes(ser.getOpenTimes());
            zergData.setStepFinish(ser.getStepFinish());
			DataPb.Team dbTeam = ser.getTeam();
			if (ser.getStepParamCount() == 0) {
				zergData.setStepParam(new ArrayList<>());
			} else {
				zergData.setStepParam(ser.getStepParamList());
			}
			zergData.setMarches(new ArrayList<>());
			if (ser.getMarchDataCount() != 0) {
				for (MarchData marchData : ser.getMarchDataList()) {
					March march = new March();
					march.readMarch(marchData);
					zergData.getMarches().add(march);
				}
			}
			if (dbTeam != null) {
				Team team = new Team();
				team.unWrapPb(dbTeam);
				zergData.setTeam(team);
			}
			this.zergData = zergData;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}


    public int getWorldPvpState() {
        return worldPvpState;
    }

    public void setWorldPvpState(int worldPvpState) {
        this.worldPvpState = worldPvpState;
    }

    public LinkedList<PvpBattle> getPvpBattles() {
        return pvpBattles;
    }

    public void setPvpBattles(LinkedList<PvpBattle> pvpBattles) {
        this.pvpBattles = pvpBattles;
    }

    public LinkedList<RankPvpHero> getRankList() {
        return rankList;
    }

    public void setRankList(LinkedList<RankPvpHero> rankList) {
        this.rankList = rankList;
    }

    public HashSet<Long> getAttenders() {
        return attenders;
    }

    public void setAttenders(HashSet<Long> attenders) {
        this.attenders = attenders;
    }

    public LinkedList<PvpHero> getDeadHeroes() {
        return deadHeroes;
    }

    public void setDeadHeroes(LinkedList<PvpHero> deadHeroes) {
        this.deadHeroes = deadHeroes;
    }

    public HashMap<Integer, HashMap<Integer, DigInfo>> getDigPapers() {
        return digPapers;
    }

    public void setDigPapers(HashMap<Integer, HashMap<Integer, DigInfo>> digPapers) {
        this.digPapers = digPapers;
    }

    public long getBanquetEndTime() {
        return banquetEndTime;
    }

    public void setBanquetEndTime(long banquetEndTime) {
        this.banquetEndTime = banquetEndTime;
    }

    public void checkPvpBattle() {
        for (int index = PvpPlaceId.UP; index <= PvpPlaceId.CENTER; index++) {
            if (!hasPlaceId(index)) {
                PvpBattle pvpBattle = new PvpBattle();
                pvpBattle.setPlaceId(index);
                pvpBattles.add(pvpBattle);
                if (pvpBattles.size() > 4) {
                    LogHelper.CONFIG_LOGGER.error("存入pvpBattles ->[{}]", new Exception("error"));
                }
            }
        }

    }

    public boolean hasPlaceId(int placeId) {
        for (PvpBattle battle : pvpBattles) {
            if (battle.getPlaceId() == placeId) {
                return true;
            }
        }
        return false;
    }

    public void checkDigInfo() {
        for (int i = 1; i <= 3; i++) {
            HashMap<Integer, DigInfo> digInfoMap = digPapers.get(i);
            if (digInfoMap == null) {
                digInfoMap = new HashMap<Integer, DigInfo>();
                digPapers.put(i, digInfoMap);
            }
        }
    }

    public PvpBattle getPvpBattle(int placeId) {
        for (PvpBattle pvpBattle : pvpBattles) {
            if (pvpBattle.getPlaceId() == placeId) {
                return pvpBattle;
            }
        }
        return null;
    }

    public void clearAttenders() {
        attenders.clear();
    }

    public void addAttenders(long lordId) {
        if (attenders.contains(lordId)) {
            return;
        }

        attenders.add(lordId);
    }

    public void handlePvpClear() {
        clearBattles();
        clearRank();
        clearAttenders();
        clearDeadHeroes();
        clearDigPaper();
    }

    public void clearBattles() {
        for (PvpBattle pvpBattle : pvpBattles) {
            if (pvpBattle != null) {
                pvpBattle.clear();
            }
        }
    }

    public void clearRank() {
        rankList.clear();
    }

    public void clearDeadHeroes() {
        deadHeroes.clear();
    }

    public void clearDigPaper() {
        for (HashMap<Integer, DigInfo> digInfo : digPapers.values()) {
            if (digInfo == null) {
                continue;
            }
            digInfo.clear();
        }
    }

    public int getPvpCountry() {
        return pvpCountry;
    }

    public void setPvpCountry(int pvpCountry) {
        this.pvpCountry = pvpCountry;
    }

    public long getPvpPeriod() {
        return pvpPeriod;
    }

    public void setPvpPeriod(long pvpPeriod) {
        this.pvpPeriod = pvpPeriod;
    }

    public long getPvpEndTime() {
        return pvpEndTime;
    }

    public void setPvpEndTime(long pvpEndTime) {
        this.pvpEndTime = pvpEndTime;
    }

    public long getPvpBattleScore() {
        return pvpBattleScore;
    }

    public void setPvpBattleScore(long pvpBattleScore) {
        this.pvpBattleScore = pvpBattleScore;
    }

    // 增加经验值
    public void addBattleScore(int lost) {
        if (this.pvpBattleScore >= Long.MAX_VALUE - (long) lost) {
            return;
        }

        this.pvpBattleScore += lost;
    }

    public Map<Integer, HashSet<Integer>> getRoitRecord() {
        return roitRecord;
    }

    public void setRoitRecord(Map<Integer, HashSet<Integer>> roitRecord) {
        this.roitRecord = roitRecord;
    }

    public Map<Integer, WorldTargetTask> getTasks() {
        return tasks;
    }

    public Map<Integer, WorldActPlan> getWorldActPlans() {
        return worldActPlans;
    }

    public void setWorldActPlans(Map<Integer, WorldActPlan> worldActPlans) {
        this.worldActPlans = worldActPlans;
    }

    public int getTotalMaxOnLineNum() {
        return totalMaxOnLineNum;
    }

    public void setTotalMaxOnLineNum(int totalMaxOnLineNum) {
        this.totalMaxOnLineNum = totalMaxOnLineNum;
    }

    public int getTodayMaxOnLineNum() {
        return todayMaxOnLineNum;
    }

    public void setTodayMaxOnLineNum(int todayMaxOnLineNum) {
        this.todayMaxOnLineNum = todayMaxOnLineNum;
    }

    public Date getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Date refreshTime) {
        this.refreshTime = refreshTime;
    }

    public String getStealCity() {
        return stealCity;
    }

    public void setStealCity(String stealCity) {
        this.stealCity = stealCity;
    }

    public int getSeasonUp() {
        return seasonUp;
    }

    public void setSeasonUp(int seasonUp) {
        this.seasonUp = seasonUp;
    }

    public int getRiotLevel() {
        return riotLevel;
    }

    public void setRiotLevel(int riotLevel) {
        this.riotLevel = riotLevel;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void dserCityRemarkData(World world) {
        try {
            byte[] remark = world.getRemark();
            StaticLimitMgr staticLimitMgr = SpringUtil.getBean(StaticLimitMgr.class);
            int num = staticLimitMgr.getNum(SimpleId.CITY_REMARK_COUNT);
            boolean flag = false;
            if (remark == null) {
                flag = true;
            } else {
                CommonPb.CityRemarkList cityRemarkCount = CommonPb.CityRemarkList.parseFrom(remark);
                List<TwoInt> countList = cityRemarkCount.getCountList();
                if (countList == null) {
                    flag = true;
                } else {
                    countList.forEach(x -> {
                        remarkMap.put(x.getV1(), new AtomicInteger(x.getV2()));
                    });
                }
                List<CommonPb.CityRemark> cityRemarkList = cityRemarkCount.getCityRemarkList();
                cityRemarkList.forEach(x -> {
                    CityRemark cityRemark = cityRemarkMap.computeIfAbsent(x.getCountry(), y -> new CityRemark());
                    cityRemark.decode(x);
                });
            }
            if (flag) {
                Country[] values = Country.values();
                for (Country value : values) {
                    remarkMap.computeIfAbsent(value.getKey(), x -> new AtomicInteger(num));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] cityRemarkDb() {
        CommonPb.CityRemarkList.Builder builder1 = CommonPb.CityRemarkList.newBuilder();
        cityRemarkMap.values().forEach(x -> {
            builder1.addCityRemark(x.encode());
        });
        com.game.constant.Country[] values = com.game.constant.Country.values();
        for (Country value : values) {
            CommonPb.TwoInt.Builder builder = CommonPb.TwoInt.newBuilder();
            builder.setV1(value.getKey());
            AtomicInteger atomicInteger = remarkMap.get(value.getKey());
            builder.setV2(atomicInteger.get());
            builder1.addCount(builder);
        }
        return builder1.build().toByteArray();
    }

    public CityRemark getCityRemark(int country) {

        return cityRemarkMap.get(country);
    }

    public Map<Integer, CityRemark> getCityRemarkMap() {
        return cityRemarkMap;
    }
}
