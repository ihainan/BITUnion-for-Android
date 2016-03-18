package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 用户模型
 */
public class Member implements Serializable {
    public final static String TAG = Member.class.getSimpleName();

    // Json mapping fields
    public long uid;
    public String status;
    public String username;
    public String avatar;
    public long credit;
    public long regdate;
    public long lastvisit;
    public String bday;
    public String signature;
    public int postnum;
    public int threadnum;
    public String email;
    public String site;
    public String icq;
    public String oicq;
    public String yahoo;
    public String msn;

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
