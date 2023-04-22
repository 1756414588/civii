package com.game.flame;

import com.game.domain.Player;
import com.game.domain.p.Buff;
import com.game.domain.p.Item;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.FlameWarPb;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import com.game.worldmap.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlamePlayer {
    private Player player;
    private long roleId;
    private int country;
    private long resource;// 个人资源
    private long firstResource;// 个人参与首杀总资源
    private long kill;// 累计杀敌数
    private Map<Integer, Item> prop = new HashMap<>(); // 个人道具
    private Map<Integer, Buff> buff = new HashMap<>(); // 个人增益
    private Map<Integer, Buff> talent = new HashMap<>(); // 战时天赋
    private long nextEnterTime;// 下次打开地图的时间（如果是战争状态退出则刷新，可进入状态进入不刷新该时间）
    private Map<Integer, Integer> awardCount = new HashMap<>();// 商店兑换次数
    private List<FlameWarPb.FlameRealOptInfo> reports = new ArrayList<>();
    private long rankResource;
    private long styId;// 当前打开得建筑id
    private long nextSyn;// 下次推送得战况时间

    public FlamePlayer() {

    }

    public FlamePlayer(Player player) {
        this.roleId = player.getRoleId();
        this.country = player.getCountry();
        this.player = player;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getResource() {
        return resource;
    }

    public void setResource(long resource) {
        this.resource = resource;
    }

    public Map<Integer, Item> getProp() {
        return prop;
    }

    public void setProp(Map<Integer, Item> prop) {
        this.prop = prop;
    }

    public Map<Integer, Buff> getBuff() {
        return buff;
    }

    public void setBuff(Map<Integer, Buff> buff) {
        this.buff = buff;
    }

    public long getNextEnterTime() {
        return nextEnterTime;
    }

    public void setNextEnterTime(long nextEnterTime) {
        this.nextEnterTime = nextEnterTime;
    }

    public long getFirstResource() {
        return firstResource;
    }

    public void setFirstResource(long firstResource) {
        this.firstResource = firstResource;
    }

    public long getKill() {
        return kill;
    }

    public void setKill(long kill) {
        this.kill = kill;
    }

    public Map<Integer, Integer> getAwardCount() {
        return awardCount;
    }

    public void setAwardCount(Map<Integer, Integer> awardCount) {
        this.awardCount = awardCount;
    }

    public void addResource(long resource) {
        this.resource += resource;
    }

    public void addFirstResource(long resource) {
        this.firstResource += resource;
    }

    public void addKill(long kill) {
        this.kill += kill;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public FlameWarPb.PlayerFlame encode() {
        FlameWarPb.PlayerFlame.Builder builder = FlameWarPb.PlayerFlame.newBuilder();
        builder.setRoleId(this.roleId);
        builder.setResource(this.resource);
        builder.setFirstResource(this.firstResource);
        builder.setKill(this.kill);
        buff.values().forEach(x -> {
            builder.addBuff(x.writeData());
        });
        talent.values().forEach(x -> {
            builder.addTalent(x.writeData());
        });
        prop.values().forEach(x -> {
            builder.addProp(x.wrapPb());
        });
        awardCount.entrySet().forEach(x -> {
            CommonPb.TwoInt.Builder builder1 = CommonPb.TwoInt.newBuilder();
            builder1.setV1(x.getKey());
            builder1.setV2(x.getValue());
            builder.addAwardCount(builder1);
        });
        builder.setCountry(this.country);
        return builder.build();
    }

    public FlamePlayer(FlameWarPb.PlayerFlame builder) {
        this.roleId = builder.getRoleId();
        this.country = builder.getCountry();
        this.firstResource = builder.getFirstResource();
        this.kill = builder.getKill();
        List<DataPb.BuffData> buffList = builder.getBuffList();
        buffList.forEach(x -> {
            Buff buff = new Buff();
            buff.readData(x);
            this.buff.put(x.getBuffId(), buff);
        });
        List<DataPb.BuffData> talentList = builder.getTalentList();

        talentList.forEach(x -> {
            Buff buff = new Buff();
            buff.readData(x);
            this.talent.put(x.getBuffId(), buff);

        });
        List<CommonPb.Prop> propList = builder.getPropList();
        propList.forEach(x -> {
            Item item = new Item(x);
            prop.put(item.getItemId(), item);
        });
        List<CommonPb.TwoInt> awardCountList = builder.getAwardCountList();
        awardCountList.forEach(x -> {
            this.awardCount.put(x.getV1(), x.getV2());
        });
        this.country = builder.getCountry();
    }

    public void addReport(FlameWarPb.FlameRealOptInfo realOptInfo) {
        if (realOptInfo.hasHeroId()) {
            if (reports.size() > 50) {
                reports.remove(0);
            }
            reports.add(realOptInfo);
        }
    }

    public List<FlameWarPb.FlameRealOptInfo> getReports() {
        return reports;
    }

    public void setReports(List<FlameWarPb.FlameRealOptInfo> reports) {
        this.reports = reports;
    }

    public Map<Integer, Buff> getTalent() {
        return talent;
    }

    public void setTalent(Map<Integer, Buff> talent) {
        this.talent = talent;
    }

    public long getStyId() {
        return styId;
    }

    public void setStyId(long styId) {
        this.styId = styId;
    }

    public long getNextSyn() {
        return nextSyn;
    }

    public void setNextSyn(long nextSyn) {
        this.nextSyn = nextSyn;
    }

    public Pos getPos() {
        return player.getPos();
    }

    public long rankResource() {
        StaticFlameWarMgr bean = SpringUtil.getBean(StaticFlameWarMgr.class);
        long staticFlameKill = bean.getStaticFlameKill(this.kill);
        long l = resource + staticFlameKill;
        this.rankResource = l;
        return l;
    }

    public long getRankResource() {
        return rankResource == 0 ? resource : rankResource;
    }

    public void setRankResource(long rankResource) {
        this.rankResource = rankResource;
    }
}
