package bit.ihainan.me.bitunionforandroid.utils.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bit.ihainan.me.bitunionforandroid.BuildConfig;
import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Extra API based on BITUnion-Api-Service Project
 */
public class ExtraApi {
    public final static String TAG = ExtraApi.class.getSimpleName();
    public final static String BASE_API = "http://192.168.31.115:8080/api/";
    public final static String VERSION = "v1";
    public final static String ENDPOINT = BASE_API + VERSION;

    /* 收藏相关接口 */
    public final static String ADD_FAVORITE_ENDPOINT = ENDPOINT + "/favorite";

    /**
     * 收藏帖子
     *
     * @param context       上下文
     * @param tid           贴子 ID
     * @param subject       帖子主题
     * @param author        帖子作者
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public final static void addFavorite(Context context, long tid, String subject, String author,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        Map<String, Object> parameters = new HashMap();
        parameters.put("app", context.getString(R.string.app_name));
        parameters.put("version", BuildConfig.VERSION_NAME);
        parameters.put("username", Global.userSession.username);
        parameters.put("tid", tid);
        parameters.put("subject", subject);
        parameters.put("author", author);

        makeRequest(Request.Method.POST, context, ADD_FAVORITE_ENDPOINT,
                "ADD_FAVOR", parameters, listener, errorListener);
    }

    /**
     * 删除帖子
     *
     * @param context       上下文
     * @param tid           贴子 ID
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public final static void delFavorite(Context context, long tid,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.DELETE, context, ADD_FAVORITE_ENDPOINT + "/" + Global.userName + "/" + tid,
                "DELETE_FAVOR", parameters, listener, errorListener);
    }


    /**
     * 获取收藏列表
     *
     * @param context       上下文
     * @param username      用户名
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public final static void getFavoriteList(Context context, String username,
                                             long from, long to,
                                             Response.Listener<JSONObject> listener,
                                             Response.ErrorListener errorListener) {
        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, ADD_FAVORITE_ENDPOINT + "/list/" + username + "?from=" + from + "&to=" + to,
                "GET_FAVOR_STATUS", parameters, listener, errorListener);
    }


    /**
     * 删除收藏状态
     *
     * @param context       上下文
     * @param tid           贴子 ID
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public final static void getFavoriteStatus(Context context, long tid,
                                               Response.Listener<JSONObject> listener,
                                               Response.ErrorListener errorListener) {
        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, ADD_FAVORITE_ENDPOINT + "/status/" + Global.userName + "/" + tid,
                "GET_FAVOR_STATUS", parameters, listener, errorListener);
    }


    /**
     * @param requestMethod 请求方法
     * @param context       上下文
     * @param url           请求 URL
     * @param tag           请求标签
     * @param parameters    请求参数
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    private static void makeRequest(final int requestMethod, final Context context, final String url, final String tag,
                                    final Map<String, Object> parameters,
                                    final Response.Listener<JSONObject> listener,
                                    final Response.ErrorListener errorListener) {
        RequestQueueManager.CustomJsonObjectRequest request = new RequestQueueManager.CustomJsonObjectRequest(requestMethod, url,
                new JSONObject(parameters), listener, errorListener);
        RequestQueueManager.getInstance(context).addToRequestQueue(request, tag);
    }
}
