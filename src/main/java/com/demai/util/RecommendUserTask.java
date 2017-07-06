package com.demai.util;

import com.demai.common.bean.Feed;
import com.demai.common.bean.User;
import com.demai.solr.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;

/**
 * Created by dear on 16/4/22.
 */

public class RecommendUserTask extends RecursiveTask<Map<Long, List<User>>> {

    private static final Logger logger = LoggerFactory.getLogger(RecommendUserTask.class);

    int threshold = 2;

    private List<Feed> feeds;

    private UserService userService;

    private int MYSQL_LIMIT = 5000;


    public RecommendUserTask(List<Feed> feeds, UserService userService) {
        this.feeds = feeds;
        this.userService = userService;
    }

    private static Map<Long, List<Long>> oriData = new ConcurrentHashMap<>();

    /**
     * The main computation performed by this task.
     */
    @Override
    protected Map<Long, List<User>> compute() {
        Map<Long, List<User>> feedMap = new ConcurrentHashMap<>();
        try {
            //logger.info("start to compute");
            if (feeds != null && feeds.size() > 0) {
                if (feeds.size() > threshold) {
                    int mid = feeds.size() / 2;

                    List<Feed> list1 = new ArrayList<>(feeds.subList(0, mid));

                    RecommendUserTask action1 = new RecommendUserTask(list1, userService);

                    List<Feed> list2 = new ArrayList<>(feeds.subList(mid, feeds.size()));

                    RecommendUserTask action2 = new RecommendUserTask(list2, userService);

                    action1.fork();
                    action2.fork();

                    Map<Long, List<User>> feedMap1 = action1.join();
                    Map<Long, List<User>> feedMap2 = action2.join();

                    feedMap.putAll(feedMap1);
                    feedMap.putAll(feedMap2);

                    action1 = null;
                    action2 = null;

                } else {
//                    logger.info("start to findRecommendUser from solr");

                    //Map<Long, List<User>> result = new ConcurrentHashMap<>();//meedId -->userList(推荐的用户列表)
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
                                    if (users != null) {
                                        userResult.addAll(users);
                                        while (users.size() >= solr_limit) {//start <1 最多取200用户做推荐
                                            start += solr_limit;
                                            users = userService.findRecommendUser(feed.getUid(), feed.getCategory(), feed.getAttribute(), feed
                                                    .getBrief(), start, solr_limit);
                                            if (users != null) {
                                                userResult.addAll(users);
                                            }

                                        }
                                        feedMap.put(feed.getId(), userResult);
                                    }
                                }
                            }
                        }

//                        logger.info("meets without brief or attribute or category size is {}", emptyUserAttributes.size());
//                        logger.info("findRecommendUser from solr result size is {}", feedMap.size());
                    } catch (Exception e) {
                        feedMap = new ConcurrentHashMap<>();
                        logger.info("findRecommendUser error", e);
                    }

                }
            }
        } catch (Exception e) {
            feedMap = new ConcurrentHashMap<>();
            logger.error("convertToRankLog error", e);
        }
        // logger.info("findRecommendUser find {} result", feedMap.size());

        return feedMap;
    }


}
