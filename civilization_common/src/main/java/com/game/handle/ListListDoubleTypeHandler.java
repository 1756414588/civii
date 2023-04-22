package com.game.handle;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListListDoubleTypeHandler implements TypeHandler<List<List<Double>>> {
  private static Logger logger = LoggerFactory.getLogger(ListListDoubleTypeHandler.class);

  private String listToString(List<List<Double>> parameter) {
    JSONArray arrays = null;
    if (parameter == null || parameter.isEmpty()) {
      arrays = new JSONArray();
      return arrays.toString();
    }
    return JSONArray.toJSONString(parameter);
  }

  private List<List<Double>> getListList(String columnName, String columnValue, int rowCount) {
    List<List<Double>> listList = new ArrayList<List<Double>>();
    if (columnValue == null || columnValue.isEmpty()) {
      return listList;
    }
    try {
      JSONArray arrays = JSONArray.parseArray(columnValue);
      for (int i = 0; i < arrays.size(); i++) {
        List<Double> list = new ArrayList<Double>();
        JSONArray array = arrays.getJSONArray(i);
        for (int j = 0; j < array.size(); j++) {
          list.add(array.getDouble(j));
        }
        listList.add(list);
      }
    } catch (Exception e) {
      // TODO: handle exception
      logger.error(
          "rowCount->[{}],ListListTypeHandler parse: columnName->[{}], columnValue->[{}]",
          rowCount,
          columnName,
          columnValue);
      throw e;
    }

    return listList;
  }

  @Override
  public void setParameter(
      PreparedStatement ps, int i, List<List<Double>> parameter, JdbcType jdbcType)
      throws SQLException {
    // TODO Auto-generated method stub
    ps.setString(i, this.listToString(parameter));
  }

  @Override
  public List<List<Double>> getResult(ResultSet rs, String columnName) throws SQLException {
    // TODO Auto-generated method stub
    String columnValue = rs.getString(columnName);
    int rowCount = rs.getRow();
    return this.getListList(columnName, columnValue, rowCount);
  }

  @Override
  public List<List<Double>> getResult(ResultSet rs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<List<Double>> getResult(CallableStatement cs, int columnIndex) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }
}
