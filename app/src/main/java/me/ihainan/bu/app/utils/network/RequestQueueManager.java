package me.ihainan.bu.app.utils.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Request Manager
 */
public class RequestQueueManager {
    public final static String TAG = RequestQueueManager.class.getName();   // Default tag for one request

    private RequestQueue mRequestQueue;
    private static RequestQueueManager mInstance;
    private static Context mContext;

    private RequestQueueManager(Context context) {
        mContext = context;
    }

    private synchronized static void newInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueManager(context);
        }
    }

    /**
     * Get RequestQueueManager instance
     *
     * @return RequestQueueManager instance
     */
    public static RequestQueueManager getInstance(Context context) {
        if (mInstance == null) newInstance(context);
        return mInstance;
    }

    /**
     * Get request queue
     *
     * @param context Context
     * @return Request queue instance
     */
    public RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    /**
     * Add new request to queue
     *
     * @param req the request need to be added
     * @param tag request id
     * @param <T> request type
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        try {
            req.setRetryPolicy(new DefaultRetryPolicy(
                    1000 * 10,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Log.d(TAG, "addToRequestQueue >> Making Request " + req.getUrl() + " With parameters " + new String(req.getBody()));
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Log.e(TAG, "addToRequestQueue failed >> " + authFailureError);
        }
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(mContext).add(req);
    }

    /**
     * Add new request to queue
     *
     * @param req     the request need to be added
     * @param tag     request id
     * @param timeout 超时时间，单位为妙
     * @param <T>     request type
     */
    public <T> void addToRequestQueue(Request<T> req, String tag, int timeout) {
        try {
            req.setRetryPolicy(new DefaultRetryPolicy(
                    1000 * timeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Log.d(TAG, "addToRequestQueue >> Making Request " + req.getUrl() + " With parameters " + new String(req.getBody()));
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Log.e(TAG, "addToRequestQueue failed >> " + authFailureError);
        }
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(mContext).add(req);
    }

    /**
     * Cancel specific request
     *
     * @param tag request id
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    protected static class CustomJsonObjectRequest extends JsonObjectRequest {

        public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
            setRetryPolicy(new DefaultRetryPolicy(
                    1000 * 10,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }

        public CustomJsonObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(url, jsonRequest, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> params = new HashMap<String, String>();
            String credits = String.format("%s:%s", ExtraApi.BASIC_AUTH_USERNAME, ExtraApi.BASIC_AUTH_PASSWORD);
            String auth = "Basic " + Base64.encodeToString(credits.getBytes(), Base64.NO_WRAP);
            params.put("Authorization", auth);
            return params;
        }

        @Override
        public String getBodyContentType() {
            return "application/json; charset=utf-8";
        }
    }
}
