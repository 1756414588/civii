package com.game.uc.log;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.game.uc.log.domain.AccountCreateLog;
import com.game.uc.log.domain.AccountloginLog;
import com.game.uc.util.DateHelper;

/**
 *
 * @date 2019/12/30 11:08
 * @description 日志帮助类
 */
@Component
public class LogHelper {
	/**
	 * 所有日志用这个标识符拼接
	 */
	public static final String CONNECTOR = ",";
	/****************************** 账号相关日志 ************************************/
	public static final String ACCOUNT_CREATE_LOG = "account_create";
	public static final String ACCOUNT_LOGIN_LOG = "account_login";

	/***
	 *
	 * @param logName
	 *            logback xml配置的名称
	 * @param prams
	 *            参数
	 */
	public void log(String logName, Object... prams) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < prams.length; i++) {
			buffer.append(prams[i].toString());
			if (i == prams.length - 1) {
				break;
			}
			buffer.append(CONNECTOR);
		}
		LoggerFactory.getLogger(logName).info(buffer.toString());
	}

	/**
	 * 账号创建的日志文件
	 */
	public void accountCreateLog(AccountCreateLog accountCreateLog) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(accountCreateLog.getChannel());
		buffer.append(CONNECTOR);
		
		buffer.append(DateHelper.formatDateTime(accountCreateLog.getOperationTime(), DateHelper.format1));
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getAccountKey());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getAccount());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getImodel());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getImei());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getIp());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getCpu());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getDeviceUuid());
		buffer.append(CONNECTOR);
		
		buffer.append(accountCreateLog.getIdfa());
		
		LoggerFactory.getLogger(ACCOUNT_CREATE_LOG).info(buffer.toString());
	}
	
	
	/**
	 * 账号登录的日志文件
	 */
	public void accountLoginLog(AccountloginLog accountloginLog) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(accountloginLog.getChannel());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getVersion());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getServerId());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getAccountId());
		buffer.append(CONNECTOR);
		
		buffer.append(DateHelper.formatDateTime(accountloginLog.getLoginTime(), DateHelper.format1));
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getLoginIp());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getDeviceUuid());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getIdfa());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getImei());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getImodel());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getResolution());
		buffer.append(CONNECTOR);
		
		buffer.append(accountloginLog.getCpu());
		
		LoggerFactory.getLogger(ACCOUNT_LOGIN_LOG).info(buffer.toString());
	}
}
