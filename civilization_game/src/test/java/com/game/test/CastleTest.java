package com.game.test;

import com.game.client.SingleClient;
import com.game.pb.BasePb.Base;
import com.game.pb.CastlePb.GetMiningHeroListRq;
import com.game.pb.CastlePb.MiningUpRq;
import com.game.util.PbHelper;

public class CastleTest {
	public static void main(String[] args) throws InterruptedException {
		SingleClient.connectServer("localhost", 9001);
		if (SingleClient.isConnected()) {
			AccountTest.userLoginRq();
			Thread.sleep(3000);
//			getMiningHeroListRq();
			miningUpRq();
		}
	}

	public static void getMiningHeroListRq() {
		GetMiningHeroListRq.Builder builder = GetMiningHeroListRq.newBuilder();
		Base.Builder baseBuilder = PbHelper.createRqBase(GetMiningHeroListRq.EXT_FIELD_NUMBER, null, GetMiningHeroListRq.ext,
				builder.build());
		SingleClient.sendMsg(baseBuilder);
	}
	
	public static void miningUpRq() {
		MiningUpRq.Builder builder = MiningUpRq.newBuilder();
		builder.setIndex(3);
		builder.setHeroId(4);
		builder.setIsExchangeEquip(false);
		Base.Builder baseBuilder = PbHelper.createRqBase(MiningUpRq.EXT_FIELD_NUMBER, null, MiningUpRq.ext,
				builder.build());
		SingleClient.sendMsg(baseBuilder);
	}
}
