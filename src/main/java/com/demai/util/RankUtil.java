package com.demai.util;

import com.demai.common.bean.Feed;
import com.demai.common.utils.StringUtils;
import com.demai.entity.RankedLog;
import com.demai.entity.RecommendLog;
import com.demai.entity.User;
import com.demai.service.IFollowsService;
import com.demai.service.IRankedLogService;
import com.demai.service.IRecommendLogService;
import com.demai.service.IUserService;
import com.demai.solr.feed.FeedService;
import com.demai.solr.user.UserService;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推荐约见备选集打分
 * <p/>
 * Created by dear on 16/4/13.
 */
@Component
public class RankUtil {


    private final static Logger logger = LoggerFactory.getLogger(RankUtil.class);

    @Resource
    RedisUtil activeRedisUtil;

    @Resource
    RedisUtil secondRedisUtil;

    @Resource
    IRecommendLogService recommendLogService;

    @Resource
    IRankedLogService rankedLogService;

    @Resource
    FeedService feedService;

    @Resource
    IUserService mysqlUserService;

    @Resource
    UserService userService;

    @Resource
    IFollowsService followsService;

    private static Map<Long, Map<String, String>> secondFriendsCache = new HashMap<>();//uid-uid , uid,存在即表示是二度人脉

    @Resource
    HttpUtil httpUtil;

    @Value("#{settings['gaode.geo.url']}")
    String geoUrl;

    @Value("#{settings['gaode.regeo.url']}")
    String regeoUrl;

    private MongoUtil mongoUtil = MongoUtil.getInstance();

    private static final int MYSQL_LIMIT = 5000;

    private static final int LIMIT = 100;//分页每页条数

    private static List<Long> leftUids = new ArrayList<>();//有效用户id列表

    private static List<Long> leftMeetIds = new ArrayList<>();//有效约见id列表


//    /**
//     * 获取三个月内登录过的uid
//     */
//    private List<Long> getActivedUids() {
//        long beginTime = System.currentTimeMillis();
//        logger.info("rankUtil getActivedUids start at {}", beginTime);
//
//        List<Long> uids = new ArrayList<>();
//
//        try {
//
//            Calendar cal = Calendar.getInstance();
//
//            long now = cal.getTimeInMillis();
//            cal.add(Calendar.MONTH, -3);
//
//            long before = cal.getTimeInMillis();
//
//            uids = new ArrayList<>();
//            Set<Tuple> members = activeRedisUtil.zrangeByScores("online_time", before, now);
//            for (Tuple member : members) {
//                uids.add(new Long(member.getElement()));
//            }
//            //logger.info("uids are {}", JSON.toJSONString(uids));
//            logger.info("rankUtil getActivedUids find {} users", uids.size());
//        } catch (Exception e) {
//            uids = new ArrayList<>();
//            logger.error("error occurred", e);
//        }
//        long endTime = System.currentTimeMillis();
//        logger.info("rankUtil getActivedUids end at {} and costs {} ms", endTime, endTime - beginTime);
//
//        return uids;
//    }


    /**
     * 查找有效的备选集
     */
    private List<RecommendLog> findRecommendLogs(int start, int size) {
        logger.info("start to rankUtil findRecommendLogs");
        List<RecommendLog> logs = new ArrayList<>();
        try {

            Map<String, Object> params = new HashMap<>();
            params.put("type", 0);
            params.put("forbidden", Constants.LogStatus.NO);
            params.put("expired", Constants.LogStatus.NO);
            params.put("rows", MYSQL_LIMIT);
            params.put("start", start);


            List<RecommendLog> tempList = recommendLogService.findLogs(params);
            //分批查询
            if (tempList != null) {
                logs.addAll(tempList);
                while (logs.size() < size) {
                    start += MYSQL_LIMIT;
                    params.put("start", start);
                    tempList = recommendLogService.findLogs(params);
                    logs.addAll(tempList);
                }
            }

        } catch (Exception e) {
            logs = new ArrayList<>();
            logger.error("rankUtil findRecommendLogs error", e);
        }
        logger.info("rankUtil findRecommendLogs find {} result", logs.size());

        return logs;
    }


