package com.game.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.game.Loading;
import com.game.channel.xinkuai.PushActivity;
import com.game.channel.xinkuai.PushGold;
import com.game.dao.uc.SChannelDao;
import com.game.define.LoadData;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.log.consumer.domin.BaseProperties;
import com.game.pay.channel.ChannelConsts;
import com.game.pay.channel.SChannelConfig;
import com.game.server.exec.HttpExecutor;
import com.game.spring.SpringUtil;
import com.game.util.HttpUtil;
import com.game.util.TimeHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author zcp
 * @date 2021/6/29 15:09 新快活动管理器
 */
@Component
@LoadData(name = "快游日志", type = Loading.LOAD_USER_DB)
public class XinkuaiManager extends BaseManager {

	@Autowired
	private SChannelDao channelDao;

	@Value("${xinkuai.activity}")
	private String activityUrl;

	@Value("${xinkuai.cost}")
	private String costUrl;

	private Map<Integer, String> channelMap = new ConcurrentHashMap<>();
	/**
	 * 渠道方游戏ID appkey
	 */
	private Map<Integer, Integer> gameIdMap = new ConcurrentHashMap<>();
	private List<SChannelConfig> list = new ArrayList<>();

	@Autowired
	HttpUtil httpUtil;

	public void load() throws Exception {
		Map<Integer, String> tmp = new ConcurrentHashMap<>();
		Map<Integer, Integer> gameTmp = new ConcurrentHashMap<>();
		list = channelDao.selectAllChannelConfig();
		list.forEach(sChannelConfig -> {
			gameTmp.put(sChannelConfig.getPlatType(), sChannelConfig.getGameChannelId());
			JSONObject login = JSON.parseObject(sChannelConfig.getLoginConfig());
			tmp.put(sChannelConfig.getGameChannelId(), login.getString("appkey"));
		});
		channelMap = tmp;
		gameIdMap = gameTmp;
	}

	@Override
	public void init() throws Exception {

	}

	/**
	 * 推送参与活动信息
	 *
	 * @param player
	 * @param activityId
	 * @param activityType
	 * @param activityAttendNum
	 */
	public void pushActivity(Player player, int activityId, String activityType, int activityAttendNum) {
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			Account account = player.account;
			if (account != null) {
				int channel = account.getChannel();
				if (channel != ChannelConsts.DEFAULT_CHANNEL && channel == ChannelConsts.NEW_KUAIYOU_ID) {
					SChannelConfig config = getAppId(channel, account.getPkgName());
					if (config == null) {
						return;
					}
					PushActivity pushActivity = PushActivity.builder().activityId(activityId).activityType(activityType).activityAttendNum(activityAttendNum).appId(config.getGameChannelId()).userId(player.getAccount().getChannelAccount()).roleId(String.valueOf(player.roleId)).roleLevel(player.getLevel()).roleCreateTime(player.account.getCreateDate()).servrerId(String.valueOf(player.getAccount().getServerId())).vip(player.getVip()).actTime(curentTime()).build();
					String result = HttpUtil.sendPost(activityUrl, pushActivity.makeSign(getAppKey(config.getGameChannelId())));
					JSONObject ret = JSON.parseObject(result);
				}
			}
		});
	}

	/**
	 * 推送钻石消耗
	 *
	 * @param player
	 * @param coinNum
	 */
	public void pushGold(Player player, int coinNum) {
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			Account account = player.account;
			if (account != null) {
				int channel = account.getChannel();
				if (channel != ChannelConsts.DEFAULT_CHANNEL && channel == ChannelConsts.NEW_KUAIYOU_ID) {
					SChannelConfig config = getAppId(channel, account.getPkgName());
					if (config == null) {
						return;
					}
					PushGold pushGold = PushGold.builder().userId(player.getAccount().getChannelAccount()).appId(config.getGameChannelId()).servrerId(String.valueOf(player.getAccount().getServerId())).roleId(String.valueOf(player.roleId)).vip(player.getVip()).coinNum(coinNum).actTime(curentTime()).build();
					HttpUtil.sendPost(costUrl, pushGold.makeSign(getAppKey(config.getGameChannelId())));
				}
			}
		});
	}

	/**
	 * 通过渠道获取APPID
	 *
	 * @param channel
	 * @return
	 */
	private SChannelConfig getAppId(int channel, String pkgName) {
		if (pkgName == null) {
			return null;
		}
		if (channel == ChannelConsts.NEW_KUAIYOU_ID) {
			for (SChannelConfig config : list) {
				if (config.getPlatType() == channel) {
					if (pkgName.equals(config.getPackageName())) {
						return config;
					}
				}
			}
		} else {
			for (SChannelConfig config : list) {
				if (config.getPlatType() == channel) {
					return config;
				}
			}
		}
		return null;
	}

	/**
	 * 获取appKey
	 *
	 * @param gameChannelId
	 * @return
	 */
	private String getAppKey(int gameChannelId) {
		if (channelMap.containsKey(gameChannelId)) {
			return channelMap.get(gameChannelId);
		}
		return "";
	}

	public long curentTime() {
		return System.currentTimeMillis() / 1000L;
	}

	String str[] = {"last_account_login_time",
		"role_create_time",
		"account_create_time",
		"first_charge_time",
		"first_off_time",
		"first_vip_level_up",
		"last_account_logout_time",
		"last_login_time",
		"mission_start_time",
		"order_complete_time",
		"time",
		"offline_time"};

	// 上报快游
	public void pushXinkuai(BaseProperties properties, String type) {
		SpringUtil.getBean(HttpExecutor.class).add(() -> {
			JSONObject object = new JSONObject();
			object.put("appid", httpUtil.getAppId());
			object.put("client_ip", "127.0.0.1");
			JSONObject data_object = new JSONObject();
			JSONArray array = new JSONArray();
			JSONObject data = new JSONObject();
			data.put("#account_id", properties.getDistinct_id());
			data.put("#distinct_id", properties.getAccount_id());
			data.put("#type", type);
			data.put("#time", TimeHelper.getNow());
			data.put("#uuid", UUID.randomUUID());
			data.put("#event_name", properties.getEventName());
			Map<String, Object> properties1 = properties.getProperties();
			for (String s : str) {
				Object o = properties1.get(s);
				if (o != null && !o.equals("") && o instanceof Date) {
					Date time = (Date) o;
					String formatData = TimeHelper.getFormatData(time);
					properties.register(s, formatData);
				}
			}
			data.put("properties", properties.getProperties());
			array.add(data);
			data_object.put("data", array);
			object.put("data_object", data_object);
			//httpUtil.sendToKuaiYou(object.toJSONString());
		});
	}
}
