package com.game.worldmap;

import com.game.constant.CityType;
import com.game.constant.MarchState;
import com.game.constant.WarType;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.MapDistance;
import com.game.domain.Player;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldMap;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.*;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// 地图信息, 都需要存盘
@Getter
@Setter
public class MapInfo extends ReentrantLock {

    private int mapId;

    // 玩家城池信息
    private Map<Pos, PlayerCity> playerCityMap = new ConcurrentHashMap<Pos, PlayerCity>();
    //伏击叛军信息
    private ConcurrentHashMap<Long, WarInfo> rebelWarMap = new ConcurrentHashMap<>();
    // 战斗集合
    private ConcurrentHashMap<Long, IWar> warMap = new ConcurrentHashMap<Long, IWar>();
    // 行军定时器, 行军，击杀流寇、采集、城战、国战
    private ConcurrentHashMap<Integer, March> marches = new ConcurrentHashMap<>();

    // 流寇信息
    private Map<Pos, Monster> monsterMap = new ConcurrentHashMap<>();
    // 叛军信息
    private Map<Pos, RebelMonster> rebelMap = new ConcurrentHashMap<>();
    // 资源信息
    private Map<Pos, Resource> resourceMap = new ConcurrentHashMap<Pos, Resource>();
    // 地图的城池信息
    private Map<Pos, CityInfo> cityInfos = new ConcurrentHashMap<Pos, CityInfo>();
    // 已占用坐标
    protected ConcurrentHashMap<Pos, Entity> posTake = new ConcurrentHashMap<Pos, Entity>();
    // 未占用坐标
    private List<Pos> posFree = new ArrayList<>();
    // 叛军数量容器
    private ConcurrentHashMap<Integer, Integer> monsterNumMap = new ConcurrentHashMap<Integer, Integer>();
    // 最后一次存盘时间
    private long lastSaveTime;
    // 世界地图的maxkey
    private AtomicLong maxKey = new AtomicLong();
    // 导人数量
    private ConcurrentHashMap<Integer, Integer> mapPlayerNum = new ConcurrentHashMap<Integer, Integer>();
    // key: country, value : cityIds
    private Map<Integer, HashSet<Integer>> cityIdRecord = new TreeMap<Integer, HashSet<Integer>>();
    /**
     * 城市首杀  city type
     */
    private Map<Integer, CityFirstBloodInfo> cityFirstBlood = new ConcurrentHashMap<>();
    //巨型虫族活动
    @Getter
    @Setter
    private Map<Pos, BigMonster> bigMonsterMap = new ConcurrentHashMap<>();
    /**
     * 待复活的虫子
     */
    @Getter
    @Setter
    private Queue<BigMonster> deathMonsterMap = new ConcurrentLinkedDeque<>();

    @Getter
    @Setter
    private Map<Integer, List<SuperResource>> superResMap = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private Map<Pos, SuperResource> superPosResMap = new ConcurrentHashMap<>();

    public MapInfo() {
    }

