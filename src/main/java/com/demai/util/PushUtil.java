package com.demai.util;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by dear on 16/3/9.
 */
@Component
public class PushUtil implements Runnable {


    private PushUtil() {

    }

    private final static Logger logger = LoggerFactory.getLogger(PushUtil.class);

    private HttpUtil httpUtil;

    private String url;

    public PushUtil(String url, String data, HttpUtil httpUtil) {
        this.url = url;
        this.data = data;
        this.httpUtil = httpUtil;
    }

    private String data;

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            //logger.info("url is {} data is {}",url,data);
            JSONObject result = httpUtil.requestPost(url, data);
            logger.info(result.toJSONString());
            //logger.info("push data {} result is {}",data,result);
        } catch (Exception e) {
            logger.error("push message error", e);
        }
    }
}
