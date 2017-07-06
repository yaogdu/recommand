package com.demai.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.demai.common.utils.StringUtils;
import com.demai.entity.CityLatlng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dear on 16/4/14.
 */
public class GaodeUtil {


    private static final Logger logger = LoggerFactory.getLogger(GaodeUtil.class);

    /**
     * 详细地址转坐标
     *
     * @param httpUtil
     * @param url
     * @param address
     * @return
     */
    public static String posiToLatlng(HttpUtil httpUtil, String url, String address) {
        String result = "";
        try {
            if(StringUtils.isEmpty(address)){
                return "";
            }

            url += address;

            if (CityLatlngUtil.unLocatedCache.containsKey(address)) {
                return result;
            }

            JSONObject jsonObject = httpUtil.request(url);

            if (jsonObject.getBoolean("success") == true) {
                if (jsonObject.get("key") != null
                        && !StringUtils.isEmpty(jsonObject.get("key").toString())) {
                    String keyData = jsonObject.get("key").toString();

                    JSONObject vo = JSONObject.parseObject(keyData);

                    String infocode = vo.getString("infocode");

                    if (!StringUtils.isEmpty(infocode) && "10000".equals(infocode)) {//10000 indicates successfully
                        // return the infomation

                        JSONArray geocodeArray = vo.getJSONArray("geocodes");

                        if (geocodeArray != null && geocodeArray.size() > 0) {
                            JSONObject voObject = (JSONObject) geocodeArray.get(0);
                            String location = voObject.getString("location");
                            if (!StringUtils.isEmpty(location)) {
                                String[] temp = location.split(",");
                                result = temp[1] + "," + temp[0];
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            result = "";
            logger.error("posiToLatlng error", e);
        }
        if(StringUtils.isEmpty(result)){
            CityLatlngUtil.unLocatedCache.put(address,1L);
        }
        return result;
    }

    /**
     * 坐标转详细地址
     */
    public static String latlngToPosi(HttpUtil httpUtil, String url, String latlng) {
        String result = "";
        try {
            //logger.info("start to latlng to posi with {}", latlng);


            if (StringUtils.isEmpty(latlng)) {
                return result;
            }

            String lat = latlng.split(",")[1];
            String lng = latlng.split(",")[0];
            url += lat + "," + lng;

            String afterFormat = FormatUtil.format(Double.parseDouble(lng)) + "," + FormatUtil.format(Double
                    .parseDouble(lat));

            if (CityLatlngUtil.tempCache.containsKey(afterFormat)) {
                result = CityLatlngUtil.tempCache.get(afterFormat).getProvince();
            } else {

                JSONObject jsonObject = httpUtil.request(url);

                if (jsonObject.getBoolean("success") == true) {
                    if (jsonObject.get("key") != null
                            && !StringUtils.isEmpty(jsonObject.get("key").toString())) {

                        String keyData = jsonObject.get("key").toString();

                        JSONObject vo = JSONObject.parseObject(keyData);

                        String infocode = vo.getString("infocode");

                        if (!StringUtils.isEmpty(infocode) && "10000".equals(infocode)) {

                            JSONObject regeocodeObject = vo.getJSONObject("regeocode");

                            if (regeocodeObject != null && !regeocodeObject.isEmpty()) {
                                JSONObject addressComponentObject = regeocodeObject.getJSONObject("addressComponent");
                                if (addressComponentObject != null && !addressComponentObject.isEmpty()) {
                                    String city = addressComponentObject.getString("city");
                                    if (!StringUtils.isEmpty(city)) {
                                        logger.info(addressComponentObject.toJSONString());
                                        result = city;
                                        CityLatlng cityLatlng = new CityLatlng();
                                        cityLatlng.setCity(city);
                                        cityLatlng.setProvince(addressComponentObject.getString("province"));
                                        cityLatlng.setLat(lat);
                                        cityLatlng.setLng(lng);
                                        cityLatlng.setArea("");
                                        CityLatlngUtil.tempCache.put(afterFormat, cityLatlng);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            result = "";
            logger.error("latlngToPosi error", e);
        }
        //logger.info("latlngToPosi findCity {} via {} ", result, latlng);
        return result;
    }
}
