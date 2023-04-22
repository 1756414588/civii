package com.game.domain.p;


import com.game.pb.CommonPb;

public class HeroInfo {
    private int heroId;
    private int lv;
    private long exp;
    private int addExp;

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getAddExp() {
        return addExp;
    }

    public void setAddExp(int addExp) {
        this.addExp = addExp;
    }

    public CommonPb.HeroInfo.Builder wrapPb() {
        CommonPb.HeroInfo.Builder data = CommonPb.HeroInfo.newBuilder();
        data.setHeroId(heroId);
        data.setLv(lv);
        data.setExp(exp);
        data.setAddExp(addExp);
        return data;
    }

    public void unwrapPb(CommonPb.HeroInfo data) {
        heroId = data.getHeroId();
        lv = data.getLv();
        exp = data.getExp();
        addExp = data.getAddExp();
    }

    public HeroInfo cloneData() {
        HeroInfo heroInfo = new HeroInfo();
        heroInfo.setHeroId(heroId);
        heroInfo.setLv(lv);
        heroInfo.setExp(exp);
        heroInfo.setAddExp(addExp);
        return heroInfo;
    }

}
