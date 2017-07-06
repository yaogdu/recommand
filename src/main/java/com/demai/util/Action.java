package com.demai.util;

import com.demai.common.bean.Feed;
import com.demai.common.utils.StringUtils;
import com.demai.entity.RankedLog;
import com.demai.entity.RecommendLog;
import com.demai.service.IFollowsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.RecursiveTask;

/**
 * Created by dear on 16/4/22.
 */

public class Action extends RecursiveTask<List<RankedLog>> {

    private static final Logger logger = LoggerFactory.getLogger(Action.class);

    int threshold = 200;

    private List<RecommendLog> logs;
    private Map<Long, Feed> feedMap;
    private HttpUtil httpUtil;

    private String regeoUrl;

    private String geoUrl;

    private RedisUtil activeRedisUtil;

    private MongoUtil mongoUtil;

    private IFollowsService followsService;

    public Action(List<RecommendLog> logs, Map<Long, Feed> feedMap, HttpUtil httpUtil, String regeoUrl, String geoUrl, RedisUtil
            activeRedisUtil, MongoUtil mongoUtil, IFollowsService followsService) {
        this.logs = logs;
        this.feedMap = feedMap;
        this.httpUtil = httpUtil;
        this.regeoUrl = regeoUrl;
        this.geoUrl = geoUrl;
        this.activeRedisUtil = activeRedisUtil;
        this.mongoUtil = mongoUtil;
        this.followsService = followsService;
    }

    /**
     * The main computation performed by this task.
     */
    @Override
    protected List<RankedLog> compute() {
        List<RankedLog> rankedLogs = new ArrayList<>();
        try {
            //logger.info("start to compute");
            if (logs != null && logs.size() > 0) {
                if (logs.size() > threshold) {
                    int mid = logs.size() / 2;
                    List<RecommendLog> list1 = new ArrayList<>(logs.subList(0, mid));

                    Action action1 = new Action(list1, feedMap, httpUtil, regeoUrl, geoUrl,
                            activeRedisUtil, mongoUtil, followsService);

                    List<RecommendLog> list2 = new ArrayList<>(logs.subList(mid, logs.size()));

                    Action action2 = new Action(list2, feedMap, httpUtil, regeoUrl, geoUrl,
                            activeRedisUtil, mongoUtil, followsService);

                    action1.fork();
                    action2.fork();

                    List<RankedLog> rankedLogs1 = action1.join();
                    List<RankedLog> rankedLogs2 = action2.join();

                    rankedLogs.addAll(rankedLogs1);
                    rankedLogs.addAll(rankedLogs2);

                    action1 = null;
                    action2 = null;

                } else {
                    com.demai.common.bean.User user = null;

                    int rank = 0;

                    String mobile = "";

                    int friendRank = 0;

                    String city = "";

                    String location = "";

                    //logger.info("logs size {}", logs.size());
                    for (RecommendLog log : logs) {

                        RankedLog rl = new RankedLog();
                        long objectId = log.getObjectId();
                        Feed feed = feedMap.get(objectId);
                        if (feed != null) {
                            rl.setObjectId(objectId);
                            rl.setUid(log.getUid());
                            rl.setCity(feed.getCity());
                            rank = 0;
                            friendRank = 0;
                            location = "";
                            city = "";
                            user = null;

                            user = DataHolder.userMap.get(rl.getUid());
                            if (user != null) {
                                rank += calcTag(user, feed);//标签权重值打分
                            }
                            friendRank = friendsRank(feed.getUid(), log.getUid());
                            if (friendRank > 0) {
                                rank += friendRank;
                            } else {
                                //logger.info("second friends rank to be calced...");
                                rank += secondFriendsRank(feed.getUid(), log.getUid());
                            }

                            mobile = DataHolder.mobileMapping.get(feed.getUid());
                            if (!StringUtils.isEmpty(mobile)) {
                                rank += isContactInMobile(log.getUid(), mobile);
                            }

                            if (null != feed.getLatlng() && !"".equals(feed.getLatlng()) && !"0.0,0.0".equals(feed
                                    .getLatlng())) {

                                if (DataHolder.latlngCache.containsKey(feed.getId())) {
                                    rl.setLatlng(feed.getLatlng());
                                    rl.setCity(DataHolder.citiesCache.get(feed.getId()));

                                    logger.debug("get latlng and city from cache");
                                } else {
                                    logger.debug("get latlng and city from gaode");
                                    city = CityLatlngUtil.findCity(feed.getLatlng());
                                    if (StringUtils.isEmpty(city)) {
                                        city = GaodeUtil.latlngToPosi(httpUtil, regeoUrl, feed.getLatlng());
                                    }

                                    rl.setCity(city);
                                    rl.setLatlng(feed.getLatlng());

                                    DataHolder.latlngCache.put(feed.getId(), feed.getLatlng() == null ? "" : feed.getLatlng());
                                    DataHolder.citiesCache.put(feed.getId(), city == null ? "" : city);
                                }
                            } else {
                                if (DataHolder.latlngCache.containsKey(feed.getId())) {
                                    rl.setLatlng(feed.getLatlng());
                                    rl.setCity(DataHolder.citiesCache.get(feed.getId()));
                                    logger.debug("get latlng and city from cache");
                                } else {
                                    logger.debug("get latlng and city from gaode");
                                    location = GaodeUtil.posiToLatlng(httpUtil, geoUrl, feed.getCity());

                                    rl.setLatlng(location);

                                    city = feed.getCity();

                                    if (!StringUtils.isEmpty(location)) {
                                        city = CityLatlngUtil.findCity(location);
                                        if (StringUtils.isEmpty(city)) {
                                            city = GaodeUtil.latlngToPosi(httpUtil, regeoUrl, location);
                                        }
                                    }

                                    if (StringUtils.isEmpty(city)) {
                                        city = feed.getCity();
                                    }
                                    rl.setCity(city);
                                    DataHolder.latlngCache.put(feed.getId(), feed.getLatlng() == null ? "" : feed.getLatlng());
                                    DataHolder.citiesCache.put(feed.getId(), city == null ? "" : city);
                                }


                            }
                            rl.setRank(rank);
                            rankedLogs.add(rl);
                        }
                    }
                }

            }
        } catch (Exception e) {
            rankedLogs = new ArrayList<>();
            logger.error("convertToRankLog error", e);
        }
        //logger.info("convertToRankLog find {} result", rankedLogs.size());

        return rankedLogs;
    }


