package com.game.worldmap.fight.manoeuvre;

import com.game.domain.p.Hero;
import com.game.pb.CommonPb.ManoeuverApply;
import com.game.pb.DataPb.HeroData;
import com.game.pb.SerializePb.ManApplyPB;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManoeuvreFighter {

	// 角色ID
	private long playerId;
	// 国家
	private int country;
	// 报名武将数据
	private List<Hero> heroes = new ArrayList<>();
	// 战力
	private long power;
	// 线路
	private int line;
	// 位置
	private int pos;
	// 报名时间
	private long applyTime;
	// 生命值
	private int blood;
	// 次数
	private int count = 0;
	//击败人数
	private int beat;

	private ManoeuverApply applyPb;

	public ManoeuvreFighter() {
	}

	public ManoeuvreFighter(ManApplyPB pb) {
		this.playerId = pb.getPlayerId();
		this.country = pb.getCountry();
		for (HeroData heroData : pb.getHeroList()) {
			Hero hero = new Hero();
			hero.readData(heroData);
			this.heroes.add(hero);
		}
		this.power = pb.getPower();
		this.line = pb.getLine();
		this.applyTime = pb.getApplyTime();
		this.blood = pb.getBlood();
		this.count = pb.getCount();
		this.pos = pb.getPos();
	}

	public ManApplyPB wrap() {
		ManApplyPB.Builder builder = ManApplyPB.newBuilder();
		builder.setPlayerId(playerId);
		builder.setCountry(country);
		for (Hero hero : heroes) {
			builder.addHero(hero.writeData().build());
		}
		builder.setPower(power);
		builder.setLine(line);
		builder.setApplyTime(applyTime);
		builder.setBlood(blood);
		builder.setCount(count);
		builder.setPos(pos);
		return builder.build();
	}

	public boolean alive() {
		return blood > 0 && count < 2;
	}

	public Hero getHero(int heroId) {
		Optional<Hero> optional = heroes.stream().filter(e -> e.getHeroId() == heroId).findFirst();
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Override
	public String toString() {
		return "playerId:" + playerId + " power:" + power + " apply:" + applyTime;
	}

}
