package com.game.worldmap;

import com.game.domain.Player;
import com.game.pb.CommonPb;
import com.game.util.BasePbHelper;

import com.game.util.PbHelper;

import java.util.*;

/**
 *
 */
public class CityFirstBloodInfo {

    /**城市类型*/
    private int cityType;
    /**战争发起者的信息*/
    private FirstBloodInfo attackerInfo = new FirstBloodInfo();
    /**发起方*/
    private List<FirstBloodInfo> helperInfo = new ArrayList<>();

    public FirstBloodInfo getAttackerInfo() {
        return attackerInfo;
    }

    public void setAttackerInfo(FirstBloodInfo attackerInfo) {
        this.attackerInfo = attackerInfo;
    }

    public List<FirstBloodInfo> getHelperInfo() {
        return helperInfo;
    }

    public void setHelperInfo(List<FirstBloodInfo> helperInfo) {
        this.helperInfo = helperInfo;
    }

    public int getCityType() {
        return cityType;
    }

    public void setCityType(int cityType) {
        this.cityType = cityType;
    }

    public CityFirstBloodInfo() {
    }

    public CityFirstBloodInfo(Integer cityType, Player attacker, List<Player> attackerList, Integer mapId) {
        Set<Long> set = new HashSet<>();
        set.add(attacker.getLord().getLordId());
        setCityType(cityType);
        attackerInfo = createFirstBloodInfo(mapId, attacker);
        for (Player player : attackerList){
            if (set.contains(player.getLord().getLordId())){
                continue;
            }
            set.add(player.getLord().getLordId());
            helperInfo.add(createFirstBloodInfo(mapId, player));
        }
        set.clear();
    }

    public FirstBloodInfo createFirstBloodInfo(Integer mapId, Player player){
        FirstBloodInfo firstBloodInfo = new FirstBloodInfo();
        firstBloodInfo.setMapId(mapId);
        firstBloodInfo.setNick(player.getNick());
        firstBloodInfo.setCountry(player.getCountry());
        firstBloodInfo.setPortrait(player.getPortrait());
        return firstBloodInfo;
    }


    public CommonPb.FirstBloodMapInfo.Builder writeData() {
        CommonPb.FirstBloodMapInfo.Builder builder = CommonPb.FirstBloodMapInfo.newBuilder();
        if (getAttackerInfo() != null) {
            CommonPb.FirstBloodInfo attackerInfo = PbHelper.createFirstBloodInfo(getAttackerInfo());
            List<CommonPb.FirstBloodInfo> helperInfo = new ArrayList<>();
            if (getHelperInfo() != null && getHelperInfo().size() != 0){
                for (FirstBloodInfo i: getHelperInfo()){
                    helperInfo.add(PbHelper.createFirstBloodInfo(i));
                }
            }
            builder.setCityType(getCityType())
                    .setAttackerInfo(attackerInfo)
                    .addAllHelperInfo(helperInfo);
        }
        return builder;
    }

    public void readData(CommonPb.FirstBloodMapInfo data) {
        if (data != null&& data.getAttackerInfo() != null){
            setCityType(data.getCityType());
            setAttackerInfo(createFirstBloodInfo(data.getAttackerInfo()));
            Set<String> set = new HashSet<>();
            set.add(data.getAttackerInfo().getNick());
            if (data.getHelperInfoList()!=null&&data.getHelperInfoList().size()!=0){
                for (CommonPb.FirstBloodInfo info : data.getHelperInfoList()) {
                    if (set.contains(info.getNick())){
                        continue;
                    }
                    set.add(info.getNick());
                    getHelperInfo().add(createFirstBloodInfo(info));
                }
            }
            set.clear();
        }
    }

    FirstBloodInfo createFirstBloodInfo(CommonPb.FirstBloodInfo firstBloodInfo){
        FirstBloodInfo info = new FirstBloodInfo();
        info.setMapId(firstBloodInfo.getMapId());
        info.setNick(firstBloodInfo.getNick());
        info.setCountry(firstBloodInfo.getCountry());
        info.setPortrait(firstBloodInfo.getPortrait());
        return info;
    }
}