    /**
     * 计算用户标签与约见标签的权重值打分
     *
     * @param user
     * @return
     */

    private int calcTag(com.demai.common.bean.User user, Feed feed) {
        int rank = 0;
        try {
            List<String> tags = new ArrayList<>();
            if (user.getMytag() != null) {
                tags.addAll(user.getMytag());
            }
            if (user.getMy_goodat_key() != null) {
                tags.addAll(user.getMy_goodat_key());
            }
            if (user.getBookdoing() != null) {
                tags.addAll(user.getBookdoing());
            }
            if (user.getBookdone() != null) {
                tags.addAll(user.getBookdone());
            }
            if (user.getBookwantdo() != null) {
                tags.addAll(user.getBookwantdo());
            }
            if (user.getMoviewantdo() != null) {
                tags.addAll(user.getMoviewantdo());
            }
            if (user.getMoviedone() != null) {
                tags.addAll(user.getMoviedone());
            }

            if (tags != null && tags.size() > 0) {
                if (feed.getBrief() != null && feed.getBrief().size() > 0) {//自动提取
                    rank += matchTag(feed.getBrief(), tags, Constants.RankValue.EXTRACT_TAG);
                }
                if (feed.getCategory() != null && feed.getCategory().size() > 0) {//运营标签
                    rank += matchTag(feed.getCategory(), tags, Constants.RankValue.OPERATION_TAG);
                }
                if (feed.getAttribute() != null && feed.getAttribute().size() > 0) {//用户标签
                    rank += matchTag(feed.getAttribute(), tags, Constants.RankValue.USER_TAG);
                }
            }
        } catch (Exception e) {
            rank = 0;
            logger.error("calcTag error", e);
        }
        return rank;
    }

    /**
     * 比较并计算tag值
     *
     * @param src   用户标签
     * @param desc  约见标签
     * @param value 权重值
     * @return
     */
    private int matchTag(List<String> src, List<String> desc, int value) {
        int rank = 0;
        try {
            StringBuffer target = new StringBuffer();

            Set<String> tmpSet = new HashSet<>();
            List<String> list3 = new ArrayList<>();

            if (src.size() >= desc.size()) {
                tmpSet.addAll(src);
                list3 = desc;
            } else {
                tmpSet.addAll(desc);
                list3 = src;
            }

            for (String i : list3) {
                if (tmpSet.contains(i)) {
                    if (!"".equals(i)) {
                        target.append(i).append(",");
                    }
                }
            }

            if (target.length() > 0) {
                rank = target.length() * value;
            }
        } catch (Exception e) {
            rank = 0;
            logger.error("matchTag error", e);
        }

        return rank;
    }

    /**
     * 双向好友rank计算-uid和desc是否是双向好友
     *
     * @param uid  贴子owner uid
     * @param desc 要被推荐的人 uid
     * @return
     */
    private int friendsRank(Long uid, Long desc) {
        int rank = 0;
        try {
            Map<String, String> friends = DataHolder.friendsCache.get(uid);

            if (friends != null && friends.size() > 0) {//从缓存中取
                //logger.info("friendsRank from memory uid:{}", uid);
                String value = friends.get(desc + "");
                if (!StringUtils.isEmpty(value)) {
                    rank = Constants.RankValue.FRIENDS;
                }
            } else {//缓存中没有

                Map<String, String> result = followsService.findFriendsByUid(uid);
                if (result != null && result.size() > 0) {
                    String value = result.get(desc + "");
                    if (!StringUtils.isEmpty(value)) {
                        rank = Constants.RankValue.FRIENDS;
                    }
                } else {
                    result.put("" + uid, "" + uid);
                }

                DataHolder.friendsCache.put(uid, result);

//                }

//                String relation = activeRedisUtil.getRedisMap(uid + ".friends", desc + "");
//                if (!StringUtils.isEmpty(relation)) {
//
//                }
            }


        } catch (Exception e) {
            rank = 0;
            logger.error("friendsRank error", e);
        }

        return rank;
    }

