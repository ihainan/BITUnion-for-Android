package me.ihainan.bu.app.utils.network;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Session;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * BIT Union Open APIs
 */
public class BUApi {
    public final static String TAG = BUApi.class.getSimpleName();
    public final static ObjectMapper MAPPER = new ObjectMapper();

    // Constants
    public final static String LOGGED_MSG = "IP+logged";
    public final static String THREAD_NO_PERMISSION_MSG = "thread_nopermission";
    public final static String FORUM_NO_PERMISSION_MSG = "forum_nopermission";

    // END POINT
    public final static String IN_SCHOOL_BASE_URL = "http://www.bitunion.org/";
    public final static String OUT_SCHOOL_BASE_URL = "http://out.bitunion.org/";
    public final static String IN_SCHOOL_ENDPOINT = IN_SCHOOL_BASE_URL + "open_api/";
    public final static String OUT_SCHOOL_ENDPOINT = OUT_SCHOOL_BASE_URL + "open_api/";
    public static String currentEndPoint = OUT_SCHOOL_ENDPOINT;

    public static String getBaseURL() {
        if (currentEndPoint.startsWith(IN_SCHOOL_ENDPOINT)) return IN_SCHOOL_BASE_URL;
        else return OUT_SCHOOL_BASE_URL;
    }

    private static String getLoginURL() {
        return currentEndPoint + "bu_logging.php";
    }

    private static String getHomePageURL() {
        return currentEndPoint + "bu_home.php";
    }

    private static String getUserInfoURL() {
        return currentEndPoint + "bu_profile.php";
    }

    private static String getThreadDetailURL() {
        return currentEndPoint + "bu_post.php";
    }

    private static String getForumListURL() {
        return currentEndPoint + "bu_thread.php";
    }

    private static String getNewPostURL() {
        return currentEndPoint + "bu_newpost.php";
    }

    /**
     * 检查联盟返回 result 字段是否为 success
     *
     * @param response BU 服务器回复数据
     * @return <code>true</code> 表示 result 字段是 success，否则不是
     */
    public static boolean checkStatus(JSONObject response) {
        try {
            return response.getString("result").equals("success");
        } catch (JSONException e) {
            Log.e(TAG, "Fail to parse JSON object " + response, e);
            return false;
        }
    }

    /**
     * 检查 Session 是否已经过去
     *
     * @param response BU 服务器回复数据
     * @return <code>true</code> 表示已经过期，否则未过期
     */
    private static boolean checkIfSessionOutOfData(JSONObject response) {
        try {
            return LOGGED_MSG.equals(response.getString("msg"));
        } catch (JSONException e) {
            Log.d(TAG, "Session is out of data", e);
            return false;
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
        makeRequest(context, getLoginURL(), "LOGIN", parameters, 0, listener, errorListener);
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
        makeRequest(context, getLoginURL(), "LOGIN", parameters, retryLimit, listener, errorListener);
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
        Map<String, String> parameters;
        parameters = new HashMap<>();
        if (BUApplication.userSession != null) {
            parameters.put("username", BUApplication.userSession.username);
            parameters.put("session", BUApplication.userSession.session);
        }
        makeRequest(context, getHomePageURL(), "HOME_PAGE", parameters, BUApplication.RETRY_LIMIT, listener, errorListener);
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
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "profile");
        parameters.put("username", BUApplication.username);
        parameters.put("session", BUApplication.userSession.session);
        if (username != null) parameters.put("queryusername", username);
        else parameters.put("uid", "" + uid);

        makeRequest(context, getUserInfoURL(), "USER_INFO", parameters, BUApplication.RETRY_LIMIT, listener, errorListener);
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

        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "post");
        if (BUApplication.userSession == null) BUApplication.getCacheSession(context);
        if (BUApplication.userSession != null) {
            parameters.put("username", BUApplication.userSession.username);
            parameters.put("session", BUApplication.userSession.session);
        } else {
            parameters.put("username", "");
            parameters.put("session", "");
        }
        parameters.put("tid", String.valueOf(tid));
        parameters.put("from", String.valueOf(from));
        parameters.put("to", String.valueOf(to));

