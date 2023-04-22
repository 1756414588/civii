package com.game.domain.p;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.game.constant.HeroState;
import com.game.domain.s.StaticHero;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;

public class Hero implements Cloneable {
	private int heroId; // 英雄Id
	private int heroLv; // 英雄等级
	private long exp; // 英雄经验
	private int currentSoliderNum; // 当前兵力,打完世界地图之后兵力减少,需要补兵
	private Property qualifyProp; // 总资质(基础(配置)+洗练属性)
	private ArrayList<HeroEquip> heroEquips; // 装备
	private Property totalProp; // 总属性
	private int advanceProcess; // 突破值
	private int advanceTime; // 突破时间,存放当天几号
	private int tryAdvanceTimes; // 尝试突破的次数
	private Property specialProp;
	private int activate; // 英雄是否激活[只对国家名将有效],0表示已激活 3表示未激活
	private int loyalty; // 忠诚度
	private ArrayList<HeroBook> heroBooks; // 兵书
	private int diviNum; // 晋升次数
	private int talentLevel;// 天赋技能
	// 二级属性,攻城、守城、闪避 - 战斗时进行计算
	private int type;// 0.普通英雄 1赛季英雄
	private int skillId;
	//private int skillType;
	//private int skillLevel;
	private int profId;

	public Hero() {
		qualifyProp = new Property();
		heroEquips = new ArrayList<HeroEquip>();
		totalProp = new Property();
		specialProp = new Property();
		heroBooks = new ArrayList<HeroBook>();
	}

	// public Hero(int heroId) {
	//
	// }

	private void cloneEquip(ArrayList<HeroEquip> heroEquips) {
		if (heroEquips == null) {
			LogHelper.ERROR_LOGGER.trace("Equip:HeroEquip == null");
			return;
		}

		for (HeroEquip item : heroEquips) {
			if (item == null) {
				LogHelper.ERROR_LOGGER.trace("cloneEquip:item == null");
				continue;
			}
			this.addHeroEquip(item.cloneInfo());
		}
	}

	private void cloneBook(ArrayList<HeroBook> heroBooks) {
		if (heroBooks == null) {
			LogHelper.ERROR_LOGGER.trace("WarBook:HeroBooks == null");
			return;
		}

		for (HeroBook item : heroBooks) {
			if (item == null) {
				LogHelper.ERROR_LOGGER.trace("cloneBook:item == null");
				continue;
			}
			this.addHerobook(item.cloneInfo());
		}
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}

	public int getHeroLv() {
		return heroLv;
	}

	public void setHeroLv(int heroLv) {
		this.heroLv = heroLv;
	}

	public long getExp() {
		return exp;
	}

	public void setExp(long exp) {
		this.exp = exp;
	}

	public int getCurrentSoliderNum() {
		return currentSoliderNum;
	}

	public void setCurrentSoliderNum(int currentSoliderNum) {
		this.currentSoliderNum = currentSoliderNum;
	}

	public int getDiviNum() {
		return diviNum;
	}

	public void setDiviNum(int diviNum) {
		this.diviNum = diviNum;
	}

//    public Hero cloneInfo() {
//        Hero hero = new Hero();
//        hero.setHeroId(this.heroId);
//        hero.setHeroLv(this.heroLv);
//        hero.setExp(this.exp);
//        hero.setCurrentSoliderNum(this.currentSoliderNum);
//        hero.setQualifyProp(qualifyProp.clone());
//        hero.setAdvanceProcess(advanceProcess);
//        hero.cloneEquip(getHeroEquips());
//        hero.specialProp = specialProp.cloneInfo();
//        hero.setActivate(activate);
//        hero.setLoyalty(this.loyalty);
//        hero.cloneBook(this.heroBooks);
//        return hero;
//    }

	public CommonPb.Hero.Builder wrapPb() {
		CommonPb.Hero.Builder builder = CommonPb.Hero.newBuilder();
		builder.setHeroId(heroId);
		builder.setLv(heroLv);
		builder.setExp(exp);
		builder.setCurrentSoliderNum(currentSoliderNum);
		builder.setQualifyProp(getQualifyProp().wrapPb());
		// Property
		builder.setProperty(totalProp.wrapPb());

		// equip
		for (HeroEquip item : heroEquips) {
			if (item == null) {
				continue;
			}

			builder.addHeroEquip(item.wrapPb());
		}

		// book
		for (HeroBook item : heroBooks) {
			if (item == null) {
				continue;
			}

			builder.addHeroBook(item.wrapPb());
		}

		// 每日刷新的时候需要特殊处理一下
		int day = GameServer.getInstance().currentDay;
		if (day != advanceTime) {
			advanceTime = day;
			advanceProcess = 0;
		}

		builder.setAdvanceProcess(advanceProcess);
		if (!specialProp.isInit()) {
			builder.setSpecial(specialProp.wrapPb());
		}

		builder.setActivate(activate);
		builder.setLoyalty(loyalty);
		builder.setDiviNum(this.diviNum);
		builder.setTelnetLv(this.talentLevel);
		builder.setType(this.type);
		builder.setSkillId(this.skillId);
		//builder.setSkillLevel(this.skillLevel);
		builder.setProfId(this.profId);
		return builder;
	}

