package com.game.handle;

import com.alibaba.fastjson.JSONObject;
import com.game.util.LogHelper;
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
 * @Date 2021/12/7 18:23
 **/
public class MapMapListIntTypeHandler extends BaseTypeHandler<Map<Integer, Map<Integer, List<Integer>>>> {

	@Override
	public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<Integer, Map<Integer, List<Integer>>> integerMapMap, JdbcType jdbcType) throws SQLException {

	}

	@Override
	public Map<Integer, Map<Integer, List<Integer>>> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		int rowCount = rs.getRow();
		return this.getMapList(columnName, columnValue, rowCount);
	}

	@Override
	public Map<Integer, Map<Integer, List<Integer>>> getNullableResult(ResultSet resultSet, int i) throws SQLException {
		return null;
	}

	@Override
	public Map<Integer, Map<Integer, List<Integer>>> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
		return null;
	}

	private Map<Integer, Map<Integer, List<Integer>>> getMapList(String columnName, String columnValue, int rowCount) {
		Map<Integer, Map<Integer, List<Integer>>> map = new HashMap<>();
		if (columnValue == null || columnValue.isEmpty()) {
			return map;
		}
		try {
			JSONObject.parseObject(columnValue).entrySet().forEach(e -> {
				Map<Integer, List<Integer>> integerListMap = map.computeIfAbsent(Integer.valueOf(e.getKey()), n -> new HashMap<>());
				JSONObject.parseObject(e.getValue().toString()).entrySet().forEach(x -> {
					List<Integer> list = integerListMap.computeIfAbsent(Integer.valueOf(x.getKey()), n -> new ArrayList<>());
					JSONObject.parseArray(x.getValue().toString()).forEach(m -> {
						Integer integer = Integer.valueOf(m.toString());
						list.add(integer);
					});
				});
			});
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("rowCount = " + rowCount + ",ListListTypeHandler parse: columnName =" + columnName + ", columnValue = " + columnValue);
			throw e;
		}
		return map;
	}

}
