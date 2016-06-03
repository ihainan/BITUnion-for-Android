package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Thread;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.DefaultViewHolder;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;

/**
 * Forum LatestThread List Adapter
 */
public class ThreadListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = ThreadListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final Long mFid;
    private List<me.ihainan.bu.app.models.Thread> mPosts;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public ThreadListAdapter(Context context, Long fid, List<Thread> posts) {
        mPosts = posts;
        mFid = fid;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return mPosts.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_thread_item, parent, false);
            return new DefaultViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final Thread post = mPosts.get(position);

        if (viewHolder instanceof DefaultViewHolder) {
            fillDefaultView(post, (DefaultViewHolder) viewHolder);
        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }


    private void fillDefaultView(final Thread thread, DefaultViewHolder viewHolder) {
        final DefaultViewHolder holder = viewHolder;
        Picasso.with(mContext).load(R.drawable.empty_avatar)
                .into(holder.avatar);

        // 不可见部分
        holder.placeHolderIn.setText("");
        holder.forumName.setText("");

        // 无差别区域
        holder.replyCount.setText(CommonUtils.decode("" + thread.replies + " 回复"));
        holder.title.setText(Html.fromHtml(CommonUtils.decode(thread.subject)));
        holder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.unixTimeStampToDate((thread.lastpost))));
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PostListActivity.class);
                intent.putExtra(PostListActivity.THREAD_ID_TAG, thread.tid);
                intent.putExtra(PostListActivity.THREAD_NAME_TAG, thread.subject);
                intent.putExtra(PostListActivity.THREAD_REPLY_COUNT_TAG, thread.replies + 1);
                intent.putExtra(PostListActivity.THREAD_AUTHOR_NAME_TAG, thread.author);
                mContext.startActivity(intent);
            }
        });

        /* 发表新帖 */
        if (thread.replies == 0) {
            // 新帖子标志
            holder.isNewOrHot.setVisibility(View.VISIBLE);
            holder.isNewOrHot.setText("  NEW");
            holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.primary_dark));

            // 其他域
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(thread.author),
                    BUApplication.MAX_USER_NAME_LENGTH));
            holder.action.setText(" 发表了新帖");

            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    thread.author);


            CommonUtils.getAndCacheUserInfo(mContext, thread.author,
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
        } else {
            /* 回复旧帖 */
            holder.isNewOrHot.setVisibility(View.INVISIBLE);
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(thread.lastposter),
                    BUApplication.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    thread.lastposter);

            holder.action.setText(" 回复了帖子");

            // 热点（Hot）
            if (thread.replies >= BUApplication.HOT_TOPIC_THREAD) {
                holder.isNewOrHot.setVisibility(View.VISIBLE);
                holder.isNewOrHot.setText("  HOT");
                holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.hot_topic));
            }

            // 从缓存中获取用户信息
            CommonUtils.getAndCacheUserInfo(mContext, thread.lastposter,
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
        }
    }

    @Override
    public int getItemCount() {
        return mPosts == null ? 0 : mPosts.size();
    }
}
