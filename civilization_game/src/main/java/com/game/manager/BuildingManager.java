package com.game.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.game.constant.*;
import com.game.pb.HeroPb;
import com.game.server.GameServer;
import com.game.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dataMgr.StaticBuildingMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.dataMgr.StaticTechMgr;
import com.game.domain.Player;
import com.game.domain.p.BuildQue;
import com.game.domain.p.Building;
import com.game.domain.p.BuildingBase;
import com.game.domain.p.Camp;
import com.game.domain.p.Command;
import com.game.domain.p.Employee;
import com.game.domain.p.Market;
import com.game.domain.p.ResBuildings;
import com.game.domain.p.ResourceInfo;
import com.game.domain.p.Staff;
import com.game.domain.p.Tech;
import com.game.domain.p.Wall;
import com.game.domain.p.Ware;
import com.game.domain.p.WorkShop;
import com.game.domain.s.StaticBuilding;
import com.game.domain.s.StaticEmployee;
import com.game.domain.s.StaticTechType;
import com.game.pb.BuildingPb;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Resource;
import com.game.pb.CommonPb.Resource.Builder;
import com.game.util.GameHelper;
import com.game.util.LogHelper;
import com.game.util.SynHelper;

@Component
public class BuildingManager {

	@Autowired
	private StaticBuildingMgr staticBuildingMgr;

	@Autowired
	private StaticTechMgr staticTechMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private RankManager rankManager;

	@Autowired
	private StaticOpenManger staticOpenManger;

	@Autowired
	private BeautyManager beautyManager;

	// 创建雇佣官
	public Employee createEmployee(int employeeId) {
		StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
		Employee employee = new Employee();
		employee.setUseTimes(0);
		employee.setEmployeeId(employeeId);
		employee.setEndTime(staticEmployee.getDurationTime() + System.currentTimeMillis());
		return employee;
	}

	public Employee createBlackEmployee(int employeeId) {
		Employee employee = new Employee();
		employee.setUseTimes(0);
		employee.setEmployeeId(employeeId);

		return employee;
	}

	// wrap buildings : 包装所有建筑等级
	public void wrapBuildings(BuildingPb.GetBuildingRs.Builder builder, Building building) {
		// 司令部
		Command command = building.getCommand();
		if (command.getLv() > 0) {
			builder.addBuilding(command.wrapBase());
		}

		// 科技
		Tech tech = building.getTech();
		if (tech.getLv() > 0) {
			builder.addBuilding(tech.wrapBase());
		}

		// 兵营
		Camp camp = building.getCamp();
		Map<Integer, BuildingBase> campInfo = camp.getCamp();
		for (Map.Entry<Integer, BuildingBase> elem : campInfo.entrySet()) {
			BuildingBase campBase = elem.getValue();
			if (campBase == null) {
				LogHelper.CONFIG_LOGGER.info("camBase");
				continue;
			}

			if (campBase.getLevel() <= 0) {
				continue;
			}

			builder.addBuilding(campBase.wrapPb());

		}

		// 城墙
		Wall wall = building.getWall();
		if (wall.getLv() > 0) {
			builder.addBuilding(wall.wrapBase());
		}

		// 仓库
		Ware ware = building.getWare();
		if (ware.getLv() > 0) {
			builder.addBuilding(ware.wrapBase());
		}

		// 工坊
		WorkShop workShop = building.getWorkShop();
		if (workShop.getLv() > 0) {
			builder.addBuilding(workShop.wrapBase());
		}

		// 资源建筑
		ResBuildings resBuildings = building.getResBuildings();
		Map<Integer, BuildingBase> resbaseMap = resBuildings.getRes();
		for (Map.Entry<Integer, BuildingBase> elem : resbaseMap.entrySet()) {
			BuildingBase resBase = elem.getValue();
			if (resBase == null) {
				continue;
			}

			if (resBase.getLevel() <= 0) {
				continue;
			}

			builder.addBuilding(resBase.wrapPb());
		}

		// 参谋部建筑
		Staff staff = building.getStaff();
		if (staff.getLv() > 0) {
			builder.addBuilding(staff.wrapBase());
		}

		Market market = building.getMarket();
		if (market.getLv() > 0) {
			builder.addBuilding(market.wrapBase());
		}

	}

