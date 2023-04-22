package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.LogUser;
import com.game.log.domain.WarBookLog;
import com.game.pb.CommonPb;
import com.game.service.AchievementService;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.TimeHelper;
import com.game.util.random.WeightRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CaoBing
 * @date 2020/12/11 17:28
 */
@Component
public class WarBookManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticWarBookMgr staticWarBookMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

 /* @Autowired
    private HeroManager heroDataManager;*/

	@Autowired
	private StaticHeroMgr staticHeroDataMgr;

	@Autowired
	private StaticMissionMgr staticMissionMgr;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private LogUser logUser;

	/**
	 * 通过ID生成兵书
	 *
	 * @param warBookId
	 * @return
	 */
	//public WarBook addWarBookById(Player player, int warBookId, int reason) {
	//	WarBook book = new WarBook();
	//	Map<Integer, WarBook> warBookMap = player.getWarBooks();
	//	book.setKeyId(player.maxKey());
	//	book.setBookId(warBookId);
	//	StaticWarBook warBookConfigById = staticWarBookMgr.getWarBookConfigById(warBookId);
	//	if (warBookConfigById == null) {
	//		logger.error("no WarBook config, bookId = " + warBookId + ", reason = " + reason);
	//		return book;
	//	}
	//	initBookPropAndSkill(book, warBookConfigById);
	//	warBookMap.put(book.getKeyId(), book);
	//	logUser.war_book_log(WarBookLog.builder()
	//		.lordId(player.roleId)
	//		.level(player.getLevel())
	//		.nick(player.getNick())
	//		.vip(player.getVip())
	//		.reason(reason)
	//		.bookName(warBookConfigById.getName())
	//		.cost(0)
	//		.build());
	//	return book;
	//}
	@Autowired
	AchievementService achievementService;


	public WarBook addWarBookById(Player player, int warBookId, int reason, int awardType, long count) {
		WarBook book = null;
		boolean flag = false;
		// WarBookPb.DecompoundWarBookRs.Builder builder = WarBookPb.DecompoundWarBookRs.newBuilder();
		List<CommonPb.Award> awardList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			book = new WarBook();
			Map<Integer, WarBook> warBookMap = player.getWarBooks();
			book.setKeyId(player.maxKey());
			book.setBookId(warBookId);
			StaticWarBook warBookConfigById = staticWarBookMgr.getWarBookConfigById(warBookId);
			if (warBookConfigById == null) {
				LogHelper.CONFIG_LOGGER.info("no WarBook config, bookId = " + warBookId + ", reason = " + reason);
				return book;
			}
			initBookPropAndSkill(book, warBookConfigById, awardType);
			warBookMap.put(book.getKeyId(), book);
			if(warBookConfigById.getQuality()==5){
				achievementService.addAndUpdate(player,AchiType.AT_8,1);
			}
			int num = 800;
			int num1 = staticLimitMgr.getNum(SimpleId.MAX_BOOK_NUM);
			if (num1 > 0) {
				num = num1;
			}
			if (warBookMap.size() > num) {
				Iterator<WarBook> iterator = warBookMap.values().iterator();
				while (iterator.hasNext()) {
					WarBook next = iterator.next();
					StaticWarBook warBookConfigById1 = staticWarBookMgr.getWarBookConfigById(next.getBookId());
					if (warBookConfigById1.getQuality() < 4 && book.getIsLock() != 1) {
						deCompound(player, next, awardList);
						// builder.addKeyId(next.getKeyId());
						iterator.remove();
						flag = true;
					}
				}
			}
			logUser.war_book_log(WarBookLog.builder().lordId(player.roleId).level(player.getLevel()).nick(player.getNick()).vip(player.getVip()).reason(reason).bookName(warBookConfigById.getName()).cost(0).build());

		}
		if (flag) {
			playerManager.sendAttachMail(player, null, MailId.WARBOOK_MAX);
//			List<Award> awardListsFinal = PbHelper.finilAward1(awardList);// 合并奖励
//			builder.addAllAward(PbHelper.createAwardList1(player, awardListsFinal));
//			player.sendMsgToPlayer(WarBookPb.DecompoundWarBookRs.ext, builder.build(), WarBookPb.DecompoundWarBookRs.EXT_FIELD_NUMBER);
		}
		return book;

	}
