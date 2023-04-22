package com.game.domain.s;

import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/16 10:07
 **/
public class StaticEndlessTDDropLimit {
	private int id;
	private String desc;
	private int limit;
	private List<Integer> param;
	private List<List<Integer>> param2;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    public List<List<Integer>> getParam2() {
        return param2;
    }

    public void setParam2(List<List<Integer>> param2) {
        this.param2 = param2;
    }

    @Override
    public String toString() {
        return "StaticEndlessTDDropLimit{" +
            "id=" + id +
            ", desc='" + desc + '\'' +
            ", limit=" + limit +
            ", param=" + param +
            ", param2=" + param2 +
            '}';
    }
}