    /**
     * 二度人脉rank计算
     *
     * @param uid  贴子owner uid
     * @param desc 要被推荐的人 uid
     * @return
     */
    private int secondFriendsRank(Long uid, Long desc) {
        int rank = 0;
        try {

            Map<String, String> srcFriends = DataHolder.friendsCache.get(uid);
            if (srcFriends == null || srcFriends.size() == 0) {
                //     srcFriends = activeRedisUtil.getRedisMap(uid + ".friends");
                //   if (srcFriends == null || srcFriends.size() == 0) {
                srcFriends = followsService.findFriendsByUid(uid);
                // }

                if (srcFriends.size() == 0) {
                    srcFriends.put("" + uid, "" + uid);
                }

                DataHolder.friendsCache.put(uid, srcFriends);
            }

            Map<String, String> descFriends = DataHolder.friendsCache.get(desc);

            if (descFriends == null || descFriends.size() == 0) {
//                descFriends = activeRedisUtil.getRedisMap(desc + ".friends");
//                if (descFriends == null || descFriends.size() == 0) {
                descFriends = followsService.findFriendsByUid(desc);
                //}

                if (descFriends.size() == 0) {
                    descFriends.put("" + desc, "" + desc);
                }

                DataHolder.friendsCache.put(desc, descFriends);
            }

            if (srcFriends != null && descFriends != null) {
                for (Map.Entry<String, String> entry : srcFriends.entrySet()) {
                    if (descFriends.containsKey(entry.getKey())) {
                        rank += Constants.RankValue.SECOND_FRIENDS;
                    }
                }
            }

//            String key = uid + "-" + desc;
//            if (secondFriendsCache.containsKey(key)) {//缓存中是否有数据
//                rank += secondFriendsCache.get(key);
//            } else {
//                int result = followsService.findSecond(uid, desc);
//                if (result > 0) {
//                    rank = Constants.RankValue.SECOND_FRIENDS;
//                }
//                secondFriendsCache.put(key, Long.parseLong(rank + ""));
//            }

        } catch (Exception e) {
            rank = 0;
            logger.error("secondFriendsRank error", e);
        }
        return rank;
    }

    /**
     * 查询desc的通讯录中是否有uid的手机号
     *
     * @return
     */
    private int isContactInMobile(Long uid, String mobile) {
        int rank = 0;
        try {

            List<Long> uids = DataHolder.mobileRelationCache.get(mobile);

            if (uids != null && uids.contains(uid)) {
                rank = Constants.RankValue.MOBILE_FRIENDS;
            }


//            if (DataHolder.contactCache.containsKey(uid)) {
//                Map<String, Integer> tmpMap = DataHolder.contactCache.get(uid);
//
//                if (tmpMap.containsKey(mobile)) {
//                    if (tmpMap.get(mobile) > 0) {
//                        rank = Constants.RankValue.MOBILE_FRIENDS;
//                    }
//                } else {
//                    mongoUtil.setDbName("chat");
//                    mongoUtil.setCollectionName("contacts");
//
//                    Map<String, Object> params = new HashMap<>();
//                    params.put("uid", uid);
//                    params.put("mobile", mobile);
//
//                    List<DBObject> lists = mongoUtil.query(params);
//
//                    int value = 0;//默认uid通讯录中没有此mobile
//
//                    if (lists != null && lists.size() > 0) {
//                        rank = Constants.RankValue.MOBILE_FRIENDS;
//                        value = 1;
//                    }
//                    tmpMap.put(mobile, value);
//                    DataHolder.contactCache.put(uid, tmpMap);
//                }
//
//            } else {//缓存中没有该uid
//                mongoUtil.setDbName("chat");
//                mongoUtil.setCollectionName("contacts");
//
//                Map<String, Object> params = new HashMap<>();
//                params.put("uid", uid);
//                params.put("contactMobile", mobile);
//
//                List<DBObject> lists = mongoUtil.query(params);
//
//                Map<String, Integer> tmpMap = new ConcurrentHashMap<>();
//
//                int value = 0;//默认uid通讯录中没有此mobile
//
//                if (lists != null && lists.size() > 0) {
//                    rank = Constants.RankValue.MOBILE_FRIENDS;
//                    value = 1;
//                }
//
//                tmpMap.put(mobile, value);
//                DataHolder.contactCache.put(uid, tmpMap);
//            }

        } catch (Exception e) {
            rank = 0;
            logger.error("isContactInMobile error", e);
        }
        return rank;
    }


}
