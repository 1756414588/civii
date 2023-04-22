package com.game.uc.netty.handler;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pb.AccountLoginPb;
import com.game.pb.BasePb;
import com.game.uc.service.VerifyLoginService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewHandler extends BaseHandler {
    @Override
    public void register() {
        addHandler(AccountLoginPb.ReviewRq.EXT_FIELD_NUMBER, this);
    }

    @Autowired
    VerifyLoginService verifyLoginService;

    @Override
    public void action(ChannelHandlerContext context, BasePb.Base msg) {
        AccountLoginPb.ReviewRq request = msg.getExtension(AccountLoginPb.ReviewRq.ext);
        String channelStr = request.getChannel();
        String packageName = request.getPackageName();
        BaseChanelConfig baseChanelConfig = null;
        if (channelStr == null || channelStr.equals("NAN") || channelStr.trim().equals("")) {
            return;
        }
        if (!channelStr.trim().equals("1")) {
            if (packageName == null || packageName.equals("") || channelStr == null || channelStr.equals("")) {
                return;
            }
            baseChanelConfig = verifyLoginService.verifyChannelLogin(packageName, Integer.parseInt(channelStr), null, null);
            if (baseChanelConfig == null) {
                return;
            }
        }
        boolean flag = baseChanelConfig == null ? false : baseChanelConfig.getIs_review() == 1;
        AccountLoginPb.ReviewRs.Builder builder = AccountLoginPb.ReviewRs.newBuilder();
        builder.setReview(flag);
        sendAndFlush(context, AccountLoginPb.ReviewRs.ext, builder.build(), AccountLoginPb.ReviewRs.EXT_FIELD_NUMBER);
    }
}
