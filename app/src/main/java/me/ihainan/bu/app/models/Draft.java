package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 草稿模型
 */

public class Draft {
    private final static String TAG = Draft.class.getSimpleName();

    public String id = UUID.randomUUID().toString();

    public String action;
    public String subject;
    public String content;
    // public Long tid;
    // public Long fid;
    public String attachmentURI;
    public Long timestamp;

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