	// wrap BuildQue : 包装所有升级队列
	public void wrapBuildQue(BuildingPb.GetBuildingRs.Builder builder, Building building) {
		ConcurrentLinkedDeque<BuildQue> buildQues = building.getBuildQues();
		for (BuildQue buildQue : buildQues) {
			if (buildQue != null) {
				builder.addBuildQue(buildQue.wrapPb());
			}
		}
	}

	public long getIron(ResBuildings resBuildings) {
		if (resBuildings == null) {
			return 0L;
		}
		return getTotalResource(resBuildings, ResourceType.IRON);
	}

	public CommonPb.Resource.Builder wrapResourcePb(ResBuildings resBuildings) {
		CommonPb.Resource.Builder resource = CommonPb.Resource.newBuilder();
		resource.setIron(getIron(resBuildings));
		resource.setCopper(getCopper(resBuildings));
		resource.setOil(getOil(resBuildings));
		resource.setStone(getStone(resBuildings));
		return resource;
	}

	public long getCopper(ResBuildings resBuildings) {
		return getTotalResource(resBuildings, ResourceType.COPPER);
	}

	public long getOil(ResBuildings resBuildings) {
		return getTotalResource(resBuildings, ResourceType.OIL);
	}

	public long getStone(ResBuildings resBuildings) {
		return getTotalResource(resBuildings, ResourceType.STONE);
	}

	public long getTotalResource(ResBuildings resBuildings, int targetType) {
		long total = 0;
		Map<Integer, BuildingBase> res = resBuildings.getRes();
		for (Map.Entry<Integer, BuildingBase> elem : res.entrySet()) {
			BuildingBase resBuilding = elem.getValue();
			if (resBuilding == null) {
				continue;
			}

			int buildingType = staticBuildingMgr.getBuildingType(resBuilding.getBuildingId());
			if (buildingType == Integer.MIN_VALUE) {
				LogHelper.CONFIG_LOGGER.info("ERROR BUILDING TYPE CONFIG");
				continue;
			}

			// 取得资源类型
			int resourceType = staticBuildingMgr.getResourceType(buildingType);
			if (resourceType == -1) {
				LogHelper.CONFIG_LOGGER.info("resourceType == -1");
				continue;
			}

			if (resourceType != targetType) {
				continue;
			}

			long num = staticBuildingMgr.getResource(buildingType, resBuilding.getLevel());
			total += num;
		}
		return total;
	}

	// 获取基础资源
	public CommonPb.Resource.Builder getResource(ResBuildings resBuildings) {
		CommonPb.Resource.Builder builder = CommonPb.Resource.newBuilder();
		// base
		long toatlIron = getIron(resBuildings);
		long totalCopper = getCopper(resBuildings);
		long totalOil = getOil(resBuildings);
		long totalStone = getStone(resBuildings);
		builder.setIron(toatlIron);
		builder.setCopper(totalCopper);
		builder.setOil(totalOil);
		builder.setStone(totalStone);

		return builder;
	}

	// 获得单次资源数
	public ResourceInfo getSingleResource(Player player) {
		ResourceInfo resourceInfo = new ResourceInfo();
		Building buildings = player.buildings;
		if (buildings == null) {
			return resourceInfo;
		}
		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return resourceInfo;
		}
		// base
		long toatlIron = getIron(resBuildings);
		long totalCopper = getCopper(resBuildings);
		long totalOil = getOil(resBuildings);
		long totalStone = getStone(resBuildings);
		resourceInfo.setIron(toatlIron);
		resourceInfo.setCopper(totalCopper);
		resourceInfo.setOil(totalOil);
		resourceInfo.setStone(totalStone);

