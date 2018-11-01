package chn.wu.jianhui.speed_detection.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import java.io.UnsupportedEncodingException;
import cz.msebera.android.httpclient.Header;

/*
* @author W.J.H
* @email jianhui.wu.chn@hotmail.com
* @create at 2018/10/27
*/
public class AmapServiceUtils
{
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    private Handler handler;
    private AsyncHttpClient httpClient = new AsyncHttpClient();

    public AmapServiceUtils(Handler handler){
        this.handler = handler;
    }

    /*
    * 创建服务
    * @author W.J.H
    * @email jianhui.wu.chn@hotmail.com
    * @create at 2018/10/27
    */
    public void startService(String key, String name)
    {
        RequestParams params = new RequestParams();
        params.put("key", key);
        params.put("name", name);
        httpClient.post("https://tsapi.amap.com/v1/track/service/add", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try
                {
                    JSONObject json = JSON.parseObject(new  String(responseBody, "UTF8"));
                    int serviceId = json.getJSONObject("data").getInteger("sid");
                    Bundle bundle = new Bundle();
                    bundle.putInt("serviceId", serviceId);
                    Message msg = new Message();
                    msg.what = SUCCESS;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = ERROR;
                    handler.sendMessage(msg);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Message msg = new Message();
                msg.what = ERROR;
                handler.sendMessage(msg);
            }
        });

    }

    /*
    * 查询服务
    * @author W.J.H
    * @email jianhui.wu.chn@hotmail.com
    * @create at 2018/10/28
    */
    public void searchService(String key)
    {
        RequestParams params = new RequestParams();
        params.put("key", key);
        httpClient.get("https://tsapi.amap.com/v1/track/service/list?key=" + key, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try
                {
                    JSONObject json = JSON.parseObject(new  String(responseBody, "UTF8"));
                    int serviceId = json.getJSONObject("data").getJSONArray("results").getJSONObject(0).getInteger("sid");
                    Bundle bundle = new Bundle();
                    bundle.putInt("serviceId", serviceId);
                    Message msg = new Message();
                    msg.what = SUCCESS;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = ERROR;
                    handler.sendMessage(msg);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Message msg = new Message();
                msg.what = ERROR;
                handler.sendMessage(msg);
            }
        });

    }
}
