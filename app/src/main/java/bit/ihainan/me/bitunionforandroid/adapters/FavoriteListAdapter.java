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
import bit.ihainan.me.bitunionforandroid.models.Favorite;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailActivity;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.DefaultViewHolder;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

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
        if (holder instanceof DefaultViewHolder) {
            // Do nothing here
            final Favorite favorite = mList.get(position);
            final DefaultViewHolder viewHolder = (DefaultViewHolder) holder;

            viewHolder.replyCount.setVisibility(View.INVISIBLE);

            Picasso.with(mContext).load(R.drawable.empty_avatar)
                    .into(viewHolder.avatar);

            // 作者
            viewHolder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(favorite.author),
                            Global.MAX_USER_NAME_LENGTH));

            // 标题
            viewHolder.title.setText(Html.fromHtml(favorite.subject));
            viewHolder.title.post(new Runnable() {
                @Override
                public void run() {
                    if (viewHolder.title.getLineCount() == 1)
                        viewHolder.title.setText(viewHolder.title.getText() + "\n     ");
                }
            });

            viewHolder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ThreadDetailActivity.class);
                    intent.putExtra(ThreadDetailActivity.THREAD_ID_TAG, favorite.tid);
                    mContext.startActivity(intent);
                }
            });

            viewHolder.date.setText("收藏于 " + favorite.dt_created);

            viewHolder.placeHolderIn.setText("  发表的主题");
            viewHolder.forumName.setVisibility(View.INVISIBLE);
            viewHolder.action.setVisibility(View.INVISIBLE);
            viewHolder.isNewOrHot.setVisibility(View.INVISIBLE);

            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    favorite.author,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            CommonUtils.setAvatarImageView(mContext, viewHolder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });

            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1,
                    CommonUtils.decode(favorite.author));
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
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
