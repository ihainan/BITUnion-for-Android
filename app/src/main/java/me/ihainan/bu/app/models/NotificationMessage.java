package me.ihainan.bu.app.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 推送消息
 */
public class NotificationMessage implements Serializable {
    public final static String TAG = NotificationMessage.class.getSimpleName();

    public int type;
    public NotificationData data;

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    public static class NotificationData implements Serializable {

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

    /**
     * 与具体楼层相关的通知数据
     */
    public static class PostNotificationMessageData extends NotificationData {
        public final static String TAG = PostNotificationMessageData.class.getSimpleName();

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

    /**
     * 关注相关通知
     */
    public static class FollowNotificationMessageData extends NotificationData {
        public final static String TAG = FollowNotificationMessageData.class.getSimpleName();

        public String follower;
        public String following;

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
