package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-3-13 上午11:39:53
 * @describe国家日志
 */
public class CtyDaily {
	private int dailyId; // 日志Id
	private long time;  // 创建时间
	private int mapId;  // 地图Id
	private int cityId;  // 城池Id
	private int country;   // 国家
	private String playerName = "unkown"; // 玩家姓名

	public int getDailyId() {
		return dailyId;
	}

	public void setDailyId(int dailyId) {
		this.dailyId = dailyId;
	}


	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public DataPb.CountryDailyData ser() {
		DataPb.CountryDailyData.Builder builder = DataPb.CountryDailyData.newBuilder();
		builder.setDailyId(dailyId);
		builder.setCreateTime(time);
		builder.setMapId(mapId);
		builder.setCityId(cityId);
		builder.setCountry(country);
		builder.setPlayerName(playerName);

		return builder.build();
	}

	public CtyDaily() {
	}

	public CtyDaily(DataPb.CountryDailyData e) {
		this.dailyId = e.getDailyId();
		this.time = e.getCreateTime();
		this.mapId = e.getMapId();
		this.cityId = e.getCityId();
        this.country = e.getCountry();
		this.playerName = e.getPlayerName();
	}

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public CommonPb.CountryDaily.Builder wrapPb() {
        CommonPb.CountryDaily.Builder builder = CommonPb.CountryDaily.newBuilder();
        builder.setDailyId(dailyId);
        builder.setCreateTime(time);
        builder.setMapId(mapId);
        builder.setCityId(cityId);
        builder.setCountry(country);
        builder.setPlayerName(playerName);

        return builder;
    }

}
