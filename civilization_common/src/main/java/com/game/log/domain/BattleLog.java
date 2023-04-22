package com.game.log.domain;

import com.game.constant.AwardType;
import com.game.constant.ResourceType;
import com.game.pb.CommonPb;
import lombok.Builder;

import java.util.List;

/**
 * @author cpz
 * @date 2020/12/30 16:36
 * @description
 */
@Builder
public class BattleLog {
    private int serverId;
    private int channel;
    private long attacker;
    private long defencer;
    private List<CommonPb.Award> robots;
    private List<CommonPb.Award> lost;
    private String attackSoldier;
    private String defenceSoldier;
    private String attackPos;
    private String defencePos;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(serverId).append(",")
                .append(channel).append(",")
                .append(attacker).append(",")
                .append(defencer).append(",");
        robots.forEach(e -> {
            switch (e.getType()) {
                case AwardType.RESOURCE: {
                    switch (e.getId()) {
                        case ResourceType.IRON:
                            builder.append("金币*").append(e.getCount() / 1000).append("k");
                            break;
                        case ResourceType.COPPER:
                            builder.append("钢铁*").append(e.getCount() / 1000).append("k");
                            break;
                        case ResourceType.OIL:
                            builder.append("食物*").append(e.getCount() / 1000).append("k");
                            break;
                    }
                }
                break;
                case AwardType.PERSON:
                    builder.append("人口*").append(e.getCount());
                    ;
                    break;
            }
        });
        builder.append(",");
        lost.forEach(e -> {
            switch (e.getType()) {
                case AwardType.RESOURCE: {
                    switch (e.getId()) {
                        case ResourceType.IRON:
                            builder.append("金币*").append(e.getCount() / 1000).append("k");
                            ;
                            break;
                        case ResourceType.COPPER:
                            builder.append("钢铁*").append(e.getCount() / 1000).append("k");
                            ;
                            break;
                        case ResourceType.OIL:
                            builder.append("食物*").append(e.getCount() / 1000).append("k");
                            ;
                            break;
                    }
                }
                break;
                case AwardType.PERSON:
                    builder.append("人口*").append(e.getCount());
                    ;
                    break;
            }
        });
        builder.append(",");
        builder.append(attackSoldier).append(",");
        builder.append(defenceSoldier).append(",");
        builder.append(attackPos.replace(",", ".")).append(",");
        builder.append(defencePos.replace(",", "."));
        return builder.toString();
    }
}
