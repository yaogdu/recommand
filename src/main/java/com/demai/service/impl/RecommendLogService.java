package com.demai.service.impl;

import com.demai.dao.IRecommendLogDao;
import com.demai.datasource.DataSourceSwitch;
import com.demai.entity.RecommendLog;
import com.demai.service.IRecommendLogService;
import com.demai.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service("recommendLogService")
public class RecommendLogService implements IRecommendLogService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendLogService.class);

    @Resource
    IRecommendLogDao recommendLogDao;


    @Override
    public void saveBatch(List<RecommendLog> logs) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        recommendLogDao.saveBatch(logs);
    }

    @Override
    public void createLog(RecommendLog log) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        recommendLogDao.save(log);
    }

    @Override
    public List<RecommendLog> findLogs(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        return recommendLogDao.findByParam(params);
    }

    @Override
    public List<RecommendLog> findObjectIds(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");

        List<RecommendLog> logs = recommendLogDao.findObjectIds(params);
        return logs;
    }

    @Override
    public void updateStatus(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        recommendLogDao.updateStatus(params);
    }

    @Override
    public List<RecommendLog> findPushedUnViewedLogs(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        return recommendLogDao.findPushedUnViewedLogs(params);
    }

    @Override
    public void deleteUnPushed() {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        try {
            recommendLogDao.deleteUnPushed(Constants.LogStatus.NO);
        } catch (Exception e) {
            logger.error("deleteUnPushed error", e);
        }

    }

    @Override
    public long findLogsCount(Map<String, Object> params) {
        DataSourceSwitch.setDataSourceType("adminDataSource");
        long result = 0;
        try {
            recommendLogDao.findLogsCount(params);
        } catch (Exception e) {
            result = 0;
            logger.error("findLogsCount error", e);
        }
        return result;
    }
}
