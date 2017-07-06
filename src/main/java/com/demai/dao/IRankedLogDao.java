package com.demai.dao;

import com.demai.dao.IBaseDao;
import com.demai.entity.RankedLog;
import com.demai.entity.RecommendLog;

import java.util.List;
import java.util.Map;

public interface IRankedLogDao extends IBaseDao<RankedLog, Long> {

    public void saveBatch(List<RankedLog> logs);

    public List<RankedLog> findByParam(Map<String, Object> params);

    public List<RankedLog> findObjectIds(Map<String, Object> params);

    public void updateStatus(Map<String, Object> params);

    public void deleteLogs(Map<String, Object> params);

    public void deleteAll();
}
