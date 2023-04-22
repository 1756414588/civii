package com.game.domain.s;

import java.util.List;

/**
 * @author cpz
 * @date 2020/8/19 15:02
 * @description
 */
public class StaticTowerWarWave {
  private int id;
  private String name;
  private List<List<Double>> monster_list;

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

  public List<List<Double>> getMonster_list() {
    return monster_list;
  }

  public void setMonster_list(List<List<Double>> monster_list) {
    this.monster_list = monster_list;
  }
}
