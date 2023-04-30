package com.game.domain.p;

import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticActWorldBossMgr;
import com.game.domain.Player;
import com.game.domain.s.StaticLairRank;
import com.game.domain.s.StaticWorldCity;
import com.game.pb.WorldPb;
import com.game.spring.SpringUtil;
import com.google.common.collect.HashBasedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 * @date 2019/12/24 9:38
 * @description
 */
public class WorldTargetTask {
    /**
     * 任务id
     */
    private int taskId;
    /**
     * 三个阵营的详细信息（用于存56789阶段排行榜信息）
     */
    Map<Integer, CountryTaskProcess> process = new ConcurrentHashMap<>();

    /**
     * key1 区域id
     * key2 阵营id
     * 8个区域三个阵营的详细信息（用于存234阶段排行榜信息）
     *
     */
    private HashBasedTable<Integer,Integer,CountryTaskProcess> pross = HashBasedTable.create();
    /**
     * 世界目标完成的数量（杀怪数量，boss剩余的血量,如果为1说明任务完成，占领据点的数量）
     */
    private int num;

    /**
     * 世界boss血量
     */
    private int curHp;

    /**
     * 该任务的开启时间
     */
    private long openTime;

    private Map<Long, WorldHitRank> hitRank = new ConcurrentHashMap<>();

    private List<WorldHitRank> hitRanks = new ArrayList<>();

    /**
     *
     * 1阶段新增一个全服任务：全世界500个指挥中心提升至7级
     */
    private int count;

    /**
     * 更新世界排名
     */
    private  List<CountryTaskProcess> rankList = new ArrayList<>();

    private int complete;//1.完成 2.未完成

    private Logger logger = LoggerFactory.getLogger(getClass());