    /**
     * distinct 有效推荐的用户id和约见id
     */
    private void distinctUsersAndLogs(List<RecommendLog> logs) {

        logger.info("start to distinctUsersAndLogs");

        leftMeetIds = new ArrayList<>();
//        leftUids = new ArrayList<>();

        try {
            if (logs != null && logs.size() > 0) {
                for (RecommendLog log : logs) {

                    if (!leftMeetIds.contains(log.getObjectId())) {
                        leftMeetIds.add(log.getObjectId());
                    }

//                    if (!leftUids.contains(log.getUid())) {
//                        leftUids.add(log.getUid());//所有要被推荐的用户id列表
//                    }
                }
            }

            logger.info("left meetIds find {} result ", leftMeetIds.size());
        } catch (Exception e) {
            logger.error("rankUtil distinctUsersAndLogs error", e);
        }

    }

//
//    /**
//     * 过滤掉一定时间内没有活跃的用户
//     */
//    private void filterActiveUser(List<Long> uids) {
//        logger.info("start to filterActiveUser");
//
//        List<Long> result = new ArrayList<>();
//        try {
//
//            if (uids != null && uids.size() > 0) {
//                if (leftUids != null && leftUids.size() > 0) {
//                    for (Long uid : leftUids) {
//                        if (uids.contains(uid)) {
//                            result.add(uid);
//                        }
//                    }
//                }
//            }
//
//            leftUids = result;
//        } catch (Exception e) {
//            logger.error("filterActiveUser error", e);
//        }
//        logger.info("filterActiveUser find {} result ", leftUids.size());
//    }


    /**
     * 根据约见id查询详细信息
     *
     * @param tids 约见id集合
     */
    private Map<Long, Feed> findMeetsDetail(List<Long> tids) {

        logger.info("start to findMeetsDetail from solr");

        Map<Long, Feed> feedMap = new ConcurrentHashMap<>();//feedId -->feed

        try {
            if (tids != null && tids.size() > 0) {//分批从solr里取数据
                for (Long tid : tids) {
                    Feed feed = DataHolder.feedMap.get(tid);
                    if (feed != null) {
                        feedMap.put(tid, feed);
                    }
                }
            }

            logger.info("findMeetsDetail from solr result size is {}", feedMap.size());
        } catch (Exception e) {
            logger.error("findMeetsDetail error", e);
            feedMap = new ConcurrentHashMap<>();
        }

        return feedMap;
    }


