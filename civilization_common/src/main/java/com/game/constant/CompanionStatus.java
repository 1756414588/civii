package com.game.constant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 战友邀请状态
 * @ProjectName halo_server
 * @Date 2021/8/5 9:07
 **/
public enum CompanionStatus {
    alreadyJoin(0, "以加入"),
    alreadyInvitation(1, "以邀请"),
    canInvite(2, "可以邀请"),
    ;

    CompanionStatus(int key, String val) {
        this.key = key;
        this.val = val;
    }

    int key;
    String val;

    public int getKey() {
        return key;
    }

    public static List<Integer> getKeyList() {
        List<Integer> list = new ArrayList<>();
        for (CompanionStatus value : CompanionStatus.values()) {
            list.add(value.key);
        }
        return list.stream().sorted(Comparator.comparing(Integer::intValue)).collect(Collectors.toList());
    }
}
