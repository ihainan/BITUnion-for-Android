package bit.ihainan.me.bitunionforandroid.utils.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bit.ihainan.me.bitunionforandroid.BuildConfig;
import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Extra API based on BITUnion-Api-Service Project
 */
public class ExtraApi {
    public final static String TAG = ExtraApi.class.getSimpleName();
    public final static String BASE_API = "http://ali.ihainan.me:8080/api/";
    // public final static String BASE_API = "http://192.168.56.1:8080/api/";
    // public final static String BASE_API = "http://192.168.31.115:8080/api/";
    public final static String VERSION = "v2";
    public final static String ENDPOINT = BASE_API + VERSION;

    // Basic Authentication
    public static String BASIC_AUTH_USERNAME = "bitunion_app";
    public static String BASIC_AUTH_PASSWORD = "bitunion_api";

    public static boolean checkStatus(JSONObject response) {
        try {
            if (response.getInt("code") == 0) return true;
            else return false;
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON object " + response, e);
            return false;
        }
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
    public static void addFavorite(Context context, long tid, String subject, String author,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = ADD_FAVORITE_ENDPOINT;
        Log.i(TAG, "addFavorite >> " + url);

        Map<String, Object> parameters = new HashMap();
        parameters.put("app", context.getString(R.string.app_name));
        parameters.put("version", BuildConfig.VERSION_NAME);
        parameters.put("username", CommonUtils.decode(Global.username));
        parameters.put("tid", tid);
        parameters.put("subject", CommonUtils.decode(subject));
        parameters.put("author", CommonUtils.decode(author));

        makeRequest(Request.Method.POST, context, url,
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
    public static void delFavorite(Context context, long tid,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = ADD_FAVORITE_ENDPOINT + "/" + CommonUtils.encode(Global.username) + "/" + tid;
        Log.i(TAG, "delFavorite >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.DELETE, context, url,
                "DELETE_FAVOR", parameters, listener, errorListener);
    }

    /**
     * 获取收藏列表
     *
     * @param context       上下文
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getFavoriteList(Context context,
                                       long from, long to,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        String url = ADD_FAVORITE_ENDPOINT + "/list/" + CommonUtils.encode(Global.username) + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getFavoriteList >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
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
    public static void getFavoriteStatus(Context context, long tid,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        String url = ADD_FAVORITE_ENDPOINT + "/status/" + CommonUtils.encode(Global.username) + "/" + tid;
        Log.i(TAG, "getFavoriteStatus >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FAVOR_STATUS", parameters, listener, errorListener);
    }


    /* 关注相关接口 */
    public final static String FOLLOW_ENDPOINT = ENDPOINT + "/follow";

    /**
     * 添加对特定用户的关注
     *
     * @param context       上下文
     * @param following     被关注者
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void addFollow(Context context, String following,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        String url = FOLLOW_ENDPOINT;
        Log.i(TAG, "addFollow >> " + url);

        Map<String, Object> parameters = new HashMap();
        parameters.put("app", context.getString(R.string.app_name));
        parameters.put("version", BuildConfig.VERSION_NAME);
        parameters.put("follower", CommonUtils.decode(Global.username));
        parameters.put("following", CommonUtils.decode(following));
        makeRequest(Request.Method.POST, context, url,
                "ADD_FOLLOW", parameters, listener, errorListener);
    }

    /**
     * 删除对特定用户的关注
     *
     * @param context       上下文
     * @param following     被关注者
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void delFollow(Context context, String following,
                                 Response.Listener<JSONObject> listener,
                                 Response.ErrorListener errorListener) {
        String url = FOLLOW_ENDPOINT + "/" + CommonUtils.encode(Global.userSession.username) + "/" + CommonUtils.encode(following);
        Log.i(TAG, "delFollow >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.DELETE, context, url,
                "DELETE_FOLLOW", parameters, listener, errorListener);
    }

    /**
     * 获取对特定用户的关注状态（关注 / 不关注）
     *
     * @param context       上下文
     * @param following     被关注者
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getFollowStatus(Context context, String following,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        String url = FOLLOW_ENDPOINT + "/status/" + CommonUtils.encode(Global.userSession.username) + "/" + following;
        Log.i(TAG, "getFollowStatus >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FOLLOW_STATUS", parameters, listener, errorListener);
    }

    /**
     * 获取用户的关注列表
     *
     * @param context       上下文
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getFollowingList(Context context, long from, long to,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {
        String url = FOLLOW_ENDPOINT + "/list/" + CommonUtils.encode(Global.userSession.username) + "?" + "from=" + from + "&to=" + to;
        Log.i(TAG, "getFollowingList >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FOLLOWING_LIST", parameters, listener, errorListener);
    }


    /* 时间轴相关接口 */
    public final static String TIMELINE_ENDPOINT = ENDPOINT + "/timeline";

    /**
     * 获得指定用户的时间轴动态
     *
     * @param context       上下文
     * @param username      指定用户的用户名
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getSpecialUserTimeline(Context context, String username,
                                              long from, long to,
                                              Response.Listener<JSONObject> listener,
                                              Response.ErrorListener errorListener) {
        String url = TIMELINE_ENDPOINT + "/spec/" + CommonUtils.encode(username) + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getSpecialUserTimeline >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_SPEC_USER_TIMELINE", parameters, listener, errorListener);
    }

    /**
     * 获得所有关注用户的时间轴动态
     *
     * @param context       上下文
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getFocusTimeline(Context context, long from, long to,
                                        Response.Listener<JSONObject> listener,
                                        Response.ErrorListener errorListener) {

        String url = TIMELINE_ENDPOINT + "/focus/" + CommonUtils.encode(Global.username) + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getFocusTimeline >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FOCUS_TIMELINE", parameters, listener, errorListener);
    }

    /* 时间轴相关接口 */
    public final static String SEARCH_ENDPOINT = ENDPOINT + "/search";

    /**
     * 根据关键词搜索主题
     *
     * @param context       上下文
     * @param keyword       关键词
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void searchThreads(Context context, String keyword,
                                     long from, long to,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        String url = ENDPOINT + "/search/threads?key=" + CommonUtils.encode(keyword) + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchThreads >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH THREADS", parameters, listener, errorListener);
    }

    /**
     * 根据关键词搜索帖子
     *
     * @param context       上下文
     * @param keyword       关键词
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void searchPosts(Context context, String keyword,
                                   long from, long to,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = ENDPOINT + "/search/posts?key=" + CommonUtils.encode(keyword) + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchPosts >> " + url);

        Map<String, Object> parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH THREADS", parameters, listener, errorListener);
    }
}
