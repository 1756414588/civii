package com.game.pay.channel;

public enum TypeIndef {
	//ios
	KY_IOS_TEST(104, "kuaiyouios_test"),
	KY_IOS(105, "kuaiyouios"),
	KY_IOS_NEW(106, "kuaiyouios_new"),
	A1576_IOS(107, "xczsm_1567"),
	KW_IOS(108, "kwIosPay"),
	KW_IOS_RELEASE(109, "kwIosReleasePay"),
	//android
	KY(201, "kuaiyou"),
	KY_FX(202, "xinkuai"),
	SM(301, "shouMengPay"),
	KW(302, "kwPay"),
	A1576_ANDROID(303, "xczjh_1567"),
	KY_304(304, "kuaiyou"),
	KY_305(305, "kuaiyou"),

	;
	int plat;
	String payName;

	TypeIndef(int plat, String payName) {
		this.plat = plat;
		this.payName = payName;
	}

	public int getPlat() {
		return plat;
	}

	public void setPlat(int plat) {
		this.plat = plat;
	}

	public String getPayName() {
		return payName;
	}

	public void setPayName(String payName) {
		this.payName = payName;
	}
}
