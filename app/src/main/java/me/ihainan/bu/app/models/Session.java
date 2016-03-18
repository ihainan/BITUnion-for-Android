package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 回话模型
 */
public class Session implements Serializable {
    public final static String TAG = Session.class.getSimpleName();

    // Json mapping fields
    public int uid; // 用户 ID
    public String result;   //
    public String username; // 用户名
    public String session;  // 用户 Session 字符串
    public String status;   //
    public int credit;      //
    public long lastactivity;   // 上一次登录时间

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }
}
