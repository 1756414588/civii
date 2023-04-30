package com.game.log;

import com.game.constant.LogTable;
import com.game.constant.ResourceType;
import com.game.domain.Player;
import com.game.domain.RecordReissueAwards;
import com.game.domain.p.Mission;
import com.game.log.domain.*;
import com.game.manager.WorldManager;
import com.game.util.DateHelper;
import com.game.spring.SpringUtil;
import com.game.util.StringUtil;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 *
 * @date 2019/12/30 11:08
 * @description 日志帮助类
 */
@Component
public class LogUser {
    /**
     * 所有日志用这个标识符拼接
     */
    public static final String CONNECTOR = ",";
    /**
     * *************************** 角色相关日志 ***********************************
     */
    public static final String ROLE_LOGIN_LOG = "role_login";

    public static final String ROLE_CREATE_LOG = "role_create";
    public static final String ROLE_TITLE_LOG = "role_title";

    /**
     * *************************** 角色资源产出和消耗日志 ***********************************
     */
    public static final String ROLE_RESOURCE_LOG = "role_resource";

    /**
     * *************************** 英雄相关日志 ***********************************
     */
    public static final String HERO_EXP_LOG = "hero_exp";

    public static final String HERO_ADVANCE_LOG = "hero_advance";
    public static final String HERO_WASH_LOG = "hero_wash";
    public static final String HERO_DIVINE_LOG = "hero_divine";

    /**
     * *************************** 装备相关日志 ***********************************
     */
    public static final String EQUIP_ADD = "equip_add"; //装备获得
    public static final String EQUIP_WASH_LOG = "equip_wash";

    public static final String EQUIP_DECOMPOUND_LOG = "equip_decompound_log";//装备分解
    public static final String KILL_EQUIP_LOG = "kill_equip_log";

    /**
     * *************************** VIP相关日志 ***********************************
     */
    public static final String VIP_EXP_LOG = "vip_exp";

    /**
     * *************************** 新手引导日志 ***********************************
     */
    public static final String ROLE_GUIDE_LOG = "role_guide";

    /**
     * *************************** 关卡日志 ***********************************
     */
    public static final String ROLE_MISSON_LOG = "role_misson";

    /**
     * *************************** 任务日志 ***********************************
     */
    public static final String ROLE_TASK_LOG = "role_task";

    /**
     * *************************** 金币 ***********************************
     */
    public static final String ROLE_IRON_LOG = "role_iron_log";

    /**
     * *************************** 钢铁 ***********************************
     */
    public static final String ROLE_COPPER_LOG = "role_copper_log";

    /**
     * *************************** 食物 ***********************************
     */
    public static final String ROLE_OIL_LOG = "role_oil_log";

    /**
     * *************************** 晶体 ***********************************
     */
    public static final String ROLE_STONE_LOG = "role_stone_log";

    /**
     * *************************** 钻石 ***********************************
     */
    public static final String ROLE_GOLD_LOG = "role_gold_log";

    /**
     * ************************* 道具 ***********************************
     */
    public static final String ROLE_ITEM_LOG = "role_item_log";

    /**
     * *************************** 聊天记录 ***********************************
     */
    public static final String CHAT_LOG = "chat_log";
    /**
     * *************************** 塔防记录 ***********************************
     */
    public static final String TD_LOG = "td_log";


    /****************************** 美女 ************************************/
    public static final String BEAUTY_ITEM_LOG = "beauty_item_log";


    public static void log(LogTable logTable,Object object) {
        LoggerFactory.getLogger(logTable.table()).info(object.toString());
    }

