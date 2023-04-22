package com.game.acion.login;

import com.game.domain.Lord;
import com.game.domain.Robot;
import com.game.domain.World;
import com.game.domain.WorldPos;
import com.game.domain.p.RobotData;
import com.game.manager.LoginManager;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.network.ChannelUtil;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.RoleLoginRs;
import com.game.server.AppPropertes;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 角色登录请求
 */
public class RoleLoginHandler extends MessageHandler {

    @Override
    public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
        RoleLoginRs msg = req.getExtension(RoleLoginRs.ext);
        RobotManager robotManager = getBean(RobotManager.class);
        Robot robot = robotManager.getRobotByKey(accountKey);

        Lord lord = new Lord();
        robot.setLord(lord);

        lord.setLordId(msg.getLordId());
        lord.setNick(msg.getNick());
        lord.setPortrait(msg.getPortrait());
        lord.setExp(msg.getExp());
        lord.setVip(msg.getVip());
        lord.setVipExp(msg.getVipDot());
        lord.setPosX(msg.getPosX());
        lord.setPosY(msg.getPosY());
        lord.setCountry(msg.getCountry());

        // 可击杀野怪等级
        World world = robot.getWorld();
        world.setMaxMonsterLv(msg.getMaxMonsterLv());

        // 新手引导步骤
        robot.setGuideKey(msg.getGuideKey());

        ChannelUtil.setRoleId(ctx, msg.getLordId());

        LogHelper.CHANNEL_LOGGER.info("[游戏.登录返回] accountKey:{} cmd:{} 坐标:{} pos:{}", robot.getId(), req.getCommand(), new WorldPos(msg.getPosX(), msg.getPosY()));

        RobotData data = robot.getData();
        data.setNick(msg.getNick());
        data.setRoleId(msg.getLordId());
        // 账号
        LoginManager loginManager = getBean(LoginManager.class);
        loginManager.loginSuccess(robot);

        // 登录初始化
        AppPropertes appPropertes = getBean(AppPropertes.class);
        if (appPropertes.isRobotAuto()) {
            getBean(EnterGameEvent.class).doEnterGame(robot);
        }
    }


}
