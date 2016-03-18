package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Favorite;
import me.ihainan.bu.app.models.Follow;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.models.TimelineEvent;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.ui.viewholders.TimelineViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * 时间轴事件适配器
 */
public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostListAdapter.class.getSimpleName();
    private List<TimelineEvent> mList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    public TimelineAdapter(Context context, List<TimelineEvent> list) {
        mList = list;
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
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_timeline, parent, false);
            return new TimelineViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TimelineViewHolder) {
            // Do nothing here
            final TimelineEvent event = mList.get(position);
            final TimelineViewHolder viewHolder = (TimelineViewHolder) holder;

            String username = null;
            viewHolder.content.setVisibility(View.VISIBLE);

            try {
                if (event.type == 1) {
                    // 帖子
                    final Post post = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Post.class);

                    username = post.author;
                    viewHolder.username.setText(CommonUtils.decode(username));

                    if (post.floor == 0) viewHolder.action.setText("发表了主题");
                    else viewHolder.action.setText("回复了主题");

                    viewHolder.title.setText(Html.fromHtml(HtmlUtil.formatHtml(CommonUtils.decode(post.t_subject))));
                    String htmlContent = HtmlUtil.getSummaryOfMessage(HtmlUtil.formatHtml(post.message));
                    if ("".equals(htmlContent)) {
                        if (!(post.attachment == null || "".equals(post.attachment)))
                            viewHolder.content.setText("[附件]");
                        else viewHolder.content.setVisibility(View.GONE);
                    } else viewHolder.content.setText(Html.fromHtml(htmlContent));
                    viewHolder.date.setText(CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate(post.dateline)));

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, PostListActivity.class);
                            intent.putExtra(PostListActivity.THREAD_ID_TAG, post.tid);
                            intent.putExtra(PostListActivity.THREAD_NAME_TAG, post.t_subject);
                            intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, post.floor);
                            mContext.startActivity(intent);
                        }
                    };

                    viewHolder.content.setOnClickListener(onClickListener);
                    viewHolder.title.setOnClickListener(onClickListener);
                } else if (event.type == 2) {
                    // 收藏
                    final Favorite favorite = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Favorite.class);

                    username = favorite.username;
                    viewHolder.username.setText(CommonUtils.decode(username));
                    viewHolder.action.setText("收藏了主题");
                    viewHolder.title.setText(Html.fromHtml(HtmlUtil.formatHtml(favorite.subject)));
                    viewHolder.content.setVisibility(View.GONE);
                    viewHolder.date.setText(CommonUtils.formatDateTime(favorite.dt_created));

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, PostListActivity.class);
                            intent.putExtra(PostListActivity.THREAD_ID_TAG, favorite.tid);
                            intent.putExtra(PostListActivity.THREAD_AUTHOR_NAME_TAG, favorite.author);
                            intent.putExtra(PostListActivity.THREAD_NAME_TAG, favorite.subject);
                            mContext.startActivity(intent);
                        }
                    };

                    viewHolder.title.setOnClickListener(onClickListener);
                } else if (event.type == 3) {
                    // 关注
                    final Follow follow = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Follow.class);

                    username = follow.following;
                    viewHolder.username.setText(CommonUtils.decode(follow.follower));

                    viewHolder.action.setText("关注了用户");
                    viewHolder.title.setText(CommonUtils.decode(follow.following));

                    viewHolder.content.setVisibility(View.GONE);

                    viewHolder.date.setText(CommonUtils.formatDateTime(follow.dt_created));

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.USER_NAME_TAG, follow.following);
                            mContext.startActivity(intent);
                        }
                    };

                    viewHolder.title.setOnClickListener(onClickListener);
                }
            } catch (Exception e) {
                Log.e(TAG, "解析时间轴事件失败", e);
            }

            // 从缓存中获取用户头像
            username = username == null ? Global.userSession.username : username;
            CommonUtils.getAndCacheUserInfo(mContext,
                    username,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                            CommonUtils.setAvatarImageView(mContext, viewHolder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });

            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1, username);
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }
}
