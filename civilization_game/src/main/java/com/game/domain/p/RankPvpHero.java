package com.game.domain.p;


import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

/**
 * @author
 */
@Getter
@Setter
public class RankPvpHero {
    private int heroId;
    private long lordId;
    private int mutilKill;
    private int mutilScore;

    public RankPvpHero() {

    }

    public RankPvpHero(PvpHero pvpHero) {
        setHeroId(pvpHero.getHeroId());
        setLordId(pvpHero.getLordId());
        setMutilKill(pvpHero.getMutilKill());
    }


    public DataPb.RankPvpHero.Builder writeData() {
        DataPb.RankPvpHero.Builder builder = DataPb.RankPvpHero.newBuilder();
        builder.setHeroId(heroId);
        builder.setLordId(lordId);
        builder.setMutilKill(mutilKill);
        builder.setMutilScore(mutilScore);
        return builder;
    }

    public void readData(DataPb.RankPvpHero data) {
        heroId = data.getHeroId();
        lordId = data.getLordId();
        mutilKill = data.getMutilKill();
        mutilScore=data.getMutilScore();
    }
}
