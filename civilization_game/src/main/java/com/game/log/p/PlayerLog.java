package com.game.log.p;

import com.game.constant.SoldierType;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.p.CtyGovern;
import com.game.domain.p.Hero;
import com.game.manager.CountryManager;
import com.game.manager.ServerManager;
import com.game.manager.SoldierManager;
import com.game.manager.WorldManager;

import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @date 2020/1/14 10:09
 * @description
 */
public class PlayerLog {
    /**
     * 玩家accountKey
     */
    private long accountKey;

    /**
     * 玩家id
     */
    private long lordId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 区服id
     */
    private int serverId;
    /**
     * 等级
     */
    private int level;
    /**
     * 经验
     */
    private int exp;
    /**
     * vip等级
     */
    private int vipLevel;

    /**
     * vip经验
     */
    private int vipExp;

    /**
     * 赠送的vip经验
     */
    private int freeVipExp;
    /**
     * 体力
     */
    private int energy;
    /**
     * 战力
     */
    private int power;
    /**
     * 所在地图id
     */
    private int mapId;

    /**
     * 正营，国家
     */
    private int country;

    /**
     * 军衔
     */
    private int title;

    /**
     * 威望
     */
    private long honor;
    /**
     * 阵营官职
     */
    private int govern;

    /**
     * 系统砖石
     */
    private int systemGold;

    /**
     * 充值砖石
     */
    private int rechargeGold;

    /**
     * 消耗砖石
     */
    private int costGold;

    /**
     * 剩余的总砖石
     */
    private int totalGold;

    /**
     * 金币
     */
    private long iron;
    /**
     * 钢铁
     */
    private long copper;
    /**
     * 食物
     */
    private long oil;

    /**
     * 晶体
     */
    private long stone;

    /**
     * 步兵
     */
    private int rocketType;

    /**
     * 战车
     */
    private int tankType;

    /**
     * 虫兵
     */
    private int warCar;

    /**
     * 创建时间
     */
    private Date registerTime;
    /**
     * 注册ip
     */
    private String registerIp;

    /**
     * 最后一次登录时间
     */
    private Date LastLoginTime;

    /**
     * 渠道标识
     */
    private int channel;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 购买礼包
     */
    private List<Integer> giftBag;

    /**
     * 总充值rmb
     */
    private Integer totalRecharge;
    private List<Hero> heros;

    public PlayerLog() {
    }

    public PlayerLog(Player player) {
        accountKey = player.account.getAccountKey();
        lordId = player.getLord().getLordId();
        name = player.getLord().getNick();
        ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
        serverId = serverManager.getServer().getServerId();
        level = player.getLevel();
        vipLevel = player.getVip();
        exp = player.getExp();
        energy = player.getEnergy();
        power = player.getMaxScore();
        WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
        mapId = worldManager.getMapId(player);
        country = player.getCountry();
        title = player.getTitle();
        honor = player.getHonor();
        CountryManager countryManager = SpringUtil.getBean(CountryManager.class);
        CtyGovern ctyGovern = countryManager.getGovern(player);
        govern = ctyGovern == null ? 0 : ctyGovern.getGovernId();
        // TODO 暂时没有这个数值
        systemGold = player.getLord().getSystemGold();
        // TODO 暂时没有这个数值
        rechargeGold = player.getLord().getRechargeGold();
        // TODO 暂时没有这个数值
        costGold = player.getLord().getGoldCost();
        totalGold = player.getGold();
        iron = player.getIron();
        copper = player.getCopper();
        oil = player.getOil();
        stone = player.getStone();
        SoldierManager soldierManager = SpringUtil.getBean(SoldierManager.class);
        rocketType = soldierManager.getSoldierNum(player, SoldierType.ROCKET_TYPE);
        tankType = soldierManager.getSoldierNum(player, SoldierType.TANK_TYPE);
        warCar = soldierManager.getSoldierNum(player, SoldierType.WAR_CAR);
        Account account = player.account;
        registerTime = account == null ? null : account.getCreateDate();
        // TODO 暂时没有这个ip
        registerIp = account == null ? "" : account.getRegisterIp();
        LastLoginTime = account == null ? null : account.getLoginDate();
        channel = account.getChannel();
        this.vipExp = player.getVipExp();
        this.lastLoginIp = account.getLastLoginIp();
        this.freeVipExp = player.getLord().getFreeVipExp();
        if (player.getVipGifts() != null) {
            this.giftBag = player.getVipGifts();
        } else {
            this.giftBag = new ArrayList<>();
        }
        this.totalRecharge = player.getLord().getTopup();
        heros = new ArrayList<>();
        for (Integer heroId : player.getEmbattleList()) {
            Hero hero = player.getHero(heroId);
            if (hero != null) {
                heros.add(hero);
            }
        }
    }


    public Integer getTotalRecharge() {
        return totalRecharge;
    }

    public void setTotalRecharge(Integer totalRecharge) {
        this.totalRecharge = totalRecharge;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public long getHonor() {
        return honor;
    }

    public void setHonor(long honor) {
        this.honor = honor;
    }

    public int getGovern() {
        return govern;
    }

    public void setGovern(int govern) {
        this.govern = govern;
    }

    public int getSystemGold() {
        return systemGold;
    }

    public void setSystemGold(int systemGold) {
        this.systemGold = systemGold;
    }

    public int getRechargeGold() {
        return rechargeGold;
    }

    public void setRechargeGold(int rechargeGold) {
        this.rechargeGold = rechargeGold;
    }

    public int getCostGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }

    public int getTotalGold() {
        return totalGold;
    }

    public void setTotalGold(int totalGold) {
        this.totalGold = totalGold;
    }

    public long getIron() {
        return iron;
    }

    public void setIron(long iron) {
        this.iron = iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public long getCopper() {
        return copper;
    }

    public void setCopper(long copper) {
        this.copper = copper;
    }

    public long getOil() {
        return oil;
    }

    public void setOil(long oil) {
        this.oil = oil;
    }

    public long getStone() {
        return stone;
    }

    public void setStone(long stone) {
        this.stone = stone;
    }

    public int getRocketType() {
        return rocketType;
    }

    public void setRocketType(int rocketType) {
        this.rocketType = rocketType;
    }

    public int getTankType() {
        return tankType;
    }

    public void setTankType(int tankType) {
        this.tankType = tankType;
    }

    public int getWarCar() {
        return warCar;
    }

    public void setWarCar(int warCar) {
        this.warCar = warCar;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public void setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
    }

    public Date getLastLoginTime() {
        return LastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        LastLoginTime = lastLoginTime;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public long getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(long accountKey) {
        this.accountKey = accountKey;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public int getFreeVipExp() {
        return freeVipExp;
    }

    public void setFreeVipExp(int freeVipExp) {
        this.freeVipExp = freeVipExp;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public List<Integer> getGiftBag() {
        return giftBag;
    }

    public void setGiftBag(List<Integer> giftBag) {
        this.giftBag = giftBag;
    }

  public List<Hero> getHeros() {
    return heros;
  }

  public void setHeros(List<Hero> heros) {
    this.heros = heros;
  }
}
