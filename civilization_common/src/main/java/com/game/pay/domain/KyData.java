package com.game.pay.domain;
/**
 * 快游红包SDK角色等级校验接口参数
 * @author caobing
 *
 */
public class KyData {
	/*rolelevel	number	角色等级
	rolename	string	角色名
	role_createtime	number	创建角色时间戳 单位s
	coin_num	number	游戏内钻石/金币个数
	vip	number	玩家在游戏内vip等级
	union	string	所在公会名称或者塔防等级*/
	
	private Integer roleid;
	private Integer serverid;
	private String rolename;
	private Integer rolelevel;
	private Long role_createtime;
	private Integer coin_num;
	private Integer vip;;
	private String union;
	
	public KyData() {
	}
	
	public KyData(Integer roleid, Integer serverid, String rolename, Integer rolelevel, Long role_createtime,
			Integer coin_num, Integer vip, String union) {
		super();
		this.roleid = roleid;
		this.serverid = serverid;
		this.rolename = rolename;
		this.rolelevel = rolelevel;
		this.role_createtime = role_createtime;
		this.coin_num = coin_num;
		this.vip = vip;
		this.union = union;
	}



	public Integer getRoleid() {
		return roleid;
	}

	public void setRoleid(Integer roleid) {
		this.roleid = roleid;
	}

	public Integer getServerid() {
		return serverid;
	}

	public void setServerid(Integer serverid) {
		this.serverid = serverid;
	}

	public String getRolename() {
		return rolename;
	}

	public void setRolename(String rolename) {
		this.rolename = rolename;
	}

	public Integer getRolelevel() {
		return rolelevel;
	}

	public void setRolelevel(Integer rolelevel) {
		this.rolelevel = rolelevel;
	}

	public Long getRole_createtime() {
		return role_createtime;
	}

	public void setRole_createtime(Long role_createtime) {
		this.role_createtime = role_createtime;
	}

	public Integer getCoin_num() {
		return coin_num;
	}

	public void setCoin_num(Integer coin_num) {
		this.coin_num = coin_num;
	}

	public Integer getVip() {
		return vip;
	}

	public void setVip(Integer vip) {
		this.vip = vip;
	}

	public String getUnion() {
		return union;
	}

	public void setUnion(String union) {
		this.union = union;
	}

	@Override
	public String toString() {
		return "KyData [roleid=" + roleid + ", serverid=" + serverid + ", rolename=" + rolename + ", rolelevel="
				+ rolelevel + ", role_createtime=" + role_createtime + ", coin_num=" + coin_num + ", vip=" + vip
				+ ", union=" + union + "]";
	}
}