    /**
     * *
     *
     * @param logName logback xml配置的名称
     * @param prams   参数
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

    public void recordTDLog(TDLog log) {
        StringBuffer buffer = new StringBuffer();
        //玩家id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);
        //塔防id
        buffer.append(log.getTdId());
        buffer.append(CONNECTOR);
        //是否可以进入
        buffer.append(log.getIsEnter());
        buffer.append(CONNECTOR);
        //进入/通关
        buffer.append(log.getType());
        buffer.append(CONNECTOR);
        //通关状态
        buffer.append(log.getState());
        buffer.append(CONNECTOR);
        //剩余血量
        buffer.append(log.getLessHp());
        LoggerFactory.getLogger(TD_LOG).info(buffer.toString());
        sendLog(TD_LOG, log);
    }

    /**
     * 角色登录日志采集
     *
     * @param player
     */
    public void roleLoginLog(Player player, String ip) {
        StringBuffer buffer = new StringBuffer();

        // `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
        buffer.append(player.getLord().getLordId());
        buffer.append(CONNECTOR);

        // `rolename` varchar(50) DEFAULT NULL, 玩家角色名称
        buffer.append(player.getLord().getNick());
        buffer.append(CONNECTOR);

        // `server_id` int(11) DEFAULT NULL COMMENT '玩家角色所属区服',
        buffer.append(player.account.getServerId());
        buffer.append(CONNECTOR);

        // `account` int(11) NOT NULL, '平台账号ID'
        buffer.append(player.account.getAccountKey());
        buffer.append(CONNECTOR);

        // `channel` varchar(50) DEFAULT NULL, 账号平台
        buffer.append(player.account.getChannel());
        buffer.append(CONNECTOR);

        // childNo 账号子平台
        buffer.append(player.account.getChildNo());
        buffer.append(CONNECTOR);

        // country 该角色所属阵营countryId
        buffer.append(player.getLord().getCountry());
        buffer.append(CONNECTOR);

        // 玩家角色军衔
        buffer.append(player.getLord().getTitle());
        buffer.append(CONNECTOR);

        // 玩家角色军功值
        buffer.append(player.getLord().getHonor());
        buffer.append(CONNECTOR);

        // 玩家角色战力值
        buffer.append(player.getSimpleData().getMaxScore());
        buffer.append(CONNECTOR);

        // 角色等级：玩家角色等级
        buffer.append(player.getLord().getLevel());
        buffer.append(CONNECTOR);

        // 角色当前经验值：玩家当前拥有的经验值
        buffer.append(player.getLord().getExp());
        buffer.append(CONNECTOR);

        // 角色VIP等级：玩家角色VIP等级
        buffer.append(player.getLord().getVip());
        buffer.append(CONNECTOR);

        // 角色VIP经验值：玩家当前拥有的VIP经验值
        buffer.append(player.getLord().getVipExp());
        buffer.append(CONNECTOR);

        // 角色所在区域：角色所在地图Id
        buffer.append(
                SpringUtil.getBean(WorldManager.class).getMapId(player.getPosX(), player.getPosY()));
        buffer.append(CONNECTOR);

        // newState` bigint(20) NOT NULL DEFAULT '0' COMMENT '新手引导步骤',
        buffer.append(player.getLord().getNewState());
        buffer.append(CONNECTOR);

        // `nowMission` int(11) DEFAULT NULL COMMENT '玩家当前关卡',
        Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
        if (missions.size() > 0) {
            Integer maxMapId = getMaxKey(missions.keySet());
            Map<Integer, Mission> map = missions.get(maxMapId);
            Integer maxMissionId = getMaxKey(map.keySet());

            buffer.append(maxMissionId);
            buffer.append(CONNECTOR);
        } else {
            buffer.append(0);
            buffer.append(CONNECTOR);
        }

        // `gold` int(11) DEFAULT NULL, 角色的钻石
        buffer.append(player.getLord().getGold());
        buffer.append(CONNECTOR);

        // 角色当前英雄数量：玩家角色当前拥有的英雄数量
        buffer.append(player.getHeros().size());
        buffer.append(CONNECTOR);

        // 角色当前装备数量：玩家角色当前拥有的装备数量
        buffer.append(player.getEquips().size());
        buffer.append(CONNECTOR);

        // 角色当前美女数量：玩家角色当前拥有的美女数量
        buffer.append(0);
        buffer.append(CONNECTOR);

        // `advance_card` int(11) DEFAULT NULL, advance_card
        // 玩家角色当前晋升卡数量：该英雄所属玩家角色当前晋升卡数量
        buffer.append(player.getItemNum(HeroAdvanceLog.ADVANCE_CARD_TYPE));
        buffer.append(CONNECTOR);

        // 角色当前资源数量：玩家角色当前拥有的资源类型的数量
        buffer.append(player.getResource(1));
        buffer.append(CONNECTOR);

        buffer.append(player.getResource(2));
        buffer.append(CONNECTOR);

        buffer.append(player.getResource(3));
        buffer.append(CONNECTOR);

        buffer.append(player.getResource(4));
        buffer.append(CONNECTOR);

        // 角色当前士兵数量：玩家角色当前拥有的士兵类型的数量
        buffer.append(player.getSoldier(1).getNum());
        buffer.append(CONNECTOR);

        buffer.append(player.getSoldier(2).getNum());
        buffer.append(CONNECTOR);

        buffer.append(player.getSoldier(3).getNum());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(player.account.getCreateDate(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `model` varchar(50) DEFAULT NULL COMMENT '型号',
        buffer.append("model");
        buffer.append(CONNECTOR);

        // `mei` varchar(50) DEFAULT NULL COMMENT '手机MEI',
        buffer.append("mei");
        buffer.append(CONNECTOR);

        // `ip` varchar(50) DEFAULT NULL,
        buffer.append(ip);
        buffer.append(CONNECTOR);

        // `online_second` int(20) DEFAULT NULL COMMENT '累计在线时长 秒',
        buffer.append(player.getLord().getOlTime());
        buffer.append(CONNECTOR);

        // online_day
        buffer.append(player.account.getLoginDays());
        buffer.append(CONNECTOR);

        // idfa
        buffer.append(player.account.getDeviceNo());
        buffer.append(CONNECTOR);

        // 设备UUID：手机UUID（取最后一次登录机型）
        buffer.append("UUID");

        LoggerFactory.getLogger(ROLE_LOGIN_LOG).info(buffer.toString());
        sendLog(ROLE_LOGIN_LOG, RoleLoginLog.builder()
                .roleId(player.getLord().getLordId())
                .roleNme(player.getNick())
                .serverId(player.account.getServerId())
                .account(player.account.getAccountKey())
                .channel(player.account.getChannel() + "")
                .childNo(player.account.getChildNo() + "")
                .country(player.getCountry())
                .titile(player.getTitle())
                .honor(player.getHonor())
                .maxScore(player.getSimpleData().getMaxScore())
                .level(player.getLevel())
                .exp(player.getExp())
                .vip(player.getVip())
                .vipExp(player.getVipExp())
                .mapId(SpringUtil.getBean(WorldManager.class).getMapId(player.getPosX(), player.getPosY()))
                .newState(player.getLord().getNewState())
                .nowMission(player.getLord().getCurMainDupicate())
                .gold(player.getGold())
                .heroCount(player.getHeros().size())
                .equipsCount(player.getEquips().size())
                .ladyCount(player.getBeautys().size())
                .advanceCard(player.getItemNum(HeroAdvanceLog.ADVANCE_CARD_TYPE))
                .res1Count(player.getResource(1))
                .res2Count(player.getResource(2))
                .res3Count(player.getResource(3))
                .res4Count(player.getResource(4))
                .soldier1Count(player.getSoldier(1).getNum())
                .soldier2Count(player.getSoldier(2).getNum())
                .soldier3Count(player.getSoldier(3).getNum())
                .roleCreateTime(player.account.getCreateDate())
                .model("model")
                .imei("mei")
                .ip(ip)
                .onlineSecond(player.getLord().getOlTime())
                .onlineDay(player.account.getLoginDays())
                .idfa(player.account.getDeviceNo())
                .uuId("UUID")
                .build());
    }

    /**
     * 角色创建日志采集
     *
     * @param player
     */
    public void roleCreateLog(Player player) {
        StringBuffer buffer = new StringBuffer();

        // channel
        buffer.append(player.account.getChannel());
        buffer.append(CONNECTOR);

        // server_id 服务器ID
        buffer.append(player.account.getServerId());
        buffer.append(CONNECTOR);

        // role_id 角色ID
        buffer.append(player.getLord().getLordId());
        buffer.append(CONNECTOR);

        // profession_type
        buffer.append(0);
        buffer.append(CONNECTOR);

        // operation_time 创建角色所用时间
        buffer.append(DateHelper.formatDateTime(player.account.getCreateDate(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // rolename
        buffer.append(player.getLord().getNick());
        buffer.append(CONNECTOR);

        // account
        buffer.append(player.account.getAccountKey());
        buffer.append(CONNECTOR);

        // is_rand 1 随机取名 2不是
        buffer.append("1");
        buffer.append(CONNECTOR);

        // model 型号
        buffer.append("0-0-0-0-0-0-0-0");
        buffer.append(CONNECTOR);

        // mei
        buffer.append("0-0-0-0-0-0-0-0");
        buffer.append(CONNECTOR);

        // mac
        buffer.append("0-0-0-0-0-0-0-0");
        buffer.append(CONNECTOR);

        // idfa
        buffer.append(player.account.getDeviceNo());

        LoggerFactory.getLogger(ROLE_CREATE_LOG).info(buffer.toString());
        sendLog(ROLE_CREATE_LOG, RoleCreateLog.builder()
                .channel(player.account.getChannel() + "")
                .serverId(player.account.getServerId())
                .roleId(player.getLord().getLordId())
                .professionType(0)
                .operationTime(player.account.getCreateDate())
                .roleName(player.getNick())
                .account(player.account.getAccountKey() + "")
                .isRand(1)
                .model("0-0-0-0-0-0-0-0")
                .mei("0-0-0-0-0-0-0-0")
                .mac("0-0-0-0-0-0-0-0")
                .idfa(player.account.getDeviceNo())
                .build());
    }

    /**
     * 获取mapkey最大值
     *
     * @return
     */
    public Integer getMaxKey(Set<Integer> set) {
        Object[] obj = set.toArray();
        Arrays.sort(obj);
        return Integer.parseInt(obj[obj.length - 1].toString());
    }

    /**
     * 角色升级log需要采集信息
     */
    public void roleExpLog(RoleExpLog expLog) {
        LoggerFactory.getLogger(LogTable.role_exp.table()).info(expLog.toString());
//        sendLog(ROLE_EXP_LOG, expLog);
    }

    /**
     * 英雄升级log需要采集信息
     */
    public void heroExpLog(HeroExpLog expLog) {
        StringBuffer buffer = new StringBuffer();

        // `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
        buffer.append(expLog.getRoleId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(expLog.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(expLog.getRolelv());
        buffer.append(CONNECTOR);

        // `hero_key_Id` long(15) DEFAULT NULL, heroKeyId
        // 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851
        buffer.append(expLog.getHeroKeyId());
        buffer.append(CONNECTOR);

        // `hero_Id` long(15) DEFAULT NULL, heroKeyId
        buffer.append(expLog.getHeroId());
        buffer.append(CONNECTOR);

        // `hero_name` long(15) DEFAULT NULL, heroKeyId 该英雄名称
        buffer.append(expLog.getHeroName());
        buffer.append(CONNECTOR);

        // `hero_type` int(11) DEFAULT NULL, hero_type 该英雄名称
        buffer.append(expLog.getHeroType());
        buffer.append(CONNECTOR);

        // `hero_lev` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(expLog.getHeroLev());
        buffer.append(CONNECTOR);

        // `hero_exp` int(11) DEFAULT NULL, hero_exp 英雄经验：该英雄当前经验值
        buffer.append(expLog.getHeroExp());
        buffer.append(CONNECTOR);

        // `incre_exp` int(11) DEFAULT NULL, 增加经验
        buffer.append(expLog.getIncreaseExp());
        buffer.append(CONNECTOR);

        /**
         * MISSION(1,"副本"),
         *
         * <p>* MONSTER(2,"世界野怪"), * * COUNTRY_WAR(3,"阵营战"), * * CITY_WAR(4,"城战"), * * PROP(5,"道具");)
         */
        buffer.append(expLog.getExpType());
        buffer.append(CONNECTOR);

        buffer.append(expLog.getChannel());

        LoggerFactory.getLogger(HERO_EXP_LOG).info(buffer.toString());
        sendLog(HERO_EXP_LOG, expLog);
    }

    /**
     * 英雄突破日志
     *
     * @param log
     */
    public void heroAvanceLog(HeroAdvanceLog log) {
        StringBuffer buffer = new StringBuffer();

        // `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRolelv());
        buffer.append(CONNECTOR);

        // `hero_key_Id` long(15) DEFAULT NULL, heroKeyId
        // 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851
        buffer.append(log.getHeroKeyId());
        buffer.append(CONNECTOR);

        // `hero_Id` long(15) DEFAULT NULL, heroKeyId
        buffer.append(log.getHeroId());
        buffer.append(CONNECTOR);

        // `hero_name` long(15) DEFAULT NULL, heroKeyId 该英雄名称
        buffer.append(log.getHeroName());
        buffer.append(CONNECTOR);

        // `hero_type` int(11) DEFAULT NULL, hero_type 该英雄类型
        buffer.append(log.getHeroType());
        buffer.append(CONNECTOR);

        // `hero_lev` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(log.getHeroLev());
        buffer.append(CONNECTOR);

        // `quality` int(11) DEFAULT NULL, quality 英雄品质：该英雄当前品质
        buffer.append(log.getQuality());
        buffer.append(CONNECTOR);

        // `advance_card` int(11) DEFAULT NULL, advance_card
        // 玩家角色当前晋升卡数量：该英雄所属玩家角色当前晋升卡数量
        buffer.append(log.getAdvanceCard());
        buffer.append(CONNECTOR);

        // 消耗晋升卡数量
        buffer.append(log.getAdvanceCost());
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());

        LoggerFactory.getLogger(HERO_ADVANCE_LOG).info(buffer.toString());
        sendLog(HERO_ADVANCE_LOG, log);
    }

    /**
     * 英雄洗练日志
     *
     * @param log
     */
    public void heroWashLog(HeroWashLog log) {
        StringBuffer buffer = new StringBuffer();

        // `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRolelv());
        buffer.append(CONNECTOR);

        // `hero_key_Id` long(15) DEFAULT NULL, heroKeyId
        // 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851
        buffer.append(log.getHeroKeyId());
        buffer.append(CONNECTOR);

        // `hero_Id` long(15) DEFAULT NULL, heroKeyId
        buffer.append(log.getHeroId());
        buffer.append(CONNECTOR);

        // `hero_name` long(15) DEFAULT NULL, heroKeyId 该英雄名称
        buffer.append(log.getHeroName());
        buffer.append(CONNECTOR);

        // `hero_type` int(11) DEFAULT NULL, hero_type 该英雄类型
        buffer.append(log.getHeroType());
        buffer.append(CONNECTOR);

        // `hero_lev` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(log.getHeroLev());
        buffer.append(CONNECTOR);

        // `quality` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(log.getQuality());
        buffer.append(CONNECTOR);

        // attack int(11) DEFAULT NULL, hero_type 攻击
        buffer.append(log.getAttack());
        buffer.append(CONNECTOR);

        // defence int(11) DEFAULT NULL, 防御
        buffer.append(log.getDefence());
        buffer.append(CONNECTOR);

        // soldierNum int(11) DEFAULT NULL,// 总兵力
        buffer.append(log.getSoldierNum());
        buffer.append(CONNECTOR);

        // maxTotalLimit int(11) DEFAULT NULL,// 英雄总资质上限：该英雄总资质上限
        buffer.append(log.getMaxTotalLimit());
        buffer.append(CONNECTOR);

        // maxTotalFlag int(11) DEFAULT NULL,// // 英雄资质上限是否满：1代表满了，0代表没满
        buffer.append(log.getMaxTotalFlag());
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());
        buffer.append(CONNECTOR);

        buffer.append(log.getCostGold());

        LoggerFactory.getLogger(HERO_WASH_LOG).info(buffer.toString());
        sendLog(HERO_WASH_LOG, log);
    }

    /**
     * 英雄晋升传奇日志
     *
     * @param log
     */
    public void heroDivineLog(HeroDivineLog log) {
        StringBuffer buffer = new StringBuffer();

        // `role_id` bigint(20) DEFAULT NULL COMMENT '角色ID',
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRolelv());
        buffer.append(CONNECTOR);

        // `hero_key_Id` long(15) DEFAULT NULL, heroKeyId
        // 根据userId&该英雄heroType生成KeyId;例如：userId为101085；heroType为1；则KeyId为1010851
        buffer.append(log.getHeroKeyId());
        buffer.append(CONNECTOR);

        // `hero_Id` long(15) DEFAULT NULL, heroKeyId
        buffer.append(log.getHeroId());
        buffer.append(CONNECTOR);

        // `hero_name` long(15) DEFAULT NULL, heroKeyId 该英雄名称
        buffer.append(log.getHeroName());
        buffer.append(CONNECTOR);

        // `hero_type` int(11) DEFAULT NULL, hero_type 该英雄类型
        buffer.append(log.getHeroType());
        buffer.append(CONNECTOR);

        // `hero_lev` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(log.getHeroLev());
        buffer.append(CONNECTOR);

        // `quality` int(11) DEFAULT NULL, hero_type 英雄等级：该英雄当前等级
        buffer.append(log.getQuality());
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());

        LoggerFactory.getLogger(HERO_DIVINE_LOG).info(buffer.toString());
        sendLog(HERO_DIVINE_LOG, log);
    }

    // 军衔升级log需要采集信息如下
    public void roleTitleLog(Player player, long count) {
        // 角色ID：玩家角色ID
        // 角色名：玩家角色名称
        // 角色等级：玩家角色等级
        // 角色军衔：玩家角色军衔
        // 角色军功：角色当前军功值
        // 升级军衔消耗军功：角色升级军衔消耗军功值
        // 角色所属阵营：该角色所属阵营countryId
        // 角色VIP等级：玩家角色VIP等级
        // 角色创建时间：玩家角色创建时间
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(player.getLord().getLordId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(player.account.getCreateDate(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(player.getLord().getLevel());
        buffer.append(CONNECTOR);

        // `viplv` int(11) DEFAULT NULL COMMENT 'vip等级',
        buffer.append(player.getLord().getVip());
        buffer.append(CONNECTOR);

        // `title` int(11) DEFAULT NULL, // 角色军衔：玩家角色军衔
        buffer.append(player.getLord().getTitle());
        buffer.append(CONNECTOR);

        // `honor` int(11) DEFAULT NULL, // 角色军功：角色当前军功值
        buffer.append(player.getLord().getHonor());
        buffer.append(CONNECTOR);

        // `decre_honor` bigint(15) DEFAULT NULL, // 升级军衔消耗军功：角色升级军衔消耗军功值
        buffer.append(count);
        buffer.append(CONNECTOR);

        // 角色所属阵营：该角色所属阵营countryId
        buffer.append(player.getLord().getCountry());
        buffer.append(CONNECTOR);

        buffer.append(player.account.getChannel());

        LoggerFactory.getLogger(ROLE_TITLE_LOG).info(buffer.toString());

        sendLog(ROLE_TITLE_LOG, RoleTitleLog.builder().roleId(player.getLord().getLordId())
                .roleCreateTime(player.account.getCreateDate())
                .rolelv(player.getLord().getLevel())
                .viplv(player.getVip())
                .title(player.getTitle())
                .honor(player.getHonor())
                .decreHonor(count)
                .country(player.getCountry())
                .channel(player.account.getChannel()).build());
    }

    /**
     * 角色资源产出和消耗日志
     */
    public void roleResourceLog(RoleResourceLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRolelv());
        buffer.append(CONNECTOR);

        // `viplv` int(11) DEFAULT NULL COMMENT 'vip等级',
        buffer.append(log.getViplv());
        buffer.append(CONNECTOR);

        // `title` int(11) DEFAULT NULL, // 角色军衔：玩家角色军衔
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // `honor` int(11) DEFAULT NULL, // 角色军功：角色当前军功值
        buffer.append(log.getHonor());
        buffer.append(CONNECTOR);

        // 角色所属阵营：该角色所属阵营countryId
        buffer.append(log.getCountryId());
        buffer.append(CONNECTOR);

        // 角色当前拥有资源数量
        buffer.append(log.getResourceCount());
        buffer.append(CONNECTOR);

        // 操作类型 1.产出 2.消耗
        buffer.append(log.getOperateType());
        buffer.append(CONNECTOR);

        buffer.append(log.getChangeCount());
        buffer.append(CONNECTOR);

        buffer.append(log.getResourceType());
        buffer.append(CONNECTOR);

        buffer.append(log.getInfoType());
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());

        LoggerFactory.getLogger(ROLE_RESOURCE_LOG).info(buffer.toString());
        sendLog(ROLE_RESOURCE_LOG, log);
    }

    /**
     * 装备洗练
     */
    public void equipWashLog(EquipWashLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // role_name
        buffer.append(log.getRoleName());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRoleLv());
        buffer.append(CONNECTOR);

        // `viplv` int(11) DEFAULT NULL COMMENT 'vip等级',
        buffer.append(log.getVip());
        buffer.append(CONNECTOR);

        // `title` int(11) DEFAULT NULL, // 角色军衔：玩家角色军衔
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // 角色所属阵营：该角色所属阵营countryId
        buffer.append(log.getCountry());
        buffer.append(CONNECTOR);

        // 玩家物品的KeyId
        buffer.append(log.getKeyId());
        buffer.append(CONNECTOR);

        // 装备ID
        buffer.append(log.getEquipId());
        buffer.append(CONNECTOR);

        // 装备品质
        buffer.append(log.getQuality());
        buffer.append(CONNECTOR);

        // 精研类型 1,免费 2.消耗钻石
        buffer.append(log.getWashType());
        buffer.append(CONNECTOR);

        // 是否满级
        buffer.append(log.getIsfull());
        buffer.append(CONNECTOR);

        // 剩余精研次数
        buffer.append(log.getSurTimes());
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());

        LoggerFactory.getLogger(EQUIP_WASH_LOG).info(buffer.toString());
        sendLog(EQUIP_WASH_LOG, log);
    }

    /**
     * vip经验日志
     */
    public void vipExpLog(VipExpLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // roleName
        buffer.append(log.getRoleName());
        buffer.append(CONNECTOR);

        // channel
        buffer.append(log.getChannel());
        buffer.append(CONNECTOR);

        // server
        buffer.append(log.getServer());
        buffer.append(CONNECTOR);

        // 玩家等级
        buffer.append(log.getRoleLev());
        buffer.append(CONNECTOR);

        // vipLev
        buffer.append(log.getVipLev());
        buffer.append(CONNECTOR);

        // vipExp
        buffer.append(log.getVipExp());
        buffer.append(CONNECTOR);

        // increaseExp
        buffer.append(log.getIncreaseExp());
        buffer.append(CONNECTOR);

        // money
        buffer.append(log.getMoney());
        buffer.append(CONNECTOR);

        // topup
        buffer.append(log.getTopup());
        buffer.append(CONNECTOR);

        // gold
        buffer.append(log.getGold());
        buffer.append(CONNECTOR);

        // lastLoginTime
        buffer.append(log.getLastLoginTime());
        buffer.append(CONNECTOR);

        buffer.append(log.getFree());

        LoggerFactory.getLogger(VIP_EXP_LOG).info(buffer.toString());
        sendLog(VIP_EXP_LOG, log);
    }

    /**
     * 新手引导日志
     */
    public void roleGuideLog(RoleGuideLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // server
        buffer.append(log.getServer());
        buffer.append(CONNECTOR);

        // channel
        buffer.append(log.getChannel());
        buffer.append(CONNECTOR);

        // guideKey 新手引导步骤
        buffer.append(log.getGuideKey());

        LoggerFactory.getLogger(ROLE_GUIDE_LOG).info(buffer.toString());
        sendLog(ROLE_GUIDE_LOG, log);
    }

    public void roleMissonLog(RoleMissonLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // server
        buffer.append(log.getServer());
        buffer.append(CONNECTOR);

        // channel
        buffer.append(log.getChannel());
        buffer.append(CONNECTOR);

        // missionId
        buffer.append(log.getMissonId());

        LoggerFactory.getLogger(ROLE_MISSON_LOG).info(buffer.toString());
        sendLog(ROLE_MISSON_LOG, log);
    }

    public void roleTaskLog(RoleTaskLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // server
        buffer.append(log.getServer());
        buffer.append(CONNECTOR);

        // channel
        buffer.append(log.getChannel());
        buffer.append(CONNECTOR);

        // taskId
        buffer.append(log.getTaskId());

        LoggerFactory.getLogger(ROLE_TASK_LOG).info(buffer.toString());
        sendLog(ROLE_TASK_LOG, log);
    }

    /**
     * 资源产出与消耗日志
     *
     * @param player
     * @param log
     * @param resType
     */
    public void resourceLog(Player player, RoleResourceChangeLog log, int resType) {
        switch (resType) {
            case ResourceType.IRON:
                log.setCount(player.getIron());
                LoggerFactory.getLogger(ROLE_IRON_LOG).info(changeResouce(log).toString());
                sendLog(ROLE_IRON_LOG, log);
                break;
            case ResourceType.COPPER:
                log.setCount(player.getCopper());
                LoggerFactory.getLogger(ROLE_COPPER_LOG).info(changeResouce(log).toString());
                sendLog(ROLE_COPPER_LOG, log);
                break;
            case ResourceType.OIL:
                log.setCount(player.getOil());
                LoggerFactory.getLogger(ROLE_OIL_LOG).info(changeResouce(log).toString());
                sendLog(ROLE_OIL_LOG, log);
                break;
            case ResourceType.STONE:
                log.setCount(player.getStone());
                LoggerFactory.getLogger(ROLE_STONE_LOG).info(changeResouce(log).toString());
                sendLog(ROLE_STONE_LOG, log);
                break;
            default:
                break;
        }
    }

    /**
     * 钻石产出与消耗日志
     *
     * @param log
     * @return
     */
    public void resourceLog(Player player, RoleResourceChangeLog log) {
        log.setCount(player.getGold());
        LoggerFactory.getLogger(ROLE_GOLD_LOG).info(changeResouce(log).toString());
        sendLog(ROLE_GOLD_LOG, log);
    }

    public StringBuffer changeResouce(RoleResourceChangeLog log) {
        StringBuffer buffer = new StringBuffer();

        // 角色ID
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // 角色名
        buffer.append(log.getNick());
        buffer.append(CONNECTOR);

        // 角色等级
        buffer.append(log.getLevel());
        buffer.append(CONNECTOR);

        // 角色军衔
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // 角色军功
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // 当前资源数量
        buffer.append(log.getCount());
        buffer.append(CONNECTOR);

        // 资源变化类型
        buffer.append(log.getChangeType());
        buffer.append(CONNECTOR);

        // 资源数量
        buffer.append(log.getChangeCount());
        buffer.append(CONNECTOR);

        // 产出类型
        buffer.append(log.getType());
        buffer.append(CONNECTOR);

        // 角色所属阵营
        buffer.append(log.getCountry());
        buffer.append(CONNECTOR);

        // 角色VIP等级
        buffer.append(log.getVip());
        buffer.append(CONNECTOR);

        // 角色创建时间
        buffer.append(log.getChannel());

        return buffer;
    }

    /**
     * 聊天日志
     *
     * @param chatLog
     */
    public void chatLog(ChatLog chatLog) {
        StringBuffer buffer = new StringBuffer();

        // 服务器ID
        buffer.append(chatLog.getSerId());
        buffer.append(CONNECTOR);

        // 聊天类型
        buffer.append(chatLog.getType());
        buffer.append(CONNECTOR);

        // 该角色userId
        buffer.append(chatLog.getRoleId());
        buffer.append(CONNECTOR);

        // 角色名
        buffer.append(StringUtil.urlEncode(chatLog.getNick()));
        buffer.append(CONNECTOR);

        // 角色等级
        buffer.append(chatLog.getLevel());
        buffer.append(CONNECTOR);

        // 角色VIP等级
        buffer.append(chatLog.getVip());
        buffer.append(CONNECTOR);

        // 信息内容
        buffer.append(chatLog.getMsg().replaceAll(",", ";"));
        buffer.append(CONNECTOR);

        // 是否是内部号
        buffer.append(chatLog.getIsGm());
        buffer.append(CONNECTOR);

        // 渠道
        buffer.append(chatLog.getChannel());
        buffer.append(CONNECTOR);

        // key
        buffer.append(chatLog.getAccountKey());
        buffer.append(CONNECTOR);

        // 阵营
        buffer.append(chatLog.getCamp());

        LoggerFactory.getLogger(CHAT_LOG).info(buffer.toString());
        sendLog(CHAT_LOG, chatLog);
    }

    /**
     * 装备分解日志
     *
     * @param log
     */
    public void equipDecompoundLog(EquipDecompoundLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // role_name
        buffer.append(log.getRoleName());
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRoleLv());
        buffer.append(CONNECTOR);

        // `viplv` int(11) DEFAULT NULL COMMENT 'vip等级',
        buffer.append(log.getVip());
        buffer.append(CONNECTOR);

        // `title` int(11) DEFAULT NULL, // 角色军衔：玩家角色军衔
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // 角色所属阵营：该角色所属阵营countryId
        buffer.append(log.getCountry());
        buffer.append(CONNECTOR);

        // 装备key
        buffer.append(log.getKeyId());
        buffer.append(CONNECTOR);

        // 装备ID
        buffer.append(log.getEquipId());
        buffer.append(CONNECTOR);

        // 装备品质
        buffer.append(log.getQuality());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);
        buffer.append(log.getChannel());

        buffer.append(CONNECTOR);
        buffer.append(log.isDecompose() ? 0 : 1);
        buffer.append(CONNECTOR);
        buffer.append(log.getReason());

        LoggerFactory.getLogger(EQUIP_DECOMPOUND_LOG).info(buffer.toString());
        sendLog(EQUIP_DECOMPOUND_LOG, log);
    }

