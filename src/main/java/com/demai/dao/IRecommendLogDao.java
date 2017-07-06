package com.demai.dao;

import com.demai.entity.RecommendLog;

import java.util.List;
import java.util.Map;

public interface IRecommendLogDao extends IBaseDao<RecommendLog, Long> {

    public void saveBatch(List<RecommendLog> logs);

    public List<RecommendLog> findByParam(Map<String, Object> params);

    public List<RecommendLog> findObjectIds(Map<String, Object> params);

    public void updateStatus(Map<String,Object> params);

    public List<RecommendLog> findPushedUnViewedLogs(Map<String, Object> params);

    public void deleteUnPushed(int pushed);

    public long findLogsCount(Map<String, Object> params);
}
