package me.ihainan.bu.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import jp.wasabeef.picasso.transformations.gpu.VignetteFilterTransformation;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.LatestThread;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.DefaultViewHolder;
import me.ihainan.bu.app.ui.viewholders.SelfieViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * Forum LatestThread List Adapter
 */
public class LatestThreadListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = LatestThreadListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<LatestThread> mLatestThreads;

    public LatestThreadListAdapter(Context context, List<LatestThread> latestThreads) {
        mLatestThreads = latestThreads;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                view = mLayoutInflater.inflate(R.layout.item_thread_item, parent, false);
                viewHolder = new DefaultViewHolder(view);
                break;
            default:
                view = mLayoutInflater.inflate(R.layout.item_thread_selfie, parent, false);
                viewHolder = new SelfieViewHolder(view);
                break;
        }

        return viewHolder;
    }

    private final static int VIEW_TYPE_DEFAULT = 1;
    private final static int VIEW_TYPE_SELFIE = 2;

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_DEFAULT;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final LatestThread latestThread = mLatestThreads.get(position);
        fillDefaultView(latestThread, viewHolder);
    }

    /**
     * 填充非 『个人展示区』 帖子内容
     *
     * @param latestThread 当前需要显示的帖子
     * @param viewHolder   View holder
     */
    private void fillDefaultView(final LatestThread latestThread, RecyclerView.ViewHolder viewHolder) {
        final DefaultViewHolder holder = (DefaultViewHolder) viewHolder;

        // 无差别区域
        holder.avatar.setImageDrawable(null);
        holder.replyCount.setText(latestThread.tid_sum + " 回复");
        holder.title.setText(Html.fromHtml(CommonUtils.addSpaces(CommonUtils.decode(latestThread.pname))));

        // Root Layout
        final Intent intent = new Intent(mContext, PostListActivity.class);
        intent.putExtra(PostListActivity.THREAD_FID_TAG, latestThread.fid);
        intent.putExtra(PostListActivity.THREAD_ID_TAG, latestThread.tid);
        intent.putExtra(PostListActivity.THREAD_AUTHOR_NAME_TAG, latestThread.author);
        intent.putExtra(PostListActivity.THREAD_REPLY_COUNT_TAG, latestThread.tid_sum + 1);
        intent.putExtra(PostListActivity.THREAD_NAME_TAG, latestThread.pname);
        if (Build.VERSION.SDK_INT >= 24) {
            if (((Activity) mContext).isInMultiWindowMode()) {
                intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            }
        }
        intent.setAction(Intent.ACTION_VIEW);
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BUApplication.homePageClickEventType == 0) {
                    // 进入尾楼
                    intent.removeExtra(PostListActivity.THREAD_JUMP_FLOOR);
                    mContext.startActivity(intent);
                } else {
                    // 进入主楼
                    intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, 0);
                    mContext.startActivity(intent);
                }
            }
        });

        // 发帖、回帖日期
        if (latestThread.lastreply != null) {
            holder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.parseDateString(CommonUtils.decode(latestThread.lastreply.when))));
        } else {
            holder.date.setText("未知次元未知时间");
        }

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            String avatarURL = CommonUtils.getRealImageURL(latestThread.avatar);
            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                    avatarURL, R.drawable.default_avatar);

            // 新帖子标志
            holder.isNewOrHot.setVisibility(View.VISIBLE);
            holder.isNewOrHot.setText("  NEW");
            holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.primary_dark));

            // 其他域
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            BUApplication.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.lastreply.who);
            holder.forumName.setText(HtmlUtil.getSummaryOfMessage(CommonUtils.decode(latestThread.fname)));
            holder.action.setText(" 发表了新帖");
        } else {
            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    latestThread.lastreply.who,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(final Member member) {
                            holder.authorName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.USER_ID_TAG, member.uid);
                                    intent.putExtra(ProfileActivity.USER_NAME_TAG, member.username);
                                    mContext.startActivity(intent);
                                }
                            });
                            String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });

            // 热帖标志
            if (latestThread.tid_sum >= BUApplication.HOT_TOPIC_THREAD) {
                holder.isNewOrHot.setVisibility(View.VISIBLE);
                holder.isNewOrHot.setText("  HOT");
                holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.hot_topic));
            } else {
                holder.isNewOrHot.setVisibility(View.INVISIBLE);
            }

            /* 回复旧帖 */
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.lastreply.who),
                            BUApplication.MAX_USER_NAME_LENGTH));
            holder.forumName.setText(HtmlUtil.getSummaryOfMessage(CommonUtils.decode(latestThread.fname)));
            holder.action.setText(" 回复了帖子");
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.lastreply.who);
        }
    }

    @Override
    public int getItemCount() {
        return mLatestThreads == null ? 0 : mLatestThreads.size();
    }
}