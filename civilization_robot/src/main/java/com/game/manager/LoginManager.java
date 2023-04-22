package com.game.manager;


import com.alibaba.fastjson.JSONObject;
import com.game.acion.login.LogoutEvent;
import com.game.cache.RobotDataCache;
import com.game.constant.UcCodeEnum;
import com.game.cache.ConfigCache;
import com.game.define.LoadData;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.load.ILoadData;
import com.game.server.AppPropertes;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.uc.Message;
import com.game.util.DateHelper;
import com.game.util.HttpUtil;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.util.TimeHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "登录管理", initSeq = 1000)
public class LoginManager implements ILoadData {

	@Getter
	private Map<Integer, RobotData> onlineMap = new ConcurrentHashMap<>();
	@Getter
	private Map<Integer, RobotData> logoutMap = new ConcurrentHashMap<>();

	@Autowired
	private RobotManager robotManager;
	@Autowired
	private RobotDataCache robotDataCache;
	@Autowired
	private ConfigCache configCache;
	@Autowired
	private MessageManager messageManager;
	@Autowired
	private MessageEventManager messageEventManager;

	@Override
	public void load() {
	}


	@Override
	public void init() {
		initLoginAccount();
		LogHelper.CHANNEL_LOGGER.info("登录管理");
	}

	/**
	 * 初始化登录服账号
	 */
	public void initLoginAccount() {
		String accountPrefix = configCache.getAccountPrefix();
		int robotCount = configCache.getRobotNumber();
		int accountIndex = configCache.getAccountIndex();
		int serverId = configCache.getServerId();

		AppPropertes appProperty = SpringUtil.getBean(AppPropertes.class);
		String url = appProperty.getAccountServerUrl() + "account/robotCreate.do";

		for (int i = 0; i < robotCount; i++) {
			int index = accountIndex + i;
			String account = accountPrefix + index;
			doAccountLogin(url, account, serverId);
		}
	}


	/**
	 * 登录账号服
	 *
	 * @param account
	 */
	public void doAccountLogin(String url, String account, int serverId) {
		Map<String, String> params = new HashMap<>();
		params.put("account", account);
		params.put("channel", "1");

		String r = HttpUtil.sendHttpPost(url, params);
		Message message = JSONObject.parseObject(r, Message.class);
		if (message == null || message.getCode() != UcCodeEnum.SUCCESS.getCode()) {
			LogHelper.CHANNEL_LOGGER.info("【登录失败】 {}", message);
			return;
		}
		JSONObject data = JSONObject.parseObject(message.getData());
		int accountKey = data.getInteger("keyId");
		RobotData robotData = robotDataCache.getRobotData(accountKey);
		if (robotData != null) {
			robotData.setToken(data.getString("token"));
			robotData.setOnline(0);
			robotDataCache.update(robotData);
			logoutMap.put(robotData.getAccountKey(), robotData);
			messageEventManager.createEventContain(robotData.getAccountKey());
			return;
		}

		// 创建机器人数据
		robotData = robotDataCache.create(account, serverId, data);
		messageEventManager.createEventContain(robotData.getAccountKey());
		logoutMap.put(robotData.getAccountKey(), robotData);
	}

	public boolean isFullOnline() {
		String onlineNumberKey = "online_number";
		int onlineNum = configCache.getIntValue(onlineNumberKey);
		if (onlineMap.size() >= onlineNum) {
			return true;
		}
		return false;
	}


	public RobotData getNextLoginRobot() {

		int today = TimeHelper.getCurrentDay();
		long curTime = System.currentTimeMillis();

//		// 未做日常的账号
//		Optional<RobotData> optional = robotDataCache.getDataMap().values().stream().filter(e -> curTime > e.getLoginCDTime() && e.getDailyDate() != today && e.getOnline() == 0 && e.isFlag()).findAny();
//		if (optional.isPresent()) {
//			return optional.get();
//		}
//
//		// 选出日常未做完的账号
//		long maxId = messageManager.getMaxDailyId();
//		optional = robotDataCache.getDataMap().values().stream().filter(e -> curTime > e.getLoginCDTime() && e.getOnline() == 0 && e.getMessageId() < maxId && e.isFlag()).findAny();
//		if (optional.isPresent()) {
//			return optional.get();
//		}

		// 随意选取一个未登录的账号
		Optional<RobotData> optional = robotDataCache.getDataMap().values().stream().filter(e -> curTime > e.getLoginCDTime() && e.getOnline() == 0 && e.isFlag()).findAny();
		if (optional.isPresent()) {
			return optional.get();
		}

		return null;
	}

	/**
	 * 获取空闲机器人
	 *
	 * @return
	 */
	public List<RobotData> getSleepRobots() {
		int today = TimeHelper.getCurrentDay();
		int maxGuildId = (int) messageManager.getMaxGuildId();
		int maxDailyId = (int) messageManager.getMaxDailyId();
		return onlineMap.values().stream().filter(e -> e.getOnline() == 1 && e.isSleep(today, maxGuildId, maxDailyId)).collect(Collectors.toList());
	}



	/**
	 * 登录成功
	 *
	 * @param robot
	 */
	public void loginSuccess(Robot robot) {
		int today = TimeHelper.getCurrentDay();
		RobotData robotData = robot.getData();

		robotData.setOnline(1);
		robotData.setPos(robot.getPos());
		robotData.setCountry(robot.getLord().getCountry());
		robotData.setLoginDate(TimeHelper.getCurrentDay());

		// 日常数据更新
		if (robotData.getDailyDate() != today) {
			robotData.setDailyDate(today);
			robotData.setMessageId(0);
			robotData.setStatus(0);
		}

		onlineMap.put(robot.getId(), robotData);
		logoutMap.remove(robot.getId());

		// 1小时到2小时后退出
		long delay = 3600000 + RandomUtil.getRandomNumber(3600000);
		long logouTime = System.currentTimeMillis() + delay;

		TimerServer.getInst().addDelayEvent(new LogoutEvent(robot, delay));
		LogHelper.CHANNEL_LOGGER.info("登录成功 robotId:{} 退出时间:{}", robot.getId(), DateHelper.getDate(logouTime));
		//robotDataCache.update(robotData);
	}

	public void logout(Robot robot) {
		robot.getRobotNet().close();
		robotManager.removeRobot(robot);
		onlineMap.remove(robot.getId());
		logoutMap.put(robot.getId(), robot.getData());
		RobotData robotData = robot.getData();
		robotData.setOnline(0);
		robotData.setLogoutDate(TimeHelper.getCurrentDay());
		robotDataCache.update(robotData);
		messageEventManager.removeEvent(robot);
		LogHelper.CHANNEL_LOGGER.info("退出 robotId:{}", robot.getId());
	}

	public long getLoginRate() {
		return configCache.getLongValue("login_rate");
	}

}
