package com.demai.util;

import com.demai.common.bean.Feed;
import com.demai.common.bean.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dear on 16/4/24.
 */
public class DataHolder {

    /**
     * 双向好友缓存
     */
    public static volatile Map<Long, Map<String, String>> friendsCache = new ConcurrentHashMap<>();


    /**
     * feedId-->city缓存
     */
    public static volatile Map<Long, String> citiesCache = new ConcurrentHashMap<>();


    /**
     * feedId-->经纬度缓存
     */
    public static volatile Map<Long, String> latlngCache = new ConcurrentHashMap<>();

    /**
     * 缓存最近活跃用户信息
     */

    public static volatile Map<Long, User> userMap = new ConcurrentHashMap<>();

    /**
     * 缓存feed信息
     */

    public static volatile Map<Long, Feed> feedMap = new ConcurrentHashMap<>();


    /**
     * 手机通讯录缓存  uid-->mobile-->value   case value >0 then has,case value ==0 then uid doesn't contact mobile
     */
    public static volatile Map<Long, Map<String, Integer>> contactCache = new ConcurrentHashMap<>();


    /**
     * mobile 及有此mobile的uids    mobile-->uid list
     */
    public static Map<String, List<Long>> mobileRelationCache = new ConcurrentHashMap<>();


    /**
     * 发贴人id -->发贴人手机号
     */
    public static Map<Long,String> mobileMapping = new ConcurrentHashMap<>();

}
