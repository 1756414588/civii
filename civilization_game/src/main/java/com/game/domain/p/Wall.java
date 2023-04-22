package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 城墙
public class Wall implements Cloneable {
    private BuildingBase base = new BuildingBase();
    // 驻防武将
    //private List<Integer> defenceHero = new ArrayList<Integer>();
    private long endTime;
    // 驻防军
    private Map<Integer, WallDefender> wallDefenders = new ConcurrentHashMap<>();
    // 友军
    private Map<Integer, WallFriend> wallFriends = new ConcurrentHashMap<Integer, WallFriend>();


    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    public int getLv() {
        return base.getLevel();
    }

    public int getBuildingId() {
        return base.getBuildingId();
    }

    public void incrementLevel() {
        base.incrementLevel();
    }

    // building wrap
    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public DataPb.BuildingData.Builder writeData() {
        return base.readData();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    public DataPb.Wall.Builder wrapPb() {
        DataPb.Wall.Builder builder = DataPb.Wall.newBuilder();
        builder.setWall(writeData());
        //if (defenceHero.size() > 0) {
        //    builder.addAllHeroId(defenceHero);
        //}
        builder.setEndTime(endTime);
        for (WallDefender defender : getWallDefenders().values()) {
            if (defender == null) {
                continue;
            }
            builder.addDefender(defender.writeData());
        }

        for (WallFriend wallFriend : getWallFriends().values()) {
            if (wallFriend == null) {
                continue;
            }

            builder.addFriend(wallFriend.writeData());

        }

        return builder;
    }

    public void unwrapPb(DataPb.Wall builder) {
        base.readData(builder.getWall());
        //defenceHero.clear();
        //defenceHero.addAll(builder.getHeroIdList());

        // 城防军
        for (DataPb.WallDefenderData defender : builder.getDefenderList()) {
            if (defender == null) {
                continue;
            }

            WallDefender wallDefender = new WallDefender();
            wallDefender.readData(defender);
            getWallDefenders().put(wallDefender.getKeyId(), wallDefender);
        }

        endTime = builder.getEndTime();
        for (DataPb.WallFriendData friend : builder.getFriendList()) {
            if (friend == null) {
                continue;
            }
            WallFriend elem = new WallFriend();
            elem.readData(friend);
            wallFriends.put(friend.getKeyId(), elem);
        }

    }

    //public List<Integer> getDefenceHero() {
    //    return defenceHero;
    //}
    //
    //public List<Integer> getDefenceDisHero() {
    //    return this.defenceHero.stream().distinct().collect(Collectors.toList());
    //}
    //
    //public void setDefenceHero(List<Integer> defenceHero) {
    //    this.defenceHero = defenceHero;
    //}

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public WallFriend getWallFriend(int marchId, long lordId) {
        for (WallFriend wallFriend : wallFriends.values()) {
            if (wallFriend.getMarchId() == marchId && wallFriend.getLordId() == lordId) {
                return wallFriend;
            }
        }
        return null;
    }


    public void removeWallFriend(int marchId, long lordId) {
        Iterator<WallFriend> iterator = wallFriends.values().iterator();
        while (iterator.hasNext()) {
            WallFriend wallFriend = iterator.next();
            if (wallFriend == null) {
                continue;
            }

            if (wallFriend.getMarchId() == marchId && wallFriend.getLordId() == lordId) {
                iterator.remove();
                break;
            }
        }

    }

    public boolean isWallDefencerExist(int keyId) {
        return wallDefenders.containsKey(keyId);
    }

    public void removeWallDefencer(int keyId) {
        wallDefenders.remove(keyId);
    }


    public WallDefender getWallDefender(int keyId) {
        return wallDefenders.get(keyId);
    }

    public void addWallDefender(WallDefender defender) {
        getWallDefenders().put(defender.getKeyId(), defender);
    }

    public void setWallFriends(HashMap<Integer, WallFriend> wallFriends) {
        this.wallFriends = wallFriends;
    }

    public Map<Integer, WallDefender> getWallDefenders() {
        return wallDefenders;
    }

    public void setWallDefenders(Map<Integer, WallDefender> wallDefenders) {
        this.wallDefenders = wallDefenders;
    }

    public Map<Integer, WallFriend> getWallFriends() {
        return wallFriends;
    }


    public WallFriend getWallFriendByHeroId(int heroId, long lordId) {
        for (WallFriend wallFriend : wallFriends.values()) {
            if (wallFriend.getHeroId() == heroId &&
                    wallFriend.getLordId() == lordId) {
                return wallFriend;
            }
        }
        return null;
    }

    public void removeWallFriendByHeroId(int heroId, long lordId) {
        Iterator<WallFriend> iterator = wallFriends.values().iterator();
        while (iterator.hasNext()) {
            WallFriend wallFriend = iterator.next();
            if (wallFriend == null) {
                continue;
            }

            if (wallFriend.getHeroId() == heroId &&
                    wallFriend.getLordId() == lordId) {
                iterator.remove();
                break;
            }
        }

    }

    public int getWallFriendSize() {
        return wallFriends.size();
    }

    @Override
    public Wall clone() {
        Wall wall = null;
        try {
            wall = (Wall) super.clone();
            wall.setBase(this.base.clone());

            ArrayList<Integer> list = new ArrayList<>();
            //this.defenceHero.forEach(integer -> {
            //    list.add(integer);
            //});
            //wall.setDefenceHero(list);

            Map<Integer, WallDefender> map = new HashMap<Integer, WallDefender>();
            this.wallDefenders.forEach((key, value) -> {
                map.put(key, value.clone());
            });
            wall.setWallDefenders(map);

            HashMap<Integer, WallFriend> map1 = new HashMap<Integer, WallFriend>();
            this.wallFriends.forEach((key, value) -> {
                map1.put(key, value.clone());
            });
            wall.setWallFriends(map1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return wall;
    }
}
