package com.game.recharge.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class DateHelper {
	public static final String format1 = "yyyy-MM-dd HH:mm:ss";
	public static final String format2 = "yyyy-MM-dd";
	public static final String format3 = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String format4 = "yyyyMMddHHmmss";
	public static SimpleDateFormat dateFormat1 = new SimpleDateFormat(format1);
	public static SimpleDateFormat dateFormat2 = new SimpleDateFormat(format2);
	public static SimpleDateFormat dateFormat3 = new SimpleDateFormat(format3);
	public static SimpleDateFormat dateFormat4 = new SimpleDateFormat(format4);

	static public boolean isSameDate(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return DateUtils.isSameDay(date1, date2);
	}

	static public boolean isSameDate(Calendar cal1, Calendar cal2) {
		return DateUtils.isSameDay(cal1, cal2);
	}

	static public boolean isBeforeOneDay(Calendar cal1) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		return DateUtils.isSameDay(cal1, calendar);
	}

	static public boolean isToday(Date date) {
		return DateUtils.isSameDay(date, new Date());
	}

	static public String displayDateTime() {
		return dateFormat3.format(new Date());
	}

	static public int getNowMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH + 1);
	}

	static public String displayNowDateTime() {
		return dateFormat1.format(new Date());
	}
	
	
	static public String orderNumTime() {
		return dateFormat4.format(new Date());
	}

	static public String formatDateTime(Date date, String format) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	static public String formatDateMiniTime(Date date) {
		return dateFormat3.format(date);
	}

	static public Date getInitDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, 1, 1);
		return calendar.getTime();
	}

	static public long getServerTime() {
		return Calendar.getInstance().getTime().getTime() / 1000;
	}

	static public long dvalue(Calendar calendar, Date date) {
		if (date == null || calendar == null) {
			return 0;
		}
		long dvalue = (calendar.getTimeInMillis() - date.getTime()) / 1000;
		return dvalue;
	}

	// cdTime --秒数
	static public boolean isOutCdTime(Date date, long cdTime) {
		Date nowDate = new Date();
		return (nowDate.getTime() - date.getTime()) > cdTime * 1000;
	}

	static public Date parseDate(String dateString) {
		try {
			return dateFormat1.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public boolean isInTime(Date now, Date begin, Date end) {
		if (now.before(end) && now.after(begin)) {
			return true;
		}

		return false;
	}

	/**
	 * 第几天,同一天为第一天
	 * 
	 * @param origin
	 * @param now
	 * @return
	 */
	static public int dayiy(Date origin, Date now) {
		Calendar orignC = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		orignC.setTime(origin);
		orignC.set(Calendar.HOUR_OF_DAY, 0);
		orignC.set(Calendar.MINUTE, 0);
		orignC.set(Calendar.SECOND, 0);
		orignC.set(Calendar.MILLISECOND, 0);

		calendar.setTime(now);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return (int) ((calendar.getTimeInMillis() - orignC.getTimeInMillis()) / (24 * 3600 * 1000)) + 1;
	}

	/**
	 * 日期增加天数
	 * 
	 * @param date
	 * @param add
	 * @return "yyyy-MM-dd 23:59:59"
	 */
	static public Date addDate(Date date, int add) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		calendar.add(Calendar.DAY_OF_YEAR, add - 1);
		return calendar.getTime();
	}

	public static String getDate(long time) {
		if (time == 0) {
			return "0000-00-00 00:00:00";
		}
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
}
