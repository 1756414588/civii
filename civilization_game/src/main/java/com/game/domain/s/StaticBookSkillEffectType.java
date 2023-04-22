package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2021/1/8 11:25
 */
public class StaticBookSkillEffectType {
    private int id;    //主键ID
    private String propType;//加成类型
    private List<Integer> param;//特殊参数

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPropType() {
        return propType;
    }

    public void setPropType(String propType) {
        this.propType = propType;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticBookSkillType{" +
               "id=" + id +
               ", propType='" + propType + '\'' +
               ", param=" + param +
               '}';
    }
}
