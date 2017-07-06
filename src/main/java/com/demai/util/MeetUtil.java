package com.demai.util;

import com.alibaba.fastjson.JSON;
import com.demai.common.bean.Feed;
import com.demai.common.bean.User;
import com.demai.common.utils.StringUtils;
import com.demai.entity.CityLatlng;
import com.demai.entity.RecommendLog;
import com.demai.entity.vo.ClientInfo;
import com.demai.entity.vo.MsgObject;
import com.demai.entity.vo.RequestVo;
import com.demai.service.*;
import com.demai.solr.feed.FeedService;
import com.demai.solr.user.UserService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dear on 16/2/16.
 * 流程：mongo中查找未过期且报名人数未满的约见，
 * 根据得到结果从solr中查找相关的约见信息，
 * 根据得到的约见信息在solr中查找相关的用户，进行推荐
 */
@Component
public class MeetUtil {

    private final static Logger logger = LoggerFactory.getLogger(MeetUtil.class);

    private MongoUtil mongoUtil = MongoUtil.getInstance();

    ExecutorService es = Executors.newFixedThreadPool(100);

    @Resource
    private UserService userService;

    @Resource
    private IRankedLogService rankedLogService;

    @Resource
    IRecommendLogService recommendLogService;

    @Resource
    private ICityLatlngService cityLatlngService;

    @Resource
    IFollowsService followsService;

    @Resource
    IUserService mysqlUserService;

    @Resource
    FeedService feedService;

    @Resource
    HttpUtil httpUtil;

    @Resource
    RankUtil rankUtil;

    @Resource
    ExpireUtil expireUtil;

    @Resource
    CityLatlngUtil cityLatlngUtil;

    @Resource
    RedisUtil activeRedisUtil;


    @Value("#{settings['recommend.api.url']}")
    private String url;


    private static Map<Long, List<Long>> oriData = new ConcurrentHashMap<>();


    private static final int LIMIT = 100;//分页每页条数


    private static final int MYSQL_LIMIT = 5000;//mysql 分页每页条数


    /**
     * 获取三个月内登录过的uid
     */
    private List<Long> getActivedUids() {
        long beginTime = System.currentTimeMillis();
        logger.info("rankUtil getActivedUids start at {}", beginTime);

        List<Long> uids = new ArrayList<>();

        try {

            Calendar cal = Calendar.getInstance();

            long now = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, -3);

            long before = cal.getTimeInMillis();

            uids = new ArrayList<>();
            Set<Tuple> members = activeRedisUtil.zrangeByScores("online_time", before, now);
            for (Tuple member : members) {
                uids.add(new Long(member.getElement()));
            }
            //logger.info("uids are {}", JSON.toJSONString(uids));
            logger.info("rankUtil getActivedUids find {} users", uids.size());
        } catch (Exception e) {
            uids = new ArrayList<>();
            logger.error("error occurred", e);
        }
        long endTime = System.currentTimeMillis();
        logger.info("rankUtil getActivedUids end at {} and costs {} ms", endTime, endTime - beginTime);