    /**
     * 计算约见发贴人的id集合
     *
     * @param feedMap 约见map
     */
    private List<Long> calcFeedOwnerIdList(Map<Long, Feed> feedMap) {
        logger.info("start to calcFeedOwnerIdList");

        List<Long> ownerIdList = new ArrayList<>();

        try {
            if (feedMap != null && feedMap.size() > 0) {
                for (Map.Entry<Long, Feed> entry : feedMap.entrySet()) {
                    Feed feed = entry.getValue();
                    ownerIdList.add(feed.getUid());
                }
            }
        } catch (Exception e) {
            ownerIdList = new ArrayList<>();
            logger.error("calcFeedOwnerIdList error", e);
        }
        return ownerIdList;
    }

//    /**
//     * 组装成ranklog
//     *
//     * @param logs
//     */
//    private List<RankedLog> convertToRankLog(List<RecommendLog> logs, Map<Long, Feed> feedMap, Map<Long, String>
//            mobileMapping, Map<Long, com.demai.common.bean.User> userMap) {
//        logger.info("start to convertToRankLog");
//        List<RankedLog> rankedLogs = new ArrayList<>();
//        try {
//
//            if (logs != null && logs.size() > 0) {
//                logger.info("logs size {}", logs.size());
//                int times = 0;
//                for (RecommendLog log : logs) {
//                    times++;
//
//                    if (leftUids.contains(log.getUid())) {
//                        RankedLog rl = new RankedLog();
//                        long objectId = log.getObjectId();
//                        Feed feed = feedMap.get(objectId);
//                        if (feed != null) {
//                            rl.setObjectId(objectId);
//                            rl.setUid(log.getUid());
//                            rl.setCity(feed.getCity());
//                            int rank = 0;
//
//                            com.demai.common.bean.User user = userMap.get(rl.getUid());
//                            if (user != null) {
//                                rank += calcTag(user, feed);//标签权重值打分
//                            }
//                            int friendRank = friendsRank(feed.getUid(), log.getUid());
//                            if (friendRank > 0) {
//                                rank += friendRank;
//                            } else {
//                                //logger.info("second friends rank to be calced...");
//                                rank += secondFriendsRank(feed.getUid(), log.getUid());
//                            }
//
//                            String mobile = mobileMapping.get(feed.getUid());
//                            if (!StringUtils.isEmpty(mobile)) {
//                                rank += isContactInMobile(log.getUid(), mobile);
//                            }
//
//                            if (null != feed.getLatlng() && !"".equals(feed.getLatlng()) && !"0.0,0.0".equals(feed
//                                    .getLatlng())) {
//                                String city = CityLatlngUtil.findCity(feed.getLatlng());
//                                if (StringUtils.isEmpty(city)) {
//                                    city = GaodeUtil.latlngToPosi(httpUtil, regeoUrl, feed.getLatlng());
//                                }
//
//                                rl.setCity(city);
//                                rl.setLatlng(feed.getLatlng());
//                            } else {
//
//                                String location = GaodeUtil.posiToLatlng(httpUtil, geoUrl, feed.getCity());
//
//                                rl.setLatlng(location);
//
//                                String city = feed.getCity();
//
//                                if (!StringUtils.isEmpty(location)) {
//                                    city = CityLatlngUtil.findCity(location);
//                                    if (StringUtils.isEmpty(city)) {
//                                        city = GaodeUtil.latlngToPosi(httpUtil, regeoUrl, location);
//                                    }
//                                }
//
//                                if (StringUtils.isEmpty(city)) {
//                                    city = feed.getCity();
//                                }
//                                rl.setCity(city);
//
//                            }
//
//                            rl.setRank(rank);
//                            logger.info("executing the {} element", times);
//                            rankedLogs.add(rl);
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            rankedLogs = new ArrayList<>();
//            logger.error("convertToRankLog error", e);
//        }
//        logger.info("convertToRankLog find {} result", rankedLogs.size());
//
//        return rankedLogs;
//    }

//    /**
//     * 计算用户标签与约见标签的权重值打分
//     *
//     * @param user
//     * @return
//     */
//
//    private int calcTag(com.demai.common.bean.User user, Feed feed) {
//        int rank = 0;
//        try {
//            List<String> tags = new ArrayList<>();
//            if (user.getMytag() != null) {
//                tags.addAll(user.getMytag());
//            }
//            if (user.getMy_goodat_key() != null) {
//                tags.addAll(user.getMy_goodat_key());
//            }
//            if (user.getBookdoing() != null) {
//                tags.addAll(user.getBookdoing());
//            }
//            if (user.getBookdone() != null) {
//                tags.addAll(user.getBookdone());
//            }
//            if (user.getBookwantdo() != null) {
//                tags.addAll(user.getBookwantdo());
//            }
//            if (user.getMoviewantdo() != null) {
//                tags.addAll(user.getMoviewantdo());
//            }
//            if (user.getMoviedone() != null) {
//                tags.addAll(user.getMoviedone());
//            }
//
//            if (tags != null && tags.size() > 0) {
//                if (feed.getBrief() != null && feed.getBrief().size() > 0) {//自动提取
//                    rank += matchTag(feed.getBrief(), tags, Constants.RankValue.EXTRACT_TAG);
//                }
//                if (feed.getCategory() != null && feed.getCategory().size() > 0) {//运营标签
//                    rank += matchTag(feed.getCategory(), tags, Constants.RankValue.OPERATION_TAG);
//                }
//                if (feed.getAttribute() != null && feed.getAttribute().size() > 0) {//用户标签
//                    rank += matchTag(feed.getAttribute(), tags, Constants.RankValue.USER_TAG);
//                }
//            }
//        } catch (Exception e) {
//            rank = 0;
//            logger.error("calcTag error", e);
//        }
//        return rank;
//    }

//    /**
//     * 比较并计算tag值
//     *
//     * @param src   用户标签
//     * @param desc  约见标签
//     * @param value 权重值
//     * @return
//     */
//    private int matchTag(List<String> src, List<String> desc, int value) {
//        int rank = 0;
//        try {
//            StringBuffer target = new StringBuffer();
//
//            Set<String> tmpSet = new HashSet<>();
//            List<String> list3 = new ArrayList<>();
//
//            if (src.size() >= desc.size()) {
//                tmpSet.addAll(src);
//                list3 = desc;
//            } else {
//                tmpSet.addAll(desc);
//                list3 = src;
//            }
//
//            for (String i : list3) {
//                if (tmpSet.contains(i)) {
//                    if (!"".equals(i)) {
//                        target.append(i).append(",");
//                    }
//                }
//            }
//
//            if (target.length() > 0) {
//                rank = target.length() * value;
//            }
//        } catch (Exception e) {
//            rank = 0;
//            logger.error("matchTag error", e);
//        }
//
//        return rank;
//    }


