package com.demai.service.impl;

import com.demai.dao.ICityLatlngDao;
import com.demai.datasource.DataSourceSwitch;
import com.demai.entity.CityLatlng;
import com.demai.service.ICityLatlngService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("cityLatlngService")
public class CityLatlngService implements ICityLatlngService {

    private final static Logger logger = LoggerFactory.getLogger(CityLatlngService.class);
    @Autowired
    ICityLatlngDao cityLatlngDao;


    @Override
    public Map<String, String> findAll() {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        Map<String, String> result = new HashMap<>();

        try {
            DataSourceSwitch.setDataSourceType("adminDataSource");
            List<CityLatlng> cities = cityLatlngDao.findAll();

            if (cities != null && cities.size() > 0) {
                for (CityLatlng city : cities) {
                    result.put(city.getLat() + "," + city.getLng(), city.getCity());
                }
            }

        } catch (Exception e) {
            result = new HashMap<>();
            logger.error("citylatlngservice error", e);
        }
        logger.info("citylatlngservice findAll find {} result", result.size());
        return result;
    }

    @Override
    public void saveBatch(List<CityLatlng> cities) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        try {
            cityLatlngDao.saveBatch(cities);
        } catch (Exception e) {
            logger.error("saveBatch error", e);
        }
    }


}
