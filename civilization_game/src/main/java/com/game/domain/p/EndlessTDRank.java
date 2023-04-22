package com.game.domain.p;

import com.game.domain.Player;
import com.game.pb.CommonPb.TDRank;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/11/25 17:25
 **/
@Setter
@Getter
public class EndlessTDRank {
	private long lordId;
	private int rank;
	private int weekMaxFraction;
	private int historyMaxFraction;
	private Player player;

	public EndlessTDRank(Player player) {
		this.player = player;
		this.lordId = player.roleId;
		this.setRank(0);
		this.weekMaxFraction = getEndlessTDInfo().getWeekMaxFraction();
		this.historyMaxFraction = getEndlessTDInfo().getHistoryMaxFraction();

	}

	public EndlessTDRank() {
	}

	public void updateScore(Player player) {
		setWeekMaxFraction(getEndlessTDInfo().getWeekMaxFraction());
		setHistoryMaxFraction(getEndlessTDInfo().getHistoryMaxFraction());
	}

	public TDRank.Builder wrapTDRank() {
		TDRank.Builder builder = TDRank.newBuilder();
		builder.setCountry(player == null ? 1 : player.getCountry());
		builder.setNick(player == null ? "" : player.getNick());
		builder.setRank(rank);
		builder.setScore(weekMaxFraction);
		return builder;
	}
	public EndlessTDInfo getEndlessTDInfo() {
		return player.getEndlessTDInfo();
	}
}
