package com.game.domain.s;

import lombok.Getter;

@Getter
public class StaticSensitiveWord {
    private int id;
    private String sensitiveWord;
    private int privateChatSwitch;
    private int countryChatSwitch;
    private int areaChatSwitch;
    private int countryMailSwitch;
}