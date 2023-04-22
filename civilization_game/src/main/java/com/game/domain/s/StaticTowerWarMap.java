package com.game.domain.s;

import java.util.List;

/**
 * @author cpz
 * @date 2020/8/19 15:02
 * @description
 */
public class StaticTowerWarMap {
  private int id;
  private String note;
  private int difficulty;
  private List<Integer> level_list;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public int getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(int difficulty) {
    this.difficulty = difficulty;
  }

  public List<Integer> getLevel_list() {
    return level_list;
  }

  public void setLevel_list(List<Integer> level_list) {
    this.level_list = level_list;
  }
}
