package com.game.log.constant;

/**
 * @author cpz
 * @date 2020/9/10 19:58
 * @description
 */
public enum OperateType {
  ROBO(101, "城战抢夺"),
  ;

  OperateType(int value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  int value;
  String desc;

    public int getValue() {
        return value;
    }
}
