package me.ihainan.bu.app.utils.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import org.json.JSONObject;

import java.util.List;
import java.util.logging.Logger;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.AtNotification;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * 小米推送接收器
 */
public class XMMessageReceiver extends PushMessageReceiver {
    public final static String TAG = XMMessageReceiver.class.getSimpleName();
    private String mRegId;
    private long mResultCode = -1;
    private String mReason;
    private String mCommand;
    private String mMessage;
    private String mTopic;
    private String mAlias;
    private String mStartTime;
    private String mEndTime;

    /**
     * 接收到穿透消息
     *
     * @param context 上下文
     * @param message 穿透消息
     */
    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        if (message.getUserAccount().equals(CommonUtils.decode(BUApplication.username))) {
            // Parse message
            try {
                JSONObject jsonObject = new JSONObject(message.getContent());
                Log.d(TAG, "onReceivePassThroughMessage >> " + message.getContent() + " " + message.getNotifyId());
                if (jsonObject.getInt("type") == 1) {
                    AtNotification atNotification = BUApi.MAPPER.readValue(message.getContent(), AtNotification.class);
                    Intent intent = new Intent(context, PostListActivity.class);
                    intent.setAction(Long.toString(System.currentTimeMillis()));
                    intent.putExtra(PostListActivity.THREAD_ID_TAG, atNotification.data.tid);
                    intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, atNotification.data.floor);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, message.getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                    mBuilder.setContentTitle(message.getTitle());
                    mBuilder.setContentText(message.getDescription());
                    mBuilder.setAutoCancel(true);
                    mBuilder.setSmallIcon(R.drawable.logo);
                    mBuilder.setColor(context.getResources().getColor(R.color.primary));
                    mBuilder.setContentIntent(pendingIntent);
                    int mNotificationId = message.getNotifyId();
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            } catch (Exception e) {
                Log.e(TAG, "解析推送数据失败 " + message.getContent(), e);
            }
        }
    }

    /**
     * 普通通知被点击之后
     *
     * @param context 上下文
     * @param message 被点击的普通通知
     */
    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }

        // Parse message
        try {
            JSONObject jsonObject = new JSONObject(message.getContent());
            if (jsonObject.getInt("type") == 1) {
                AtNotification atNotification = BUApi.MAPPER.readValue(message.getContent(), AtNotification.class);
                Intent intent = new Intent(context, PostListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(PostListActivity.THREAD_ID_TAG, atNotification.data.tid);
                intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, atNotification.data.floor);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "解析推送数据失败 " + message.getContent(), e);
        }
    }

    /**
     * 普通通知到达
     *
     * @param context 上下文
     * @param message 普通通知
     */
    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage message) {
        mMessage = message.getContent();
        if (!TextUtils.isEmpty(message.getTopic())) {
            mTopic = message.getTopic();
        } else if (!TextUtils.isEmpty(message.getAlias())) {
            mAlias = message.getAlias();
        }
    }

    /**
     * 获取给服务器发送命令的结果，
     *
     * @param context 上下文
     * @param message 结果消息
     */
    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
            }
        }
    }

    /**
     * 获取给服务器发送注册命令的结果
     *
     * @param context 上下文
     * @param message 结果消息
     */
    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage message) {
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
            }
        }
    }
}
