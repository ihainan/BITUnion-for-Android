package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 反馈内容
 */
public class Feedback implements Serializable {
    private final static String TAG = Feedback.class.getSimpleName();

    public String email;
    public String content;
    public String version;
    public int versionCode;
    public String deviceName;
    public String username;
    public String application;
    public String dtCreated;

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
