package com.game.handle;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.game.util.LogHelper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class ListIntTypeHandler implements TypeHandler<List<Integer>> {
	private List<Integer> getIntegerList(String columnValue,String columnName) {
		List<Integer> list = new ArrayList<Integer>();
		if (columnValue == null || columnValue.isEmpty()) {
			return list;
		}

		try {
            JSONArray array = JSONArray.parseArray(columnValue);
            for (int i = 0; i < array.size(); i++) {
                int value = array.getIntValue(i);
                list.add(value);
            }
        }catch (Exception ex) {
            LogHelper.ERROR_LOGGER.error("columnValue = " + columnValue + ", columnName = " +columnName + " data error!");
		    throw ex;
        }
		return list;
	}

	private String listToString(List<Integer> parameter) {
		JSONArray arrays = null;
		if (parameter == null || parameter.isEmpty()) {
			arrays = new JSONArray();
			return arrays.toJSONString();
		}

		return JSON.toJSONString(parameter);
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, List<Integer> parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, this.listToString(parameter));
	}

	@Override
	public List<Integer> getResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		return this.getIntegerList(columnValue,columnName);
	}

	@Override
	public List<Integer> getResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public List<Integer> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}
}
