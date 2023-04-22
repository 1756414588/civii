package com.game.domain.p;


import com.game.domain.Award;
import com.game.pb.DataPb;

import java.util.ArrayList;
import java.util.List;

public class CityElection implements Comparable<CityElection> {
    private long lordId;    // 选举人Id
    private List<Award> awards = new ArrayList<Award>(); // 选举人的消耗
    private long electionTime; // 选举时间
    private int cityId;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public List<Award> getAwards() {
        return awards;
    }

    public void setAwards(List<Award> awards) {
        this.awards = awards;
    }

    public long getElectionTime() {
        return electionTime;
    }

    public void setElectionTime(long electionTime) {
        this.electionTime = electionTime;
    }

    public DataPb.CityElectionData.Builder writeData() {
        DataPb.CityElectionData.Builder builder = DataPb.CityElectionData.newBuilder();
        builder.setLordId(lordId);
        for (Award award : awards) {
            builder.addAward(award.writeData());
        }
        builder.setElectionTime(electionTime);
        builder.setCityId(cityId);
        return builder;
    }

    public void readData(DataPb.CityElectionData builder) {
        lordId = builder.getLordId();
        for (DataPb.AwardData awardData : builder.getAwardList()) {
            Award award = new Award();
            award.readData(awardData);
            awards.add(award);
        }

        electionTime = builder.getElectionTime();
        cityId = builder.getCityId();
    }

    @Override
    public int compareTo(CityElection param) {
        return Long.compare(electionTime, param.getElectionTime());
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public List<Award> copyAwards() {
        List<Award> data = new ArrayList<Award>();
        for (Award award : awards) {
            data.add(new Award(award));
        }

        return data;
    }
}
