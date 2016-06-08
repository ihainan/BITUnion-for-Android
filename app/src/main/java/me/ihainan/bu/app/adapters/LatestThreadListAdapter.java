package me.ihainan.bu.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import jp.wasabeef.picasso.transformations.gpu.VignetteFilterTransformation;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.LatestThread;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.viewholders.DefaultViewHolder;
import me.ihainan.bu.app.ui.viewholders.SelfieViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * Forum LatestThread List Adapter
 */
public class LatestThreadListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = LatestThreadListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List<LatestThread> mLatestThreads;

    public LatestThreadListAdapter(Context context, List<LatestThread> latestThreads) {
        mLatestThreads = latestThreads;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                view = mLayoutInflater.inflate(R.layout.item_thread_item, parent, false);
                viewHolder = new DefaultViewHolder(view);
                break;
            default:
                view = mLayoutInflater.inflate(R.layout.item_thread_selfie, parent, false);
                viewHolder = new SelfieViewHolder(view);
                break;
        }

        return viewHolder;
    }

    private final static int VIEW_TYPE_DEFAULT = 1;
    private final static int VIEW_TYPE_SELFIE = 2;

    @Override
    public int getItemViewType(int position) {
        final LatestThread latestThread = mLatestThreads.get(position);
        if (BUApplication.saveDataMode || !CommonUtils.decode(latestThread.fname).equals("个人展示区"))
            return VIEW_TYPE_DEFAULT;
        else return VIEW_TYPE_SELFIE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final LatestThread latestThread = mLatestThreads.get(position);
        // 注意节省流量模式
        if (getItemViewType(position) == VIEW_TYPE_DEFAULT) {
            fillDefaultView(latestThread, viewHolder);
        } else {
            fillSelfieView(latestThread, viewHolder);
        }
    }

    /**
     * 填充非 『个人展示区』 帖子内容
     *
     * @param latestThread 当前需要显示的帖子
     * @param viewHolder   View holder
     */
    private void fillDefaultView(final LatestThread latestThread, RecyclerView.ViewHolder viewHolder) {
        final DefaultViewHolder holder = (DefaultViewHolder) viewHolder;

        // 无差别区域
        Picasso.with(mContext).load(R.drawable.empty_avatar)
                .into(holder.avatar);
        holder.replyCount.setText("" + latestThread.tid_sum + " 回复");
        holder.title.setText(Html.fromHtml(CommonUtils.decode(latestThread.pname)));
        holder.title.post(new Runnable() {
            @Override
            public void run() {
                if (holder.title.getLineCount() == 1) {
                    // holder.title.setText(holder.title.getText() + "\n     ");
                }
            }
        });

        final Intent intent = new Intent(mContext, PostListActivity.class);
        intent.putExtra(PostListActivity.THREAD_FID_TAG, latestThread.fid);
        intent.putExtra(PostListActivity.THREAD_ID_TAG, latestThread.tid);
        intent.putExtra(PostListActivity.THREAD_AUTHOR_NAME_TAG, latestThread.author);
        intent.putExtra(PostListActivity.THREAD_REPLY_COUNT_TAG, latestThread.tid_sum + 1);
        intent.putExtra(PostListActivity.THREAD_NAME_TAG, latestThread.pname);
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.removeExtra(PostListActivity.THREAD_JUMP_FLOOR);
                mContext.startActivity(intent);
            }
        });

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, 0);
                mContext.startActivity(intent);
            }
        });

        // 发帖、回帖日期
        if (latestThread.lastreply != null) {
            // holder.date.setText(CommonUtils.decode(latestThread.lastreply.when));
            holder.date.setText(CommonUtils.getRelativeTimeSpanString(CommonUtils.parseDateString(CommonUtils.decode(latestThread.lastreply.when))));
        } else {
            holder.date.setText("未知次元未知时间");
        }

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            String avatarURL = CommonUtils.getRealImageURL(latestThread.avatar);
            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                    avatarURL, R.drawable.default_avatar);

            // 新帖子标志
            holder.isNewOrHot.setVisibility(View.VISIBLE);
            holder.isNewOrHot.setText("  NEW");
            holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.primary_dark));

            // 其他域
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            BUApplication.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.lastreply.who);
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 发表了新帖");
        } else {
            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    latestThread.lastreply.who,
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

            // 热帖标志
            if (latestThread.tid_sum >= BUApplication.HOT_TOPIC_THREAD) {
                holder.isNewOrHot.setVisibility(View.VISIBLE);
                holder.isNewOrHot.setText("  HOT");
                holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.hot_topic));
            } else {
                holder.isNewOrHot.setVisibility(View.INVISIBLE);
            }

            /* 回复旧帖 */
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.lastreply.who),
                            BUApplication.MAX_USER_NAME_LENGTH));
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 回复了帖子");
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.lastreply.who);
        }
    }

    /**
     * 填充 『个人展示区』 帖子内容
     *
     * @param latestThread 当前需要显示的帖子
     * @param viewHolder   View holder
     */
    private void fillSelfieView(final LatestThread latestThread, RecyclerView.ViewHolder viewHolder) {
        final SelfieViewHolder holder = (SelfieViewHolder) viewHolder;

        // 标题
        holder.title.setText(Html.fromHtml(CommonUtils.decode(latestThread.pname)));
        Picasso.with(mContext).load(R.drawable.empty_avatar)
                .into(holder.avatar);

        final Intent intent = new Intent(mContext, PostListActivity.class);
        intent.putExtra(PostListActivity.THREAD_FID_TAG, latestThread.fid);
        intent.putExtra(PostListActivity.THREAD_ID_TAG, latestThread.tid);
        intent.putExtra(PostListActivity.THREAD_AUTHOR_NAME_TAG, latestThread.author);
        intent.putExtra(PostListActivity.THREAD_REPLY_COUNT_TAG, latestThread.tid_sum + 1);
        intent.putExtra(PostListActivity.THREAD_NAME_TAG, latestThread.pname);

        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.removeExtra(PostListActivity.THREAD_JUMP_FLOOR);
                mContext.startActivity(intent);
            }
        });

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, 0);
                mContext.startActivity(intent);
            }
        });

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            BUApplication.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.author);
            holder.action.setText(" 发布了自拍");
            String avatarURL = CommonUtils.getRealImageURL(latestThread.avatar);
            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                    avatarURL, R.drawable.default_avatar);
        } else {
            /* 回复旧帖 */
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.lastreply.who),
                            BUApplication.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    latestThread.lastreply.who);
            holder.action.setText(" 评价了 " +
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            BUApplication.MAX_USER_NAME_LENGTH) + " 的自拍");

            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    latestThread.lastreply.who,
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(final Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                            holder.authorName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(mContext, ProfileActivity.class);
                                    intent.putExtra(ProfileActivity.USER_ID_TAG, member.uid);
                                    intent.putExtra(ProfileActivity.USER_NAME_TAG, member.username);
                                    mContext.startActivity(intent);
                                }
                            });
                            // 缓存模式下不会进入本方法，所以直接显示图片
                            if (avatarURL.endsWith("/images/standard/noavatar.gif")) {
                                Picasso.with(mContext).load(R.drawable.default_avatar)
                                        .error(R.drawable.default_avatar)
                                        .into(holder.avatar);
                            } else {
                                Picasso.with(mContext).load(avatarURL)
                                        .error(R.drawable.default_avatar)
                                        .into(holder.avatar);
                            }
                        }
                    });
        }

        // 获取背景图片
        Post reply = (Post) BUApplication.getCache(mContext).getAsObject(BUApplication.CACHE_LATEST_THREAD_FIRST_POST + "_" + latestThread.tid);
        if (reply == null) {
            Log.i(TAG, "fillDefaultView >> 拉取回复数据");
            BUApi.getPostReplies(mContext, latestThread.tid, 0, 1, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (((Activity) mContext).isFinishing()) return;
                        if (BUApi.checkStatus(response)) {
                            JSONArray newListJson = response.getJSONArray("postlist");
                            List<Post> postReplies = BUApi.MAPPER.readValue(newListJson.toString(),
                                    new TypeReference<List<Post>>() {
                                    });
                            if (postReplies != null && postReplies.size() > 0) {
                                Post firstReply = postReplies.get(0);
                                Log.i(TAG, "fillSelfieView >> 拉取得到回复数据，放入缓存：" + firstReply);
                                BUApplication.getCache(mContext).put(BUApplication.CACHE_LATEST_THREAD_FIRST_POST + "_" + latestThread.tid, firstReply);

                                if (firstReply.attachext.equals("png") || firstReply.attachext.equals("jpg")
                                        || firstReply.attachext.equals("jpeg")) {
                                    // 缓存模式下不会进入本方法，所以直接显示图片
                                    String imageURL = CommonUtils.getRealImageURL(firstReply.attachment);

                                    // 调整图片显示大小
                                    final Point displaySize = CommonUtils.getDisplaySize(((Activity) mContext).getWindowManager().getDefaultDisplay());
                                    // final int size = (int) Math.ceil(Math.sqrt(displaySize.x * displaySize.y));
                                    Picasso.with(mContext).load(imageURL).resize(displaySize.x, 0)
                                            .transform(new VignetteFilterTransformation(mContext)).into(holder.background);
                                }
                            } else {
                                handleUnknownError(response);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, mContext.getString(R.string.error_parse_json) + "\n" + response, e);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (((Activity) mContext).isFinishing()) return;
                    String message = mContext.getString(R.string.error_network);
                    String debugMessage = "getPostReplies >> " + message;
                    CommonUtils.debugToast(mContext, debugMessage);
                    Log.e(TAG, debugMessage, error);
                }
            });
        } else {
            Log.i(TAG, "fillSelfieView >> 从缓存中拿到回复数据 " + reply);
            if (reply.attachext.equals("png") || reply.attachext.equals("jpg")
                    || reply.attachext.equals("jpeg")) {
                String imageURL = CommonUtils.getRealImageURL(reply.attachment);

                final Point displaySize = CommonUtils.getDisplaySize(((Activity) mContext).getWindowManager().getDefaultDisplay());
                // final int size = (int) Math.ceil(Math.sqrt(displaySize.x * displaySize.y));
                /* Picasso.with(mContext).load(imageURL).resize(size, size)
                        .transform(new VignetteFilterTransformation(mContext)).into(holder.background); */
                Picasso.with(mContext).load(imageURL).resize(displaySize.x, 0)
                        .transform(new VignetteFilterTransformation(mContext)).into(holder.background);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mLatestThreads == null ? 0 : mLatestThreads.size();
    }

    private void handleUnknownError(JSONObject response) throws JSONException {
        String message = mContext.getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
        String debugMessage = message + " - " + response;
        Log.w(TAG, debugMessage);
        CommonUtils.debugToast(mContext, debugMessage);
    }
}