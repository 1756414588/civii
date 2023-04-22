package com.game.uc.service;

import com.alibaba.fastjson.JSONObject;
import com.game.constant.CdkConsts;
import com.game.constant.UcCodeEnum;
import com.game.uc.Cdkey;
import com.game.uc.CdkeyItem;
import com.game.uc.CdkeyUniversal;
import com.game.uc.Message;

import com.game.uc.dao.ifs.p.CdkeyDao;
import com.game.uc.dao.ifs.p.CdkeyUniversalDao;
import com.game.uc.manager.CdkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author jyb
 * @date 2020/6/1 17:44
 * @description
 */
@Service
public class CdkService {
    @Autowired
    private CdkManager cdkManager;

    @Autowired
    private CdkeyUniversalDao cdKeyUniversalDao;

    @Autowired
    private CdkeyDao cdKeyDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public Message getCdkAward(long roleId, int serverId, String cdk, int channel, int level) {
        try {
            cdkManager.getLock().lock();
            Cdkey cdKey = cdkManager.getCdk(cdk);
            if (cdKey == null) {
                return new Message(UcCodeEnum.CDK_NOT_EXIST);
            }
            if (cdKey.getChannel() != 0 && cdKey.getChannel() != channel) {
                return new Message(UcCodeEnum.CDK_AWARD_NOT_EXIST);
            }
            if (cdKey.getAreaid() != 0 && cdKey.getAreaid() != serverId) {
                return new Message(UcCodeEnum.CDK_IS_OVER_TIME);
            }
            Date now = new Date();
            if (now.before(cdKey.getStarttime()) || now.after(cdKey.getEndtime())) {
                return new Message(UcCodeEnum.CDK_SERVER_ERROR);
            }
            if (!canGetCdk(roleId, cdKey)) {
                return new Message(UcCodeEnum.CDK_IS_USE);
            }
            List<CdkeyItem> cdKeyItems = cdkManager.getCdKeyItems(cdKey.getRewardobjectid());
            if (cdKeyItems == null || cdKeyItems.size() < 1) {
                return new Message(UcCodeEnum.CDK_AWARD_NOT_EXIST);
            }
            if (cdKey.getUse_level() != 0 && level < cdKey.getUse_level()) {
                String data = String.valueOf(cdKey.getUse_level());
                return new Message(UcCodeEnum.CDK_LEVEL_NOT_ENOUTH, data);
            }
            logger.info("CdkService  cdk cdKeyItems {} ", JSONObject.toJSONString(cdKeyItems));
            String data = JSONObject.toJSONString(cdKeyItems);
            CdkeyUniversal cdKeyUniversal = new CdkeyUniversal();
            cdKeyUniversal.setRoleid(roleId);
            cdKeyUniversal.setRewardobjectid(cdKey.getRewardobjectid());
            cdKeyUniversal.setAreaid(serverId);
            cdKeyUniversal.setKeychar(cdKey.getKeychar());
            cdKeyUniversal.setChannel(cdKey.getChannel());
            cdKeyUniversalDao.insert(cdKeyUniversal);
            cdKey.setIsuse(1);
            cdKey.setRoleid(roleId);
            cdKeyDao.updateByPrimaryKey(cdKey);
            logger.info("CdkService  cdk data {} ", data);
            return new Message(UcCodeEnum.SUCCESS, data);
        } finally {
            cdkManager.getLock().unlock();
        }

    }

    /**
     * 同一个激活码类型每个角色只能使用一个
     *
     * @param roleId
     * @param cdKey
     * @return
     */
    private boolean canGetCdk(long roleId, Cdkey cdKey) {
        //检查有没没有用过同类型的
        if (cdKey.getUniversal().intValue() == CdkConsts.CDK_TYPE_1
                || cdKey.getUniversal().intValue() == CdkConsts.CDK_TYPE_0) {
            //检查自己有没有用过同类型的
            List<CdkeyUniversal> cdKeyUniversals = cdKeyUniversalDao.selectByRoleId(roleId);
            List<Cdkey> cdKeys = cdkManager.getCdKeys(cdKey.getKeytype());
            if (cdKeyUniversals != null && cdKeyUniversals.size() > 0) {
                for (Cdkey key : cdKeys) {
                    for (CdkeyUniversal cdKeyUniversal : cdKeyUniversals) {
                        if (key.getKeychar().equals(cdKeyUniversal.getKeychar())) {
                            return false;
                        }
                    }
                }
            }
        }
        if (cdKey.getUniversal().intValue() == CdkConsts.CDK_TYPE_0
                || cdKey.getUniversal().intValue() == CdkConsts.CDK_TYPE_2) {
            //检查有没有被别人用
            List<CdkeyUniversal> useCdks = cdKeyUniversalDao.selectByCdk(cdKey.getKeychar());
            if (useCdks.size() > 0) {
                return false;
            }
        }
        return true;
    }
}
