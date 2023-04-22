package com.game.manager;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.AwardType;
import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticTDMgr;
import com.game.dataMgr.StaticTaskMgr;
import com.game.dataMgr.StaticTechMgr;
import com.game.domain.Player;
import com.game.domain.p.Hero;
import com.game.domain.p.Lord;
import com.game.domain.p.TD;
import com.game.domain.p.Task;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticTowerWarBonus;
import com.game.pb.CommonPb;
import com.game.pb.TDPb;
import com.game.pb.TDPb.TDBounsRs;
import com.game.server.GameServer;
import com.game.service.BuildingService;
import com.game.service.HeroService;
import com.game.service.MijiService;
import com.game.service.TDService;
import com.game.service.TechService;
import com.game.spring.SpringUtil;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description测试用户类
 * @Date 2023/3/21 11:33
 **/

@Component
public class PlayerTestManager {

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;
	@Autowired
	private BuildingManager buildingManager;
	@Autowired
	private BuildingService buildingService;
	@Autowired
	private MissionManager missionManager;
	@Autowired
	private TechService techService;
	@Autowired
	private StaticTechMgr staticTechMgr;
	@Autowired
	private MijiService mijiService;
	@Autowired
	private HeroService heroService;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private StaticHeroMgr staticHeroMgr;
	@Autowired
	private TDManager tdManager;
	@Autowired
	private StaticTDMgr staticTDMgr;
	@Autowired
	private TDService tdService;
	@Autowired
	private StaticTaskMgr staticTaskMgr;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private StaticEquipDataMgr staticEquipDataMgr;
	@Autowired
	private PlayerManager playerManager;


	public boolean isTestAccount(Message message) {
		// 服务器模式为测试模式
		Server server = SpringUtil.getBean(ServerManager.class).getServer();
		if (server.isGmOpen()) {// 测试服
			String data = message.getData();
			com.game.uc.Account ucAccount = JSONObject.parseObject(data, com.game.uc.Account.class);
			if (ucAccount.getChannel() == 1 && ucAccount.getAccount().startsWith("lz")) {// 直接满级的账号
				return true;
			}
		}
		return false;
	}


	/**
	 * 创建测试角色
	 *
	 * @param player
	 */
	public void createTestPlayer(Player player) {
		// 角色等级
		Lord lord = player.getLord();
		lord.setVip(12);
		lord.setLevel(150);
		lord.setGold(999999999);
		lord.setEnergy(120000);

		// 新手引导设置完成
		lord.setGuideKey(103300);
		lord.setNewState(103300);
		lord.setMaxMonsterLv(15);

		// 资源
		Map<Integer, Long> resource = player.getResource().getResource();
		resource.put(ResourceType.IRON, 999999999L);
		resource.put(ResourceType.COPPER, 999999999L);
		resource.put(ResourceType.OIL, 999999999L);
		resource.put(ResourceType.STONE, 999999999L);

		// 背包容量最大
		int maxBuyTimes = staticEquipDataMgr.maxBuyEquipSlotTimes();
		lord.setBuyEquipSlotTimes(maxBuyTimes);

		// 开启建筑,并且将所有的建筑升至满级
		buildingManager.synBuildings(player, staticBuildingMgr.getBuildIds());

		staticBuildingMgr.getBuildTypeMap().values().forEach(e -> {
			if (e.getResourceType() == 0) {// 主城建筑需要收复
				player.buildings.getRecoverBuilds().add(e.getBuildingType());
			}
			buildingService.gmUpBuildLevel(player, e.getBuildingType(), e.getMaxLv());
		});

		// 资源建筑升级到30
		player.buildings.getResBuildings().getRes().values().forEach(e -> {
			e.setLevel(30);
		});

		// 科技满级
		staticTechMgr.getTechTypeMap().values().forEach(e -> {
			techService.gmLevelUpTech(e.getTechType(), e.getMaxLevel(), player);
		});

		// 副本全开
		missionManager.openAllMission(player);

		// 添加全部的装备
		for (StaticEquip staticEquip : staticEquipDataMgr.getEquipMap().values()) {
			if (staticEquip.getQuality() == 6) {
				playerManager.addAward(player, AwardType.EQUIP, staticEquip.getEquipId(), 1, Reason.GM_ADD_GOODS);
			}
		}
		mijiService.SycEquipChange(player);

		// 任务做完
		Map<Integer, Task> taskMap = player.getTaskMap();
		taskMap.clear();// 清理掉初始化的任务
		lord.setCurMainTask(29800);// 最后一个任务
		staticTaskMgr.getTaskMap().values().forEach(staticTask -> {
			int maxProcess = staticTask.getProcess();
			Task task = taskManager.addTask(staticTask.getTaskId(), taskMap);
			task.setProcess(maxProcess);
			task.setCond(maxProcess);
			task.setStatus(2);
			player.getFinishedTask().add(staticTask.getTaskId());
		});

		// 英雄全满级
		Map<Integer, Hero> heroMap = player.getHeros();
		staticHeroMgr.getHeroMap().values().forEach(staticHero -> {
			if (staticHero.getAdvancedId() == 0 && staticHero.getRareLevel() != 0) {
				Hero hero = new Hero();
				// 初始化英雄基础属性
				hero.init(staticHero);
				heroManager.caculateProp(hero, player);
				// 添加英雄
				heroMap.put(staticHero.getHeroId(), hero);
				heroService.heroLvUp(player, staticHero.getHeroId(), 150);
			}
		});

		// 塔防通关
		initTD(player);

		//
	}


	private void initTD(Player player) {
		List<Entry<Integer, Integer>> list = player.getTdBouns().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey())).collect(Collectors.toList());
		TDPb.TDBounsRs.Builder builder = TDPb.TDBounsRs.newBuilder();
		tdManager.getBounds(player).forEach(e -> {
			builder.addPowers(e);
		});
		player.getTdBouns().forEach((e, f) -> {
			StaticTowerWarBonus bonus = staticTDMgr.getTowerWarBonusMap(f);
			builder.addBounds(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(bonus.getEffect()).build());
		});
		GameServer.getInstance().sendMsgToPlayer(player, BasePbHelper.createRqBase(TDBounsRs.EXT_FIELD_NUMBER, TDPb.TDBounsRs.ext, builder.build()));

		// 开启第一关
		tdService.initPlayerTdMap(player);

		// 通关塔防经典
		player.getTdMap().values().forEach(e -> {
			e.setStar(3);// 3通关星
			e.setState(1);// 通关且已领取奖励
		});
	}
}
