package com.game.test;

import com.game.client.SingleClient;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.UserLoginRq;
import com.game.util.PbHelper;

public class AccountTest {
	public static void main(String[] args) {
		SingleClient.connectServer("localhost", 9001);
		if (SingleClient.isConnected()) {
			userLoginRq();
		}
	}

	public static void userLoginRq() {
		UserLoginRq.Builder builder = UserLoginRq.newBuilder();
		builder.setKeyId(3);
		builder.setToken("69e9dc7fb61f4af1b374f39bf0608e6a");
		builder.setServerId(1);
		builder.setClientVer("0.0.0");
		builder.setDeviceNo("00000000-2625-0b64-7b72-55e30033c587");
		Base.Builder baseBuilder = PbHelper.createRqBase(UserLoginRq.EXT_FIELD_NUMBER, null, UserLoginRq.ext,
				builder.build());
		SingleClient.sendMsg(baseBuilder);
	}
}
