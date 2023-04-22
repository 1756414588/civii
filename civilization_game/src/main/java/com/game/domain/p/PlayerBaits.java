package com.game.domain.p;

import com.game.pb.CommonPb.PlayerBaitsPB;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PlayerBaits {
    private int baitId = 0;
    private int count = 0;

    public PlayerBaitsPB.Builder encode() {

        PlayerBaitsPB.Builder builder = PlayerBaitsPB.newBuilder();

        builder.setBaitId(this.baitId);
        builder.setCount(this.count);

        return builder;
    }

    public void decode(PlayerBaitsPB BaitsPB) {
        this.baitId = BaitsPB.getBaitId();
        this.count = BaitsPB.getCount();
    }

}
