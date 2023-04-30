package com.game.worldmap;

import com.game.pb.CommonPb;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2020/4/29 10:28
 * @description
 */
@Getter
@Setter
public class RebelMonster extends Monster {
    private long createTime;
    private long warId;

    @Override
    public CommonPb.WorldEntity.Builder wrapPb() {
        CommonPb.WorldEntity.Builder builder = super.wrapPb();
        builder.setCreateTime(createTime);
        return builder;
    }
}
