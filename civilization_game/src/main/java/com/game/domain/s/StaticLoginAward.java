package com.game.domain.s;

import java.util.List;

import com.game.pb.CommonPb;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-3-21 下午8:14:09
 * @describe
 */
public class StaticLoginAward {

	private int loginId;
	private List<List<Integer>> awardList;
	private List<CommonPb.Award> awardPbList;

	public int getLoginId() {
		return loginId;
	}

	public void setLoginId(int loginId) {
		this.loginId = loginId;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public List<CommonPb.Award> getAwardPbList() {
		return awardPbList;
	}

	public void setAwardPbList(List<CommonPb.Award> awardPbList) {
		this.awardPbList = awardPbList;
	}

}