    /**
     * 神奇升级日志
     *
     * @param log
     */
    public void killEquipLog(KillEquipLog log) {
        StringBuffer buffer = new StringBuffer();

        // role_id
        buffer.append(log.getRoleId());
        buffer.append(CONNECTOR);

        // role_name
        buffer.append(log.getRoleName());
        buffer.append(CONNECTOR);

        // `rolelv` int(11) DEFAULT NULL, 角色等级
        buffer.append(log.getRoleLv());
        buffer.append(CONNECTOR);

        // `viplv` int(11) DEFAULT NULL COMMENT 'vip等级',
        buffer.append(log.getVip());
        buffer.append(CONNECTOR);

        // `title` int(11) DEFAULT NULL, // 角色军衔：玩家角色军衔
        buffer.append(log.getTitle());
        buffer.append(CONNECTOR);

        // 角色所属阵营：该角色所属阵营countryId
        buffer.append(log.getCountry());
        buffer.append(CONNECTOR);

        // 装备ID
        buffer.append(log.getEquipId());
        buffer.append(CONNECTOR);

        // 装备等级
        buffer.append(log.getEquipLevel());
        buffer.append(CONNECTOR);

        // 晶体总量
        buffer.append(log.getStone());
        buffer.append(CONNECTOR);

        // 消耗量
        buffer.append(log.getCost());
        buffer.append(CONNECTOR);

        // `role_create_time` datetime DEFAULT NULL,
        buffer.append(DateHelper.formatDateTime(log.getRoleCreateTime(), DateHelper.format1));
        buffer.append(CONNECTOR);

        buffer.append(log.getChannel());

        LoggerFactory.getLogger(KILL_EQUIP_LOG).info(buffer.toString());
        sendLog(KILL_EQUIP_LOG, log);
    }

