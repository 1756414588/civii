package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorldMap {

    private int mapId;
    private byte[] mapData;   //16M
    private long lastSaveTime;
    private byte[] quickWarData;   //16M
    private byte[] farWarData;   //16M
    private byte[] countryWarData;   //16M
    private long maxKey;
    private byte[] bigMonsterWarData;
	private byte[] zergWarData;//
}
