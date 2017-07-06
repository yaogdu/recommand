package com.demai.dao;

import com.demai.entity.CityLatlng;

import java.util.List;

public interface ICityLatlngDao extends IBaseDao<CityLatlng, Long> {

    public List<CityLatlng> findAll();

    public void saveBatch(List<CityLatlng> cities);
}
