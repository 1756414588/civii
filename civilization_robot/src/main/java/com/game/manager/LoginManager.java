package com.game.manager;


import com.alibaba.fastjson.JSONObject;
import com.game.acion.login.LoginEvent;
import com.game.acion.login.RoleLoginAction;
import com.game.constant.UcCodeEnum;
import com.game.cache.ConfigCache;
import com.game.dao.p.LoginMessageDao;
import com.game.define.LoadData;
import com.game.domain.LoginAccount;
import com.game.domain.Robot;
import com.game.domain.p.LoginMessage;
import com.game.load.ILoadData;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.RoleLoginRq;
import com.game.server.AppPropertes;
import com.game.spring.SpringUtil;
import com.game.uc.Message;
import com.game.util.BasePbHelper;
import com.game.util.HttpUtil;
import com.game.util.LogHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "登录管理", initSeq = 1000)
public class LoginManager implements ILoadData {

	private Map<String, LoginAccount> loginAccountMap = new ConcurrentHashMap<>();

	private List<LoginMessage> loginMessages = new ArrayList<>();
	private List<LoginEvent> loginEvents = new ArrayList<>();

	@Autowired
	private ConfigCache robotConfigData;
	@Autowired
	private LoginMessageDao loginMessageDao;

	@Override
	public void load() {
		loginMessages = loginMessageDao.load();
		for (LoginMessage loginMessage : loginMessages) {
			LoginEvent loginEvent = new LoginEvent(loginMessage);
		}
	}



	@Override
	public void init() {
		initLoginAccount();
	}

	/**
	 * 初始化登录服账号
	 */
	public void initLoginAccount() {
		String accountPrefix = robotConfigData.getAccountPrefix();
		int robotCount = robotConfigData.getRobotNumber();
		int accountIndex = robotConfigData.getAccountIndex();
		int serverId = robotConfigData.getServerId();

		AppPropertes appProperty = SpringUtil.getBean(AppPropertes.class);
		String url = appProperty.getAccountServerUrl() + "account/robotCreate.do";

		for (int i = 0; i < robotCount; i++) {
			int index = accountIndex + i;
			String account = accountPrefix + index;
			doLoginAccountServer(url, account, serverId);
		}
		LogHelper.CHANNEL_LOGGER.info("账号服登录完毕,登录账号：{} 总数量：{} 下标：{}", accountPrefix, robotCount, accountIndex);
	}


	/**
	 * 登录账号服
	 *
	 * @param account
	 */
	public void doLoginAccountServer(String url, String account, int serverId) {
		Map<String, String> params = new HashMap<>();
		params.put("account", account);
		params.put("channel", "1");

		String r = HttpUtil.sendHttpPost(url, params);
		Message message = JSONObject.parseObject(r, Message.class);
		if (message.getCode() == UcCodeEnum.SUCCESS.getCode()) {
			JSONObject data = JSONObject.parseObject(message.getData());
			LoginAccount loginAccount = new LoginAccount();
			loginAccount.setAccount(account);
			loginAccount.setKeyId(data.getInteger("keyId"));
			loginAccount.setToken(data.getString("token"));
			loginAccount.setServerId(serverId);
			loginAccountMap.put(account, loginAccount);
		} else {
			LogHelper.CHANNEL_LOGGER.info("");
		}
	}

	public Map<String, LoginAccount> getLoginAccountMap() {
		return loginAccountMap;
	}

}
