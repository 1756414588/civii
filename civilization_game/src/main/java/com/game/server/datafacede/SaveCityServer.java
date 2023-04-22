package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.p.City;
import com.game.manager.CityManager;
import com.game.server.thread.SaveCityThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.util.LogHelper;

import com.game.spring.SpringUtil;
import java.util.Iterator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 城池数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "city")
@Service
public class SaveCityServer extends SaveServer<City> {

	public SaveCityServer() {
		super("SAVE_CITY_SERVER", 2);
	}

	@Override
	public SaveThread createThread(String name) {
		return new SaveCityThread(name);
	}

	@Override
	public void saveData(City city) {
		SaveThread thread = threadPool.get((city.getCityId() % threadNum));
		thread.add(city);
	}

	@Override
	public void saveAll() {
		CityManager cityManager = SpringUtil.getBean(CityManager.class);
		Iterator<City> iterator = cityManager.getCityMap().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			City city = iterator.next();
			try {
				city.setLastSaveTime(now);
				saveData(city);
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_CITY_SERVER:{}", e.getMessage(), e);
			}
		}
	}

}
