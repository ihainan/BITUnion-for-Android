package me.ihainan.bu.app.utils.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import me.ihainan.bu.app.BuildConfig;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Feedback;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * Extra API based on BITUnion-Api-Service Project
 */
public class ExtraApi {
    private final static String TAG = ExtraApi.class.getSimpleName();
    private final static String BASE_API = "http://bu.ihainan.me:8080/api/";
    // public final static String BASE_API = "http://192.168.56.1:8080/api/";
    // public final static String BASE_API = "http://192.168.31.115:8080/api/";
    private final static String VERSION = "v2";
    private final static String ENDPOINT = BASE_API + VERSION;

    // Basic Authentication
    public static final String BASIC_AUTH_USERNAME = "bitunion_app";
    public static final String BASIC_AUTH_PASSWORD = "bitunion_api";

    public static boolean checkStatus(JSONObject response) {
        try {
            return response.getInt("code") == 0;
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
                                    final HashMap parameters,
                                    final Response.Listener<JSONObject> listener,
                                    final Response.ErrorListener errorListener) {
        RequestQueueManager.CustomJsonObjectRequest request = new RequestQueueManager.CustomJsonObjectRequest(requestMethod, url,
                new JSONObject(parameters), listener, errorListener);
        RequestQueueManager.getInstance(context).addToRequestQueue(request, tag);
    }

    /* 收藏相关接口 */
    private final static String ADD_FAVORITE_ENDPOINT = ENDPOINT + "/favorite";

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

        HashMap parameters = new HashMap<>();
        parameters.put("app", context.getString(R.string.app_name));
        parameters.put("version", BuildConfig.VERSION_NAME);
        parameters.put("username", CommonUtils.decode(BUApplication.username));
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
        String url = ADD_FAVORITE_ENDPOINT + "/" + CommonUtils.encode(BUApplication.username) + "/" + tid;
        Log.i(TAG, "delFavorite >> " + url);

        HashMap parameters = new HashMap();
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
        String url = ADD_FAVORITE_ENDPOINT + "/list/" + CommonUtils.encode(BUApplication.username) + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getFavoriteList >> " + url);

        HashMap parameters = new HashMap();
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
        String url = ADD_FAVORITE_ENDPOINT + "/status/" + CommonUtils.encode(BUApplication.username) + "/" + tid;
        Log.i(TAG, "getFavoriteStatus >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FAVOR_STATUS", parameters, listener, errorListener);
    }

    /* 用户相关接口 */
    private final static String USER_ENDPOINT = ENDPOINT + "/user";

    /**
     * 获取用户的主题列表
     *
     * @param context       上下文
     * @param username      用户名
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getUserThreadList(Context context, String username, long from, long to,
                                         Response.Listener<JSONObject> listener,
                                         Response.ErrorListener errorListener) {
        String url = USER_ENDPOINT + "/" + CommonUtils.encode(username) + "/threads?" + "from=" + from + "&to=" + to;
        Log.i(TAG, "getUserThreadList >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_USER_THREAD_LIST", parameters, listener, errorListener);
    }

    /**
     * 获取用户的回帖列表
     *
     * @param context       上下文
     * @param username      用户名
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getUserPostList(Context context, String username, long from, long to,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {
        String url = USER_ENDPOINT + "/" + CommonUtils.encode(username) + "/replies?" + "from=" + from + "&to=" + to;
        Log.i(TAG, "getUserPostList >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_USER_POST_LIST", parameters, listener, errorListener);
    }

    /* 关注相关接口 */
    private final static String FOLLOW_ENDPOINT = ENDPOINT + "/follow";

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

        HashMap parameters = new HashMap();
        parameters.put("app", context.getString(R.string.app_name));
        parameters.put("version", BuildConfig.VERSION_NAME);
        parameters.put("follower", CommonUtils.decode(BUApplication.username));
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
        String url = FOLLOW_ENDPOINT + "/" + CommonUtils.encode(BUApplication.userSession.username) + "/" + CommonUtils.encode(following);
        Log.i(TAG, "delFollow >> " + url);

        HashMap parameters = new HashMap();
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
        String url = FOLLOW_ENDPOINT + "/status/" + BUApplication.userSession.username + "/" + CommonUtils.encode(following);
        Log.i(TAG, "getFollowStatus >> " + url);

