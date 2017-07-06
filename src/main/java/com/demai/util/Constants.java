package com.demai.util;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dear on 16/3/21.
 */
public class Constants implements Serializable {


    //状态
    public static class LogStatus {
        public final static int YES = 1;
        public final static int NO = 0;
    }

    //推荐rank值计算--单项权重值
    public static class RankValue {
        public final static int FRIENDS = 10;//双向好友
        public final static int SECOND_FRIENDS = 10;//二度人脉
        public final static int MOBILE_FRIENDS = 8;//通讯录好友
        public final static int OPERATION_TAG = 5;//运营标签
        public final static int EXTRACT_TAG = 1;//自动提取标签
        public final static int USER_TAG = 2;//用户标签
    }

    //数据来源方式
    public static class LogSource {
        public final static int CALCED = 0;//计算
        public final static int MAN = 1;//人工
    }

    public static void main(String[] args) {

        // 39.937926,116.103674
        List<Long> logs = new ArrayList<>();
        logs.add(1l);
        logs.add(2l);
        logs.add(3l);
        logs.add(4l);
        logs.add(5l);
        logs.add(6l);
        logs.add(7l);

        if (logs.size() > 2) {
            int mid = logs.size() / 2;
            System.out.println(mid);
            List<Long> list1 = new ArrayList<>(logs.subList(0, mid));
            for (Long l : list1) {
                System.out.println(l);
            }
            System.out.println("----");

            List<Long> list2 = new ArrayList<>(logs.subList(mid, logs.size()));
            for (Long l : list2) {
                System.out.println(l);
            }
        }

        DecimalFormat df = new DecimalFormat("#.00");
        df.setRoundingMode(RoundingMode.FLOOR);
        //double a = Math.floor(116.103674*100d)/100;
        System.out.println(df.format(116.106674));


        Map<Long, Map<String, String>> cacheMap = new ConcurrentHashMap<>();

        Map<String, String> tmpMap = new ConcurrentHashMap<>();
        tmpMap.put("123", "123");

        cacheMap.put(1l, tmpMap);

        for (Map.Entry<Long, Map<String, String>> entry : cacheMap.entrySet()) {
            System.out.println(entry.getKey());
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                System.out.println(entry1.getKey() + "==" + entry1.getValue());
            }
        }



        Map<Long, Map<String, String>> cacheMap1 = new ConcurrentHashMap<>();
        Map<String, String> tmpMap1 = new ConcurrentHashMap<>();
        tmpMap1.put("456", "456");
        cacheMap1.put(1l,tmpMap1);

        cacheMap.putAll(cacheMap1);

        for (Map.Entry<Long, Map<String, String>> entry : cacheMap.entrySet()) {
            System.out.println(entry.getKey());
            for (Map.Entry<String, String> entry1 : entry.getValue().entrySet()) {
                System.out.println(entry1.getKey() + "==" + entry1.getValue());
            }
        }


    }

    public static boolean containsDuplicate(int[] nums) {

        if (nums.length > 0) {

            Map<Integer, Integer> map = new ConcurrentHashMap<>();

            for (int i : nums) {
                Integer num = Integer.valueOf(i);
                if (map.get(num) != null) {
                    System.out.println(num);
                    return true;
                } else {
                    map.put(num, num);
                }
            }
        }

        return false;
    }
}