	public Property getQualifyProp() {
		return qualifyProp;
	}

	public void setQualifyProp(Property qualifyProp) {
		this.qualifyProp = qualifyProp;
	}

	public ArrayList<HeroEquip> getHeroEquips() {
		return heroEquips;
	}

	public void setHeroEquips(ArrayList<HeroEquip> heroEquips) {
		this.heroEquips = heroEquips;
	}

	public void addHeroEquip(HeroEquip heroEquip) {
		if (heroEquip != null && heroEquips != null) {
			heroEquips.add(heroEquip);
		}
	}

	public void addHerobook(HeroBook heroBook) {
		if (heroBook != null && heroBooks != null) {
			heroBooks.add(heroBook);
		}
	}

	public void init(StaticHero staticHero) {
		heroId = staticHero.getHeroId();
		heroLv = staticHero.getLevel();
		exp = 0;
		currentSoliderNum = 0;
		// 总资质
		qualifyProp.setAttack(staticHero.getAttack());
		qualifyProp.setDefence(staticHero.getDefence());
		qualifyProp.setSoldierNum(staticHero.getSoldierCount());
		// 总属性
		Property property = new Property();
		property.setAttack(staticHero.getBaseAttack());
		property.setDefence(staticHero.getBaseDefence());
		property.setSoldierNum(staticHero.getBaseSoldierCount());

		totalProp = property;

		this.type = staticHero.getCompseason();
		this.skillId = staticHero.getCompseasonSkill();
		//this.skillType = staticHero.getSkillType();
		//this.skillLevel = 0;

		this.profId = 101;
	}

	public Property getTotalProp() {
		return totalProp;
	}

	public HeroEquip removeEquip(int pos) {
		for (int i = 0; i < heroEquips.size(); ++i) {
			HeroEquip heroEquip = heroEquips.get(i);
			if (heroEquip == null) {
				continue;
			}

			if (heroEquip.getPos() == pos) {
				heroEquips.remove(i);
				return heroEquip;
			}
		}
		return null;
	}

	public HeroBook removeBook(int pos) {
		for (int i = 0; i < heroBooks.size(); ++i) {
			HeroBook heroBook = heroBooks.get(i);
			if (heroBook == null) {
				continue;
			}

			if (heroBook.getPos() == pos) {
				heroBooks.remove(i);
				return heroBook;
			}
		}
		return null;
	}

	public HeroEquip getEquipByUId(int uniqueId) {
		for (int i = 0; i < heroEquips.size(); ++i) {
			HeroEquip heroEquip = heroEquips.get(i);
			if (heroEquip == null) {
				continue;
			}

			Equip equip = heroEquip.getEquip();
			if (equip == null) {
				continue;
			}

			if (equip.getKeyId() == uniqueId) {
				return heroEquip;
			}
		}

		return null;
	}

	public HeroBook getBookByUId(int uniqueId) {
		for (int i = 0; i < heroBooks.size(); ++i) {
			HeroBook heroBook = heroBooks.get(i);
			if (heroBook == null) {
				continue;
			}

			WarBook book = heroBook.getBook();
			if (book == null) {
				continue;
			}

			if (book.getKeyId() == uniqueId) {
				return heroBook;
			}
		}

		return null;
	}

	public void swapEquip(Hero param) {
		ArrayList<HeroEquip> list1 = param.getHeroEquips();
		List<HeroEquip> tmpList = new ArrayList<HeroEquip>(list1);
		list1.clear();
		list1.addAll(heroEquips);
		heroEquips.clear();
		heroEquips.addAll(tmpList);
	}

	public void swapBook(Hero param) {
		ArrayList<HeroBook> list1 = param.getHeroBooks();
		List<HeroBook> tmpList = new ArrayList<HeroBook>(list1);
		list1.clear();
		list1.addAll(heroBooks);
		heroBooks.clear();
		heroBooks.addAll(tmpList);
	}

	public void clearEquip() {
		heroEquips.clear();
	}

	public void clearBook() {
		heroBooks.clear();
	}

	public CommonPb.HeroInfo.Builder wrapHeroInfo() {
		CommonPb.HeroInfo.Builder builder = CommonPb.HeroInfo.newBuilder();
		builder.setExp(exp);
		builder.setHeroId(heroId);
		builder.setLv(heroLv);
		return builder;
	}

