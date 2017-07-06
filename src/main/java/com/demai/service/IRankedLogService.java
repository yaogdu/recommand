package com.demai.service;

import com.demai.entity.RankedLog;

import java.util.List;
import java.util.Map;

public interface IRankedLogService {

    public void saveBatch(List<RankedLog> logs);

    public void createLog(RankedLog log);

    public List<RankedLog> findLogs(Map<String, Object> params);

    public List<RankedLog> findObjectIds(Map<String, Object> params);

    public void updateStatus(Map<String, Object> params);

    public void deleteLogs(Map<String,Object> params);

    public void deleteAll();


}
