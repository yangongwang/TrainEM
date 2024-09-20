package DataCollection;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 获取天气情况
 * 1.拼接url
 * 2.访问浏览器
 * 3.解析json
 */
public class MakeWeather {
    public static String getWea (String lng,String lat) throws Exception {
        //拼接url
        String urlStr = "https://free-api.heweather.net/s6/weather/now?location=" + lng + "," + lat + "&key=" + "870d1cd893ab43b998a376c460369e10";
        //URL
        URL url = new URL(urlStr);
        //url可以连接服务端
        URLConnection conn = url.openConnection();
        //获取返回的值
        InputStream inputStream = conn.getInputStream();
        //转成string类型
        String response = IOUtils.toString(inputStream);
        //调用解析json
        return parseJson(response);
    }

    private static String parseJson(String response) {
        //定义天气和湿度的变量
        String wea = "";
        String hum = "";
        //null ""
        if (StringUtils.isNotEmpty(response)) {
            //解析json
            JSONObject jsonObject = JSON.parseObject(response);
            //获取HeWeather6
            JSONArray heWeather6 = jsonObject.getJSONArray("HeWeather6");
            //获取第0个元素
            JSONObject jsonObject1 = heWeather6.getJSONObject(0);
            //获取now
            JSONObject now = jsonObject1.getJSONObject("now");
            //获取cond_txt
            String cond_txt = now.getString("cond_txt");
            //获取wind_dir
            String wind_dir = now.getString("wind_dir");
            //获取hum
            hum = now.getString("hum");
            //判断获取cond_txt和wind_dir
            if (StringUtils.isNotEmpty(cond_txt)) {
                if (cond_txt.contains("雨")) {
                    wea = "雨";
                }else if (cond_txt.contains("雪")){
                    wea = "雪";
                }else if (cond_txt.contains("冰雹")){
                    wea = "冰雹";
                }else if (cond_txt.contains("浮尘")){
                    wea = "浮尘";
                }else if (cond_txt.contains("扬沙")){
                    wea = "扬沙";
                }else {
                    if (wind_dir.contains("风")) {
                        wea = "风";
                    }else{
                        wea = "晴";
                    }
                }
            }
        }
        return wea+"|"+hum;
    }
}