    public WorldTargetTask() {
        openTime = System.currentTimeMillis();
        this.complete = 2;//未完成
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Map<Integer, CountryTaskProcess> getProcess() {
        return process;
    }

    public void setProcess(Map<Integer, CountryTaskProcess> process) {
        this.process = process;
    }

//    public List<CountryTaskProcess> getCountryTaskProcess() {
//        List<CountryTaskProcess> countryTaskProcesses = new ArrayList<>(process.values());
//        return countryTaskProcesses.stream().sorted(Comparator.comparingInt(CountryTaskProcess::getPoints).reversed()).collect(Collectors.toList());
//    }

    public List<CountryTaskProcess> getCountryTaskProcess() {
        flushRank();
        return rankList;
    }

    public int getRank(int country) {
        List<CountryTaskProcess> countryTaskProcesses = getCountryTaskProcess();
        for (int index = 0; index < countryTaskProcesses.size(); index++) {
            CountryTaskProcess countryTask = countryTaskProcesses.get(index);
            if (countryTask.getCountryId() == country) {
                return index;
            }
        }
        return 0;
    }

    public List<WorldHitRank> getHitRanks() {
       return hitRanks;
    }

    public int getHitRank(long roleId) {
        List<WorldHitRank> hitRanks = getHitRanks();
        for (int index = 0; index < hitRanks.size(); index++) {
            WorldHitRank worldHitRank = hitRanks.get(index);
            if (worldHitRank.getPlayer().roleId == roleId) {
                return index;
            }
        }
        return 0;
    }

    public void rank(){
        List<WorldHitRank> hitranks = new ArrayList<>(hitRank.values());
        this.hitRanks =  hitranks.stream().sorted(Comparator.comparingInt(WorldHitRank::getTotalHit).reversed()).collect(Collectors.toList());
    }

    /**
     * 获取世界boss面板的伤害排行信息
     *
     * @param player
     * @return
     */
    public WorldPb.WorldHitRankInfo.Builder getWorldHitRankInfo(Player player) {
        WorldPb.WorldHitRankInfo.Builder builder = WorldPb.WorldHitRankInfo.newBuilder();
        WorldHitRank worldHitRank = hitRank.get(player.roleId);
        builder.setTodayHit(worldHitRank != null ? worldHitRank.getHit() : 0);
        int cruIndex = getHitRank(player.roleId) + 1;//自己的位置
        builder.setRank(worldHitRank != null ? cruIndex : 0);
        StaticActWorldBossMgr bean = SpringUtil.getBean(StaticActWorldBossMgr.class);
        List<StaticLairRank> lairRankList = bean.getLairRankList(taskId);
        List<WorldHitRank> newLs = new ArrayList<>();
        if (lairRankList != null) {
            for (int i = 0; i < lairRankList.size(); i++) {
                StaticLairRank staticLairRank = lairRankList.get(i);
                List<Integer> rankRand = staticLairRank.getRankRand();
                if (cruIndex >= rankRand.get(0) && cruIndex <= rankRand.get(1) && worldHitRank != null) {
                    worldHitRank.setIndex(cruIndex);
                    worldHitRank.setDesc(staticLairRank.getDesc());
                    newLs.add(worldHitRank);
                }
                for (int j = 0; j < hitRanks.size(); j++) {
                    WorldHitRank rank = hitRanks.get(j);
                    int curIndex = j + 1;
                    if (curIndex >= rankRand.get(0) && curIndex <= rankRand.get(1) && rank.getTotalHit() > 0) {
                        if (rank.getPlayer() != player) {
                            rank.setIndex(curIndex);//排名
                            rank.setDesc(staticLairRank.getDesc());
                            newLs.add(rank);
                        }
                        break;
                    }
                }
            }
        }
        List<WorldHitRank> collect = newLs.stream().distinct().sorted(Comparator.comparingInt(WorldHitRank::getIndex)).collect(Collectors.toList());
        collect.forEach(rank -> {
            WorldPb.WorldRank.Builder builder2 = WorldPb.WorldRank.newBuilder();
            builder2.setCampId(rank.getPlayer().getLord().getCountry());
            builder2.setNickName(rank.getPlayer().getLord().getNick());
            builder2.setHit(rank.getTotalHit());
            builder2.setIndex(rank.getIndex());
            builder.addRankList(builder2);
//            logger.error(rank.toString());
        });
        return builder;
    }


    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getCurHp() {
        return curHp;
    }

    public void setCurHp(int curHp) {
        this.curHp = curHp;
    }

    public long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public Map<Long, WorldHitRank> getHitRank() {
        return hitRank;
    }

    public void setHitRank(Map<Long, WorldHitRank> hitRank) {
        this.hitRank = hitRank;
    }

    public void setHitRanks(List<WorldHitRank> hitRanks) {
        this.hitRanks = hitRanks;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(){
        this.count++;
    }

    //2-4阶段排行
    public void updatePross(int area, int camp, StaticWorldCity city){
        CountryTaskProcess countryTaskProcess = pross.get(area, camp);
        if(countryTaskProcess==null){
            countryTaskProcess=new CountryTaskProcess(area,camp);
            pross.put(area,camp,countryTaskProcess);
        }
        countryTaskProcess.addPoints(city.getCityScore());
    }

    //6-8阶段排行
    public void updatePross( int camp, StaticWorldCity city){
         process.computeIfAbsent(camp,x->new CountryTaskProcess()).addPoints(city.getCityScore());
    }


    /**
     * 跟新某个世界目标的 正营进度
     *
     * @param player
     * @param worldTargetTask
     * 5,9阶段改变任务进程
     */
    public CountryTaskProcess updateCountryProcess(Player player, WorldTargetTask worldTargetTask,int count) {
        CountryTaskProcess countryTaskProcess = worldTargetTask.getProcess().computeIfAbsent(player.getCountry(),x->new CountryTaskProcess(0,player.getCountry()));
        countryTaskProcess.addPoints(count);
        countryTaskProcess.setLastRefreshTime(System.currentTimeMillis());
        return countryTaskProcess;
    }

    /**
     * 刷新世界排行榜
     */
    public void flushRank(){
        if(taskId>= WorldActivityConsts.ACTIVITY_2 && taskId<=WorldActivityConsts.ACTIVITY_4){
            this.rankList = pross.values().stream().sorted(Comparator.comparingInt(CountryTaskProcess::getPoints).reversed().thenComparing(CountryTaskProcess::getArea).thenComparing(CountryTaskProcess::getCountryId)).collect(Collectors.toList());
            return;
        }
        this.rankList = process.values().stream().sorted(Comparator.comparingInt(CountryTaskProcess::getPoints).reversed()).collect(Collectors.toList());
    }

    public HashBasedTable<Integer, Integer, CountryTaskProcess> getPross() {
        return pross;
    }

    public void setPross(HashBasedTable<Integer, Integer, CountryTaskProcess> pross) {
        this.pross = pross;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }
}
