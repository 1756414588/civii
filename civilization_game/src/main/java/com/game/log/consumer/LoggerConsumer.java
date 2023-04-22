package com.game.log.consumer;

import cn.thinkingdata.tga.javasdk.ThinkingDataAnalytics;
import cn.thinkingdata.tga.javasdk.exception.InvalidArgumentException;
import com.game.log.consumer.domin.BaseProperties;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author cpz
 * @date 2020/12/5 19:43
 * @description 数数上报
 */
@Slf4j
@Component
public class LoggerConsumer {

	@Value("${LOG_DIRECTORY}")
	String LOG_DIRECTORY;
	@Value("${LOG_FILESIZE}")
	int FILE_SIZE;

	/**
	 * 初始化
	 */
	private ThinkingDataAnalytics ta;

	public void init() {
		//LoggerConsumer的配置类
		ThinkingDataAnalytics.LoggerConsumer.Config config = new ThinkingDataAnalytics.LoggerConsumer.Config(LOG_DIRECTORY);
		//设置在按天切分的前提下，按大小切分文件，单位是M,例如设置2G切分文件
		config.setFileSize(FILE_SIZE);
		ta = new ThinkingDataAnalytics(new ThinkingDataAnalytics.LoggerConsumer(config));
	}


	public boolean stop() {
		try {
			ta.flush();
			ta.close();
		} catch (Exception e) {
			//异常处理
			//System.out.println("except:" + e);
		}
		return true;
	}

	public void track(String event_name, Map<String, Object> properties) {
		try {
			//设置访客ID"ABCDEFG123456789"
			String distinct_id = "ABCDEFG123456789";
			//设置账号ID"TA_10001"
			String account_id = "TA_10001";
			if (ta != null) {
				ta.track(distinct_id, account_id, event_name, properties);
				ta.flush();
			}
		} catch (InvalidArgumentException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void track(BaseProperties baseProperties) {
		try {
			if (ta != null) {
				ta.track(baseProperties.getDistinct_id(), baseProperties.getAccount_id(), baseProperties.getEventName(), baseProperties.getProperties());
				ta.flush();
			}
		} catch (InvalidArgumentException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void track_update(BaseProperties baseProperties, String eventId) {
		try {
			if (ta != null) {
				ta.track_update(baseProperties.getDistinct_id(), baseProperties.getAccount_id(), baseProperties.getEventName(), eventId, baseProperties.getProperties());
				ta.flush();
			}
		} catch (InvalidArgumentException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void setOnce(BaseProperties baseProperties) {
		try {
			if (ta != null) {
				ta.user_setOnce(baseProperties.getDistinct_id(), baseProperties.getAccount_id(), baseProperties.getProperties());
				ta.flush();
			}
		} catch (InvalidArgumentException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	public void set(BaseProperties baseProperties) {
		try {
			if (ta != null) {
				ta.user_set(baseProperties.getDistinct_id(), baseProperties.getAccount_id(), baseProperties.getProperties());
				ta.flush();
			}
		} catch (InvalidArgumentException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