    /**
     * 双向好友rank计算-uid和desc是否是双向好友
     *
     * @param uid  贴子owner uid
     * @param desc 要被推荐的人 uid
     * @return
     */
//    private int friendsRank(Long uid, Long desc) {
//        int rank = 0;
//        try {
//            String relation = activeRedisUtil.getRedisMap(uid + ".friends", desc + "");
//            if (!StringUtils.isEmpty(relation)) {
//                rank = Constants.RankValue.FRIENDS;
//            }
//        } catch (Exception e) {
//            rank = 0;
//            logger.error("friendsRank error", e);
//        }
//
//        return rank;
//    }
//
//
//    /**
//     * 二度人脉rank计算
//     *
//     * @param uid  贴子owner uid
//     * @param desc 要被推荐的人 uid
//     * @return
//     */
//    private int secondFriendsRank(Long uid, Long desc) {
//        int rank = 0;
//        try {
//
//            Map<String, String> srcFriends = secondFriendsCache.get(uid);
//            if (srcFriends == null || srcFriends.size() == 0) {
//                srcFriends = activeRedisUtil.getRedisMap(uid + ".friends");
//                secondFriendsCache.put(uid, srcFriends);
//            }
//
//            Map<String, String> descFriends = secondFriendsCache.get(desc);
//
//            if (descFriends == null || descFriends.size() == 0) {
//                descFriends = activeRedisUtil.getRedisMap(desc + ".friends");
//                secondFriendsCache.put(desc, descFriends);
//            }
//
//            if (srcFriends != null && descFriends != null) {
//                for (Map.Entry<String, String> entry : srcFriends.entrySet()) {
//                    if (descFriends.containsKey(entry.getKey())) {
//                        rank += Constants.RankValue.SECOND_FRIENDS;
//                    }
//                }
//            }
//
////            String key = uid + "-" + desc;
////            if (secondFriendsCache.containsKey(key)) {//缓存中是否有数据
////                rank += secondFriendsCache.get(key);
////            } else {
////                int result = followsService.findSecond(uid, desc);
////                if (result > 0) {
////                    rank = Constants.RankValue.SECOND_FRIENDS;
////                }
////                secondFriendsCache.put(key, Long.parseLong(rank + ""));
////            }
//
//        } catch (Exception e) {
//            rank = 0;
//            logger.error("secondFriendsRank error", e);
//        }
//        return rank;
//    }


//    /**
//     * 查询desc的通讯录中是否有uid的手机号
//     *
//     * @return
//     */
//    private int isContactInMobile(Long uid, String mobile) {
//        int rank = 0;
//        try {
//            mongoUtil.setDbName("chat");
//            mongoUtil.setCollectionName("contacts");
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("uid", uid);
//            params.put("mobile", mobile);
//
//            List<DBObject> lists = mongoUtil.query(params);
//
//            if (lists != null && lists.size() > 0) {
//                rank = Constants.RankValue.MOBILE_FRIENDS;
//            }
//
//        } catch (Exception e) {
//            rank = 0;
//            logger.error("isContactInMobile error", e);
//        }
//        return rank;
//    }

