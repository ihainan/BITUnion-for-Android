package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 用户
 */
public class User implements Serializable {
    private final static String TAG = User.class.getSimpleName();

    // Json mapping fields
    public int uid; // 用户 ID
    public String username; // 用户名

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

