package com.game.domain.p;

public class EquipSkill {
    private int skillId;                        //装备技能Id
    private int skillType;                      //技能类型
    private int skillNum;                       //技能值
    public EquipSkill() {
        skillId = 0;
        skillType = 0;
        skillNum = 0;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int equipSkillId) {
        this.skillId = equipSkillId;
    }


    public int getSkillType() {
        return skillType;
    }

    public void setSkillType(int skillType) {
        this.skillType = skillType;
    }

    public int getSkillNum() {
        return skillNum;
    }

    public void setSkillNum(int skillNum) {
        this.skillNum = skillNum;
    }

}