        HashMap parameters = new HashMap();
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
        String url = FOLLOW_ENDPOINT + "/list/" + CommonUtils.encode(BUApplication.userSession.username) + "?" + "from=" + from + "&to=" + to;
        Log.i(TAG, "getFollowingList >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FOLLOWING_LIST", parameters, listener, errorListener);
    }


    /* 时间轴相关接口 */
    private final static String TIMELINE_ENDPOINT = ENDPOINT + "/timeline";

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

        HashMap parameters = new HashMap();
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

        String url = TIMELINE_ENDPOINT + "/focus/" + CommonUtils.encode(BUApplication.username) + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getFocusTimeline >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET_FOCUS_TIMELINE", parameters, listener, errorListener);
    }

    /* 时间轴相关接口 */
    private final static String SEARCH_ENDPOINT = ENDPOINT + "/search";

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
        String url = SEARCH_ENDPOINT + "/threads?key=" + CommonUtils.encode(keyword) + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchThreads >> " + url);

        HashMap parameters = new HashMap();
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
        String url = SEARCH_ENDPOINT + "/posts?key=" + CommonUtils.encode(keyword) + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchPosts >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH THREADS", parameters, listener, errorListener);
    }

    /**
     * 在特定板块内根据关键词搜索主题
     *
     * @param context       上下文
     * @param keyword       关键词
     * @param fid           板块编号
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void searchThreadsInForum(Context context, String keyword, Long fid,
                                            long from, long to,
                                            Response.Listener<JSONObject> listener,
                                            Response.ErrorListener errorListener) {
        String url = ENDPOINT + "/search/forum/threads?key=" + CommonUtils.encode(keyword) + "&fid=" + fid + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchThreadsInForum >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH THREADS IN FORUM", parameters, listener, errorListener);
    }

    /**
     * 在特定板块内根据关键词搜索帖子
     *
     * @param context       上下文
     * @param keyword       关键词
     * @param fid           板块编号
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void searchPostsInForum(Context context, String keyword, Long fid,
                                          long from, long to,
                                          Response.Listener<JSONObject> listener,
                                          Response.ErrorListener errorListener) {
        String url = ENDPOINT + "/search/forum/posts?key=" + CommonUtils.encode(keyword) + "&fid=" + fid + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchPostsInForum >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH THREADS IN FORUM", parameters, listener, errorListener);
    }

    /**
     * 搜索用户
     *
     * @param context       上下文
     * @param keyword       关键词
     * @param from          起始位置
     * @param to            结束位置
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void searchUsers(Context context, String keyword,
                                   long from, long to,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = ENDPOINT + "/search/users?key=" + CommonUtils.encode(keyword) + "&from=" + from + "&to=" + to;
        Log.i(TAG, "searchUsers >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "SEARCH USERS", parameters, listener, errorListener);
    }

    /* 通知相关接口 */
    private final static String NOTIFICATION_ENDPOINT = ENDPOINT + "/notification";

    public static void getNotificationList(Context context, String username,
                                           long from, long to,
                                           Response.Listener<JSONObject> listener,
                                           Response.ErrorListener errorListener) {
        String url = NOTIFICATION_ENDPOINT + "/list/" + username + "?from=" + from + "&to=" + to;
        Log.i(TAG, "getNotificationList >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "GET NOTIFICATION LIST", parameters, listener, errorListener);
    }

    /**
     * 将一条消息标记为已读
     *
     * @param context       上下文
     * @param notifyId      消息 ID
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    private static void markAsRead(Context context, long notifyId,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = NOTIFICATION_ENDPOINT + "/" + notifyId;
        Log.i(TAG, "markAsRead >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.PUT, context, url,
                "MARK AS READ", parameters, listener, errorListener);
    }

    /**
     * 获取指定用户的未读消息个数
     *
     * @param context       上下文
     * @param username      用户名
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getUnreadCount(final Context context, String username,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {
        String url = NOTIFICATION_ENDPOINT + "/count/" + username + "/unread";
        Log.i(TAG, "getUnreadCount >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "UNREAD COUNT", parameters, listener, errorListener);
    }

    /**
     * 获取指定用户的所有消息的个数
     *
     * @param context       上下文
     * @param username      用户名
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getNotificationCount(final Context context, String username,
                                            Response.Listener<JSONObject> listener,
                                            Response.ErrorListener errorListener) {
        String url = NOTIFICATION_ENDPOINT + "/count/" + username + "/all";
        Log.i(TAG, "getUnreadCount >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.GET, context, url,
                "UNREAD COUNT", parameters, listener, errorListener);
    }

    /**
     * 将一条消息标记为已读
     *
     * @param context  上下文
     * @param notifyId 消息 ID
     */
    public static void markAsRead(final Context context, Integer notifyId) {
        ExtraApi.markAsRead(context, notifyId, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "markAsRead >> " + response.toString());
                CommonUtils.debugToast(context, "成功标为已读");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = context.getString(R.string.error_network);
                String debugMessage = TAG + " >> " + message;
                CommonUtils.debugToast(context, debugMessage);
                Log.e(TAG, debugMessage, error);
            }
        });
    }

    /**
     * 将一个用户所有消息标记为已读
     *
     * @param context       上下文
     * @param username      用户名，需要是 URL 编码
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void markAllAsRead(Context context, String username,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener) {
        String url = NOTIFICATION_ENDPOINT + "/list/" + username + "/all";
        Log.i(TAG, "markAllAsRead >> " + url);

        HashMap parameters = new HashMap();
        makeRequest(Request.Method.PUT, context, url,
                "MARK ALL AS READ", parameters, listener, errorListener);
    }

    /* 反馈相关接口 */
    private final static String FEEDBACK_ENDPOINT = ENDPOINT + "/feedback";

    public static void addFeedback(Context context, Feedback feedback,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        String url = FEEDBACK_ENDPOINT;
        Log.i(TAG, "addFeedback >> " + url);

        HashMap parameters = new HashMap<>();
        parameters.put("application", feedback.application);
        parameters.put("deviceName", feedback.deviceName);
        parameters.put("version", feedback.version);
        parameters.put("versionCode", feedback.versionCode);
        parameters.put("username", feedback.username);
        parameters.put("email", feedback.email);
        parameters.put("content", feedback.content);
        parameters.put("dtCreated", feedback.dtCreated);

        makeRequest(Request.Method.POST, context, url,
                "ADD_FEEDBACK", parameters, listener, errorListener);
    }

}