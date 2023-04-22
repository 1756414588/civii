package com.game.uc.dao.handle;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MapMapIntTypeHandler extends BaseTypeHandler<Map<Integer, Map<Integer, Integer>>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Map<Integer, Map<Integer, Integer>> parameter, JdbcType jdbcType) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // TODO Auto-generated method stub
        String columnValue = rs.getString(columnName);
        return getmapMap(columnValue);
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }


    private Map<Integer, Map<Integer, Integer>> getmapMap(String columnValue) {
        Map<Integer, Map<Integer, Integer>> mapMap = new HashMap<Integer, Map<Integer, Integer>>();
        if (columnValue == null || columnValue.isEmpty()) {
            return mapMap;
        }

        JSONArray arrays = JSONArray.parseArray(columnValue);
        for (int i = 0; i < arrays.size(); i++) {
            JSONArray array = arrays.getJSONArray(i);
            //[1,1,1000]
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            if(array.size() >= 3) {
                map.put(array.getIntValue(1), array.getIntValue(2));
                mapMap.put(array.getInteger(0), map);
            }
        }

        return mapMap;
    }
}
