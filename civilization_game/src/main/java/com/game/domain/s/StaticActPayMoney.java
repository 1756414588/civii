package com.game.domain.s;

import com.game.constant.ActivityConst;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 直购物品的计费点 2020年5月27日
 *
 * @CaoBing halo_game StaticActPayMoney.java
 **/
@Getter
@Setter
public class StaticActPayMoney {

	private int payMoneyId;

	private int awardId;

	private String name;

	private int money;

	private int limit;

	private List<List<Integer>> sellList;

	private String desc;

	private int vipExp;

	private String percent;

	private int sort;
	private int nextJump;
	private int position;
	private String asset;
	private int openBegin;
	private int openDays;
	private Date beginTime;
	private Date endTime;
	private int limitDate;
	private int illustration;// 是否显示指引
	private int levelDisplay;// 显示等级,0为无限制

	public long getTime(Date openTime) {
		// 开服即开启的 判定开启时间和结束时间
		if (openBegin != 0) {
			// 永久性活动
			if (openDays == ActivityConst.ACTIVITY_LONG) {
				return 0L;
			}
			// 开服时间(设置为00:00:00)
			Calendar open = Calendar.getInstance();
			open.setTime(openTime);
			open.set(Calendar.HOUR_OF_DAY, 0);
			open.set(Calendar.MINUTE, 0);
			open.set(Calendar.SECOND, 0);

			// 预开活动(00:00:00)
			open.add(Calendar.DATE, (openBegin - 2));

			// 活动开启时间(00:00:00)
			open.add(Calendar.DATE, 1);

			// 活动结束时间23:59:59
			open.add(Calendar.DATE, openDays);
			open.add(Calendar.SECOND, -1);
			Date realEndTime = open.getTime();
			return realEndTime.getTime();
		} else {
			return endTime.getTime();
		}
	}

	/**
	 * @param openTime 开服时间
	 * @return
	 */
	public boolean isOpen(Date openTime, Date now) {
		// 开服即开启的 判定开启时间和结束时间
		if (openBegin != 0) {
			// 开启天数是0的 返回
			if (openDays == 0) {
				LogHelper.GAME_LOGGER.info("payMoneyId:{} config error", payMoneyId);
				return false;
			}
			// 永久性活动
			if (openDays == ActivityConst.ACTIVITY_LONG) {
				return true;
			}
			// 开服时间(设置为00:00:00)
			Calendar open = Calendar.getInstance();
			open.setTime(openTime);
			open.set(Calendar.HOUR_OF_DAY, 0);
			open.set(Calendar.MINUTE, 0);
			open.set(Calendar.SECOND, 0);

			// 预开活动(00:00:00)
			open.add(Calendar.DATE, (openBegin - 2));

			// 活动开启时间(00:00:00)
			open.add(Calendar.DATE, 1);
			Date realBeginTime = open.getTime();

			// 活动结束时间23:59:59
			open.add(Calendar.DATE, openDays);
			open.add(Calendar.SECOND, -1);
			Date realEndTime = open.getTime();

			// 在开启阶段的,扔到队列里
			if (now.after(realBeginTime) && now.before(realEndTime)) {
				return true;
			}
		} else {
			// 非开服就开启的 判定开启时间
			if (beginTime == null || endTime == null) {
				LogHelper.CONFIG_LOGGER.info("payMoneyId:{} config error", payMoneyId);
				return false;
			}
			// 在开启阶段的,扔到队列里
			if (now.after(beginTime) && now.before(endTime)) {
				return true;
			}
		}
		return false;
	}
}
