package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 最新主题模型
 */
public class LatestThread implements Serializable {
    public final static String TAG = LatestThread.class.getSimpleName();

    public static class LastReply implements Serializable {
        public final static String TAG = LastReply.class.getSimpleName();

        // Json mapping fields
        public String when; // 嘛时候
        public String who;  // 你谁呀
        public String what; // 干嘛呢

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

    // Json mapping fields
    public String pname;        // 帖子名
    public String fname;        // 论坛名
    public String author;       // 发帖作者
    public long tid;            // 帖子编号
    public long tid_sum;        // 回帖总数
    public long fid;            // 论坛 ID
    public long fid_sum;        // 论坛新帖总数
    public LastReply lastreply; // 最后回复
    public String avatar;       // 发帖会员头像


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
