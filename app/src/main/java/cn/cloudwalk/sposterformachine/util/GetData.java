package cn.cloudwalk.sposterformachine.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Jing on 16/5/4.
 */
public class GetData {
    /**
     * TODO 传入数据库的路径，需要post给(何春节)数据接口的map数据,数据接口返回一个json对象
     *
     * @param urlServer
     *            数据库路径
     * @param map
     *            需要提交的数据
     * @return JSONObject
     * @throws ClientProtocolException
     * @throws IOException
     * @throws JSONException
     */
    @SuppressWarnings("unused")
    public static JSONObject post(String urlServer, HashMap<String, String> map)
            throws ClientProtocolException, IOException, JSONException {
        if (urlServer != null) {
            HttpClient httpclient = new DefaultHttpClient();
            httpclient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
            // 设置通信协议版本
            httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpPost httppost = new HttpPost(urlServer);

            LinkedList<NameValuePair> param = new LinkedList<NameValuePair>();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {

                Map.Entry<String, String> entry = iterator.next();
                param.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

            }

            httppost.setEntity(new UrlEncodedFormEntity(param, "utf-8"));

            HttpResponse response = httpclient.execute(httppost);

            HttpEntity resEntity = response.getEntity();

            String json = "";
            JSONObject jb1 = null;
            if (resEntity != null) {
                json = EntityUtils.toString(resEntity, "utf-8");

                Log.d("TAG", json + "");
                jb1 = new JSONObject(json);
                Log.d("TAG", "" + jb1);
            }
            if (resEntity != null) {
                resEntity.consumeContent();
            }

            httpclient.getConnectionManager().shutdown();
            return jb1;
        }
        return null;
    }
}
