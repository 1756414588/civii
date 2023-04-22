package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.p.Country;
import com.game.server.thread.SaveServer;
import com.game.spring.SpringUtil;
import java.util.Iterator;

import com.game.domain.CountryData;
import com.game.manager.CountryManager;
import com.game.server.thread.SaveCountryThread;
import com.game.server.thread.SaveThread;
import com.game.util.LogHelper;
import org.springframework.stereotype.Service;

/**
 * @Author 陈奎
 * @Description 国家数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "国家")
@Service
public class SaveCountryServer extends SaveServer<Country> {

	public SaveCountryServer() {
		super("SAVE_COUNTRY_SERVER", 1);
	}

	public SaveThread createThread(String name) {
		return new SaveCountryThread(name);
	}

	public void saveData(Country country) {
		SaveThread thread = threadPool.get(0);
		thread.add(country);
	}

	@Override
	public void saveAll() {
		CountryManager countryManager = SpringUtil.getBean(CountryManager.class);
		Iterator<CountryData> it = countryManager.getCountrys().values().iterator();
		while (it.hasNext()) {
			try {
				CountryData countryData = it.next();
				saveData(countryData.copyCountry());
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_COUNTRY_SERVER:{}", e.getMessage(), e);
			}
		}
	}

}
