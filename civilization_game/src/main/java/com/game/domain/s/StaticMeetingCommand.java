package com.game.domain.s;

/**
 *
 * @date 2019/12/11 15:15
 * @description 指挥学属性配置
 */
public class StaticMeetingCommand {
    private int id ;
    private String name;
    private int level ;
    private int  effect;
    private int meetType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;

    }

    public int getMeetType() {
        return meetType;
    }

    public void setMeetType(int meetType) {
        this.meetType = meetType;
    }
}
