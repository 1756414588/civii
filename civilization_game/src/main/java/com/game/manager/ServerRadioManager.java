package com.game.manager;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.game.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.game.dao.p.ServerRadioDao;
import com.game.servlet.domain.SendMail;
import com.game.servlet.domain.ServerRadio;

/**
 * 2020年6月29日
 *
 *    halo_game
 * ServerRadioManager.java
 **/
@Component
public class ServerRadioManager {
    @Autowired
    private ServerRadioDao serverRadioDao;

    private ConcurrentHashMap<Long, ServerRadio> serverRadioMap = new ConcurrentHashMap<Long, ServerRadio>();

    //最近一次更新时间
    public long lastSaveTime = System.currentTimeMillis();

//    @PostConstruct
//    public void addAllServerRadio() {
//        List<ServerRadio> selectAllServerRadio = serverRadioDao.selectAllServerRadio();
//        if (!CollectionUtils.isEmpty(selectAllServerRadio)) {
//            for (ServerRadio serverRadio : selectAllServerRadio) {
//                serverRadio.setLastSendTime(System.currentTimeMillis());
//                if (!StringUtil.isNullOrEmpty(serverRadio.getChannel())) {
//                    //serverRadio.setChannelList(StringUtil.stringToList(serverRadio.getChannel()));
//                }
//                serverRadioMap.put(serverRadio.getKeyId(), serverRadio);
//            }
//        }
//    }

    public ConcurrentHashMap<Long, ServerRadio> selectServerRadio() {
        return serverRadioMap;
    }

    public Long addServerRadio(ServerRadio serverRadio) {
        serverRadio.setCreateTime(new Date());
        serverRadio.setUpdateTime(new Date());
        if (serverRadioDao.insertSelective(serverRadio) > 0) {
            serverRadioMap.put(serverRadio.getKeyId(), serverRadio);
        }
        return serverRadio.getKeyId();
    }

    public void updateServerRadio(ServerRadio serverRadio) {
        serverRadio.setUpdateTime(new Date());
        serverRadioDao.updateByPrimaryKeySelective(serverRadio);
    }

    public ServerRadio findServerRadio(Long keyId) {
        return serverRadioMap.get(keyId);
    }

    public void deleteServerRadio(Long keyId) {
        ServerRadio serverRadio = serverRadioMap.get(keyId);
        if (null != serverRadio) {
            serverRadio.setRemove(SendMail.HAVE_REMOVE);
            serverRadio.setDeleteTime(new Date());
        }
    }
}
