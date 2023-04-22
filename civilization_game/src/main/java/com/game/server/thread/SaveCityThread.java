package com.game.server.thread;

import com.game.domain.p.City;
import com.game.manager.CityManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 城池数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveCityThread extends SaveThread<City> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> city_queue = new LinkedBlockingQueue<Integer>();
	private HashMap<Integer, City> cityMap = new HashMap<Integer, City>();

	public SaveCityThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !city_queue.isEmpty()) {

			City city = null;
			synchronized (this) {
				Integer cityId = city_queue.poll();
				if (cityId != null) {
					city = cityMap.remove(cityId);
				}
			}
			if (city == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage(), e);
				}

			} else {
				try {
					CityManager cityManager = SpringUtil.getBean(CityManager.class);
					cityManager.update(city);
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("City Exception:" + city.getCityId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save count :" + saveCount);
	}

	@Override
	public void add(City city) {
		try {
			synchronized (this) {
				if (!cityMap.containsKey(city.getCityId())) {
					this.city_queue.add(city.getCityId());
				}
				this.cityMap.put(city.getCityId(), city);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}

}
