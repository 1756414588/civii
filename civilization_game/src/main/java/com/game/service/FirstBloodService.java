package com.game.service;

import com.game.constant.CityType;
import com.game.constant.GameError;
import com.game.constant.WorldAreaType;
import com.game.dataMgr.StaticFirstBloodMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.s.StaticFirstBloodAward;
import com.game.domain.s.StaticWorldMap;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.Handler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.FirstBloodMapInfo;
import com.game.pb.FirstBloodPb.AllFirstBloodRs;
import com.game.pb.FirstBloodPb.CityFirstBloodRq;
import com.game.pb.FirstBloodPb.CityFirstBloodRs;
import com.game.util.PbHelper;
import com.game.worldmap.CityFirstBloodInfo;
import com.game.worldmap.MapInfo;
import com.game.worldmap.FirstBloodInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * 城市首杀
 * @author liyue
 * @date 20201010
 */
@Service
public class FirstBloodService {

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticFirstBloodMgr staticFirstBloodMgr;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    public void getFirstBloodInfo(CityFirstBloodRq req, ClientHandler handler){
        int mapId = req.getMapId();
        int page = req.getPage();
        int cityType = req.getCityType();
        StaticWorldMap map = staticWorldMgr.getStaticWorldMap(mapId);
        if (map == null) {
            handler.sendErrorMsgToPlayer(GameError.MAP_ID_ERROR);
            return;
        }

        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        MapInfo mapInfo = worldMapInfo.get(req.getMapId());
        CityFirstBloodRs.Builder builder = CityFirstBloodRs.newBuilder();
        Map<Integer, StaticFirstBloodAward> staticFirstBloodAwardMap = staticFirstBloodMgr.getStaticFirstBloodAwardMap();
        if (null == staticFirstBloodAwardMap || staticFirstBloodAwardMap.size() == 0) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        StaticFirstBloodAward values = staticFirstBloodAwardMap.get(cityType);
        if (values == null) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        if (map.getAreaType() != values.getAreaType()) {
            handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
            return;
        }
        FirstBloodMapInfo.Builder mapInfoPb = FirstBloodMapInfo.newBuilder();
        mapInfoPb.setCityType(cityType);
        for (List<Integer> l : values.getAward()) {
            if (l.size() != 3) {
                continue;
            }
            mapInfoPb.addAward(PbHelper.createAward(l.get(0), l.get(1), l.get(2)));
        }
        CityFirstBloodInfo info = mapInfo.getCityFirstBlood().get(cityType);
        if (info != null && info.getAttackerInfo() != null) {
            CommonPb.FirstBloodInfo attackerInfo = PbHelper.createFirstBloodInfo(info.getAttackerInfo());
            Map<Integer, List<CommonPb.FirstBloodInfo>> pageMap = new HashMap<>();
            List<CommonPb.FirstBloodInfo> helperInfo = new ArrayList<>();
            if (info.getHelperInfo() != null && info.getHelperInfo().size() != 0) {
                int pageCount = 1;
                for (FirstBloodInfo i : info.getHelperInfo()) {
                    helperInfo.add(PbHelper.createFirstBloodInfo(i));
                    if (helperInfo.size() >= 20) {
                        List<CommonPb.FirstBloodInfo> newInfo = new ArrayList<>();
                        newInfo.addAll(helperInfo);
                        pageMap.put(pageCount, newInfo);
                        helperInfo.clear();
                        pageCount++;
                    }
                }
                pageMap.put(pageCount, helperInfo);
            }
            mapInfoPb.setAttackerInfo(attackerInfo);
            mapInfoPb.setCount(pageMap.size());
            if (pageMap.get(page) != null) {
                mapInfoPb.addAllHelperInfo(pageMap.get(page));
            }
        }
        builder.setMapInfo(mapInfoPb);

        handler.sendMsgToPlayer(CityFirstBloodRs.ext, builder.build());
    }

    public void getAllFirstBloodInfo(ClientHandler handler){
        ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
        Iterator<MapInfo> mapInfoIterator = worldMapInfo.values().iterator();
        AllFirstBloodRs.Builder builder = AllFirstBloodRs.newBuilder();
        while (mapInfoIterator.hasNext()) {
            MapInfo mapInfo = mapInfoIterator.next();
            if (mapInfo == null || mapInfo.getCityFirstBlood() == null) {
                continue;
            }
            if(mapInfo.getCityFirstBlood().containsKey(CityType.WALL)){
                CityFirstBloodInfo info = mapInfo.getCityFirstBlood().get(CityType.WALL);
                CommonPb.FirstBloodInfo attackerInfo = PbHelper.createFirstBloodInfo(info.getAttackerInfo());
                builder.addAllInfo(attackerInfo);
            }
            if(mapInfo.getCityFirstBlood().containsKey(CityType.CAPITAL)){
                CityFirstBloodInfo info = mapInfo.getCityFirstBlood().get(CityType.CAPITAL);
                CommonPb.FirstBloodInfo attackerInfo = PbHelper.createFirstBloodInfo(info.getAttackerInfo());
                builder.addAllInfo(attackerInfo);
            }
            if(mapInfo.getCityFirstBlood().containsKey(CityType.WORLD_FORTRESS)){
                CityFirstBloodInfo info = mapInfo.getCityFirstBlood().get(CityType.WORLD_FORTRESS);
                CommonPb.FirstBloodInfo attackerInfo = PbHelper.createFirstBloodInfo(info.getAttackerInfo());
                builder.addAllInfo(attackerInfo);
            }
        }
        handler.sendMsgToPlayer(AllFirstBloodRs.ext, builder.build());
    }

}