    public void roleItemLog(RoleItemLog log) {
        StringBuilder builder = new StringBuilder();
        builder.append(DateHelper.formatDateTime(log.getCreateDate(), DateHelper.format1)); //
        builder.append(CONNECTOR);

        builder.append(log.getRoleId());
        builder.append(CONNECTOR);

        builder.append(log.getItemId());
        builder.append(CONNECTOR);

        builder.append(log.getState());
        builder.append(CONNECTOR);

        builder.append(log.getItem_count());
        builder.append(CONNECTOR);

        builder.append(log.getItem_resource());

        LoggerFactory.getLogger(ROLE_ITEM_LOG).info(builder.toString());
        // 道具记录
        sendLog(ROLE_ITEM_LOG, log);
    }

    public void beautyItemLog(BeautyItemLog log) {
        StringBuilder builder = new StringBuilder();

        builder.append(log.getRoleId());
        builder.append(CONNECTOR);

        builder.append(log.getType());
        builder.append(CONNECTOR);

        builder.append(log.getBeautyId());
        builder.append(CONNECTOR);

        builder.append(log.getChangeNumber());
        builder.append(CONNECTOR);

        builder.append(log.getItemId());
        builder.append(CONNECTOR);

        builder.append(log.getItemAdd());
        builder.append(CONNECTOR);

        builder.append(log.getItemSource());
        builder.append(CONNECTOR);

        builder.append(log.getState());

        LoggerFactory.getLogger(BEAUTY_ITEM_LOG).info(builder.toString());

        sendLog(BEAUTY_ITEM_LOG, log);
    }


