package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import co.dift.ui.SwipeToAction;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Follow;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * 关注列表 Adapter
 */
public class FollowingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = FollowingListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private List<Follow> mList;
    private final Context mContext;

    public FollowingListAdapter(Context context, List<Follow> mList) {
        this.mContext = context;
        this.mList = mList;
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
            view = mLayoutInflater.inflate(R.layout.item_following_list, parent, false);
            return new FollowViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FollowViewHolder) {
            // Do nothing here
            final Follow follow = mList.get(position);
            final FollowViewHolder viewHolder = (FollowViewHolder) holder;
            viewHolder.data = follow;

            // 头像
            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1,
                    follow.following);
            CommonUtils.getAndCacheUserInfo(mContext, follow.following,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = member.avatar == null ? "" : CommonUtils.getRealImageURL(member.avatar);
                            CommonUtils.setAvatarImageView(mContext, viewHolder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });


            // 名字
            viewHolder.username.setText(CommonUtils.decode(follow.following));
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class FollowViewHolder extends SwipeToAction.ViewHolder<Follow> {
        public ImageView avatar;
        public TextView username;

        public FollowViewHolder(View itemView) {
            super(itemView);

            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            username = (TextView) itemView.findViewById(R.id.username);
        }
    }
}