		return resourceInfo;
	}

	public long getSingleResource(Player player, int id) {
		if (id == ResourceType.IRON) {
			return player.getIron();
		} else if (id == ResourceType.COPPER) {
			return player.getCopper();
		} else if (id == ResourceType.OIL) {
			return player.getOil();
		} else if (id == ResourceType.STONE) {
			return player.getStone();
		}
		return 0;
	}

	public boolean isTechOk(int techType) {
		StaticTechType staticTechType = staticTechMgr.getStaticTechType(techType);
		if (staticTechType == null) {
			return false;
		}

		return true;
	}

	// 获取基础铁资源
	public long getBaseIron(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return 0L;
		}

		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return 0L;
		}

		return getIron(resBuildings);
	}

	// 获取基础铜资源
	public long getBaseCopper(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return 0L;
		}

		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return 0L;
		}

		return getCopper(resBuildings);
	}

	// 获取基础石油资源
	public long getBaseOil(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return 0L;
		}

		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return 0L;
		}

		return getOil(resBuildings);
	}

	// 获取基础宝石资源
	public long getBaseStone(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return 0L;
		}

		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return 0L;
		}

		return getStone(resBuildings);
	}

	//资源建筑resType大于等于resLevel级的个数
	public int getResouceNum(Player player, int resType, int resLevel) {
		Building building = player.buildings;
		if (building == null) {
			LogHelper.CONFIG_LOGGER.info("doUpResBuilding building is null");
			return 0;
		}

		// 资源建筑
		ResBuildings resBuildings = building.getResBuildings();
		if (resBuildings == null) {
			LogHelper.CONFIG_LOGGER.info("doUpResBuilding resBuildings is null");
			return 0;

		}

		Map<Integer, BuildingBase> res = resBuildings.getRes();
		if (res == null) {
			LogHelper.CONFIG_LOGGER.info("doUpResBuilding res is null");
			return 0;
		}

		int count = 0;
		for (Map.Entry<Integer, BuildingBase> elem : res.entrySet()) {
			if (elem == null) {
				continue;
			}
			BuildingBase buildingBase = elem.getValue();
			if (buildingBase == null) {
				continue;
			}

			int buildingId = buildingBase.getBuildingId();
			int buildingType = staticBuildingMgr.getBuildingType(buildingId);
			if (buildingType == Integer.MIN_VALUE) {
				LogHelper.CONFIG_LOGGER.info("ERROR BUILDING TYPE CONFIG");
				continue;
			}

			// 取得资源类型
			int resourceType = staticBuildingMgr.getResourceType(buildingType);
			if (resourceType == -1) {
				LogHelper.CONFIG_LOGGER.info("resourceType == -1");
				continue;
			}

			if (resourceType != resType) {
				continue;
			}

			if (buildingBase.getLevel() >= resLevel) {
				++count;
			}
		}

		return count;
	}

	public boolean isResouceBuilding(int buildingId) {
		int buildingType = staticBuildingMgr.getBuildingType(buildingId);
		if (buildingType == Integer.MIN_VALUE) {
			return false;
		}

		// 取得资源类型
		int resourceType = staticBuildingMgr.getResourceType(buildingType);
		if (resourceType == -1) {
			return false;
		}

		return true;
	}


	public boolean hasEnoughResounce(Player player, int resourceType, int resouceNum) {
		long currentNum = getSingleResource(player, resourceType);
		return currentNum >= resouceNum;
	}


	public void helpResourceAdd(CommonPb.Resource.Builder resAdd, CommonPb.Resource.Builder addition) {
		resAdd.setIron(resAdd.getIron() + addition.getIron());
		resAdd.setCopper(resAdd.getCopper() + addition.getCopper());
		resAdd.setOil(resAdd.getOil() + addition.getOil());
		resAdd.setStone(resAdd.getStone() + addition.getStone());
	}


	public ResourceInfo getTotalRes(Player player) {
		ResourceInfo resourceInfo = new ResourceInfo();
		CommonPb.Resource.Builder resAdd = getAllResAdd(player);
		resourceInfo.setIron(resAdd.getIron());
		resourceInfo.setOil(resAdd.getOil());
		resourceInfo.setCopper(resAdd.getCopper());
		resourceInfo.setStone(resAdd.getStone());

		return resourceInfo;
	}

	// 升级建筑等级
	public void addBuildingLv(Player player, int id, int lv, int reason) {
		try {
			BuildingBase buildingBase = getBuilding(player, id);
			if (buildingBase == null) {
				return;
			}
			buildingBase.setLevel(lv);
			LogHelper.CONFIG_LOGGER.info("add building lv =" + reason);

		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
		}

	}


	public BuildingBase getBuilding(Player player, int id) {
		Command command = player.getCommand();
		// 司令部
		if (command.getBuildingId() == id) {
			return command.getBase();
		}

		// 科技
		Tech tech = player.getTech();
		if (tech.getBuildingId() == id) {
			return tech.getBase();
		}

		// 资源建筑
		BuildingBase buildingBase = player.getResBuilding(id);
		if (buildingBase != null) {
			return buildingBase;
		}

		// 仓库
		Ware ware = player.getWare();
		if (ware.getBuildingId() == id) {
			return ware.getBase();
		}

		// 城墙
		Wall wall = player.getWall();
		if (wall.getBuildingId() == id) {
			return wall.getBase();
		}

		// 作坊
		WorkShop workShop = player.buildings.getWorkShop();
		if (workShop.getBuildingId() == id) {
			return workShop.getBase();
		}

		// 兵营
		Camp camp = player.buildings.getCamp();
		BuildingBase campBase = camp.getCamp().get(id);
		if (campBase != null) {
			return campBase;
		}
		//参谋部

		Staff staff = player.buildings.getStaff();
		if (staff != null && staff.getBase().getBuildingId() == id) {
			return staff.getBase();
		}
		Market market = player.buildings.getMarket();
		if (market != null && market.getBase().getBuildingId() == id) {
			return market.getBase();
		}

		return null;

	}

	public int getBuildingScore(Player player) {
		int battleScore = 0;
		Command command = player.getCommand();
		// 司令部
		if (command != null) {
			battleScore += staticBuildingMgr.getBattlScore(command.getBuildingId(), command.getLv());
		}

		// 科技
		Tech tech = player.getTech();
		if (tech != null) {
			battleScore += staticBuildingMgr.getBattlScore(tech.getBuildingId(), tech.getLv());

		}

		// 资源建筑
		Building buildings = player.buildings;
		if (buildings != null) {
			ResBuildings resBuildings = buildings.getResBuildings();
			if (resBuildings != null) {
				Map<Integer, BuildingBase> res = resBuildings.getRes();
				if (res != null) {
					for (BuildingBase buildingBase : res.values()) {
						if (buildingBase == null) {
							continue;
						}
						battleScore += staticBuildingMgr.getBattlScore(buildingBase.getBuildingId(), buildingBase.getLevel());
					}
				}
			}
		}

		// 仓库
		Ware ware = player.getWare();
		if (ware != null) {
			battleScore += staticBuildingMgr.getBattlScore(ware.getBuildingId(), ware.getLv());
		}

		// 城墙
		Wall wall = player.getWall();
		if (wall != null) {
			battleScore += staticBuildingMgr.getBattlScore(wall.getBuildingId(), wall.getLv());
		}

		// 作坊
		WorkShop workShop = player.buildings.getWorkShop();
		if (workShop != null) {
			battleScore += staticBuildingMgr.getBattlScore(workShop.getBuildingId(), workShop.getLv());
		}

		// 兵营
		Camp camp = player.buildings.getCamp();
		if (camp != null) {
			Map<Integer, BuildingBase> campBase = camp.getCamp();
			if (campBase != null) {
				for (BuildingBase buildingBase : campBase.values()) {
					if (buildingBase == null) {
						continue;
					}
					battleScore += staticBuildingMgr.getBattlScore(buildingBase.getBuildingId(), buildingBase.getLevel());
				}
			}
		}

		//参谋部
		Staff staff = player.buildings.getStaff();
		if (staff != null) {
			if (staff.getBase() != null && staff.getBase().getLevel() > 0) {
				battleScore += staticBuildingMgr.getBattlScore(staff.getBuildingId(), staff.getLv());
			}
		}

		Market market = player.buildings.getMarket();
		if (market != null) {
			if (market.getBase() != null && market.getBase().getLevel() > 0) {
				battleScore += staticBuildingMgr.getBattlScore(market.getBuildingId(), market.getLv());
			}
		}
		return battleScore;
	}

	public int getBuildType(Player player) {
		Building buildings = player.buildings;
		// 检查当前建造队列类型
		ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
		int teamType = 1;
		if (buildQues.isEmpty()) {
			teamType = 1;
		} else if (buildQues.size() == 1) {
			BuildQue buildQue = buildQues.getFirst();
			if (buildQue != null) {
				teamType = ((buildQue.getBuildQueType() == 1) ? 2 : 1);
			}
		} else {
			return 1;
		}
		return teamType;
	}


	public CommonPb.Resource.Builder getAllResAdd(Player player) {
		CommonPb.Resource.Builder resAdd = CommonPb.Resource.newBuilder();
		Building building = player.buildings;
		if (building == null) {
			LogHelper.CONFIG_LOGGER.error("building is null!");
			return resAdd;
		}

		List<CommonPb.Resource.Builder> resources = new ArrayList<>();

		// 基础加成
		resAdd = getResource(building.getResBuildings());

		// 检测季节加成
		int resType = worldManager.getSeasonResType();
		Builder helpSeasonAdd = helpSeasonAdd(resAdd, resType);
		resources.add(helpSeasonAdd);

		// 内政官加成
		CommonPb.Resource.Builder officerAdd = playerManager.getOfficerAdd(player);
		resources.add(officerAdd);

		// 科技加成
		CommonPb.Resource.Builder techAdd = techManager.getTechAdd(player);
		resources.add(techAdd);

		//美女加成金币征收
		int beautySkillEffectIron = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.RESOURCE_BONUS, ResourceType.IRON);
		if (0 != beautySkillEffectIron) {
			Builder additon = helpBeautyAdd(resAdd, ResourceType.IRON, beautySkillEffectIron);
			resources.add(additon);
		}

		//美女加成钢铁征收
		int beautySkillEffectCopper = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.RESOURCE_BONUS, ResourceType.COPPER);
		if (0 != beautySkillEffectCopper) {
			Builder additon = helpBeautyAdd(resAdd, ResourceType.COPPER, beautySkillEffectCopper);
			resources.add(additon);
		}

		//美女加成食物征收
		int beautySkillEffectOil = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.RESOURCE_BONUS, ResourceType.OIL);
		if (0 != beautySkillEffectOil) {
			Builder additon = helpBeautyAdd(resAdd, ResourceType.OIL, beautySkillEffectOil);
			resources.add(additon);
		}

		//美女加成晶体征收
		int beautySkillEffectStone = beautyManager.getBeautySkillEffect(player, BeautySkillTypeNew.RESOURCE_BONUS, ResourceType.STONE);
		if (0 != beautySkillEffectStone) {
			Builder additon = helpBeautyAdd(resAdd, ResourceType.STONE, beautySkillEffectStone);
			resources.add(additon);
		}

		Builder caculateResorceAdd = caculateResorceAdd(resources);
		helpResourceAdd(resAdd, caculateResorceAdd);

		return resAdd;
	}


	public CommonPb.Resource.Builder caculateResorceAdd(List<CommonPb.Resource.Builder> resources) {
		CommonPb.Resource.Builder additon = Resource.newBuilder();
		if (null != resources && resources.size() > 0) {
			for (Builder builder : resources) {
				if (null != builder) {
					additon.setCopper(additon.getCopper() + builder.getCopper());
					additon.setStone(additon.getStone() + builder.getStone());
					additon.setOil(additon.getOil() + builder.getOil());
					additon.setIron(additon.getIron() + builder.getIron());
				}
			}
		}
		return additon;
	}


	//美女加成资源
	public CommonPb.Resource.Builder helpBeautyAdd(CommonPb.Resource.Builder base, int resType, int effectValue) {
		CommonPb.Resource.Builder additon = Resource.newBuilder();
		float pre = effectValue / 100.0f;

		if (resType == ResourceType.IRON) {
			long iron = (long) (base.getIron() * pre);
			additon.setIron(iron);
		} else if (resType == ResourceType.COPPER) {
			long copper = (long) (base.getCopper() * pre);
			additon.setCopper(copper);
		} else if (resType == ResourceType.OIL) {
			long oil = (long) (base.getOil() * pre);
			additon.setOil(oil);
		} else if (resType == ResourceType.STONE) {
			long stone = (long) (base.getStone() * pre);
			additon.setStone(stone);
		}
		return additon;
	}

	// 季节翻倍
	public CommonPb.Resource.Builder helpSeasonAdd(CommonPb.Resource.Builder base, int resType) {
		CommonPb.Resource.Builder additon = Resource.newBuilder();

		if (resType == 0) {
			return null;
		}
		float resFactor = worldManager.getSeasonResFactor();
		if (resType == ResourceType.IRON) {
			long res = (long) ((float) base.getIron() * (resFactor));
			additon.setIron(res);
		} else if (resType == ResourceType.COPPER) {
			long res = (long) ((float) base.getCopper() * (resFactor));
			additon.setCopper(res);
		} else if (resType == ResourceType.OIL) {
			long res = (long) ((float) base.getOil() * (resFactor));
			additon.setOil(res);
		} else if (resType == ResourceType.STONE) {
			long res = (long) ((float) base.getStone() * (resFactor));
			additon.setStone(res);
		}

		return additon;
	}

	public int caculateBattleScore(Player player) {
		// 建筑的战力
		int buildingScore = getBuildingScore(player);
		int heroScore = player.getHeroScore();
		player.setBuildingScore(buildingScore);
		try {
			rankManager.checkRankList(player.getLord());  // 检查排行榜
		} catch (Exception ex) {
			LogHelper.ERROR_LOGGER.error(ex.getMessage());
		}

		int total = heroScore + buildingScore;
		player.setMaxScore(total);

		HeroPb.SynScoreRs.Builder builder = HeroPb.SynScoreRs.newBuilder();
		builder.setScore(player.getMaxScore());
		GameServer.getInstance().sendMsgToPlayer(player, PbHelper.createSynBase(HeroPb.SynScoreRs.EXT_FIELD_NUMBER, HeroPb.SynScoreRs.ext, builder.build()));
		return total;
	}

	public boolean isHadStoneBuild(Player player) {
		Building buildings = player.buildings;
		if (buildings == null) {
			return false;
		}

		ResBuildings resBuildings = buildings.getResBuildings();
		if (resBuildings == null) {
			return false;
		}

		BuildingBase buildBase1 = resBuildings.getBuilding(BuildingId.BUILDING_STONE_1);
		BuildingBase buildBase2 = resBuildings.getBuilding(BuildingId.BUILDING_STONE_2);
		if (buildBase1 != null || buildBase2 != null) {
			return true;
		}
		return false;
	}

	/**
	 * 通过任务开启建筑
	 *
	 * @param player
	 * @param taskId
	 */
	public void synBuildingsByTask(Player player, int taskId) {
		List<Integer> openBuilds = staticOpenManger.getBuildOpen(taskId, OpenConditionType.TASK);
		if (openBuilds.size() > 0) {
			synBuildings(player, openBuilds);
		}
	}

	/**
	 * 通过等级开启建筑
	 *
	 * @param player
	 */
	public void synBuildingsByLv(Player player) {
		List<Integer> openBuilds = staticOpenManger.getBuildOpen(player.getLevel(), OpenConditionType.PLAYER_LEVEL);
		if (openBuilds.size() > 0) {
			synBuildings(player, openBuilds);
		}
	}


	/**
	 * 通过主城等级开启建筑
	 *
	 * @param player
	 */
	public void synBuildingsByCommandlv(Player player) {
		List<Integer> openBuilds = staticOpenManger.getBuildOpen(player.buildings.getCommandLv(), OpenConditionType.COMMAND_LEVEL);
		if (openBuilds.size() > 0) {
			synBuildings(player, openBuilds);
		}
	}


	// 新手引导开启建筑
	public void synBuildings(Player player, List<Integer> openBuildingId) {
		if (openBuildingId == null || openBuildingId.isEmpty()) {
			return;
		}

		Building buildings = player.buildings;
		int buildingLv = 1;
		List<BuildingBase> buildingBases = new ArrayList<BuildingBase>();
		for (Integer buildingId : openBuildingId) {
			StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
			if (staticBuilding == null) {
				continue;
			}
			if (staticBuilding.getBuildingType() == BuildingType.COMMAND) {
				Command command = buildings.getCommand();
				if (command.getLv() >= 1) {
					continue;
				}

				command.initBase(buildingId, buildingLv);
				buildingBases.add(command.getBase());
			} else if (GameHelper.isCamp(staticBuilding.getBuildingType())) {
				Camp camp = buildings.getCamp();
				BuildingBase buildingBase = camp.getBuilding(buildingId);
				if (buildingBase != null && buildingBase.getLevel() >= 1) {
					continue;
				}
				if (staticBuilding.getBuildingType() != BuildingType.MILITIA_CAMP) {
					camp.addCamp(buildingId, buildingLv);
				}
				buildingBases.add(camp.getBuilding(buildingId));
			} else if (staticBuilding.getBuildingType() == BuildingType.TECH) {
				Tech tech = buildings.getTech();
				if (tech.getLv() >= 1) {
					continue;
				}
				tech.initBase(buildingId, buildingLv);
				buildingBases.add(tech.getBase());
			} else if (staticBuilding.getBuildingType() == BuildingType.WALL) {
				Wall wall = buildings.getWall();
				if (wall.getLv() >= 1) {
					continue;
				}
				wall.initBase(buildingId, buildingLv);
				buildingBases.add(wall.getBase());
			} else if (staticBuilding.getBuildingType() == BuildingType.WORK_SHOP) {
				WorkShop workShop = buildings.getWorkShop();
				if (workShop.getLv() >= 1) {
					continue;
				}
				workShop.initBase(buildingId, buildingLv);
				buildingBases.add(workShop.getBase());
			} else if (staticBuilding.getBuildingType() == BuildingType.WARE) {
				Ware ware = buildings.getWare();
				if (ware.getLv() >= 1) {
					continue;
				}

				ware.initBase(buildingId, buildingLv);
				buildingBases.add(ware.getBase());
			} else if (GameHelper.isResourceBuilding(staticBuilding.getBuildingType())) {
				ResBuildings resBuildings = buildings.getResBuildings();
				BuildingBase buildingBase = resBuildings.getBuilding(buildingId);
				if (buildingBase != null && buildingBase.getLevel() >= 1) {
					continue;
				}
				resBuildings.addResourceBuilding(buildingId, buildingLv);
				buildingBases.add(resBuildings.getBuilding(buildingId));
			} else if (staticBuilding.getBuildingType() == BuildingType.STAFF) {
				Staff staff = buildings.getStaff();
				if (staff.getLv() >= 1) {
					continue;
				}
				staff.initBase(buildingId, buildingLv);
				buildingBases.add(staff.getBase());
			} else if (staticBuilding.getBuildingType() == BuildingType.MARKET) {
				Market market = buildings.getMarket();
				if (market.getLv() >= 1) {
					continue;
				}
				market.initBase(buildingId, buildingLv);
				buildingBases.add(market.getBase());
			}

			//LogHelper.logBuilding(player, buildingId, buildingLv);
		}

		BuildingPb.SynBuildingRq.Builder builder = BuildingPb.SynBuildingRq.newBuilder();
		for (BuildingBase buildingBase : buildingBases) {
			if (buildingBase != null) {
				builder.addBuilding(buildingBase.wrapPb());
			}
		}

		if (player.isLogin && player.getChannelId() != -1) {
//            BasePb.Base.Builder msg = PbHelper.createSynBase(BuildingPb.SynBuildingRq.EXT_FIELD_NUMBER,
//                    BuildingPb.SynBuildingRq.ext, builder.build());
			checkOpenAutoBuild(openBuildingId, player, builder);
			SynHelper.synMsgToPlayer(player, BuildingPb.SynBuildingRq.EXT_FIELD_NUMBER, BuildingPb.SynBuildingRq.ext,
				builder.build());
			//GameServer.getInstance().synMsgToPlayer(player, msg);
		}

		// 建筑开启计算战斗力
		caculateBattleScore(player);

	}


	public void checkOpenAutoBuild(List<Integer> openBuildingId, Player player, BuildingPb.SynBuildingRq.Builder builder) {
		if (!openBuildingId.contains(36)) {
			// builder.setOnBuild(player.getOnBuild());
			return;
		}
		player.setOnBuild(1);
		builder.setOnBuild(1);
	}

	public void calRes(Player player) {
//        if(player.getOnlineMessage().getResList()!=null){
//            player.getOnlineMessage().getResList().clear();
//        }
		int collectTimes = player.getLord().getCollectTimes();
		CommonPb.Resource.Builder resAdd = this.getAllResAdd(player);
		long coper = resAdd.getCopper() * collectTimes;
		long iron = resAdd.getIron() * collectTimes;
		long oil = resAdd.getOil() * collectTimes;
		long stone = resAdd.getStone() * collectTimes;
		if (coper > 0) {
			player.getOnlineMessage().addRes(CommonPb.TwoInt.newBuilder().setV1(ResourceType.COPPER).setV2((int) coper).build());
		}
		if (iron > 0) {
			player.getOnlineMessage().addRes(CommonPb.TwoInt.newBuilder().setV1(ResourceType.IRON).setV2((int) iron).build());
		}
		if (oil > 0) {
			player.getOnlineMessage().addRes(CommonPb.TwoInt.newBuilder().setV1(ResourceType.OIL).setV2((int) oil).build());
		}
		if (stone > 0) {
			player.getOnlineMessage().addRes(CommonPb.TwoInt.newBuilder().setV1(ResourceType.STONE).setV2((int) stone).build());
		}
	}
}
