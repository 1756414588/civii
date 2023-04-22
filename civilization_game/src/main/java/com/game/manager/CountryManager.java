package com.game.manager;

import com.game.constant.*;
import com.game.dao.p.CountryDao;
import com.game.dataMgr.StaticCountryMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Award;
import com.game.domain.CountryData;
import com.game.domain.Nation;
import com.game.domain.Player;
import com.game.domain.p.Country;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.domain.GloverLog;
import com.game.pb.CommonPb;
import com.game.pb.CountryPb;
import com.game.server.GameServer;
import com.game.util.*;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class CountryManager {
    @Autowired
    private CountryDao countryDao;
    @Autowired
    private StaticCountryMgr staticCountryMgr;
    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private CityManager cityManager;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    private StaticWorldMgr staticWorldMgr;
    @Autowired
    private StaticLimitMgr staticLimitMgr;
    @Autowired
    private EquipManager equipManager;
    @Autowired
    private StaticHeroMgr staticHeroMgr;
    @Autowired
    private ChatManager chatManager;
    @Autowired
    private HeroManager heroManager;
    @Autowired
    private ServerManager serverManager;

    private long lastTime;

    // 所有国家的信息
    private ConcurrentHashMap<Integer, CountryData> countrys = new ConcurrentHashMap<Integer, CountryData>();
    // 按照国家导人信息
    private ConcurrentHashMap<Integer, Integer> playerNum = new ConcurrentHashMap<Integer, Integer>(); // 玩家数量(新手玩家国家选取)

    /**
     * 加载国家,数据库缺失国家数据则直接失败,不让服务器启动
     */
    public void loadCountry() throws Exception {
        this.lastTime = System.currentTimeMillis();
        // 从数据库读取国家存盘数据
        List<Country> list = countryDao.selectCountryList();
        if (list.size() != 3) {
            if (list.size() == 0) {
                for (int i = 1; i < 4; i++) {
                    Country country = new Country(i, 1);
                    countryDao.insertSelective(country);
                    list.add(country);
                }
            } else {
                throw new ConfigException("country size == " + list.size());
            }

        }

        for (int i = 0; i < 3; i++) {
            Country country = list.get(i);
            if (country == null) {
                LogHelper.CONFIG_LOGGER.info("country is null!");
                continue;
            }
            countrys.put(country.getCountryId(), new CountryData(country));
        }

        // 已经开州,因其他原因导致没有开启选举,则再服务器启动的时候开启选举
        if (cityManager.captureCity(staticLimitMgr.getNum(163))) {
            Iterator<CountryData> it = countrys.values().iterator();
            while (it.hasNext()) {
                CountryData coutry = it.next();
                if (coutry.voteState == CountryConst.VOTE_NO) {
                    coutry.voteState = CountryConst.VOTE_PREPREA;
                }
            }
        }

        checkCountryHero();
        initPlayerNum();
        serverManager.updateBootStrap("courty");
    }

    public void initPlayerNum() {
        for (int i = 1; i <= 3; i++) {
            playerNum.put(i, 0);
        }

        Map<Long, Player> playerMap = playerManager.getPlayers();
        for (Player player : playerMap.values()) {
            if (player == null) {
                continue;
            }
            updatePlayerCt(player.getCountry());
        }
    }

    // 更新国家信息
    public void updateCountry(Country country) {
        if (country.getAnnouncement() != null) {
            country.setAnnouncement(EmojiUtil.emojiChange(country.getAnnouncement()));
        }
        countryDao.updateCountry(country);
    }

    /**
     * 获取国家信息
     */
    public CountryData getCountry(int country) {
        return countrys.get(country);
    }

    public ConcurrentHashMap<Integer, CountryData> getCountrys() {
        return countrys;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * 国家增加经验
     */
    public synchronized int addCountryExp(CountryData country, int exp) {

        // 判断国家是否满级, 满经验值
        int maxLevel = staticCountryMgr.getMaxLevel();
        if (country.getLevel() >= maxLevel) {
            return 1;
        }

        StaticCountryLevel countryLv = staticCountryMgr.getCountryLvExp(country.getLevel() + 1);
        if (countryLv == null) {
            LogHelper.CONFIG_LOGGER.info("countryLv is null, level = " + country.getLevel() + 1);
            return -1;
        }

        long countryExp = country.getExp() + exp;
        // 当前经验比下一级经验还多，则进行升级
        if (countryExp >= countryLv.getNeedExp()) {
            int level = Math.min(maxLevel, countryLv.getCountryLv());
            country.setLevel(level);
            country.setExp(countryExp - countryLv.getNeedExp());
        } else {
            country.setExp(countryExp);
        }

        return 0;
    }

    /**
     * 获取玩家国家信息
     */
    public Nation getNation(Player player) {
        Nation nation = player.getNation();
        int refreshTime = nation.getRefreshTime();
        long weekTime = nation.getWeekTime();
        // 每日荣誉,任务刷新
        if (refreshTime != GameServer.getInstance().currentDay) {
            nation.getGloryLv().clear();
            nation.setVote(0);
            nation.setBuild(0);
            nation.refreshTask();
            nation.setRefreshTime(GameServer.getInstance().currentDay);
        }
        // 周荣誉刷新
        if (weekTime != TimeHelper.getFridayTime()) {
            nation.setTotalBuild(0);
            nation.setTotalCtWar(0);
            nation.setTotalPvpWar(0);
            nation.setWeekTime(TimeHelper.getFridayTime());
        }
        return nation;
    }

    /**
     * 获取玩家官职数据
     */
    public CtyGovern getGovern(Player player) {
        CountryData countryData = countrys.get(player.getCountry());
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("countryData is null!");
            return null;
        }

        long lordId = player.roleId;
        CtyGovern govern = countryData.getCtyGovern(lordId);
        if (govern == null) {
            return null;
        }
        return govern;
    }

    /**
     * 国家任务
     */
    public Map<Integer, CtyTask> getCountryTask(Player player) {
        Nation nation = getNation(player);
        if (nation == null) {
            LogHelper.CONFIG_LOGGER.info("getNewTask nation is null!");
            return new HashMap<Integer, CtyTask>();
        }

        Map<Integer, CtyTask> taskMap = nation.getCtyTask();

        // 星期五没有任务
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.FRIDAY) {
            return new HashMap<Integer, CtyTask>();
        }

        long nowTaskTime = System.currentTimeMillis();
        if (taskMap.isEmpty()) {
            nation.setTaskTime(nowTaskTime + TimeHelper.HOUR_MS * 3);
            getNewTask(player);
        } else {
            long taskTime = nation.getTaskTime();
            if (taskTime <= nowTaskTime) {
                // flush task
                getNewTask(player);
                nation.refreshTask();
                nation.setTaskTime(nowTaskTime + TimeHelper.HOUR_MS * 3);
            }
        }

        return taskMap;
    }

    // 获取当前最新的任务
    public void getNewTask(Player player) {
        CountryData countryData = getCountry(player.getCountry());
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("countryData is null.");
            return;
        }

        Nation nation = getNation(player);
        if (nation == null) {
            LogHelper.CONFIG_LOGGER.info("getNewTask nation is null!");
            return;
        }

        Map<Integer, CtyTask> taskMap = nation.getCtyTask();
        taskMap.clear();
        List<StaticCountryTask> tasks = staticCountryMgr.getCountryTaskByLv(countryData.getLevel());
        if (tasks == null) {
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            StaticCountryTask countryTask = tasks.get(i);
            if (countryTask == null) {
                continue;
            }
            taskMap.put(countryTask.getTaskId(), new CtyTask(countryTask));
        }
    }

    /**
     * 获取单个国家任务
     */
    public CtyTask getCountryTask(Player player, int taskId) {
        Map<Integer, CtyTask> taskMap = getCountryTask(player);
        return taskMap.get(taskId);
    }

    public CtyTask getCountryTaskByType(Player player, int taskType) {
        Map<Integer, CtyTask> taskMap = getCountryTask(player);
        for (CtyTask ctyTask : taskMap.values()) {
            if (ctyTask == null) {
                continue;
            }

            StaticCountryTask staticCountryTask = staticCountryMgr.getCountryTask(ctyTask.getTaskId());
            if (staticCountryTask == null) {
                LogHelper.CONFIG_LOGGER.info("config is null, ctkTask Id = " + ctyTask.getTaskId());
                continue;
            }

            if (staticCountryTask.getType() == taskType) {
                return ctyTask;
            }
        }

        return null;
    }

    /**
     * 更新国家任务[需要同步任务状态给客户端]
     */
    public void doCountryTask(Player player, int taskType, int value) {
        CtyTask ctyTask = getCountryTaskByType(player, taskType);
        if (ctyTask == null) {
            // LogHelper.CONFIG_LOGGER.info("ctyTask is null, taskType = " +
            // taskType);
            return;
        }

        if (ctyTask.getState() >= 1) {
            return;
        }

        StaticCountryTask staticCountryTask = staticCountryMgr.getCountryTask(ctyTask.getTaskId());
        if (staticCountryTask == null) {
            LogHelper.CONFIG_LOGGER.info("ctyTask is null, taskId = " + ctyTask.getTaskId());
            return;
        }

        if (ctyTask.getCond() >= staticCountryTask.getCond()) {
            return;
        }

        int cond = ctyTask.getCond() + value;
        if (cond >= staticCountryTask.getCond()) {
            ctyTask.setState(1);
        }
        ctyTask.setCond(cond);

        // 同步客户端数据
        CountryPb.SynCountryTaskRq.Builder builder = CountryPb.SynCountryTaskRq.newBuilder();
        builder.setCountryTask(ctyTask.wrapPb());
        SynHelper.synMsgToPlayer(player, CountryPb.SynCountryTaskRq.EXT_FIELD_NUMBER, CountryPb.SynCountryTaskRq.ext, builder.build());

    }

    /**
     * 更新国家荣誉记录
     *
     * @param player
     * @param type
     */
    public void updCountryHoror(Player player, int type) {
        CountryData countryData = getCountry(player.getCountry());
        if (countryData == null) {
            return;
        }
        CountryRank countryRank = countryData.getCountryRank(type);
        if (countryRank == null) {
            countryRank = new CountryRank();
            countryRank.setType(type);
            countryData.getRanks().put(type, countryRank);
        }

        // 国家当天的荣誉
        int totalValue = updCountryTodayHoror(player, type);
        if (totalValue != 0) {
            countryRank.addRank(player, totalValue);
        }

    }

    /**
     * 国家当天荣誉目标
     *
     * @param player
     * @param type
     */
    public int updCountryTodayHoror(Player player, int type) {
        CountryData countryData = getCountry(player.getCountry());
        if (countryData == null) {
            return 0;
        }

        Nation nation = getNation(player);
        CtyGlory glory = countryData.getGlory();

        int total = 0;
        if (type == CountryConst.RANK_CITY) {
            glory.addCityFight(1);
            nation.addPvpWar(1);
            total = nation.getTotalPvpWar();
        } else if (type == CountryConst.RANK_STATE) {
            glory.addStateFight(1);
            nation.addCtWar(1);
            total = nation.getTotalCtWar();
        } else if (type == CountryConst.RANK_BUILD) {
            glory.addBuild(1);
            nation.addBuild(1);
            total = nation.getTotalBuild();
        }

        //推送玩家荣誉任务完成红点
        CountryPb.SynCountryGloryRq msg = CountryPb.SynCountryGloryRq.newBuilder().setLordId(player.roleId).setType(1).build();
        Set<Integer> currentDayPush = glory.getCurrentDayPush();
        for (StaticCountryGlory e : staticCountryMgr.getGlorys().values()) {
            if (!currentDayPush.contains(e.getGloryId()) && glory.getBuilds() >= e.getBuilds() && glory.getCityFight() >= e.getCityFight() && glory.getStateFight() >= e.getStateFight()) {
                playerManager.getOnlinePlayer().forEach(x -> {
                    Nation nationTar = getNation(x);
                    if (nationTar != null && nationTar.getGloryLv() != null
                            && e != null && nationTar.getGloryLv().containsKey(e.getGloryId())) {
                        SynHelper.synMsgToPlayer(x, CountryPb.SynCountryGloryRq.EXT_FIELD_NUMBER, CountryPb.SynCountryGloryRq.ext, msg);
                    }
                });
                currentDayPush.add(e.getGloryId());
                break;
            }
        }
        return total;
    }

    /**
     * 添加国家日志 1.国战失败 2.重建据点 3.国战成功 4.撤销城主
     */
    public void addCountryDaily(int country, CtyDaily ctyDaily) {
        CountryData countryData = getCountry(country);
        if (countryData == null) {
            return;
        }

        ConcurrentLinkedQueue<CtyDaily> dailys = countryData.getDailys();

        dailys.add(ctyDaily);

        if (dailys.size() > CountryConst.DAILY_MAX) {
            dailys.remove();
        }
    }

    /**
     * 获取爵位的攻击
     */
    public Property getTitleAttack(Player player) {
        Property property = new Property();
        StaticCountryTitle staticTitle = staticCountryMgr.getCountryTitle(player.getTitle());
        if (staticTitle == null) {
            return property;
        }
        property.setSoldierNum(staticTitle.getSoldierCount());
        property.setAttack(staticTitle.getAttack());
        property.setDefence(staticTitle.getDefence());
        return property;
    }

    public Property getTitleAttack(Player player, Property property) {
        StaticCountryTitle staticTitle = staticCountryMgr.getCountryTitle(player.getTitle());
        if (staticTitle == null) {
            return property;
        }
        property.addAttackValue(staticTitle.getAttack());
        property.addDefenceValue(staticTitle.getDefence());
        property.addSoldierNumValue(staticTitle.getSoldierCount());
        return property;
    }

    // 玩家是否是国王
    public boolean hasWarPermission(long lordId) {
        for (CountryData countryData : countrys.values()) {
            if (countryData.hasPermission(lordId)) {
                return true;
            }
        }
        return true; // to false
    }

    /**
     * 排行榜容易转换未投票
     *
     * @param country
     */
    public void changeRankToVote(CountryData country) {
        Iterator<CountryRank> it = country.getRanks().values().iterator();
        while (it.hasNext()) {
            CountryRank countryRank = it.next();
            List<CtyRank> list = countryRank.getTopRankList();
            // 初始状态时,不进行累计票
            if (country.voteState != 0) {
                for (CtyRank p : list) {
                    Player target = playerManager.getPlayer(p.getLordId());
                    if (target == null) {
                        continue;
                    }
                    Nation nation = getNation(target);
                    StaticCountryRank staticRank = staticCountryMgr.getCountryRank(countryRank.getType(), p.getRank());
                    if (staticRank == null) {
                        continue;
                    }
                    int voteExtra = nation.getVoteExtra() + staticRank.getVote();
                    voteExtra = voteExtra > 150 ? 150 : voteExtra;
                    nation.setVoteExtra(voteExtra);
                    com.game.log.LogUser.log(LogTable.glover_log, GloverLog.builder()
                            .ticket(staticRank.getVote())
                            .vote(nation.getVoteExtra())
                            .lordId(target.roleId)
                            .nick(target.getNick())
                            .vip(target.getVip())
                            .lv(target.getLevel())
                            .channel(target.getAccount().getChannel())
                            .build());
                }
            }
            countryRank.clearRankList();
        }
    }

    /**
     * 开州调用该接口
     */
    public synchronized void notifyVote() {
        Iterator<CountryData> it = countrys.values().iterator();
        while (it.hasNext()) {
            CountryData next = it.next();

            // 非初始化状态
            if (next.voteState != CountryConst.VOTE_NO) {
                continue;
            }
            next.voteState = CountryConst.VOTE_PREPREA;
        }
    }

    public void updatePlayerCt(int country) {
        if (country <= 0 || country > 3) {
            return;
        }
        Integer num = playerNum.get(country);
        if (num == null) {
            playerNum.put(country, 1);
        } else {
            playerNum.put(country, num + 1);
        }
    }

    public ArrayList<Integer> getMinCt() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        int country = 1;
        int minNum = Integer.MAX_VALUE;
        int countryA = 0;
        int countryB = 0;
        int countryC = 0;
        checkPlayerNum();
        for (Map.Entry<Integer, Integer> entry : playerNum.entrySet()) {
            if (minNum > entry.getValue()) {
                minNum = entry.getValue();
                country = entry.getKey();
            }

            if (entry.getKey() == 1) {
                countryA += entry.getValue();
            } else if (entry.getKey() == 2) {
                countryB += entry.getValue();
            } else if (entry.getKey() == 3) {
                countryC += entry.getValue();
            }
        }

        if (countryA == countryB && countryB == countryC) {
            res.add(1);
            res.add(countryA + countryB + countryC);
            return res;
        }

        res.add(country);
        res.add(countryA + countryB + countryC);
        return res;
    }

    public void checkPlayerNum() {
        for (int i = 1; i <= 3; i++) {
            Integer number = playerNum.get(i);
            if (number == null) {
                playerNum.put(i, 0);
            }
        }

    }

    // 获取排行榜信息
    public List<CtyRank> getRankData(CountryData country, int rankType) {
        List<CtyRank> resRanks = new ArrayList<CtyRank>();
        CountryRank countryRank = country.getCountryRank(rankType);
        if (countryRank == null) {
            return resRanks;
        }

        resRanks = countryRank.getTopRankList();

        return resRanks;
    }

    public List<CommonPb.CountryRank> wrapCountryRank(List<CtyRank> ctyRanks, int type) {
        List<CommonPb.CountryRank> data = new ArrayList<CommonPb.CountryRank>();
        for (int i = 0; i < ctyRanks.size(); i++) {
            CtyRank ctyRank = ctyRanks.get(i);
            if (ctyRank == null) {
                continue;
            }
            int rank = i + 1;
            Player target = playerManager.getPlayer(ctyRank.getLordId());
            if (target == null) {
                continue;
            }

            StaticCountryRank staticRank = staticCountryMgr.getCountryRank(type, rank);
            if (staticRank == null) {
                LogHelper.CONFIG_LOGGER.info("static Rank is null, rank = " + rank);
                continue;
            }

            data.add(PbHelper.createCountryRank(ctyRank.getV(), rank, target.getNick(), staticRank.getVote()).build());
        }

        return data;

    }

    public int getOfficeId(Player player) {
        int countryId = player.getCountry();
        CountryData countryData = getCountry(countryId);
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("country is null!");
            return 0;
        }

        int office = countryData.getGovernId(player.roleId);
        return office;
    }

    // 获得国家名将能够随机的地图Id
    public List<Integer> getHeroMapIds() {
        List<Integer> mapIds = new ArrayList<Integer>();
        if (worldManager.isBoss2Killed()) {
            mapIds.addAll(staticWorldMgr.getPrimaryMapId());
            mapIds.addAll(staticWorldMgr.getMiddleMapId());
            mapIds.addAll(staticWorldMgr.getCenterMapId());
        } else if (worldManager.isBoss1Killed()) {
            mapIds.addAll(staticWorldMgr.getPrimaryMapId());
            mapIds.addAll(staticWorldMgr.getMiddleMapId());
        } else {
            mapIds.addAll(staticWorldMgr.getPrimaryMapId());
        }
        return mapIds;
    }

    // 在地图上刷一个国家名将, 服务器启动的时候也要检查下，看有没有国家名将产生
    public void flushCountryHero(CountryData countryData) {
        if (staticLimitMgr.isCloseCtyHero()) {
            return;
        }

        int countryLv = countryData.getLevel();
        int countryId = countryData.getCountryId();
        for (int i = 4; i <= countryLv; i++) {
            checkCountryLv(countryId, i, countryData);
        }
    }

    public void checkCountryLv(int countryId, int countryLv, CountryData countryData) {
        StaticCountryHero staticCountryHero = staticCountryMgr.getCountryHero(countryId, countryLv);
        if (staticCountryHero == null) {
            return;
        }

        // create country hero
        int heroLv = staticLimitMgr.getNum(137);
        int monsterId = staticCountryHero.getMonsterId();
        int heroId = staticCountryHero.getHeroId();
        Map<Integer, CountryHero> countryHeroMap = countryData.getCountryHeroMap();
        CountryHero checkHero = countryHeroMap.get(heroId);
        if (checkHero != null) {
            // check monster
            Pos heroPos = checkHero.getPos();
            int mapId = worldManager.getMapId(heroPos);
            if (mapId == 0) {
                LogHelper.CONFIG_LOGGER.info("country hero pos error, heroPos = " + heroPos);
                return;
            }
            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
            Monster monster = monsterMap.get(heroPos);
            // 野怪为空,且没有人获得名将的时候
            if (monster == null && checkHero.getLordId() == 0) {
                worldManager.addMonster(heroPos, monsterId, heroLv, mapInfo, AddMonsterReason.COUNTRY_HERO);
            }
            return;
        }

        Integer mapId = getRandomMapId();
        if (mapId == 0) {
            LogHelper.CONFIG_LOGGER.info("mapId is 0.");
            return;
        }

        MapInfo mapInfo = worldManager.getMapInfo(mapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.info("mapInfo is null.");
            return;
        }

        Pos monsterPos = mapInfo.randPickPos();
        if (monsterPos.isError() || !mapInfo.isFreePos(monsterPos)) {
            LogHelper.CONFIG_LOGGER.info("randPos is error!");
            return;
        }
        ArrayList<Entity> list = new ArrayList<>();
        Monster monster = worldManager.addMonster(monsterPos, monsterId, heroLv, mapInfo, AddMonsterReason.COUNTRY_HERO);
        if (monster == null) {
            LogHelper.CONFIG_LOGGER.info("monster is null!");
            return;
        }
        list.add(monster);
        worldManager.synEntityAddRq(list);
        //worldManager.synEntityAddRq(monster, mapId);
        CountryHero countryHero = new CountryHero();
        countryHero.setHeroId(heroId);
        countryHero.setLordId(0);
        countryHero.setState(HeroState.OPENED);
        countryHero.setFightTimes(0);
        countryHero.setHeroLv(heroLv);
        countryHero.setPos(monsterPos);
        countryHeroMap.put(heroId, countryHero);
    }

    public int getRandomMapId() {
        List<Integer> mapIds = getHeroMapIds();
        if (mapIds.isEmpty()) {
            LogHelper.CONFIG_LOGGER.info("mapId is empty.");
            return 0;
        }

        int randIndex = RandomHelper.threadSafeRand(1, mapIds.size());
        Integer mapId = mapIds.get(randIndex - 1);
        if (mapId == null) {
            LogHelper.CONFIG_LOGGER.info("mapId is null.");
            return 0;
        }
        return mapId;
    }

    // 服务器启动时进行检查
    public void checkCountryHero() {
        for (CountryData countryData : countrys.values()) {
            flushCountryHero(countryData);
        }
    }

    public CountryHero getCountryHero(int heroId) {
        for (CountryData countryData : countrys.values()) {
            CountryHero countryHero = countryData.getCountryHero(heroId);
            if (countryHero != null) {
                return countryHero;
            }
        }

        return null;
    }

    // 处理城战玩家英雄忠诚度, 防守方
    public void cityWarHeroLoyalty(Player player, int attackCountry) {
        if (player == null) {
            return;
        }

        // remove loyalty
        int same = staticLimitMgr.getNum(145);
        int notSame = staticLimitMgr.getNum(146);
        Map<Integer, Hero> heroMap = player.getHeros();
        for (Hero hero : heroMap.values()) {
            if (hero == null) {
                continue;
            }

            int heroId = hero.getHeroId();
            StaticCountryHero config = staticCountryMgr.getCountryHero(heroId);
            if (config == null) {
                continue;
            }

            if (config.getCountry() == attackCountry) {
                hero.subLoyalty(same);
            } else {
                hero.subLoyalty(notSame);
            }

            // 同步武将的忠诚度
            CountryPb.SynCountryHeroRq.Builder builder = CountryPb.SynCountryHeroRq.newBuilder();
            builder.setFightHeroId(heroId);
            builder.setHero(hero.wrapPb());
            SynHelper.synMsgToPlayer(player, CountryPb.SynCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.SynCountryHeroRq.ext, builder.build());
        }

        checkHeroEscape(player);
    }

    // 处理玩家名将逃跑
    public void checkHeroEscape(Player player) {
        Map<Integer, Hero> heroMap = player.getHeros();
        Iterator<Hero> iterator = heroMap.values().iterator();
        List<Award> mailAwards = new ArrayList<Award>();
        List<Award> addAwards = new ArrayList<Award>();
        List<Integer> removeHeroId = new ArrayList<Integer>();
        while (iterator.hasNext()) {
            Hero hero = iterator.next();
            if (hero == null) {
                continue;
            }

            int heroId = hero.getHeroId();
            StaticCountryHero config = staticCountryMgr.getCountryHero(heroId);
            if (config == null) {
                continue;
            }
            int loyalty = hero.getLoyalty() / 10 * 10;
            Integer escapeRate = 0;
            if (config.getCountry() == player.getCountry()) {
                escapeRate = staticCountryMgr.getEscapeRate(1, loyalty);
            } else {
                escapeRate = staticCountryMgr.getEscapeRate(2, loyalty);
            }

            if (escapeRate == null) {
                LogHelper.CONFIG_LOGGER.info("escapeRate == null, loyalty = " + loyalty);
                return;
            }

            if (hero.getLoyalty() <= 0) {
                if (!isHeroStateOk(player, hero)) {
                    continue;
                }

                handleHeroEquip(hero, mailAwards, addAwards, player); // 处理英雄装备
                countryHeroEscape(hero.getHeroId(), false); // 名将逃跑
                removeHeroId.add(heroId);
                removeWallHero(player, heroId);
                iterator.remove();
            } else {
                if (!isHeroStateOk(player, hero)) {
                    continue;
                }

                // 计算逃跑概率
                int randNum = RandomHelper.threadSafeRand(1, 100);
                if (randNum < escapeRate) { // 逃跑
                    handleHeroEquip(hero, mailAwards, addAwards, player); // 处理英雄装备
                    countryHeroEscape(hero.getHeroId(), false); // 名将逃跑
                    removeHeroId.add(heroId);
                    iterator.remove();
                }
            }
        }

        // 有删除的英雄
        if (!removeHeroId.isEmpty()) {
            CountryPb.SynCountryHeroRq.Builder builder = CountryPb.SynCountryHeroRq.newBuilder();
            for (Award award : addAwards) {
                builder.addAward(award.wrapPb());
            }
            builder.addAllHeroRemoveId(removeHeroId);
            SynHelper.synMsgToPlayer(player, CountryPb.SynCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.SynCountryHeroRq.ext, builder.build());
            // 发送邮件
            if (!mailAwards.isEmpty()) {
                playerManager.sendAttachMail(player, addAwards, MailId.GIVE_AWARD);
            }
            heroManager.synBattleScoreAndHeroList(player,player.getAllHeroList());
        }

        for (Integer heroId : removeHeroId) {
            if (heroId == null) {
                continue;
            }

            sendLoseHeroMail(player, heroId);
        }
    }

    public boolean isHeroStateOk(Player player, Hero hero) {
        if (hero == null) {
            return false;
        }
        // 行军
        if (player.isInMarch(hero)) {
            return false;
        }
        // 集结
        if (player.isInMass(hero.getHeroId())) {
            return false;
        }
        // 参加皇城血战
        if (player.hasPvpHero(hero.getHeroId())) {
            return false;
        }

        return true;
    }

    public void handleHeroEquip(Hero hero, List<Award> mailAwards, List<Award> addAwards, Player player) {
        int heroId = hero.getHeroId();
        ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
        for (HeroEquip heroEquip : heroEquips) {
            if (heroEquip == null) {
                continue;
            }

            Equip equip = heroEquip.getEquip();
            if (equip == null) {
                continue;
            }

            int freeSlot = equipManager.getFreeSlot(player);
            Award award = new Award(equip.getKeyId(), AwardType.EQUIP, equip.getEquipId(), 1);
            if (freeSlot <= 0) {
                mailAwards.add(award);
                continue;
            } else {
                playerManager.addAward(player, award, Reason.COUNTRY_HERO_ESCAPE);
                addAwards.add(award);
            }
        }

        // 武将下阵
        List<Integer> embattleList = player.getEmbattleList();
        for (int i = 0; i < embattleList.size(); i++) {
            if (embattleList.get(i) == heroId) {
                embattleList.set(i, 0);
                break;
            }
        }
    }

    // holdLv : 逃跑的时候需要判断是否保存等级, true:保存, false:不保存
    public void countryHeroEscape(int heroId, boolean holdLv) {
        Integer country = staticCountryMgr.getCountryByHeroId(heroId);
        if (country == null) {
            LogHelper.CONFIG_LOGGER.info("hero is null exist, heroId = " + heroId);
            return;
        }

        CountryData countryData = getCountry(country);
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("countryData is null, country = " + country);
            return;
        }

        StaticCountryHero staticCountryHero = staticCountryMgr.getCountryHero(heroId);
        if (staticCountryHero == null) {
            LogHelper.CONFIG_LOGGER.info("staticCountryHero is null...");
            return;
        }

        CountryHero countryHero = countryData.getCountryHero(heroId);
        if (countryHero == null) {
            LogHelper.CONFIG_LOGGER.info("countryHero is null, heroId = " + heroId);
            return;
        }
        // 当前地图
        int checkMapId = 0;
        if (countryHero.getOccurRound() % 2 == 1) {
            checkMapId = worldManager.getMapId(countryHero.getPos());
        } else { // 随机开启的地图
            Integer mapId = getRandomMapId();
            if (mapId == 0) {
                LogHelper.CONFIG_LOGGER.info("mapId is 0.");
                checkMapId = worldManager.getMapId(countryHero.getPos());
            } else {
                checkMapId = mapId;
            }
        }

        MapInfo mapInfo = worldManager.getMapInfo(checkMapId);
        if (mapInfo == null) {
            LogHelper.CONFIG_LOGGER.info("mapInfo is null.");
            return;
        }

        Pos monsterPos = mapInfo.randPickPos();
        if (monsterPos.isError() || !mapInfo.isFreePos(monsterPos)) {
            LogHelper.CONFIG_LOGGER.info("randPos is error!");
            return;
        }

        // create country hero
        int heroLv = staticLimitMgr.getNum(137);
        int monsterId = staticCountryHero.getMonsterId();
        int resultLv = countryHero.getHeroLv();
        if (!holdLv) {
            resultLv = heroLv;
        }
        List<Entity> list = new ArrayList<>();
        Monster monster = worldManager.addMonster(monsterPos, monsterId, resultLv, mapInfo, AddMonsterReason.COUNTRY_HERO);
        if (monster == null) {
            LogHelper.CONFIG_LOGGER.info("monster is null!");
            return;
        }
        list.add(monster);

        worldManager.synEntityAddRq(list);
        countryHero.setLordId(0);
        countryHero.setHeroLv(resultLv);
        countryHero.setState(HeroState.OPENED);
        countryHero.setFightTimes(0);
        countryHero.setPos(monsterPos);
    }

    public CountryHero getCountryHeroById(int heroId) {
        Integer country = staticCountryMgr.getCountryByHeroId(heroId);
        if (country == null) {
            return null;
        }

        CountryData countryData = getCountry(country);
        if (countryData == null) {
            return null;
        }

        StaticCountryHero staticCountryHero = staticCountryMgr.getCountryHero(heroId);
        if (staticCountryHero == null) {
            return null;
        }

        return countryData.getCountryHero(heroId);

    }

    // send got country hero mail
    public void sendGotHeroMail(Player player, int heroId) {
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        if (player != null && staticHero != null && staticHero.getHeroName() != null) {
            playerManager.sendNormalMail(player, MailId.GOT_COUNTRY_HERO, staticHero.getHeroName());
        }
    }

    public void sendChatCountryHero(Player player, int heroId) {
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        String params[] = new String[3];
        params[0] = "unkown";
        params[1] = "unkown";
        params[2] = "unkown";

        if (player != null && player.getNick() != null) {
            params[0] = String.valueOf(player.getCountry());
            params[1] = player.getNick();
        } else {
            LogHelper.CONFIG_LOGGER.info("player is null or nick is null.");
        }

        if (staticHero != null && staticHero.getHeroName() != null) {
            params[2] = staticHero.getHeroName();
        } else {
            LogHelper.CONFIG_LOGGER.info("staticHero is null, heroId = " + heroId);
        }

        chatManager.sendWorldChat(ChatId.GOT_COUNTRY_HERO, params);
    }

    // send lose country hero mail
    public void sendLoseHeroMail(Player player, int heroId) {
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        if (player != null && staticHero != null) {
            playerManager.sendNormalMail(player, MailId.LOSE_COUNTRY_HERO, staticHero.getHeroName());
        }
    }

    public void sendChatFoundHero(Player player, int heroId, Pos pos) {
        StaticCountryHero countryHero = staticCountryMgr.getCountryHero(heroId);
        StaticHero staticHero = staticHeroMgr.getStaticHero(heroId);
        String params[] = new String[4];
        params[0] = "null";
        params[1] = "null";
        params[2] = "null";
        params[3] = "null";

        if (countryHero != null) {
            params[0] = String.valueOf(countryHero.getCountry());
        } else {
            LogHelper.CONFIG_LOGGER.info("player is null or nick is null.");
        }

        if (staticHero != null && staticHero.getHeroName() != null) {
            params[1] = staticHero.getHeroName();
        } else {
            LogHelper.CONFIG_LOGGER.info("staticHero is null, heroId = " + heroId);
        }

        if (pos != null) {
            params[2] = String.valueOf(pos.getX());
            params[3] = String.valueOf(pos.getY());
        } else {
            LogHelper.CONFIG_LOGGER.info("pos is null");
        }

        chatManager.sendWorldChat(ChatId.FOUND_COUNTRY_HERO, params);
    }

    public void removeWallHero(Player player, int removeId) {
        Wall wall = player.getWall();
        if (wall == null) {
            return;
        }
        List<Integer> defenceHero = wall.getDefenceHero();
        Iterator<Integer> iterator = defenceHero.iterator();
        while (iterator.hasNext()) {
            Integer heroId = iterator.next();
            if (heroId == removeId) {
                iterator.remove();
                break;
            }

        }
    }

    // 更新国家点兵数量
    @Deprecated
    public void updateSoldierNum(Player player, int soldierNum) {
        if (soldierNum <= 0) {
            return;
        }
        int country = player.getCountry();
        CountryData countryData = countrys.get(country);
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("country data is null, player country is = " + country);
            return;
        }

        countryData.updateSoldierNum(soldierNum);
    }

    public int getSoldierNum(Player player) {
        int country = player.getCountry();
        CountryData countryData = countrys.get(country);
        if (countryData == null) {
            LogHelper.CONFIG_LOGGER.info("country data is null, player country is = " + country);
            return 0;
        }

        return countryData.getSoldierNum();
    }


    //晋升军衔 发滚屏公告
    public void sendChatProMili(Player player, int titleId) {
//        String params[] = {player.getNick(), String.valueOf(titleId)};
//        chatManager.sendWorldChat(ChatId.PRO_MILI, params);

        chatManager.updateChatShow(ChatShowType.PASS_PROMILI, titleId, player);
    }
}
