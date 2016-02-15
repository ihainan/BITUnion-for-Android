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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.LatestThread;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.models.ThreadReply;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailActivity;
import bit.ihainan.me.bitunionforandroid.ui.UserInfoActivity;
import bit.ihainan.me.bitunionforandroid.utils.ACache;
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

        if (latestThread.lastreply != null)
            holder.date.setText(CommonUtils.decode(latestThread.lastreply.when));

        /* 发表新帖 */
        if (latestThread.lastreply == null || latestThread.tid_sum == 0) {
            // 新帖子标志
            holder.isNewOrHot.setVisibility(View.VISIBLE);
            holder.isNewOrHot.setText("  NEW");
            holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.primary));

            // 其他域
            holder.authorName.setText(CommonUtils.decode(latestThread.author));
            setUserClickListener(mContext, holder.avatar, -1, CommonUtils.decode(latestThread.lastreply.who));
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 发表了新帖");
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(""));
            Picasso.with(mContext)
                    .load(avatarURL)
                    .error(R.drawable.default_avatar)
                    .into(holder.avatar);
        } else {
            /* 回复旧帖 */
            holder.isNewOrHot.setVisibility(View.INVISIBLE);
            holder.authorName.setText(CommonUtils.decode(latestThread.lastreply.who));
            holder.forumName.setText(CommonUtils.decode(latestThread.fname));
            holder.action.setText(" 回复了 " + CommonUtils.decode(latestThread.author) + " 的帖子");
            setUserClickListener(mContext, holder.avatar, -1, CommonUtils.decode(latestThread.lastreply.who));

            // 热点（Hot）
            if (latestThread.tid_sum >= Global.HOT_TOPIC_THREAD) {
                holder.isNewOrHot.setVisibility(View.VISIBLE);
                holder.isNewOrHot.setText("  HOT");
                holder.isNewOrHot.setTextColor(ContextCompat.getColor(mContext, R.color.hot_topic));
            }

            // 从缓存中获取用户信息
            final Member lastReplyMember = (Member) Global.getCache(mContext)
                    .getAsObject(Global.CACHE_USER_INFO + latestThread.lastreply.who);
            if (lastReplyMember == null) {
                Log.i(TAG, "fillDefaultView >> 拉取用户数据");

                // 从服务器拉取数据并写入到缓存当中
                Api.getUserInfo(mContext, -1,
                        CommonUtils.decode(latestThread.lastreply.who),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (Api.checkStatus(response)) {
                                    try {
                                        Member member = Api.MAPPER.readValue(
                                                response.getJSONObject("memberinfo").toString(),
                                                Member.class);
                                        String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                                        Picasso.with(mContext).load(avatarURL)
                                                .error(R.drawable.default_avatar)
                                                .into(holder.avatar);

                                        // 将用户信息放入到缓存当中
                                        Log.i(TAG, "fillDefaultView >> 拉取得到用户数据，放入缓存：" + member);
                                        Global.getCache(mContext).put(
                                                Global.CACHE_USER_INFO + latestThread.lastreply.who,
                                                member,
                                                Global.cacheDays * ACache.TIME_DAY);
                                    } catch (Exception e) {
                                        Log.e(TAG, mContext.getString(R.string.error_parse_json) + "\n" + response, e);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, mContext.getString(R.string.error_network), error);
                            }
                        });

            } else {
                Log.i(TAG, "fillDefaultView >> 从缓存中拿到用户数据 " + lastReplyMember);
                String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(lastReplyMember.avatar));
                Picasso.with(mContext).load(avatarURL)
                        .error(R.drawable.default_avatar)
                        .into(holder.avatar);
            }
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
            holder.authorName.setText(CommonUtils.decode(latestThread.author));
            setUserClickListener(mContext, holder.avatar, -1, CommonUtils.decode(latestThread.author));
            holder.action.setText(" 发布了自拍");
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(latestThread.avatar));
            Picasso.with(mContext)
                    .load(avatarURL)
                    .error(R.drawable.default_avatar)
                    .into(holder.avatar);
        } else {
            /* 回复旧帖 */
            holder.authorName.setText(CommonUtils.decode(latestThread.lastreply.who));
            setUserClickListener(mContext, holder.avatar, -1, CommonUtils.decode(latestThread.lastreply.who));
            holder.action.setText(" 评价了 " + CommonUtils.decode(latestThread.author) + " 的自拍");

            // 从缓存中获取用户信息
            Member lastReplyMember = (Member) Global.getCache(mContext)
                    .getAsObject(Global.CACHE_USER_INFO + latestThread.lastreply.who);
            if (lastReplyMember == null) {
                // 从服务器拉取数据并写入到缓存当中
                Log.i(TAG, "fillDefaultView >> 拉取用户数据");
                Api.getUserInfo(mContext, -1,
                        CommonUtils.decode(latestThread.lastreply.who),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (Api.checkStatus(response)) {
                                    try {
                                        Member member = Api.MAPPER.readValue(
                                                response.getJSONObject("memberinfo").toString(),
                                                Member.class);
                                        String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                                        Picasso.with(mContext).load(avatarURL)
                                                .error(R.drawable.default_avatar)
                                                .into(holder.avatar);

                                        // 将用户信息放入到缓存当中
                                        Log.i(TAG, "fillSelfieView >> 拉取得到用户数据，放入缓存：" + member);
                                        Global.getCache(mContext).put(
                                                Global.CACHE_USER_INFO + latestThread.lastreply.who,
                                                member,
                                                Global.cacheDays * ACache.TIME_DAY);
                                    } catch (Exception e) {
                                        Log.e(TAG, mContext.getString(R.string.error_parse_json) + "\n" + response, e);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, mContext.getString(R.string.error_network), error);
                            }
                        });

            } else {
                Log.i(TAG, "fillSelfieView >> 从缓存中拿到用户数据 " + lastReplyMember);
                String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(lastReplyMember.avatar));
                Picasso.with(mContext).load(avatarURL)
                        .error(R.drawable.default_avatar)
                        .into(holder.avatar);
            }
        }

        // 获取背景图片
        ThreadReply reply = (ThreadReply) Global.getCache(mContext).getAsObject(Global.CACHE_REPLY_CONTENT + "_" + latestThread.tid);
        Picasso.with(mContext).load(R.drawable.nav_background).into(holder.background);
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
                                    String imageURL = CommonUtils.getRealImageURL(CommonUtils.decode(firstReply.attachment));
                                    Picasso.with(mContext).load(imageURL)
                                            .placeholder(R.drawable.nav_background)
                                            .error(R.drawable.nav_background)
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

        // 标题
        holder.title.setText(Html.fromHtml(CommonUtils.decode(latestThread.pname)));
    }

    public static void setUserClickListener(final Context context, View view, final long userId, final String userName) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.USER_ID_TAG, userId);
                intent.putExtra(UserInfoActivity.USER_NAME_TAG, userName);
                context.startActivity(intent);
            }
        });
    }

    public static void fillImageView(ImageView imageView, String imageUrl) {

    }

    public static class SelfieViewHolder extends RecyclerView.ViewHolder {
        public TextView authorName;
        public TextView action;
        public TextView title;
        public ImageView background;
        public ImageView avatar;

        public SelfieViewHolder(View view) {
            super(view);
            background = (ImageView) view.findViewById(R.id.post_item_background);
            authorName = (TextView) view.findViewById(R.id.thread_item_author);
            title = (TextView) view.findViewById(R.id.thread_item_title);
            action = (TextView) view.findViewById(R.id.thread_item_action);
            avatar = (ImageView) view.findViewById(R.id.thread_item_avatar);
        }
    }

    public static class DefaultViewHolder extends RecyclerView.ViewHolder {
        public TextView authorName;
        public TextView forumName;
        public TextView action;
        public TextView title;
        public ImageView avatar;
        public TextView replyCount;
        public TextView date;
        public TextView isNewOrHot;
        public TextView placeHolderIn;

        public DefaultViewHolder(View view) {
            super(view);
            authorName = (TextView) view.findViewById(R.id.thread_item_author);
            forumName = (TextView) view.findViewById(R.id.thread_item_forum);
            title = (TextView) view.findViewById(R.id.thread_item_title);
            avatar = (ImageView) view.findViewById(R.id.thread_item_avatar);
            replyCount = (TextView) view.findViewById(R.id.thread_item_reply);
            action = (TextView) view.findViewById(R.id.thread_item_action);
            date = (TextView) view.findViewById(R.id.thread_item_date);
            isNewOrHot = (TextView) view.findViewById(R.id.thread_item_new_or_hot);
            placeHolderIn = (TextView) view.findViewById(R.id.thread_item_in);
        }
    }

    @Override
    public int getItemCount() {
        return mLatestThreads == null ? 0 : mLatestThreads.size();
    }
}
