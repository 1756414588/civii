package com.game.uc.dao.handle;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListStringTypeHandler implements TypeHandler<List<String>> {
	private List<String> getLongList(String columnValue) {
		List<String> list = new ArrayList<String>();
		if (columnValue == null || columnValue.isEmpty()) {
			return list;
		}

//		JSONArray array = JSONArray.fromObject(columnValue);
		JSONArray array = JSONArray.parseArray(columnValue);
		for (int i = 0; i < array.size(); i++) {
			String value = array.getString(i);
			list.add(value);
		}
		return list;
	}

	private String listToString(List<String> parameter) {
		JSONArray arrays = null;
		if (parameter == null || parameter.isEmpty()) {
			arrays = new JSONArray();
			return arrays.toString();
		}

//		arrays = JSONArray.fromObject(parameter);
		return JSONArray.toJSONString(parameter);
	}

	@Override
	public void setParameter(PreparedStatement arg0, int arg1, List<String> arg2, JdbcType arg3) throws SQLException {
		// TODO Auto-generated method stub
		arg0.setString(arg1, this.listToString(arg2));
	}

	@Override
	public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
		// TODO Auto-generated method stub
		String columnValue = rs.getString(columnName);
		return this.getLongList(columnValue);
	}

	@Override
	public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
