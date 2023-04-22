package com.game.util;

import com.game.constant.WeekEnum;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {

	final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	public static final ZoneId GMT = ZoneId.systemDefault();
	final static public String yyyyMMdd = "yyyyMMdd";
	final static public long SECOND_MS = 1000L;
	final static public long OFFTIME_MS = 30000L;
	final static public long THREE_SECOND_MS = 3000L;
	final static public long MINUTE_MS = 60000L;
	final static public long TWO_MINUTE_MS = 120000L;
	final static public int DAY_S = 86400;
	final static public int HOUR_S = 3600;
	final static public long HOUR_MS = 3600000L;
	final static public int HALF_HOUR_S = 1800;
	final static public long DAY_MS = 86400000L;
	final static public long MONTH_MS = 2592000000L;
	final static public long SEASON_MS = 7776000000L;
	final static public long WEEK_MS = 604800000L;
	final static public long FIVE_MINUTE_MS = 300000L;

	public static int getCurrentSecond() {
		return (int) (System.currentTimeMillis() / SECOND_MS);
	}

	public static long curentTime() {
		return System.currentTimeMillis();
	}

	/**
	 * 禁止其他地方调用该方法！
	 *
	 * @return
	 */
	public static int getCurrentDay() {
		LocalDate localDate = LocalDate.now();
		int d = localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
		return d;
	}

	public static int getCurrentHour() {
		LocalTime localDate = LocalTime.now();
		return localDate.getHour();
	}

	public static int getDay(Date date) {
		LocalDate localDate = LocalDateTime.ofInstant(date.toInstant(), GMT).toLocalDate();
		return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
	}

	public static long getLeftTime(long endTime) {
		long now = System.currentTimeMillis();
		long left = endTime - now;
		left = left > 0 ? left : 0;
		return left;
	}

	public static long getEndTime(long lastTime) {
		long now = System.currentTimeMillis();
		long endTime = lastTime + now;
		return endTime;
	}

	public static String getNow() {
		return formatter.format(LocalDateTime.now());
	}

	public static String getFormatData(Date date) {
		return formatter.format(LocalDateTime.ofInstant(date.toInstant(), GMT));
	}

	public static long getTotalMinute(long endTime) {
		long timeLeft = getLeftTime(endTime);
		long minutes = timeLeft / TimeHelper.MINUTE_MS;
		long mod = timeLeft % TimeHelper.MINUTE_MS;
		if (mod > 0) {
			minutes += 1;
		}

		return minutes;
	}

	public static long getZeroOfDay() {
		return LocalDateTime.of(LocalDate.now(), LocalTime.MIN).atZone(GMT).toInstant().toEpochMilli();
	}

	public static long getTimeZero(long time) {
		if (time == 0) {
			return 0L;
		}
		LocalDateTime localDateTime = LocalDateTime.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), GMT).toLocalDate(), LocalTime.MIN);
		return localDateTime.atZone(GMT).toInstant().toEpochMilli();
	}

	public static long getEndTimeOfDay() {
		return LocalDateTime.of(LocalDate.now(), LocalTime.MAX).atZone(GMT).toInstant().toEpochMilli();
	}

	@Deprecated
	//public static int passDay(long time) {
	//	LocalDate localDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), GMT).toLocalDate();
	//	LocalDate now = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toLocalDate();
	//	return now.getDayOfYear() - localDate.getDayOfYear();
	//}

	public static int passNowDay(long zoreTime) {
		int num = (int) ((System.currentTimeMillis() - zoreTime) / DAY_MS);
		return Math.max(0, num);
	}

	// 获得指定时间[21点定时发邮件]
	public static long getCityMailTime(int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
	}

	public static int getTodayHour(int hour) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		int d = year * 1000000 + (month + 1) * 10000 + day * 100 + hour;
		return d;
	}

	private static int getTimeByType(int type) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		return c.get(type);
	}

	public static long getHoursOfDay(int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
	}

	// 获取凌晨的时间(毫秒)
	public static long getZeroTimeMs() {
		return getEndTimeOfDay() + 1L;
	}

	// 周五刷新,周五凌晨
	public static long getFridayTime() {
		int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (weekDay < 6) {
			cal.add(Calendar.DAY_OF_YEAR, -7);
		}
		return cal.getTimeInMillis() / 1000;
	}


	//计算两个时间戳间隔多少天
	public static int equation(long startTime, long endTime) {
		startTime = dateToStamp(stampToDate(startTime));
		endTime = dateToStamp(stampToDate(endTime));
		int newL = (int) ((endTime - startTime) / (1000 * 3600 * 24));
		return newL;
	}


	/*
	 * 将时间戳转换为时间
	 */
	public static String stampToDate(long l) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		long lt = l;
		Date date = new Date(lt);
		res = simpleDateFormat.format(date);
		return res;
	}

	/*
	 * 将时间转换为时间戳
	 */
	public static long dateToStamp(String s) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = simpleDateFormat.parse(s);
			return date.getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return -1;
		}

	}

	public static final int SECONDS_IN_DAY = 60 * 60 * 24;
	public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;


	/**
	 * 判断是否在同一天
	 *
	 * @param ms1
	 * @param ms2
	 * @return
	 */
	public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(ms1);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.setTimeInMillis(ms2);
		int lastDay = calendar.get(Calendar.DAY_OF_MONTH);
		return day == lastDay;
	}

	/**
	 * 判断是否跨天
	 */
	public static boolean isNextDay(int hour, Date now, Date orignd) {
		int num = whichDay(hour, now, orignd) - 1;
		if (num > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 第几天
	 */
	public static int whichDay(int hour, Date now, Date orignd) {
		Calendar orign = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		orign.setTime(orignd);
		int preHour = orign.get(Calendar.HOUR_OF_DAY);
		orign.set(Calendar.HOUR_OF_DAY, 0);
		orign.set(Calendar.MINUTE, 0);
		orign.set(Calendar.SECOND, 0);
		orign.set(Calendar.MILLISECOND, 0);

		calendar.setTime(now);
		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int num = (int) ((calendar.getTimeInMillis() - orign.getTimeInMillis()) / (24 * 3600 * 1000));
		if (hour != 0 && preHour < hour && nowHour > hour) {
			return num + 2;
		} else {
			return num + 1;
		}
	}

	public static long addDay(long begin, int day) {
		Date date = new Date();
		Calendar orign = Calendar.getInstance();
		date.setTime(begin);
		orign.setTime(date);
		orign.add(Calendar.DATE, day);
		return orign.getTimeInMillis();
	}

	/**
	 * //判断时间相差多少个小时
	 *
	 * @return
	 */
	public static int getDifferHours(Long time1, Long time2) {
		int hours = 0;
		if (time1 >= time2) {
			return hours;
		}
		hours = (int) ((time2 - time1) / 1000 / 60 / 60);
		return hours;
	}

	/**
	 * 获取当天几点的时间戳
	 *
	 * @param time
	 * @return
	 */
	public static long getTimeOfDay(int time) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, time);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	/*
	 * 将时间转换为时间戳
	 */
	public static long dateToStampY(String s) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = simpleDateFormat.parse(s);
			return date.getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * 将时间转换为时间戳
	 */
	public static long dateToStamp(String s, String patten) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patten);
		Date date = null;
		try {
			date = simpleDateFormat.parse(s);
			return date.getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return -1;
		}

	}

	public static Date getCurRewardTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 19);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		//计算的时间在结束时间之前 就延迟一天
		if (calendar.getTime().before(date)) {
			//往前推一天
			long time = calendar.getTime().getTime() + MILLIS_IN_DAY;
			calendar.setTimeInMillis(time);
		}
		return calendar.getTime();
	}

	public static Date getRewardTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 19);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		//计算的时间在结束时间之后 就再提前一天
		if (calendar.getTime().after(date)) {
			//往前推一天
			long time = calendar.getTime().getTime() - MILLIS_IN_DAY;
			calendar.setTimeInMillis(time);
		}
		return calendar.getTime();
	}

	public static Date getHourTime(Date date, int hourTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, hourTime);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}

	public static int getAppointDay(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int d = c.get(Calendar.YEAR) * 10000 + (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
		return d;
	}


	/**
	 * @Description 判断时间是否是当天
	 * @Date 2021/3/30 16:32
	 * @Param [time]
	 * @Return
	 **/
	public static boolean isSameDay(long time) {
		return getAppointDay(new Date(time)) - getAppointDay(new Date()) == 0;
	}

	public static int zoneOffsetV = 999;

	public static int zoneOffset() {
		if (zoneOffsetV != 999) {
			return zoneOffsetV;
		}
		Calendar c = Calendar.getInstance();
		int zoneOffset = c.get(java.util.Calendar.ZONE_OFFSET);
		zoneOffsetV = zoneOffset / 3600000;
		return zoneOffsetV;
	}

	public static long getTime(Date date, int ihour) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - ihour);
		return cal.getTime().getTime();
	}

	public static long getTimeMinute(Date date, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// cal.set(Calendar.h, cal.get(Calendar.HOUR_OF_DAY) - ihour);
		cal.add(Calendar.MINUTE, minute);
		return cal.getTime().getTime();
	}
	/**
	 * @param time     时间
	 * @param openWeek 离date第几周 1 本周 2 下一周 依次类推
	 * @param week     星期几 （中国习惯）
	 * @Param hour     几点
	 */
	public static long getTime(long time, int openWeek, int week, int hour) {
		// logger.info("******时间 *********** {}", getDate(time));
		Date date = new Date(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.add(Calendar.DATE, openWeek * 7);
		cal.set(Calendar.DAY_OF_WEEK, WeekEnum.getUsaWeek(week));
		cal.set(Calendar.HOUR_OF_DAY, hour);
		// 分
		cal.set(Calendar.MINUTE, 0);
		// 秒
		cal.set(Calendar.SECOND, 0);
		//logger.info("******时间 *********** {}", getDate(cal.getTime().getTime()));
		return cal.getTime().getTime();
	}

	//判断选择的日期是否是本周
	public static boolean isThisWeek(long time) {
		Calendar calendar = Calendar.getInstance();
		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		calendar.setTime(new Date(time));
		int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		if (paramWeek == currentWeek) {
			return true;
		}
		return false;
	}

	/**
	 *
	 * @return中国习惯1-7为礼拜一到礼拜天
	 */
	public static int getCurWeek() {
		Calendar cal = Calendar.getInstance();
		int week = cal.get(Calendar.DAY_OF_WEEK);
		if (week == 1) {
			return 7;
		} else {
			week--;
		}
		return week;
	}

	public static long getCountryTime(int day, int hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, day);
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime().getTime();
	}

	public static long getRemarkTime(long time, int day, int hour,int min) {
		// logger.info("******时间 *********** {}", getDate(time));
		Date date = new Date(time);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		cal.add(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		// 分
		cal.set(Calendar.MINUTE, min);
		// 秒
		cal.set(Calendar.SECOND, 0);
		// logger.info("******时间 *********** {}", getDate(cal.getTime().getTime()));
		return cal.getTime().getTime();
	}
}
