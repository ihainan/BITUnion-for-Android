package me.ihainan.bu.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * 帖子相关搜索结果适配器
 */
public class SearchThreadOrPostResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = SearchThreadOrPostResultAdapter.class.getSimpleName();
    private final List<Post> mList;
    private String mKeyword;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final boolean mIsThread;

    public SearchThreadOrPostResultAdapter(Context context, String keyword, List<Post> list, Boolean isThread) {
        mList = list;
        mKeyword = keyword;
        mIsThread = isThread;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private final int VIEW_TYPE_ITEM = 0;

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
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
            view = mLayoutInflater.inflate(R.layout.item_event_post, parent, false);
            return new TimelineViewHolder.TimelinePostViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    public void setKeyword(String keyword) {
        mKeyword = keyword;
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
            String username = post.author;
            viewHolder.content.setVisibility(View.INVISIBLE);
            // viewHolder.title.setTextAppearance(mContext, R.style.boldText);
            viewHolder.username.setText(username);
            viewHolder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.unixTimeStampToDate(post.dateline)));
            final Intent intent = new Intent(mContext, PostListActivity.class);
            intent.putExtra(PostListActivity.THREAD_FID_TAG, post.fid);
            intent.putExtra(PostListActivity.THREAD_ID_TAG, post.tid);
            intent.putExtra(PostListActivity.THREAD_NAME_TAG, post.t_subject);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (((Activity) mContext).isInMultiWindowMode()) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                }
            }
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

            if (mIsThread) {
                viewHolder.content.setVisibility(View.GONE);
                viewHolder.action.setText("发表的主题");
                String formatSubject = highLightStr(CommonUtils.decode(post.subject));
                viewHolder.title.setText(Html.fromHtml(HtmlUtil.formatHtml(formatSubject)));
            } else {
                viewHolder.content.setVisibility(View.VISIBLE);
                viewHolder.action.setText("发表的帖子");
                String content = HtmlUtil.getSummaryOfMessage(HtmlUtil.formatHtml(post.message));
                String htmlContent = highLightStr(content);

                if ("".equals(htmlContent)) {
                    if (!(post.attachment == null || "".equals(post.attachment)))
                        viewHolder.content.setText("[附件]");
                    else viewHolder.content.setVisibility(View.GONE);
                } else viewHolder.content.setText(Html.fromHtml(htmlContent));
                viewHolder.title.setText(Html.fromHtml(HtmlUtil.formatHtml(CommonUtils.decode(post.t_subject == null ? "Title" : post.t_subject))));
            }

            // 从缓存中获取用户头像
            username = username == null ? BUApplication.userSession.username : username;
            CommonUtils.getAndCacheUserInfo(mContext,
                    username,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(final Member member) {
                            viewHolder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.USER_ID_TAG, member.uid);
                                    intent.putExtra(ProfileActivity.USER_NAME_TAG, member.username);
                                    mContext.startActivity(intent);
                                }
                            });
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

    private String highLightStr(String str) {
        Pattern pattern = Pattern.compile("((?i)" + mKeyword + ")");
        Matcher matcher = pattern.matcher(str);
        boolean firstTime = true;
        while (matcher.find()) {
            if (firstTime) {
                if (matcher.start() >= 80)
                    str = "……" + str.substring(matcher.start() - 70);
                firstTime = false;
            }
            str = str.replace(matcher.group(1), "<b><font color = '#426aa3'>" + matcher.group(1) + "</font></b>");
        }

        return str;
    }
}
