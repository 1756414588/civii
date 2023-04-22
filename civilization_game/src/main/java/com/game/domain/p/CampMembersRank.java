package com.game.domain.p;


import com.game.domain.Player;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;

/**
 * @Description TODO
 * @Date 2021/3/15 14:51
 **/
public class CampMembersRank {
    private int rank;
    private int country;
    private Long lordId;
    private int fight;
    private int fightMax;

    public CampMembersRank() {
    }

    public CampMembersRank(Player player) {
        this.rank = 0;
        this.country = player.getCountry();
        this.lordId = player.roleId;
        this.fight = player.getBattleScore();
        this.fightMax = player.getBattleScore();
    }

    public CampMembersRank(DataPb.CampMemberDate e) {
        this.rank = e.getRank();
        this.country = e.getCountry();
        this.lordId = e.getLordId();
        this.fight = e.getFight();
        this.fightMax = e.getFightMax();
    }

    public DataPb.CampMemberDate serCampMembers() {
        return DataPb.CampMemberDate.newBuilder()
                .setRank(this.rank)
                .setCountry(this.country)
                .setFight(this.fight)
                .setFightMax(this.fightMax)
                .setLordId(this.lordId)
                .build();
    }

    public CommonPb.ActRank serActRank() {
        return CommonPb.ActRank.newBuilder()
                .setRank(this.rank)
                .setCountry(this.country)
                .setFight(this.fightMax)
                .build();
    }


    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public Long getLordId() {
        return lordId;
    }

    public void setLordId(Long lordId) {
        this.lordId = lordId;
    }

    public int getFight() {
        return fight;
    }

    public void setFight(int fight) {
        this.fight = fight;
    }

    public int getFightMax() {
        return fightMax;
    }

    public void setFightMax(int fightMax) {
        this.fightMax = fightMax;
    }

    @Override
    public String toString() {
        return "CampMembersRank{" +
                "rank=" + rank +
                ", country=" + country +
                ", lordId=" + lordId +
                ", fight=" + fight +
                ", fightMax=" + fightMax +
                '}';
    }
}