    /**
     * 推荐记录到mysql
     *
     * @return
     */
    private void record(List<RankedLog> logs) {

        try {
            logger.info("start to record data to mysql");

            if (logs != null && logs.size() > 0) {
                //List<RankedLog> result = mixData(logs);
                //if (result != null && result.size() > 0) {

                //rankedLogService.deleteAll();//删除所有数据，录入重新计算的数据
                if (logs.size() > MYSQL_LIMIT) {

                    int size = logs.size();

                    for (int i = 0; i < size / MYSQL_LIMIT; i++) {
                        List<RankedLog> list1 = logs.subList(i *
                                MYSQL_LIMIT, (i + 1) * MYSQL_LIMIT);

                        rankedLogService.saveBatch(list1);//批量保存到mysql
                    }
                    int leftCount = size % MYSQL_LIMIT;
                    if (leftCount > 0) {
                        List<RankedLog> list1 = logs.subList(size - leftCount, size);
                        rankedLogService.saveBatch(list1);//批量保存到mysql
                    }
                } else {
                    rankedLogService.saveBatch(logs);//批量保存到mysql
                }
                logger.info("{} records saved to mysql", logs.size());
//                } else {
//                    logger.info("0 records saved to mysql", result.size());
//                }
            } else {
                logger.info("0 records saved to mysql");
            }


        } catch (Exception e) {
            logger.error("record error", e);
        }
    }


    /**
     * 入库数据去重
     */
//    private List<RankedLog> mixData(List<RankedLog> descData) {
//        logger.info("start to mixData");
//
//        List<RankedLog> result = new ArrayList<>();
//        try {
//            result.addAll(descData);
//            int start = 0;
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("type", 0);
//            params.put("start", start);
//            params.put("rows", MYSQL_LIMIT);
//
//            List<RankedLog> oriData = new ArrayList<>();
//
//            List<RankedLog> tempList = rankedLogService.findLogs(params);
//            //分批查询
//            if (tempList != null) {
//                oriData.addAll(tempList);
//                while (tempList.size() >= MYSQL_LIMIT) {
//                    start += MYSQL_LIMIT;
//                    params.put("start", start);
//                    tempList = rankedLogService.findLogs(params);
//                    result.addAll(tempList);
//                }
//            }
//
//
//            if (oriData != null && oriData.size() > 0) {
//                if (descData != null && descData.size() > 0) {
//
//                    Map<Long, Map<Long, RankedLog>> oriMap = new ConcurrentHashMap<>();//objectId-->uid-->rankedlog
//
//                    for (RankedLog log : oriData) {
//                        Map<Long, RankedLog> logMap = oriMap.get(log.getObjectId());
//                        if (logMap == null || logMap.size() == 0) {
//                            logMap = new HashMap<>();
//                        }
//                        logMap.put(log.getUid(), log);
//                        oriMap.put(log.getObjectId(), logMap);
//                    }
//
//                    for (RankedLog log : descData) {
//
//                        if (oriMap.containsKey(log.getObjectId())) {
//                            Map<Long, RankedLog> logMap = oriMap.get(log.getObjectId());
//                            if (logMap.containsKey(log.getUid())) {
//                                result.remove(log);
//                            }
//                        }
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            result = descData;
//            logger.info("mixData error", e);
//        }
//
//        return result;
//    }


