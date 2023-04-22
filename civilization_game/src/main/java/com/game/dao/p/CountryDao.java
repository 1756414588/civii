package com.game.dao.p;

import java.util.List;

import com.game.domain.p.Country;

public interface CountryDao {

	public List<Country> selectCountryList();

	public void updateCountry(Country country);

	public void insertCountry(Country country);
	
	public void insertSelective(Country country);

}
