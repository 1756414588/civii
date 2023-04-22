package com.game.handle;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListListListTypeHandler implements TypeHandler<List<List<List<Integer>>>> {
    private String listToString(List<List<List<Integer>>> parameter) {
        JSONArray arrays = null;
        if (parameter == null || parameter.isEmpty()) {
            arrays = new JSONArray();
            return arrays.toString();
        }

        // arrays = JSONArray.fromObject(parameter);
        return JSONArray.toJSONString(parameter);
    }

    private List<List<List<Integer>>> getListList(String columnName, String columnValue, int rowCount) {
        List<List<List<Integer>>> listList = new ArrayList<List<List<Integer>>>();
        if (columnValue == null || columnValue.isEmpty()) {
            return listList;
        }
        try {
            JSONArray arrays = JSONArray.parseArray(columnValue);
            for (int i = 0; i < arrays.size(); i++) {
                JSONArray colArr = arrays.getJSONArray(i);
                List<List<Integer>> colList = new ArrayList<>();
                for (int j = 0; j < colArr.size(); j++) {
                    List<Integer> valArr = colArr.getObject(j, List.class);
                    colList.add(valArr);
                }
                listList.add(colList);
            }
        } catch (Exception e) {
            // TODO: handle exception
            LoggerFactory.getLogger(getClass()).error("rowCount->[{}],colName->[{}],val->[{}]", rowCount, columnName, columnValue);
            throw e;
        }

        return listList;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, List<List<List<Integer>>> parameter, JdbcType jdbcType) throws SQLException {
        // TODO Auto-generated method stub
        ps.setString(i, this.listToString(parameter));
    }

    @Override
    public List<List<List<Integer>>> getResult(ResultSet rs, String columnName) throws SQLException {
        // TODO Auto-generated method stub
        String columnValue = rs.getString(columnName);
        int rowCount = rs.getRow();
        return this.getListList(columnName, columnValue, rowCount);
    }

    @Override
    public List<List<List<Integer>>> getResult(ResultSet rs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<List<List<Integer>>> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
}
