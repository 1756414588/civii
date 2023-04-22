package com.game.domain.p;

import com.game.domain.Award;
import com.game.pb.CommonPb;

import java.util.ArrayList;
import java.util.List;

public class ReportMsg {
    private FightBefore fightBefore = new FightBefore();
    private FightIn fightIn = new FightIn();
    private List<HeroInfo> heroInfos = new ArrayList<HeroInfo>();
    private List<Award> awards = new ArrayList<Award>();

    public FightBefore getFightBefore() {
        return fightBefore;
    }

    public void setFightBefore(FightBefore fightBefore) {
        this.fightBefore = fightBefore;
    }

    public FightIn getFightIn() {
        return fightIn;
    }

    public void setFightIn(FightIn fightIn) {
        this.fightIn = fightIn;
    }


    public List<HeroInfo> getHeroInfos() {
        return heroInfos;
    }

    public void setHeroInfos(List<HeroInfo> heroInfos) {
        this.heroInfos = heroInfos;
    }

    public CommonPb.ReportMsg.Builder wrapPb() {
        CommonPb.ReportMsg.Builder builder = CommonPb.ReportMsg.newBuilder();
        builder.setFightBefore(fightBefore.wrapPb());
        builder.setFightIn(fightIn.wrapPb());
        for (HeroInfo heroInfo : heroInfos) {
            if (heroInfo == null)
                continue;
            builder.addHeroInfo(heroInfo.wrapPb());
        }
        for (Award award : awards) {
            builder.addAward(award.wrapPb());
        }

        return builder;
    }

    public void unwrapPb(CommonPb.ReportMsg data) {
        fightBefore.unwrapPb(data.getFightBefore());
        fightIn.unwrapPb(data.getFightIn());
        for (CommonPb.HeroInfo heroInfo: data.getHeroInfoList()) {
            if (heroInfo == null)
                continue;
            HeroInfo elem = new HeroInfo();
            elem.unwrapPb(heroInfo);
            heroInfos.add(elem);
        }

        for (CommonPb.Award award : data.getAwardList()) {
            Award elem = new Award();
            elem.unwrapPb(award);
            awards.add(elem);
        }

    }

    public List<Award> getAwards() {
        return awards;
    }

    public void setAwards(List<Award> awards) {
        this.awards = awards;
    }

    public void addAwards(List<Award> awardsParam) {
        this.awards.clear();
        for (Award award : awardsParam) {
            if (award == null) {
                continue;
            }
            Award data = new Award(award);
            awards.add(data);
        }
    }

    public void addHeroInfo(List<HeroInfo> heroInfo) {
        heroInfos.clear();
        for (HeroInfo elem : heroInfo) {
            if (elem == null)
                continue;
            HeroInfo data = elem.cloneData();
            heroInfos.add(data);
        }
    }

}
