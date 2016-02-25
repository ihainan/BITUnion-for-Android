package bit.ihainan.me.bitunionforandroid.adapters;

import android.content.Context;
import android.content.Intent;
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
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.LatestThread;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.models.ThreadReply;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailActivity;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.DefaultViewHolder;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.SelfieViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.Api;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

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
        if (!CommonUtils.decode(latestThread.fname).equals("个人展示区")) return VIEW_TYPE_DEFAULT;
        else return VIEW_TYPE_SELFIE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final LatestThread latestThread = mLatestThreads.get(position);
        // 注意节省流量模式
        if (Global.saveDataMode || getItemViewType(position) == VIEW_TYPE_DEFAULT) {
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
        holder.replyCount.setText(CommonUtils.decode("" + latestThread.tid_sum + " 回复"));
        holder.title.setText(Html.fromHtml(CommonUtils.decode(latestThread.pname)));
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ThreadDetailActivity.class);
                intent.putExtra(ThreadDetailActivity.THREAD_ID_TAG, latestThread.tid);
                intent.putExtra(ThreadDetailActivity.THREAD_NAME_TAG, CommonUtils.decode(latestThread.pname));
                intent.putExtra(ThreadDetailActivity.THREAD_REPLY_COUNT_TAG, latestThread.tid_sum + 1);
                intent.putExtra(ThreadDetailActivity.THREAD_AUTHOR_NAME_TAG, CommonUtils.decode(latestThread.author));
                mContext.startActivity(intent);
            }
        });

        // 发帖、回帖日期
        if (latestThread.lastreply != null)
            holder.date.setText(CommonUtils.decode(latestThread.lastreply.when));
        else
            holder.date.setText("未知次元未知时间");

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(latestThread.avatar));
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
                            Global.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(latestThread.lastreply.who));
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 发表了新帖");
        } else {
            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(latestThread.lastreply.who),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                                    avatarURL, R.drawable.default_avatar);
                        }
                    });

            // 热帖标志
            if (latestThread.tid_sum >= Global.HOT_TOPIC_THREAD) {
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
                            Global.MAX_USER_NAME_LENGTH));
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 回复了帖子");
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(latestThread.lastreply.who));
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
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ThreadDetailActivity.class);
                intent.putExtra(ThreadDetailActivity.THREAD_ID_TAG, latestThread.tid);
                intent.putExtra(ThreadDetailActivity.THREAD_NAME_TAG, CommonUtils.decode(latestThread.pname));
                intent.putExtra(ThreadDetailActivity.THREAD_REPLY_COUNT_TAG, latestThread.tid_sum + 1);
                intent.putExtra(ThreadDetailActivity.THREAD_AUTHOR_NAME_TAG, CommonUtils.decode(latestThread.author));
                mContext.startActivity(intent);
            }
        });

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            Global.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(latestThread.author));
            holder.action.setText(" 发布了自拍");
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(latestThread.avatar));
            CommonUtils.setAvatarImageView(mContext, holder.avatar,
                    avatarURL, R.drawable.default_avatar);
        } else {
            /* 回复旧帖 */
            holder.authorName.setText(
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.lastreply.who),
                            Global.MAX_USER_NAME_LENGTH));
            CommonUtils.setUserAvatarClickListener(mContext,
                    holder.avatar, -1,
                    CommonUtils.decode(latestThread.lastreply.who));
            holder.action.setText(" 评价了 " +
                    CommonUtils.truncateString(
                            CommonUtils.decode(latestThread.author),
                            Global.MAX_USER_NAME_LENGTH) + " 的自拍");

            // 从缓存中获取用户头像
            CommonUtils.getAndCacheUserInfo(mContext,
                    CommonUtils.decode(latestThread.lastreply.who),
                    new CommonUtils.UserInfoAndFillAvatarCallback() {
                        @Override
                        public void doSomethingIfHasCached(Member member) {
                            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
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
        ThreadReply reply = (ThreadReply) Global.getCache(mContext).getAsObject(Global.CACHE_REPLY_CONTENT + "_" + latestThread.tid);
        Picasso.with(mContext).load(R.drawable.background).into(holder.background);
        if (reply == null) {
            Log.i(TAG, "fillDefaultView >> 拉取回复数据");
            Api.getPostReplies(mContext, latestThread.tid, 0, 1, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (Api.checkStatus(response)) {
                            JSONArray newListJson = response.getJSONArray("postlist");
                            List<ThreadReply> postReplies = Api.MAPPER.readValue(newListJson.toString(),
                                    new TypeReference<List<ThreadReply>>() {
                                    });
                            if (postReplies != null && postReplies.size() > 0) {
                                ThreadReply firstReply = postReplies.get(0);
                                Log.i(TAG, "fillSelfieView >> 拉取得到回复数据，放入缓存：" + firstReply);
                                Global.getCache(mContext).put(Global.CACHE_REPLY_CONTENT + "_" + latestThread.tid, firstReply);

                                if (firstReply.attachext.equals("png") || firstReply.attachext.equals("jpg")
                                        || firstReply.attachext.equals("jpeg")) {
                                    // 缓存模式下不会进入本方法，所以直接显示图片
                                    String imageURL = CommonUtils.getRealImageURL(CommonUtils.decode(firstReply.attachment));
                                    Picasso.with(mContext).load(imageURL)
                                            .placeholder(R.drawable.background)
                                            .error(R.drawable.background)
                                            .into(holder.background);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, mContext.getString(R.string.error_parse_json) + "\n" + response, e);
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, mContext.getString(R.string.error_network), error);
                }
            });
        } else {
            Log.i(TAG, "fillSelfieView >> 从缓存中拿到回复数据 " + reply);
            if (reply.attachext.equals("png") || reply.attachext.equals("jpg")
                    || reply.attachext.equals("jpeg")) {
                String imageURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.attachment));
                Picasso.with(mContext).load(imageURL)
                        .placeholder(R.drawable.nav_background)
                        .error(R.drawable.nav_background)
                        .into(holder.background);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mLatestThreads == null ? 0 : mLatestThreads.size();
    }
}