    /**
     * 查寻发贴人的手机号,返回map  uid-->mobile
     */
    private Map<Long, String> findOwnerMobile(List<Long> ownerIdList) {
        logger.info("start to findOwnerMobile");
        Map<Long, String> result = new ConcurrentHashMap<>();
        try {
            if (ownerIdList != null && ownerIdList.size() > 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("uids", ownerIdList);
                List<User> users = mysqlUserService.findByParam(params);

                if (users != null && users.size() > 0) {
                    for (User u : users) {
                        if (!StringUtils.isEmpty(u.getMobile())) {
                            result.put(u.getTkey(), u.getMobile());
                        }
                    }
                }
            }
        } catch (Exception e) {
            result = new ConcurrentHashMap<>();
            logger.error("findOwnerMobile error", e);
        }
        return result;
    }

    public void run(List<RecommendLog> logs) {
        try {
            long beginTime = System.currentTimeMillis();

            logger.info("RankUtil run starts at {}", beginTime);

            secondFriendsCache = new HashMap<>();//清除缓存

            //分拆有效的约见推荐数据，分别得到meetid列表和用户id列表

            //List<Long> uids = getActivedUids();//三个月内活跃用户

//            Map<String, Object> params = new HashMap<>();
//            params.put("type", 0);
//            params.put("forbidden", Constants.LogStatus.NO);
//            params.put("expired", Constants.LogStatus.NO);
//
//            long count = recommendLogService.findLogsCount(params);//recommend_log表中的记录数
//
//            long loopTimes = count % MYSQL_LIMIT > 0 ? count / MYSQL_LIMIT : count / MYSQL_LIMIT + 1;
//
//            if (times == 0) {
//                rankedLogService.deleteAll();
//            }

            //for (int i = 0; i < loopTimes; i++) {

            //List<RecommendLog> logs = findRecommendLogs(MYSQL_LIMIT * i, MYSQL_LIMIT);

            distinctUsersAndLogs(logs);

            //过滤掉在一段时间内没有登录过用的用户
            //查询所有有效约见信息
            Map<Long, Feed> feedMap = findMeetsDetail(leftMeetIds);

            //Map<Long, com.demai.common.bean.User> userMap = findUsers(leftUids);

           // Map<Long, String> mobileMapping = findOwnerMobile(calcFeedOwnerIdList(feedMap));

            //record(convertToRankLog(logs, feedMap, mobileMapping, userMap));
            Action task = new Action(logs, feedMap,
                     httpUtil, regeoUrl, geoUrl,
                    activeRedisUtil, mongoUtil, followsService);

//            int poolSize = 100;
//            if (logs != null && logs.size() > 0) {
//                poolSize = logs.size() / 100 + 1;
//            }

            //ForkJoinPool pool = new ForkJoinPool();
            logger.info("start to invoke task");
            PoolUtil.pool.invoke(task);

//                if (i == 0) {
//                    rankedLogService.deleteAll();
//                }

            record(task.getRawResult());

            logger.info("task result is {}", task.getRawResult().size());
            task = null;
            secondFriendsCache = new HashMap<>();//清除缓存
            //}
            long endTime = System.currentTimeMillis();
            logger.info("RankUtil run ends at {} and costs {} ms", endTime, endTime - beginTime);

        } catch (Exception e) {
            logger.error("RankUtil run error", e);
        }
    }

}
