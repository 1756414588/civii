package com.game.dao.s;

import com.game.domain.s.*;
import com.game.flame.entity.*;
import com.game.season.StaticCompPlan;
import com.game.season.directgift.entity.StaticSeasonLimitGift;
import com.game.season.grand.entity.StaticSeasonTreasury;
import com.game.season.hero.StaticComProf;
import com.game.season.hero.StaticComSkill;
import com.game.season.journey.entity.*;
import com.game.season.seasongift.entity.StaticSeasonPayGift;
import com.game.season.seven.entity.StaticSeasonSeven;
import com.game.season.seven.entity.StaticSeasonSevenAward;
import com.game.season.seven.entity.StaticSeasonSevenRank;
import com.game.season.seven.entity.StaticSeasonSevenType;
import com.game.season.talent.entity.StaticCompTalent;
import com.game.season.talent.entity.StaticCompTalentType;
import com.game.season.talent.entity.StaticCompTalentUp;
import com.game.season.turn.entity.StaticTurn;
import com.game.season.turn.entity.StaticTurnAward;
import com.game.season.turn.entity.StaticTurnConfig;
import org.apache.ibatis.annotations.MapKey;
import com.game.domain.s.StaticBookSkillEffectType;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface StaticDataDao {
    public StaticIniLord selectLord();

    @MapKey("lordLv")
    public Map<Integer, StaticLordLv> selectLordLv();

    @MapKey("lordId")
    public Map<Integer, StaticLost> selectStaticLost();

    @MapKey("propId")
    public Map<Integer, StaticProp> selectProp();

    @MapKey("awardId")
    public Map<Integer, StaticAwards> selectAwardsMap();

    @MapKey("heroId")
    public Map<Integer, StaticHero> selectHeroMap();

    @MapKey("heroLv")
    public List<StaticHeroLv> selectHeroExpList();

    @MapKey("mailId")
    public Map<Integer, StaticMail> selectMail();

    public List<StaticVip> selectVip();

    public List<StaticPay> selectPay();

    public List<StaticPayPoint> selectPayPoint();

    public List<StaticMailPlat> selectMailPlat();

    public List<StaticHeroProp> selectHeroPropList();

    @MapKey("equipId")
    public Map<Integer, StaticEquip> selectEquipMap();

    @MapKey("skillId")
    public Map<Integer, StaticSkill> selectSkillMap();

    @MapKey("targetId")
    public Map<Integer, StaticTargetAward> selectTargetAwardMap();

    public StaticLimit selectLimit();

    @MapKey("buyTimes")
    public Map<Integer, StaticBuyEqupSlot> selectBuyEquipSlot();

    @MapKey("soldierLv")
    public Map<Integer, StaticSoldierLv> selectStaticSoldierLv();

    public List<StaticCapacityTimes> selectStaticCapacityTimes();

    @MapKey("buyTimes")
    public Map<Integer, StaticBuySolodierTime> selectStaticBuySolodierTime();

    @MapKey("washId")
    public Map<Integer, StaticHeroWash> selectStaticHeroWash();

    @MapKey("missionId")
    public Map<Integer, StaticMission> selectMissionMap();

    @MapKey("monsterId")
    public Map<Integer, StaticMonster> selectMonsterMap();

    @MapKey("id")
    public Map<Integer, StaticBattle> selectStaticBattle();

    @MapKey("keyId")
    public Map<Integer, StaticBuildingLv> selectBuildingLvMap();

    @MapKey("employId")
    public Map<Integer, StaticEmployee> selectStaticEmployee();

    @MapKey("buildingType")
    public Map<Integer, StaticBuildingType> selectStaticBuilding();

    @MapKey("buildingId")
    public Map<Integer, StaticBuilding> selectBuildingMap();

    @MapKey("techType")
    public Map<Integer, StaticTechType> selectTechTypeMap();

    @MapKey("keyId")
    public Map<Integer, StaticTechInfo> selectTechLevelsMap();

    public List<StaticDepot> selectStaticDepot();

    public List<StaticVipBuy> selectStaticVipBuy();

    @MapKey("times")
    public Map<Integer, StaticCapacityTimes> selectStaticCapacityTimesMap();

    @MapKey("energyTimes")
    public Map<Integer, StaticEnergyPrice> selectEnergyPrice();

    @MapKey("lootId")
    public Map<Integer, StaticLootHero> selectStaticLootHero();

    @MapKey("type")
    public Map<Integer, StaticHeroAdvance> selectStaticHeroAdvance();

    @MapKey("buildingId")
    public Map<Integer, StaticPropBuilding> selectStaticPropBuilding();

    @MapKey("mapId")
    public Map<Integer, StaticWorldMap> selectWorldMap();

    @MapKey("cityId")
    public Map<Integer, StaticWorldCity> selectWorldCity();

    @MapKey("taskId")
    public Map<Integer, StaticTask> selectTaskMap();

    @MapKey("id")
    public Map<Integer, StaticWorldResource> selectWorldResourceMap();

    @MapKey("level")
    public Map<Integer, StaticWorkShop> selectWorkShopMap();

    @MapKey("buyTimes")
    public Map<Integer, StaticWorkShopBuy> selectWorkShopBuyMap();

    @MapKey("commandLv")
    public Map<Integer, StaticPeople> selectPeopleMap();

    @MapKey("equipId")
    public Map<Integer, StaticKillEquip> selectStaticKillEquip();

    @MapKey("keyId")
    public Map<Integer, StaticKillEquipLevel> selectStaticKillEquipLevel();

    @MapKey("criti")
    public Map<Integer, StaticKillEquipRate> selectStaticKillEquipRate();

    public List<StaticActDialStone> selectActDialStone();

    public List<StaticActAward> selectActAward();

    public List<StaticActCommand> selectActCommand();

    @MapKey("id")
    public Map<Integer, StaticDailyCheckin> selectDailyCheckinAwards();

    public List<StaticActivityPlan> selectActPlan();

    @MapKey("activityId")
    public Map<Integer, StaticActivity> selectActivity();

    public List<StaticActQuota> selectActQuota();

    public List<StaticActTask> selectActTask();

    public List<StaticActShop> selectActShop();

    @MapKey("id")
    public Map<Integer, StaticWorldMonster> selectWorldMonsterMap();

    public List<StaticWorldPlayer> selectWorldPlayer();

    @MapKey("id")
    public Map<Integer, StaticSimpleConfig> selectSimpleConfig();

    @MapKey("id")
    public Map<Integer, StaticPrimaryResource> selectPrimaryResource();

    @MapKey("id")
    public Map<Integer, StaticMysteriousCave> selectMysteriousCave();

    public List<StaticMysteriousCave> selectMysteriousCaveList();

    @MapKey("commandLv")
    public Map<Integer, StaticMonsterFlush> selectMonsterFlush();

    @MapKey("id")
    public Map<Integer, StaticMonsterNum> selectMonsterNum();

    public List<StaticActFoot> selectActFoot();

    @MapKey("id")
    public Map<Integer, StaticWorldResNum> selectWorldResNumMap();

    public List<StaticActDial> selectActDial();

    public List<StaticActDialLuck> selectActDialLuck();

    public List<StaticActDialPurp> selectActDialPurp();

    @MapKey("wareLevel")
    public Map<Integer, StaticWare> selectWareMap();

    @MapKey("taskId")
    public Map<Integer, StaticCountryTask> selectCountryTask();

    @MapKey("gloryId")
    public Map<Integer, StaticCountryGlory> selectCountryGlory();

    @MapKey("dailyId")
    public Map<Integer, StaticCountryDaily> selectCountryRecord();

    public List<StaticCountryRank> selectCountryRank();

    @MapKey("heroId")
    public Map<Integer, StaticCountryHero> selectCountryHero();

    @MapKey("titleId")
    public Map<Integer, StaticCountryTitle> selectCountryTitle();

    @MapKey("targetId")
    public Map<Integer, StaticWorldTarget> selectStaticWorldTarget();

    public List<StaticCountryBuild> selectCountryBuild();

    @MapKey("countryLv")
    public Map<Integer, StaticCountryLevel> selectCountryLevel();

    @MapKey("type")
    public Map<Integer, StaticScout> selectScoutMap();

    @MapKey("level")
    public Map<Integer, StaticScoutLv> selectScoutLvMap();

    @MapKey("mapType")
    public Map<Integer, StaticMapMove> selectMapMoveMap();

    public StaticLoginAward selectLoginAward();

    public List<StaticCountryGovern> selectCountryGovern();

    public List<StaticWallMonster> selectWallMonster();

    @MapKey("Id")
    public Map<Integer, StaticWallMonsterLv> selectWallMonsterLv();

    @MapKey("stateId")
    public Map<Integer, StaticNewState> selectStaticNewState();

    @MapKey("keyId")
    public Map<Integer, StaticWorldExp> selectWorldExpMap();

    @MapKey("mapType")
    public Map<Integer, StaticPlayerKick> selectPlayerKickMap();

    @MapKey("level")
    public Map<Integer, StaticSquareMonster> selectSquareMonsterMap();

    @MapKey("keyId")
    public Map<Integer, StaticFortressMonster> selectFortressMonsterMap();

    @MapKey("keyId")
    public Map<Integer, StaticFortressResource> selectFortressResource();

    @MapKey("chatId")
    public Map<Integer, StaticChat> selectChat();

    @MapKey("awardId")
    public Map<Integer, StaticActDrop> selectActDrop();

    @MapKey("awardId")
    public Map<Integer, StaticActDouble> selectActDouble();

    public List<StaticWashSkill> selectWashSkillRate();

    public List<StaticExchange> selectStaticExchange();

    @MapKey("keyId")
    public Map<Integer, StaticSeason> selectSeasonMap();

    @MapKey("keyId")
    public Map<Integer, StaticChatShow> selectChatShow();

    @MapKey("keyId")
    public Map<Integer, StaticActMonster> selectActMonster();

    @MapKey("itemId")
    public Map<Integer, StaticExchangeItem> selectExchangeItem();

    @MapKey("heroId")
    public Map<Integer, StaticExchangeHero> selectExchangeHero();

    @MapKey("actionId")
    public Map<Integer, StaticPvpCost> selectWorldPvpCost();

    public List<StaticActPayGift> selectActPayGift();

    public List<StaticActPayCard> selectActPayCard();

    public List<StaticActPayMoney> selectActPayMoney();

    public List<StaticActPayArms> selectActPayArms();

    @MapKey("rankLv")
    public Map<Integer, StaticPvpRank> selectPvpRankMap();

    @MapKey("keyId")
    public Map<Integer, StaticPvpTotalKill> selectPvpTotalKillMap();

    @MapKey("keyId")
    public Map<Integer, StaticPvpDig> selectPvpDigMap();

    @MapKey("propId")
    public Map<Integer, StaticPvpExchange> selectPvpExchange();

    @MapKey("propId")
    public Map<Integer, StaticPvpExchange> selectBroodWarShop();

    @MapKey("battleLv")
    public Map<Integer, StaticPvpBattleLv> selectBattleLvMap();

    public List<StaticCountryHeroEscape> selectHeroEscape();

    @MapKey("moldId")
    Map<Integer,StaticActFirstPay> selectActFirstPay();

    public List<StaticActRankDisplay> selectActRankDisplay();

    @MapKey("taskId")
    public Map<Integer, StaticStaffTask> selectStaffTaskMap();

    @MapKey("keyId")
    public Map<Integer, StaticRiotMonster> selectRoitMonster();

    @MapKey("keyId")
    public Map<Integer, StaticRoitTime> selectRoitTime();

    @MapKey("keyId")
    public Map<Integer, StaticRoitItemShop> selectItemShop();

    @MapKey("keyId")
    public Map<Integer, StaticRoitScoreShop> selectRoitScoreShop();

    @MapKey("keyId")
    public Map<Integer, StaticRoitWaveMonster> selectRoitWave();

    public List<StaticRiotAward> selectRiotAward();

    public List<StaticInitName> selectInitName();

    @MapKey("portraitId")
    public Map<Integer, StaticPortrait> selectPortraitMap();

    @MapKey("keyId")
    public Map<Integer, StaticActRedDial> selectActRedDial();

    public List<StaticActSeven> selectActSeven();

    @MapKey("keyId")
    public Map<Integer, StaticActStealCity> selectActStealCity();

    @MapKey("id")
    public Map<Integer, StaticMeetingCommand> selectMeetingCommand();

    @MapKey("id")
    public List<StaticMeetingSoldier> selectMeetingSoldier();

    @MapKey("id")
    public Map<Integer, StaticMeetingTask> selectMeetingTask();

    @MapKey("targetId")
    public Map<Integer, StaticWorldNewTarget> selectWorldNewTarget();

    @MapKey("id")
    public Map<Integer, StaticActWorldBoss> selectActWorldBoss();

    List<StaticLairRank> getStaticLairRank();

    @MapKey("id")
    public Map<Integer, StaticResPackager> selectStaticResPackager();

    @MapKey("keyId")
    public Map<Integer, StaticOpen> selectStaticOpen();

    @MapKey("id")
    public Map<Integer, StaticWorldActPlan> selectWorldActPlan();

    @MapKey("id")
    Map<Integer, StaticRebelExchange> selectStaticRebelExchange();

    @MapKey("id")
    Map<Integer, StaticRebelRankAward> selectStaticRebelRankAward();

    @MapKey("lv")
    Map<Integer, StaticRebelZergDrop> selectStaticRebelZergDrop();

    @MapKey("cityType")
    Map<Integer, StaticWorldCityType> selectStaticWorldCityType();

    // 美女系统查询所有美女基本信息
    @MapKey("keyId")
    public Map<Integer,StaticBeautyBase> selectStaticBeautyBases();

    public List<StaticBeautyDateSkills> selectStaticBeautyStarSkills();

    public List<StaticBeautyDateSkills> selectStaticBeautyDateSkills();

    @MapKey("registerId")
    Map<Integer, StaticRegisterConfig> selectRegisterConfig();

    // 配饰系统相关配置查询
    public List<StaticOmament> selectStaticOmament(); // 获取配饰配置列表

    public List<StaticOmType> selectStaticOmType(); // 获取配饰类型配置列表

    /*public List<StaticBeautyVoices> selectStaticBeautyVoices();*/
    public List<StaticOmament> selectStaticOmamentByType(int type); // 根据type类型获取配饰类型配置列表

    @MapKey("mailId")
    Map<Integer, StaticMentorAward> selectMentorAward();

    @MapKey("id")
    Map<Integer, StaticFriendshipScoreShop> selectFriendshipScoreShop();

    @MapKey("id")
    Map<Integer, StaticApprenticeAward> selectApprenticeAward();

    @MapKey("journeyId")
    Map<Integer, StaticJourney> selectStaticJourney();

    @MapKey("journeyTimes")
    Map<Integer, StaticJourneyPrice> selectStaticJourneyPrice();

    @MapKey("id")
    Map<Integer, StaticTowerWarLevel> loadAllStaticTowerWarLevel();

    @MapKey("id")
    Map<Integer, StaticTowerWarMap> loadAllStaticTowerWarMap();

    List<StaticTowerWarMonster> loadAllStaticTowerWarMonster();

    List<StaticTowerWarTower> loadStaticTowerWarTower();

    @MapKey("id")
    Map<Integer, StaticTowerWarWave> loadStaticTowerWarWave();

    @MapKey("id")
    Map<Integer, StaticTowerWarBonus> loadStaticTowerWarBonus();

    @MapKey("cityType")
    Map<Integer, StaticFirstBloodAward> loadStaticFirstBloodAward();

    @MapKey("level")
    Map<Integer, StaticActHope> loadStaticActHope();

    List<StaticPassPortAward> selectPassPortAward();

    List<StaticPassPortTask> selectPassPortTask();

    @MapKey("lv")
    Map<Integer, StaticPassPortLv> loadStaticPassPortLv();

    List<StaticActEquipUpdate> loadStaticActEquipUpdate();

    @MapKey("id")
    Map<Integer, StaticCityGame> loadStaticCityGame();

    List<StaticPayPassPort> selectStaticPayPassPorts();

    List<StaticActFreeBuy> selectActFreeBuy();

    @MapKey("level")
    Map<Integer, StaticApprenticeRank> selectMentorRank();

    @MapKey("awardId")
    Map<Integer,StaticDialCost> selectDialCost();

    List<StaticWarBook> selectStaticWarBooks();

    List<StaticWarBookBaseProperty> selectWarBookBaseProperty();

    List<StaticWarBookSkill> selectWarBookSkill();

    @MapKey("keyId")
    Map<Integer,StaticActExchange> selectActExchange();

    List<StaticWarBookDrop> selectWarBookDrop();

    @MapKey("keyId")
    Map<Integer,StaticActivityChrismas> loadStaticActivityChrismas();

    @MapKey("keyId")
    Map<Integer,StaticActivityChrismasAward> loadStaticActivityChrismasAward();

    List<StaticWarBookDecom> selectWarBookDecom();

    @MapKey("mailId")
    Map<Integer,StaticMailAward> loadStaticMailAward();

    List<StaticWarBookBuy> selectWarBookBuy();

    List<StaticWarBookShopGoods> selectWarBookShopGoods();

    List<StaticWarBookExchange> selectWarBookExchanges();

    @MapKey("boxId")
    Map<Integer, StaticWorldBox> loadStaticWorldBox();

    @MapKey("eventId")
    Map<Integer, StaticWorldBoxCollect> loadStaticWorldBoxCollect();

    public List<StaticBookSkillEffectType> selectBookSkillEffectType();

    public List<StaticBaseSkin> selectStaticBaseSkin();
    @MapKey("id")
    Map<Integer, StaticPersonality> loadStaticPersonality();

    List<StaticMyExchange> queryAllMyExchange();

    @MapKey("id")
    Map<Integer, StaticTaskDaily> loadStaticTaskDaily();

    @MapKey("id")
    Map<Integer, StaticTaskDailyAward> loadStaticTaskDailyAward();

    @MapKey("id")
    Map<Integer, StaticHeroTask> loadStaticHeroTask();

    @MapKey("keyId")
    Map<Integer,StaticLimitGift> loadStaticLimitGift();

    @MapKey("keyId")
    Map<Integer,StaticDialAwards> queryALLDialAwardConfig();

    @MapKey("id")
    Map<Long,StaticGiantZerg> loadStaticGiantZerg();

    @MapKey("id")
    Map<Integer,StaticGiantZergBuff> loadStaticGiantZergBuff();

    List<StaticSkinSkill> selectStaticSkinSkills();

    List<StaticBeautyDate> selectStaticBeautyDate();

    List<StaticBeautyDateAward> selectStaticBeautyDateAward();

    @MapKey("type")
    Map<Integer, StaticPayCalculate> queryPayCal();

    @MapKey("id")
    Map<Integer, StaticHerDiviConfig> selectStaticDivi();

    List<StaticSuperRes> querySuperRes();

    @MapKey("lv")
    Map<Integer, StaticFortressLv> queryFortressLv();

    @MapKey("times")
    Map<Integer, StaticFortressBuild> queryFortressBuild();


    List<StaticBroodWarBuff> loadStaticBroodWarBuff();

    List<StaticBroodWarCommand> loadStaticBroodWarCommand();

    List<StaticBroodWarShop> loadStaticBroodWarShop();

    List<StaticBroodBuffCost> loadStaticBroodBuffCost();

    List<StaticBroodWarKillScore> loadStaticBroodWarKillScore();

    List<StaticZergMonster> loadStaticZergMonster();

    List<StaticResourceGift> loadStaticResourceGift();

    List<StaticZergShop> loadStaticZergShop();

    List<StaticZergRound> loadStaticZergRound();

    List<StaticHeroTalent> selectHeroTalent();

	List<StaticManoeuvreMatch> loadStaticManoeuvreMatch();

	List<StaticManoeuvreShop> loadStaticManoeuvreShop();

	List<StaticManoeuvreRankAward> loadStaticManoeuvreRankAward();

	@MapKey("id")
	Map<Integer, StaticTowerWarBonus> loadStaticEndlessBonus();

	@MapKey("id")
	Map<Integer, StaticEndlessArmory> loadStaticEndlessShop();

	@MapKey("id")
	Map<Integer, StaticEndlessArmory> loadStaticEndlessArmory();

	List<StaticEndlessAward> loadStaticEndlessAward();

	@MapKey("id")
	Map<Integer, StaticEndlessBaseinfo> loadStaticEndlessBaseInfo();

	@MapKey("id")
	Map<Integer, StaticEndlessLevel> loadStaticEndlessLevel();

	@MapKey("propId")
	Map<Integer, StaticEndlessItem> loadStaticEndlessItem();

	@MapKey("id")
	Map<Integer, StaticEndlessTDDropLimit> loadEndlessTDDropLimit();

	@MapKey("id")
	Map<Integer, StaticTowerWarTower> loadEndlessTower();

	@MapKey("id")
	Map<Integer, StaticTowerWarMonster> loadEndlessMonster();

	@MapKey("id")
	Map<Integer, StaticTowerWarWave> loadEndlessWave();

	@MapKey("vip")
	Map<Integer, StaticMaterialSubstituteVip> loadMaterialSubstituteVip();

	List<StaticMaterialSubstituteCost> loadMaterialSubstituteCost();

	List<StaticActSpringFestival> loadSpringFestival();

	List<StaticLimitGift> loadSpringGift();

	@MapKey("taskId")
	Map<Integer, StaticTDSevenTask> loadStaticTDSevenTaskMap();
	@MapKey("keyId")
	Map<Integer, StaticTDSevenBoxAward> loadStaticTDSevenBoxAwardMap();

    List<StaticGroup> queryGroup();


    // 钓鱼相关
    @MapKey("id")
    Map<Integer, StaticFish> loadStaticFish();

    @MapKey("baitId")
    Map<Integer, StaticFishBait> loadStaticFishBait();

    @MapKey("id")
    Map<Integer, StaticFishHeroGroup> loadStaticFishHeroGroup();

    @MapKey("id")
    Map<Integer, StaticFishLv> loadStaticFishLv();

    @MapKey("id")
    Map<Integer, StaticFishShop> loadStaticFishShop();

    @MapKey("id")
    Map<Integer,StaticSensitiveWord> loadSensitiveWord(@Param("_parameter") String parameter);

	List<StaticFlameSafe> loadFlameSafe();

	@MapKey("id")
	Map<Integer, StaticFlameMine> loadFlameMin();

	List<StaticFlameFlushMine> loadFlameMinFlush();

	@MapKey("id")
	Map<Integer, StaticFlameBuff> loadFlameBuff();

	@MapKey("id")
	Map<Integer, StaticFlameBuild> loadFlameBuild();

	@MapKey("id")
	Map<Integer, StaticFlameShop> loadFlameShop();

	List<StaticFlameRankGear> loadFlameRankGears();

	@MapKey("id")
	Map<Integer, StaticFlameRankCamp> loadFlameRankCamp();

	List<StaticFlameKill> loadFlameKill();

    @MapKey("id")
    Map<Integer, StaticBulletWarLevel> loadBulletWar();

    List<StaticCompPlan> loadComPlan();

    List<StaticSeasonJourney> loadSeasonJourney();
    List<StaticJourneyAward> loadJourneyAward();
    List<StaticJourneyCamp> loadJourneyCamp();
    List<StaticJourneyPerson> loadJourneyPerson();
    List<StaticJourneyRankOfficer> loadJourneyRankOff();


    List<StaticTurn> loadStaticTurn();
    List<StaticTurnAward> loadStaticTurnAward();
    StaticTurnConfig loadStaticTurnConfig();

    @MapKey("keyId")
    Map<Integer, StaticSeasonLimitGift> loadStaticSeasonLimitGift();

    @MapKey("payMoneyId")
    Map<Integer, StaticSeasonPayGift> loadStaticSeasonPayGift();

    List<StaticSeasonSeven> loadStaticSeasonSeven();

    List<StaticSeasonSevenType> loadStaticSeasonSevenType();

    @MapKey("id")
    Map<Integer, StaticSeasonSevenAward> loadStaticSeasonSevenAward();

    List<StaticSeasonSevenRank> loadStaticSeasonSevenRank();

    @MapKey("id")
    Map<Integer, StaticSeasonTreasury> loadStaticSeasonTreasury();

    @MapKey("id")
    Map<Integer, StaticComSkill> loadStaticSeasonSkill();


    @MapKey("profId")
    Map<Integer, StaticComProf> loadStaticComProf();

    StaticCompTalent loadStaticCompTalent();

    @MapKey("keyId")
    Map<Integer, StaticCompTalentUp> loadStaticCompTalentUp();

    @MapKey("talentType")
    Map<Integer, StaticCompTalentType> loadStaticCompTalentType();
}
