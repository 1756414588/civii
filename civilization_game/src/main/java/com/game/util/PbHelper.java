package com.game.util;

import com.game.domain.Award;
import com.game.register.PBFile;
import com.game.spring.SpringUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.game.constant.*;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.manager.CountryManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ActWashEquipt;
import com.game.pb.DataPb;
import com.game.server.GameServer;
import com.game.worldmap.FirstBloodInfo;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.InvalidProtocolBufferException;

public class PbHelper {

	public static byte[] putShort(short s) {
		byte[] b = new byte[2];
		b[0] = (byte) (s >> 8);
		b[1] = (byte) (s >> 0);
		return b;
	}

	static public short getShort(byte[] b, int index) {
		return (short) (((b[index + 1] & 0xff) | b[index + 0] << 8));
	}

	static public Base parseFromByte(byte[] result) throws InvalidProtocolBufferException {
		short len = PbHelper.getShort(result, 0);
		byte[] data = new byte[len];
		System.arraycopy(result, 2, data, 0, len);
		Base rs = Base.parseFrom(data, PBFile.registry);
		return rs;
	}

	static public Base createRsBase(int cmd, int code, long index) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		baseBuilder.setCode(code);
		baseBuilder.setIndex(index);
		return baseBuilder.build();
	}

	static public Base.Builder createError(int cmd, GameError gameError) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		baseBuilder.setCode(gameError.getCode());
		return baseBuilder;
	}

	static public <T> Base.Builder createRqBase(int cmd, Long param, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		if (param != null) {
			baseBuilder.setParam(param);
		}

		baseBuilder.setExtension(ext, msg);
		return baseBuilder;
	}

	public static CommonPb.EquipExChange createEquipExchangePb(Hero hero) {
		CommonPb.EquipExChange.Builder builder = CommonPb.EquipExChange.newBuilder();
		builder.setHeroId(hero.getHeroId());
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		for (HeroEquip item : heroEquips) {
			builder.addEquip(item.wrapPb());
		}
		Property totalProperty = hero.getTotalProp();
		builder.setProperty(totalProperty.wrapPb());
		return builder.build();
	}

	public static CommonPb.HeroInfo.Builder createHeroInfoPb(Hero hero) {
		CommonPb.HeroInfo.Builder heroInfo = CommonPb.HeroInfo.newBuilder();
		heroInfo.setHeroId(hero.getHeroId());
		heroInfo.setExp(hero.getExp());
		heroInfo.setLv(hero.getHeroLv());
		return heroInfo;
	}

	public static CommonPb.Prop.Builder createItemPb(int itemId, int itemNum) {
		CommonPb.Prop.Builder builder = CommonPb.Prop.newBuilder();
		builder.setPropId(itemId);
		builder.setPropNum(itemNum);
		return builder;
	}

	static public <T> Base.Builder createSynBase(int cmd, GeneratedExtension<Base, T> ext, T msg) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(cmd);
		if (msg != null) {
			baseBuilder.setExtension(ext, msg);
		}

		return baseBuilder;
	}

	public static CommonPb.Shop.Builder createShop(StaticProp staticProp) {
		CommonPb.Shop.Builder builder = CommonPb.Shop.newBuilder();
		builder.setPropId(staticProp.getPropId());
		builder.setPrice(staticProp.getPrice());
		return builder;
	}

	public static CommonPb.Award.Builder createAward(int type, int id, int count) {
		CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
		builder.setType(type);
		builder.setId(id);
		builder.setCount(count);
		return builder;
	}

	public static CommonPb.Award.Builder createAward(Player player, int type, int id, int count, int keyId) {
		CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
		builder.setType(type);
		builder.setId(id);
		builder.setCount(count);
		if (keyId != 0) {
			builder.setKeyId(keyId);
		}

		if (type == AwardType.EQUIP) {
			Map<Integer, Equip> equips = player.getEquips();
			Equip equip = equips.get(keyId);
			if (null != equip && null != equip.getSkills()) {
				ArrayList<Integer> skills = equip.getSkills();
				builder.addAllSkillId(skills);
			}
		}

		if (type == AwardType.WAR_BOOK || type == AwardType.WAR_BOOK_SKILL || type == AwardType.WAR_BOOK_SPECIAL) {
			builder.setType(AwardType.WAR_BOOK);
			Map<Integer, WarBook> warBook = player.getWarBooks();
			WarBook book = warBook.get(keyId);
			if (null != book) {
				if (null != book.getAllSkill()) {
					ArrayList<Integer> allSkill = book.getAllSkill();
					builder.addAllAllSkill(allSkill);
				}
				if (null != book.getCurrentSkill()) {
					ArrayList<Integer> currentSkill = book.getCurrentSkill();
					builder.addAllCurrentSkill(currentSkill);
				}
				if (null != book.getBaseProperty()) {
					ArrayList<Integer> baseProperty = book.getBaseProperty();
					builder.addAllBaseProperty(baseProperty);
				}
				builder.setBasePropertyLv(book.getBasePropertyLv());
				builder.setSoldierType(book.getSoldierType());
			}
		}

		return builder;
	}


	public static CommonPb.Award.Builder createWarBookAward(WarBook book) {
		CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
		builder.setType(AwardType.WAR_BOOK);
		builder.setId(book.getBookId());
		builder.setCount(1);

		if (null != book) {
			if (null != book.getAllSkill()) {
				ArrayList<Integer> allSkill = book.getAllSkill();
				builder.addAllAllSkill(allSkill);
			}
			if (null != book.getCurrentSkill()) {
				ArrayList<Integer> currentSkill = book.getCurrentSkill();
				builder.addAllCurrentSkill(currentSkill);
			}
			if (null != book.getBaseProperty()) {
				ArrayList<Integer> baseProperty = book.getBaseProperty();
				builder.addAllBaseProperty(baseProperty);
			}
			builder.setBasePropertyLv(book.getBasePropertyLv());
			builder.setSoldierType(book.getSoldierType());
		}

		return builder;
	}

	static public CommonPb.Activity createActivityPb(ActivityBase activityBase, Date beginTime, Date endTime, boolean canAward,
		boolean tips, int num) {
		CommonPb.Activity.Builder builder = CommonPb.Activity.newBuilder();
		builder.setActivityId(activityBase.getActivityId());
		builder.setName(activityBase.getStaticActivity().getName());
		builder.setBeginTime(beginTime.getTime());
		if (endTime == null) {
			if (activityBase.getActivityId() != ActivityConst.BLOOD_ACTIVITY) {
				builder.setEndTime(activityBase.getEndTime().getTime());
			} else {
				LocalDateTime now = LocalDateTime.now();
				LocalDateTime localDateTime = now.withHour(23).withMinute(59).withSecond(59);
				Long milliSecond = localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
				builder.setEndTime(milliSecond);
				LocalDateTime localDateTime1 = now.withHour(0).withMinute(0).withSecond(1);
				long l = localDateTime1.toInstant(ZoneOffset.of("+8")).toEpochMilli();
				builder.setBeginTime(l);
			}
		} else {
			builder.setEndTime(endTime.getTime());
		}
		if (activityBase.getDisplayTime() != null) {
			builder.setDisplayTime(activityBase.getDisplayTime().getTime());
		}
		if (activityBase.getStaticActivity().getDisplay() == ActivityConst.ACTIVITY_LONG && activityBase.getActivityId() != ActivityConst.BLOOD_ACTIVITY) {
			builder.setDisplayTime(ActivityConst.ACTIVITY_LONG);
		}
		builder.setCanAward(canAward);
		builder.setTips(tips);
		if (activityBase.getStaticActivity().getDesc() != null) {
			builder.setDetail(activityBase.getStaticActivity().getDesc());
		}

		switch (activityBase.getStaticActivity().getRank()) {
			case ActivityConst.RANK_1:
				builder.setRewardTime(TimeHelper.getCurRewardTime(activityBase.getBeginTime()).getTime());
				break;
			case ActivityConst.RANK_3:
				builder.setRewardTime(TimeHelper.getRewardTime(activityBase.getEndTime()).getTime());
				break;
			default:
				builder.setRewardTime(0);
				break;
		}
		builder.setNum(num);
		return builder.build();
	}

	public static CommonPb.Award makeAward(int type, int id, int count) {
		CommonPb.Award.Builder builder = CommonPb.Award.newBuilder();
		builder.setType(type);
		builder.setId(id);
		builder.setCount(count);
		return builder.build();
	}

	public static CommonPb.RankInfo createRank(Lord lord, int rank) {
		CommonPb.RankInfo.Builder builder = CommonPb.RankInfo.newBuilder();
		builder.setRank(rank);
		builder.setName(lord.getNick());
		builder.setLevel(lord.getLevel());
		builder.setCountry(lord.getCountry());
		builder.setTitle(lord.getTitle());
		builder.setBattleSocre(lord.getAllScore());
		builder.setLordId(lord.getLordId());
		builder.setPortrait(lord.getPortrait());
		return builder.build();
	}

	static public CommonPb.Activity createActivityPb(ActivityBase activityBase, Date endTime, boolean canAward,
		boolean tips) {
		return createActivityPb(activityBase, activityBase.getBeginTime(), endTime, canAward, tips, 0);
	}

	static public CommonPb.Activity createActivityPb(ActivityBase activityBase, Date endTime, boolean canAward,
		boolean tips, int num) {
		return createActivityPb(activityBase, activityBase.getBeginTime(), endTime, canAward, tips, num);
	}

	static public CommonPb.ActivityCond createDailyCheckinCondPb(StaticDailyCheckin actAward, int isAward, int param) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actAward.getId());
		builder.setCond(actAward.getId());
		builder.setIsAward(isAward);
		builder.setParam(String.valueOf(param));
		List<Integer> award = actAward.getAward();
		builder.addAward(createAward(award.get(0), award.get(1), award.get(2)));
		return builder.build();
	}

	static public CommonPb.ActivityCond createActivityCondPb(StaticActAward actAward, int isAward) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actAward.getKeyId());
		builder.setCond(actAward.getCond());
		builder.setIsAward(isAward);
		if (actAward.getParam() != null && !actAward.getParam().equals("")) {
			builder.setParam(actAward.getParam());
		}
		List<List<Integer>> awardList = actAward.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		builder.setDesc(StringUtil.isNullOrEmpty(actAward.getDesc()) ? "" : actAward.getDesc());
		return builder.build();
	}

	static public CommonPb.ActPassPortAward createActPassPortAwardPb(StaticPassPortAward actAward, int isAward) {
		CommonPb.ActPassPortAward.Builder actPassPortAward = CommonPb.ActPassPortAward.newBuilder();
		actPassPortAward.setKeyId(actAward.getId());
		actPassPortAward.setAwardId(actAward.getAwardId());
		actPassPortAward.setLv(actAward.getLv());
		actPassPortAward.setType(actAward.getType());
		actPassPortAward.setIsAward(isAward);

		List<List<Integer>> awardList = actAward.getAward();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			actPassPortAward.addAward(createAward(type, id, count));
		}

		return actPassPortAward.build();
	}


	static public CommonPb.ActivityCond createActivityCond(StaticActEquipUpdate actAward, int isAward, boolean canAward) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actAward.getKeyId())
			.setCond(actAward.getCond())
			.setIsAward(isAward)
			.setCanAward(canAward);
		if (actAward.getParam() != null && !actAward.getParam().equals("")) {
			builder.setParam(actAward.getParam());
		}
		List<List<Integer>> awardList = actAward.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public CommonPb.ActivityCond createActivityCondPb(StaticActSeven actSeven, int isAward) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actSeven.getKeyId());
		builder.setCond(actSeven.getCond());
		builder.setIsAward(isAward);
		if (actSeven.getParam() != 0) {
			builder.setParam(String.valueOf(actSeven.getParam()));
		}
		List<List<Integer>> awardList = actSeven.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public CommonPb.ActivityCond createActivityCondPb(StaticActAward actAward, int isAward, boolean canAward) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actAward.getKeyId())
			.setCond(actAward.getCond())
			.setIsAward(isAward)
			.setDesc(actAward.getDesc());
		if (actAward.getParam() != null && !actAward.getParam().equals("")) {
			builder.setParam(actAward.getParam());
		}
		builder.setCanAward(canAward);
		List<List<Integer>> awardList = actAward.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public CommonPb.ActivityCond createActivityCondPb(StaticHeroTask actAward, int isAward, boolean canAward) {
		CommonPb.ActivityCond.Builder builder = CommonPb.ActivityCond.newBuilder();
		builder.setKeyId(actAward.getId())
			.setCond(actAward.getCond())
			.setIsAward(isAward)
			.setDesc(StringUtil.isNullOrEmpty(actAward.getName()) ? "" : actAward.getName());
		builder.setParam(actAward.getJumpType() + "");
		builder.setCanAward(canAward);
		List<List<Integer>> awardList = actAward.getAwardlist();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public CommonPb.ActivityCondState createCondState(StaticActAward actAward, int isAward, int state) {
		CommonPb.ActivityCondState.Builder builder = CommonPb.ActivityCondState.newBuilder();
		builder.setState(state);
		builder.setActivityCond(createActivityCondPb(actAward, isAward));
		return builder.build();
	}

	static public CommonPb.ActivityCondState createCondState(StaticActSeven actSeven, int isAward, int state) {
		CommonPb.ActivityCondState.Builder builder = CommonPb.ActivityCondState.newBuilder();
		builder.setState(state);
		builder.setActivityCond(createActivityCondPb(actSeven, isAward));
		return builder.build();
	}

	static public CommonPb.Quota createQuotaPb(StaticActQuota staticActQuota, int state) {
		CommonPb.Quota.Builder builder = CommonPb.Quota.newBuilder();
		builder.setQuotaId(staticActQuota.getQuotaId());
		builder.setDisplay(staticActQuota.getDisplay());
		builder.setPrice(staticActQuota.getPrice());
		builder.setCount(staticActQuota.getCount());
		builder.setBuy(state);
		List<List<Integer>> awardList = staticActQuota.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public DataPb.ActivityRank createActRank(ActPlayerRank actPlayerRank) {
		DataPb.ActivityRank.Builder builder = DataPb.ActivityRank.newBuilder();
		builder.setLordId(actPlayerRank.getLordId());
		builder.setValue(actPlayerRank.getRankValue());
		return builder.build();
	}

	static public DataPb.Addtion createAddtionPb(long id, long value) {
		DataPb.Addtion.Builder builder = DataPb.Addtion.newBuilder();
		builder.setAddtionId(id);
		builder.setAddtionValue(value);
		return builder.build();
	}

	static public DataPb.Status createStatusPb(long key, long value) {
		DataPb.Status.Builder builder = DataPb.Status.newBuilder();
		builder.setK(key);
		builder.setV(value);
		return builder.build();
	}

	static public CommonPb.ActRank createActRank(int country, long lordId, int rank, String nick, long value) {
		CommonPb.ActRank.Builder builder = CommonPb.ActRank.newBuilder();
		builder.setCountry(country);
		builder.setLordId(lordId);
		builder.setRank(rank);
		builder.setName(nick);
		builder.setValue(value);
		return builder.build();
	}

	static public CommonPb.ActRank createActRank(int country, long lordId, int rank, String nick, long value, long fight) {
		CommonPb.ActRank.Builder builder = CommonPb.ActRank.newBuilder();
		builder.setCountry(country);
		builder.setLordId(lordId);
		builder.setRank(rank);
		builder.setName(nick);
		builder.setValue(value);
		builder.setFight(fight);
		return builder.build();
	}

	static public CommonPb.ActRank createActRank(Player player, CampMembersRank campMember) {
		CommonPb.ActRank.Builder builder = CommonPb.ActRank.newBuilder();
		builder.setCountry(campMember.getCountry());
		builder.setLordId(campMember.getLordId());
		builder.setRank(campMember.getRank());
		builder.setName(player.getNick());
		builder.setValue(campMember.getFight());
		builder.setFight(campMember.getFightMax());
		return builder.build();
	}

	static public CommonPb.ActDial createActDial(StaticActDial staticActDial, int got) {
		CommonPb.ActDial.Builder builder = CommonPb.ActDial.newBuilder();
		builder.setDialId(staticActDial.getDialId());
		builder.setPlace(staticActDial.getPlace());
		builder.setItemType(staticActDial.getItemType());
		builder.setItemId(staticActDial.getItemId());
		builder.setItemCount(staticActDial.getItemCount());
		builder.setType(staticActDial.getType());
		builder.setGot(got);
		builder.setEquipId(staticActDial.getEquipId());
		return builder.build();
	}

	static public CommonPb.ActDial createActDial(StaticActDial staticActDial) {
		CommonPb.ActDial.Builder builder = CommonPb.ActDial.newBuilder();
		builder.setDialId(staticActDial.getDialId());
		builder.setPlace(staticActDial.getPlace());
		builder.setItemType(staticActDial.getItemType());
		builder.setItemId(staticActDial.getItemId());
		builder.setItemCount(staticActDial.getItemCount());
		builder.setType(staticActDial.getType());
		return builder.build();
	}

	static public CommonPb.ActTask createActTask(StaticActTask staticActTask, int state) {
		CommonPb.ActTask.Builder builder = CommonPb.ActTask.newBuilder();
		builder.setTaskId(staticActTask.getTaskId());
		builder.setCond(staticActTask.getProcess());
		builder.setState(state);
		return builder.build();
	}

	static public CommonPb.ActShop createActShop(ActShopProp actShopProp) {
		CommonPb.ActShop.Builder builder = CommonPb.ActShop.newBuilder();
		builder.setGrid(actShopProp.getGrid());
		builder.setPropId(actShopProp.getPropId());
		builder.setPropCount(actShopProp.getPropNum());
		builder.setPrice(actShopProp.getPrice());
		builder.setBuy(actShopProp.getIsBuy());
		return builder.build();
	}

	static public CommonPb.GrowFoot.Builder createGrowFoot(int footId, int type, int state) {
		CommonPb.GrowFoot.Builder builder = CommonPb.GrowFoot.newBuilder();
		builder.setFootId(footId);
		builder.setType(type);
		builder.setState(state);
		return builder;
	}

	static public CommonPb.CountryTask.Builder createTask(CtyTask ctyTask) {
		CommonPb.CountryTask.Builder builder = CommonPb.CountryTask.newBuilder();
		builder.setTaskId(ctyTask.getTaskId());
		builder.setCond(ctyTask.getCond());
		builder.setState(ctyTask.getState());
		return builder;
	}

	static public CommonPb.CountryHero.Builder createHero(int heroId, int heroLv, String nick) {
		CommonPb.CountryHero.Builder builder = CommonPb.CountryHero.newBuilder();
		builder.setHeroId(heroId);
		if (heroLv > 0) {
			builder.setLevel(heroLv);
		}
		if (nick != null) {
			builder.setNick(nick);
		}
		return builder;
	}

	static public CommonPb.CountryRank.Builder createCountryRank(int rankValue, int rank, String nick, int vote) {
		CommonPb.CountryRank.Builder builder = CommonPb.CountryRank.newBuilder();
		builder.setRank(rank);
		builder.setRankValue(rankValue);
		builder.setNick(nick);
		builder.setVote(vote);
		return builder;
	}

	static public CommonPb.Govern.Builder createGovern(CtyVote ctyVote, int lv, String nick) {
		CommonPb.Govern.Builder builder = CommonPb.Govern.newBuilder();
		builder.setLordId(ctyVote.getLordId());
		builder.setLevel(lv);
		builder.setNick(nick);
		builder.setVote(ctyVote.getVote());
		return builder;
	}

	static public CommonPb.Govern.Builder createGovern(CtyGovern ctyGovern, Player player) {
		CommonPb.Govern.Builder builder = CommonPb.Govern.newBuilder();
		builder.setLordId(ctyGovern.getLordId());
		builder.setLevel(player.getLevel());
		builder.setNick(player.getNick());
		builder.setArea(player.getLord().getMapId());
		builder.setVote(ctyGovern.getVote());
		builder.setOnline(player.isLogin ? 1 : 0);
		return builder;
	}

	static public CommonPb.CountryDaily.Builder createDaily(CtyDaily ctyDaily) {
		CommonPb.CountryDaily.Builder builder = CommonPb.CountryDaily.newBuilder();
		builder.setDailyId(ctyDaily.getDailyId());
		builder.setCreateTime(ctyDaily.getTime());
		return builder;
	}

	static public List<CommonPb.Award> createListAward(List<List<Integer>> list) {
		List<CommonPb.Award> rt = new ArrayList<CommonPb.Award>();
		for (List<Integer> e : list) {
			if (e.size() >= 3) {
				rt.add(createAward(e.get(0), e.get(1), e.get(2)).build());
			}
		}
		return rt;
	}

	static public List<CommonPb.Award> createAwardList(List<Award> list) {
		List<CommonPb.Award> rt = new ArrayList<CommonPb.Award>();
		for (Award e : list) {
			rt.add(createAward(e.getType(), e.getId(), e.getCount()).build());
		}

		return rt;
	}

	static public List<CommonPb.Award> createAwardList1(Player player, List<Award> list) {
		List<CommonPb.Award> rt = new ArrayList<CommonPb.Award>();
		for (Award e : list) {
			if (e.getKeyId() == 0) {
				rt.add(createAward(e.getType(), e.getId(), e.getCount()).build());
			} else {
				rt.add(createAward(player, e.getType(), e.getId(), e.getCount(), e.getKeyId()).build());
			}
		}

		return rt;
	}

	public static CommonPb.MapCity.Builder createMapCity(City city) {
		CommonPb.MapCity.Builder builder = CommonPb.MapCity.newBuilder();
		builder.setCityId(city.getCityId());
		builder.setCityLv(city.getCityLv());
		builder.setCountry(city.getCountry());
		return builder;
	}

	public static List<CommonPb.LevelupAwards> createLevelAwards(Player player) {
		List<CommonPb.LevelupAwards> rt = new ArrayList<CommonPb.LevelupAwards>();
		Map<Integer, LevelAward> levelAwards = player.getLevelAwardsMap();
		LevelAward levelAward = levelAwards.values().stream().filter(x -> x.getStatus() == 0).sorted(Comparator.comparing(LevelAward::getLevel)).findFirst().orElse(null);
		if (levelAward != null) {
			rt.add(levelAward.wrapPb());
		}
		return rt;
	}

	static public List<DataPb.AwardData> createAwardDataList(List<Award> list) {
		List<DataPb.AwardData> rt = new ArrayList<DataPb.AwardData>();
		for (Award e : list) {
			if (e == null) {
				continue;
			}
			rt.add(e.writeData().build());
		}

		return rt;
	}

	static public CommonPb.ManBlack createManBlack(long lordId, int level, int portrait, int country, String nick, int officer, int title, int maxScore) {
		CommonPb.ManBlack.Builder builder = CommonPb.ManBlack.newBuilder();
		builder.setLordId(lordId);
		builder.setLevel(level);
		builder.setNick(nick);
		builder.setPortrait(portrait);
		builder.setCountry(country);
		builder.setOfficer(officer);
		builder.setTitle(title);
		builder.setMaxScore(maxScore);
		return builder.build();

	}

	static public CommonPb.MailReply createMailReply(long replyTime, String msg, String[] param) {
		CommonPb.MailReply.Builder builder = CommonPb.MailReply.newBuilder();
		builder.setTime(replyTime);
		builder.setMsg(msg);
		for (String p : param) {
			builder.addTitleParam(p);
		}
		return builder.build();
	}

	static public CommonPb.Quota createPayGift(StaticActPayGift staticActPayGift, int state) {
		CommonPb.Quota.Builder builder = CommonPb.Quota.newBuilder();
		builder.setQuotaId(staticActPayGift.getPayGiftId());
		builder.setDisplay(staticActPayGift.getDisplay());
		builder.setPrice(staticActPayGift.getMoney());
		builder.setCount(staticActPayGift.getCount());
		builder.setBuy(state);
		List<List<Integer>> sellList = staticActPayGift.getSellList();
		for (List<Integer> e : sellList) {
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	//    private int awardId;
	//    private String name;
	//    private int vipExp;
	//    private int productType;
	//    private int period;
	//    private String asset;

	static public CommonPb.Quota createPayArms(StaticActPayArms staticActPayArms, int state) {
		CommonPb.Quota.Builder builder = CommonPb.Quota.newBuilder();
		builder.setQuotaId(staticActPayArms.getPayArmsId());
		builder.setDisplay(staticActPayArms.getOriginal());
		builder.setPrice(staticActPayArms.getTopup());
		builder.setCount(staticActPayArms.getLimit());
		builder.setBuy(state);
		builder.setDesc(staticActPayArms.getDesc().replace("%s", staticActPayArms.getScore() + ""));
		List<List<Integer>> sellList = staticActPayArms.getSellList();
		for (List<Integer> e : sellList) {
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			builder.addAward(createAward(type, id, count));
		}
		return builder.build();
	}

	static public CommonPb.ActDialRank createActDialRank(int rank, String nick, int score, List<CommonPb.Award> list) {
		CommonPb.ActDialRank.Builder builder = CommonPb.ActDialRank.newBuilder();
		builder.setRank(rank);
		builder.setName(nick);
		builder.setScore(score);
		for (CommonPb.Award e : list) {
			builder.addAward(e);
		}
		return builder.build();
	}

	public static CommonPb.RankInfo createBeautyRank(Lord lord, int rank, int sorce) {
		CommonPb.RankInfo.Builder builder = CommonPb.RankInfo.newBuilder();
		builder.setRank(rank);
		builder.setName(lord.getNick());
		builder.setLevel(lord.getLevel());
		builder.setCountry(lord.getCountry());
		builder.setTitle(lord.getTitle());
		builder.setBattleSocre(sorce);
		builder.setLordId(lord.getLordId());
		builder.setPortrait(lord.getPortrait());
		return builder.build();
	}

	public static List<Award> finilAward(List<CommonPb.Award> awards) {
		List<Award> newAwards = new ArrayList<>();
		for (CommonPb.Award award1 : awards) {
			boolean add = true;
			if (newAwards == null || newAwards.size() == 0) {
				newAwards.add(new Award(award1));
			} else {
				ListIterator<Award> iterator = newAwards.listIterator();
				while (iterator.hasNext()) {
					Award award2 = iterator.next();
					if (award1.getType() == award2.getType() && award1.getId() == award2.getId()) {
						award2.setCount(award2.getCount() + award1.getCount());
						add = false;
					}
				}
				if (add) {
					iterator.add(new Award(award1));
				}
			}

		}
		return newAwards;
	}

	public static List<Award> finilAward1(List<CommonPb.Award> awards) {
		List<Award> newAwards = new ArrayList<>();
		for (CommonPb.Award award1 : awards) {
			boolean add = true;
			if (newAwards == null || newAwards.size() == 0) {
				newAwards.add(new Award(award1));
			} else {
				ListIterator<Award> iterator = newAwards.listIterator();
				while (iterator.hasNext()) {
					Award award2 = iterator.next();
					if (award1.getKeyId() == award2.getKeyId() && award1.getType() == award2.getType() && award1.getId() == award2.getId()) {
						award2.setCount(award2.getCount() + award1.getCount());
						add = false;
					}
				}
				if (add) {
					iterator.add(new Award(award1));
				}
			}

		}
		return newAwards;
	}

	public static CommonPb.Good createGood(Player player, int type) {
		CommonPb.Good.Builder builder = CommonPb.Good.newBuilder();
		builder.setLordId(player.roleId);
		builder.setCaculateScorce(player.getMaxScore());
		builder.setCountry(player.getCountry());
		builder.setLevel(player.getLevel());
		builder.setNick(player.getNick());
		builder.setType(type);
		builder.setPortrait(player.getLord().getPortrait());
		builder.setTitle(player.getTitle());
		boolean isLogin = player.isLogin;
		builder.setOfflineTime(isLogin ? System.currentTimeMillis() : player.getLord().getOffTime());
		CountryManager countryManager = SpringUtil.getBean(CountryManager.class);
		CtyGovern ctyGovern = countryManager.getGovern(player);
		int govern = ctyGovern == null ? 0 : ctyGovern.getGovernId();
		builder.setOffice(govern);
		builder.setHeadSculpture(player.getLord().getHeadIndex());
		builder.setOnline(player.isOnline());
		builder.setHeadIndex(player.getLord().getHeadIndex());
		return builder.build();

	}

	public static CommonPb.FirstBloodInfo createFirstBloodInfo(FirstBloodInfo info) {
		CommonPb.FirstBloodInfo firstBloodInfo = CommonPb.FirstBloodInfo.newBuilder()
			.setCountry(info.getCountry())
			.setMapId(info.getMapId())
			.setNick(info.getNick())
			.setPortrait(info.getPortrait()).build();

		return firstBloodInfo;
	}

	public static ActWashEquipt createActWashEquipt(StaticActEquipUpdate update) {
		List<CommonPb.Award> awards = new ArrayList<>();
		List<List<Integer>> list = update.getAwardList();
		for (List<Integer> i : list) {
			awards.add(createAward(i.get(0), i.get(1), i.get(2)).build());
		}

		ActWashEquipt actWashEquipt = ActWashEquipt.newBuilder()
			.setKeyId(update.getKeyId())
			.setAwardId(update.getAwardId())
			.setCond(update.getCond())
			.setCost(update.getCost())
			.setDesc(update.getDesc())
			.setParam(update.getParam())
			.setSortId(update.getSortId())
			.setType(update.getType())
			.addAllAward(awards).build();
		return actWashEquipt;

	}

	public static CommonPb.ActPassPortPayItem createStaticPayPassPort(StaticPayPassPort staticPayPassPort) {
		CommonPb.ActPassPortPayItem.Builder payItem = CommonPb.ActPassPortPayItem.newBuilder();
		List<List<Integer>> selllist = staticPayPassPort.getSellList();
		for (List<Integer> i : selllist) {
			payItem.addSellAward(createAward(i.get(0), i.get(1), i.get(2)).build());
		}

		List<List<Integer>> viewList = staticPayPassPort.getViewList();
		for (List<Integer> i : viewList) {
			payItem.addViewAward(createAward(i.get(0), i.get(1), i.get(2)).build());
		}
		payItem.setKeyId(staticPayPassPort.getKeyId());
		payItem.setAwardId(staticPayPassPort.getAwardId());
		payItem.setPrice(staticPayPassPort.getMoney());
		return payItem.build();
	}

	public static CommonPb.Depot depot(Depot depot) {
		CommonPb.Depot.Builder builder = CommonPb.Depot.newBuilder();
		builder.setGrid(depot.getGrid());
		builder.setState(depot.getState());
		Award award = depot.getAward();
		builder.setAward(createAward(award.getType(), award.getId(), award.getCount()));
		if (depot.getIron() != 0) {
			builder.setIron(depot.getIron());
		}
		if (depot.getGold() != 0) {
			builder.setGold(depot.getGold());
		}

		return builder.build();
	}

	public static <T> Base.Builder createRsBase(GameError gameError, GeneratedExtension<Base, T> ext, T msg, ClientHandler clientHandler) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(clientHandler.getRsMsgCmd());
		baseBuilder.setCode(gameError.getCode());
		if (clientHandler.getMsg().hasParam()) {
			baseBuilder.setParam(clientHandler.getMsg().getParam());
		}

		if (clientHandler.getMsg().hasIndex()) {
			baseBuilder.setIndex(clientHandler.getMsg().getIndex());
		}

		if (msg != null) {
			baseBuilder.setExtension(ext, msg);
		}

		return baseBuilder;
	}

	public static <T> Base.Builder createRsBase(GameError gameError, GeneratedExtension<Base, T> ext, T msg, int rsMsgCmd) {
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setCommand(rsMsgCmd);
		baseBuilder.setCode(gameError.getCode());
		if (msg != null) {
			baseBuilder.setExtension(ext, msg);
		}
		return baseBuilder;
	}
}
