package com.game.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/8/20 20:56
 * @description
 */
public class MapListListTypeHandler extends BaseTypeHandler<Map<Integer, List<List<Integer>>>> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, Map<Integer, List<List<Integer>>> parameter, JdbcType jdbcType)
            throws SQLException {
    }

    @Override
    public Map<Integer, List<List<Integer>>> getNullableResult(ResultSet rs, String columnName) {
        String columnValue = "";
        try {

            columnValue = rs.getString(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.getMapList(columnValue);
    }

    @Override
    public Map<Integer, List<List<Integer>>> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return null;
    }

    @Override
    public Map<Integer, List<List<Integer>>> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return null;
    }

    private Map<Integer, List<List<Integer>>> getMapList(String columnValue) {
        Map<Integer, List<List<Integer>>> mapList = new HashMap<>();
        if (columnValue == null || columnValue.isEmpty()) {
            return mapList;
        }
        JSONObject object = JSONObject.parseObject(columnValue);
        object
                .keySet()
                .forEach(
                        key -> {
                            JSONArray array = object.getJSONArray(key);
                            List<List<Integer>> result = new ArrayList<>();
                            for (int i = 0; i < array.size(); i++) {
                                JSONArray tmp = array.getJSONArray(i);
                                List<Integer> list = new ArrayList<>();
                                for (int j = 0; j < tmp.size(); j++) {
                                    list.add(tmp.getIntValue(j));
                                }
                                result.add(list);
                            }
                            mapList.put(Integer.parseInt(key), result);
                        });
        return mapList;
    }

    public static void main(String[] args) {
        //
        String str = "{6:[[101,1]],26:[[101,1],[102,1]]}";
        JSONObject object = JSONObject.parseObject(str);
        object
                .keySet()
                .forEach(
                        key -> {
                            JSONArray array = object.getJSONArray(key);
                            List<List<Integer>> result = new ArrayList<>();
                            for (int i = 0; i < array.size(); i++) {
                                JSONArray tmp = array.getJSONArray(1);
                                List<Integer> list = new ArrayList<>();
                                for (int j = 0; i < tmp.size(); j++) {
                                    list.add(tmp.getIntValue(j));
                                }
                                result.add(list);
                            }
                        });
    }
}
