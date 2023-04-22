package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticZergShop;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "主宰商店")
public class StaticZergShopMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	@Getter
	private List<StaticZergShop> shops = new ArrayList<>();
	@Getter
	private Map<Integer, StaticZergShop> shopMap = new HashMap<>();

	@Override
	public void load() throws Exception {
		List<StaticZergShop> list = staticDataDao.loadStaticZergShop();
		list.forEach(e -> {
			shopMap.put(e.getPropId(), e);
		});
		shops = list.stream().sorted(Comparator.comparing(StaticZergShop::getSort)).collect(Collectors.toList());
	}

	@Override
	public void init() throws Exception {

	}

}
