package com.game.domain;

import java.util.*;

import com.game.domain.p.CtyTask;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerNation;
import com.game.server.GameServer;
import com.game.util.TimeHelper;

/**
 * 玩家国家相关信息
 */
public class Nation implements Cloneable {
    private long lordId;
    // 已领取荣誉
    private TreeMap<Integer, Integer> gloryLv = new TreeMap<Integer, Integer>();
    // 建设次数
    private int build;
    // 排行
    private int rank;
    // 投票记录
    private int vote;
    // 排行得到的投票数据
    private int voteExtra;
    // 刷新时间
    private int refreshTime;
    // 任务时间
    private long taskTime;
    // 周刷新时间
    private long weekTime;
    // 国家任务
    private Map<Integer, CtyTask> ctyTask = new HashMap<Integer, CtyTask>();
    // 每周建设次数[每周清除一下]
    private int totalBuild;
    // 城战参与总次数
    private int totalPvpWar;
    // 国战总参与次数
    private int totalCtWar;

    public Nation() {

    }

    public void checkGloryLv() {
        if (gloryLv.isEmpty()) {
            for (int i = 1; i <= 3; i++) {
                gloryLv.put(i, 0);
            }
        }
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(int build) {
        this.build = build;
    }

    public void addBuild(int build) {
        this.build += build;
        this.totalBuild += build;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public Map<Integer, CtyTask> getCtyTask() {
        return ctyTask;
    }

    public void setCtyTask(Map<Integer, CtyTask> ctyTask) {
        this.ctyTask = ctyTask;
    }

    public long getTaskTime() {
        return taskTime;
    }

    public void setTaskTime(long taskTime) {
        this.taskTime = taskTime;
    }

    public long getWeekTime() {
        return weekTime;
    }

    public void setWeekTime(long weekTime) {
        this.weekTime = weekTime;
    }

    public int getVoteExtra() {
        return voteExtra;
    }

    public void setVoteExtra(int voteExtra) {
        this.voteExtra = voteExtra;
    }

    public int getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    public void refresh() {
        if (this.refreshTime != GameServer.getInstance().currentDay) {
            gloryLv.clear();
            this.vote = 0;
        }
    }

    public void refreshTask() {
        this.taskTime = 0;
        Iterator<CtyTask> it = ctyTask.values().iterator();
        while (it.hasNext()) {
            CtyTask next = it.next();
            next.setCond(0);
            next.setState(0);
        }
    }

    public TreeMap<Integer, Integer> getGloryLv() {
        return gloryLv;
    }

    public void setGloryLv(TreeMap<Integer, Integer> gloryLv) {
        this.gloryLv = gloryLv;
    }

    public int getTotalBuild() {
        return totalBuild;
    }

    public void setTotalBuild(int totalBuild) {
        this.totalBuild = totalBuild;
    }

    public int getTotalCtWar() {
        return totalCtWar;
    }

    public void setTotalCtWar(int totalCtWar) {
        this.totalCtWar = totalCtWar;
    }

    public int getTotalPvpWar() {
        return totalPvpWar;
    }

    public void setTotalPvpWar(int totalPvpWar) {
        this.totalPvpWar = totalPvpWar;
    }

    public void addPvpWar(int times) {
        totalPvpWar += times;
    }

    public void addCtWar(int times) {
        totalCtWar += times;
    }

    public byte[] serNationData() {
        SerNation.Builder builder = SerNation.newBuilder();
        for (Map.Entry<Integer, Integer> entry : gloryLv.entrySet()) {
            if (entry == null) {
                continue;
            }
            DataPb.GloryItem.Builder gloryItem = DataPb.GloryItem.newBuilder();
            gloryItem.setGloryId(entry.getKey());
            gloryItem.setStatus(entry.getValue());
            builder.addGloryItem(gloryItem);
        }

        builder.setBuild(build);
        builder.setVote(vote);
        builder.setVoteExtra(voteExtra);
        builder.setRefreshTime(refreshTime);
        builder.setTaskTime(taskTime);
        builder.setWeekTime(weekTime);
        for (CtyTask elem : ctyTask.values()) {
            builder.addTasks(elem.writeData());
        }

        builder.setTotalBuild(totalBuild);
        builder.setTotalPvpWar(totalPvpWar);
        builder.setTotalCtWar(totalCtWar);
        return builder.build().toByteArray();

    }

    public void dserNationData(SerNation data) {
        for (DataPb.GloryItem elem : data.getGloryItemList()) {
            if (elem == null) {
                continue;
            }

            gloryLv.put(elem.getGloryId(), elem.getStatus());
        }

        build = data.getBuild();
        vote = data.getVote();
        voteExtra = data.getVoteExtra();
        refreshTime = data.getRefreshTime();
        taskTime = data.getTaskTime();
        weekTime = data.getWeekTime();

        for (DataPb.CountryTaskData countryTaskData : data.getTasksList()) {
            if (countryTaskData == null) {
                continue;
            }

            CtyTask taskData = new CtyTask();
            taskData.readData(countryTaskData);
            ctyTask.put(taskData.getTaskId(), taskData);

        }

        totalBuild = data.getTotalBuild();
        totalPvpWar = data.getTotalPvpWar();
        totalCtWar = data.getTotalCtWar();
    }

    public int getRankValue(int type) {
        if (type == 1) {
            return totalPvpWar;
        } else if (type == 2) {
            return totalCtWar;
        } else if (type == 3) {
            return totalBuild;
        }

        return 0;
    }

    @Override
    public Nation clone() {
        Nation nation = null;
        try {
            nation = (Nation) super.clone();
            TreeMap<Integer, Integer> treeMap = new TreeMap();
            treeMap.putAll(this.gloryLv);
            nation.setGloryLv(treeMap);
            HashMap<Integer, CtyTask> map = new HashMap<>();
            this.ctyTask.forEach((key, value) -> {
                map.put(key, value.clone());
            });
            nation.setCtyTask(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return nation;
    }
}
