package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 帖子模型
 */
public class Thread implements Serializable{
    public final static String TAG = Thread.class.getSimpleName();

    // Json mapping fields
    public long tid;
    public String author;
    public long authorid;
    public String subject;
    public long dateline;
    public long lastpost;
    public String lastposter;
    public long views;
    public long replies;


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
