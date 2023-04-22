package com.game.domain.s;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 * @date 2020/8/19 15:02
 * @description 关卡
 */
public class StaticTowerWarLevel {
  private int id;
  private String name;
  private String icon;
  private String resource;
  private List<Integer> condition;
  private int life_point;
  private int base_supplies;
  private List<Integer> start_limit;
  private List<List<Integer>> award_list_1;
  private List<List<Integer>> award_list_2;
  private List<List<Integer>> award_list_3;
  private Map<Integer,List<List<Integer>>> wave_list;
  private Map<Integer,List<Integer>> way_list;
  private List<List<Integer>> tower_list;
  private List<Integer> monster_list;
  private List<List<Double>> way_point_list;
  private List<List<Double>> tower_base_list;
  private List<Integer> camera_limit;

  public List<Integer> getCamera_limit() {
    return camera_limit;
  }

  public void setCamera_limit(List<Integer> camera_limit) {
    this.camera_limit = camera_limit;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public List<Integer> getCondition() {
    return condition;
  }

  public void setCondition(List<Integer> condition) {
    this.condition = condition;
  }

  public int getLife_point() {
    return life_point;
  }

  public void setLife_point(int life_point) {
    this.life_point = life_point;
  }

  public int getBase_supplies() {
    return base_supplies;
  }

  public void setBase_supplies(int base_supplies) {
    this.base_supplies = base_supplies;
  }

  public List<Integer> getStart_limit() {
    return start_limit;
  }

  public void setStart_limit(List<Integer> start_limit) {
    this.start_limit = start_limit;
  }

  public List<List<Integer>> getAward_list_1() {
    return award_list_1;
  }

  public void setAward_list_1(List<List<Integer>> award_list_1) {
    this.award_list_1 = award_list_1;
  }

  public List<List<Integer>> getAward_list_2() {
    return award_list_2;
  }

  public void setAward_list_2(List<List<Integer>> award_list_2) {
    this.award_list_2 = award_list_2;
  }

  public List<List<Integer>> getAward_list_3() {
    return award_list_3;
  }

  public void setAward_list_3(List<List<Integer>> award_list_3) {
    this.award_list_3 = award_list_3;
  }

  public Map<Integer, List<List<Integer>>> getWave_list() {
    return wave_list;
  }

  public void setWave_list(Map<Integer, List<List<Integer>>> wave_list) {
    this.wave_list = wave_list;
  }

  public List<List<Integer>> getTower_list() {
    return tower_list;
  }

  public void setTower_list(List<List<Integer>> tower_list) {
    this.tower_list = tower_list;
  }

  public List<Integer> getMonster_list() {
    return monster_list;
  }

  public void setMonster_list(List<Integer> monster_list) {
    this.monster_list = monster_list;
  }

  public Map<Integer, List<Integer>> getWay_list() {
    return way_list;
  }

  public void setWay_list(Map<Integer, List<Integer>> way_list) {
    this.way_list = way_list;
  }

  public List<List<Double>> getWay_point_list() {
    return way_point_list;
  }

  public void setWay_point_list(List<List<Double>> way_point_list) {
    this.way_point_list = way_point_list;
  }

  public List<List<Double>> getTower_base_list() {
    return tower_base_list;
  }

  public void setTower_base_list(List<List<Double>> tower_base_list) {
    this.tower_base_list = tower_base_list;
  }
}
