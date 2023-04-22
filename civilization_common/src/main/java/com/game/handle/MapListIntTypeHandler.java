package com.game.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/12/10 0:02
 **/
public class MapListIntTypeHandler extends BaseTypeHandler<Map<Integer, List<Integer>>> {

	@Override
	public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<Integer, List<Integer>> integerListMap, JdbcType jdbcType) throws SQLException {

	}

	@Override
	public Map<Integer, List<Integer>> getNullableResult(ResultSet resultSet, String s) throws SQLException {
		String columnValue = resultSet.getString(s);
		return getMapList(columnValue);
	}

	@Override
	public Map<Integer, List<Integer>> getNullableResult(ResultSet resultSet, int i) throws SQLException {
		return null;
	}

	@Override
	public Map<Integer, List<Integer>> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
		return null;
	}

	private Map<Integer, List<Integer>> getMapList(String columnValue) {
		Map<Integer, List<Integer>> mapList = new HashMap<>();
		if (columnValue == null || columnValue.isEmpty()) {
			return mapList;
		}
		JSONObject.parseObject(columnValue).entrySet().forEach(e -> {
			List<Integer> list = mapList.computeIfAbsent(Integer.valueOf(e.getKey()), x -> new ArrayList<>());
			JSONObject.parseArray(e.getValue().toString()).forEach(v -> {
				list.add(Integer.valueOf(v.toString()));
			});
		});
		return mapList;
	}

	public static void main(String[] args) {
		Map<Integer, List<Integer>> mapList = new HashMap<>();
		JSONObject jsonObject = JSONObject.parseObject("{1:[1,2,3,4],2:[1,2,3,4]}");
		jsonObject.entrySet().forEach(e -> {
			List<Integer> list = mapList.computeIfAbsent(Integer.valueOf(e.getKey()), x -> new ArrayList<>());
			JSONObject.parseArray(e.getValue().toString()).forEach(v -> {
				list.add(Integer.valueOf(v.toString()));
			});
		});
		System.out.println(mapList);
	}
}
