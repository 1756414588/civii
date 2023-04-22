package com.game.uc.dao.handle;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.HashBasedTable;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableTypeHandler extends BaseTypeHandler<HashBasedTable<Integer, Integer, Integer>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, HashBasedTable<Integer, Integer, Integer> parameter, JdbcType jdbcType) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public HashBasedTable<Integer, Integer, Integer> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // TODO Auto-generated method stub
        String columnValue = rs.getString(columnName);
        return getTable(columnValue);
    }

    @Override
    public HashBasedTable<Integer, Integer, Integer> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashBasedTable<Integer, Integer, Integer> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    private HashBasedTable<Integer, Integer, Integer> getTable(String columnValue) {
        HashBasedTable<Integer, Integer, Integer> table = HashBasedTable.create();
        if (columnValue == null || columnValue.isEmpty()) {
            return table;
        }

        if (!columnValue.startsWith("[[")) {
            return table;
        }

        //[[1,1,3], [1,2,3], [1,3,4]
        JSONArray arrays = JSONArray.parseArray(columnValue);
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < arrays.size(); i++) {
            list.clear();
            JSONArray array = arrays.getJSONArray(i);
            for (int j = 0; j < array.size(); j++) {
                list.add(array.getInteger(j));
            }

            if (list.size() != 3) {
                continue;
            }
            table.put(list.get(0), list.get(1), list.get(2));
        }

        return table;
    }
}

