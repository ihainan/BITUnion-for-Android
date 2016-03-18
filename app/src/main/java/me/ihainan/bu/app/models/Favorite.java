package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 收藏模型
 */
public class Favorite {
    public final static String TAG = Favorite.class.getSimpleName();

    public Long fav_id;
    public String username;
    public Long tid;
    public String subject;
    public String author;
    public String dt_created;

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
