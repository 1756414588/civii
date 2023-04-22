package com.game.handle;


import com.alibaba.fastjson.JSONArray;
import com.game.domain.Award;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AwardTypeHandler extends BaseTypeHandler<List<Award>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Award> parameter, JdbcType jdbcType) throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Award> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // TODO Auto-generated method stub
        String columnValue = rs.getString(columnName);
        return getAwards(columnValue);
    }

    @Override
    public List<Award> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Award> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    private List<Award> getAwards(String columnValue) {
        List<Award> awards = new ArrayList<Award>();
        if (columnValue == null || columnValue.isEmpty()) {
            return awards;
        }

        if (!columnValue.startsWith("[[")) {
            return awards;
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
            Award elem = new Award(list.get(0), list.get(1), list.get(2));
            awards.add(elem);
        }

        return awards;
    }
}