package com.game.recharge.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.game.constant.UcCodeEnum;
import com.game.pay.BaseOrderConfig;
import com.game.pay.channel.TypeIndef;
import com.game.pay.domain.KuaiYouConfig;
import com.game.recharge.Ihandler.BasePayHandler;
import com.game.recharge.manager.ChannelConfigManager;
import com.game.recharge.service.PayOrderService;
import com.game.uc.Message;
import com.game.uc.PayOrder;
import com.game.util.Md5Util;
import com.game.util.SortUtils;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class XinKuaiPayHandler extends BasePayHandler {
    @Override
    public Message validate(HttpServletRequest requetst, PayOrderService payOrderService, ChannelConfigManager channelConfigManager,String payName) {
        try {
            if (requetst.getInputStream() == null) {
                log.error("PayUtils : params {}, desc{} ", "数据流为空", UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }
            String pars = IOUtils.toString(requetst.getInputStream());
            if (null == pars || pars.equals("")) {
                log.error("PayUtils : params {}, desc{} ", pars, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }
            log.error("PayUtils : params {}", pars);
            Map<String, String> param = JsonToMap(pars);
            BaseOrderConfig kuaiYouOrder = JSON.parseObject(pars, BaseOrderConfig.class);
            String kySign = kuaiYouOrder.getSign();

            if (null == kySign || kySign.equals("") || null == kuaiYouOrder.getPrice() || null == kuaiYouOrder.getTrade_sn() || null == kuaiYouOrder.getOrder_id() || null == kuaiYouOrder.getExtradata() || null == kuaiYouOrder.getAppid()) {
                log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PARAM_ERROR.getDesc());
                return new Message(UcCodeEnum.PARAM_ERROR);
            }

            if (null == kySign || kySign.equals("")) {
                log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
                return new Message(UcCodeEnum.PAY_SIGN_ERROR);
            }

            param.remove("sign");
            KuaiYouConfig kuaiYouConfig = (KuaiYouConfig) channelConfigManager.getChanelConfigByAppId(kuaiYouOrder.getAppid());
            if (null == kuaiYouConfig) {
                log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.CHANNEL_CONFIG_ERROR.getDesc());
                return new Message(UcCodeEnum.CHANNEL_CONFIG_ERROR);
            }
            String formatKyParam = SortUtils.formatUrlParam(param, "utf-8", false) + kuaiYouConfig.getAppKey();
            String newKySign = Md5Util.string2MD5(formatKyParam);
            if (!kySign.equals(newKySign)) {
                log.error("PayUtils : params {}, desc{} ", param, UcCodeEnum.PAY_SIGN_ERROR.getDesc());
                return new Message(UcCodeEnum.PAY_SIGN_ERROR);
            }
            PayOrder payOrder = payOrderService.findPayOrder(kuaiYouOrder.getOrder_id());
            int price = kuaiYouOrder.getPrice();
            String trade_sn = kuaiYouOrder.getTrade_sn();
            String orderId = kuaiYouOrder.getOrder_id();
            Integer userid = kuaiYouOrder.getUserid();
            if (null == payOrder) {
                payOrder = new PayOrder(orderId, trade_sn, price);
                payOrder.setPlatId(String.valueOf(userid));
                payOrderService.createOrder(payOrder);
            } else {
                payOrder.resetData(orderId, trade_sn, price);
            }
            return afterValidate(payOrderService, payOrder);
        } catch (Exception e) {
            log.error("PayUtils : desc{} ", UcCodeEnum.PAY_SIGN_ERROR.getDesc());
            return new Message(UcCodeEnum.PARAM_ERROR);
        }
    }

    @Override
    public void register() {
        //HandlerManager.getInst().addHandler(TypeIndef.KY_FX.name(), this);

        addHandler(TypeIndef.KY_FX.getPayName(), this);
    }
}
