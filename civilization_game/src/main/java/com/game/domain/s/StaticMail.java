package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaticMail {

    private int mailId;
    private int type;
    private int share;
    private List<Integer> shareIndex;
    private List<Integer> titleIndex;
    private String param;
    private String title;
    private String titleContent;
    private String head;
    private int[] titleIndexArr;
    private List<List<Integer>> award;
}
