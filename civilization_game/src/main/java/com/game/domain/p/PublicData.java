package com.game.domain.p;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/2 10:32
 **/
public class PublicData {
	private Integer id;
	private Long lastSaveTime;
	private byte[] endlessTDRank;// 无尽塔防排行信息

	public PublicData(Integer id) {
		this.id = id;
		setLastSaveTime(System.currentTimeMillis());
	}

	public PublicData() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte[] getEndlessTDRank() {
		return endlessTDRank;
	}

	public void setEndlessTDRank(byte[] endlessTDRank) {
		this.endlessTDRank = endlessTDRank;
	}

	public Long getLastSaveTime() {
		return lastSaveTime;
	}

	public void setLastSaveTime(Long lastSaveTime) {
		this.lastSaveTime = lastSaveTime;
	}
}
