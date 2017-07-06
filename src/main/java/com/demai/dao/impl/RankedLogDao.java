package com.demai.dao.impl;

import com.demai.dao.IRankedLogDao;
import com.demai.entity.RankedLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RankedLogDao extends BaseDao<RankedLog, Long> implements IRankedLogDao {

    private final String namespace = RankedLog.class.getName();

    @Override
    public void update(RankedLog t) {
        super.update("update", t);
    }

    @Override
    public RankedLog save(RankedLog RankedLog) {
        return super.save(RankedLog);
    }

    @Override
    public void saveBatch(List<RankedLog> logs) {
        sqlSession.insert(namespace + ".insertBatch", logs);
    }

    @Override
    public List<RankedLog> findByParam(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findLogs", params);
    }

    @Override
    public List<RankedLog> findObjectIds(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findObjectIds", params);
    }

    @Override
    public void updateStatus(Map<String, Object> params) {
        sqlSession.update(namespace + ".updateStatus", params);
    }

    @Override
    public void deleteLogs(Map<String, Object> params) {
        sqlSession.delete(namespace + ".deleteInBatch", params);
    }

    @Override
    public void deleteAll() {
        sqlSession.delete(namespace + ".deleteAll");
    }

}
