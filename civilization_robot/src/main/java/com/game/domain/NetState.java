package com.game.domain;

import com.game.network.INet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NetState {

	private String netId;
	private INet net;
	private long heatBeatTime;

}
