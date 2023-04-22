package com.game.domain.p;

import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActDial;
import com.game.domain.s.StaticActivity;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.DataPb.Status;
import com.game.pb.DataPb.TowInt;
import com.game.server.GameServer;
import com.game.util.TimeHelper;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */

public class ActRecord implements Cloneable {
    protected int activityId;
    protected int beginTime;
    protected int awardId;

    // 商店类型活动的购买
    protected Map<Integer, ActShopProp> shops = new ConcurrentHashMap<Integer, ActShopProp>();
    // 活动达成条件或者状态记录
    protected Map<Long, Long> status = new ConcurrentHashMap<>();
    // 领取记录
    private Map<Integer, Integer> received = new ConcurrentHashMap<Integer, Integer>();
    // 活动记录值:本次活动过程中,该状态不被重置.重新开启活动,该状态重置
    protected Map<Integer, Integer> record = new ConcurrentHashMap<Integer, Integer>();
    // 活动新开
    private boolean isNew;
    //任务类型活动的记录值
    protected Map<Integer, ActPassPortTask> tasks = new ConcurrentHashMap<Integer, ActPassPortTask>();

    //1转盘类型活动(不包含幸运转盘和至尊转盘)的保底记录值
    protected Map<Integer, Integer> dailGuarantee = new ConcurrentHashMap<Integer, Integer>();

    @Getter
    @Setter
    private List<ActivityRecord> activityRecords = new LinkedList<>();

    // 活动状态
    private int cleanTime;
    private boolean sendMail;
    private boolean close;
    private boolean isShow;
    private int count;//免费领取次数，魅影转盘跨天清0
    private int beforeReceiveDay;  //上次领奖时间

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public Map<Long, Long> getStatus() {
        return status;
    }

    public void setStatus(Map<Long, Long> status) {
        this.status = status;
    }

    public Map<Integer, Integer> getReceived() {
        return received;
    }

    public int getReceived(int sortId) {
        if (!received.containsKey(sortId)) {
            return 0;
        }
        return received.get(sortId);
    }

    public long getStatus(long sortId) {
        if (!status.containsKey(sortId)) {
            return 0L;
        }
        return status.get(sortId);
    }

    public void putState(long sortId, long state) {
        status.put(sortId, state);
    }

    public void setReceived(Map<Integer, Integer> received) {
        this.received = received;
    }

    public int getRecord(int recordId) {
        if (!record.containsKey(recordId)) {
            return 0;
        }
        return record.get(recordId);
    }

    public void putRecord(int recordId, int count) {
        record.put(recordId, count);
    }

    public int addRecord(int recordId, int count) {
        int t = getRecord(recordId);
        t += count;
        record.put(recordId, t);
        return t;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getCleanTime() {
        return cleanTime;
    }

    public void setCleanTime(int cleanTime) {
        this.cleanTime = cleanTime;
    }

    public boolean isSendMail() {
        return sendMail;
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }

    public Map<Integer, ActShopProp> getShops() {
        return shops;
    }

    public void setShops(Map<Integer, ActShopProp> shops) {
        this.shops = shops;
    }

    public Map<Integer, ActPassPortTask> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Integer, ActPassPortTask> tasks) {
        this.tasks = tasks;
    }

    public Map<Integer, Integer> getDailGuarantee() {
        return dailGuarantee;
    }

    public void setDailGuarantee(Map<Integer, Integer> dailGuarantee) {
        this.dailGuarantee = dailGuarantee;
    }

    public Map<Integer, Integer> getRecord() {
        return record;
    }

