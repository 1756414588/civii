package com.game.worldmap;


import com.game.constant.MarchState;
import com.game.domain.s.StaticSuperRes;
import com.game.flame.NodeType;
import com.game.pb.CommonPb;
import com.game.util.TimeHelper;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class SuperResource extends Entity {

    /**
     * 生产中
     */
    public static final int STATE_PRODUCED = 1;
    /**
     * 停产中
     */
    public static final int STATE_STOP = 2;
    /**
     * 重置中地图不显示
     */
    public static final int STATE_RESET = 3;


    private int seqId; // 矿点的编号
    private int country;// 阵营(不会改变)
    private int state;// 状态
    private int resId; // 对应配置的id
    private int convertRes;// 已结算矿点
    private int capacity;// 总容量
    private int cityId;// 所属某个城池id
    private long nextTime;// 下一次事件触发时间
    private int resType;// 资源type
    private ConcurrentLinkedDeque<SuperGuard> collectArmy = new ConcurrentLinkedDeque<>(); // 采集部队
    private ConcurrentLinkedDeque<March> helpArmy = new ConcurrentLinkedDeque<>();// 助防部队

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }

    public SuperResource() {

    }

    public SuperResource(int id) {
        this.id = id;
    }


    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getConvertRes() {
        return convertRes;
    }

    public void setConvertRes(int convertRes) {
        this.convertRes = convertRes;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public ConcurrentLinkedDeque<SuperGuard> getCollectArmy() {
        return collectArmy;
    }

    public void setCollectArmy(ConcurrentLinkedDeque<SuperGuard> collectArmy) {
        this.collectArmy = collectArmy;
    }

    public ConcurrentLinkedDeque<March> getHelpArmy() {
        return helpArmy;
    }

    public void setHelpArmy(ConcurrentLinkedDeque<March> helpArmy) {
        this.helpArmy = helpArmy;
    }

    //从重置状态改为生产状态
    public void reset(Pos pos, StaticSuperRes sSm, int cityId) {
        if (this.state == STATE_RESET) { // 重置 -> 生产
            this.state = STATE_PRODUCED;
            this.resId = sSm.getResId();
            this.cityId = cityId;
            this.capacity = sSm.getResourceNum();
            this.convertRes = 0;
            this.nextTime = 0;
            this.collectArmy.clear();
            this.helpArmy.clear();
            setPos(pos);
            setEntityType(EntityType.BIG_RESOURCE);
            this.resType = sSm.getResType();
            this.id = sSm.getResId();
        }
    }

    public SuperResource(Pos pos, StaticSuperRes sSm, int cityId, int country) {
        this.state = STATE_PRODUCED;
        this.resId = sSm.getResId();
        this.cityId = cityId;
        this.capacity = sSm.getResourceNum();
        this.convertRes = 0;
        this.nextTime = 0;
        this.collectArmy.clear();
        this.helpArmy.clear();
        setPos(pos);
        setEntityType(EntityType.BIG_RESOURCE);
        this.country = country;
        this.resType = sSm.getResType();
        this.id = sSm.getResId();
    }


    /**
     * 设置成重置状态
     *
     * @param now
     */
    public void setResetState(long now, StaticSuperRes res) {
        this.state = STATE_RESET;
        this.nextTime = now + res.getResetTime() * 1000;
        setPos(null);
        //this.resId = 0;
        this.convertRes = 0;
        this.capacity = 0;
        this.cityId = 0;
    }

    /**
     * 设置成重置状态
     *
     * @param now
     */
    public void setStopState(long now, StaticSuperRes res) {
        this.state = STATE_STOP;
        this.nextTime = now + res.getStopTime() * 1000;
    }


    /**
     * 恢复生产状态 停产 -> 生产 (只有重新争夺名城才会有)
     *
     * @param now
     */
    public void setStopToProducedState(long now, StaticSuperRes res) {
        if (this.state == STATE_STOP) { // 停产 -> 生产
            for (SuperGuard sg : collectArmy) {
                sg.reProducedState(now);
            }
            this.state = STATE_PRODUCED;
            this.nextTime = 0;
            reCalcAllCollectArmyTime(now, res);
        }
    }

    /**
     * 计算剩余可采集量
     *
     * @param speed
     * @return
     */
    public int calcCollectRemaining(int speed) {
        int remaining = 0;
        if (state == STATE_PRODUCED || state == STATE_STOP) { // 只有停产和重置状态才会有余量
            int allTime = 0;
            for (SuperGuard sg : collectArmy) {
                allTime += sg.calcCollectedTime(System.currentTimeMillis());
            }
            int calcRes = (int) Math.floor((allTime * 1.0 / TimeHelper.HOUR_MS) * speed); // 时间 * 速度
            remaining = capacity - convertRes - calcRes;
            if (remaining <= 0) {
                remaining = 0;
            }
        }
        return remaining;
    }

    /**
     * 加入采集
     *
     * @param march
     * @param res
     * @param maxTime
     */
    public void joinCollect(March march, StaticSuperRes res, long maxTime) {

        long now = System.currentTimeMillis();
        // 部队状态修改
        march.setState(MarchState.Collect);
        // 添加到采集队列中
        collectArmy.add(new SuperGuard(march, this, now, maxTime));
        reCalcAllCollectArmyTime(now, res);


    }

    /**
     * 计算剩余部队的剩余采集时间
     *
     * @param now
     * @param res
     * @return
     */
    public boolean reCalcAllCollectArmyTime(long now, StaticSuperRes res) {
        int collectedTime = 0; // 已采集的时间
        int canCollectTime = 0;// 还可以采集时间
        for (SuperGuard sg : collectArmy) {
            canCollectTime += sg.furtherCollectTime(now);//还可以采集的时间
            collectedTime += sg.calcCollectedTime(now);//已经采集的时间
        }
        int collectedRes = (int) Math.floor((collectedTime * 1.0 / TimeHelper.HOUR_MS) * res.getSpeed());// 已采集的数量
        int canCollectRes = (int) Math.floor((canCollectTime * 1.0 / TimeHelper.HOUR_MS) * res.getSpeed());// 采集将领还未来可以采集的数量
        int remainingCollectRes = capacity - convertRes - collectedRes; // 剩余的数量
        if (canCollectRes > remainingCollectRes) { // 还可以采集 大于 余量 说明不够采集;需要把余下的采集数量进行平分
            int size = collectArmy.size();
            if (size <= 0) {
                return false;
            }
            int canRes = (int) Math.ceil(remainingCollectRes / size); // 平分后的余量
            // 换算成时间
            float speedSec = (float) res.getSpeed() / TimeHelper.HOUR_S;// 每秒的速度
            int durationTime = (int) Math.ceil(canRes / speedSec) * 1000;// 未来还可以采集多少时间
            collectArmy.forEach(sg -> {
                sg.setCanMaxCollectTime(now, durationTime);
                sg.setArmyTime(now, durationTime);
            });// 设置部队时间
            return true;
        } else {// 余量足够采集
            collectArmy.forEach(sg -> {
                sg.setCanMaxCollectTimeEnoughRes();
                sg.setArmyTimeInEnoughRes(now);
            });
        }
        return false;
    }

    public int getResType() {
        return resType;
    }

    public void setResType(int resType) {
        this.resType = resType;
    }

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = CommonPb.WorldEntity.newBuilder();
        builder.setEntityType(entityType);
        builder.setId(getResId());
        builder.setLevel(level);
        if (pos != null) {
            builder.setPos(pos.wrapPb());
        }
        builder.setState(this.state);
        builder.setCountry(this.country);
        return builder;
    }

    public int getHelpArmyCount() {
        return this.getHelpArmy().stream().flatMap(x -> x.getHeroIds().stream()).collect(Collectors.toList()).size();
    }

    public void addConvertRes(int convertRes) {
        this.convertRes += convertRes;
    }

    public CommonPb.SuperMine encode() {
        CommonPb.SuperMine.Builder builder = CommonPb.SuperMine.newBuilder();
        builder.setSeqId(this.seqId);
        builder.setCountry(this.country);
        if (pos != null) {
            builder.setPos(CommonPb.Pos.newBuilder().setX(pos.getX()).setY(pos.getY()).build());
        }
        builder.setState(this.state);
        builder.setConfigId(this.resId);
        builder.setCapacity(this.capacity);
        builder.setCityId(this.cityId);
        builder.setNextTime(this.nextTime);
        collectArmy.forEach(x -> {
            CommonPb.SuperGuard.Builder builder1 = CommonPb.SuperGuard.newBuilder();
            builder1.setMarchId(x.getMarch().getKeyId());
            builder1.setStartTime(x.getStartTime());
            builder1.setMaxCollectTime(x.getMaxCollectTime());
            builder1.setCollectTime(x.getCollectTime());
            builder1.setCanMaxCollectTime(x.getCanMaxCollectTime());
            builder1.setArmyArriveTime(x.getArmyArriveTime());
            builder.addCollectArmy(builder1);
        });
        helpArmy.forEach(helpArmy -> {
            builder.addHelpArmy(helpArmy.getKeyId());
        });
        return builder.build();
    }

    @Override
    public String toString() {
        return "SuperResource{" +
                "seqId=" + seqId +
                ", country=" + country +
                ", state=" + state +
                ", resId=" + resId +
                ", convertRes=" + convertRes +
                ", capacity=" + capacity +
                ", cityId=" + cityId +
                ", nextTime=" + nextTime +
                ", resType=" + resType +
                ", collectArmy=" + collectArmy +
                ", helpArmy=" + helpArmy +
                '}';
    }
}
