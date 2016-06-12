package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;
import com.tr4android.recyclerviewslideitem.SwipeAdapter;
import com.tr4android.recyclerviewslideitem.SwipeConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Follow;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.ui.viewholders.UserViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 关注列表适配器
 */
public class FollowingListAdapter extends SwipeAdapter {
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
    public RecyclerView.ViewHolder onCreateSwipeViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_following_list, parent, true);
            return new UserViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.listview_progress_bar, parent, true);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindSwipeViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            // Do nothing here
            final Follow follow = mList.get(position);
            final UserViewHolder viewHolder = (UserViewHolder) holder;
            viewHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.USER_NAME_TAG, follow.following);
                    mContext.startActivity(intent);
                }
            });

            // 头像
            Picasso.with(mContext).load(R.drawable.empty_avatar)
                    .into(viewHolder.avatar);
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

    @Override
    public SwipeConfiguration onCreateSwipeConfiguration(Context context, int i) {
        return new SwipeConfiguration.Builder(context)
                .setLeftBackgroundColorResource(R.color.color_delete)
                .setDrawableResource(R.drawable.ic_delete_white_24dp)
                .setLeftUndoable(false)
                .setLeftUndoDescription(R.string.action_deleted)
                .setDescriptionTextColorResource(android.R.color.white)
                .setLeftSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.NORMAL_SWIPE)
                .setRightSwipeBehaviour(SwipeConfiguration.SwipeBehaviour.RESTRICTED_SWIPE)
                .build();
    }

    @Override
    public void onSwipe(int position, int direction) {
        if (direction == SWIPE_LEFT) {
            final Follow follow = mList.get(position);
            mList.remove(position);
            notifyItemRemoved(position);
            ExtraApi.delFollow(mContext, CommonUtils.decode(follow.following), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (ExtraApi.checkStatus(response)) {
                                    String message = mContext.getString(R.string.action_remove_following) + " " + CommonUtils.decode(follow.following);
                                    Log.d(TAG, message + " " + response);
                                    Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
                                    toast.show();
                                } else {
                                    String message = "取消关注失败，失败原因 " + response.getString("message");
                                    Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
                                    toast.show();
                                    Log.w(TAG, message);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, mContext.getString(R.string.error_parse_json) + ": " + response, e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String message = mContext.getString(R.string.error_network);
                            String debugMessage = "onSwipe >> " + message;
                            CommonUtils.debugToast(mContext, debugMessage);
                            Log.e(TAG, debugMessage, error);
                        }
                    }
            );
        }
    }
}
