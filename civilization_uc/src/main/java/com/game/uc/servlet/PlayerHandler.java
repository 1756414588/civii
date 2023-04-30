package com.game.uc.servlet;

import com.game.spring.SpringUtil;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.game.constant.UcCodeEnum;
import com.game.pay.domain.KuaiYouConfig;
import com.game.pay.domain.KyData;
import com.game.pay.domain.KyValidateParams;
import com.game.uc.Message;
import com.game.uc.Server;
import com.game.uc.manager.AccountManager;
import com.game.uc.manager.ChannelConfigManager;
import com.game.uc.manager.ServerManager;
import com.game.uc.service.PlayerService;
import com.game.util.HttpUtil;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

/**
 *
 * @date 2020/6/2 16:55
 * @description
 */
@Controller
public class PlayerHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @ResponseBody
    @RequestMapping(value = "/player/closeAccount.do", method = RequestMethod.POST)
    public Message closeAccount(int accountKey) {
        SpringUtil.getBean(AccountManager.class).init(2);
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/openAccount.do", method = RequestMethod.POST)
    public Message openAccount(int accountKey) {
        SpringUtil.getBean(AccountManager.class).init(2);
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/closeRole.do", method = RequestMethod.POST)
    public Message closeRole(long roleId, int serverId) {
        Message message = SpringUtil.getBean(PlayerService.class).closeRole(roleId, serverId);
        if (message.getCode() == UcCodeEnum.SUCCESS.getCode()) {
            SpringUtil.getBean(AccountManager.class).init(1);
        }
        return message;
    }

    @ResponseBody
    @RequestMapping(value = "/player/openRole.do", method = RequestMethod.POST)
    public Message openRole(int roleId, int serverId) {
        Message message = SpringUtil.getBean(PlayerService.class).openRole(roleId, serverId);
        if (message.getCode() == UcCodeEnum.SUCCESS.getCode()) {
            SpringUtil.getBean(AccountManager.class).init(1);
        }
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/closeSpeak.do", method = RequestMethod.POST)
    public Message closeSpeak(long roleId, int serverId, long endTime) {
        SpringUtil.getBean(AccountManager.class).init(3);
        Message message = SpringUtil.getBean(PlayerService.class).closeSpeak(roleId, serverId, endTime);
        return message;
    }

    @ResponseBody
    @RequestMapping(value = "/player/openSpeak.do", method = RequestMethod.POST)
    public Message openSpeak(long roleId, int serverId) {
        SpringUtil.getBean(AccountManager.class).init(3);
        Message message = SpringUtil.getBean(PlayerService.class).openSpeak(roleId, serverId);
        return message;
    }

    /**
     * 快游红包SDK查询角色接口
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/player/findRole.do", method = RequestMethod.POST)
    public void findRole(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();

        try {
            //获取参数
            String parmas = IOUtils.toString(request.getInputStream());
            if (null == parmas) {
                writer.write(JSON.toJSONString(new KyValidateParams(100, "参数错误")));
                return;
            }

            String[] split = parmas.split("&");
            Map<String, String> param = new HashMap<>();
            for (String string : split) {
                String[] splitKey = string.split("=");
                param.put(splitKey[0], splitKey[1]);
            }

            String userid = param.get("userid");
            String appId = param.get("appId");
            String roleid = param.get("roleid");
            String serverid = param.get("serverid");
            String sign = param.get("sign");

            //判断参数
            if (null == userid || userid.equals("") || null == appId || appId.equals("") || null == roleid
                    || roleid.equals("") || null == serverid || serverid.equals("") || null == sign
                    || sign.equals("")) {

                logger.error("PlayerHandler findRole : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "参数错误")));
                return;
            }

            //检查SDK游戏的参数配置
            ChannelConfigManager channelConfigManager = SpringUtil.getBean(ChannelConfigManager.class);
            KuaiYouConfig kuaiYou2318Config = (KuaiYouConfig) channelConfigManager
                    .getChanelConfigByAppId(Integer.parseInt(appId));

            if (null == kuaiYou2318Config) {
                logger.error("PlayerHandler findRole : params {}, desc{} ", param, UcCodeEnum.CHANNEL_CONFIG_ERROR.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "invalid appId")));
                return;
            }

            param.remove("sign");
            String formatUrlParam = SortUtils.formatUrlParam(param, "utf-8", false) + kuaiYou2318Config.getAppKey();
            String newSign = Md5Util.string2MD5(formatUrlParam);

            //验证签名
            if (!sign.equals(newSign)) {
                logger.error("PlayerHandler : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "签名检验失败")));
                return;
            }

            //到对应区服查找玩家信息
            ServerManager serverManager = SpringUtil.getBean(ServerManager.class);
            Server serverById = serverManager.getServerById(Integer.parseInt(serverid));
            if (null == serverById) {
                logger.error("PlayerHandler findRole : params {}, desc{} ", param, UcCodeEnum.SERVER_NOT_EXIST.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "区服不存在")));
                return;
            }

            Map<String, String> parms = new HashMap<>();
            parms.put("lordId", roleid);
            String sendHttpPost = HttpUtil.sendHttpPost("http://" + serverById.getIp() + ":" + serverById.getHttpPort() + "/log/queryPlayer", parms);
            if (null == sendHttpPost) {//请求异常
                logger.error("PlayerHandler findRole : params {}, desc{} ", param, UcCodeEnum.SYS_ERROR.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "请求失败")));
                return;
            }

            Message message = JSON.parseObject(sendHttpPost, Message.class);
            if (message.getCode() != UcCodeEnum.SUCCESS.getCode()) {//玩家信息不存在
                logger.error("PlayerHandler findRole : params {}, desc{} ", param, UcCodeEnum.PLAYER_IS_NOT_EXIST.getDesc());
                writer.write(JSON.toJSONString(new KyValidateParams(100, "玩家信息不存在")));
                return;
            }

            JSONObject player = JSON.parseObject(message.getData());
            Integer lordId = (Integer) player.get("lordId");
            Integer serverId = (Integer) player.get("serverId");
            String rolename = (String) player.get("name");
            Integer rolelevel = (Integer) player.get("level");
            Long role_createtime = (Long) player.get("registerTime");
            Integer coin_num = (Integer) player.get("totalGold");
            Integer vip = (Integer) player.get("vipLevel");

            KyData kyData = new KyData(lordId, serverId, rolename, rolelevel, role_createtime / 1000, coin_num, vip, "");
            String successMes = JSON.toJSONString(new KyValidateParams(0, "操作成功", kyData));
            writer.write(successMes);
            logger.info("PlayerHandler findRole : params {}, desc{} ", successMes, UcCodeEnum.SUCCESS.getDesc());
        } catch (Exception e) {
            e.printStackTrace();
            writer.write(JSON.toJSONString(new KyValidateParams(100, "接口异常")));
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
    }

    @ResponseBody
    @RequestMapping(value = "/player/closeIP.do", method = RequestMethod.POST)
    public Message closeIP() {
        SpringUtil.getBean(AccountManager.class).initCloseIp();
        return new Message(UcCodeEnum.SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/player/closeUUID.do", method = RequestMethod.POST)
    public Message closeUUID() {
        SpringUtil.getBean(AccountManager.class).initCloseUuid();
        return new Message(UcCodeEnum.SUCCESS);
    }
}
