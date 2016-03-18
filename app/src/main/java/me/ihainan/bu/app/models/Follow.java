package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 关注模型
 */
public class Follow {
    public final static String TAG = Follow.class.getSimpleName();

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    public Long fl_id;
    public String follower;
    public String following;
    public String dt_created;
}
