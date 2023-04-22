package com.game.manager;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.game.server.GameServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.p.SuggestDao;
import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.domain.p.Suggest;

@Component
public class SuggestManager {

	@Autowired
	private SuggestDao suggestDao;

	public static AtomicInteger suggest;
	public int today;

	public boolean addSuggest(Player player, String content) {
		if (today != GameServer.getInstance().currentDay) {
			suggest = new AtomicInteger(0);
			today = GameServer.getInstance().currentDay;
		}
		int count = suggest.incrementAndGet();
		if (count > 1000) {
			return false;
		}

		Lord lord = player.getLord();
		if (lord.getSuggestTime() != GameServer.getInstance().currentDay) {
			lord.setSuggestTime(GameServer.getInstance().currentDay);
			lord.setSuggestCount(0);
		}

		if (lord.getSuggestCount() >= 2) {
			return false;
		}

		lord.setSuggestCount(lord.getSuggestCount() + 1);

		Suggest suggest = new Suggest();
		suggest.setLordId(player.getLord().getLordId());
		suggest.setContent(content);
		suggest.setLevel(player.getLevel());
		suggest.setSendTime(new Date());
		suggestDao.insertSuggest(suggest);
		return true;
	}
}