    public void setRecord(Map<Integer, Integer> record) {
        this.record = record;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public ActRecord() {
    }

    /**
     * 启动初始化
     *
     * @param actRecordPb
     */
    public ActRecord(DataPb.ActRecord actRecordPb) {
        this.activityId = actRecordPb.getActivityId();
        this.awardId = actRecordPb.getAwardId();
        this.beginTime = actRecordPb.getBeginTime();
        this.cleanTime = actRecordPb.getCleanTime();
        this.close = actRecordPb.getClose();
        this.isNew = actRecordPb.getIsnew();
        this.isShow = actRecordPb.getIsShow();
        this.count = actRecordPb.getCount();
        this.beforeReceiveDay = actRecordPb.getBeforeReceiveDay();
        List<Status> statusList = actRecordPb.getStatusList();
        if (statusList != null) {
            for (Status e : statusList) {
                status.put(e.getK(), e.getV());
            }
        }

        List<TowInt> towIntList = actRecordPb.getReceivedList();
        if (towIntList != null) {
            for (TowInt e : towIntList) {
                received.put(e.getK(), e.getV());
            }
        }

        List<DataPb.ActShopProp> propList = actRecordPb.getActShopPropList();
        if (propList != null) {
            for (DataPb.ActShopProp e : propList) {
                shops.put(e.getGrid(), new ActShopProp(e));
            }
        }

        List<TowInt> recordList = actRecordPb.getRecordList();
        if (recordList != null) {
            for (TowInt e : recordList) {
                record.put(e.getK(), e.getV());
            }
        }

        List<DataPb.ActPassPortTaskData> actPassPortTaskList = actRecordPb.getActPassPortTaskDataList();
        if (actPassPortTaskList != null) {
            for (DataPb.ActPassPortTaskData actPassPortTask : actPassPortTaskList) {
                tasks.put(actPassPortTask.getId(), new ActPassPortTask(actPassPortTask));
            }
        }

        List<TowInt> dailGuaranteeList = actRecordPb.getDailGuaranteeList();
        if (dailGuaranteeList != null) {
            for (TowInt e : dailGuaranteeList) {
                dailGuarantee.put(e.getK(), e.getV());
            }
        }
        List<CommonPb.PairIntLong> pairIntLongs = actRecordPb.getPairIntLongList();
        if (pairIntLongs != null) {
            pairIntLongs.forEach(e -> {
                activityRecords.add(ActivityRecord.builder()
                        .key(e.getV1())
                        .buyCount(e.getV2())
                        .expireTime(e.getV3())
                        .build());
            });
        }

    }

    public ActRecord(ActivityBase activityBase, int begin) {
        this.beginTime = begin;
        this.activityId = activityBase.getActivityId();
        this.awardId = activityBase.getAwardId();
        this.received = new HashMap<Integer, Integer>();
        this.isNew = true;
    }

    /**
     * 开启时间不同,则为两次开启活动,重置活动数据
     *
     * @param begin
     */
    public boolean isReset(int begin) {
        if (this.beginTime == begin) {
            return false;
        }
        this.close = false;
        this.isNew = true;
        this.beginTime = begin;
        this.cleanTime = begin;
        this.record.clear();
        this.received.clear();
        this.status.clear();
        this.cleanActivity();
        this.checkExprie();
        return true;
    }

    /**
     * 自动清理活动数据
     */
    public boolean autoDayClean(ActivityBase activityBase) {
        StaticActivity staticActivity = activityBase.getStaticActivity();
        int clean = staticActivity.getClean();
        if (clean == 0) {
            return false;
        } else if (clean >= 1 && clean <= 23) {
            int nowHour = TimeHelper.getTodayHour(clean);
            if (this.cleanTime != nowHour) {
                cleanActivity();
                this.cleanTime = nowHour;
                return true;
            }
        } else if (clean == 24 && activityBase.getActivityId() != ActivityConst.ACT_MONTH_CARD) {//月卡记录特殊处理  计时器清除
            int nowDay = GameServer.getInstance().currentDay;
            if (this.cleanTime != nowDay) {
                cleanActivity();
                if (activityBase.getActivityId() == ActivityConst.DAILY_TRAINRS) {
                    this.record.clear();
                }
                this.cleanTime = nowDay;
                return true;
            }
        }
        return false;
    }

    /**
     * 清理活动中记录的数据
     */
    public void cleanActivity() {
        if (this.activityId != ActivityConst.LUCK_DIAL) {
            this.received.clear();
            this.status.clear();
        }
        this.shops.clear();
        this.tasks.clear();
        this.dailGuarantee.clear();
        this.count = 0;
    }


    /**
     * 检测记录是否过期
     */
    public void checkExprie() {
        if (activityRecords.size() == 0) {
            return;
        }
        Iterator<ActivityRecord> it = activityRecords.iterator();
        while (it.hasNext()) {
            ActivityRecord record = it.next();
            if (System.currentTimeMillis() >= record.getExpireTime() && !received.containsKey(record.getKey())) {
                it.remove();
            }
        }
    }

    public boolean hasNoExprie() {
        if (activityRecords.size() == 0) {
            return false;
        }
        for (ActivityRecord record : activityRecords) {
            if (!received.containsKey(record.getKey())) {
                return true;
            }
        }
        return false;
    }

    public long getExpireTime() {
        return activityRecords.stream().filter(e -> !received.containsKey(e.getKey())).sorted(Comparator.comparingLong(ActivityRecord::getExpireTime).reversed()).findFirst().get().getExpireTime();
    }

    public int getRecordNum(int id) {
        Integer currentNum = record.get(id);
        if (currentNum == null) {
            return 0;
        }

        return currentNum;
    }

    public void updateRecordNum(int id) {
        Integer currentNum = record.get(id);
        if (currentNum == null) {
            record.put(id, 1);
        } else {
            record.put(id, currentNum + 1);
        }
    }

    /**
     * 获取当前转盘物品未抽到的数量
     *
     * @param id
     */
    public int getDailGuaranteeNum(int id) {
        Integer currentNum = dailGuarantee.get(id);
        if (currentNum == null) {
            return 0;
        }

        return currentNum;
    }

    /**
     * 修改当前转盘物品未抽到的数量
     *
     * @param id
     */
    public void updateDailGuaranteeNum(int id) {
        Integer currentNum = dailGuarantee.get(id);
        if (currentNum == null) {
            dailGuarantee.put(id, 1);
        } else {
            dailGuarantee.put(id, currentNum + 1);
        }
    }

    /**
     * 获取保底数量
     *
     * @param id
     */
    public Integer getDailMinGuaranteeNum(StaticActivityMgr staticActivityMgr, int id, int awardId, int type) {
        List<StaticActDial> actDialList = staticActivityMgr.getActDialList(awardId, type);
        if (actDialList == null) {
            return null;
        }
        int total = 0;
        for (StaticActDial e : actDialList) {
            total += e.getWeight();
        }

        if (total == 0) {
            return null;
        }

        for (StaticActDial staticActDial : actDialList) {
            if (staticActDial.getDialId() == id) {
                float pre = staticActDial.getWeight() / (float) total;
                BigDecimal bg = new BigDecimal(pre);
                double f1 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                if (f1 == 0.00) {
                    return null;
                }
                double num = 1 / f1;
                int number = (int) Math.ceil(num);
                if (number == 0) {
                    return null;
                }
                return number;
            }
        }
        return null;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount() {
        this.count++;
    }

    public void updateReceive(int key, int count) {
        this.received.merge(key, count, (a, b) -> a + b);
    }

    public int getRecevie(int key) {
        return this.received.getOrDefault(key, 0);
    }

    public ActivityRecord getActivityRecord(int keyId) {
        for (ActivityRecord r : activityRecords) {
            if (r.getKey() == keyId) {
                return r;
            }
        }
        return null;
    }

    @Override
    public ActRecord clone() {
        ActRecord actRecord = null;
        try {
            actRecord = (ActRecord) super.clone();
            HashMap<Integer, ActShopProp> map = new HashMap<>();
            this.shops.forEach((key, value) -> {
                map.put(key, value);
            });
            actRecord.setShops(map);
            HashMap<Long, Long> map1 = new HashMap<>();
            this.status.forEach((key, value) -> {
                map1.put(key, value);
            });
            actRecord.setStatus(map1);
            HashMap<Integer, Integer> map2 = new HashMap<>();
            map2.putAll(this.received);
            actRecord.setReceived(map2);
            HashMap<Integer, Integer> map3 = new HashMap<>();
            map3.putAll(this.record);
            actRecord.setRecord(map3);
            HashMap<Integer, ActPassPortTask> map4 = new HashMap<>();
            this.tasks.forEach((key, value) -> {
                map4.put(key, value.clone());
            });
            actRecord.setTasks(map4);
            HashMap<Integer, Integer> map5 = new HashMap<>();
            map5.putAll(this.dailGuarantee);
            actRecord.setDailGuarantee(map5);
            List<ActivityRecord> list = new LinkedList<>();
            Iterator<ActivityRecord> iterator = activityRecords.iterator();
            while (iterator.hasNext()){
                ActivityRecord next = iterator.next();
                list.add(next.clone());
            }
            actRecord.setActivityRecords(list);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return actRecord;
    }

    /**
     * @Description 是否是当天首次领取奖励
     * @Param []
     * @Return boolean
     * @Date 2021/7/2 18:25
     **/
    public boolean isFirstReceive() {
        int currentDay = GameServer.getInstance().currentDay;
        if (beforeReceiveDay == currentDay) {
            return false;
        }
        beforeReceiveDay = currentDay;
        return true;
    }

    public int getBeforeReceiveDay() {
        return beforeReceiveDay;
    }
}
