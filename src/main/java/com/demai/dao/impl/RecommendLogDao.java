package com.demai.dao.impl;

import com.demai.dao.IRecommendLogDao;
import com.demai.entity.RecommendLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecommendLogDao extends BaseDao<RecommendLog, Long> implements IRecommendLogDao {

    private final String namespace = RecommendLog.class.getName();

    @Override
    public void update(RecommendLog t) {
        super.update(namespace + ".update", t);
    }

    @Override
    public RecommendLog save(RecommendLog recommendLog) {
        return super.save(recommendLog);
    }

    @Override
    public void saveBatch(List<RecommendLog> logs) {
        sqlSession.insert(namespace + ".insertBatch", logs);
    }

    @Override
    public List<RecommendLog> findByParam(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findLogs", params);
    }

    @Override
    public List<RecommendLog> findObjectIds(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findObjectIds", params);
    }

    @Override
    public void updateStatus(Map<String, Object> params) {
        sqlSession.update(namespace + ".updateStatus", params);
    }

    @Override
    public List<RecommendLog> findPushedUnViewedLogs(Map<String, Object> params) {
        return sqlSession.selectList(namespace + ".findPushedUnViewedLogs", params);
    }

    @Override
    public void deleteUnPushed(int pushed) {
        sqlSession.delete(namespace + ".deleteUnPushed", pushed);
    }

    @Override
    public long findLogsCount(Map<String, Object> params) {

        Object result = sqlSession.selectOne(namespace + ".findLogsCount", params);

        if (result != null) {
            return Long.parseLong(result.toString());
        } else {
            return 0L;
        }

    }
}
