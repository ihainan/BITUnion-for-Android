package bit.ihainan.me.bitunionforandroid.adapters;

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

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.models.Thread;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailActivity;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Forum LatestThread List Adapter
 */
public class ThreadListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = ThreadListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<bit.ihainan.me.bitunionforandroid.models.Thread> mPosts;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    public ThreadListAdapter(Context context, List<Thread> posts) {
        mPosts = posts;
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
            return new LatestThreadListAdapter.DefaultViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final Thread post = mPosts.get(position);

        if (viewHolder instanceof LatestThreadListAdapter.DefaultViewHolder) {
            fillDefaultView(post, (LatestThreadListAdapter.DefaultViewHolder) viewHolder);
        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }


    private void fillDefaultView(final Thread thread, LatestThreadListAdapter.DefaultViewHolder viewHolder) {
        final LatestThreadListAdapter.DefaultViewHolder holder = viewHolder;

        // 不可见部分
        holder.placeHolderIn.setText("");
        holder.forumName.setText("");

        // 无差别区域
        holder.replyCount.setText(CommonUtils.decode("" + thread.replies + " 回复"));
        holder.title.setText(Html.fromHtml(CommonUtils.decode(thread.subject)));
        holder.date.setText(CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate((thread.dateline))));
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ThreadDetailActivity.class);
                intent.putExtra(ThreadDetailActivity.THREAD_ID_TAG, thread.tid);
                intent.putExtra(ThreadDetailActivity.THREAD_NAME_TAG, CommonUtils.decode(thread.subject));
                intent.putExtra(ThreadDetailActivity.THREAD_REPLY_COUNT_TAG, thread.replies + 1);
                intent.putExtra(ThreadDetailActivity.THREAD_AUTHOR_NAME_TAG, CommonUtils.decode(thread.author));
                mContext.startActivity(intent);
            }
        });

        /* 发表新帖 */
        if (thread.replies == 0) {
            // 新帖子标志
            holder.isNewOrHot.setVisibility(View.VISIBLE);
            holder.isNewOrHot.setText("  NEW");
            holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.primary));

            // 其他域
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(thread.author),
                    Global.MAX_USER_NAME_LENGTH));
            holder.action.setText(" 发表了新帖");

            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(thread.author));


            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(thread.author),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            Picasso.with(mContext)
                                    .load(avatarURL)
                                    .error(R.drawable.default_avatar)
                                    .into(holder.avatar);
                        }
                    });
        } else {
            /* 回复旧帖 */
            holder.isNewOrHot.setVisibility(View.INVISIBLE);
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(thread.lastposter),
                    Global.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(thread.lastposter));

            holder.action.setText(" 回复了 " + CommonUtils.truncateString(
                    CommonUtils.decode(thread.author),
                    Global.MAX_USER_NAME_LENGTH) + " 的帖子");

            // 热点（Hot）
            if (thread.replies >= Global.HOT_TOPIC_THREAD) {
                holder.isNewOrHot.setVisibility(View.VISIBLE);
                holder.isNewOrHot.setText("  HOT");
                holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.hot_topic));
            }

            // 从缓存中获取用户信息
            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(thread.author),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            Picasso.with(mContext).load(avatarURL)
                                    .error(R.drawable.default_avatar)
                                    .into(holder.avatar);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return mPosts == null ? 0 : mPosts.size();
    }
}
