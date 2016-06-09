package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Notification;
import me.ihainan.bu.app.models.NotificationMessage;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.ui.viewholders.NotificationViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 通知适配器
 */
public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = NotificationAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<Notification> mList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        mList = notificationList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    @Override
    public int getItemViewType(int position) {
        return mList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof NotificationViewHolder) {
            // Do nothing here
            final Notification notification = mList.get(position);
            final NotificationViewHolder viewHolder = (NotificationViewHolder) holder;

            if (notification.is_read == 0) {
                viewHolder.rootLayout.setBackgroundColor(mContext.getResources().getColor(R.color.background_white));
                viewHolder.content.setTextAppearance(mContext, R.style.boldText);
            } else {
                viewHolder.rootLayout.setBackgroundColor(mContext.getResources().getColor(R.color.background_read));
                viewHolder.content.setTextAppearance(mContext, R.style.normalText);
            }

            // 收藏
            viewHolder.username.setText(notification.title);
            viewHolder.content.setText(notification.description);
            viewHolder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.parseDateString(notification.dt_created)));


            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    notification.title,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(final Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                            CommonUtils.setAvatarImageView(mContext, viewHolder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                            viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.USER_ID_TAG, member.uid);
                                    intent.putExtra(ProfileActivity.USER_NAME_TAG, member.username);
                                    mContext.startActivity(intent);
                                }
                            });
                        }
                    });

            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1, notification.title);

            try {
                JSONObject jsonObject = new JSONObject(notification.payload);
                int type = notification.type;
                Intent intent = null;
                if (type == 0 || type == 1 || type == 2) {
                    NotificationMessage.PostNotificationMessageData notificationMessageData = BUApi.MAPPER.readValue(jsonObject.getJSONObject("data").toString(), NotificationMessage.PostNotificationMessageData.class);
                    intent = new Intent(mContext, PostListActivity.class);
                    intent.setAction(Long.toString(System.currentTimeMillis()));
                    intent.putExtra(PostListActivity.THREAD_ID_TAG, notificationMessageData.tid);
                    intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, notificationMessageData.floor);

                } else if (type == 3) {
                    NotificationMessage.FollowNotificationMessageData followNotificationMessageData = BUApi.MAPPER.readValue(jsonObject.getJSONObject("data").toString(), NotificationMessage.FollowNotificationMessageData.class);
                    intent = new Intent(mContext, ProfileActivity.class);
                    intent.setAction(Long.toString(System.currentTimeMillis()));
                    intent.putExtra(ProfileActivity.USER_NAME_TAG, followNotificationMessageData.following);
                }

                if (intent != null) {
                    final Intent newIntent = intent;
                    viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (notification.is_read == 0) {
                                /*
                                ExtraApi.markAsRead(mContext, notification.nt_id, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d(TAG, "markAsRead >> " + response.toString());
                                        CommonUtils.debugToast(mContext, "标为已读成功成功");
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        String message = mContext.getString(R.string.error_network);
                                        String debugMessage = TAG + " >> " + message;
                                        CommonUtils.debugToast(mContext, debugMessage);
                                        Log.e(TAG, debugMessage, error);
                                    }
                                }); */
                            }
                            notification.is_read = 1;
                            notifyDataSetChanged();
                            mContext.startActivity(newIntent);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, mContext.getString(R.string.error_parse_json), e);
            }
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