	public int getAdvanceProcess() {
		int day = GameServer.getInstance().currentDay;
		if (day != advanceTime) {
			advanceTime = day;
			advanceProcess = 0;
		}
		return advanceProcess;
	}

	public void setAdvanceProcess(int advanceProcess) {
		this.advanceProcess = advanceProcess;
	}

	public void cloneHeroEquip(Hero hero) {
		ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
		if (heroEquips == null) {
			LogHelper.ERROR_LOGGER.trace("Equip:HeroEquip == null");
			return;
		}

		for (HeroEquip item : heroEquips) {
			if (item == null) {
				LogHelper.ERROR_LOGGER.trace("cloneEquip:item == null");
				continue;
			}
			this.addHeroEquip(item.cloneInfo());
		}
	}

	public void cloneHeroBook(Hero hero) {
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (heroBooks == null) {
			LogHelper.ERROR_LOGGER.trace("Book:HeroBook == null");
			return;
		}

		for (HeroBook item : heroBooks) {
			if (item == null) {
				LogHelper.ERROR_LOGGER.trace("cloneBook:item == null");
				continue;
			}
			this.addHerobook(item.cloneInfo());
		}
	}

	public int getAdvanceTime() {
		return advanceTime;
	}

	public void setAdvanceTime(int advanceTime) {
		this.advanceTime = advanceTime;
	}

	public int getSoldierNum() {
		if (totalProp != null) {
			return totalProp.getSoldierNum();
		}

		LogHelper.ERROR_LOGGER.error("totalProp is null!");

		return 0;
	}

	public boolean hasEquip(int equipId) {
		for (HeroEquip heroEquip : heroEquips) {
			if (heroEquip == null) {
				continue;
			}

			Equip equip = heroEquip.getEquip();
			if (equip == null) {
				continue;
			}

			if (equip.getEquipId() == equipId) {
				return true;
			}
		}

		return false;
	}

	public int getTryAdvanceTimes() {
		return tryAdvanceTimes;
	}

	public void setTryAdvanceTimes(int tryAdvanceTimes) {
		this.tryAdvanceTimes = tryAdvanceTimes;
	}

	public DataPb.HeroData.Builder writeData() {
		DataPb.HeroData.Builder builder = DataPb.HeroData.newBuilder();
		builder.setHeroId(heroId);
		builder.setLv(heroLv);
		builder.setExp(exp);
		builder.setCurrentSoliderNum(currentSoliderNum);
		builder.setQualifyProp(getQualifyProp().writeData());
		// Property
		builder.setProperty(totalProp.writeData());
		// equip
		for (HeroEquip item : heroEquips) {
			if (item == null) {
				continue;
			}

			builder.addEquip(item.writeData());
		}

		// book
		for (HeroBook item : heroBooks) {
			if (item == null) {
				continue;
			}

			builder.addBook(item.writeData());
		}

		// 每日刷新的时候需要特殊处理一下
		int day = GameServer.getInstance().currentDay;
		if (day != advanceTime) {
			advanceTime = day;
			advanceProcess = 0;
		}
		builder.setAdvanceProcess(advanceProcess);
		builder.setSpecial(specialProp.writeData());
		builder.setActivate(activate);
		builder.setLoyalty(loyalty);
		builder.setDiviNum(this.diviNum);
		builder.setTelnetLv(this.talentLevel);

		builder.setType(this.type);
		builder.setSkillId(this.skillId);
		//builder.setSkillLevel(this.skillLevel);
		builder.setProfId(this.profId);
		return builder;
	}

	public void readData(DataPb.HeroData heroPb) {
		// 基本信息
		setHeroId(heroPb.getHeroId());
		setHeroLv(heroPb.getLv());
		setExp(heroPb.getExp());
		setCurrentSoliderNum(heroPb.getCurrentSoliderNum());
		// 品质
		if (heroPb.hasQualifyProp()) {
			Property qualification = getQualifyProp();
			qualification.readData(heroPb.getQualifyProp());
		}

		// 属性
		DataPb.PropertyData propertyPb = heroPb.getProperty();
		if (propertyPb != null) {
			Property property = new Property();
			property.readData(propertyPb);
			totalProp = property;
		}

		// 装备
		for (DataPb.HeroEquipData item : heroPb.getEquipList()) {
			if (item == null) {
				continue;
			}

			HeroEquip heroEquip = new HeroEquip();
			heroEquip.readData(item);
			addHeroEquip(heroEquip);
		}

		// 兵书
		for (DataPb.HeroBookData item : heroPb.getBookList()) {
			if (item == null) {
				continue;
			}

			HeroBook heroBook = new HeroBook();
			heroBook.readData(item);
			addHerobook(heroBook);
		}
		advanceProcess = heroPb.getAdvanceProcess();
		this.diviNum = heroPb.getDiviNum();
		DataPb.PropertyData specialData = heroPb.getSpecial();
		if (specialData != null) {
			Property property = new Property();
			property.readData(specialData);
			specialProp = property;
			if (specialProp.isInit()) {
				this.diviNum = 0;
			}
			if (!specialProp.isInit() && this.diviNum == 0) {
				this.diviNum = 1;
			}
		}
		activate = heroPb.getActivate();
		loyalty = heroPb.getLoyalty();
		this.talentLevel = heroPb.getTelnetLv();
		this.type = heroPb.getType();
		this.skillId = heroPb.getSkillId();
		//this.skillLevel = heroPb.getSkillLevel();
		this.profId = heroPb.getProfId();

	}

