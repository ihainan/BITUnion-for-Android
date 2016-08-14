package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.ui.viewholders.TimelineViewHolder;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * 个人主题或者回帖列表适配器
 */
public class PersonalThreadAndPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostListAdapter.class.getSimpleName();
    private final List<Post> mList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    public PersonalThreadAndPostAdapter(Context context, List<Post> list) {
        mList = list;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private final int VIEW_TYPE_POST_ITEM = 0;

    @Override
    public int getItemViewType(int position) {
        Post post = mList.get(position);
        int VIEW_TYPE_LOADING = 1;
        if (post == null) return VIEW_TYPE_LOADING;
        else return VIEW_TYPE_POST_ITEM;
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_POST_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_event_post, parent, false);
            return new TimelineViewHolder.TimelinePostViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TimelineViewHolder.TimelinePostViewHolder) {
            // Do nothing here
            final Post post = mList.get(position);
            final TimelineViewHolder.TimelinePostViewHolder viewHolder = (TimelineViewHolder.TimelinePostViewHolder) holder;

            // 占位头像
            Picasso.with(mContext).load(R.drawable.empty_avatar)
                    .into(viewHolder.avatar);

            // 公共部分
            String username = null;
            viewHolder.content.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT < 23) {
                viewHolder.title.setTextAppearance(mContext, R.style.boldText);
            } else {
                viewHolder.title.setTextAppearance(R.style.boldText);
            }

            try {
                // 帖子
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
                viewHolder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.unixTimeStampToDate(post.dateline)));

                final Intent intent = new Intent(mContext, PostListActivity.class);
                intent.putExtra(PostListActivity.THREAD_FID_TAG, post.fid);
                intent.putExtra(PostListActivity.THREAD_ID_TAG, post.tid);
                intent.putExtra(PostListActivity.THREAD_NAME_TAG, post.t_subject);
                intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                View.OnClickListener onRootClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, post.floor);
                        mContext.startActivity(intent);
                    }
                };

                View.OnClickListener onTitleClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, 0);
                        mContext.startActivity(intent);
                    }
                };

                viewHolder.rootLayout.setOnClickListener(onRootClickListener);
                viewHolder.title.setOnClickListener(onTitleClickListener);

            } catch (Exception e) {
                Log.e(TAG, "解析时间轴事件失败", e);
            }

            // 从缓存中获取用户头像
            username = username == null ? BUApplication.userSession.username : username;
            CommonUtils.getAndCacheUserInfo(mContext,
                    username,
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
                    viewHolder.avatar, -1, username);
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }
}
