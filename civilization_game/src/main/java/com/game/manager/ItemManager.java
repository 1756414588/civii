package com.game.manager;

import com.game.domain.s.StaticProp;
import com.game.log.consumer.EventManager;
import com.game.server.GameServer;
import com.game.server.datafacede.SaveItemServer;
import com.game.spring.SpringUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.GameError;
import com.game.dataMgr.StaticPropMgr;
import com.game.domain.Player;
import com.game.domain.p.Item;
import com.game.util.LogHelper;


//所有物品添加和删除都写这里面
//参数中需要type,id,count,reason
//物品增删接口和日志绑定，减少后期维护
@Component
public class ItemManager {

	@Autowired
	private StaticPropMgr staticPropMgr;

	@Autowired
	private SaveItemServer saveItemServer;

	public Item getItem(Player player, int itemId) {
		return player.getItemMap().get(itemId);
	}

	public Item addItem(Player player, int itemId, int count, int reason) {
		Item item = player.getItemMap().get(itemId);
		if (item != null) {
			item.setItemNum(count + item.getItemNum());
		} else {
			item = new Item(itemId, count);
			player.getItemMap().put(itemId, item);
		}
		item.setLordId(player.roleId);
		saveItemServer.saveData(item);
		return item;
	}

	public GameError removeItem(Player player, int itemId, int count, int reason) {
		if (count < 0) {
			LogHelper.CONFIG_LOGGER.info("subItem count < 0");
			return GameError.ITEM_COUNT_LESS_ZERO;
		}
		Item item = player.getItemMap().get(itemId);
		if (item == null) {
			return GameError.ITEM_NOT_FOUND;
		}
		if (item.getItemNum() < count) {
			LogHelper.CONFIG_LOGGER.info("subItem  less than count, reason = " + reason);
			return GameError.NOT_ENOUGH_ITEM;
		}
		item.setItemNum(item.getItemNum() - count);

		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp != null) {
			SpringUtil.getBean(EventManager.class).cost_item(player, Lists.newArrayList(
				itemId,
				count,
				reason,
				staticProp.getPropName(),
				item.getItemNum()
			));
		}
		item.setLordId(player.roleId);
		saveItemServer.saveData(item);
		return GameError.OK;
	}

	public Item subItem(Player player, int itemId, int count, int reason) {
		if (count < 0) {
			return null;
		}
		Item item = player.getItemMap().get(itemId);
		if (item == null || item.getItemNum() < count) {
			return null;
		}
		item.setItemNum(item.getItemNum() - count);
		item.setLordId(player.roleId);
		saveItemServer.saveData(item);
		return item;
	}

	// 获得物品的品质
	public int getQuality(int itemId) {
		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp == null) {
			return 0;
		}

		return staticProp.getColor();
	}

	public boolean hasEnoughItem(Player player, int itemId, int itemNum) {
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("hasEnoughItem player is null");
			return false;
		}
		Item item = player.getItem(itemId);
		if (item == null) {
			//LogHelper.CONFIG_LOGGER.info("hasEnoughItem item is null");
			return false;
		}

		return item.getItemNum() >= itemNum;
	}

	public Item addItemWithLimit(Player player, int itemId, int count, int maxCount, int reason) {
		Item item = player.getItemMap().get(itemId);
		if (item != null) {
			int resCount = Math.min(maxCount, count + item.getItemNum());
			item.setItemNum(resCount);
		} else {
			int resCount = Math.min(maxCount, count);
			item = new Item(itemId, resCount);
			player.getItemMap().put(itemId, item);
		}
		item.setLordId(player.roleId);
		saveItemServer.saveData(item);
		return item;
	}


}
