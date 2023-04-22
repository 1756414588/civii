package com.game.log.domain;

import com.game.constant.Reason;
import com.game.log.constant.RoleExpType;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 角色升级日志类
 *
 * @author CaoBing 2020年4月27日 RoleExpLog.java
 */
@Data
public class RoleExpLog {
    /**
     * 角色ID：玩家角色ID
     */
    private Long roleId;

    /**
     * 角色名：玩家角色名称
     */
    private String roleName;

    /**
     * 角色军衔
     */
    private int title;

    /**
     * 角色所属阵营：该角色所属阵营country
     */
    private int country;

    /**
     * 角色等级
     */
    private int rolelv;

    /**
     * 角色VIP等级：玩家角色VIP等级
     */
    private int vip;

    /**
     * 增加经验
     */
    private long increaseExp;

    /**
     * 当前经验值
     */
    private long exp;

    /**
     * 当前体力值
     */
    private int energy;

    /**
     * 获得经验的途径
     */
    private int expType;

    /**
     * 角色创建时间
     */
    private Date roleCreateTime;
    private int commandLevel;
    private int techLevel;

    private int channel;

    public RoleExpLog() {
    }

    @Builder
    public RoleExpLog(
            long roleId,
            Date roleCreateTime,
            int rolelv,
            int exp,
            int energy,
            int vip,
            int country,
            int title,
            String roleName,
            int reason,
            int increaseExp,
            int channel, int commandLevel, int techLevel) {
        this.roleId = roleId;
        this.roleCreateTime = roleCreateTime;
        this.rolelv = rolelv;
        this.exp = exp;
        this.energy = energy;
        this.increaseExp = increaseExp;
        this.vip = vip;
        this.country = country;
        this.title = title;
        this.roleName = roleName;
        this.channel = channel;
        this.commandLevel = commandLevel;
        this.techLevel = techLevel;

        switch (reason) {
            case Reason.TASK_AWARD:
                this.expType = RoleExpType.TASK.getCode();
                break;

            case Reason.MISSION_DONE:
                this.expType = RoleExpType.MISSION.getCode();
                break;
        /*case Reason.START_MISSION_WIN:
        this.expType = ExpType.STAR_MISSION;
        break;*/
            case Reason.MAIL_AWARD:
                this.expType = RoleExpType.MAIL.getCode();
                break;
            case Reason.GM_ADD_GOODS:
                this.expType = RoleExpType.GM.getCode();
                break;
            case Reason.LEVEL_UP_BUILDING:
                this.expType = RoleExpType.REBUILD.getCode();
                break;
            case Reason.ACT_AWARD:
                this.expType = RoleExpType.ACT.getCode();
                break;
            case Reason.CREATE_ACCOUNT:
                this.expType = RoleExpType.CREATE.getCode();
                break;
        }
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public int getRolelv() {
        return rolelv;
    }

    public void setRolelv(int rolelv) {
        this.rolelv = rolelv;
    }

    public long getIncreaseExp() {
        return increaseExp;
    }

    public void setIncreaseExp(long increaseExp) {
        this.increaseExp = increaseExp;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getExpType() {
        return expType;
    }

    public void setExpType(int expType) {
        this.expType = expType;
    }

    public Date getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(Date roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getCountry() {
        return country;
    }

    public void setCountry(int country) {
        this.country = country;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(roleId).append(",");
        // 角色名称
        buffer.append(roleName).append(",");
        // `rolelv` int(11) DEFAULT NULL,
        buffer.append(rolelv).append(",");
        // 角色的军衔
        buffer.append(title).append(",");
        // 角色的阵营
        buffer.append(country).append(",");
        // 角色VIP等级：玩家角色VIP等级
        buffer.append(vip).append(",");
        // `energy` int(11) DEFAULT NULL ,角色当前体力值：玩家角色当前拥有的体力值
        buffer.append(energy).append(",");
        // `increaseExp` int(11) DEFAULT NULL, 角色增加经验
        buffer.append(increaseExp).append(",");
        // `exp` int(11) DEFAULT NULL, 角色经验
        buffer.append(exp).append(",");
        // `exp_type` int(11) DEFAULT NULL, 经验值类型(1.副本 2.三星副本 3.任务 4.建筑 5.活动 6.邮件
        // 7.GM命令)
        buffer.append(expType).append(",");
        buffer.append(channel).append(",");
        buffer.append(commandLevel).append(",");
        buffer.append(techLevel);
        return buffer.toString();
    }
}
