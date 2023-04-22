package com.game.dao.p;

import java.util.List;

import com.game.domain.p.City;

public interface CityDao {
	
    public City selectCity(int cityId);

    public void updateCity(City city);

    public void insertCity(City city);

    public List<City> selectCityList();
}
