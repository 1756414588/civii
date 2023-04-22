package com.game.manager;

import com.game.dataMgr.StaticLimitMgr;
import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.domain.s.StaticLimit;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.util.StringUtil;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author zcp
 * @date 2021/6/25 10:00
 */
@Component
public class NickManager {
	private static final int nickLenth = 5;

	private BlockingQueue<String> nickMap = new ArrayBlockingQueue(5000);

	private String HEAD = "commander@";
	private static List key = Lists.newArrayList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;

	public void init() {
		StaticLimit limit = staticLimitMgr.getStaticLimit();
		HEAD = limit.getNickPrefix();
	}

	public String getNewNick() {
		List<String> keys = Lists.newArrayList(key);
		Collections.shuffle(keys);
		List<String> list = keys.subList(0, nickLenth);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(HEAD);
		list.forEach(e -> {
			stringBuffer.append(e);
		});
		String nick = stringBuffer.toString();
		if (playerManager.getUsedNames().contains(nick)) {
			return getNewNick();
		}
		return nick;
	}

	public void setPlayerNick(Lord lord, String newNick) {
		if (StringUtil.isNullOrEmpty(newNick)) {
			return;
		}
        String oldNick = lord.getNick();
        lord.setNick(newNick);
		Set<String> usedNames = playerManager.getUsedNames();
		if (!StringUtil.isNullOrEmpty(oldNick)) {
			usedNames.remove(oldNick);
		}
		usedNames.add(newNick);
		playerManager.updateNickPlayer(oldNick);
	}
}
