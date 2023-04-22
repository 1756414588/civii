package com.game.season;


import com.game.dao.p.SeasonActDao;
import com.game.domain.Player;
import com.game.spring.SpringUtil;

public abstract class BaseModule {

	private Player player;

	public abstract SeasonAct getType();

	public abstract void load(SeasonInfo seasonInfo);

	public abstract byte[] save();

	public void saveInfo() {
		byte[] save = save();
		SeasonInfo seasonInfo = new SeasonInfo();
		seasonInfo.setRoleId(player.getRoleId());
		seasonInfo.setSeasonType(this.getType().getActId());
		seasonInfo.setInfo(save);
		SpringUtil.getBean(SeasonActDao.class).insert(seasonInfo);
	}

	public abstract void clean();

	public abstract void clean(int actId);

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
}
