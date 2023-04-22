package com.game.domain.p;

import com.game.pb.CommonPb.TeamQueuePB;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TeamQueue {

	private int teamId;
	private int status;          // 队列状态 0=未上阵 1=未派遣 2=派遣中 3=已派遣
	private List<Integer> usedHeroId;
	private int heroGroup;
	private long beginTime;
	private long endTime;
	private List<Integer> baits;

	public TeamQueuePB.Builder encode(List<Integer> heroList) {

		TeamQueuePB.Builder builder = TeamQueuePB.newBuilder();
		builder.setTeamId(this.teamId);
		builder.setStatus(this.status);
		builder.addAllUsedHeroId(heroList == null ? this.usedHeroId : heroList);
		builder.setHeroGroup(this.heroGroup);
		builder.setBeginTime(this.beginTime);
		builder.setEndTime(this.endTime);
		builder.addAllBaitId(this.baits);

		return builder;
	}

	public void decode(TeamQueuePB teamPB) {
		this.setTeamId(teamPB.getTeamId());
		this.setStatus(teamPB.getStatus());
		this.setUsedHeroId(new ArrayList<>(teamPB.getUsedHeroIdList()));
		this.setHeroGroup(teamPB.getHeroGroup());
		this.setBeginTime(teamPB.getBeginTime());
		this.setEndTime(teamPB.getEndTime());
		this.setBaits(new ArrayList<>(teamPB.getBaitIdList()));
	}

	public void initData() {
		this.setStatus(0);
		//声明一个空list,长度为4,值都为0
		List<Integer> list = new ArrayList<>();
		for (int j = 0; j < 4; j++) {
			list.add(0);
		}
		this.setUsedHeroId(list);
		this.setHeroGroup(0);
		this.setBeginTime(0);
		this.setEndTime(0);
		this.setBaits(new ArrayList<>());
	}


}
