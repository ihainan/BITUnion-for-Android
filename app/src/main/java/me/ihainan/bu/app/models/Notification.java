package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 通知
 */
public class Notification implements Serializable {
    private final static String TAG = Notification.class.getSimpleName();

    // Json mapping fields
    public Integer nt_id;
    public final int type = 0;
    public String username;
    public String title;
    public int is_read;
    public String description;
    public String payload;
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