	public CommonPb.HeroChange.Builder createHeroChange() {
		CommonPb.HeroChange.Builder builder = CommonPb.HeroChange.newBuilder();
		builder.setHeroId(heroId);
		builder.setSoldier(currentSoliderNum);
		builder.setLv(heroLv);
		builder.setExp(exp);
		return builder;
	}

	public void addSoldierNum(int add) {
		if (add <= 0) {
			return;
		}
		currentSoliderNum += add;
		currentSoliderNum = Math.min(getSoldierNum(), currentSoliderNum);
	}

	public void fixDb() {
//        ArrayList<HeroEquip> heroEquips
		Iterator<HeroEquip> iterator = heroEquips.iterator();
		while (iterator.hasNext()) {
			HeroEquip heroEquip = iterator.next();
			if (heroEquip == null) {
				continue;
			}
			Equip equip = heroEquip.getEquip();
			if (equip == null) {
				continue;
			}
			if (equip.getKeyId() == 0) {
				iterator.remove();
			}
		}

		Iterator<HeroBook> iteratorBook = heroBooks.iterator();
		while (iteratorBook.hasNext()) {
			HeroBook heroBook = iteratorBook.next();
			if (heroBook == null) {
				continue;
			}
			WarBook book = heroBook.getBook();
			if (book == null) {
				continue;
			}
			if (book.getKeyId() == 0) {
				iteratorBook.remove();
			}
		}
	}

	public Property getSpecialProp() {
		return specialProp;
	}

	public void setSpecialProp(Property specialProp) {
		this.specialProp = specialProp;
	}

	public int isActivate() {
		return activate;
	}

	public void setActivate(int activate) {
		this.activate = activate;
	}

	public boolean isActivated() {
		return activate == HeroState.ACTIVATE;
	}

	public int getLoyalty() {
		return loyalty;
	}

	public void setLoyalty(int loyalty) {
		this.loyalty = loyalty;
	}

	public void addLoyalty(int param) {
		if (param <= 0) {
			return;
		}
		loyalty += param;
		loyalty = Math.max(0, loyalty);
		loyalty = Math.min(100, loyalty);

	}

	public void subLoyalty(int param) {
		if (param <= 0) {
			return;
		}
		loyalty -= param;
		loyalty = Math.max(0, loyalty);
		loyalty = Math.min(100, loyalty);
	}

	public ArrayList<HeroBook> getHeroBooks() {
		return heroBooks;
	}

	public void setHeroBooks(ArrayList<HeroBook> heroBooks) {
		this.heroBooks = heroBooks;
	}

	public void setTotalProp(Property totalProp) {
		this.totalProp = totalProp;
	}

	@Override
	public Hero clone() {
		Hero hero = null;
		try {
			hero = (Hero) super.clone();
			hero.setQualifyProp(this.qualifyProp.clone());

			ArrayList<HeroEquip> list1 = new ArrayList<>();
			this.heroEquips.forEach(heroEquip -> {
				list1.add(heroEquip.clone());
			});
			hero.setHeroEquips(list1);

			hero.setTotalProp(this.totalProp.clone());
			hero.setSpecialProp(this.specialProp.clone());

			ArrayList<HeroBook> list2 = new ArrayList<>();
			this.heroBooks.forEach(heroBook -> {
				list2.add(heroBook.clone());
			});
			hero.setHeroBooks(list2);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return hero;
	}

	public int getTalentLevel() {
		return talentLevel;
	}

	public void setTalentLevel(int talentLevel) {
		this.talentLevel = talentLevel;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	// public int getSkillId() {
	// return skillId;
	// }
	//
	// public void setSkillId(int skillId) {
	// this.skillId = skillId;
	// }

	//public int getSkillType() {
	//	return skillType;
	//}
	//
	//public void setSkillType(int skillType) {
	//	this.skillType = skillType;
	//}
	//
	//public int getSkillLevel() {
	//	return skillLevel;
	//}
	//
	//public void setSkillLevel(int skillLevel) {
	//	this.skillLevel = skillLevel;
	//}

	public int getProfId() {
		return profId;
	}

	public void setProfId(int profId) {
		this.profId = profId;
	}

	public int getSkillId() {
		return skillId;
	}

	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}
}
