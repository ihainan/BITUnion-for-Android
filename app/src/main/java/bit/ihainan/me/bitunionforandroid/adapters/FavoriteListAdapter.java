package bit.ihainan.me.bitunionforandroid.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Favorite;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.ui.PostListActivity;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.TimelineViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;

/**
 * 收藏列表适配器
 */
public class FavoriteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostListAdapter.class.getSimpleName();
    private List<Favorite> mList;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;


    public FavoriteListAdapter(Context context, List<Favorite> list) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof TimelineViewHolder) {
            // Do nothing here
            final Favorite favorite = mList.get(position);
            final TimelineViewHolder viewHolder = (TimelineViewHolder) holder;

            // 收藏
            String username = favorite.author;
            viewHolder.username.setText(username);
            viewHolder.action.setText("发表的主题");
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

            // 从缓存中获取用户头像
            // viewHolder.avatar.setVisibility(View.GONE);
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
}
