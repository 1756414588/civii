package com.game.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.ShopType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticVipBuy;

@Component
public class StaticPropMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticProp> propMap;

	// vip特价商品
	private List<StaticProp> vipShops = new ArrayList<StaticProp>();
	// 其他商品
	private List<StaticProp> shops = new ArrayList<StaticProp>();
	// 商店限购
	private Map<Integer, Map<Integer, StaticVipBuy>> shopLimit = new HashMap<Integer, Map<Integer, StaticVipBuy>>();
	// 折扣道具列表
	private List<StaticVipBuy> discountList = new ArrayList<StaticVipBuy>();

	@Override
	public void init() throws Exception {
		setPropMap(staticDataDao.selectProp());
		vipShops.clear();
		shops.clear();
		shopLimit.clear();
		discountList.clear();
		Iterator<StaticProp> it = getPropMap().values().iterator();
		while (it.hasNext()) {
			StaticProp next = it.next();
			if (next.getShopType() == ShopType.VIP_SHOP) {
				vipShops.add(next);
			} else if (next.getShopType() == ShopType.MILITARYS_SHOP || next.getShopType() == ShopType.SHOP) {
				shops.add(next);
			}
		}
		iniShop();
	}

	public void iniShop() {
		List<StaticVipBuy> buys = staticDataDao.selectStaticVipBuy();
		for (StaticVipBuy buy : buys) {
			int type = buy.getType();
			if (buy.getType() != 3 && buy.getType() != 4) {
				continue;
			}
			Map<Integer, StaticVipBuy> pmap = shopLimit.get(type);
			if (pmap == null) {
				pmap = new HashMap<Integer, StaticVipBuy>();
				shopLimit.put(type, pmap);
			}
			pmap.put(buy.getPropId(), buy);

			// 折扣列表
			if (buy.getLevelDisplay() == 0) {
				discountList.add(buy);
			}
		}
	}

	public StaticProp getStaticProp(int propId) {
		if (!propMap.containsKey(propId)) {
			return null;
		}
		return propMap.get(propId);
	}

	public int getPropType(int propId) {
		StaticProp staticProp = getStaticProp(propId);
		if (staticProp != null) {
			return staticProp.getShopType();
		}

		return 0;
	}


	public List<StaticProp> getShops() {
		return shops;
	}

	public StaticVipBuy getShopLimit(int type, int shopId) {
		if (type != 3 && type != 4) {
			return null;
		}
		return shopLimit.get(type).get(shopId);
	}

	public List<StaticVipBuy> getDiscountList() {
		return discountList;
	}

	public Map<Integer, StaticProp> getPropMap() {
		return propMap;
	}

	public void setPropMap(Map<Integer, StaticProp> propMap) {
		this.propMap = propMap;
	}
}
