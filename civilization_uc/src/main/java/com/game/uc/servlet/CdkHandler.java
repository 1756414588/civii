package com.game.uc.servlet;

import com.game.spring.SpringUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.game.constant.UcCodeEnum;
import com.game.uc.Message;
import com.game.uc.manager.CdkManager;
import com.game.uc.service.CdkService;

/**
 *
 * @date 2020/6/1 16:52
 * @description
 */
@Controller
public class CdkHandler {

	@ResponseBody
	@RequestMapping(value = "/cdk/updateCdk.do", method = RequestMethod.POST)
	public Message updateCdk(int autoId, String oldKeyChar) throws Exception {
		boolean result = SpringUtil.getBean(CdkManager.class).updateCdk(autoId, oldKeyChar);
		if (!result) {
			return new Message(UcCodeEnum.CDK_NOT_EXIST);
		}
		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/cdk/refresh.do", method = RequestMethod.POST)
	public Message getServerList() throws Exception {
		SpringUtil.getBean(CdkManager.class).init();
		return new Message(UcCodeEnum.SUCCESS);
	}

	@ResponseBody
	@RequestMapping(value = "/cdk/getCdkAward.do", method = RequestMethod.POST)
	public Message getCdkAward(String cdk, long roleId, int serverId, int channel, int level) {
		Message message =
			SpringUtil.getBean(CdkService.class).getCdkAward(roleId, serverId, cdk, channel, level);
		return message;
	}
}