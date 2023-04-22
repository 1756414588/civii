package com.game.uc.netty.handler;

import com.game.pay.channel.BaseChanelConfig;
import com.game.pb.AccountLoginPb;
import com.game.pb.BasePb;
import com.game.uc.domain.p.ServerList;
import com.game.uc.service.ServerService;
import com.game.uc.service.VerifyLoginService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginVerifyHandler extends BaseHandler {

    @Autowired
    ServerService serverService;

    @Autowired
    VerifyLoginService verifyLoginService;

    @Override
    public void register() {
        addHandler(AccountLoginPb.AccountLoginRq.EXT_FIELD_NUMBER, this);
    }

    @Override
    public void action(ChannelHandlerContext context, BasePb.Base msg) {
        try {
            AccountLoginPb.AccountLoginRq request = msg.getExtension(AccountLoginPb.AccountLoginRq.ext);
            String channelStr = request.getChannel();
            String packageName = request.getPackageName();
            String token = request.getToken();
            String account = request.getAccount();
            BaseChanelConfig baseChanelConfig = null;
            if (channelStr == null || channelStr.equals("NAN") || channelStr.trim().equals("")) {
                return;
            }
            if (!channelStr.trim().equals("1")) {
                if (packageName == null || packageName.equals("") || channelStr == null || channelStr.equals("") || token == null || token.equals("") || account == null || account.equals("")) {
                    return;
                }
                baseChanelConfig = verifyLoginService.verifyChannelLogin(packageName, Integer.parseInt(channelStr), account, token);
                if (baseChanelConfig == null) {
                    return;
                }
                account = baseChanelConfig.getParent_type() + "_" + account;
            }
            String version = request.getAppVersion() == null ? "" : request.getAppVersion();
            String imodel = request.getImodel() == null ? "" : request.getImodel();
            String imei = request.getImei() == null ? "" : request.getImei();
            String cpu = request.getCpu() == null ? "" : request.getCpu();
            String idfa = request.getIdfa() == null ? "" : request.getIdfa();
            String resolution = request.getResolution() == null ? "" : request.getResolution();
            String deviceUuid = request.getDeviceUuid() == null ? "" : request.getDeviceUuid();
            String ip = context.channel().remoteAddress().toString();
            String versionFile = request.getVersionFile() == null ? "" : request.getVersionFile();
            boolean flag = baseChanelConfig == null ? false : baseChanelConfig.getIs_review() == 1;
            ServerList serverList = serverService.getServerList(account, Integer.parseInt(channelStr), version, imodel, imei, cpu, idfa, resolution, deviceUuid, ip, flag, packageName, versionFile);
            sendAndFlush(context, AccountLoginPb.AccountLoginRs.ext, serverList.encode(), AccountLoginPb.AccountLoginRs.EXT_FIELD_NUMBER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
