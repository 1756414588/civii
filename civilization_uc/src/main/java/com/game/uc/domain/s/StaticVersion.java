package com.game.uc.domain.s;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2020/12/18 11:25
 * @description
 */
@Getter
@Setter
public class StaticVersion {
    private int id;
    private String cur_version;
    private int log_open;
    private String name;
}
