package com.game.domain.s;

import com.game.pb.CommonPb;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StaticFishBait {

    private int baitId;
    private String name;
    private int color;
    private String colorDesc;
    private Map<Integer, StaticBaitToFishProbability> baitToFishProbability;
    private int time;
    private int probability;
    private String atlasDesc;

    // build
    public CommonPb.BaitPB.Builder encode() {
        CommonPb.BaitPB.Builder builder = CommonPb.BaitPB.newBuilder();
        builder.setBaitId(baitId);
        builder.setBaitName(name);
        builder.setColor(color);
        builder.setColorDesc(colorDesc);
        // 遍历baitToFishProbability
        for (Map.Entry<Integer, StaticBaitToFishProbability> entry : baitToFishProbability.entrySet()) {
            CommonPb.BaitToFishProbabilityPB.Builder baitToFishProbabilityPB = CommonPb.BaitToFishProbabilityPB.newBuilder();
            baitToFishProbabilityPB.setFishId(entry.getKey());
            baitToFishProbabilityPB.setProbability(entry.getValue().getProbability());
            builder.addBaitToFishProbability(baitToFishProbabilityPB);
        }

        builder.setTime(time);
        builder.setProbability(probability);
        builder.setAtlasDesc(atlasDesc);
        return builder;
    }

    public void setBaitToFishProbability(List<List<Integer>> baitToFishProbability) {
        Map<Integer, StaticBaitToFishProbability> baitToFishProbabilityMap = new HashMap<>();
        // 遍历bait，构建baitToFishProbability
        for (List<Integer> list : baitToFishProbability) {
            StaticBaitToFishProbability staticBaitToFishProbability = new StaticBaitToFishProbability();
            staticBaitToFishProbability.setFishId(list.get(0));
            staticBaitToFishProbability.setProbability(list.get(1));
            baitToFishProbabilityMap.put(list.get(0), staticBaitToFishProbability);
        }
        this.baitToFishProbability = baitToFishProbabilityMap;
    }


}
