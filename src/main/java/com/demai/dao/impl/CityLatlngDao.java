package com.demai.dao.impl;

import com.demai.dao.ICityLatlngDao;
import com.demai.entity.CityLatlng;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityLatlngDao extends BaseDao<CityLatlng, Long> implements ICityLatlngDao {

    private final String namespace = CityLatlng.class.getName();

    @Override
    public void update(CityLatlng t) {
        super.update("update", t);
    }

    @Override
    public List<CityLatlng> findAll() {
        return sqlSession.selectList(namespace + ".findAll");
    }

    @Override
    public void saveBatch(List<CityLatlng> cities) {
        sqlSession.insert(namespace+".insertBatch",cities);
    }


}
