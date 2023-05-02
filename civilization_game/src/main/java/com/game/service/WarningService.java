package com.game.service;

import com.alibaba.fastjson.JSONObject;
import com.game.domain.p.Mail;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.Charset;

/**
 * 预警管理
 *
 *
 * @date 2021/6/4 14:43
 */
@Service
public class WarningService {

	@Value("${mail.host}")
	private String host;
	@Value("${mail.username}")
	private String userName;
	@Value("${mail.password}")
	private String pwd;
	@Value("${mail.sendTo}")
	private String sendTo;

	@Autowired
	private JavaMailSenderImpl javaMailSender;


	/**
	 * Spring邮件配置
	 */
	@Bean
	public JavaMailSenderImpl javaMailSenderImpl() {
		JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
		javaMailSenderImpl.setHost(host);
		javaMailSenderImpl.setUsername(userName);
		javaMailSenderImpl.setPassword(pwd);
		javaMailSenderImpl.setDefaultEncoding("utf-8");
		return javaMailSenderImpl;
	}


	public void sendMail(String activityName, Mail mail) {
		MimeMessage message = javaMailSender.createMimeMessage();

		//发送带附件和内联元素的邮件需要将第二个参数设置为true
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(message, true);
			//发送方邮箱，和配置文件中的mail.username要一致
			helper.setFrom(userName);
			String[] sendToArr = sendTo.split(",");
			//接收方
			helper.setTo(sendToArr);
			//主题
			helper.setSubject("游戏警告!");
			//邮件内容
			StringBuilder builder = new StringBuilder();
			builder.append("活动: " + activityName + " 奖励短时间内发放多次!请立即检查!").append("\r\n");
			builder.append(JSONObject.toJSONString(mail));
			helper.setText(builder.toString(), false);
			javaMailSender.send(message);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("send mail error cause:{}", e.getMessage(), e);
		}
	}
}
