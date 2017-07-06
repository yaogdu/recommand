package com.demai.service;

import com.demai.entity.RecommendLog;

import java.util.List;
import java.util.Map;

public interface IRecommendLogService {

    public void saveBatch(List<RecommendLog> logs);

    public void createLog(RecommendLog log);

    public List<RecommendLog> findLogs(Map<String, Object> params);

    public List<RecommendLog> findObjectIds(Map<String, Object> params);

    public void updateStatus(Map<String, Object> params);

    public List<RecommendLog> findPushedUnViewedLogs(Map<String, Object> params);

    public void deleteUnPushed();

    public long findLogsCount(Map<String, Object> params);

}
