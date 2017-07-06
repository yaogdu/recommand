package com.demai.service.impl;

import com.demai.dao.IRankedLogDao;
import com.demai.datasource.DataSourceSwitch;
import com.demai.entity.RankedLog;
import com.demai.service.IRankedLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("rankedLogService")
public class RankedLogService implements IRankedLogService {


    @Resource
    IRankedLogDao rankedLogDao;


    @Override
    public void saveBatch(List<RankedLog> logs) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        rankedLogDao.saveBatch(logs);
    }

    @Override
    public void createLog(RankedLog log) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        rankedLogDao.save(log);
    }

    @Override
    public List<RankedLog> findLogs(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        return rankedLogDao.findByParam(params);
    }

    @Override
    public List<RankedLog> findObjectIds(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        return rankedLogDao.findObjectIds(params);
    }

    @Override
    public void updateStatus(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        rankedLogDao.updateStatus(params);
    }

    @Override
    public void deleteLogs(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        rankedLogDao.deleteLogs(params);
    }

    @Override
    public void deleteAll() {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        rankedLogDao.deleteAll();
    }

}
