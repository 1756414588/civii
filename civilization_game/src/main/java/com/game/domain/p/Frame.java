package com.game.domain.p;

import com.game.enumerate.FrameState;
import com.game.util.TimeHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 *
 * @date 2021/1/23 17:42
 * @description
 */
@Getter
@Setter
public class Frame implements Cloneable {
    private int id; //头像ID
    private int index;  //头像框0 聊天框 1
    private FrameState state;//是否解锁
    private Date expireTime;    //过期时间
    private boolean show = false;   //是否查看过

    public Frame() {

    }

    @Builder
    public Frame(int id, int index, FrameState state, Date expireTime) {
        this.id = id;
        this.index = index;
        this.state = state;
        this.expireTime = expireTime;
        this.show = false;
    }

    public int expireTime() {
        if (expireTime != null) {
            return (int) (expireTime.getTime() / 1000);
        }
        return 0;
    }

    public void expireTime(long time) {
        if (time == 0) {
            return;
        }
        if (expireTime != null) {   //增加过期时间
            expireTime = new Date(expireTime.getTime() + time);
        } else {
            expireTime = new Date(System.currentTimeMillis() + time);
        }
    }

    @Override
    public Frame clone() {
        Frame frame = null;
        try {
            frame = (Frame) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return frame;
    }
}
