package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Lord implements Cloneable {
    private long lordId;
    private String nick;
    private int portrait;
    private int sex;
    private int level;
    private int exp;
    private int vip;
    private int vipExp;
    private int topup;
    private int gold;
    private int goldCost;
    private int goldGive;
    private int title;
    private long honor;
    private long newState;
    private int country;
    private int energy;
    private long energyTime; // 上一次恢复体力的时间
    private int buyEnergy;
    private int buyEnergyTime;
    private int newerGift;
    private long onTime;
    private int olTime;
    private long offTime;
    private int ctTime;
    private int olAward;
    private int silence;
    private int olMonth;
    private int onBuild; // 自动建造开关
    private int loginDays;
    private int firstPay;
    private int tvip;
    private int tvipTime;
    private int buyEquipSlotTimes;
    private int washSkillTimes;
    private int expertWashSkillTimes;//秘技精研次数
    private int washHeroTimes;
    private int soliderLines;
    private int miss;
    private int hit;
    private int criti;
    private int tenacity;
    private int collectTimes;
    private long collectEndTime; // 表示上一次恢复的时间
    private int battleScore; // 英雄战力
    private long monthCard;// 月卡结束时间
    private long seasonCard;// 季卡结束时间
    private int useEnergyNum;
    private int useEnergyDay;
    private long depotTime;
    private int depotRefresh;
    private int depotBuyTime;
    private long washSkillEndTime; // 上一次洗练的时间
    private long washHeroEndTime; // 上一次洗练英雄的时间
    private int lootCommonHero; // 抽良将的次数
    private long lootCommonHeroTime; // 上一次抽良将的时间
    private long lootGoodHeroEndTime; // 抽神将的结束时间
    private int lootGoodHeroFiveTimes; // 是否已经五抽神将的状态
    private int lootGoodFreeTimes; // 抽神将免费次数
    private int goodHeroProcess; // 神将抽取进度
    private boolean isDataOk; // 是否注册异常
    private int lootCommonFreeTimes; // 免费抽取良将的次数
    private long protectedTime; // 城防结束时间
    private long buildTeamTime; // 建造队结束时间
    private int people; // 人口
    private int buyWorkShopQue; // 购买作坊队列的次数
    private long recoverPeopleTime; // 上次恢复人口的时间
    private int posX;
    private int posY;
    private int maxMonsterLv;
    private int killMonsterNum;
    private int worldKillMonsterStatus;
    private int killWorldBossDay;
    private int loginAward;
    private int wareTimes; // 家园重建次数
    private int wareHighTimes; // 家园高级重建次数
    private int wareBuildDay; // 家园重建次数
    private int flyTimes;
    private int flyDay;
    private int autoBuildTimes; // 自动建造次数
    private int autoWallTimes; // 自动补防次数
    private int cityId;
    private int vipTech;
    private int vipWorkShop;
    private int vipEquip;
    private int freeBackTimes;
    private int freeBackDay;
    private long exchangeRes;
    private int minCountry;
    private int loginMail;
    private int soldierAuto;
    private int onWall;
    private int buildingScore;
    private int mailShareDay;
    private int mailTimes;
    private int callTimes;
    private int callDay;
    private int callCount; // 可召唤总人数
    private int callReply; // 召唤应答人数
    private long callEndTime; // 召唤结束时间
    private boolean governLogin;
    private long growfoot;// 屯田
    private long lvUpTime;// 升级时间
    private int suggestCount;// 建议次数
    private int suggestTime;// 建议时间
    private int createState; // 玩家状态:0:未创建角色,1进入游戏但是没有创建角色,2进入游戏且创建了角色
    private int resPacketNum; // 市场资源打包今日的次数
    private long resPacketTime;// 上一次资源打包的时间
    private int guideKey; // 新手引导key

    private int seekingTimes; // 美女约会次数
    private int safety; // 美女安全感
    private int sGameTimes;// 美女小游戏次数
    private int buySGameTimes;// 总共购买小游戏次数
    private int buySeekingTimes;// 总共购买约会次数
    private long freeSGameEndTime; // 上一次小游戏次数重置的时间
    private long freeSeekingEndTime; // 上一次约会次数重置的时间
    private int firstBReName;// 美女是否是第一次改名:0是第一次  1.不是第一次
    private List<Integer> payStatusList; //每个计费点是否是第一次支付
    private String payStatus; //每个计费点是否是第一次支付
    private int openSpeak; //聊天是否开启 0未开启 1 开启
    private int freeVipExp;    //赠送的vip经验
    private int systemGold;        //系统赠送钻石
    private int rechargeGold;        //充值获得钻石


    private int lastJourney;    //最后通关的征途关卡
    private int journeyTimes;    //征途剩余次数
    private long freeJourneyEndTime;    //上一次征途免费次数重置的时间
    private long buyJourneyEndTime;    //上一次征途购买次数的时间
    private int buyJourneyTimes;    //购买征途购买次数

    private int rebelCall;  //叛军召唤数量
    private int killRebel;  //击杀叛军数量
    private int attackPlayerNum;   //攻打玩家主城
    private int attackCityNum;     //攻打地图上据点次数
    private int curMainTask;    //当前主线任务
    private int curMainDupicate;    //当前副本进度
    private int buildGift;       //建造礼包开启状态
    private long firstPlaySGameTime; //首次玩小游戏的时间
    private long warBookShopRefreshTime; //兵书商城刷新时间
    private int warBookShopRefresh; //兵书商城刷新次数
    private int buyBookShopRefreshTime; //兵书商城购买次数刷新时间
    private int dayRecharge;    //每日充值次数
    private int wordBoxNum;     //世界宝箱获得次数
    private int killRoitNum;     //虫族入侵虫子击杀
    private long bookEffectHoronCd;////兵书对阵营战额外增加荣誉的CD时间
    private int headIndex;  //默认头像框
    private int chatIndex;  //默认聊天框
    private int skin;// 玩家当前皮肤
    @Getter
    @Setter
    private int clothes;    //玩家默认服饰
    @Getter
    @Setter
    private int mergeServerStatus;    //合服状态  0:不合服   1:合服
    @Getter
    @Setter
    private int isSeven;//1.不推7日活动 2.推送
    @Getter
    @Setter
    private int mapId; //所在区域
    @Getter
    @Setter
    private int city;//玩家所在区域属于哪个名称（只有在母巢的时候有值）
    @Getter
    @Setter
    private int tdMoney;//塔防币

    @Getter
    @Setter
    private int commandLevel;

    @Override
    public Lord clone() {
        Lord lord = null;
        try {
            lord = (Lord) super.clone();
            ArrayList<Integer> list1 = new ArrayList<>();
            if (this.payStatusList != null) {
                this.payStatusList.forEach(integer -> {
                    list1.add(integer);
                });
            }
            lord.setPayStatusList(list1);
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return lord;
    }

    public void addKillRoitNum() {
        this.killMonsterNum++;
    }

    public void addWorldBoxNum() {
        this.wordBoxNum++;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public String getNick() {
        if (nick == null) {
            return "";
        }
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
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

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getTopup() {
        return topup;
    }

    public void setTopup(int topup) {
        this.topup = topup;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGoldCost() {
        return goldCost;
    }

    public void setGoldCost(int goldCost) {
        this.goldCost = goldCost;
    }

    public int getGoldGive() {
        return goldGive;
    }

    public void setGoldGive(int goldGive) {
        this.goldGive = goldGive;
    }

    public int getTitle() {
        if (title <= 0) {
            return 1;
        }
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public long getNewState() {
        return newState;
    }

    public void setNewState(long newState) {
        this.newState = newState;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public long getEnergyTime() {
        return energyTime;
    }

    public void setEnergyTime(long energyTime) {
        this.energyTime = energyTime;
    }

    public int getBuyEnergy() {
        return buyEnergy;
    }

    public void setBuyEnergy(int buyEnergy) {
        this.buyEnergy = buyEnergy;
    }

    public int getBuyEnergyTime() {
        return buyEnergyTime;
    }

    public void setBuyEnergyTime(int buyEnergyTime) {
        this.buyEnergyTime = buyEnergyTime;
    }

    public int getNewerGift() {
        return newerGift;
    }

    public void setNewerGift(int newerGift) {
        this.newerGift = newerGift;
    }

    public int getOlTime() {
        return olTime;
    }

    public void setOlTime(int olTime) {
        this.olTime = olTime;
    }

    public long getOnTime() {
        return onTime;
    }

    public void setOnTime(long onTime) {
        this.onTime = onTime;
    }

    public long getOffTime() {
        return offTime;
    }

    public void setOffTime(long offTime) {
        this.offTime = offTime;
    }

    public int getCtTime() {
        return ctTime;
    }

    public void setCtTime(int ctTime) {
        this.ctTime = ctTime;
    }

    public int getOlAward() {
        return olAward;
    }

    public void setOlAward(int olAward) {
        this.olAward = olAward;
    }

    public int getSilence() {
        return silence;
    }

    public void setSilence(int silence) {
        this.silence = silence;
    }

    public int getOlMonth() {
        return olMonth;
    }

    public void setOlMonth(int olMonth) {
        this.olMonth = olMonth;
    }

    public int getOnBuild() {
        return onBuild;
    }

    public void setOnBuild(int onBuild) {
        this.onBuild = onBuild;
    }

    public int getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(int loginDays) {
        this.loginDays = loginDays;
    }

    public int getFirstPay() {
        return firstPay;
    }

    public void setFirstPay(int firstPay) {
        this.firstPay = firstPay;
    }

    public int getTvip() {
        return tvip;
    }

    public void setTvip(int tvip) {
        this.tvip = tvip;
    }

    public int getTvipTime() {
        return tvipTime;
    }

    public void setTvipTime(int tvipTime) {
        this.tvipTime = tvipTime;
    }

    public int getBuyEquipSlotTimes() {
        return buyEquipSlotTimes;
    }

    public void setBuyEquipSlotTimes(int buyEquipSlotTimes) {
        this.buyEquipSlotTimes = buyEquipSlotTimes;
    }

    public int getWashSkillTimes() {
        return washSkillTimes;
    }

    public void setWashSkillTimes(int washSkillTimes) {
        this.washSkillTimes = washSkillTimes;
    }

    public int getWashHeroTimes() {
        return washHeroTimes;
    }

    public void setWashHeroTimes(int washHeroTimes) {
        this.washHeroTimes = washHeroTimes;
    }

    public int getSoliderLines() {
        return soliderLines;
    }

    public void setSoliderLines(int soliderLines) {
        this.soliderLines = soliderLines;
    }

    public int getMiss() {
        return miss;
    }

    public void setMiss(int miss) {
        this.miss = miss;
    }

    public int getHit() {
        return hit;
    }

    public void setHit(int hit) {
        this.hit = hit;
    }

    public int getCriti() {
        return criti;
    }

    public void setCriti(int criti) {
        this.criti = criti;
    }

    public int getTenacity() {
        return tenacity;
    }

    public void setTenacity(int tenacity) {
        this.tenacity = tenacity;
    }

    public int getCollectTimes() {
        collectTimes = Math.min(99, collectTimes);
        collectTimes = Math.max(0, collectTimes);
        return collectTimes;
    }

    public void setCollectTimes(int collectTimes) {
        collectTimes = Math.min(99, collectTimes);
        collectTimes = Math.max(0, collectTimes);
        this.collectTimes = collectTimes;
    }

    public long getCollectEndTime() {
        return collectEndTime;
    }

    public void setCollectEndTime(long collectEndTime) {
        this.collectEndTime = collectEndTime;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public int getBattleScore() {
        return battleScore;
    }

    public void setBattleScore(int battleScore) {
        this.battleScore = battleScore;
    }

    public long getMonthCard() {
        return monthCard;
    }

    public void setMonthCard(long monthCard) {
        this.monthCard = monthCard;
    }

    public long getSeasonCard() {
        return seasonCard;
    }

    public void setSeasonCard(long seasonCard) {
        this.seasonCard = seasonCard;
    }

    public int getUseEnergyNum() {
        return useEnergyNum;
    }

    public void setUseEnergyNum(int useEnergyNum) {
        this.useEnergyNum = useEnergyNum;
    }

    public int getUseEnergyDay() {
        return useEnergyDay;
    }

    public void setUseEnergyDay(int useEnergyDay) {
        this.useEnergyDay = useEnergyDay;
    }

    public long getDepotTime() {
        return depotTime;
    }

    public void setDepotTime(long depotTime) {
        this.depotTime = depotTime;
    }

    public int getDepotRefresh() {
        return depotRefresh;
    }

    public void setDepotRefresh(int depotRefresh) {
        this.depotRefresh = depotRefresh;
    }

    public int getDepotBuyTime() {
        return depotBuyTime;
    }

    public void setDepotBuyTime(int depotBuyTime) {
        this.depotBuyTime = depotBuyTime;
    }

    public long getWashSkillEndTime() {
        return washSkillEndTime;
    }

    public void setWashSkillEndTime(long washSkillEndTime) {
        this.washSkillEndTime = washSkillEndTime;
    }

    public long getWashHeroEndTime() {
        return washHeroEndTime;
    }

    public void setWashHeroEndTime(long washHeroEndTime) {
        this.washHeroEndTime = washHeroEndTime;
    }

    public int getLootCommonHero() {
        return lootCommonHero;
    }

    public void setLootCommonHero(int lootCommonHero) {
        this.lootCommonHero = lootCommonHero;
    }

    public long getLootCommonHeroTime() {
        return lootCommonHeroTime;
    }

    public void setLootCommonHeroTime(long lootCommonHeroTime) {
        this.lootCommonHeroTime = lootCommonHeroTime;
    }

    public long getLootGoodHeroEndTime() {
        return lootGoodHeroEndTime;
    }

    public void setLootGoodHeroEndTime(long lootGoodHeroEndTime) {
        this.lootGoodHeroEndTime = lootGoodHeroEndTime;
    }

    public int getLootGoodHeroFiveTimes() {
        return lootGoodHeroFiveTimes;
    }

    public void setLootGoodHeroFiveTimes(int lootGoodHeroFiveTimes) {
        this.lootGoodHeroFiveTimes = lootGoodHeroFiveTimes;
    }

    public int getLootGoodFreeTimes() {
        return lootGoodFreeTimes;
    }

    public void setLootGoodFreeTimes(int lootGoodFreeTimes) {
        this.lootGoodFreeTimes = lootGoodFreeTimes;
    }

    public int getGoodHeroProcess() {
        return goodHeroProcess;
    }

    public void setGoodHeroProcess(int goodHeroProcess) {
        this.goodHeroProcess = goodHeroProcess;
    }

    public boolean isDataOk() {
        return isDataOk;
    }

    public void setDataOk(boolean dataOk) {
        isDataOk = dataOk;
    }

    public int getLootCommonFreeTimes() {
        return lootCommonFreeTimes;
    }

    public void setLootCommonFreeTimes(int lootCommonFreeTimes) {
        this.lootCommonFreeTimes = lootCommonFreeTimes;
    }

    public long getProtectedTime() {
        return protectedTime;
    }

    public void setProtectedTime(long protectedTime) {
        this.protectedTime = protectedTime;
    }

    public long getBuildTeamTime() {
        return buildTeamTime;
    }

    public void setBuildTeamTime(long buildTeamTime) {
        this.buildTeamTime = buildTeamTime;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public int getBuyWorkShopQue() {
        return buyWorkShopQue;
    }

    public void setBuyWorkShopQue(int buyWorkShopQue) {
        this.buyWorkShopQue = buyWorkShopQue;
    }

    public long getRecoverPeopleTime() {
        return recoverPeopleTime;
    }

    public void setRecoverPeopleTime(long recoverPeopleTime) {
        this.recoverPeopleTime = recoverPeopleTime;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getMaxMonsterLv() {
        return maxMonsterLv;
    }

    public void setMaxMonsterLv(int maxMonsterLv) {
        this.maxMonsterLv = maxMonsterLv;
    }

    public int getKillMonsterNum() {
        return killMonsterNum;
    }

    public void setKillMonsterNum(int killMonsterNum) {
        this.killMonsterNum = killMonsterNum;
    }

    public int getWorldKillMonsterStatus() {
        return worldKillMonsterStatus;
    }

    public void setWorldKillMonsterStatus(int worldKillMonsterStatus) {
        this.worldKillMonsterStatus = worldKillMonsterStatus;
    }

    public int getKillWorldBossDay() {
        return killWorldBossDay;
    }

    public void setKillWorldBossDay(int killWorldBossDay) {
        this.killWorldBossDay = killWorldBossDay;
    }

    public long getHonor() {
        return honor;
    }

    public void setHonor(long honor) {
        this.honor = honor;
    }

    public int getLoginAward() {
        return loginAward;
    }

    public void setLoginAward(int loginAward) {
        this.loginAward = loginAward;
    }

    public int getWareTimes() {
        return wareTimes;
    }

    public void setWareTimes(int wareTimes) {
        this.wareTimes = wareTimes;
    }

    public int getWareHighTimes() {
        return wareHighTimes;
    }

    public void setWareHighTimes(int wareHighTimes) {
        this.wareHighTimes = wareHighTimes;
    }

    public int getWareBuildDay() {
        return wareBuildDay;
    }

    public void setWareBuildDay(int wareBuildDay) {
        this.wareBuildDay = wareBuildDay;
    }

    public int getFlyTimes() {
        return flyTimes;
    }

    public void setFlyTimes(int flyTimes) {
        this.flyTimes = flyTimes;
    }

    public int getFlyDay() {
        return flyDay;
    }

    public void setFlyDay(int flyDay) {
        this.flyDay = flyDay;
    }

    public int getAutoBuildTimes() {
        return autoBuildTimes;
    }

    public void setAutoBuildTimes(int autoBuildTimes) {
        this.autoBuildTimes = autoBuildTimes;
    }

    public int getAutoWallTimes() {
        return autoWallTimes;
    }

    public void setAutoWallTimes(int autoWallTimes) {
        this.autoWallTimes = autoWallTimes;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getVipTech() {
        return vipTech;
    }

    public void setVipTech(int vipTech) {
        this.vipTech = vipTech;
    }

    public int getVipWorkShop() {
        return vipWorkShop;
    }

    public void setVipWorkShop(int vipWorkShop) {
        this.vipWorkShop = vipWorkShop;
    }

    public int getVipEquip() {
        return vipEquip;
    }

    public void setVipEquip(int vipEquip) {
        this.vipEquip = vipEquip;
    }

    public int getFreeBackTimes() {
        return freeBackTimes;
    }

    public void setFreeBackTimes(int freeBackTimes) {
        this.freeBackTimes = freeBackTimes;
    }

    public int getFreeBackDay() {
        return freeBackDay;
    }

    public void setFreeBackDay(int freeBackDay) {
        this.freeBackDay = freeBackDay;
    }

    public long getExchangeRes() {
        return exchangeRes;
    }

    public void setExchangeRes(long exchangeRes) {
        this.exchangeRes = exchangeRes;
    }

    public int getMinCountry() {
        return minCountry;
    }

    public void setMinCountry(int minCountry) {
        this.minCountry = minCountry;
    }

    public int getLoginMail() {
        return loginMail;
    }

    public void setLoginMail(int loginMail) {
        this.loginMail = loginMail;
    }

    public int getSoldierAuto() {
        return soldierAuto;
    }

    public void setSoldierAuto(int soldierAuto) {
        this.soldierAuto = soldierAuto;
    }

    public int getOnWall() {
        return onWall;
    }

    public void setOnWall(int onWall) {
        this.onWall = onWall;
    }

    public int getBuildingScore() {
        return buildingScore;
    }

    public void setBuildingScore(int buildingScore) {
        this.buildingScore = buildingScore;
    }

    public int getMailShareDay() {
        return mailShareDay;
    }

    public void setMailShareDay(int mailShareDay) {
        this.mailShareDay = mailShareDay;
    }

    public int getMailTimes() {
        return mailTimes;
    }

    public void setMailTimes(int mailTimes) {
        this.mailTimes = mailTimes;
    }

    public int getCallTimes() {
        return callTimes;
    }

    public void setCallTimes(int callTimes) {
        this.callTimes = callTimes;
    }

    public int getCallDay() {
        return callDay;
    }

    public void setCallDay(int callDay) {
        this.callDay = callDay;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    public int getCallReply() {
        return callReply;
    }

    public void setCallReply(int callReply) {
        this.callReply = callReply;
    }

    public long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(long callEndTime) {
        this.callEndTime = callEndTime;
    }

    public boolean isGovernLogin() {
        return governLogin;
    }

    public void setGovernLogin(boolean governLogin) {
        this.governLogin = governLogin;
    }

    public long getGrowfoot() {
        return growfoot;
    }

    public void setGrowfoot(long growfoot) {
        this.growfoot = growfoot;
    }

    public long getLvUpTime() {
        return lvUpTime;
    }

    public void setLvUpTime(long lvUpTime) {
        this.lvUpTime = lvUpTime;
    }

    public int getSuggestCount() {
        return suggestCount;
    }

    public void setSuggestCount(int suggestCount) {
        this.suggestCount = suggestCount;
    }

    public int getSuggestTime() {
        return suggestTime;
    }

    public void setSuggestTime(int suggestTime) {
        this.suggestTime = suggestTime;
    }

    // 获取玩家所有战斗力
    public int getAllScore() {
        return battleScore + buildingScore; // 英雄属性 + 建筑战力
    }

    public int getCreateState() {
        return createState;
    }

    public void setCreateState(int createState) {
        this.createState = createState;
    }

    public int getResPacketNum() {
        return resPacketNum;
    }

    public void setResPacketNum(int resPacketNum) {
        this.resPacketNum = resPacketNum;
    }

    public long getResPacketTime() {
        return resPacketTime;
    }

    public void setResPacketTime(long resPacketTime) {
        this.resPacketTime = resPacketTime;
    }

    public int getGuideKey() {
        return guideKey;
    }

    public void setGuideKey(int guideKey) {
        this.guideKey = guideKey;
    }

    public int getSeekingTimes() {
        return seekingTimes;
    }

    public void setSeekingTimes(int seekingTimes) {
        this.seekingTimes = seekingTimes;
    }

    public int getSafety() {
        return safety;
    }

    public void setSafety(int safety) {
        this.safety = safety;
    }

    public int getsGameTimes() {
        return sGameTimes;
    }

    public void setsGameTimes(int sGameTimes) {
        this.sGameTimes = sGameTimes;
    }

    public int getBuySGameTimes() {
        return buySGameTimes;
    }

    public void setBuySGameTimes(int buySGameTimes) {
        this.buySGameTimes = buySGameTimes;
    }

    public int getFirstBReName() {
        return firstBReName;
    }

    public void setFirstBReName(int firstBReName) {
        this.firstBReName = firstBReName;
    }

    public long getFreeSGameEndTime() {
        return freeSGameEndTime;
    }

    public void setFreeSGameEndTime(long freeSGameEndTime) {
        this.freeSGameEndTime = freeSGameEndTime;
    }

    public int getBuySeekingTimes() {
        return buySeekingTimes;
    }

    public void setBuySeekingTimes(int buySeekingTimes) {
        this.buySeekingTimes = buySeekingTimes;
    }

    public long getFreeSeekingEndTime() {
        return freeSeekingEndTime;
    }

    public void setFreeSeekingEndTime(long freeSeekingEndTime) {
        this.freeSeekingEndTime = freeSeekingEndTime;
    }

    public List<Integer> getPayStatusList() {
        return payStatusList;
    }

    public void setPayStatusList(List<Integer> payStatusList) {
        this.payStatusList = payStatusList;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public int getOpenSpeak() {
        return openSpeak;
    }

    public void setOpenSpeak(int openSpeak) {
        this.openSpeak = openSpeak;
    }

    public int getFreeVipExp() {
        return freeVipExp;
    }

    public void setFreeVipExp(int freeVipExp) {
        this.freeVipExp = freeVipExp;
    }

    public void addFreeVipExp(int freeVipExp) {
        this.freeVipExp += freeVipExp;
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

    public void addSystemGold(int systemGold) {
        this.systemGold += systemGold;
    }

    public void addRechargeGold(int rechargeGold) {
        this.rechargeGold += rechargeGold;
    }

    public int getLastJourney() {
        return lastJourney;
    }

    public void setLastJourney(int lastJourney) {
        this.lastJourney = lastJourney;
    }

    public int getJourneyTimes() {
        return journeyTimes;
    }

    public void setJourneyTimes(int journeyTimes) {
        this.journeyTimes = journeyTimes;
    }

    public long getFreeJourneyEndTime() {
        return freeJourneyEndTime;
    }

    public void setFreeJourneyEndTime(long freeJourneyEndTime) {
        this.freeJourneyEndTime = freeJourneyEndTime;
    }

    public long getBuyJourneyEndTime() {
        return buyJourneyEndTime;
    }

    public void setBuyJourneyEndTime(long buyJourneyEndTime) {
        this.buyJourneyEndTime = buyJourneyEndTime;
    }

    public int getBuyJourneyTimes() {
        return buyJourneyTimes;
    }

    public void setBuyJourneyTimes(int buyJourneyTimes) {
        this.buyJourneyTimes = buyJourneyTimes;
    }

    public int getRebelCall() {
        return rebelCall;
    }

    public void setRebelCall(int rebelCall) {
        this.rebelCall = rebelCall;
    }

    public int getKillRebel() {
        return killRebel;
    }

    public void setKillRebel(int killRebel) {
        this.killRebel = killRebel;
    }

    public int getAttackPlayerNum() {
        return attackPlayerNum;
    }

    public void setAttackPlayerNum(int attackPlayerNum) {
        this.attackPlayerNum = attackPlayerNum;
    }

    public int getAttackCityNum() {
        return attackCityNum;
    }

    public void setAttackCityNum(int attackCityNum) {
        this.attackCityNum = attackCityNum;
    }

    public int getCurMainTask() {
        return curMainTask;
    }

    public void setCurMainTask(int curMainTask) {
        this.curMainTask = curMainTask;
    }

    public int getCurMainDupicate() {
        return curMainDupicate;
    }

    public void setCurMainDupicate(int curMainDupicate) {
        this.curMainDupicate = curMainDupicate;
    }

    public int getExpertWashSkillTimes() {
        return expertWashSkillTimes;
    }

    public void setExpertWashSkillTimes(int expertWashSkillTimes) {
        this.expertWashSkillTimes = expertWashSkillTimes;
    }

    public int getBuildGift() {
        return buildGift;
    }

    public void setBuildGift(int buildGift) {
        this.buildGift = buildGift;
    }

    public long getFirstPlaySGameTime() {
        return firstPlaySGameTime;
    }

    public void setFirstPlaySGameTime(long firstPlaySGameTime) {
        this.firstPlaySGameTime = firstPlaySGameTime;
    }

    public long getWarBookShopRefreshTime() {
        return warBookShopRefreshTime;
    }

    public void setWarBookShopRefreshTime(long warBookShopRefreshTime) {
        this.warBookShopRefreshTime = warBookShopRefreshTime;
    }

    public int getWarBookShopRefresh() {
        return warBookShopRefresh;
    }

    public void setWarBookShopRefresh(int warBookShopRefresh) {
        this.warBookShopRefresh = warBookShopRefresh;
    }

    public int getBuyBookShopRefreshTime() {
        return buyBookShopRefreshTime;
    }

    public void setBuyBookShopRefreshTime(int buyBookShopRefreshTime) {
        this.buyBookShopRefreshTime = buyBookShopRefreshTime;
    }

    public int getDayRecharge() {
        return dayRecharge;
    }

    public void setDayRecharge(int dayRecharge) {
        this.dayRecharge = dayRecharge;
    }

    public void addDayRecharge() {
        this.dayRecharge++;
    }

    public int getSkin() {
        if (skin <= 0) {
            return 1;
        }
        return skin;
    }

    public void setSkin(int skin) {
        this.skin = skin;
    }

    public long getBookEffectHoronCd() {
        return bookEffectHoronCd;
    }

    public void setBookEffectHoronCd(long bookEffectHoronCd) {
        this.bookEffectHoronCd = bookEffectHoronCd;
    }

    public int getWordBoxNum() {
        return wordBoxNum;
    }

    public void setWordBoxNum(int wordBoxNum) {
        this.wordBoxNum = wordBoxNum;
    }

    public int getKillRoitNum() {
        return killRoitNum;
    }

    public void setKillRoitNum(int killRoitNum) {
        this.killRoitNum = killRoitNum;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    public void setHeadIndex(int headIndex) {
        this.headIndex = headIndex;
    }

    public int getChatIndex() {
        return chatIndex;
    }

    public void setChatIndex(int chatIndex) {
        this.chatIndex = chatIndex;
    }

    public void copyLord(Lord res) {
        this.portrait = res.portrait;
        this.level = res.level;
        this.exp = res.exp;
        this.vip = res.vip;
        this.vipExp = res.vipExp;
        this.topup = res.topup;
        this.gold = res.gold;
        this.goldCost = res.goldCost;
        this.goldGive = res.goldGive;
        this.title = res.title;
        this.honor = res.honor;
        this.newState = res.newState;
        this.energy = res.energy;
        this.energyTime = res.energyTime; // 上一次恢复体力的时间
        this.buyEnergy = res.buyEnergy;
        this.buyEnergyTime = res.buyEnergyTime;
        this.newerGift = res.newerGift;
        this.onTime = res.onTime;
        this.olTime = res.olTime;
        this.offTime = res.offTime;
        this.ctTime = res.ctTime;
        this.olAward = res.olAward;
        this.silence = res.silence;
        this.olMonth = res.olMonth;
        this.onBuild = res.onBuild; // 自动建造开关
        this.loginDays = res.loginDays;
        this.firstPay = res.firstPay;
        this.tvip = res.tvip;
        this.tvipTime = res.tvipTime;
        this.buyEquipSlotTimes = res.buyEquipSlotTimes;
        this.washSkillTimes = res.washSkillTimes;
        this.expertWashSkillTimes = res.expertWashSkillTimes;//秘技精研次数
        this.washHeroTimes = res.washHeroTimes;
        this.soliderLines = res.soliderLines;
        this.miss = res.miss;
        this.hit = res.hit;
        this.criti = res.criti;
        this.tenacity = res.tenacity;
        this.collectTimes = res.collectTimes;
        this.collectEndTime = res.collectEndTime; // 表示上一次恢复的时间
        this.battleScore = res.battleScore; // 英雄战力
        this.monthCard = res.monthCard;// 月卡结束时间
        this.seasonCard = res.seasonCard;// 季卡结束时间
        this.useEnergyNum = res.useEnergyNum;
        this.useEnergyDay = res.useEnergyDay;
        this.depotTime = res.depotTime;
        this.depotRefresh = res.depotRefresh;
        this.depotBuyTime = res.depotBuyTime;
        this.washSkillEndTime = res.washSkillEndTime; // 上一次洗练的时间
        this.washHeroEndTime = res.washHeroEndTime; // 上一次洗练英雄的时间
        this.lootCommonHero = res.lootCommonHero; // 抽良将的次数
        this.lootCommonHeroTime = res.lootCommonHeroTime; // 上一次抽良将的时间
        this.lootGoodHeroEndTime = res.lootGoodHeroEndTime; // 抽神将的结束时间
        this.lootGoodHeroFiveTimes = res.lootGoodHeroFiveTimes; // 是否已经五抽神将的状态
        this.lootGoodFreeTimes = res.lootGoodFreeTimes; // 抽神将免费次数
        this.goodHeroProcess = res.goodHeroProcess; // 神将抽取进度
        this.isDataOk = res.isDataOk; // 是否注册异常
        this.lootCommonFreeTimes = res.lootCommonFreeTimes; // 免费抽取良将的次数
        this.protectedTime = res.protectedTime; // 城防结束时间
        this.buildTeamTime = res.buildTeamTime; // 建造队结束时间
        this.people = res.people; // 人口
        this.buyWorkShopQue = res.buyWorkShopQue; // 购买作坊队列的次数
        this.recoverPeopleTime = res.recoverPeopleTime; // 上次恢复人口的时间
        this.maxMonsterLv = res.maxMonsterLv;
        this.killMonsterNum = res.killMonsterNum;
        this.worldKillMonsterStatus = res.worldKillMonsterStatus;
        this.killWorldBossDay = res.killWorldBossDay;
        this.loginAward = res.loginAward;
        this.wareTimes = res.wareTimes; // 家园重建次数
        this.wareHighTimes = res.wareHighTimes; // 家园高级重建次数
        this.wareBuildDay = res.wareBuildDay; // 家园重建次数
        this.flyTimes = res.flyTimes;
        this.flyDay = res.flyDay;
        this.autoBuildTimes = res.autoBuildTimes; // 自动建造次数
        this.autoWallTimes = res.autoWallTimes; // 自动补防次数
        this.cityId = res.cityId;
        this.vipTech = res.vipTech;
        this.vipWorkShop = res.vipWorkShop;
        this.vipEquip = res.vipEquip;
        this.freeBackTimes = res.freeBackTimes;
        this.freeBackDay = res.freeBackDay;
        this.exchangeRes = res.exchangeRes;
        this.minCountry = res.minCountry;
        this.loginMail = res.loginMail;
        this.soldierAuto = res.soldierAuto;
        this.onWall = res.onWall;
        this.buildingScore = res.buildingScore;
        this.mailShareDay = res.mailShareDay;
        this.mailTimes = res.mailTimes;
        this.callTimes = res.callTimes;
        this.callDay = res.callDay;
        this.callCount = res.callCount; // 可召唤总人数
        this.callReply = res.callReply; // 召唤应答人数
        this.callEndTime = res.callEndTime; // 召唤结束时间
        this.governLogin = res.governLogin;
        this.growfoot = res.growfoot;// 屯田
        this.lvUpTime = res.lvUpTime;// 升级时间
        this.suggestCount = res.suggestCount;// 建议次数
        this.suggestTime = res.suggestTime;// 建议时间
        this.createState = res.createState; // 玩家状态:0:未创建角色,1进入游戏但是没有创建角色,2进入游戏且创建了角色
        this.resPacketNum = res.resPacketNum; // 市场资源打包今日的次数
        this.resPacketTime = res.resPacketTime;// 上一次资源打包的时间
        this.guideKey = res.guideKey; // 新手引导key

        this.seekingTimes = res.seekingTimes; // 美女约会次数
        this.safety = res.safety; // 美女安全感
        this.sGameTimes = res.sGameTimes;// 美女小游戏次数
        this.buySGameTimes = res.buySGameTimes;// 总共购买小游戏次数
        this.buySeekingTimes = res.buySeekingTimes;// 总共购买约会次数
        this.freeSGameEndTime = res.freeSGameEndTime; // 上一次小游戏次数重置的时间
        this.freeSeekingEndTime = res.freeSeekingEndTime; // 上一次约会次数重置的时间
        this.firstBReName = res.firstBReName;// 美女是否是第一次改名:0是第一次  1.不是第一次
        this.payStatusList = res.payStatusList; //每个计费点是否是第一次支付
        this.payStatus = res.payStatus; //每个计费点是否是第一次支付
        this.openSpeak = res.openSpeak; //聊天是否开启 0未开启 1 开启
        this.freeVipExp = res.freeVipExp;    //赠送的vip经验
        this.systemGold = res.systemGold;        //系统赠送钻石
        this.rechargeGold = res.rechargeGold;        //充值获得钻石


        this.lastJourney = res.lastJourney;    //最后通关的征途关卡
        this.journeyTimes = res.journeyTimes;    //征途剩余次数
        this.freeJourneyEndTime = res.freeJourneyEndTime;    //上一次征途免费次数重置的时间
        this.buyJourneyEndTime = res.buyJourneyEndTime;    //上一次征途购买次数的时间
        this.buyJourneyTimes = res.buyJourneyTimes;    //购买征途购买次数

        this.rebelCall = res.rebelCall;  //叛军召唤数量
        this.killRebel = res.killRebel;  //击杀叛军数量
        this.attackPlayerNum = res.attackPlayerNum;   //攻打玩家主城
        this.attackCityNum = res.attackCityNum;     //攻打地图上据点次数
        this.curMainTask = res.curMainTask;    //当前主线任务
        this.curMainDupicate = res.curMainDupicate;    //当前副本进度
        this.buildGift = res.buildGift;       //建造礼包开启状态
        this.firstPlaySGameTime = res.firstPlaySGameTime;      //首次玩小游戏的时间
        this.warBookShopRefreshTime = res.warBookShopRefreshTime;      //兵书商城刷新时间
        this.warBookShopRefresh = res.warBookShopRefresh;      //兵书商城刷新次数
        this.buyBookShopRefreshTime = res.buyBookShopRefreshTime;      //兵书商城购买次数刷新时间
        this.bookEffectHoronCd = res.bookEffectHoronCd;     //兵书对阵营战额外增加荣誉的CD时间
        this.skin = res.skin;     //玩家当前皮肤
        this.clothes = res.clothes;
        this.mergeServerStatus = res.mergeServerStatus;  //合服状态  0:不合服   1:合服
        this.isSeven = res.isSeven;  //1.不推7日活动 2.推送
        this.tdMoney = res.tdMoney;  //塔防币
        this.commandLevel = res.commandLevel;  //塔防币
    }
}