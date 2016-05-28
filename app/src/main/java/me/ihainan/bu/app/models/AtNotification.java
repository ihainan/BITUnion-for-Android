package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 推送消息
 */
public class AtNotification implements Serializable {
    public final static String TAG = AtNotification.class.getSimpleName();

    public int type;
    public AtNotificationMessageData data;

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    public static class AtNotificationMessageData implements Serializable {
        public final static String TAG = AtNotificationMessageData.class.getSimpleName();

        public Long tid;
        public int floor;

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
}
