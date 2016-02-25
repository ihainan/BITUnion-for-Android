package bit.ihainan.me.bitunionforandroid.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Session;

/**
 * BIT Union Open APIs
 */
public class Api {
    public final static String TAG = Api.class.getSimpleName();
    public final static ObjectMapper MAPPER = new ObjectMapper();

    public static boolean checkStatus(JSONObject response) {
        try {
            if (!response.getString("result").equals("success")) return false;
            else return true;
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON object " + response, e);
            return false;
        }
    }

    public static String getErrorMessage(JSONObject response) {
        try {
            return response.getString("msg");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON object :" + response, e);
            return null;
        }
    }

    /**
     * 尝试登陆系统
     *
     * @param context       上下文
     * @param userName      登陆用户名
     * @param password      登陆密码
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void tryLogin(Context context,
                                String userName, String password,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "login");
        parameters.put("username", userName);
        parameters.put("password", password);
        MobclickAgent.onProfileSignIn(CommonUtils.decode(userName));
        makeRequest(context, Global.getLoginURL(), "LOGIN", parameters, 0, listener, errorListener);
    }

    /**
     * 尝试登陆系统
     *
     * @param context       上下文
     * @param userName      登陆用户名
     * @param password      登陆密码
     * @param retryLimit    尝试重新登录的次数
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void tryLogin(Context context, String userName, String password, final int retryLimit,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "login");
        parameters.put("username", userName);
        parameters.put("password", password);
        makeRequest(context, Global.getLoginURL(), "LOGIN", parameters, retryLimit, listener, errorListener);
    }

    /**
     * 发送请求，如果 Session 过期则更新 Session，如果失败则多次尝试，直到达到了上限 retryLimit
     *
     * @param context       上下文
     * @param url           请求 URL
     * @param tag           请求标签
     * @param parameters    请求参数
     * @param retryLimit    重复登录最大次数
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    private static void makeRequest(final Context context, final String url, final String tag,
                                    final Map<String, String> parameters, final int retryLimit,
                                    final Response.Listener<JSONObject> listener,
                                    final Response.ErrorListener errorListener) {
        Log.i(TAG, "Want to make request with parameters: " + parameters.toString() + " URL: " + url + " Retry Limit: " + retryLimit);

        // 不设置 retryLimit，只尝试一次
        if (retryLimit <= 0) {
            JsonObjectRequest request = new JsonObjectRequest(url,
                    new JSONObject(parameters), listener, errorListener);
            RequestQueueManager.getInstance(context).addToRequestQueue(request, tag);
        } else {
            // 设置 retryLimit，最多尝试特定次数，直到登录成功为止
            makeRequest(context, url, tag, parameters, 0,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (Api.checkStatus(response)) {
                                listener.onResponse(response);
                            } else {
                                // 妈蛋，又过期了，有病呀这 API！！
                                // 尝试重新登录
                                Log.i(TAG, "makeRequest >> Session 过期，尝试重新登录 " + retryLimit + " " + url);
                                tryLogin(context, Global.userName, Global.password, retryLimit - 1,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (checkStatus(response)) {
                                                    // 登录成功，拿到 session
                                                    try {
                                                        Global.userSession = Api.MAPPER.readValue(response.toString(), Session.class);
                                                        Global.saveConfig(context);
                                                        CommonUtils.debugToast(context, "makeRequest >> 成功拿到新 Session " + Global.userSession);
                                                        Log.i(TAG, "makeRequest >> 成功拿到新 Session " + Global.userSession);
                                                        parameters.put("session", Global.userSession.session);
                                                        Global.saveConfig(context);
                                                        makeRequest(context, url, tag, parameters, retryLimit - 1, listener, errorListener);
                                                    } catch (Exception e) {
                                                        Log.e(TAG, context.getString(R.string.error_parse_json) + "\n" + response, e);
                                                    }
                                                } else {
                                                    // 登录失败……继续尝试重新登录，直到成功，或者 retryLimit = 0
                                                    Log.i(TAG, "makeRequest >> 尝试重新登录失败，继续尝试 " + retryLimit + " URL: " + url);
                                                    tryLogin(context, Global.userName, Global.password,
                                                            retryLimit - 1, listener, errorListener);
                                                }
                                            }
                                        }, errorListener);
                            }
                        }
                    }, errorListener);
        }
    }

    /**
     * 获取首页前 20 个帖子
     *
     * @param context       上下文
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getHomePage(Context context,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", Global.userSession.username);
        parameters.put("session", Global.userSession.session);

        makeRequest(context, Global.getHomePageURL(), "HOME_PAGE", parameters, Global.RETRY_LIMIT, listener, errorListener);
    }

    /**
     * 获取指定用户的信息
     *
     * @param context       上下文
     * @param uid           需要查询用户的 uid，与 username 二选一，若 username 不为 null 则默认为 username
     * @param username      需要查询用户的 username，与 uid 二选一，若 username 不为 null 则默认为 username
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getUserInfo(Context context,
                                   long uid, String username,
                                   Response.Listener<JSONObject> listener,
                                   Response.ErrorListener errorListener) {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "profile");
        parameters.put("username", Global.userName);
        parameters.put("session", Global.userSession.session);
        if (username != null) parameters.put("queryusername", username);
        else parameters.put("uid", "" + uid);

        makeRequest(context, Global.getUserInfoURL(), "USER_INFO", parameters, Global.RETRY_LIMIT, listener, errorListener);
    }

    /**
     * @param context       上下文
     * @param tid           需要查询的帖子 ID
     * @param from          查询帖子起始 ID，从 0 开始
     * @param to            查询帖子结束 ID，to - from <= 20
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getPostReplies(Context context, long tid,
                                      long from, long to,
                                      Response.Listener<JSONObject> listener,
                                      Response.ErrorListener errorListener) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "post");
        parameters.put("username", Global.userSession.username);
        parameters.put("session", Global.userSession.session);
        parameters.put("tid", String.valueOf(tid));
        parameters.put("from", String.valueOf(from));
        parameters.put("to", String.valueOf(to));

        makeRequest(context, Global.getThreadDetailURL(), "POST_DETAIL", parameters, Global.RETRY_LIMIT, listener, errorListener);
    }

    /**
     * @param context       上下文
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getForumList(Context context,
                                    Response.Listener<JSONObject> listener,
                                    Response.ErrorListener errorListener) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "forum");
        parameters.put("username", Global.userSession.username);
        parameters.put("session", Global.userSession.session);
        makeRequest(context, Global.getForumListURL(), "FORUM_LIST", parameters, Global.RETRY_LIMIT, listener, errorListener);
    }

    /**
     * @param context       上下文
     * @param fid           需要查询的论坛 ID
     * @param from          查询帖子起始 ID，从 0 开始
     * @param to            查询帖子结束 ID，to - from <= 20
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     */
    public static void getForumThreads(Context context, long fid,
                                       long from, long to,
                                       Response.Listener<JSONObject> listener,
                                       Response.ErrorListener errorListener) {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "thread");
        parameters.put("username", Global.userSession.username);
        parameters.put("session", Global.userSession.session);
        parameters.put("fid", String.valueOf(fid));
        parameters.put("from", String.valueOf(from));
        parameters.put("to", String.valueOf(to));

        makeRequest(context, Global.getForumListURL(), "THREAD_LIST", parameters, Global.RETRY_LIMIT, listener, errorListener);
    }
}
