package com.game.domain.p;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/3/11 17:15
 * 诵我真名者,永不见bug
 * 记录周卡过期时间
 */
@Getter
@Setter
public class WeekCard implements Cloneable {
    long iornExpireTime;
    long copperExpireTime;
    long oilExpireTime;
    long stoneExpireTime;

    @Builder
    public WeekCard(long iornExpireTime, long copperExpireTime, long oilExpireTime, long stoneExpireTime) {
        this.iornExpireTime = iornExpireTime;
        this.copperExpireTime = copperExpireTime;
        this.oilExpireTime = oilExpireTime;
        this.stoneExpireTime = stoneExpireTime;
    }

    @Override
    public WeekCard clone() {
        WeekCard weekCard = null;
        try {
            weekCard = (WeekCard) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return weekCard;
    }

    public WeekCard() {
    }

    public long getExpireTime(int awardId) {
        switch (awardId) {
            case 1:
                return iornExpireTime;
            case 2:
                return copperExpireTime;
            case 3:
                return oilExpireTime;
            case 4:
                return stoneExpireTime;
            default:
                return 0;
        }
    }

    public void setExpireTime(int awardId, long time) {
        switch (awardId) {
            case 1:
                this.iornExpireTime = time;
                break;
            case 2:
                this.copperExpireTime = time;
                break;
            case 3:
                this.oilExpireTime = time;
                break;
            case 4:
                this.stoneExpireTime = time;
                break;
            default:
                break;
        }
    }

    public String toData() {
        String json = JSON.toJSONString(this);
        return json;
    }

    public void serData(String value) {
        WeekCard weekCard = JSON.parseObject(value, WeekCard.class);
        this.iornExpireTime = weekCard.getIornExpireTime();
        this.copperExpireTime = weekCard.getCopperExpireTime();
        this.oilExpireTime = weekCard.getOilExpireTime();
        this.stoneExpireTime = weekCard.getStoneExpireTime();
    }
}
