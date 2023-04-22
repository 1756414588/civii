package com.game.domain.s;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/27 15:54
 **/
public class StaticMaterialSubstituteVip {
	private int vip;
	private int freeTimes;

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public int getFreeTimes() {
		return freeTimes;
	}

	public void setFreeTimes(int freeTimes) {
		this.freeTimes = freeTimes;
	}

	@Override
	public String toString() {
		return "StaticMaterialSubstitute{" + "vip=" + vip + ", freeTimes=" + freeTimes + '}';
	}
}