    public void loginLog(LoginLog log) {
        LoggerFactory.getLogger(LogTable.login_log.name()).info(log.toString());
        sendLog(LogTable.login_log.table(), log);
    }

    public void energyLog(EnergyLog log) {
        LoggerFactory.getLogger(LogTable.energy_log.name()).info(log.toString());
        sendLog(LogTable.energy_log.table(), log);
    }

    public void hatchery_log(HatcheryLog log) {
        LoggerFactory.getLogger(LogTable.hatchery_log.name()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    public void battle_log(BattleLog log) {
        LoggerFactory.getLogger(LogTable.battle_log.name()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    public void activity_log(ActivityLog log) {
        LoggerFactory.getLogger(LogTable.activity_log.name()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }
    public void activity_log(CampMembersRankLog log) {
        LoggerFactory.getLogger(LogTable.activity_log.name()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    public void act_hope_log(ActHopeLog log) {
        LoggerFactory.getLogger(LogTable.act_hope_log.name()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    public void world_box_log(WorldBoxLog log) {
        LoggerFactory.getLogger(LogTable.world_box_log.name()).info(log.toString());
//        sendLog(LogTable.world_box_log.table(), log);
    }

    public void mail_log(MailLog log) {
        LoggerFactory.getLogger(LogTable.mail_log.name()).info(log.toString());
//        sendLog(LogTable.world_box_log.table(), log);
    }

    public void seek_log(SeekLog log) {
        LoggerFactory.getLogger(LogTable.seek_log.name()).info(log.toString());
//        sendLog(LogTable.world_box_log.table(), log);
    }

    /**
     * @Description 个性签名
     * @Date 2021/1/27 15:14
     * @Param [log]
     * @Return
     **/
    public void personalSignatureLog(PersonalSignatureLog log) {
        LoggerFactory.getLogger(LogTable.personal_signature_log.name()).info(log.toString());
        //sendLog(LogTable.personal_signature_log.table(), log);
    }

    public void war_book_log(WarBookLog log) {
        LoggerFactory.getLogger(LogTable.war_book_log.table()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    public void wear_book_log(WearBookLog log) {
        LoggerFactory.getLogger(LogTable.wear_book_log.table()).info(log.toString());
//        sendLog(LogTable.hatchery_log.table(), log);
    }

    /**
     * 合服前活动记录
     * @param log
     */
    public void recordReissueAwardsLog(RecordReissueAwards log) {
        LoggerFactory.getLogger(LogTable.record_reissue_awards_log.name()).info(log.toString());
//        sendLog(LogTable.record_reissue_awards_log.table(), log);
    }
    
    /**
    * @Description 母巢之战增益购买
    * @Param [log]  
    * @Return void
    * @Date 2021/9/18 14:53
    **/
    public void broodWarBuyBuffLog(NewBroodWarBuyBuffLog log) {
        LoggerFactory.getLogger(LogTable.broodWar_buyBuff_log.name()).info(log.toString());
    }

    /**
    * @Description 母巢之战实体战斗次数
    * @Param [log]  
    * @Return void
    * @Date 2021/9/18 14:55
    **/
    public void broodWarEntityBattleCountLog(NewBroodWarBattleLog log) {
        LoggerFactory.getLogger(LogTable.broodWar_entity_battle_count_log.name()).info(log.toString());
    }

    /**
    * @Description 体力补领功能的埋点
    * @Param [log]
    * @Return void
    * @Date 2021/11/23 14:17
    **/
    public void getActPowerRqLog(GetActPowerLog log) {
        LoggerFactory.getLogger(LogTable.get_act_power_rq_log.name()).info(log.toString());
    }

    /**
    * @Description 无尽塔防埋点
    * @Param [log]
    * @Return void
    * @Date 2021/12/27 9:41
    **/
    public void getEndlessTDLog(EndlessTDLog log) {
        LoggerFactory.getLogger(LogTable.endless_td_log.name()).info(log.toString());
    }
    /**
     * 无尽塔防异常数据记录
     **/
    public void endlessTDErrorLog(EndlessTDErrorLog log) {
        LoggerFactory.getLogger(LogTable.endless_td_error_log.name()).info(log.toString());
    }


    /**
    * @Description 材料置换
    * @Param [log]
    * @Return void
    * @Date 2021/12/28 10:04
    **/
    public static void getMaterialSubstitutionLog(ActMaterialSubstitutionLog log) {
        LoggerFactory.getLogger(LogTable.material_substitution_log.name()).info(log.toString());
    }

    /**
     * 勋章获得log
     * @param log
     */
    public void omamentLog(OmamentLog log) {
        LoggerFactory.getLogger(LogTable.omament_log.name()).info(log.toString());
    }

    /**
     * 沙盘演武日志
     * @param log
     */
    public void manoeuvre_log(ManoeuvreLog log) {
        LoggerFactory.getLogger(LogTable.manoeuvre_log.name()).info(log.toString());
    }

    /**
     * kafka报送日志接口
     *
     * @param table
     * @param log
     */
    public void sendLog(String table, Object log) {
    }
}
