package bit.ihainan.me.bitunionforandroid.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.InetAddress;

/**
 * HTTP Request Manager
 */
public class RequestQueueManager {
    public final static String TAG = RequestQueueManager.class.getName();   // Default tag for one request

    private final static String DNS_SERVER = "10.0.0.9";

    public static boolean checkIfInSchool() {
        try {
            InetAddress in = InetAddress.getByName(DNS_SERVER);
            return in.isReachable(300);
        } catch (IOException e) {
            Log.e(TAG, "Failed to check if in school", e);
            return false;
        }
    }

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
            Log.d(TAG, "addToRequestQueue >> Making Request " + req.getUrl() + " With parameters " + new String(req.getBody()));
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Log.e(TAG, "addToRequestQueue >> " + authFailureError);
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
}
