package com.game.domain.s;

import java.util.Calendar;
import java.util.Date;

import com.game.constant.ActivityConst;
import com.game.util.DateHelper;

/**
 * @filename
 *
 * @version 1.0
 * @time 2017-3-3 下午5:11:37
 * @describe
 */
public class ActivityBase {

	private Date openTime;// 开服时间
	private StaticActivity staticActivity;
	private StaticActivityPlan plan;

	private Date toBeginTime;// 预设时间
	private Date beginTime;// 活动开启时间
	private Date endTime;// 结束时间
	private Date displayTime;// 领奖时间
	private Date sendTime;// 未领取奖励,则发送邮件给玩家

	// 存放在内存中
	private boolean sendMail = true;

	public ActivityBase(Date openTime, StaticActivity staticActivity, StaticActivityPlan activityPlan) {
		this.openTime = openTime;
		this.staticActivity = staticActivity;
		this.plan = activityPlan;
		initData();
	}

	public boolean initData() {
		int beginDate = plan.getOpenBegin();
		int display = staticActivity.getDisplay();
		if (beginDate != 0) {
			int openDuration = plan.getOpenDays();
			if (openDuration == 0) {
				return false;
			}
			// 开服时间(设置为00:00:00)
			Calendar open = Calendar.getInstance();
			open.setTime(openTime);
			open.set(Calendar.HOUR_OF_DAY, 0);
			open.set(Calendar.MINUTE, 0);
			open.set(Calendar.SECOND, 0);

			// 预开活动(00:00:00)
			open.add(Calendar.DATE, (beginDate - 2));
			this.toBeginTime = open.getTime();

			// 活动开启时间(00:00:00)
			open.add(Calendar.DATE, 1);
			this.beginTime = open.getTime();

			// 活动结束时间23:59:59
			open.add(Calendar.DATE, openDuration);
			open.add(Calendar.SECOND, -1);
			this.endTime = open.getTime();

			// 永久性活动,则固定开启,结束时间
			if (display == ActivityConst.ACTIVITY_LONG) {
				this.beginTime = DateHelper.parseDate("2016-11-17 00:00:00");
				this.endTime = DateHelper.parseDate("2027-11-17 23:59:59");
				this.sendMail = false;
			} else if (display != 0) {//
				open.add(Calendar.DATE, display);
				this.displayTime = open.getTime();
			}
			return true;
		} else {
			if (plan.getBeginTime() == null || plan.getEndTime() == null) {
				return false;
			}

			Calendar toBeginTime = Calendar.getInstance();
			toBeginTime.setTime(plan.getBeginTime());
			toBeginTime.add(Calendar.DATE, -1);

			this.toBeginTime = toBeginTime.getTime();
			this.beginTime = plan.getBeginTime();
			this.endTime = plan.getEndTime();

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.endTime);
			if (display != 0 && display != ActivityConst.ACTIVITY_LONG) {
				calendar.add(Calendar.DATE, display);
				this.displayTime = calendar.getTime();
			}
			return true;
		}

	}

	public int getKeyId() {
		return plan.getAwardId();
	}

	public StaticActivity getStaticActivity() {
		return staticActivity;
	}

	public StaticActivityPlan getPlan() {
		return plan;
	}

	public int getActivityId() {
		return plan.getActivityId();
	}

	public int getAwardId() {
		return plan.getAwardId();
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Date getDisplayTime() {
		return displayTime;
	}

	public Date getToBeginTime() {
		return toBeginTime;
	}

	public boolean isWonderfulActivity() {
		return staticActivity.getWonderful() == 1;
	}

	/**
	 * 活动结束需要补发邮件的活动
	 * 
	 * @return
	 */
	public boolean initSendMail() {
		if (staticActivity.getMail() != ActivityConst.SEND_MAIL) {
			return false;
		}
		if (this.displayTime != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.displayTime);
			calendar.add(Calendar.HOUR_OF_DAY, 0);
			//这里需要特殊处理(消费排行，充值排行 需要在结束当天发放未领取的活动奖励且第二天还得再展示一天)
			if(this.getActivityId()==ActivityConst.ACT_TOPUP_RANK|| this.getActivityId()==ActivityConst.ACT_COST_GOLD){
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
			}
			this.sendTime = calendar.getTime();
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(this.endTime);
			calendar.add(Calendar.HOUR_OF_DAY, 0);
			this.sendTime = calendar.getTime();
		}
		if(new Date().after(this.sendTime)){
			return false;
		}
		return true;
	}

	/**
	 * 活动
	 * 
	 * @param openTime
	 * @return
	 */
	public int getStep() {
		Date now = new Date();// 当前时间

		if (plan.getLimitDate() != 0) {
			// 活动开启时间距离开服时间少于限制时间,活动不开启
			int dayiy = DateHelper.dayiy(openTime, beginTime);
			if (dayiy <= plan.getLimitDate()) {
				return ActivityConst.ACTIVITY_CLOSE;
			}
		}

		if (toBeginTime == null || beginTime == null || endTime == null) {
			return ActivityConst.ACTIVITY_CLOSE;
		} else if (now.before(toBeginTime)) {
			return ActivityConst.ACTIVITY_CLOSE;
		} else if (now.after(toBeginTime) && now.before(beginTime)) {
			return ActivityConst.ACTIVITY_TO_BEGIN;
		} else if (now.after(beginTime) && now.before(endTime)) {
			return ActivityConst.ACTIVITY_BEGIN;
		} else if (now.after(endTime)) {
			if (displayTime == null) {
				return ActivityConst.ACTIVITY_CLOSE;
			} else if (now.before(displayTime)) {
				return ActivityConst.ACTIVITY_DISPLAY;
			}
		}
		return ActivityConst.ACTIVITY_CLOSE;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public boolean isSendMail() {
		return sendMail;
	}

	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}

	/**
	 * 
	 * @return
	 */
	public boolean canAward() {
		int step = getStep();
		int podium = staticActivity.getPodium();
		if (step == ActivityConst.ACTIVITY_CLOSE || step == ActivityConst.ACTIVITY_TO_BEGIN) {// 活动未开启
			return false;
		} else if (step == ActivityConst.ACTIVITY_BEGIN && podium == 0) {// 活动过程中可领奖
			return true;
		} else if (podium == 1 && step == ActivityConst.ACTIVITY_DISPLAY) {// 活动结束后才可领奖
			return true;
		} else if (podium == 2) {// 活动过程和结束后都可领奖
			return true;
		}
		return false;
	}

	public boolean isRankAct() {
		return staticActivity.getRank() == ActivityConst.RANK_1;
	}

	public boolean isRankThree() {
		return staticActivity.getRank() == ActivityConst.RANK_3;
	}

	/**
	 * 活动是否消失
	 * 
	 * @param now
	 * @return
	 */
	public boolean disappear(Date now) {
		if (this.displayTime != null) {
			if (now.after(this.displayTime)) {
				return true;
			}
			return false;
		} else if (this.endTime != null) {
			if (now.after(this.endTime)) {
				return true;
			}
			return false;
		}
		return false;
	}
}
