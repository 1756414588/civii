package com.game.domain.p;

import java.util.ArrayList;
import java.util.List;

import com.game.pb.CommonPb;

public class FightIn {
	private List<AttackInfo> leftInfo = new ArrayList<AttackInfo>();
	private List<AttackInfo> rightInfo = new ArrayList<AttackInfo>();

	public List<AttackInfo> getLeftInfo() {
		return leftInfo;
	}

	public void setLeftInfo(List<AttackInfo> leftInfo) {
		this.leftInfo = leftInfo;
	}

	public List<AttackInfo> getRightInfo() {
		return rightInfo;
	}

	public void setRightInfo(List<AttackInfo> rightInfo) {
		this.rightInfo = rightInfo;
	}

	public CommonPb.FightIn.Builder wrapPb() {
		CommonPb.FightIn.Builder builder = CommonPb.FightIn.newBuilder();
		for (AttackInfo attackInfo : leftInfo) {
			if (attackInfo == null) {
				continue;
			}

			builder.addLeftInfo(attackInfo.wrapPb());
		}

		for (AttackInfo attackInfo : rightInfo) {
			if (attackInfo == null) {
				continue;
			}
			builder.addRightInfo(attackInfo.wrapPb());
		}

		return builder;
	}

	// 反序列化
	public void unwrapPb(CommonPb.FightIn builder) {
		for (CommonPb.AttackInfo attackInfoPb : builder.getLeftInfoList()) {
			if (attackInfoPb == null) {
				continue;
			}

			AttackInfo attackInfo = new AttackInfo();
			attackInfo.unwrapPb(attackInfoPb);
			leftInfo.add(attackInfo);
		}

		for (CommonPb.AttackInfo attackInfoPb : builder.getRightInfoList()) {
			if (attackInfoPb == null) {
				continue;
			}

			AttackInfo attackInfo = new AttackInfo();
			attackInfo.unwrapPb(attackInfoPb);
			rightInfo.add(attackInfo);
		}

	}

}
