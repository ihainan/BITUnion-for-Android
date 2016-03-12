package bit.ihainan.me.bitunionforandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.models.Post;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailActivity;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.DefaultViewHolder;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * 关注动态列表适配器
 */
public class FollowNewsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostListAdapter.class.getSimpleName();
    private List<Post> mList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    public FollowNewsListAdapter(Context context, List<Post> list) {
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
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final Post reply = mList.get(position);

        if (viewHolder instanceof DefaultViewHolder) {
            fillDefaultView(reply, (DefaultViewHolder) viewHolder);
        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    private void fillDefaultView(final Post reply, DefaultViewHolder viewHolder) {
        final DefaultViewHolder holder = viewHolder;
        Picasso.with(mContext).load(R.drawable.empty_avatar)
                .into(holder.avatar);

        // 不可见部分
        holder.placeHolderIn.setText("");
        holder.forumName.setText("");

        // 无差别区域
        holder.replyCount.setVisibility(View.INVISIBLE);
        holder.title.setText(Html.fromHtml(CommonUtils.decode(reply.subject)));
        holder.date.setText(CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate((reply.dateline))));
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ThreadDetailActivity.class);
                intent.putExtra(ThreadDetailActivity.THREAD_ID_TAG, reply.tid);
                mContext.startActivity(intent);
            }
        });

        holder.isNewOrHot.setVisibility(View.INVISIBLE);

        /* 发表新帖 */
        if (reply.level == 0) {
            // 新帖子标志

            // 其他域
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(reply.author),
                    Global.MAX_USER_NAME_LENGTH));
            holder.action.setText(" 发表了新帖");

            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(reply.author));


            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(reply.author),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });
        } else {
            /* 回复旧帖 */
            holder.authorName.setText(CommonUtils.truncateString(
                    CommonUtils.decode(reply.author),
                    Global.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(reply.author));

            holder.action.setText(" 回复了帖子");

            // 从缓存中获取用户信息
            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(reply.author),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });
        }
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
}
