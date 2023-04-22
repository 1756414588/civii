package com.game.domain;

import com.game.constant.WarType;
import com.game.worldmap.WarInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarAssemble {

    private WarInfo info;
    private List<WarInfo> infos = new ArrayList<>();
    private Map<Integer, Integer> mapNum = new HashMap<>();

    public WarInfo getInfo() {
        return info;
    }

    public void setInfo(WarInfo info) {
        this.info = info;
    }

    public List<WarInfo> getInfos() {
        return infos;
    }

    public void setInfos(List<WarInfo> infos) {
        this.infos = infos;
    }


    public void addNum(int key, int num) {
        int type = 1;
        if (key == WarType.ATTACK_COUNTRY) {

        } else if (key == WarType.Attack_WARFARE || key == WarType.ATTACK_FAR || key == WarType.ATTACK_QUICK) {
            type = 2;
        } else if (key == WarType.BIGMONSTER_WAR) {
            type = 3;
        } else if (key == WarType.REBEL_WAR) {
            type = 4;
        }
        mapNum.merge(type, num, (a, b) -> a + b);
    }

    public Map<Integer, Integer> getMapNum() {
        return mapNum;
    }

    public void setMapNum(Map<Integer, Integer> mapNum) {
        this.mapNum = mapNum;
    }

    public void flush(HashSet<WarInfo> set) {
        //此处前端要求必须发数据
        Stream.of(1, 2, 3, 4).forEach(x -> {
            mapNum.put(x, 0);
        });
        set.forEach(x -> {
            addNum(x.getWarType(), 1);
            if (info == null) {
                info = x;
            } else {
                if (x.getEndTime() < info.getEndTime()) {
                    info = x;
                }
            }
            infos.add(x);
        });
    }
}
