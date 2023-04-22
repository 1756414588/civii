package com.game.log.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/28 9:39
 **/
@Builder
@Getter
@Setter
public class ActMaterialSubstitutionLog {
	private Long lordId;
	private Date date; // 1.兑换时间
	private int lv; // 2.玩家等级
	private int vip; // 3.玩家VIP等级
	private int propId; // 4.被置换材料
	private int targetId; // 5.目标材料
	private int gold; // 6.花费：免费或者钻石
	private int freeCount;// 7.免费兑换次数
	private int convertCount;// 8.钻石兑换次数

	public ActMaterialSubstitutionLog() {
	}

    public ActMaterialSubstitutionLog(Long lordId, Date date, int lv, int vip, int propId, int targetId, int gold, int freeCount, int convertCount) {
        this.lordId = lordId;
        this.date = date;
        this.lv = lv;
        this.vip = vip;
        this.propId = propId;
        this.targetId = targetId;
        this.gold = gold;
        this.freeCount = freeCount;
        this.convertCount = convertCount;
    }

    /*
     * 时间戳传化为时间
     */
    private String timeStamp2Date(Date time) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(time);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(lordId).append(",")
            .append(timeStamp2Date(date)).append(",")
            .append(lv).append(",")
            .append(vip).append(",")
            .append(propId).append(",")
            .append(targetId).append(",")
            .append(gold).append(",")
            .append(freeCount).append(",")
            .append(convertCount).toString();
    }
}
