package com.game.flame;

import com.game.pb.FlameWarPb;

import java.util.HashMap;
import java.util.Map;

public class FlameCountry {
	private int country;
	private long firstResource;// 首占资源
	private long resource;// 国家资源（累计）
	private Map<Integer, Integer> count = new HashMap<>();// key 据点等级 val 据点数量
	private long extra;// 额外奖励

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public long getFirstResource() {
		return firstResource;
	}

	public void setFirstResource(long firstResource) {
		this.firstResource = firstResource;
	}

	public long getResource() {
		return resource;
	}

	public void setResource(long resource) {
		this.resource = resource;
	}

	public void addResource(long resource) {
		this.resource += resource;
	}

	public void addFirstResource(long resource) {
		this.firstResource += resource;
	}

	public FlameWarPb.CountryFlame encode() {
		FlameWarPb.CountryFlame.Builder builder = FlameWarPb.CountryFlame.newBuilder();
		builder.setCountry(this.country);
		builder.setFirstResource(this.firstResource);
		builder.setResource(this.resource);
		return builder.build();
	}

	public FlameCountry() {

	}

	public FlameCountry(FlameWarPb.CountryFlame builder) {
		this.country = builder.getCountry();
		this.firstResource = builder.getFirstResource();
		this.resource = builder.getResource();
	}

	public void addExtra(long res) {
		this.extra += res;
	}

	public Map<Integer, Integer> getCount() {
		return count;
	}

	public void setCount(Map<Integer, Integer> count) {
		this.count = count;
	}

	public long getExtra() {
		return extra;
	}

	public void setExtra(long extra) {
		this.extra = extra;
	}
}
