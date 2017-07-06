package com.demai.service;

import com.demai.entity.CityLatlng;

import java.util.List;
import java.util.Map;

public interface ICityLatlngService {


    /**
     * @return lat, lng-->city
     */
    public Map<String, String> findAll();

    public void saveBatch(List<CityLatlng> cities);


}