        return uids;
    }


    /**
     * 从solr中查找未过期且报名人数未满的约见
     *
     * @return List<Feed> 符合条件(未过期且报名人数未满)的约见集合
     */
    private List<Long> findMeetsId() {
        List<Long> targetTids = new ArrayList<>();//推送的约见id
        try {
            mongoUtil.setDbName("chat");
            mongoUtil.setCollectionName("feed");

            Map<String, Object> params = new HashMap<>();
            params.put("subtype", 20008);//subtype=20008 为约见

            Long now = System.currentTimeMillis() / 1000;//现在时间的unix 时间戳  1475913599L

            logger.info(now + "");
//            params.put("meetDates.1", new BasicDBObject("$elemMatch", new BasicDBObject("$gt",
//                    now)));

            logger.info("start to find meets from mongo with time {}", now);
            //meetDates为数组，第二个元素值应该为大于当时时间的值
            params.put("private", 0);//公开
            params.put("isdelete", 0);//未删除

            int start = 0;

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
                    boolean valid = false;
                    for (String str : meetDatesArray) {
                        if (!"false".equals(str.trim())) {
                            Long date = Long.parseLong(str.trim());
                            if (date > now) {
                                valid = true;
                            }
                        }
                    }

                    if (!valid) {
                        it.remove();
                    }
                }
            } catch (Exception e) {
                logger.error("filter expired meeting error", e);
            }

            logger.info("find meets from mongo result size is {}", result.size());
            List<DBObject> targetMeets = findApplyNum(result);

            for (DBObject dbObject : targetMeets) {
                Long tid = Long.parseLong(dbObject.get("uuid").toString());
                if (!targetTids.contains(tid)) {
                    targetTids.add(tid);
                }
            }

        } catch (Exception e) {
            logger.error("findMeetsId error", e);
            targetTids = new ArrayList<>();
        }

        return targetTids;
    }


    /**
     * 在目标lists中查找报名人数未满的约见
     * <p/>
     * 根据约见列表id,在报名记录collection中查寻报名人数，
     * 且与约见的最大人数对比，若报名人数未满，则继续下一步操作
     *
     * @param lists 未过期约见列表
     */
    private List<DBObject> findApplyNum(List<DBObject> lists) {
        List<DBObject> targetMeets = new ArrayList<>();
        try {
            mongoUtil.setDbName("chat");
            mongoUtil.setCollectionName("feedapply");//报名记录

            logger.info("start to findApplyNum from mongo ");
            BasicDBList tids = new BasicDBList();//约见id集合

            Map<Long, DBObject> meetMap = new ConcurrentHashMap<>();//约见map  id --> 约见
            for (DBObject dbObject : lists) {
                tids.add(dbObject.get("uuid"));
                meetMap.put(Long.parseLong(dbObject.get("uuid").toString()), dbObject);
            }

            BasicDBObject params = new BasicDBObject();
            params.put("tid", new BasicDBObject("$in", tids));
            params.put("status", 1);//status为1表示已经确认

            DBObject result = mongoUtil.group("tid", "count", params);//分组统计

            Map<Long, Long> countMap = new ConcurrentHashMap<>();//tid,count  e.g. {1111:10}

            Map<Long, Map<String, Object>> map = result.toMap();//result map  3={"tid":11111,"count":11.0}
            for (Map.Entry<Long, Map<String, Object>> entry : map.entrySet()) {
                Map<String, Object> value = entry.getValue();//{"tid":11111,"count":11.0}
                countMap.put(Long.parseLong(value.get("tid").toString()),
                        Math.round(Double.parseDouble(value.get("count").toString())));
            }


            //遍历匹配 清洗出报名人数未满的约见
            for (Map.Entry<Long, DBObject> entry : meetMap.entrySet()) {
                Long meetId = entry.getKey();//约见id
                DBObject meet = entry.getValue();//约见

                Long pernumber = Long.parseLong(meet.get("pernumber").toString());//约见最大人数
                Long confirmedCount = countMap.get(meetId);

                if (confirmedCount != null) {
                    if (confirmedCount < pernumber) {//报名确定人数 小于约见规定人数，则可以进行推送
                        targetMeets.add(meet);
                    }
                } else {
                    targetMeets.add(meet);
                }
            }

            logger.info("findApplyNum from mongo result size is {}", targetMeets.size());
        } catch (Exception e) {
            targetMeets = new ArrayList<>();
            logger.error("findApplyNum error", e);
        }

        return targetMeets;
    }


    /**
     * 根据约见id查询详细信息
     *
     * @param tids 约见id集合
     */
    private List<Feed> findMeetsDetail(List<Long> tids) {

        logger.info("start to findMeetsDetail from solr");

        Map<Long, Feed> feedMap = new ConcurrentHashMap<>();//feedId -->feed

        List<Feed> feeds = new ArrayList<>();//结果
        try {
            if (tids != null && tids.size() > 0) {//分批从solr里取数据
                int size = tids.size();

                if (size / LIMIT > 0) {//分批取
                    for (int i = 0; i < size / LIMIT; i++) {
                        feedMap.putAll(feedService.getListUserFeed(tids.subList
                                (i * LIMIT, (i + 1) * LIMIT)));
                    }
                    int leftCount = size % LIMIT;
                    if (leftCount > 0) {
                        feedMap.putAll(feedService.getListUserFeed(tids.subList
                                (size - leftCount, size)));
                    }
                } else {//一次取
                    feedMap.putAll(feedService.getListUserFeed(tids));
                }
            }

            Set<Feed> set = new HashSet<>(feedMap.values());

            feeds = new ArrayList<>(set);//把map转为feed list

            logger.info("findMeetsDetail from solr result size is {}", feeds.size());
        } catch (Exception e) {
            logger.error("findMeetsDetail error", e);
            feeds = new ArrayList<>();
        }

        return feeds;
    }


    /**
     * 查找约见推荐的相关用户
     *
     * @param feeds
     */
    private Map<Long, List<User>> findRecommendUser(List<Feed> feeds) {
        logger.info("start to findRecommendUser from solr");

        Map<Long, List<User>> result = new ConcurrentHashMap<>();//meedId -->userList(推荐的用户列表)
        int start = 0;

        List<Long> emptyUserAttributes = new ArrayList<>();

        int solr_limit = 600;

        int loopTimes = 0;

        try {
            if (feeds != null && feeds.size() > 0) {
                for (Feed feed : feeds) {

                    loopTimes++;
                    logger.info("looping the {} times", loopTimes);
                    start = 0;

                    //约见没有标签，直接跳过
                    if ((feed.getBrief() == null || feed.getBrief().size() == 0) && (feed.getAttribute() == null || feed
                            .getAttribute().size() == 0) && (feed.getCategory() == null || feed.getCategory().size() == 0)) {
                        //logger.info("meetId {} 's tag are all 'null',will not recommend to any user", feed.getId());
                        emptyUserAttributes.add(feed.getId());
                        continue;
                    } else {
                        //分批获取meetId 相对应的推荐用户信息
                        List<User> users = userService.findRecommendUser(feed.getUid(), feed.getCategory(), feed
                                .getAttribute(), feed
                                .getBrief(), start, solr_limit);
                        List<User> userResult = new ArrayList<>();
                        int times = 0;
                        if (users != null) {
                            userResult.addAll(users);
                            while (users.size() >= solr_limit) {//start <1 最多取200用户做推荐
                                start += solr_limit;
                                users = userService.findRecommendUser(feed.getUid(), feed.getCategory(), feed.getAttribute(), feed
                                        .getBrief(), start, solr_limit);
                                times++;
                                logger.info("executing {} times", times);
                                if (users != null) {
                                    userResult.addAll(users);
                                }

                            }
                            result.put(feed.getId(), userResult);
                        }
                    }
                }
            }

            logger.info("meets without brief or attribute or category size is {}", emptyUserAttributes.size());
            logger.info("findRecommendUser from solr result size is {}", result.size());
        } catch (Exception e) {
            result = new ConcurrentHashMap<>();
            logger.info("findRecommendUser error", e);
        }
        return result;
    }


    /**
     * 每次都只从mysql中查，redis只做短暂的存放
     */
    private Map<Long, List<Long>> filterDataAndPushToMemory(Map<Long, List<User>> map) {

        logger.info("start to filterDataAndPushToRedisWithoutRedis");
        Map<Long, List<Long>> tempResult = new ConcurrentHashMap<>();//meetId-->UserIdList

        Map<Long, List<Long>> result = new ConcurrentHashMap<>();//meetId-->UserIdList
        try {
            List<Long> userIds = new ArrayList<>();

            //遍历map,得到结果 meetId -->userIdList

            List<Long> objectIds = new ArrayList<>();

            for (Map.Entry<Long, List<User>> entry : map.entrySet()) {
                Long meetId = entry.getKey();

                if (!objectIds.contains(meetId)) {
                    objectIds.add(meetId);
                }

                List<User> users = entry.getValue();
                userIds = new ArrayList<>();
                for (User user : users) {
                    userIds.add(user.getId());
                }

                Set<Long> set = new HashSet<>(userIds);

                tempResult.put(entry.getKey(), new ArrayList<Long>(set));
            }

            if (objectIds.size() == 0) {//没有查询出约见 直接返回空
                return tempResult;
            }

            List<RecommendLog> logs = new ArrayList<>();
            try {
                logs = findRecommendLog(objectIds);//find recommended log list from mysql
            } catch (Exception e) {
                logs = new ArrayList<>();
                logger.error("findRecommendLog error", e);
            }
            if (logs != null && logs.size() > 0) {

                for (RecommendLog log : logs) {
                    Long meetId = log.getObjectId();

                    List<Long> userIdList = oriData.get(meetId);
                    if (userIdList == null) {
                        userIdList = new ArrayList<>();
                    }
                    if (!userIdList.contains(log.getUid())) {
                        userIdList.add(log.getUid());
                        oriData.put(meetId, userIdList);
                    }
                }
                logger.info("prepare to mix data ");
                result = mixData(tempResult);//清洗需要推荐的数据
            } else {
                logger.info("got no original data from mysql ");
                result.putAll(tempResult);
            }
        } catch (Exception e) {
            tempResult = new ConcurrentHashMap<>();
            logger.error("put result to redis error", e);
        }

        return result;
    }


    /**
     * 原redis数据与最新计算出的推荐数据进行对比，进行清洗数据，
     * 即，若原来推送过的数据不再进行推送，
     * 只保留还未推送过的数据，且与原数据混合到一起形成最新的数据
     *
     * @param tempResult
     * @return 清洗过后的最新推送数据
     */
    private Map<Long, List<Long>> mixData(Map<Long, List<Long>> tempResult) {
        Map<Long, List<Long>> result = new ConcurrentHashMap<>();

        try {

            logger.info("start to mixData ..");
            //Map<Long, List<Long>> originalData = redisUtil.getLongMap(KEY);//meetId-->userIdList

            //如果redis里数据不为空 则进行数据对比，如MeetId已经推送过给某位用户，则过滤
            if (oriData != null && !oriData.keySet().isEmpty()) {

                for (Map.Entry<Long, List<Long>> entry : tempResult.entrySet()) {//遍历新计算出的对应数据  feedId-->userIdList

                    Long meetId = entry.getKey();//feedId
                    List<Long> userIds = entry.getValue();//计算出的关系

                    List<Long> calcedUserIds = new ArrayList<>();//原计算结果不包含的用户id列表

                    if (userIds != null && userIds.size() > 0) {

                        List<Long> oriUserIds = oriData.get(meetId);//原来已推送关系

                        if (oriUserIds != null && oriUserIds.size() > 0) {
                            calcedUserIds.addAll(matchLongList(oriUserIds, userIds));
                        } else {
                            calcedUserIds.addAll(userIds);  //原来没有数据，则把最新计算的数据全部放到结果中
                        }
                        Set<Long> set = new HashSet<>(calcedUserIds);
                        result.put(meetId, new ArrayList<Long>(set));
                    }
                }
            } else {
                result.putAll(tempResult);
            }
            logger.info("mixData result size is {}", result.size());
        } catch (Exception e) {
            logger.error("mixData error", e);
        }
        return result;
    }


    /**
     * 查询mysql中所有的推荐记录
     *
     * @return List<RecommendLog>
     */
    private List<RecommendLog> findRecommendLog(List<Long> objectIds) {
        List<RecommendLog> result = new ArrayList<>();
        int start = 0;
        logger.info("start to findRecommendLog from mysql");
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("start", start);
            params.put("rows", MYSQL_LIMIT);
            params.put("type", 0);//0 为约见
            params.put("expired", Constants.LogStatus.NO);
            params.put("pushed", Constants.LogStatus.YES);
            params.put("objectIds", objectIds);

            List<RecommendLog> tempList = recommendLogService.findLogs(params);
            //分批查询
            if (tempList != null) {
                result.addAll(tempList);
                while (tempList.size() >= MYSQL_LIMIT) {
                    start += MYSQL_LIMIT;
                    params.put("start", start);
                    tempList = recommendLogService.findLogs(params);
                    result.addAll(tempList);
                }
            }
        } catch (Exception e) {
            logger.error("findRecommendLog error", e);
            result = new ArrayList<>();
        }
        logger.info("findRecommendLog from mysql result is {}", result.size());
        return result;
    }


    /**
     * 批量查询uid双向好友
     *
     * @param uids
     */
    private Map<Long, Map<String, String>> searchFriends(List<Long> uids) {

        Map<Long, Map<String, String>> resultMap = new ConcurrentHashMap<>();
        try {
            int start = 0;

            Map<String, Object> params = new HashMap<>();
            params.put("uids", uids);

            params.put("start", start);
            params.put("rows", MYSQL_LIMIT);

            Map<Long, Map<String, String>> tempMap = followsService.findFriends(params);//分批拿数据
            logger.info("findFriends with uid {}", uids);
            if (!tempMap.isEmpty()) {
                resultMap.putAll(tempMap);
                while (!tempMap.isEmpty()) {
                    start += MYSQL_LIMIT;
                    params.put("start", start);
                    tempMap = followsService.findFriends(params);
                    if (!tempMap.isEmpty()) {
                        resultMap.putAll(tempMap);
                    }
                }
            }


        } catch (Exception e) {
            resultMap = new ConcurrentHashMap<>();
            logger.error("searchFriends error", e);
        }
        return resultMap;
    }


    /**
     * 缓存好友关系
     *
     * @param uids
     */
    private void cacheFriends(List<Long> uids) {
        logger.info("start to cacheFriends");
        try {

            //for (Map.Entry<Long, List<Long>> entry : map.entrySet()) {
            //List<Long> userIdList = entry.getValue();


            if (uids != null && uids.size() > 0) {
//
//                for (Long userId : uids) {
//                    if (!DataHolder.friendsCache.containsKey(userId)) {
//                        uids.add(userId);
//                    }
//                }

                int threshold = 500;

                int size = uids.size();

                Map<Long, Map<String, String>> resultMap = new ConcurrentHashMap<>();

                if (size > threshold) {

                    int start = 0;

                    for (int i = 0; i < size / threshold; i++) {//分批计算
                        resultMap.putAll(searchFriends(uids.subList(i *
                                threshold, (i + 1) * threshold)));

                    }
                    int leftCount = size % threshold;
                    if (leftCount > 0) {
                        start = 0;
                        Map<String, Object> params = new HashMap<>();
                        resultMap.putAll(searchFriends(uids.subList(size - leftCount, size)));
                    }
                } else {
                    resultMap.putAll(searchFriends(uids));
                }

                //put to memory
                if (!resultMap.isEmpty()) {
                    for (Long uid : uids) {
                        if (!resultMap.containsKey(uid)) {//如果没有双向好友，则把自己放入缓存中，防止action.java
                            // friendsRank和secondFriendRank 方法再次查询，影响性能
                            Map<String, String> selfMap = new HashMap<>();
                            selfMap.put("" + uid, "" + uid);
                            DataHolder.friendsCache.put(uid, selfMap);
                        }
                    }
                    for (Map.Entry<Long, Map<String, String>> entry : resultMap.entrySet()) {
                        Long uid = entry.getKey();
                        if (DataHolder.friendsCache.containsKey(uid)) {
                            Map<String, String> tempMap = DataHolder.friendsCache.get(uid);
                            if (tempMap != null && !tempMap.isEmpty()) {
                                tempMap.putAll(resultMap.get(uid));
                                DataHolder.friendsCache.put(uid, tempMap);
                            }
                        } else {
                            DataHolder.friendsCache.put(uid, resultMap.get(uid));
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("cacheFriends error", e);
        }

    }

    /**
     * 推荐记录到mysql
     *
     * @return
     */
    private void record(Map<Long, List<Long>> map) {//meetI -->list<userId>

        try {
            logger.info("start to record data to mysql");
            Date now = new Date();
            final List<RecommendLog> logs = new ArrayList<>();
            for (Map.Entry<Long, List<Long>> entry : map.entrySet()) {
                Long meetId = entry.getKey();

                List<Long> userIdList = entry.getValue();
                for (Long userId : userIdList) {
                    RecommendLog log = new RecommendLog();
                    log.setType(0);
                    log.setUid(userId);
                    log.setCreateTime(now);
                    log.setObjectId(meetId);
                    log.setSource(Constants.LogSource.CALCED);
                    logs.add(log);

                }
            }

            if (logs != null && logs.size() > 0) {

                int size = logs.size();
                if (logs.size() > MYSQL_LIMIT) {
                    for (int i = 0; i < size / MYSQL_LIMIT; i++) {
                        List<RecommendLog> list1 = logs.subList(i *
                                MYSQL_LIMIT, (i + 1) * MYSQL_LIMIT);

                        recommendLogService.saveBatch(list1);//批量保存到mysql
                    }
                    int leftCount = size % MYSQL_LIMIT;
                    if (leftCount > 0) {
                        List<RecommendLog> list1 = logs.subList(size - leftCount, size);
                        recommendLogService.saveBatch(list1);//批量保存到mysql
                    }
                } else {
                    recommendLogService.saveBatch(logs);//批量保存到mysql
                }

                logger.info("{} records saved to mysql", logs.size());

                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        rankUtil.run(logs);
                    }
                });

            } else {
                logger.info("0 records saved to mysql");
            }


        } catch (Exception e) {
            logger.error("record error", e);
        }
    }


    /**
     * 查询用户信息
     *
     * @param uids
     * @return
     */
    private Map<Long, User> findUsers(List<Long> uids) {
        Map<Long, User> userMap = new HashMap<>();
        try {
            if (uids != null) {
                int size = uids.size();

                if (size / LIMIT > 0) {//分批取
                    for (int i = 0; i < size / LIMIT; i++) {
                        userMap.putAll(userService.findListUser(uids.subList
                                (i * LIMIT, (i + 1) * LIMIT)));
                    }
                    int leftCount = size % LIMIT;
                    if (leftCount > 0) {
                        userMap.putAll(userService.findListUser(uids.subList
                                (size - leftCount, size)));
                    }
                } else {//一次取
                    userMap.putAll(userService.findListUser(uids));
                }
            }
        } catch (Exception e) {
            userMap = new HashMap<>();
            logger.error("findUsers error", e);
        }
        logger.info("findUsers find {} users", userMap.size());
        return userMap;
    }


    private void cacheActiveUser(List<Long> uids) {
        try {

            if (uids != null && uids.size() > 0) {
                DataHolder.userMap.putAll(findUsers(uids));
            }

        } catch (Exception e) {
            logger.error("cacheActiveUser error", e);
        }

    }


    /**
     * 推送消息
     */
    public void pushMessage(Map<Long, List<Long>> map) {

        try {
            //httpUtil.requestPost()


            ClientInfo clientInfo = new ClientInfo();

            clientInfo.setApp(11);
            clientInfo.setChannel("recommend");
            clientInfo.setDevice_id("7A0C057F-0944-4EA9-B193-D1ACB439F607");
            clientInfo.setDevice_model("recommend");
            clientInfo.setType("android");
            clientInfo.setIp("127.0.0.1");
            clientInfo.setIsp("cmcc");
            clientInfo.setNetwork("Wifi");
            clientInfo.setVersion("4.8");
            clientInfo.setSsid("");


            logger.info("start to push message,param is {}", map.toString());

            Map<Feed, List<User>> result = getFullData(map);

            // List<MsgObject> lists = new ArrayList<>();

            if (result != null && !result.entrySet().isEmpty()) {
                for (Map.Entry<Feed, List<User>> entry : result.entrySet()) {
                    Feed feed = entry.getKey();
                    List<User> userList = entry.getValue();

                    MsgObject msg = new MsgObject();

                    for (User u : userList) {
                        msg.setPic(feed.getPic());
                        msg.setTitle(feed.getTitle());
                        msg.setAddress(feed.getCity());
                        msg.setBegintime(feed.getBegintime());
                        msg.setEndtime(feed.getEndtime());
                        msg.setTopic_id(feed.getId());
                        msg.setChatType("demai-appointment");
                        msg.setName(feed.getName());
                        msg.setUid(feed.getUid());

                        RequestVo rVo = new RequestVo();
                        rVo.setData_type(5);//推荐
                        rVo.setData(JSON.toJSONString(msg));
                        //rVo.setApptoken("demaitoken01");
                        rVo.setUid(65331);

                        //rVo.setRuid(u.getId());
                        rVo.setRuid(u.getId());

                        rVo.setVerifystr(MD5Util.string2MD5(JSON.toJSONString(msg)));

                        rVo.setClient_info(clientInfo);


                        es.submit(new PushUtil(url, JSON.toJSONString(rVo), httpUtil));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("push message error", e);
        }

    }


    private Map<Feed, List<User>> getFullData(Map<Long, List<Long>> map) {

        Map<Feed, List<User>> result = new HashMap<>();//约见id--> 对应feed-->对应用户列表

        try {

            List<Long> meetIds = new ArrayList<>();

            List<Long> userIds = new ArrayList<>();

            if (map != null && !map.entrySet().isEmpty()) {
                for (Map.Entry<Long, List<Long>> entry : map.entrySet()) {
                    Long meetId = entry.getKey();
                    List<Long> userList = entry.getValue();

                    if (!meetIds.contains(meetId)) {
                        meetIds.add(meetId);
                    }
                    userIds.addAll(userList);
                }
            }


            if (meetIds.size() > 0 && userIds.size() > 0) {

                Map<Long, Feed> feedMap = feedService.getListUserFeed(meetIds);//约见信息

                Set<Long> set = new HashSet<>(userIds);//用户Id去重

                List<Long> targetUsers = new ArrayList<>(set);//所有用户信息


                logger.info("getFullData find {} meets and {} users", meetIds.size(), targetUsers.size());

                Map<Long, User> userMap = new HashMap<>();

                int limit = 200;

                int size = targetUsers.size();
                if (size / limit > 0) {//分批取
                    for (int i = 0; i < size / limit; i++) {
                        userMap.putAll(userService.findListUser(targetUsers.subList
                                (i * limit, (i + 1) * limit)));
                    }
                    int leftCount = size % limit;
                    if (leftCount > 0) {
                        userMap.putAll(userService.findListUser(targetUsers.subList
                                (size - leftCount, size)));
                    }
                } else {//一次取
                    userMap.putAll(userService.findListUser(targetUsers));
                }

                if (feedMap == null || feedMap.isEmpty()) {
                    return result;
                }


                //遍历组装结果
                if (map != null && !map.entrySet().isEmpty()) {
                    for (Map.Entry<Long, List<Long>> entry : map.entrySet()) {
                        Long meetId = entry.getKey();
                        List<Long> userList = entry.getValue();


                        Feed feed = feedMap.get(meetId);

                        List<User> tmpUser = new ArrayList<>();

                        if (userList != null && userList.size() > 0) {
                            for (Long uid : userList) {
                                User u = userMap.get(uid);
                                if (u != null) {
                                    tmpUser.add(u);
                                }
                            }

                            if (feed != null && tmpUser.size() > 0) {
                                result.put(feed, tmpUser);
                            }
                        }
                    }
                }
            }

            logger.info("getFullData totally find {} result", result.size());

        } catch (Exception e) {
            logger.error("getFullData error", e);
        }
        return result;

    }


    private static List<Long> matchLongList(List<Long> ori, List<Long> desc) {
        List<Long> target = new ArrayList<>();
        try {
            for (Long i : desc) {
                if (!ori.contains(i)) {
                    target.add(i);
                }
            }

        } catch (Exception e) {
            logger.error("matchStringList error", e);
            target = new ArrayList<>();
        }

        return target;
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


    /**
     * 查寻发贴人的手机号,返回map  uid-->mobile
     */
    private  Map<Long, String>  findOwnerMobile(List<Long> ownerIdList) {
        logger.info("start to findOwnerMobile");
        Map<Long, String> result = new ConcurrentHashMap<>();
        try {

            DataHolder.mobileMapping = new ConcurrentHashMap<>();
            if (ownerIdList != null && ownerIdList.size() > 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("uids", ownerIdList);
                List<com.demai.entity.User> users = mysqlUserService.findByParam(params);

                if (users != null && users.size() > 0) {
                    for (com.demai.entity.User u : users) {
                        if (!StringUtils.isEmpty(u.getMobile())) {
                            result.put(u.getTkey(), u.getMobile());
                        }
                    }
                }
            }

            DataHolder.mobileMapping.putAll(result);
        } catch (Exception e) {
            result = new ConcurrentHashMap<>();
            logger.error("findOwnerMobile error", e);
        }
        return result;
    }


    /**
     * 缓存手机号和存有该手机号的uid
     *
     * @return
     */
    private void cacheContactRelation(List<String> mobiles) {
        int rank = 0;
        try {
            long beginTime = System.currentTimeMillis();

            logger.info("cacheContactRelation starts at {}", beginTime);

            DataHolder.mobileRelationCache = new ConcurrentHashMap<>();

            mongoUtil.setDbName("chat");
            mongoUtil.setCollectionName("contacts");

            if (mobiles != null && mobiles.size() > 0) {
                for (String mobile : mobiles) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("contactMobile", mobile);

                    List<DBObject> lists = mongoUtil.query(params);//mongo中查询
                    if (lists != null && lists.size() > 0) {
                        List<Long> uids = new ArrayList<>();

                        for (DBObject obj : lists) {
                            uids.add(Long.parseLong(obj.get("uid").toString()));
                        }
                        DataHolder.mobileRelationCache.put(mobile, uids);//缓存
                    } else {
                        DataHolder.mobileRelationCache.put(mobile, new ArrayList<Long>());//没有数据也缓存
                    }
                }
            }
            logger.info("cacheContactRelation finds {} result", DataHolder.mobileRelationCache.size());

            long endTime = System.currentTimeMillis();

            logger.info("cacheContactRelation ends at {} and costs {} ms", endTime, endTime - beginTime);
        } catch (Exception e) {
            rank = 0;
            logger.error("isContactInMobile error", e);
        }
    }

    public void run() {
        try {
            long beginTime = System.currentTimeMillis();

            logger.info("MeetUtil run starts at {}", beginTime);


            DataHolder.contactCache = new ConcurrentHashMap<>();
            DataHolder.friendsCache = new ConcurrentHashMap<>();
            DataHolder.citiesCache = new ConcurrentHashMap<>();
            DataHolder.latlngCache = new ConcurrentHashMap<>();
            DataHolder.userMap = new ConcurrentHashMap<>();
            DataHolder.feedMap = new ConcurrentHashMap<>();

            List<Feed> feeds = findMeetsDetail(findMeetsId());


            if (feeds != null && feeds.size() > 0) {
                for (Feed feed : feeds) {
                    DataHolder.feedMap.put(feed.getId(), feed);
                }
            }


            /**
             * 所有发贴人的手机号
             */
            Map<Long, String> mobileMapping = findOwnerMobile(calcFeedOwnerIdList(DataHolder.feedMap));

            List<String> mobiles = new ArrayList<>();

            for(Map.Entry<Long,String> entry : mobileMapping.entrySet()){
                mobiles.add(entry.getValue());
            }

            /**
             * 缓存手机号和存有该手机好的所有用户id(uid)
             */
            cacheContactRelation(mobiles);

            List<Long> uids = getActivedUids();

            cacheActiveUser(getActivedUids());

            cacheFriends(uids);

            recommendLogService.deleteUnPushed();
            rankedLogService.deleteAll();

//            if (feeds != null && feeds.size() > 0) {
//                for (int i = 0; i < feeds.size(); i++) {
//                    final Feed feed = feeds.get(i);
//                    List<Feed> list1 = new ArrayList<>();
//                    list1.add(feed);
//                    final Map<Long, List<Long>> result = filterDataAndPushToMemory(findRecommendUser(list1));
//                    es.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            record(result, feed);
//                        }
//                    });
//
//                }
//            }
            RecommendUserTask task = null;

            //ForkJoinPool pool = new ForkJoinPool();

            if (feeds != null && feeds.size() > 0) {

                int perSize = 10;

                int size = feeds.size();

                if (size / perSize > 0) {//分批取
                    for (int i = 0; i < size / perSize; i++) {

                        oriData = new ConcurrentHashMap<>();

                        List<Feed> list1 = feeds.subList(i *
                                perSize, (i + 1) * perSize);

                        task = new RecommendUserTask(list1, userService);

//                        int poolSize = 10;
//                        if (list1 != null && list1.size() > 0) {
//                            poolSize = list1.size() / 2 + 1;
//                        }
//
//                        pool = new ForkJoinPool(poolSize);
                        logger.info("start to invoke task");
                        PoolUtil.pool.invoke(task);

                        Map<Long, List<Long>> result = filterDataAndPushToMemory(task.getRawResult());
                        //cacheFriends(result);
                        record(result);
                    }
                    int leftCount = size % perSize;
                    if (leftCount > 0) {

                        oriData = new ConcurrentHashMap<>();

                        List<Feed> list1 = feeds.subList(size - leftCount, size);

                        task = new RecommendUserTask(list1, userService);

//                        int poolSize = 10;
//                        if (list1 != null && list1.size() > 0) {
//                            poolSize = list1.size() / 2 + 1;
//                        }
//
//                        pool = new ForkJoinPool(poolSize);
                        logger.info("start to invoke task");
                        PoolUtil.pool.invoke(task);

                        Map<Long, List<Long>> result = filterDataAndPushToMemory(task.getRawResult());

                        //cacheFriends(result);
                        record(result);
                    }
                } else {//一次取

                    oriData = new ConcurrentHashMap<>();

                    task = new RecommendUserTask(feeds, userService);

//                    int poolSize = 10;
//                    if (feeds != null && feeds.size() > 0) {
//                        poolSize = feeds.size() / 2 + 1;
//                    }
//                    pool = new ForkJoinPool(poolSize);
                    logger.info("start to invoke task");
                    PoolUtil.pool.invoke(task);

                    Map<Long, List<Long>> result = filterDataAndPushToMemory(task.getRawResult());
//                    cacheFriends(result);
                    record(result);
                }
            }


            task = null;

            expireUtil.run();

            insertCityLatlng();


            cityLatlngUtil.init();//重新加载city数据

            long endTime = System.currentTimeMillis();

            logger.info("meetUtil run ends at {} and costs {} ms", endTime, endTime - beginTime);


        } catch (Exception e) {
            logger.error("meetUtil run error", e);
        }
    }


    private void insertCityLatlng() {
        try {
            if (!CityLatlngUtil.tempCache.isEmpty()) {
                List<CityLatlng> cities = new ArrayList<>();

                for (Map.Entry<String, CityLatlng> entry : CityLatlngUtil.tempCache.entrySet()) {
                    cities.add(entry.getValue());
                }
                cityLatlngService.saveBatch(cities);

                CityLatlngUtil.tempCache.clear();
            }

        } catch (Exception e) {
            logger.error("insertCityLatlng error", e);
        }
    }


}
