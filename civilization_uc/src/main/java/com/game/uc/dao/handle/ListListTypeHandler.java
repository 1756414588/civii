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

public class ListListTypeHandler implements TypeHandler<List<List<Integer>>> {
	private String listToString(List<List<Integer>> parameter) {
		JSONArray arrays = null;
		if (parameter == null || parameter.isEmpty()) {
			arrays = new JSONArray();
			return arrays.toString();
		}

		// arrays = JSONArray.fromObject(parameter);
		return JSONArray.toJSONString(parameter);
	}

	private List<List<Integer>> getListList(String columnName, String columnValue, int rowCount) {
		List<List<Integer>> listList = new ArrayList<List<Integer>>();
		if (columnValue == null || columnValue.isEmpty()) {
			return listList;
		}

		// JSONArray arrays = JSONArray.fromObject(columnValue);
		try {
			JSONArray arrays = JSONArray.parseArray(columnValue);
			for (int i = 0; i < arrays.size(); i++) {
				List<Integer> list = new ArrayList<Integer>();
				JSONArray array = arrays.getJSONArray(i);
				for (int j = 0; j < array.size(); j++) {
					list.add(array.getInteger(j));
				}

				// if (!list.isEmpty()) {
				listList.add(list);
				// }
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("rowCount = " + rowCount + ",ListListTypeHandler parse: columnName =" + columnName + ", columnValue = " + columnValue);
			throw e;
		}

		return listList;
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, List<List<Integer>> parameter, JdbcType jdbcType) throws SQLException {
		// TODO Auto-generated method stub
		ps.setString(i, this.listToString(parameter));
	}

	@Override
	public List<List<Integer>> getResult(ResultSet rs, String columnName) throws SQLException {
		// TODO Auto-generated method stub
		String columnValue = rs.getString(columnName);
		int rowCount = rs.getRow();
		return this.getListList(columnName, columnValue, rowCount);
	}

	@Override
	public List<List<Integer>> getResult(ResultSet rs, int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<Integer>> getResult(CallableStatement cs, int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
