package com.demai.util;

import com.demai.entity.RecommendLog;
import com.demai.service.IRankedLogService;
import com.demai.service.IRecommendLogService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 定期检查推荐备选集中的约见是否过期
 * Created by dear on 16/4/12.
 */
@Component
public class ExpireUtil {

    private final static Logger logger = LoggerFactory.getLogger(ExpireUtil.class);

    private MongoUtil mongoUtil = MongoUtil.getInstance();

    private static final int LIMIT = 100;//分页每页条数

    @Resource
    IRecommendLogService recommendLogService;

    @Resource
    IRankedLogService rankedLogService;

    /**
     * 从solr中查找未过期且报名人数未满的约见
     *
     * @return List<Feed> 符合条件(未过期且报名人数未满)的约见集合
     */
    private List<Long> findMeetsId(List<Long> meetIds) {
        List<Long> targetTids = new ArrayList<>();//推送的约见id

        List<Long> resultTids = new ArrayList<>();//推送的约见id


        if (meetIds == null || meetIds.size() == 0) {
            logger.info("no meets need to be searched from mongo to check if they are expired");
            return resultTids;
        }

        try {
            mongoUtil.setDbName("chat");
            mongoUtil.setCollectionName("feed");

            Map<String, Object> params = new HashMap<>();

            params.put("private", 0);//公开
            params.put("isdelete", 0);//未删除

            BasicDBList values = new BasicDBList();

            for (Long id : meetIds) {
                values.add(id);
            }
            params.put("uuid", new BasicDBObject("$in", values));

            int start = 0;

            Long now = System.currentTimeMillis() / 1000;//现在时间的unix 时间戳  1475913599L

            List<DBObject> result = new ArrayList<>();

            params.put("paging_start", start);
            params.put("paging_limit", LIMIT);

            List<DBObject> lists = mongoUtil.querySkipLimit(params);

            if (lists != null) {
                result.addAll(lists);
//                logger.info("first lists size is {}", lists.size());
                while (lists.size() >= LIMIT) {//循环分页查询满足条件的约见
                    start += LIMIT;
                    params.put("paging_start", start);
                    lists = mongoUtil.querySkipLimit(params);
                    result.addAll(lists);
//                    logger.info("query meets with start {} , limit {} and return size is {} and total so far is {}", start,
//                            LIMIT, lists.size(), result.size());
                }
            }

            try {
                //过滤掉时间已经过期的约见
                Iterator<DBObject> it = result.iterator();
                while (it.hasNext()) {
                    DBObject obj = it.next();
                    String meetDatesStr = obj.get("meetDates").toString();
                    meetDatesStr = meetDatesStr.replace("[", " ").replace("]", " ").replace("\"", " ");
                    String[] meetDatesArray = meetDatesStr.split(",");

                    int valid = 0;

                    for (String str : meetDatesArray) {
                        if (!"false".equals(str.trim())) {
                            Long date = Long.parseLong(str.trim());
                            if (date < now) {
                                valid++;
                            }
                        }
                    }
                    if (valid >= meetDatesArray.length) {
                        targetTids.add(Long.parseLong(obj.get("uuid").toString()));
                    }
                }
            } catch (Exception e) {
                logger.error("filter expired meeting error", e);
            }

            Set<Long> set = new HashSet<>(targetTids);

            resultTids = new ArrayList<>(set);
            logger.info("find meets from mongo targetSize size is {}", resultTids.size());

        } catch (Exception e) {
            logger.error("findMeetsId error", e);
            targetTids = new ArrayList<>();
        }

        return resultTids;
    }


    /**
     * 查找未过期且没有被屏蔽的约见id
     *
     * @return
     */
    private List<Long> findUnexpiredAndForbiddenMeetsId() {
        List<Long> result = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("expired", Constants.LogStatus.NO);
            params.put("forbidden", Constants.LogStatus.NO);
            params.put("type", 0);//0为约见

            List<RecommendLog> logs = recommendLogService.findObjectIds(params);

            StringBuffer sb = new StringBuffer("");

            for (RecommendLog log : logs) {
                sb.append(log.getObjectId()).append(",");
                result.add(log.getObjectId());
            }

            logger.info("findUnexpiredAndForbiddenMeetsId find {} meets i.e. {}", logs.size(), sb.toString());

        } catch (Exception e) {
            logger.error("findUnexpiredAndForbiddenMeetsId error", e);
            result = new ArrayList<>();
        }
        return result;
    }

    public void run() {
        try {
            long beginTime = System.currentTimeMillis();

            logger.info("expireUtil run starts at {}", beginTime);

            updateExpireMeets(findMeetsId(findUnexpiredAndForbiddenMeetsId()));

            long endTime = System.currentTimeMillis();
            logger.info("expireUtil run ends at {} and costs {} ms", endTime, endTime - beginTime);

        } catch (Exception e) {
            logger.error("expireUtil run error", e);
        }
    }


    private void updateExpireMeets(List<Long> meetIds) {
        try {
            Map<String, Object> params = new HashMap<>();

            if (meetIds != null && meetIds.size() > 0) {
                params.put("objectIds", meetIds);
                params.put("type", 0);
                params.put("expired", Constants.LogStatus.YES);
                recommendLogService.updateStatus(params);
                //rankedLogService.deleteLogs(params);
                logger.info("successfully update status and delete logs from ranked_log");
            } else {
                logger.info("0 matched recored need to be update");
            }


        } catch (Exception e) {
            logger.error("updateExpireMeets error", e);
        }
    }

}
