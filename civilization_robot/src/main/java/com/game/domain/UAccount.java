package com.game.domain;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UAccount {

	private int keyId;
	private int channel;
	private String account;
	private int childNo;
	private int forbid;
	private int active;
	private String baseVersion;
	private String versionNo;
	private int white;
	private int firstSvr;
	private int secondSvr;
	private int thirdSvr;
	private String token;
	private String deviceNo;
	private Date loginDate;
	private Date createDate;
	private Date gameDate;
	private int platType;
	private boolean isCreate;

	private long closeSpeakTime;

	private String imodel;
	private String imei;
	private String ip;
	private String cpu;
	private String deviceUuid;
	private String idfa;
	private String resolution;
	//用户所在区服列表
	private String serverInfos;
	//包名
	private String packageName;
	//玩家的历史服务器
	private String loggedServer;

}