    // 初始化坐标
    public void initPos(StaticWorldMap staticWorldMap, HashBasedTable<Integer, Integer, Pos> illegalPos) {
        int x1 = staticWorldMap.getX1();
        int x2 = staticWorldMap.getX2();
        int y1 = staticWorldMap.getY1();
        int y2 = staticWorldMap.getY2();
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                if (illegalPos.contains(x, y)) {//已被战力
                    continue;
                }
                posFree.add(new Pos(x, y));
            }
        }
        Collections.shuffle(posFree);
    }

    public IWar getWarInfoByWarId(long warId) {
        return warMap.get(warId);
    }

    public void addWar(IWar war) {
//        LogHelper.GAME_LOGGER.info("【战斗.添加】 战斗Key:{} 类型:{} 状态:{} 结束世间:{}", war.getWarId(), war.getWarType(), war.getState(), DateHelper.getDate(war.getEndTime()));
        warMap.put(war.getWarId(), war);
    }

    public IWar getWar(long warId) {
        return warMap.get(warId);
    }

    public void removeWar(IWar war) {
        warMap.remove(war.getWarId());
    }

    public boolean isContain(long warId) {
        return warMap.containsKey(warId);
    }

    public List<IWar> getWarList(Predicate<IWar> predicate) {
        return warMap.values().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 拿到某个id  某个等级的怪 离自己城市最近的
     *
     * @param level
     * @return
     */
    public Entity getEntitys(int level, Pos pos) {
        Iterator<Monster> iterator = monsterMap.values().iterator();
        MapDistance mapDistance = new MapDistance(pos);
        while (iterator.hasNext()) {
            Entity value = iterator.next();
            if (value.getLevel() == level) {
                mapDistance.findNearestEntity(value);
            }
        }
        return mapDistance.getNearest();
    }

    public Map<Integer, CityFirstBloodInfo> getCityFirstBlood() {
        return cityFirstBlood;
    }

    public void setFirstBlood(Integer cityType, Player attacker, List<Player> attackerList, int mapId) {
        if (attacker == null) {
            return;
        }
        CityFirstBloodInfo cityFirstBloodInfo = new CityFirstBloodInfo(cityType, attacker, attackerList, mapId);
        if (cityType == CityType.WORLD_FORTRESS) {
            if (cityFirstBlood.containsKey(cityType)) {
                cityFirstBlood.remove(cityType);
            }
            cityFirstBlood.put(cityType, cityFirstBloodInfo);
        } else if (!cityFirstBlood.containsKey(cityType)) {
            cityFirstBlood.put(cityType, cityFirstBloodInfo);
        }
    }

    public MapInfo(int mapId) {
        this.mapId = mapId;
    }

    public Map<Pos, PlayerCity> getPlayerCityMap() {
        return playerCityMap;
    }

    public Map<Pos, Monster> getMonsterMap() {
        return monsterMap;
    }

    public Map<Pos, Resource> getResourceMap() {
        return resourceMap;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public Map<Pos, CityInfo> getCityInfos() {
        return cityInfos;
    }

    public boolean addPos(Pos pos, Entity entity) {
        Entity result = posTake.putIfAbsent(pos, entity);
        if (result == null) {// 添加成功
            return true;
        }
        return false;
    }

    public void setCityPos(Pos pos, Entity entity) {
        posTake.put(pos, entity);
    }

    public void removeCityPos(Pos pos) {
        posFree.remove(pos);
    }

    // 被击飞、野怪清理、资源采集完
    public void removePos(Pos pos) {
        lock();
        try {
            posFree.add(pos);
            posTake.remove(pos);
        } catch (Exception e) {

        } finally {
            unlock();
        }
    }

    // 随机获得坐标
    // 随机获得坐标
    public Pos randPickPos() {
        lock();
        try {
            if (posFree.isEmpty()) {
                return new Pos();
            }
            return posFree.remove(0);
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return new Pos();
    }


    /**
     * @Description 随机指定范围的格子
     * @Param [rise, end]
     * @Return com.game.worldmap.Pos
     * @Date 2021/6/1 21:28
     **/
    public Pos randomAppointPos(int rise, int end) {
        lock();
        try {
            StaticWorldMap staticWorldMap = SpringUtil.getBean(StaticWorldMgr.class).getStaticWorldMap(mapId);
            if (staticWorldMap == null || posFree.isEmpty()) {
                return new Pos();
            }

            int x1 = staticWorldMap.getX1() + rise;
            int x2 = staticWorldMap.getX1() + end;
            int x3 = staticWorldMap.getX2() - rise;
            int x4 = staticWorldMap.getX2() - end;

            int y1 = staticWorldMap.getY1() + rise;
            int y2 = staticWorldMap.getY1() + end;
            int y3 = staticWorldMap.getY2() - rise;
            int y4 = staticWorldMap.getY2() - end;
            List<Pos> keys = new ArrayList<Pos>();
            Iterator<Pos> it = posFree.iterator();
            while (it.hasNext()) {
                Pos pos = it.next();
                //Pos pos = entry.getKey();
                int x = pos.getX();
                int y = pos.getY();
                if (x >= x1 && x <= x3 && y >= y1 && y <= y3) {
                    if (x <= x2 || x >= x4) {
                        keys.add(pos);
                        continue;
                    }
                    if (y <= y2 || y > y4) {
                        keys.add(pos);
                        continue;
                    }
                }
            }
            if (keys.isEmpty()) {
                return new Pos();
            }
            Pos randomPos = keys.get(RandomUtil.getRandomNumber(keys.size()));
            posFree.remove(randomPos);
            return randomPos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return new Pos();
    }

    // 随机获得坐标
    public Pos randResPickPos() {
        lock();
        try {
            if (posFree.isEmpty()) {
                return new Pos();
            }
            List<Pos> resFreePos = isResFreePos();
            if (resFreePos.isEmpty()) {
                return randPickPos();
            }
            Pos randomPos = resFreePos.get(RandomUtil.getRandomNumber(resFreePos.size()));
            posFree.remove(randomPos);
            return randomPos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        // 防御性代码
        return randPickPos();
    }


    public int getMonsterNum(int monsterLv) {
        Integer num = monsterNumMap.get(monsterLv);
        if (num == null) {
            return 0;
        }
        return num;
    }

    public void removeMonsterNum(int level) {
        Integer num = monsterNumMap.get(level);
        if (num != null) {
            monsterNumMap.put(level, num - 1 >= 0 ? num - 1 : 0);
        }
    }

    public void updateMonsterNum(int monsterLv) {
        int monsterNum = getMonsterNum(monsterLv);
        monsterNumMap.put(monsterLv, monsterNum + 1);
    }

    public ConcurrentLinkedDeque<March> getMarches() {
        return new ConcurrentLinkedDeque<>(marches.values());
    }

    public void addMarch(March march) {
//		LogHelper.MESSAGE_LOGGER.info("【地图.行军.添加】 MarchType:{} state:{} 结束时间：{}", march.getMarchType(), march.getState(), DateHelper.getDate(march.getEndTime()));
        marches.put(march.getKeyId(), march);
    }

    public void removeMarch(March march) {
        if (march == null) {
            return;
        }
        marches.remove(march.getKeyId());
    }

    public ConcurrentHashMap<Integer, March> getMarchMap() {
        return marches;
    }


    public Entity getEntity(Pos pos) {
        if (posTake.containsKey(pos)) {
            return posTake.get(pos);
        }
        return null;
    }

    public PlayerCity getPlayerCity(Pos pos) {
        if (playerCityMap.containsKey(pos)) {
            return playerCityMap.get(pos);
        }
        return null;
    }

    // 检测一个点上是否有行军
    public March getMarch(Pos pos) {
        for (March march : marches.values()) {
            if (march.getEndPos().isEqual(pos) && march.getState() == MarchState.Collect) {
                return march;
            }
        }

        return null;
    }

    public March getMarch(int keyId) {
        if (marches.containsKey(keyId)) {
            return marches.get(keyId);
        }
        return null;
    }

    public long getLastSaveTime() {
        return lastSaveTime;
    }

    public void setLastSaveTime(long lastSaveTime) {
        this.lastSaveTime = lastSaveTime;
    }

    public WorldMap createWorldMap() {
        if (maxKey == null) {
            return null;
        }
        WorldMap worldMap = new WorldMap();
        worldMap.setMaxKey(maxKey.get());
        worldMap.setMapId(mapId);
        worldMap.setLastSaveTime(lastSaveTime);
        SerMapInfo.Builder builder = SerMapInfo.newBuilder();
        Iterator<Monster> iterator = monsterMap.values().iterator();
        while (iterator.hasNext()) {
            Monster monster = iterator.next();
            builder.addMonster(monster.writeData());
        }

        this.marches.values().forEach(march -> {
            builder.addMarchData(march.writeMarch());
        });
        for (Resource resource : resourceMap.values()) {
            if (resource != null) {
                builder.addResource(resource.writeData());
            }
        }

        for (CityFirstBloodInfo info : cityFirstBlood.values()) {
            if (info != null) {
                builder.addFirstBlood(info.writeData());
            }
        }
        Iterator<BigMonster> bigMonsterIterator = bigMonsterMap.values().iterator();
        while (bigMonsterIterator.hasNext()) {
            BigMonster monster = bigMonsterIterator.next();
            if (monster.getTeam() == null) {
                continue;
            }
            builder.addMonster(monster.writeData());
        }
        Iterator<BigMonster> deathIterator = deathMonsterMap.iterator();
        while (deathIterator.hasNext()) {
            BigMonster e = deathIterator.next();
            builder.addMonster(e.writeData());
        }

        if (getCityIdRecord().isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                getCityIdRecord().put(i, new HashSet<Integer>());
            }
        }

        for (Map.Entry<Integer, HashSet<Integer>> record : getCityIdRecord().entrySet()) {
            Integer country = record.getKey();
            if (country == null) {
                continue;
            }

            DataPb.CtCityIdRecord.Builder crBuilder = DataPb.CtCityIdRecord.newBuilder();
            crBuilder.setCountry(country);
            HashSet<Integer> cityIds = record.getValue();
            if (cityIds != null && !cityIds.isEmpty()) {
                crBuilder.addAllCityId(cityIds);
            }
            builder.addCtCityIdRecord(crBuilder);
        }

        //地图大型矿点入库
        this.getSuperResMap().values().forEach(x -> {
            x.forEach(mine -> {
                builder.addSuperMine(mine.encode());
            });
        });

        SerQuickWar.Builder quickWar = SerQuickWar.newBuilder();
        SerFarWar.Builder farWar = SerFarWar.newBuilder();
        SerCountryWar.Builder countryWar = SerCountryWar.newBuilder();
        SerZergWar.Builder zergWar = SerZergWar.newBuilder();
        SerQuickWar.Builder bigmonsterWar = SerQuickWar.newBuilder();
        for (IWar war : warMap.values()) {
            DataPb.WarData.Builder warPb = war.writeData();
            if (warPb.getWarType() == WarType.ATTACK_QUICK) {
                quickWar.addWarData(warPb.build());
            } else if (warPb.getWarType() == WarType.ATTACK_FAR || warPb.getWarType() == WarType.Attack_WARFARE) {
                farWar.addWarData(warPb.build());
            } else if (warPb.getWarType() == WarType.ATTACK_COUNTRY) {
                countryWar.addWarData(warPb.build());
            } else if (warPb.getWarType() == WarType.ATTACK_ZERG || warPb.getWarType() == WarType.DEFEND_ZERG) {
                zergWar.addWarData(warPb.build());
            } else if (warPb.getWarType() == WarType.BIGMONSTER_WAR) {
                bigmonsterWar.addWarData(warPb.build());
            }
        }

        if (quickWar.getWarDataCount() > 0) {
            worldMap.setQuickWarData(quickWar.build().toByteArray());
        }
        if (farWar.getWarDataCount() > 0) {
            worldMap.setFarWarData(farWar.build().toByteArray());
        }
        if (countryWar.getWarDataCount() > 0) {
            worldMap.setCountryWarData(countryWar.build().toByteArray());
        }
        if (zergWar.getWarDataCount() > 0) {
            worldMap.setZergWarData(zergWar.build().toByteArray());
        }
        if (bigmonsterWar.getWarDataCount() > 0) {
            worldMap.setBigMonsterWarData(bigmonsterWar.build().toByteArray());
        }

        worldMap.setMapData(builder.build().toByteArray());
        return worldMap;
    }

    public long maxKey() {
        return maxKey.addAndGet(1);
    }

    public long getMaxKey() {
        return maxKey.get();
    }

    public void setMaxKey(long maxKey) {
        this.maxKey.set(maxKey);
    }

    public boolean isFreePos(Pos pos) {
        lock();
        try {
            Entity entity = getEntity(pos);
            if (entity == null && posFree.contains(pos)) {
                posFree.remove(pos);
                return true;
            }

        } catch (Exception e) {

        } finally {
            unlock();
        }

        return false;
    }


    public List<Pos> isResFreePos() {
        StaticLimitMgr limit = SpringUtil.getBean(StaticLimitMgr.class);
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        List<Pos> list = new ArrayList<>();
        List<Integer> addtion12 = limit.getAddtion(293);
        List<Integer> addtion11 = limit.getAddtion(294);
        List<Integer> addtion10 = limit.getAddtion(295);
        List<Integer> addtion9 = limit.getAddtion(296);
        if (addtion9.size() != 4 || addtion10.size() != 4 || addtion11.size() != 4 || addtion12.size() != 4) {
            return list;
        }
        if (mapId == 9) {
            x1 = addtion9.get(0);
            x2 = addtion9.get(1);
            y1 = addtion9.get(2);
            y2 = addtion9.get(3);
        } else if (mapId == 10) {
            x1 = addtion10.get(0);
            x2 = addtion10.get(1);
            y1 = addtion10.get(2);
            y2 = addtion10.get(3);
        } else if (mapId == 11) {
            x1 = addtion11.get(0);
            x2 = addtion11.get(1);
            y1 = addtion11.get(2);
            y2 = addtion11.get(3);
        } else if (mapId == 12) {
            x1 = addtion12.get(0);
            x2 = addtion12.get(1);
            y1 = addtion12.get(2);
            y2 = addtion12.get(3);
        }
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                Pos pos = new Pos(x, y);
                if (posFree.contains(pos)) {
                    list.add(pos);
                }
            }
        }
        return list;


        //if (pos.getX() >= x1 && pos.getX() <= x2 && pos.getY() >= y1 && pos.getY() <= y2) {
        //    return true;
        //} else {
        //    return false;
        //}
    }


    public Entity getEntity(int x, int y) {
        return posTake.get(new Pos(x, y));
    }

    public Resource getResource(Pos pos) {
        return resourceMap.get(pos);
    }

    public void clearPos(Pos pos) {
        Entity common = posTake.get(pos);
        if (common != null) {
            removePos(pos);
            if (common instanceof Monster) {
                monsterMap.remove(pos);
                removeMonsterNum(common.getLevel());
            } else if (common instanceof RebelMonster) {
                rebelMap.remove(pos);
                removeMonsterNum(common.getLevel());
            } else if (common instanceof BigMonster) {
                bigMonsterMap.remove(pos);
            } else if (common instanceof Resource) {
                resourceMap.remove(pos);
            } else if (common instanceof SuperResource) {
                superPosResMap.remove(pos);
            }
            CommonPb.Pos posPB = pos.wrapPb();
            PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
            playerManager.getOnlinePlayer().forEach(player -> {
                playerManager.synEntityToPlayer(player, null, posPB);
            });
        } else {
            LogHelper.CONFIG_LOGGER.info("clear pos:{} error", pos);
        }
    }


    public ConcurrentHashMap<Integer, Integer> getMapPlayerNum() {
        return mapPlayerNum;
    }

    public void setMapPlayerNum(ConcurrentHashMap<Integer, Integer> mapPlayerNum) {
        this.mapPlayerNum = mapPlayerNum;
    }

    public CountryCityWarInfo getCountryCityWar(int cityId, int country) {
        Optional<IWar> optional = warMap.values().stream().filter(e -> {
            if (e instanceof CountryCityWarInfo) {
                if (cityId == ((CountryCityWarInfo) e).getCityId() && e.getAttacker().getCountry() == country) {
                    return true;
                }
            }
            return false;
        }).findAny();
        if (optional.isPresent()) {
            return (CountryCityWarInfo) optional.get();
        }
        return null;
    }

    public List<IWar> getCountryCityWar(int cityId) {
        return warMap.values().stream().filter(e -> {
            if (e instanceof CountryCityWarInfo) {
                if (cityId == 0 || cityId == ((CountryCityWarInfo) e).getCityId()) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public synchronized void updatePlayerNum(int country, int change) {
        Integer num = mapPlayerNum.get(country);
        if (num == null) {
            num = 0;
        }

        mapPlayerNum.put(country, num + change);
    }

    public Map<Integer, HashSet<Integer>> getCityIdRecord() {
        return cityIdRecord;
    }

    public void setCityIdRecord(Map<Integer, HashSet<Integer>> cityIdRecord) {
        this.cityIdRecord = cityIdRecord;
    }

    public Map<Pos, RebelMonster> getRebelMap() {
        return rebelMap;
    }


    public ConcurrentHashMap<Long, WarInfo> getRebelWarMap() {
        return rebelWarMap;
    }

    public List<WarInfo> getRebelWarMapByCountry(int country) {
        return Lists.newArrayList(rebelWarMap.values());
    }

    public void addBroodEntity(Pos pos, Entity entity) {
        if (posTake.containsKey(pos)) {
            posTake.put(pos, entity);
        }
        if (cityInfos.containsKey(pos) && entity instanceof CityInfo) {
            cityInfos.put(pos, (CityInfo) entity);
        }
    }

    /**
     * @Description 随机城市范围内的坐标
     * @Param [cityID]
     * @Return com.game.worldmap.Pos
     * @Date 2021/7/27 11:04
     **/
    //public Pos randomCityRangePos(StaticWorldCity city) {
    //    lock();
    //    try {
    //        if (this.mapId != MapId.CENTER_MAP_ID) {
    //            return null;
    //        }
    //        if (city == null || city.getMapId() != this.mapId) {
    //            return null;
    //        }
    //        int x1 = city.getRangex1();
    //        int x2 = city.getRangex2();
    //        int y1 = city.getRangey1();
    //        int y2 = city.getRangey2();
    //
    //        // 防御性代码
    //        if (posFree.isEmpty()) {
    //            return null;
    //        }
    //        List<Pos> freeSet = new ArrayList<>();
    //        Iterator<Pos> it = posFree.iterator();
    //        while (it.hasNext()) {
    //            Pos pos = it.next();
    //            int x = pos.getX();
    //            int y = pos.getY();
    //            if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
    //                freeSet.add(pos);
    //            }
    //        }
    //        // 随机算法
    //        if (freeSet.isEmpty()) {
    //            return null;
    //        }
    //        Pos pos = freeSet.get(RandomUtil.getRandomNumber(freeSet.size()));
    //        posFree.remove(pos);
    //        return pos;
    //
    //
    //
    //
    //
    //    } catch (Exception e) {
    //
    //    } finally {
    //        unlock();
    //    }
    //    return new Pos();
    //}
    public March getMarchByPos(Pos pos) {
        for (March march : marches.values()) {
            if (march.getEndPos().isEqual(pos)) {
                return march;
            }
        }
        return null;
    }


    public List<Pos> getPos(Player player, int configNum, int num) {
        lock();
        try {
            List<Pos> pos = new ArrayList<Pos>();
            int posx = player.getPosX();
            int posy = player.getPosY();

            for (int i = posx - configNum; i <= posx + configNum; i++) {
                if (i <= 0) {
                    continue;
                }
                for (int j = posy - configNum; j <= posy + configNum; j++) {
                    if (j <= 0) {
                        continue;
                    }
                    if (i == posx && j == posy) {
                        continue;
                    }
                    Pos elem = new Pos(i, j);
                    if (posFree.contains(elem)) {
                        pos.add(elem);
                    }
                }
            }
            if (pos.size() > num) {
                pos = pos.subList(0, num);
            }
            posFree.removeAll(pos);
            return pos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return new ArrayList<>();
    }


    public Pos randPos(Player player, int configNum) {
        lock();
        Pos pos = new Pos();
        try {
            List<Pos> aroundPos = new ArrayList<Pos>();
            int x = player.getPosX();
            int y = player.getPosY();
            // 找出合法的坐标
            for (int i = x - configNum; i <= x + configNum; i++) {
                if (i < 0) {
                    continue;
                }
                for (int j = y - configNum; j <= y + configNum; j++) {
                    if (j < 0) {
                        continue;
                    }
                    if (i == x && j == y) {
                        continue;
                    }
                    Pos elem = new Pos(i, j);
                    if (posFree.contains(elem)) {
                        aroundPos.add(elem);
                    }
                }
            }
            // 随机一个坐标
            int posSize = aroundPos.size();
            if (posSize <= 0) {
                return pos;
            }
            pos = aroundPos.get(RandomUtil.getRandomNumber(posSize));
            posFree.remove(pos);
            return pos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return pos;
    }

    public Pos getRandomCityPos(StaticWorldCity worldCity) {
        lock();
        try {
            int rangeX1 = worldCity.getRangex1();
            int rangeX2 = worldCity.getRangex2();
            int rangeY1 = worldCity.getRangey1();
            int rangeY2 = worldCity.getRangey2();
            List<Pos> list = new ArrayList<>();
            for (int x = rangeX1; x <= rangeX2; x++) {
                for (int y = rangeY1; y <= rangeY2; y++) {
                    Pos pos = new Pos(x, y);
                    if (posFree.contains(pos)) {
                        list.add(pos);
                    }
                }
            }
            if (list.isEmpty()) {
                return randPickPos();
            }
            Pos pos = list.get(RandomUtil.getRandomNumber(list.size()));
            posFree.remove(pos);
            return pos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return randPickPos();
    }

    /**
     * 跑引导刷怪
     *
     * @param player
     * @return
     */
    public Pos getFlushTaskMosterPos(Player player) {
        lock();
        try {
            List<Pos> list = new ArrayList<>();
            int posx = player.getPosX();
            int posy = player.getPosY();
            MapDistance mapDistance = new MapDistance(player.getPos());
            for (int i = posx - 1; i <= posx + 1; i++) {
                for (int j = posy - 1; j <= posy + 1; j++) {
                    if (i == posx && j == posy) {
                        continue;
                    }
                    Pos pos = new Pos(i, j);
                    int dis = mapDistance.calcDistance(pos);//距离
                    if (dis > 1) {
                        continue;
                    }
                    if (posFree.contains(pos)) {
                        posFree.remove(pos);
                        System.err.println("第一波" + pos.toPosStr());
                        return pos;
                    }
                    list.add(pos);
                }
            }
            if (!list.isEmpty()) {
                Iterator<Pos> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Pos next = iterator.next();
                    Entity entity = getEntity(next);
                    if (entity instanceof Monster && getMarchByPos(next) == null) {
                        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
                        clearPos(next);
//                        worldManager.synEntityRemove(entity, getMapId(), next);
                        posFree.remove(next);
                        return next;
                    }
                }
            }
            mapDistance = new MapDistance(player.getPos());
            for (int i = posx - 2; i <= posx + 2; i++) {
                for (int j = posy - 2; j <= posy + 2; j++) {
                    if (i == posx && j == posy) {
                        continue;
                    }
                    Pos pos = new Pos(i, j);
                    if (posFree.contains(pos)) {
                        mapDistance.findNearestPos(pos);
                    }
                }
            }
            Pos nearestPos = mapDistance.getNearestPos();
            posFree.remove(nearestPos);
            return nearestPos;
        } catch (Exception e) {

        } finally {
            unlock();
        }
        return randPickPos();
    }

}