        makeRequest(context, getThreadDetailURL(), "POST_DETAIL", parameters, BUApplication.RETRY_LIMIT, listener, errorListener);
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

        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "thread");
        parameters.put("username", BUApplication.userSession.username);
        parameters.put("session", BUApplication.userSession.session);
        parameters.put("fid", String.valueOf(fid));
        parameters.put("from", String.valueOf(from));
        parameters.put("to", String.valueOf(to));

        makeRequest(context, getForumListURL(), "THREAD_LIST", parameters, BUApplication.RETRY_LIMIT, listener, errorListener);
    }

    /**
     * 发表新回复
     *
     * @param context       上下文
     * @param tid           回复的帖子 ID
     * @param message       回复的帖子内容
     * @param attachment    附件数据，可为 null
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     * @throws IOException 构建 Multipart 请求失败
     */
    public static void postNewPost(Context context, Long tid, String message,
                                   String fileName,
                                   @Nullable byte[] attachment,
                                   Response.Listener<NetworkResponse> listener,
                                   Response.ErrorListener errorListener) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", BUApplication.userSession.username);    // session 里面的 username 未曾 encode 过
        parameters.put("session", BUApplication.userSession.session);
        parameters.put("action", "newreply");
        parameters.put("tid", String.valueOf(tid));
        parameters.put("message", CommonUtils.encode(message));
        parameters.put("attachment", attachment == null ? "0" : "1");

        // String url = "http://192.168.56.1:8080/api/v2/multipart/att";
        // String url = "http://ali.ihainan.me:8080/api/v2/multipart/att";
        String url = getNewPostURL();

        if (attachment == null) {
            CommonUtils.debugToast(context, "POST_NEW_POST_NO_ATT >> " + url);
            makeMultipartRequest(context, url, "POST_NEW_POST_NO_ATT", parameters, fileName, null, listener, errorListener);
        } else {
            CommonUtils.debugToast(context, "POST_NEW_POST_NO_ATT >> " + url);
            makeMultipartRequest(context, url, "POST_NEW_POST_NO_ATT", parameters, fileName, attachment, listener, errorListener);
        }
    }

    /**
     * 发表新回复
     *
     * @param context       上下文
     * @param fid           回复的帖子 ID
     * @param title         主题标题
     * @param message       回复的帖子内容
     * @param attachment    附件数据，可为 null
     * @param listener      response 事件监听器
     * @param errorListener error 事件监听器
     * @throws IOException 构建 Multipart 请求失败
     */
    public static void postNewThread(Context context, Long fid, String title, String message,
                                     String fileName,
                                     @Nullable byte[] attachment,
                                     Response.Listener<NetworkResponse> listener,
                                     Response.ErrorListener errorListener) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", BUApplication.userSession.username);    // session 里面的 username 未曾 encode 过
        parameters.put("session", BUApplication.userSession.session);
        parameters.put("action", "newthread");
        parameters.put("fid", fid);
        parameters.put("subject", CommonUtils.encode(title));
        parameters.put("message", CommonUtils.encode(message));
        parameters.put("attachment", attachment == null ? "0" : "1");


        // String url = "http://192.168.56.1:8080/api/v2/multipart/att";
        // String url = "http://ali.ihainan.me:8080/api/v2/multipart/att";
        String url = getNewPostURL();

        if (attachment == null) {
            CommonUtils.debugToast(context, "POST_NEW_THREAD_NOT_ATT >> " + url);
            makeMultipartRequest(context, url, "POST_NEW_THREAD_NOT_ATT", parameters, fileName, null, listener, errorListener);
        } else {
            CommonUtils.debugToast(context, "POST_NEW_THREAD_ATT >> " + url);
            makeMultipartRequest(context, url, "POST_NEW_THREAD_ATT", parameters, fileName, attachment, listener, errorListener);
        }
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
            makeRequest(context, url, tag, parameters, listener, errorListener);
        } else {
            // 设置 retryLimit，最多尝试特定次数，直到登录成功为止
            makeRequest(context, url, tag, parameters,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (checkIfSessionOutOfData(response)) {
                                // Success + IP+logged，尝试重新登录
                                Log.i(TAG, "makeRequest " + tag + ">> Session " +
                                        (BUApplication.userSession == null ? "NULL" : BUApplication.userSession.session) +
                                        " 过期，尝试重新登录 " + retryLimit + " " + url);
                                BUApi.tryLogin(context, BUApplication.username, BUApplication.password, retryLimit - 1,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                if (BUApi.checkStatus(response)) {
                                                    // 登录成功，拿到 session
                                                    try {
                                                        BUApplication.userSession = BUApi.MAPPER.readValue(response.toString(), Session.class);
                                                        BUApplication.setCacheSession(context);
                                                        CommonUtils.debugToast(context, "makeRequest " + tag + ">> 成功拿到新 Session " + BUApplication.userSession);
                                                        Log.i(TAG, "makeRequest >> " + tag + "成功拿到新 Session " + BUApplication.userSession);
                                                        parameters.put("session", BUApplication.userSession.session);
                                                        makeRequest(context, url, tag, parameters, retryLimit - 1, listener, errorListener);
                                                    } catch (Exception e) {
                                                        Log.e(TAG, context.getString(R.string.error_parse_json) + "\n" + response, e);
                                                    }
                                                } else {
                                                    // 登录失败……继续尝试重新登录，直到成功，或者 retryLimit = 0
                                                    Log.i(TAG, "makeRequest >> 尝试重新登录失败，继续尝试 " + retryLimit + " URL: " + url);
                                                    BUApi.tryLogin(context, BUApplication.username, BUApplication.password,
                                                            retryLimit - 2, listener, errorListener);
                                                }
                                            }
                                        }, errorListener);
                            } else {
                                // 在 tryLimit 次成功
                                listener.onResponse(response);
                            }
                        }
                    }, errorListener);
        }
    }


    private static void makeRequest(final Context context, final String url, final String tag,
                                    final Map<String, String> parameters,
                                    final Response.Listener<JSONObject> listener,
                                    final Response.ErrorListener errorListener) {
        Log.i(TAG, "Want to make request with parameters: " + parameters.toString() + " URL: " + url);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new JSONObject(parameters), listener, errorListener);
        RequestQueueManager.getInstance(context).addToRequestQueue(request, tag);
    }

    private final static String twoHyphens = "--";
    private final static String lineEnd = "\r\n";
    private final static String boundary = "0xKhTmLbOuNdArY-D6FD9655-98B1-4414-9351-64C773F11138";  // TODO: 删除测试
    private final static String mimeType = " multipart/form-data; charset=utf-8; boundary=" + boundary;

    private static void buildPart(DataOutputStream dataOutputStream, String parameterName, String fileName, byte[] fileData) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"; filename=\"" + fileName + "\"" + lineEnd);
        dataOutputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
        dataOutputStream.writeBytes(lineEnd);

        ByteArrayInputStream fileInputStream = new ByteArrayInputStream(fileData);
        int bytesAvailable = fileInputStream.available();

        int maxBufferSize = 1024 * 1024;
        int bufferSize = Math.min(bytesAvailable, maxBufferSize);
        byte[] buffer = new byte[bufferSize];

        // read file and write it into form...
        int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
    }

    private static void buildTextPart(DataOutputStream dataOutputStream, String parameterName, String parameterValue) throws IOException {
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"" + lineEnd);    // 然而并不需要 id 字段
        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(parameterValue + lineEnd);
    }

    private static void
    makeMultipartRequest(final Context context, final String url, final String tag,
                         final Map<String, Object> parameters, String fileName, byte[] fileData,
                         final Response.Listener<NetworkResponse> listener,
                         final Response.ErrorListener errorListener) throws IOException {
        byte[] multipartBody;

        Log.d(TAG, "Start making multipart request");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        // Build JSON Part
        buildTextPart(dos, "json", new JSONObject(parameters).toString());

        // Build Attachment Part
        if (fileData != null) buildPart(dos, "attach", fileName, fileData);

        // send multipart form data necessary after file data
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        // pass to multipart body
        multipartBody = bos.toByteArray();

        // Build MultipartRequest
        MultipartRequest multipartRequest = new MultipartRequest(url, null, mimeType, multipartBody, listener, errorListener);

        // Add to queue
        Log.d(TAG, "makeMultipartRequest >> " + tag + " - " + url + " " + parameters);
        RequestQueueManager.getInstance(context).addToRequestQueue(multipartRequest, tag, 30);
    }
}
