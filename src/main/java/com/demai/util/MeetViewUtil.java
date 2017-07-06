package com.demai.util;

import com.demai.common.DateUtil;
import com.demai.entity.RecommendLog;
import com.demai.service.SolrService;
import com.demai.service.impl.RecommendLogService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dear on 16/3/22.
 * 用于查询用户点击推荐过的约见日志，更新数据库
 */
@Component
public class MeetViewUtil {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MeetViewUtil.class);

    private final int LIMIT = 500;

    private ExecutorService es = Executors.newFixedThreadPool(1);

    @Resource
    RecommendLogService recommendLogService;

    @Resource
    HttpUtil httpUtil;

    @Resource
    SolrService solrService;

    /**
     * 查询用户点击过的约见，
     * 并更新数据库
     */
    public void run() {
        try {
            long beginTime = System.currentTimeMillis();

            DataHolder.friendsCache = new ConcurrentHashMap<>();

            logger.info("MeetViewUtil run starts at {}", beginTime);

            checkViewed(pushedMessage());

            long endTime = System.currentTimeMillis();
            logger.info("MeetViewUtil run ends at {} and costs {} ms", endTime, endTime - beginTime);
        } catch (Exception e) {
            logger.error("MeetViewUtil run error", e);
        }
    }


    /**
     * 查询已经推荐过的但没有点击过的推荐记录
     */
    private Map<Long, List<Long>> pushedMessage() {
        Map<Long, List<Long>> result = new ConcurrentHashMap<>();

        try {

            Map<String, Object> map = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -10);

            map.put("time", DateUtil.format(cal.getTime(), "YYYY-MM-dd HH:mm:ss"));//10天内推送的信息

            int start = 0;

            map.put("start", start);
            map.put("rows", LIMIT);
            map.put("type", 0);//0 stands for meet,20008 in solr

            List<RecommendLog> logs = recommendLogService.findPushedUnViewedLogs(map);

            List<RecommendLog> lists = new ArrayList<>();

            if (logs != null && logs.size() >= LIMIT) {//分页取
                lists.addAll(logs);
                while (logs.size() >= LIMIT) {//循环分页查询满足条件的约见
                    start += LIMIT;
                    map.put("start", start);
                    logs = recommendLogService.findPushedUnViewedLogs(map);
                    lists.addAll(logs);
                }
            } else {
                lists.addAll(logs);
            }

            logger.info("pushedMessage find {} logs", lists.size());

            if (lists.size() > 0) {

                List<Long> tmpUids = new ArrayList<>();
                for (RecommendLog log : lists) {

                    if (result.containsKey(log.getObjectId())) {
                        tmpUids = result.get(log.getObjectId());
                    } else {
                        tmpUids = new ArrayList<>();
                    }

                    tmpUids.add(log.getUid());
                    result.put(log.getObjectId(), tmpUids);
                }
            }

            logger.info("pushedMessage result is {}", result);


        } catch (Exception e) {
            logger.error("pushedMessage error", e);
        }
        return result;
    }


    /**
     * 从日志查询是否点击
     *
     * @param param
     */
    private void checkViewed(Map<Long, List<Long>> param) {
        try {

            logger.info("start to check viewed via httputil");
            if (param == null || param.isEmpty()) {
                logger.info("no records need to be checked viewed");
                return;
            }

            for (Map.Entry<Long, List<Long>> entry : param.entrySet()) {
                final Long meetId = entry.getKey();

                final List<Long> uids = entry.getValue();

                if (uids != null && uids.size() > 0) {

                    es.submit(new Runnable() {
                        @Override
                        public void run() {
                            Set<Long> meetUids = solrService.queryMeetUids(uids, meetId);

                            logger.info("meetId {} uids {} result is {}", meetId, uids, meetUids);
                            analysis(meetId, meetUids);
                        }
                    });

                } else {
                    logger.info("meetId {} has no corresponding uids", meetId);
                }
            }

        } catch (Exception e) {
            logger.error("checkViewed error", e);
        }

    }


    private void analysis(Long meetId, Set<Long> uids) {

        Map<String, Object> result = new HashMap<>();
        try {
            if (uids == null || uids.size() == 0) {
                logger.info("analysis uids is empty");
                return;
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uids", uids);
            map.put("meetId", meetId);
            map.put("viewed", com.demai.util.Constants.LogStatus.YES);
            map.put("viewTime", DateUtil.format(new Date(), "YYYY-MM-dd HH:mm:ss"));

            recommendLogService.updateStatus(map);

            logger.info("update meetId {} uids {} to viewed successfully", meetId, uids);

        } catch (Exception e) {
            logger.error("analysis error", e);
        }


    }
}
