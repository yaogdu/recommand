package com.demai.util;

import com.demai.common.utils.StringUtils;
import com.demai.entity.CityLatlng;
import com.demai.service.ICityLatlngService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dear on 16/4/18.
 */
@Component
public class CityLatlngUtil {

    private final static Logger logger = LoggerFactory.getLogger(CityLatlngUtil.class);

    private static Map<String, String> dataHolder = new ConcurrentHashMap<>();

    public static Map<String, CityLatlng> tempCache = new ConcurrentHashMap<>();

    public static Map<String, Long> unLocatedCache = new ConcurrentHashMap<>();

    @Resource
    MeetUtil meetUtil;

    @Resource
    RankUtil rankUtil;

    @Resource
    ICityLatlngService cityLatlngService;

    public void init() {
        try {
            dataHolder = cityLatlngService.findAll();
            //meetUtil.run();
            logger.info("citylatlng util init successfully find {} result", dataHolder.size());
        } catch (Exception e) {
            logger.error("citylatlng util init error", e);
        }
    }


    /**
     * 根据经纬度查看城市
     *
     * @param latlng
     * @return
     */
    public static String findCity(String latlng) {
        String result = "";
        try {

            if (!StringUtils.isEmpty(latlng)) {

                String[] tempArray = latlng.split(",");

                String afterFormat = FormatUtil.format(Double.parseDouble(tempArray[0])) + "," + FormatUtil.format(Double
                        .parseDouble(tempArray[1]));
                result = dataHolder.get(afterFormat);
                //logger.info("findCity {} via formatted latlng {}", result, afterFormat);
            }
        } catch (Exception e) {
            logger.error("findCity error", e);
        }

        return result;
    }


}
