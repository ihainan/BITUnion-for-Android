package bit.ihainan.me.bitunionforandroid.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.ThreadReply;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.ui.viewholders.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.PicassoImageGetter;

/**
 * Just for test
 */
public class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final String mAuthorName;
    private final long mReplyCount;
    private final Context mContext;
    private List<ThreadReply> mList;
    private ImageView mCover;   // 标题图片

    public PostListAdapter(Context context, List<ThreadReply> mList, String authorName, long replyCount) {
        this.mContext = context;
        this.mList = mList;
        mLayoutInflater = LayoutInflater.from(context);
        mAuthorName = authorName;
        mReplyCount = replyCount;
        mCover = (ImageView) ((Activity) mContext).getWindow().getDecorView().findViewById(R.id.thread_item_conver);
    }

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    @Override
    public int getItemViewType(int position) {
        return mList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PostViewHolder) {
            // Do nothing here
            final ThreadReply reply = mList.get(position);
            final PostViewHolder viewHolder = (PostViewHolder) holder;

            // 楼层
            final long currentLevel = Global.increaseOrder ? (position + 1) : (mReplyCount - position);
            viewHolder.number.setText("# " + currentLevel);

            // 头像
            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1,
                    CommonUtils.decode(reply.author));
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.avatar == null ? "" : reply.avatar));
            CommonUtils.setImageView(mContext, viewHolder.avatar,
                    avatarURL, R.drawable.default_avatar);

            // 作者
            if (CommonUtils.decode(reply.author).equals(mAuthorName)) {
                viewHolder.author.setTextColor(Color.RED);
                viewHolder.author.setText(CommonUtils.decode(reply.author) + "（楼主）");
            } else {
                viewHolder.author.setTextColor(Color.BLACK);
                viewHolder.author.setText(CommonUtils.decode(reply.author));
            }

            // 标题和正文
            if (CommonUtils.decode(reply.subject).equals("")) {
                viewHolder.subject.setVisibility(View.GONE);
            } else {
                viewHolder.subject.setVisibility(View.VISIBLE);
            }
            viewHolder.subject.setText(CommonUtils.decode(reply.subject));
            SpannableString spannableString = new SpannableString(
                    Html.fromHtml(
                            reply.message,
                            new PicassoImageGetter(mContext, viewHolder.message),
                            null));
            CustomSpan.replaceQuoteSpans(mContext, spannableString);
            CustomSpan.replaceClickableSpan(mContext, spannableString);
            viewHolder.message.setText(spannableString);

            // 日期
            viewHolder.date.setText(CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate(reply.dateline)));

            // 移动端
            if (reply.useMobile) viewHolder.useMobile.setVisibility(View.VISIBLE);
            else viewHolder.useMobile.setVisibility(View.INVISIBLE);

        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_thread_detail_new, parent, false);
            return new PostViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar, reply, useMobile;
        public TextView author, subject, date, number;
        public TextView message;

        public PostViewHolder(View itemView) {
            super(itemView);

            avatar = (ImageView) itemView.findViewById(R.id.thread_author_avatar);
            author = (TextView) itemView.findViewById(R.id.thread_author_name);
            subject = (TextView) itemView.findViewById(R.id.thread_subject);
            date = (TextView) itemView.findViewById(R.id.thread_date);
            number = (TextView) itemView.findViewById(R.id.thread_item_number);
            reply = (ImageView) itemView.findViewById(R.id.btn_repost);
            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonUtils.showDialog(mContext, "提醒", "开发者拼死拼活实现该功能中 T T");
                }
            });

            message = (TextView) itemView.findViewById(R.id.thread_message);
            message.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
            message.setLineSpacing(6, 1.2f);

            useMobile = (ImageView) itemView.findViewById(R.id.thread_from_mobile);
        }
    }
}
