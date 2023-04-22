package com.game.domain.p;

import com.game.spring.SpringUtil;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.game.constant.BuildingType;
import com.game.pb.CommonPb;
import com.game.service.WorldTargetTaskService;
import com.game.util.TimeHelper;

public class Building implements Cloneable {
    private Command command;                //司令部
    private Tech tech;                   //科技
    private Camp camp;                   //兵营
    private Wall wall;                   //城墙
    private Ware ware;                   //仓库
    private WorkShop workShop;              //补给站
    private ResBuildings resBuildings;      //资源建筑
    private ConcurrentLinkedDeque<BuildQue> buildQues;       //升级队列, 队列个数=建造队个数，默认一个, 时间到的建筑，从队列里面删除
    private int buildingTeams;              //自带建造队个数 = 默认1个
    private LinkedList<WorkQue> equipWorkQue; // 铁匠铺
    private Staff staff;                      // 参谋部
    private Market market;                    //市场
    private List<Integer> recoverBuilds;      //收复的建筑
    private Omament omament;      //配饰

    public Building() {
        setCommand(new Command());
        setCamp(new Camp());
        setTech(new Tech());
        setWall(new Wall());
        setWare(new Ware());
        setStaff(new Staff());
        setMarket(new Market());
        setWorkShop(new WorkShop());
        setResBuildings(new ResBuildings());
        buildQues = new ConcurrentLinkedDeque<BuildQue>();
        equipWorkQue = new LinkedList<WorkQue>();
        recoverBuilds = new ArrayList<>();
        setOmament(new Omament());

    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public Camp getCamp() {
        return camp;
    }

    public void setCamp(Camp camp) {
        this.camp = camp;
    }

    public Tech getTech() {
        return tech;
    }

    public void setTech(Tech tech) {
        this.tech = tech;
    }

    public Wall getWall() {
        return wall;
    }

    public void setWall(Wall wall) {
        this.wall = wall;
    }

    public WorkShop getWorkShop() {
        return workShop;
    }

    public void setWorkShop(WorkShop workShop) {
        this.workShop = workShop;
    }

    public ResBuildings getResBuildings() {
        return resBuildings;
    }

    public void setResBuildings(ResBuildings resBuildings) {
        this.resBuildings = resBuildings;
    }


    public int getCommandLv() {
        return command.getLv();
    }


    public ConcurrentLinkedDeque<BuildQue> getBuildQues() {
        return buildQues;
    }

    public void setBuildQues(ConcurrentLinkedDeque<BuildQue> buildQues) {
        this.buildQues = buildQues;
    }

    public int getBuildingTeams() {
        return buildingTeams;
    }

    public void setBuildingTeams(int buildingTeams) {
        this.buildingTeams = buildingTeams;
    }

    // 获得司令部Id
    public int getCommandId() {
        return command.getBuildingId();
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Omament getOmament() {
        return omament;
    }

    public void setOmament(Omament omament) {
        this.omament = omament;
    }

    // 检查建筑正在升级
    public boolean isBuildUping(int buildingId) {
        for (BuildQue item : buildQues) {
            if (item.getBuildingId() != buildingId) {
                continue;
            }

            long leftTime = TimeHelper.getLeftTime(item.getEndTime());
            if (leftTime > 0) {
                return true;
            }
        }

        return false;
    }

    public int getBuildQueSize() {
        return buildQues.size();
    }


    public boolean isCamp(int buildingType) {
        return buildingType == BuildingType.ROCKET_CAMP ||
                buildingType == BuildingType.TANK_CAMP ||
                buildingType == BuildingType.WAR_CAR_CAMP ||
                buildingType == BuildingType.MILITIA_CAMP;
    }

    public boolean isResourceBuilding(int buildingType) {
        return buildingType == BuildingType.IRON ||
                buildingType == BuildingType.COPPER ||
                buildingType == BuildingType.OIL ||
                buildingType == BuildingType.STONE;
    }


    // 获取当前建筑等级
    public int getBuildingLv(int buildingType, int buildingId) {
        if (buildingType == BuildingType.COMMAND) {
            return command.getLv();
        } else if (buildingType == BuildingType.TECH) {
            return tech.getLv();
        } else if (isCamp(buildingType)) {
            return camp.getBuildingLv(buildingId);
        } else if (buildingType == BuildingType.WALL) {
            return wall.getLv();
        } else if (buildingType == BuildingType.WARE) {
            return getWare().getLv();
        } else if (buildingType == BuildingType.WORK_SHOP) {
            return workShop.getLv();
        } else if (isResourceBuilding(buildingType)) {
            return resBuildings.getBuildingLv(buildingId);
        } else if (buildingType == BuildingType.STAFF) {
            return staff.getLv();
        } else if (buildingType == BuildingType.MARKET) {
            return market.getLv();
        } else if (buildingType == BuildingType.OMAMENT) {
            return omament.getLv();
        }

        return Integer.MIN_VALUE;
    }

    // 升级建筑
    public void levelupBuilding(int buildingType, int buildingId) {
        if (buildingType == BuildingType.COMMAND) {
            command.incrementLevel();
            if (command.getLv() == 7) {
                SpringUtil.getBean(WorldTargetTaskService.class).updateWorldTaskTarget();
            }
        } else if (buildingType == BuildingType.TECH) {
            tech.incrementLevel();
        } else if (isCamp(buildingType)) {
            camp.incrementLevel(buildingId);
        } else if (buildingType == BuildingType.WALL) {
            wall.incrementLevel();
        } else if (buildingType == BuildingType.WARE) {
            ware.incrementLevel();
        } else if (buildingType == BuildingType.WORK_SHOP) {
            workShop.incrementLevel();
        } else if (isResourceBuilding(buildingType)) {
            resBuildings.incrementLevel(buildingId);
        } else if (buildingType == BuildingType.STAFF) {
            staff.incrementLevel();
        } else if (buildingType == BuildingType.MARKET) {
            market.incrementLevel();
        }
    }

    // 升级建筑到多少级， gm命令使用
    public void gmlevelupBuilding(int buildingType, int buildingId, int level) {
        if (buildingType == BuildingType.COMMAND) {
            command.getBase().setLevel(level);
        } else if (buildingType == BuildingType.TECH) {
            tech.getBase().setLevel(level);
        } else if (isCamp(buildingType)) {
            camp.gmIncrementLevel(buildingId, level);
        } else if (buildingType == BuildingType.WALL) {
            wall.getBase().setLevel(level);
        } else if (buildingType == BuildingType.WARE) {
            ware.getBase().setLevel(level);
        } else if (buildingType == BuildingType.WORK_SHOP) {
            workShop.getBase().setLevel(level);
        } else if (isResourceBuilding(buildingType)) {
            resBuildings.gmIncrementLevel(buildingId, level);
        } else if (buildingType == BuildingType.STAFF) {
            staff.getBase().setLevel(level);
        } else if (buildingType == BuildingType.MARKET) {
            market.getBase().setLevel(level);
        }
    }


    // 打包Base Pb
    //building wrap
    public CommonPb.Building.Builder wrapBase(int buildingType, int buildingId) {
        if (buildingType == BuildingType.COMMAND) {
            return command.wrapBase();
        } else if (buildingType == BuildingType.TECH) {
            return tech.wrapBase();
        } else if (isCamp(buildingType)) {
            return camp.wrapBase(buildingId);
        } else if (buildingType == BuildingType.WALL) {
            return wall.wrapBase();
        } else if (buildingType == BuildingType.WARE) {
            return getWare().wrapBase();
        } else if (buildingType == BuildingType.WORK_SHOP) {
            return workShop.wrapBase();
        } else if (isResourceBuilding(buildingType)) {
            return resBuildings.wrapBase(buildingId);
        } else if (buildingType == BuildingType.STAFF) {
            return staff.wrapBase();
        } else if (buildingType == BuildingType.MARKET) {
            return market.wrapBase();
        }

        return null;
    }

    public Ware getWare() {
        return ware;
    }

    public void setWare(Ware ware) {
        this.ware = ware;
    }


    public BuildQue getBuildQue(int buildingId) {
        for (BuildQue buildQue : buildQues) {
            if (buildQue.getBuildingId() == buildingId) {
                return buildQue;
            }
        }

        return null;
    }

    public void removeBuildQue(int buildingId) {
        Iterator<BuildQue> iterator = buildQues.iterator();
        while (iterator.hasNext()) {
            BuildQue e = iterator.next();
            if (e.getBuildingId() == buildingId) {
                iterator.remove();
                break;
            }
        }
    }

    public LinkedList<WorkQue> getEquipWorkQue() {
        return equipWorkQue;
    }

    public void setEquipWorkQue(LinkedList<WorkQue> equipWorkQue) {
        this.equipWorkQue = equipWorkQue;
    }


    public BuildingBase openResBuilding(int buildingId) {
        return resBuildings.openResBuilding(buildingId);
    }

    public int getBuildingLv(int buildingId) {
        int resouceLv = resBuildings.getBuildingLv(buildingId);
        if (resouceLv != Integer.MIN_VALUE) {
            return resouceLv;
        }

        if (command.getBuildingId() == buildingId) {
            return command.getLv();
        }

        if (tech.getBuildingId() == buildingId) {
            return tech.getLv();
        }

        int campLv = camp.getBuildingLv(buildingId);
        if (campLv != Integer.MIN_VALUE) {
            return campLv;
        }

        if (wall.getBuildingId() == buildingId) {
            return wall.getLv();
        }

        if (ware.getBuildingId() == buildingId) {
            return ware.getLv();
        }

        if (workShop.getBuildingId() == buildingId) {
            return workShop.getLv();
        }

        if (staff.getBuildingId() == buildingId) {
            return staff.getLv();
        }
        if (market.getBuildingId() == buildingId) {
            return market.getLv();
        }

        return -1;
    }


    public BuildingBase getBuilding(int buildingId) {
        BuildingBase buildingBase = resBuildings.getBuilding(buildingId);
        if (buildingBase != null) {
            return buildingBase;
        }

        if (command.getBuildingId() == buildingId) {
            return command.getBase();
        }

        if (tech.getBuildingId() == buildingId) {
            return tech.getBase();
        }

        BuildingBase campBase = camp.getBuilding(buildingId);
        if (campBase != null) {
            return campBase;
        }

        if (wall.getBuildingId() == buildingId) {
            return wall.getBase();
        }

        if (ware.getBuildingId() == buildingId) {
            return ware.getBase();
        }

        if (workShop.getBuildingId() == buildingId) {
            return workShop.getBase();
        }

        if (staff.getBuildingId() == buildingId) {
            return staff.getBase();
        }

        if (market.getBuildingId() == buildingId) {
            return market.getBase();
        }

        return null;
    }


    public TreeSet<Integer> getBuildingIds() {
        TreeSet<Integer> buildingIds = new TreeSet<Integer>();
        Map<Integer, BuildingBase> resBuilding = resBuildings.getRes();
        for (BuildingBase buildingBase : resBuilding.values()) {
            if (buildingBase.getBuildingId() <= 0) {
                continue;
            }
            buildingIds.add(buildingBase.getBuildingId());
        }

        if (command.getBuildingId() > 0) {
            buildingIds.add(command.getBuildingId());
        }

        if (tech.getBuildingId() > 0) {
            buildingIds.add(tech.getBuildingId());
        }


        Map<Integer, BuildingBase> campBase = camp.getCamp();
        for (BuildingBase buildingBase : campBase.values()) {
            if (buildingBase.getBuildingId() <= 0) {
                continue;
            }
            buildingIds.add(buildingBase.getBuildingId());

        }

        if (wall.getBuildingId() > 0) {
            buildingIds.add(wall.getBuildingId());
        }

        if (ware.getBuildingId() > 0) {
            buildingIds.add(ware.getBuildingId());
        }

        if (workShop.getBuildingId() > 0) {
            buildingIds.add(workShop.getBuildingId());
        }

        if (staff.getBuildingId() > 0) {
            buildingIds.add(staff.getBuildingId());
        }
        if (market.getBuildingId() > 0) {
            buildingIds.add(market.getBuildingId());
        }

        return buildingIds;

    }

    public void setLevel(int buildingType, int buildingId, int level) {
        if (buildingType == BuildingType.COMMAND) {
            command.getBase().setLevel(level);
        } else if (buildingType == BuildingType.TECH) {
            tech.getBase().setLevel(level);
        } else if (isCamp(buildingType)) {
            camp.setLevel(buildingId, level);
        } else if (buildingType == BuildingType.WALL) {
            wall.getBase().setLevel(level);
        } else if (buildingType == BuildingType.WARE) {
            ware.getBase().setLevel(level);
        } else if (buildingType == BuildingType.WORK_SHOP) {
            workShop.getBase().setLevel(level);
        } else if (isResourceBuilding(buildingType)) {
            resBuildings.setLevel(buildingId, level);
        } else if (buildingType == BuildingType.STAFF) {
            staff.getBase().setLevel(level);
        } else if (buildingType == BuildingType.MARKET) {
            market.getBase().setLevel(level);
        }
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }


    public List<Integer> getRecoverBuilds() {
        return recoverBuilds;
    }

    public void setRecoverBuilds(List<Integer> recoverBuilds) {
        this.recoverBuilds = recoverBuilds;
    }

    @Override
    public Building clone() {
        Building building = null;
        try {
            building = (Building) super.clone();
            building.setCommand(this.command.clone());
            building.setTech(this.tech.clone());
            building.setCamp(this.camp.clone());
            building.setWall(this.wall.clone());
            building.setWare(this.ware.clone());
            building.setWorkShop(this.workShop.clone());
            building.setResBuildings(this.resBuildings.clone());

            ConcurrentLinkedDeque<BuildQue> list = new ConcurrentLinkedDeque<>();
            this.buildQues.forEach(value -> {
                list.add(value.clone());
            });
            building.setBuildQues(list);

            LinkedList<WorkQue> list1 = new LinkedList<>();
            this.equipWorkQue.forEach(value -> {
                list1.add(value.clone());
            });
            building.setEquipWorkQue(list1);
            building.setStaff(this.staff.clone());
            building.setMarket(this.market.clone());

            ArrayList<Integer> list2 = new ArrayList<>();
            this.recoverBuilds.forEach(value -> {
                list2.add(value);
            });
            building.setRecoverBuilds(list2);

            building.setOmament(this.omament.clone());

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return building;
    }

}
