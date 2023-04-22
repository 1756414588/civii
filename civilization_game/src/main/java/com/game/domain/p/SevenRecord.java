package com.game.domain.p;

import com.game.constant.Quality;
import com.game.pb.DataPb;
import com.google.common.collect.HashBasedTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class SevenRecord {
    private HashSet<Integer> washHeroId = new HashSet<Integer>();   // 英雄属性洗满记录Id
    private Map<Integer, Integer> washHeroNum = new HashMap<Integer, Integer>();  // row: heroId col:品质, value:数量
    private HashBasedTable<Integer, Integer, Integer> makeEquipNum = HashBasedTable.create();  // row:品质 col:装备类型 num:数量
    private Map<Integer, Integer> washEquipNum = new HashMap<Integer, Integer>(); // row: keyId col:品质 value:数量
    private HashSet<Integer> washEquipId = new HashSet<Integer>();  // 装备技能洗满的Id
    private int maxScore = 0;

    public Map<Integer, Integer> getWashHeroNum() {
        return washHeroNum;
    }

    public void setWashHeroNum(Map<Integer, Integer> washHeroNum) {
        this.washHeroNum = washHeroNum;
    }

    public HashBasedTable<Integer, Integer, Integer> getMakeEquipNum() {
        return makeEquipNum;
    }

    public void setMakeEquipNum(HashBasedTable<Integer, Integer, Integer> makeEquipNum) {
        this.makeEquipNum = makeEquipNum;
    }

    public Map<Integer, Integer> getWashEquipNum() {
        return washEquipNum;
    }

    public void setWashEquipNum(Map<Integer, Integer> washEquipNum) {
        this.washEquipNum = washEquipNum;
    }

    public void updateWashHeroNum(int heroId, int quality) {
        if (washHeroId.contains(heroId)) {
            return;
        }
        Integer currentNum = getWashHeroNum().get(quality);
        if (currentNum == null) {
            currentNum = 0;
        }
        if (currentNum >= 50) {
            return;
        }
        getWashHeroNum().put(quality, currentNum + 1);
        washHeroId.add(heroId);
    }

    public void updateMakeEquipNum(int quality, int equipType) {
        Integer currentNum = makeEquipNum.get(quality, equipType);
        if (currentNum == null) {
            currentNum = 0;
        }
        makeEquipNum.put(quality, equipType, currentNum + 1);
    }

    public void updateWashEquipNum(int keyId, int quality) {
        if (washEquipId.contains(keyId)) {
            return;
        }
        Integer currentNum = washEquipNum.get(quality);
        if (currentNum == null) {
            currentNum = 0;
        }
        if (currentNum >= 50) {
            return;
        }
        washEquipNum.put(quality, currentNum + 1);
        washEquipId.add(keyId);
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        /* if (maxScore > this.maxScore) {*/
        this.maxScore = maxScore;
        /*}*/
    }

    // 序列化
    public DataPb.SevenRecord.Builder wrapPb() {
        DataPb.SevenRecord.Builder builder = DataPb.SevenRecord.newBuilder();
        for (Map.Entry<Integer, Integer> heroWashEntry : getWashHeroNum().entrySet()) {
            if (heroWashEntry == null) {
                continue;
            }

            int quality = heroWashEntry.getKey();
            int value = heroWashEntry.getValue();
            DataPb.HeroWashNum.Builder heroNum = DataPb.HeroWashNum.newBuilder();
            heroNum.setQuality(quality);
            heroNum.setNum(value);
            builder.addHeroNum(heroNum);
        }

        Map<Integer, Map<Integer, Integer>> makeEquipMap = makeEquipNum.rowMap();
        for (Map.Entry<Integer, Map<Integer, Integer>> makeEquipEntry : makeEquipMap.entrySet()) {
            if (makeEquipEntry == null) {
                continue;
            }

            int quality = makeEquipEntry.getKey();
            Map<Integer, Integer> valueNum = makeEquipEntry.getValue();
            for (Map.Entry<Integer, Integer> valueEntry : valueNum.entrySet()) {
                if (valueEntry == null) {
                    continue;
                }
                int equipType = valueEntry.getKey();
                int num = valueEntry.getValue();
                DataPb.MakeEquipNum.Builder makeEquip = DataPb.MakeEquipNum.newBuilder();
                makeEquip.setQuality(quality);
                makeEquip.setNum(num);
                makeEquip.setEquipType(equipType);
                builder.addMakeEquipNum(makeEquip);
            }
        }

        for (Map.Entry<Integer, Integer> washEquipEntry : washEquipNum.entrySet()) {
            if (washEquipEntry == null) {
                continue;
            }

            DataPb.WashEquipNum.Builder washNum = DataPb.WashEquipNum.newBuilder();
            washNum.setQuality(washEquipEntry.getKey());
            washNum.setNum(washEquipEntry.getValue());
            builder.addWashEquipNum(washNum);
        }
        builder.setMaxBattleScore(maxScore);
        builder.addAllHeroId(washHeroId);
        builder.addAllEquipKeyId(washEquipId);
        return builder;
    }

    // 反序列化
    public void unwrap(DataPb.SevenRecord data) {
        maxScore = data.getMaxBattleScore();
        for (DataPb.HeroWashNum heroNumData : data.getHeroNumList()) {
            if (heroNumData == null) {
                continue;
            }
            getWashHeroNum().put(heroNumData.getQuality(), heroNumData.getNum());
        }

        for (DataPb.MakeEquipNum makeEquipNumData : data.getMakeEquipNumList()) {
            if (makeEquipNumData == null) {
                continue;
            }
            makeEquipNum.put(makeEquipNumData.getQuality(), makeEquipNumData.getEquipType(), makeEquipNumData.getNum());
        }

        for (DataPb.WashEquipNum equipNumData : data.getWashEquipNumList()) {
            if (equipNumData == null) {
                continue;
            }

            washEquipNum.put(equipNumData.getQuality(), equipNumData.getNum());
        }
        washHeroId.addAll(data.getHeroIdList());
        washEquipId.addAll(data.getEquipKeyIdList());
    }

    public HashSet<Integer> getWashHeroId() {
        return washHeroId;
    }

    public void setWashHeroId(HashSet<Integer> washHeroId) {
        this.washHeroId = washHeroId;
    }

    public HashSet<Integer> getWashEquipId() {
        return washEquipId;
    }

    public void setWashEquipId(HashSet<Integer> washEquipId) {
        this.washEquipId = washEquipId;
    }

    // private Map<Integer, Integer> washEquipNum = new HashMap<Integer, Integer>(); // row: keyId col:品质 value:数量
    public int getWashEquipMax(int quality) {
        int total = 0;
        for (int index = quality; index <= Quality.PURPLE.get(); index++) {
            Integer num = washEquipNum.get(index);
            if (num != null) {
                total += num;
            }
        }


        return total;
    }

    public int getWashHeroMax(int quality) {
        int total = 0;
        for (int index = quality; index <= Quality.PURPLE.get(); index++) {
            Integer num = getWashHeroNum().get(index);
            if (num != null) {
                total += num;
            }
        }


        return total;
    }

    public int getEquipMakebyQuality(int quality) {
        Map<Integer, Map<Integer, Integer>> rowMap = makeEquipNum.rowMap();
        if (rowMap == null) {
            return 0;
        }

        if (rowMap.isEmpty()) {
            return 0;
        }

        int all = 0;
        for (int index = quality; index <= Quality.PURPLE.get(); index++) {
            Map<Integer, Integer> equipTypeMap = rowMap.get(index);
            if (equipTypeMap == null) {
                continue;
            }

            if (equipTypeMap.isEmpty()) {
                continue;
            }

            int total = 0;
            for (Integer num : equipTypeMap.values()) {
                total += num;
            }
            all += total;
        }

        return all;
    }

    public int getEquipMakeByType(int quality, int equipType) {
        int total = 0;
        for (int index = quality; index <= Quality.PURPLE.get(); index++) {
            Integer num = makeEquipNum.get(index, equipType);
            if (num != null) {
                total += num;
            }
        }

        return total;

    }

    public int getEquipMake(int quality, int equipType) {
        if (equipType == 0) {
            return getEquipMakebyQuality(quality);
        } else {
            return getEquipMakeByType(quality, equipType);
        }

    }

    public void clear() {
        makeEquipNum.clear();
        washEquipNum.clear();
        washHeroNum.clear();
        washHeroId.clear();
        ;
        washEquipId.clear();
    }

}
