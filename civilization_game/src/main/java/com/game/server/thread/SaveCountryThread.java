package com.game.server.thread;

import com.game.domain.p.Country;
import com.game.manager.CountryManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author 陈奎
 * @Description 国家数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveCountryThread extends SaveThread<Country> {

	private LinkedBlockingQueue<Integer> country_queue = new LinkedBlockingQueue<Integer>();

	private HashMap<Integer, Country> countryMap = new HashMap<Integer, Country>();

	public SaveCountryThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !country_queue.isEmpty()) {

			Country country = null;
			synchronized (this) {
				Integer countryId = country_queue.poll();
				if (countryId != null) {
					country = countryMap.remove(countryId);
				}
			}
			if (country == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}

			} else {
				try {
					CountryManager countryManager = SpringUtil.getBean(CountryManager.class);
					countryManager.updateCountry(country);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("country Exception:" + country.getCountryId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save count :" + saveCount);
	}

	@Override
	public void add(Country country) {
		try {
			synchronized (this) {
				if (!countryMap.containsKey(country.getCountryId())) {
					this.country_queue.add(country.getCountryId());
				}
				this.countryMap.put(country.getCountryId(), country);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
