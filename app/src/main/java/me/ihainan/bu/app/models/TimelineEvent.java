package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 时间轴模型
 */
public class TimelineEvent {
    public final static String TAG = TimelineEvent.class.getSimpleName();

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert TimelineEvent object to JSON string", e);
            return null;
        }
    }

    public long tl_id;
    public int type;
    public Object content;
}