//	public WarBook addWarBookById(Player player, int warBookId, int reason, int awardType) {
//		WarBook book = null;
//		boolean flag = false;
//		// WarBookPb.DecompoundWarBookRs.Builder builder = WarBookPb.DecompoundWarBookRs.newBuilder();
//		List<CommonPb.Award> awardList = new ArrayList<>();
//		book = new WarBook();
//		Map<Integer, WarBook> warBookMap = player.getWarBooks();
//		book.setKeyId(player.maxKey());
//		book.setBookId(warBookId);
//		StaticWarBook warBookConfigById = staticWarBookMgr.getWarBookConfigById(warBookId);
//		if (warBookConfigById == null) {
//			logger.error("no WarBook config, bookId = " + warBookId + ", reason = " + reason);
//			return book;
//		}
//		initBookPropAndSkill(book, warBookConfigById, awardType);
//		warBookMap.put(book.getKeyId(), book);
//		int num = 800;
//		int num1 = staticLimitMgr.getNum(SimpleId.MAX_BOOK_NUM);
//		if (num1 > 0) {
//			num = num1;
//		}
//		if (warBookMap.size() > num) {
//			Iterator<WarBook> iterator = warBookMap.values().iterator();
//			while (iterator.hasNext()) {
//				WarBook next = iterator.next();
//				StaticWarBook warBookConfigById1 = staticWarBookMgr.getWarBookConfigById(next.getBookId());
//				if (warBookConfigById1.getQuality() < 4 && book.getIsLock() != 1) {
//					deCompound(player, next, awardList);
//					// builder.addKeyId(next.getKeyId());
//					iterator.remove();
//					flag = true;
//				}
//			}
//		}
//		logHelper.war_book_log(WarBookLog.builder().lordId(player.roleId).level(player.getLevel()).nick(player.getNick()).vip(player.getVip()).reason(reason).bookName(warBookConfigById.getName()).cost(0).build());
//
//		if (flag) {
//			playerManager.sendAttachMail(player, null, MailId.WARBOOK_MAX);
////			List<Award> awardListsFinal = PbHelper.finilAward1(awardList);// 合并奖励
////			builder.addAllAward(PbHelper.createAwardList1(player, awardListsFinal));
////			player.sendMsgToPlayer(WarBookPb.DecompoundWarBookRs.ext, builder.build(), WarBookPb.DecompoundWarBookRs.EXT_FIELD_NUMBER);
//		}
//		return book;
//
//	}

	public void deCompound(Player player, WarBook book, List<CommonPb.Award> awardList) {
		// 查找配置
		StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(book.getBookId());
		if (staticWarBook == null) {
			return;
		}
		int quality = staticWarBook.getQuality();
		int level = 0;
		ArrayList<Integer> baseProperty = book.getBaseProperty();
		if (baseProperty.size() > 0) {
			Integer basePropId = book.getBaseProperty().get(0);
			StaticWarBookBaseProperty warBookBaseProperty = staticWarBookMgr.getWarBookBasePropById(basePropId);
			if (null != warBookBaseProperty) {
				level = warBookBaseProperty.getLevel();
			}
		}
		StaticWarBookDecom warBookWarBookDecom = staticWarBookMgr.getWarBookWarBookDecom(quality, level);
		if (warBookWarBookDecom == null) {
			return;
		}
		List<Award> awards = decompoundWarBook(player, warBookWarBookDecom);
		for (Award award : awards) {
			int newKeyId = award.getKeyId();
			// 兵书类型的道具在生成时候已经添加到玩家身上不需要重新添加
			if (newKeyId != 0) {
				awardList.add(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), newKeyId).build());
			} else {
				playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.DECOMPOUSE_BOOK);
				awardList.add(PbHelper.createAward(award.getType(), award.getId(), award.getCount()).build());
			}
		}
		logUser.war_book_log(WarBookLog.builder().lordId(player.roleId).level(player.getLevel()).nick(player.getNick()).vip(player.getVip()).reason(Reason.DECOMPOUSE_BOOK).bookName(staticWarBook.getName()).build());

	}

	public void initBookPropAndSkill(WarBook book, StaticWarBook staticWarBook, int awardType) {
		randomBaseProperty(book, staticWarBook);
		createAllSkill(book, staticWarBook);
		randomSkill(book, staticWarBook, awardType);
	}

	int[] skillType = { 11, 21, 22, 23, 24, 25, 26, 27, 28 };// 11.兵种技能 其他是特殊技能

	public void randomSkill(WarBook book, StaticWarBook staticWarBook, int awardType) {
		// 随机生成技能的数量
		List<List<Integer>> randSkillNum = staticWarBook.getRandSkillNum();
		// 技能配置
		List<List<Integer>> skillTypes = new ArrayList<>(staticWarBook.getSkill());
		// 随机技能个数
		List<Integer> temp = new ArrayList<>();
		for (List<Integer> weight : randSkillNum) {
			temp.add(weight.get(1));
		}
		int numIndex = WeightRandom.initData(temp);
		Integer skillNum = randSkillNum.get(numIndex).get(0);
		if (awardType == AwardType.WAR_BOOK_SKILL || awardType == AwardType.WAR_BOOK_SPECIAL) {
			skillNum = 3;
		}
		temp.clear();
		for (List<Integer> weight : skillTypes) {
			temp.add(weight.get(1));
		}
		List<Integer> tempSkillTypes = new ArrayList<>();
		if (awardType != AwardType.WAR_BOOK_SPECIAL) {
			for (Integer i = 0; i < skillNum; i++) {
				int skillIndex = WeightRandom.initData(temp);
				Integer skillId = skillTypes.get(skillIndex).get(0);
				StaticWarBookSkill warBookSkillByRandomType = staticWarBookMgr.getWarBookSkillByRandomType(skillId);
				if (warBookSkillByRandomType != null) {
					tempSkillTypes.add(skillId);
					skillTypes.remove(skillIndex);
					temp.remove(skillIndex);
				}
			}
		} else {
			int skillIndex;
			while (true) {
				skillIndex = WeightRandom.initData(temp);
				int skillId = skillTypes.get(skillIndex).get(0);
				StaticWarBookSkill warBookSkillByRandomType = staticWarBookMgr.getWarBookSkillByRandomType(skillId);
				if (warBookSkillByRandomType != null && Arrays.stream(skillType).anyMatch(x -> x == skillId)) {
					tempSkillTypes.add(skillId);
					skillTypes.remove(skillIndex);
					temp.remove(skillIndex);
				}
				if (tempSkillTypes.size() == 2) {
					break;
				}
			}
			skillIndex = WeightRandom.initData(temp);
			Integer skillId = skillTypes.get(skillIndex).get(0);
			StaticWarBookSkill warBookSkillByRandomType = staticWarBookMgr.getWarBookSkillByRandomType(skillId);
			if (warBookSkillByRandomType != null) {
				tempSkillTypes.add(skillId);
				skillTypes.remove(skillIndex);
				temp.remove(skillIndex);
			}
		}
		ArrayList<Integer> allSkill = book.getAllSkill();
		List<Integer> collect = allSkill.stream().filter(x -> tempSkillTypes.contains(staticWarBookMgr.getWarBookSkillById(x).getSkillType())).collect(Collectors.toList());
		book.setCurrentSkill((ArrayList<Integer>) collect);
	}

	public WarBook createWarBookById(int warBookId) {
		WarBook book = new WarBook();
		book.setBookId(warBookId);
		StaticWarBook warBookConfigById = staticWarBookMgr.getWarBookConfigById(warBookId);
		if (warBookConfigById == null) {
			return book;
		}
		initBookPropAndSkill(book, warBookConfigById);
		return book;
	}

	public void initBookPropAndSkill(WarBook book, StaticWarBook staticWarBook) {
		randomBaseProperty(book, staticWarBook);
		createAllSkill(book, staticWarBook);
		randomSkill(book, staticWarBook);
	}

	/**
	 * 战役掉落兵书
	 *
	 * @return
	 */
	public StaticWarBook missionDropWarBook() {
		List<StaticWarBookDrop> warBookDrops = staticWarBookMgr.getWarBookDrops();
		List<Integer> temp = new ArrayList<>();
		for (StaticWarBookDrop warBookDrop : warBookDrops) {
			temp.add(warBookDrop.getProability());
		}
		int index = WeightRandom.initData(temp);
		int quality = warBookDrops.get(index).getQuality();
		if (quality == 0) {
			return null;
		}
		StaticWarBook staticWarBook = addWarBookByQuality(quality, Reason.MISSION_DONE);
		return staticWarBook;
	}

	/**
	 * 通过品质生成兵书
	 *
	 * @param quality
	 * @return
	 */
	public StaticWarBook addWarBookByQuality(int quality, int reason) {
		List<StaticWarBook> warBookConfigByQuality = staticWarBookMgr.getWarBookConfigByQuality(quality);
		if (null == warBookConfigByQuality || warBookConfigByQuality.size() == 0) {
			LogHelper.CONFIG_LOGGER.info("no WarBook config, quality = " + quality + ", reason = " + reason);
			return null;
		}
		Random random = new Random();
		int type = random.nextInt(warBookConfigByQuality.size());
		StaticWarBook staticWarBook = warBookConfigByQuality.get(type);
		return staticWarBook;
	}

	/**
	 * 随机生成基础属性
	 *
	 * @param staticWarBook
	 * @return
	 */
	public void randomBaseProperty(WarBook book, StaticWarBook staticWarBook) {
		List<List<Integer>> staticBaseProperty = staticWarBook.getBaseProperty();
		if (null == staticBaseProperty || staticBaseProperty.size() == 0) {
			logger.error("no WarBook baseProperty cofig, bookId = " + book.getBookId());
			return;
		}
		List<Integer> temp = new ArrayList<>();
		for (List<Integer> weight : staticBaseProperty) {
			temp.add(weight.get(1));
		}
		int index = WeightRandom.initData(temp);

		temp.clear();
		Integer basePropertyType = staticBaseProperty.get(index).get(0);
		StaticWarBookBaseProperty warBookBasePropByTypeAndlev = staticWarBookMgr.getWarBookBasePropByTypeAndlev(basePropertyType, 0);
		if (null != warBookBasePropByTypeAndlev) {
			temp.add(warBookBasePropByTypeAndlev.getId());
		} else {
			logger.error("no WarBook baseProperty cofig, bookId = " + book.getBookId());
			return;
		}

		if (null == temp || temp.size() == 0) {
			logger.error("no WarBook baseProperty cofig, bookId = " + book.getBookId());
			return;
		}
		book.setBasePropertyLv(warBookBasePropByTypeAndlev.getLevel());
		book.setBaseProperty((ArrayList<Integer>) temp);
	}

	/**
	 * 随机生成技能
	 *
	 * @param staticWarBook
	 * @return
	 */
	public void randomSkill(WarBook book, StaticWarBook staticWarBook) {
		// 随机生成技能的数量
		List<List<Integer>> randSkillNum = staticWarBook.getRandSkillNum();
		// 技能配置
		List<List<Integer>> skillTypes = new ArrayList<>();
		ArrayList<Integer> allSkill = book.getAllSkill();
		List<List<Integer>> skillType = staticWarBook.getSkill();
		for (Integer allSkillId : allSkill) {
			StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(allSkillId);
			for (List<Integer> skillTypeId : skillType) {
				if (warBookSkillById.getSkillType() == skillTypeId.get(0)) {
					skillTypes.add(skillTypeId);
				}
			}
		}
//        System.out.println("随机技能类型 " + skillTypes);

		// 随机技能个数
		List<Integer> temp = new ArrayList<>();
		for (List<Integer> weight : randSkillNum) {
			temp.add(weight.get(1));
		}
//        System.out.println("随机技能数量权重" + temp);
		int numIndex = WeightRandom.initData(temp);
		Integer skillNum = randSkillNum.get(numIndex).get(0);
//        System.out.println("技能数量=" + skillNum);

		temp.clear();

		for (List<Integer> weight : skillTypes) {
			temp.add(weight.get(1));
		}
		List<Integer> tempSkillTypes = new ArrayList<>();
		for (Integer i = 0; i < skillNum; i++) {
//            System.out.println("随机技能权重" + temp);
			int skillIndex = WeightRandom.initData(temp);

			Integer skillId = skillTypes.get(skillIndex).get(0);
			tempSkillTypes.add(skillId);

			Iterator<List<Integer>> iterator = skillTypes.iterator();
			while (iterator.hasNext()) {
				List<Integer> next = iterator.next();
				if (next.get(0).intValue() == skillId.intValue()) {
					temp.remove(skillTypes.get(skillIndex).get(1));
					iterator.remove();
					break;
				}
			}
//            System.out.println("随机到的技能" + tempSkillTypes);
		}

		List<Integer> tempSkills = new ArrayList<>();
		for (Integer tempSkillType : tempSkillTypes) {
			for (Integer skillId : allSkill) {
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
				if (null == warBookSkillById) {
					logger.error("SkillType config is not find = " + tempSkillType);
					continue;
				}
				if (tempSkillType.intValue() == warBookSkillById.getSkillType()) {
					tempSkills.add(skillId);
					break;
				}
			}
		}

		List<Integer> newTempSkill = new ArrayList<>();
		for (Integer allSkillId : allSkill) {
			StaticWarBookSkill allSkillById = staticWarBookMgr.getWarBookSkillById(allSkillId);
			for (Integer tempSkillId : tempSkills) {
				StaticWarBookSkill tempSkillById = staticWarBookMgr.getWarBookSkillById(tempSkillId);
				if (null != allSkillById && null != tempSkillById && allSkillById.getSkillType() == tempSkillById.getSkillType()) {
					newTempSkill.add(tempSkillId);
					break;
				}
			}
		}
		book.setCurrentSkill((ArrayList<Integer>) newTempSkill);
	}

	/**
	 * 设置兵书的配置技能库
	 *
	 * @param book
	 * @param staticWarBook
	 */
	private void createAllSkill(WarBook book, StaticWarBook staticWarBook) {
//        System.out.println("开始配置总技能");
		List<List<Integer>> skills = new ArrayList<>(staticWarBook.getSkill());
		if (skills.isEmpty()) {
			return;
		}
		List<Integer> newAllSkill = new ArrayList<>();
		Random random = new Random();
		for (List<Integer> skill : skills) {
			if (null != skill) {
				Integer skillTypeId = skill.get(0);
				if (null != skillTypeId) {
					StaticWarBookSkill warBookSkillByTypeAndLev = staticWarBookMgr.getWarBookSkillByTypeAndLev(skillTypeId, 1);
					if (null != warBookSkillByTypeAndLev) {
						if (warBookSkillByTypeAndLev.getIsSoldierSkill() == 0) {
							newAllSkill.add(warBookSkillByTypeAndLev.getId());
						} else {
							List<Integer> soldierSkillLev1 = staticWarBookMgr.getSoldierSkillLev1();

							int index = random.nextInt(soldierSkillLev1.size());
							Integer skillId = soldierSkillLev1.get(index);
							StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
							newAllSkill.add(warBookSkillById.getId());
							book.setSoldierType(warBookSkillById.getSoldierType());
						}
					}
				}
			}
		}
		book.setAllSkill((ArrayList<Integer>) newAllSkill);
	}

	public List<Award> decompoundWarBook(Player player, StaticWarBookDecom staticWarBookDecom) {
		List<Award> awards = new ArrayList<>();
		List<List<Integer>> baseStrongProp = staticWarBookDecom.getBaseStrongProp();
		if (baseStrongProp != null) {
			for (List<Integer> baseStrongPropItem : baseStrongProp) {
				awards.add(new Award(baseStrongPropItem.get(0), baseStrongPropItem.get(1), baseStrongPropItem.get(2)));
			}
		}
		List<List<Integer>> warbookProability = staticWarBookDecom.getWarbookProability();
		if (warbookProability != null) {
			List<Integer> temp = new ArrayList<>();
			for (List<Integer> weight : warbookProability) {
				temp.add(weight.get(1));
			}
			int index = WeightRandom.initData(temp);
			Integer quality = warbookProability.get(index).get(0);
			if (quality != 0) {
				StaticWarBook staticWarBook = addWarBookByQuality(quality, 0);
				if (null != staticWarBook) {
					int newKeyId = playerManager.addAward(player, AwardType.WAR_BOOK, staticWarBook.getId(), 1, Reason.MISSION_WIN);
					awards.add(new Award(newKeyId, AwardType.WAR_BOOK, staticWarBook.getId(), 1));
				}
			} else {
				List<List<Integer>> decomposeAward = staticWarBookDecom.getDecomposeAward();
				if (decomposeAward != null) {
					for (List<Integer> decomposeAwardItem : decomposeAward) {
						awards.add(new Award(decomposeAwardItem.get(0), decomposeAwardItem.get(1), decomposeAwardItem.get(2)));
					}
				}
			}
		}
		return awards;
	}

	/**
	 * 强化兵书
	 *
	 * @param book
	 * @return
	 */
	public int strongWarBook(WarBook book) {
		int strongSkillId = 0;
		// 1.兵书的主技能
		ArrayList<Integer> baseProperty = book.getBaseProperty();
		// 兵书当前的技能
		ArrayList<Integer> currentSkill = book.getCurrentSkill();
		// 2.兵书的配置
		int bookId = book.getBookId();
		StaticWarBook warBookConfigById = staticWarBookMgr.getWarBookConfigById(bookId);
		if (null == warBookConfigById) {
			return strongSkillId;
		}
		// 3.兵书主技能的配置
		StaticWarBookBaseProperty warBookBasePropById = staticWarBookMgr.getWarBookBasePropById(baseProperty.get(0));
		if (null == warBookBasePropById) {

			return strongSkillId;
		}
		int basePropType = warBookBasePropById.getBasePropType();
		int baseProplevel = warBookBasePropById.getLevel();
		// 4.升级兵书主技能的配置
		StaticWarBookBaseProperty warBookBasePropByTypeAndlev = staticWarBookMgr.getWarBookBasePropByTypeAndlev(basePropType, baseProplevel + 1);
		if (warBookBasePropByTypeAndlev == null) {
			return strongSkillId;
		}
		int isStrengthenSkill = warBookBasePropById.getIsStrengthenSkill();
		if (isStrengthenSkill != 0) {
			// 判断技能是否激活满了(满了就只能强化已有的技能)
			if (currentSkill.size() == warBookConfigById.getMaxSkillNum()) {
				strongSkillId = strongSkill(book);
				baseProperty.clear();
				baseProperty.add(warBookBasePropByTypeAndlev.getId());
				book.setBasePropertyLv(warBookBasePropByTypeAndlev.getLevel());
//                System.err.println("兵书强化升级已有技能" + book);
				return strongSkillId;
			}

			// 判断是否有技能(没有就激活一个)
			if (currentSkill.size() == 0) {
				strongSkillId = activeSkill(book);
				baseProperty.clear();
				baseProperty.add(warBookBasePropByTypeAndlev.getId());
				book.setBasePropertyLv(warBookBasePropByTypeAndlev.getLevel());
//                System.err.println("兵书强化开启已有技能" + book);
				return strongSkillId;
			}
			// 兵书激活新技能和强化已有技能的概率占比([50,50])
//            List<Integer> addtion = staticLimitMgr.getAddtion(267);
//            if (addtion.size() != 0) {
//                List<Integer> temp = new ArrayList<>(addtion);
//                int index = WeightRandom.initData(temp);
//                temp.clear();
//                switch (index) {
//                    case 0:  //兵书激活新技能
//                        strongSkillId = activeSkill(book);
//                        break;
//                    case 1:  //强化已有技能
//                        strongSkillId = strongSkill(book);
//                        break;
//                }
//            }
			strongSkillId = randBookSkill(new ArrayList<>(warBookConfigById.getSkill()), book);
		}
		baseProperty.clear();
		baseProperty.add(warBookBasePropByTypeAndlev.getId());
		book.setBasePropertyLv(warBookBasePropByTypeAndlev.getLevel());
		return strongSkillId;
	}

	public int randBookSkill(ArrayList<List<Integer>> lists, WarBook book) {
		while (!lists.isEmpty()) {
			List<Integer> collect = lists.stream().mapToInt(x -> x.get(1)).boxed().collect(Collectors.toList());
			int index = WeightRandom.initData(collect);
			List<Integer> integers = lists.get(index);
			Integer integer1 = book.getCurrentSkill().stream().filter(x -> staticWarBookMgr.getWarBookSkillById(x).getSkillType() == integers.get(0)).findAny().orElse(null);
			if (integer1 != null) {
				// 如果存在就强化
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(integer1);
				if (warBookSkillById != null) {
					StaticWarBookSkill warBookSkillById1 = staticWarBookMgr.getWarBookSkillById(warBookSkillById.getNextSkill());
					if (warBookSkillById1 != null) {
						book.getCurrentSkill().set(book.getCurrentSkill().indexOf(integer1), warBookSkillById1.getId());
						book.getAllSkill().set(book.getAllSkill().indexOf(integer1), warBookSkillById1.getId());
						return warBookSkillById1.getId();
					} else {
						lists.remove(index);
					}
				}
			} else {
				Integer integer = book.getAllSkill().stream().filter(x -> staticWarBookMgr.getWarBookSkillById(x).getSkillType() == integers.get(0)).findAny().orElse(null);
				if (integer != null) {
					StaticWarBookSkill warBookSkillByTypeAndLev = staticWarBookMgr.getWarBookSkillById(integer);
					if (warBookSkillByTypeAndLev != null) {
						book.getCurrentSkill().add(warBookSkillByTypeAndLev.getId());
						List<Integer> collect1 = book.getCurrentSkill().stream().map(x -> staticWarBookMgr.getWarBookSkillById(x).getSkillType()).collect(Collectors.toList());
						List<Integer> collect2 = book.getAllSkill().stream().filter(x -> collect1.contains(staticWarBookMgr.getWarBookSkillById(x).getSkillType())).collect(Collectors.toList());
						book.setCurrentSkill((ArrayList<Integer>) collect2);
						return warBookSkillByTypeAndLev.getId();
					}
				}
			}
		}
		return 0;
	}

	/**
	 * 强化已有的技能
	 *
	 * @param book
	 */
	private int strongSkill(WarBook book) {
		int strongSkillId = 0;
		StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(book.getBookId());
		// 1.获取当前拥有的技能集合
		ArrayList<Integer> currentSkill = book.getCurrentSkill();
		ArrayList<Integer> allSkill = book.getAllSkill();

		// 2.获取当前技能集合的权重配置
		List<List<Integer>> currentSkillTypes = new ArrayList<>();
		List<List<Integer>> skillType = staticWarBook.getSkill();
		for (Integer currentSkillId : currentSkill) {
			StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(currentSkillId);
			if (warBookSkillById == null) {
				logger.error("config error");
				continue;
			}

			int type = staticWarBookMgr.getSkillType(currentSkillId);
			if (type == 0 || type == 11) {
				continue;
			}
			StaticWarBookSkill warBookSkillByTypeAndLev = staticWarBookMgr.getWarBookSkillByTypeAndLev(type, warBookSkillById.getLevel() + 1);
			if (warBookSkillByTypeAndLev == null) {
				continue;
			}

			for (List<Integer> skillTypeId : skillType) {
				if (warBookSkillById.getSkillType() == skillTypeId.get(0)) {
					currentSkillTypes.add(skillTypeId);
				}
			}
		}
		// 3.计算技能权重
		List<Integer> temp = new ArrayList<>();
		for (List<Integer> weight : currentSkillTypes) {
			temp.add(weight.get(1));
		}

		// 4.按照技能强化等级进行操作(1.满级直接进行下一次循环 2.只要强化成功一个就停止循环)
		boolean flag = false;
		for (int i = 0; i < temp.size(); i++) {
			if (flag) {
				break;
			}
			int skillTypeIndex = WeightRandom.initData(temp);
			int skillTypeId = currentSkillTypes.get(skillTypeIndex).get(0);

			Iterator<Integer> iterator = currentSkill.iterator();
			while (iterator.hasNext()) {
				Integer currentSkillId = iterator.next();
				int type = staticWarBookMgr.getSkillType(currentSkillId);

				if (skillTypeId == type) {
					StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(currentSkillId);
					if (warBookSkillById == null) {
						logger.error("config error");
						continue;
					}
					StaticWarBookSkill warBookSkillByTypeAndLev = staticWarBookMgr.getWarBookSkillByTypeAndLev(skillTypeId, warBookSkillById.getLevel() + 1);
					if (null != warBookSkillByTypeAndLev) {
						int currentIndex = currentSkill.indexOf(currentSkillId.intValue());
						if (currentIndex == -1) {
							continue;
						}
						iterator.remove();
						currentSkill.add(currentIndex, warBookSkillByTypeAndLev.getId());

						int allIndex = allSkill.indexOf(currentSkillId.intValue());
						if (allIndex == -1) {
							continue;
						}
						allSkill.remove(currentSkillId);
						allSkill.add(allIndex, warBookSkillByTypeAndLev.getId());

						flag = true;
						strongSkillId = warBookSkillByTypeAndLev.getId();
						break;
					}
				}
			}
			List<Integer> newTempSkill = new ArrayList<>();
			for (Integer allSkillId : allSkill) {
				StaticWarBookSkill allSkillById = staticWarBookMgr.getWarBookSkillById(allSkillId);
				for (Integer tempSkillId : currentSkill) {
					StaticWarBookSkill tempSkillById = staticWarBookMgr.getWarBookSkillById(tempSkillId);
					if (null != allSkillById && null != tempSkillById && allSkillById.getSkillType() == tempSkillById.getSkillType()) {
						newTempSkill.add(tempSkillId);
						break;
					}
				}
			}
			book.setCurrentSkill((ArrayList<Integer>) newTempSkill);
		}
		// 当所有技能已经满级了,就开启一个
		if (!flag) {
			strongSkillId = activeSkill(book);
		}
		return strongSkillId;
	}

	/**
	 * 激活新技能
	 *
	 * @param book
	 */
	private int activeSkill(WarBook book) {
		int strongSkillId = 0;

		// 1.获取技能配置
		StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(book.getBookId());
		ArrayList<Integer> allSkill = book.getAllSkill();
		ArrayList<Integer> currentSkill = book.getCurrentSkill();

		if (currentSkill.size() >= staticWarBook.getMaxSkillNum()) {
			return strongSkillId;
		}
		// 2.获取当前没有激活的技能
		List<Integer> noHaveSkillIds = new ArrayList<>();
		for (Integer allSkillId : allSkill) {
			if (!currentSkill.contains(allSkillId)) {
				noHaveSkillIds.add(allSkillId);
			}
		}

		// 3.获取技能的配置
		List<List<Integer>> skills = new ArrayList<>();
		List<List<Integer>> skillType = staticWarBook.getSkill();
		for (Integer noHaveSkill : noHaveSkillIds) {
			StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(noHaveSkill);
			if (warBookSkillById == null) {
				logger.error("config error");
				continue;
			}
			for (List<Integer> skillTypeId : skillType) {
				if (warBookSkillById.getSkillType() == skillTypeId.get(0)) {
					skills.add(skillTypeId);
					break;
				}
			}
		}

		if (skills.size() == 0) {
			return strongSkillId;
		}

		// 4.计算技能权重
		List<Integer> temp = new ArrayList<>();
		for (List<Integer> weight : skills) {
			temp.add(weight.get(1));
		}

		int skillTypeIndex = WeightRandom.initData(temp);
		int skillTypeId = skills.get(skillTypeIndex).get(0);
		// 5.随机技能
		for (Integer noHaveSkillId : noHaveSkillIds) {
			StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(noHaveSkillId);
			if (warBookSkillById.getSkillType() == skillTypeId) {
				currentSkill.add(warBookSkillById.getId());
				strongSkillId = warBookSkillById.getId();
				break;
			}
		}

		List<Integer> newTempSkill = new ArrayList<>();
		for (Integer allSkillId : allSkill) {
			StaticWarBookSkill allSkillById = staticWarBookMgr.getWarBookSkillById(allSkillId);
			for (Integer tempSkillId : currentSkill) {
				StaticWarBookSkill tempSkillById = staticWarBookMgr.getWarBookSkillById(tempSkillId);
				if (null != allSkillById && null != tempSkillById && allSkillById.getSkillType() == tempSkillById.getSkillType()) {
					newTempSkill.add(tempSkillId);
					break;
				}
			}
		}
		book.setCurrentSkill((ArrayList<Integer>) newTempSkill);
		return strongSkillId;
	}

	/**
	 * 生成兵书商城物品
	 *
	 * @param player
	 */
	public void refreshWarbookShop(Player player) {
		// 1.获取当前玩家等级对应的商店配置
		List<Integer> warBookShopConfig = getWarBookShopConfig(player.getLevel());
		if (null == warBookShopConfig) {
			return;
		}
		List<CommonPb.WarBookShopItem> warBookShopItems = new ArrayList<>();
		randWarbookShopGoods(warBookShopItems, warBookShopConfig);

		// 将生成的商品按照place排序放入队列
		List<CommonPb.WarBookShopItem> list = new ArrayList<>(warBookShopItems);
		Collections.sort(list, (o1, o2) -> {
			if (o1 == null && o2 == null) {
				return 0;
			}
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			if (o1.getPlace() > o2.getPlace()) {
				return 1;
			}
			if (o2.getPlace() > o1.getPlace()) {
				return -1;
			}
			return 0;
		});

		Map<Integer, CommonPb.WarBookShopItem> newWarBookShops = new HashMap<Integer, CommonPb.WarBookShopItem>();
		int index = 1;
		Iterator<CommonPb.WarBookShopItem> iterator = list.iterator();
		while (iterator.hasNext()) {
			CommonPb.WarBookShopItem next = iterator.next();
			if (null != next) {
				CommonPb.WarBookShopItem.Builder builder = CommonPb.WarBookShopItem.newBuilder();
				builder.setPos(index);
				builder.setPlace(next.getPlace());
				builder.setPrice(next.getPrice());
				builder.setIsbuy(next.getIsbuy());
				builder.setAward(next.getAward());
				builder.setIsFreeBuy(next.getIsFreeBuy());
				newWarBookShops.put(index, builder.build());
				index++;
			}
		}
		list.clear();
		player.getWarBookShops().clear();
		player.getWarBookShops().putAll(newWarBookShops);
	}

	public void randWarbookShopGoods(List<CommonPb.WarBookShopItem> warBookShopItems, List<Integer> warBookShopConfig) {
		// 2.获取要随机的类型的区间
		// 要生成物品的总数量
		Integer type = warBookShopConfig.get(1);
		Integer num = warBookShopConfig.get(2);
		Random random = new Random();
		// 生成非最后一个类型商品
		Map<Integer, StaticWarBookBuy> warBookBuy = staticWarBookMgr.getWarBookBuy();
		for (int i = 1; i <= type; i++) {
			StaticWarBookBuy staticWarBookBuy = warBookBuy.get(i);
			if (staticWarBookBuy.getLast() == 0) {
				List<StaticWarBookShopGoods> staticWarBookShopGoodsByType = staticWarBookMgr.getStaticWarBookShopGoodsByType(i);
				if (staticWarBookShopGoodsByType == null) {
					return;
				}

				// 3.计算商品权重
				List<List<Integer>> randoms = new ArrayList<>(staticWarBookBuy.getRand());
				List<Integer> temp = new ArrayList<Integer>();
				for (List<Integer> weight : randoms) {
					temp.add(weight.get(1));
				}
				int numIndex = WeightRandom.initData(temp);
				Integer goodsNum = randoms.get(numIndex).get(0);
				// 4.随机出当前类型物品应该生成的物品的数量
				int nextInt = random.nextInt(staticWarBookShopGoodsByType.size());
				StaticWarBookShopGoods staticWarBookShopGoods = staticWarBookShopGoodsByType.get(nextInt);
				List<Integer> award = staticWarBookShopGoods.getAward();
				List<Integer> price = staticWarBookShopGoods.getPrice();
				Integer awardType = award.get(0);
				CommonPb.WarBookShopItem.Builder item = CommonPb.WarBookShopItem.newBuilder();
				if (awardType == AwardType.WAR_BOOK) {
					for (int j = 0; j < goodsNum; j++) {
						StaticWarBook staticWarBook = addWarBookByQuality(award.get(1), 0);
						if (staticWarBook == null) {
							continue;
						}
						WarBook warBookById = createWarBookById(staticWarBook.getId());
						item.setAward(PbHelper.createWarBookAward(warBookById));
						item.setIsbuy(0);
						if (price.size() != 0) {
							item.setPrice(PbHelper.createAward(price.get(0), price.get(1), price.get(2)));
							item.setIsFreeBuy(0);
						} else {
							item.setIsFreeBuy(1);
						}
						item.setPlace(staticWarBookShopGoods.getPlace());
						warBookShopItems.add(item.build());
					}
				} else {
//                    System.err.println("商品类型位为物品 goodsNum = " + goodsNum);
					if (goodsNum > staticWarBookShopGoodsByType.size()) {
						for (int j = 0; j < staticWarBookShopGoodsByType.size(); j++) {
							StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(j);
							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							} else {
								item.setIsFreeBuy(1);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						}

						for (int j = 0; j < goodsNum - staticWarBookShopGoodsByType.size(); j++) {

							int randomOtherIndex = random.nextInt(staticWarBookShopGoodsByType.size());
							StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(randomOtherIndex);

							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							} else {
								item.setIsFreeBuy(1);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						}
					} else {
//                        for (int j = 0; j < goodsNum; j++) {
//                            StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(j);
//                            item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
//                            item.setIsbuy(0);
//                            if (goods.getPrice().size() != 0) {
//                                item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
//                                item.setIsFreeBuy(0);
//                            } else {
//                                item.setIsFreeBuy(1);
//                            }
//                            item.setPlace(goods.getPlace());
//                            warBookShopItems.add(item.build());
//                        }
						ArrayList<StaticWarBookShopGoods> staticWarBookShopGoods1 = new ArrayList<>(staticWarBookShopGoodsByType);
						Collections.shuffle(staticWarBookShopGoods1);
						List<StaticWarBookShopGoods> staticWarBookShopGoods2 = staticWarBookShopGoods1.subList(0, goodsNum);
						staticWarBookShopGoods2 = staticWarBookShopGoods2.stream().sorted(Comparator.comparingInt(StaticWarBookShopGoods::getId)).collect(Collectors.toList());
						staticWarBookShopGoods2.forEach(goods -> {
							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							item.setIsFreeBuy(1);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						});
					}
				}
			}
		}

		// 生成最后一种类型物品的商品
		int size = warBookShopItems.size();
		if (size < num) {
			StaticWarBookBuy warBookBuyLast = staticWarBookMgr.getWarBookBuyLast();
			if (null != warBookBuyLast) {
				List<StaticWarBookShopGoods> staticWarBookShopGoodsByType = staticWarBookMgr.getStaticWarBookShopGoodsByType(warBookBuyLast.getType());
				List<Integer> award = staticWarBookShopGoodsByType.get(0).getAward();
				List<Integer> price = staticWarBookShopGoodsByType.get(0).getPrice();

				Integer awardType = award.get(0);
				int goodsNum = num - size;
				CommonPb.WarBookShopItem.Builder item = CommonPb.WarBookShopItem.newBuilder();
				if (awardType == AwardType.WAR_BOOK) {
					for (int j = 0; j < goodsNum; j++) {
						StaticWarBook staticWarBook = addWarBookByQuality(award.get(1), 0);
						if (staticWarBook == null) {
							continue;
						}
						WarBook warBookById = createWarBookById(staticWarBook.getId());
						item.setAward(PbHelper.createWarBookAward(warBookById));
						item.setIsbuy(0);
						if (price.size() != 0) {
							item.setPrice(PbHelper.createAward(price.get(0), price.get(1), price.get(2)));
							item.setIsFreeBuy(0);
						} else {
							item.setIsFreeBuy(1);
						}
						item.setPlace(staticWarBookShopGoodsByType.get(0).getPlace());
						warBookShopItems.add(item.build());
					}
				} else {
					if (goodsNum > staticWarBookShopGoodsByType.size()) {
						for (int j = 0; j < goodsNum; j++) {
							StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(j);
							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							} else {
								item.setIsFreeBuy(1);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						}

						for (int j = 0; j < goodsNum - staticWarBookShopGoodsByType.size(); j++) {
							int randomOtherIndex = random.nextInt(staticWarBookShopGoodsByType.size());
							StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(randomOtherIndex);

							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							} else {
								item.setIsFreeBuy(1);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						}
					} else {
						for (int j = 0; j < goodsNum; j++) {

							int randomOtherIndex = random.nextInt(staticWarBookShopGoodsByType.size());
							StaticWarBookShopGoods goods = staticWarBookShopGoodsByType.get(randomOtherIndex);

							item.setAward(PbHelper.createAward(goods.getAward().get(0), goods.getAward().get(1), goods.getAward().get(2)));
							item.setIsbuy(0);
							if (goods.getPrice().size() != 0) {
								item.setPrice(PbHelper.createAward(goods.getPrice().get(0), goods.getPrice().get(1), goods.getPrice().get(2)));
								item.setIsFreeBuy(0);
							} else {
								item.setIsFreeBuy(1);
							}
							item.setPlace(goods.getPlace());
							warBookShopItems.add(item.build());
						}
					}
				}
			}
		}
	}

	/**
	 * 获取兵书商城的配置
	 *
	 * @param level
	 * @return
	 */
	public List<Integer> getWarBookShopConfig(int level) {
		List<List<Integer>> warBookShop = staticLimitMgr.getWarBookShop();
		if (null == warBookShop || warBookShop.size() == 0) {
			return null;
		}
		for (List<Integer> config : warBookShop) {
			if (config.size() != 3) {
				return null;
			}
		}
		if (warBookShop.size() > 0) {
			if (level >= warBookShop.get(warBookShop.size() - 1).get(0)) {
				return warBookShop.get(warBookShop.size() - 1);
			}
			for (int i = 0; i < warBookShop.size(); i++) {
				if (i + 1 == warBookShop.size()) {
					return warBookShop.get(i);
				}
				Integer beginLevel = warBookShop.get(i).get(0);
				Integer endLevel = warBookShop.get(i + 1).get(0);
				if (level >= beginLevel && level < endLevel) {
					return warBookShop.get(i);
				}
			}
		}
		return null;
	}

	// 获得兵书的一级属性
	public Property getProperty(WarBook book, StaticHero hero) {
		Property property = new Property();
		if (book == null) {
			return property;
		}
		// 获得兵书主属性
		ArrayList<Integer> baseProperty = book.getBaseProperty();
		if (baseProperty.size() == 1) {
			StaticWarBookBaseProperty warBookBasePropById = staticWarBookMgr.getWarBookBasePropById(baseProperty.get(0));
			if (warBookBasePropById == null) {
				return property;
			}
			List<List<Integer>> affect = warBookBasePropById.getAffect();
			if (affect.size() > 0) {
				for (List<Integer> affectValue : affect) {
					if (affectValue.size() == 2) {
						Integer propType = affectValue.get(0);
						switch (propType) {
						case (BookEffectType.ATTCK):
							property.setAttack(property.getAttack() + affectValue.get(1));
							break;
						case (BookEffectType.DEFENCE):
							property.setDefence(property.getDefence() + affectValue.get(1));
							break;
						case (BookEffectType.SOLDIER_NUM):
							property.setSoldierNum(property.getSoldierNum() + affectValue.get(1));
							break;
						case (BookEffectType.STRONG_ATTACK):
							property.setStrongAttack(property.getStrongAttack() + affectValue.get(1));
							break;
						case (BookEffectType.STRONG_DEFENCE):
							property.setStrongDefence(property.getStrongDefence() + affectValue.get(1));
							break;
						case (BookEffectType.ATTACK_CITY):
							property.setAttackCity(property.getAttackCity() + affectValue.get(1));
							break;
						case (BookEffectType.DEFENCE_CITY):
							property.setDefenceCity(property.getDefenceCity() + affectValue.get(1));
							break;
						}
					}
				}
			}
		}

		// 获得兵书技能属性
		ArrayList<Integer> currentSkill = book.getCurrentSkill();
		if (currentSkill.size() > 0) {
			for (Integer skillId : currentSkill) {
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
				if (null != warBookSkillById) {
					int soldierType = warBookSkillById.getSoldierType();
					if (soldierType != 0 && hero.getSoldierType() != soldierType) {// 判断英雄的兵种类型是否与当前技能对应
						continue;
					}
					List<List<Integer>> affect = warBookSkillById.getAffect();
					for (List<Integer> affectValue : affect) {
						if (affectValue.size() == 2) {
							Integer propType = affectValue.get(0);
							switch (propType) {
							case (BookEffectType.ATTCK):
								property.setAttack(property.getAttack() + affectValue.get(1));
								break;
							case (BookEffectType.DEFENCE):
								property.setDefence(property.getDefence() + affectValue.get(1));
								break;
							case (BookEffectType.SOLDIER_NUM):
								property.setSoldierNum(property.getSoldierNum() + affectValue.get(1));
								break;
							case (BookEffectType.STRONG_ATTACK):
								property.setStrongAttack(property.getStrongAttack() + affectValue.get(1));
								break;
							case (BookEffectType.STRONG_DEFENCE):
								property.setStrongDefence(property.getStrongDefence() + affectValue.get(1));
								break;
							case (BookEffectType.ATTACK_CITY):
								property.setAttackCity(property.getAttackCity() + affectValue.get(1));
								break;
							case (BookEffectType.DEFENCE_CITY):
								property.setDefenceCity(property.getDefenceCity() + affectValue.get(1));
								break;
							}
						}
					}
				}
			}
		}
		return property;
	}

	// 获得兵书的二级属性
	public BattleProperty getBookBattleProperty(Hero hero) {
		BattleProperty property = new BattleProperty();
		// 获得兵书技能属性
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (null == hero || heroBooks.size() == 0) {
			return property;
		}

		StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
		if (null == staticHero) {
			return property;
		}

		HeroBook heroBook = heroBooks.get(0);
		WarBook book = heroBook.getBook();
		if (null == book) {
			return property;
		}

		// 获得兵书主属性
		ArrayList<Integer> baseProperty = book.getBaseProperty();
		if (baseProperty.size() == 1) {
			StaticWarBookBaseProperty warBookBasePropById = staticWarBookMgr.getWarBookBasePropById(baseProperty.get(0));
			if (warBookBasePropById == null) {
				return property;
			}
			List<List<Integer>> affect = warBookBasePropById.getAffect();
			if (affect.size() > 0) {
				for (List<Integer> affectValue : affect) {
					if (affectValue.size() == 2) {
						Integer propType = affectValue.get(0);
						switch (propType) {
						case (BookEffectType.STRONG_ATTACK):
							property.setStrongAttack(property.getStrongAttack() + affectValue.get(1));
							break;
						case (BookEffectType.STRONG_DEFENCE):
							property.setStrongDefence(property.getStrongDefence() + affectValue.get(1));
							break;
						case (BookEffectType.CRITI):
							property.setCriti(property.getCriti() + affectValue.get(1));
							break;
						case (BookEffectType.MISS):
							property.setMiss(property.getMiss() + affectValue.get(1));
							break;
						}
					}
				}
			}
		}

		ArrayList<Integer> currentSkill = book.getCurrentSkill();
		if (currentSkill.size() > 0) {
			for (Integer skillId : currentSkill) {
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
				if (null != warBookSkillById) {
					int soldierType = warBookSkillById.getSoldierType();
					if (soldierType != 0 && staticHero.getSoldierType() != soldierType) {// 判断英雄的兵种类型是否与当前技能对应
						continue;
					}
					List<List<Integer>> affect = warBookSkillById.getAffect();
					for (List<Integer> affectValue : affect) {
						if (affectValue.size() == 2) {
							Integer propType = affectValue.get(0);
							switch (propType) {
							case (BookEffectType.STRONG_ATTACK):
								property.setStrongAttack(property.getStrongAttack() + affectValue.get(1));
								break;
							case (BookEffectType.STRONG_DEFENCE):
								property.setStrongDefence(property.getStrongDefence() + affectValue.get(1));
								break;
							case (BookEffectType.CRITI):
								property.setCriti(property.getCriti() + affectValue.get(1));
								break;
							case (BookEffectType.MISS):
								property.setMiss(property.getMiss() + affectValue.get(1));
								break;
							}
						}
					}
				}
			}
		}
		return property;
	}

	// 获得兵书的总二级属性
	public BattleProperty getBookBattleTotalProperty(Player player) {
		BattleProperty property = new BattleProperty();
		// 获得兵书技能属性
		List<Integer> embattleList = player.getEmbattleList();
		for (Integer heroId : embattleList) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}
			ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
			if (null == hero || heroBooks.size() == 0) {
				continue;
			}
			StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
			if (null == staticHero) {
				continue;
			}
			HeroBook heroBook = heroBooks.get(0);
			WarBook book = heroBook.getBook();
			if (null == book) {
				continue;
			}
			ArrayList<Integer> currentSkill = book.getCurrentSkill();
			if (currentSkill.size() > 0) {
				for (Integer skillId : currentSkill) {
					StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);
					if (null != warBookSkillById) {
						int soldierType = warBookSkillById.getSoldierType();
						if (soldierType != 0 && staticHero.getSoldierType() != soldierType) {// 判断英雄的兵种类型是否与当前技能对应
							continue;
						}
						List<List<Integer>> affect = warBookSkillById.getAffect();
						for (List<Integer> affectValue : affect) {
							if (affectValue.size() == 2) {
								Integer propType = affectValue.get(0);
								switch (propType) {
								case (BookEffectType.STRONG_ATTACK):
									property.setStrongAttack(property.getStrongAttack() + affectValue.get(1));
									break;
								case (BookEffectType.STRONG_DEFENCE):
									property.setStrongDefence(property.getStrongDefence() + affectValue.get(1));
									break;
								case (BookEffectType.CRITI):
									BattleProperty battlePropertyCri = new BattleProperty();
									battlePropertyCri.setCriti(affectValue.get(1) / 10);
									property.add(battlePropertyCri);
									break;
								case (BookEffectType.MISS):
									BattleProperty battlePropertyMiss = new BattleProperty();
									battlePropertyMiss.setMiss(affectValue.get(1) / 10);
									property.add(battlePropertyMiss);
									break;
								}
							}
						}
					}
				}
			}
		}
		return property;
	}

	/**
	 * 获取兵书技能加成
	 *
	 * @param heroBooks
	 * @param heroId
	 * @param effectType
	 * @return
	 */
	public Integer getHeroWarBookSkillEffect(List<HeroBook> heroBooks, int heroId, int effectType) {
		if (heroBooks == null || heroBooks.isEmpty()) {
			return null;
		}
		for (HeroBook hroBook : heroBooks) {
			if (hroBook == null) {
				continue;
			}

			WarBook book = hroBook.getBook();
			if (book == null) {
				continue;
			}

			int bookId = book.getBookId();
			StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
			if (staticWarBook == null) {
				continue;
			}

			ArrayList<Integer> currentSkill = book.getCurrentSkill();
			for (Integer skillId : currentSkill) {
				StaticHero staticHero = staticHeroDataMgr.getStaticHero(heroId);
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);

				if (null == warBookSkillById || staticHero == null) {
					continue;
				}

				int soldierType = warBookSkillById.getSoldierType();
				if (soldierType != 0 && staticHero.getSoldierType() != soldierType) {
					continue;
				}

				List<List<Integer>> affect = warBookSkillById.getAffect();
				for (List<Integer> affectValue : affect) {
					if (affectValue.size() == 2) {
						Integer propType = affectValue.get(0);
						if (propType.intValue() == effectType) {
							return affectValue.get(1);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 特殊添加有类似buff加成的技能
	 *
	 * @param hero
	 */
	public void addWarBookBuff(Player player, Hero hero) {
		HeroBook heroBook = null;
		if (hero == null) {
			return;
		}
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (heroBooks == null) {
			return;
		}
		if (heroBooks.size() <= 0) {
			return;
		}

		heroBook = heroBooks.get(0);
		if (heroBook == null) {
			return;
		}

		Integer heroWarBookSkill24Effect = getHeroWarBookSkillEffect(heroBooks, hero.getHeroId(), BookEffectType.GET_HERO_EXP);
		if (null != heroWarBookSkill24Effect) {
			Buff buff = new Buff();
			buff.setEndTime(TimeHelper.getZeroTimeMs());
			buff.setBuffId(BookEffectType.GET_HERO_EXP);
			heroBook.getBuffMap().put(BookEffectType.GET_HERO_EXP, buff);
		}
	}

	/**
	 * 特殊移除处理有类似buff加成的技能
	 *
	 * @param hero
	 */
	public void removeWarBookBuff(Player player, Hero hero) {
		HeroBook heroBook = null;
		if (hero == null) {
			return;
		}
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (heroBooks == null) {
			return;
		}
		if (heroBooks.size() <= 0) {
			return;
		}

		heroBook = heroBooks.get(0);
		if (heroBook == null) {
			return;
		}

		Integer heroWarBookSkill24Effect = getHeroWarBookSkillEffect(heroBooks, hero.getHeroId(), BookEffectType.GET_HERO_EXP);
		if (null == heroWarBookSkill24Effect) {
			heroBook.getBuffMap().remove(BookEffectType.GET_HERO_EXP);
		}

		/*
		 * Integer heroWarBookSkill21Effect = getHeroWarBookSkillEffect(hero, BookEffectType.COUNTRY_WAR); if (null == heroWarBookSkill21Effect) { heroBook.getBuffMap().remove(BookEffectType.COUNTRY_WAR); }
		 */
		player.getLord().setBookEffectHoronCd(0);
	}

	/**
	 * 特殊修改处理有类似buff加成的技能
	 *
	 * @param hero
	 */
	public void updateWarBookBuff(Player player, Hero hero, int bookSkillType) {
		HeroBook heroBook = null;
		if (hero == null) {
			return;
		}
		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		if (heroBooks == null) {
			return;
		}
		if (heroBooks.size() <= 0) {
			return;
		}
		heroBook = heroBooks.get(0);
		if (heroBook == null) {
			return;
		}

		switch (bookSkillType) {
		case (BookEffectType.GET_HERO_EXP):
			Buff buff24 = heroBook.getBuffMap().get(BookEffectType.GET_HERO_EXP);
			buff24.setEndTime(TimeHelper.getZeroTimeMs() + TimeHelper.DAY_MS);
			break;
		case (BookEffectType.COUNTRY_WAR):
			Buff buff21 = heroBook.getBuffMap().get(BookEffectType.COUNTRY_WAR);
			if (null == buff21) {
				buff21 = new Buff();
				buff21.setBuffId(BookEffectType.COUNTRY_WAR);
				heroBook.getBuffMap().put(BookEffectType.COUNTRY_WAR, buff21);
			}
			buff21.setEndTime(System.currentTimeMillis() + 30 * TimeHelper.MINUTE_MS);
			player.getLord().setBookEffectHoronCd(System.currentTimeMillis() + 30 * TimeHelper.MINUTE_MS);
			break;
		}
	}

	/**
	 * 获取兵书对行军速度加成的影响值
	 *
	 * @param player
	 * @param heroIds
	 * @return
	 */
	public float getBookEffectMarch(Player player, List<Integer> heroIds) {
		int flag = 0;

		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}
			List<HeroBook> heroBooks = hero.getHeroBooks();
			if (heroBooks == null || heroBooks.isEmpty()) {
				continue;
			}
			Integer heroWarBookSkillEffect = getHeroWarBookSkillEffect(heroBooks, hero.getHeroId(), BookEffectType.SPEED_UP_MARCH);
			if (heroWarBookSkillEffect != null) {
				flag = flag + 1;
			}
		}

		StaticBookSkillEffectType bookSkillEffectType = staticWarBookMgr.getBookSkillEffectType(BookEffectType.SPEED_UP_MARCH);
		if (flag == 0) {
			return 0.00f;
		} else if (flag == heroIds.size()) {
			return bookSkillEffectType.getParam().get(0) / 100.00f;
		} else if (flag > 0 && flag < heroIds.size()) {
			return bookSkillEffectType.getParam().get(1) / 100.00f;
		}
		return 0.00f;
	}

	/**
	 * 获取兵书加成对应的兵种类型
	 *
	 * @param player
	 * @param heroId
	 * @return
	 */
	public Integer getWarBookSoldierType(Player player, int heroId) {
		if (player == null) {
			return null;
		}

		Hero hero = player.getHero(heroId);
		if (hero == null) {
			return null;
		}

		ArrayList<HeroBook> heroBooks = hero.getHeroBooks();
		for (HeroBook hroBook : heroBooks) {
			if (hroBook == null) {
				continue;
			}

			WarBook book = hroBook.getBook();
			if (book == null) {
				continue;
			}

			int bookId = book.getBookId();
			StaticWarBook staticWarBook = staticWarBookMgr.getWarBookConfigById(bookId);
			if (staticWarBook == null) {
				continue;
			}

			ArrayList<Integer> currentSkill = book.getCurrentSkill();
			for (Integer skillId : currentSkill) {
				StaticHero staticHero = staticHeroDataMgr.getStaticHero(hero.getHeroId());
				StaticWarBookSkill warBookSkillById = staticWarBookMgr.getWarBookSkillById(skillId);

				if (null == warBookSkillById || staticHero == null) {
					continue;
				}

				int soldierType = warBookSkillById.getSoldierType();
				if (soldierType == 0 || staticHero.getSoldierType() != soldierType) {
					continue;
				}
				switch (skillId) {
				case BookEffectType.SOLDIER_TYPE_111:
					return BookEffectType.SOLDIER_TYPE_111;
				case BookEffectType.SOLDIER_TYPE_112:
					return BookEffectType.SOLDIER_TYPE_112;
				case BookEffectType.SOLDIER_TYPE_113:
					return BookEffectType.SOLDIER_TYPE_113;
				case BookEffectType.SOLDIER_TYPE_114:
					return BookEffectType.SOLDIER_TYPE_114;
				case BookEffectType.SOLDIER_TYPE_115:
					return BookEffectType.SOLDIER_TYPE_115;
				case BookEffectType.SOLDIER_TYPE_116:
					return BookEffectType.SOLDIER_TYPE_116;
				case BookEffectType.SOLDIER_TYPE_117:
					return BookEffectType.SOLDIER_TYPE_117;
				case BookEffectType.SOLDIER_TYPE_118:
					return BookEffectType.SOLDIER_TYPE_118;
				case BookEffectType.SOLDIER_TYPE_119:
					return BookEffectType.SOLDIER_TYPE_119;
				}
			}
		}
		return null;
	}

	/**
	 * 兵书技能加成
	 * <p>
	 * 英雄每天获得40次扫荡最高可扫荡副本获得的英雄经验
	 *
	 * @param player
	 * @return
	 */
	public int getSweepHeroBookEffect(Player player) {
		// 1.倒序排序玩家副本列表
		Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
		TreeMap<Integer, Map<Integer, Mission>> newMissions = new TreeMap<Integer, Map<Integer, Mission>>(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (o1 == null || o2 == null) {
					return 0;
				} else {
					int a = (int) o1;
					int b = (int) o2;
					if (a > b) {
						return -1;
					} else if (a == b) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		});
		// 2.遍历获取玩家最高可扫荡副本关卡
		newMissions.putAll(missions);
		StaticMission staticMissionTar = null;
		Set<Map.Entry<Integer, Map<Integer, Mission>>> entrieMaps = newMissions.entrySet();
		boolean flag = true;
		for (Map.Entry<Integer, Map<Integer, Mission>> maps : entrieMaps) {
			if (!flag) {
				break;
			}
			Map<Integer, Mission> value = maps.getValue();
			Set<Map.Entry<Integer, Mission>> entryeMission = value.entrySet();

			for (Map.Entry<Integer, Mission> integerMissionEntry : entryeMission) {
				Mission mission = integerMissionEntry.getValue();
				StaticMission staticMission = staticMissionMgr.getStaticMission(mission.getMissionId());
				if (staticMission.getMissionType() == MissionType.BossMission) {
					if (mission.getStar() < CommonDefine.MISSION_STAR) {
						// 是否vip开启
						StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
						if (staticVip != null) {
							// vip等级不够
							if (staticVip.getWipeCombat() == 1) {
								staticMissionTar = staticMission;
								flag = false;
								break;
							}
						}
					} else {
						staticMissionTar = staticMission;
						flag = false;
						break;
					}
				}
			}
		}
		int heroExp = 0;
		if (null != staticMissionTar) {
//            System.err.println("最高可扫荡副本>>>>>>>>>>>>>>" + staticMissionTar);
			heroExp = staticMissionTar.getHeroExp() * 40;
			// heroDataManager.addAllHeroExp(player, heroExp, Reason.SWEEP_MISSION);
		}
//        System.err.println(heroExp);
		return heroExp;
	}

	public void updateRefeshWarShopTime(Player player) {
		List<Integer> addtion = staticLimitMgr.getAddtion(287);
		if (null == addtion || addtion.size() == 0) {
			return;
		}

		int currentHour = TimeHelper.getCurrentHour();
		Integer lastHour = addtion.get(addtion.size() - 1);
		if (currentHour >= lastHour) {
			player.getLord().setWarBookShopRefreshTime(TimeHelper.getZeroTimeMs());
			return;
		}

		for (int i = 0; i < addtion.size(); i++) {
			if (i + 1 == addtion.size() && addtion.get(i) >= currentHour) {
				player.getLord().setWarBookShopRefreshTime(TimeHelper.getCityMailTime(addtion.get(i)));
				return;
			}

			if (i + 1 <= addtion.size() - 1 && addtion.get(i) >= currentHour) {
				player.getLord().setWarBookShopRefreshTime(TimeHelper.getCityMailTime(addtion.get(i + 1)));
				return;
			}
		}
	}
}