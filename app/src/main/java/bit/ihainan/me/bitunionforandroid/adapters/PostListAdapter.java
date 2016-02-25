package bit.ihainan.me.bitunionforandroid.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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
    private RecyclerView mRecyclerView;

    public PostListAdapter(Context context, List<ThreadReply> mList, String authorName, long replyCount) {
        this.mContext = context;
        this.mList = mList;
        mLayoutInflater = LayoutInflater.from(context);
        mAuthorName = authorName;
        mReplyCount = replyCount;
        mRecyclerView = (RecyclerView) ((Activity) mContext).getWindow().getDecorView().findViewById(R.id.detail_recycler_view);
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

            // 防止附件出现重复
            viewHolder.attachmentLayout.removeAllViews();

            // 楼层
            final long currentLevel = Global.increaseOrder ? (position + 1) : (mReplyCount - position + 2);
            viewHolder.number.setText("# " + currentLevel);

            // 头像
            CommonUtils.setUserAvatarClickListener(mContext,
                    viewHolder.avatar, -1,
                    CommonUtils.decode(reply.author));
            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.avatar == null ? "" : reply.avatar));
            CommonUtils.setAvatarImageView(mContext, viewHolder.avatar,
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

            // 附件
            if (!(reply.attachment == null || reply.attachment.equals(""))) {
                showAttachmentView(viewHolder.attachmentLayout, reply);
            }
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    private void showAttachmentView(LinearLayout linearLayout, final ThreadReply reply) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.item_thread_detail_attachment, null, false);

        // UI references
        TextView attachmentName = (TextView) itemView.findViewById(R.id.thread_attachment_name);
        RelativeLayout attachmentImageLayout = (RelativeLayout) itemView.findViewById(R.id.thread_attachment_image_layout);
        final ImageView attachmentImage = (ImageView) itemView.findViewById(R.id.thread_attachment_image);
        final TextView clickToLoad = (TextView) itemView.findViewById(R.id.load_image_text);

        // 附件名
        String fileName = CommonUtils.decode(reply.filename);
        fileName = fileName.length() >= 6 ? fileName.substring(0, 5) + ".." : fileName;

        attachmentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Global.getBaseURL() + CommonUtils.decode(reply.attachment);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mContext.startActivity(i);
            }
        });
        attachmentName.setText(fileName + "（" + Integer.valueOf(reply.filesize) / 1000.0 + " KB）");

        Log.d(TAG, "REPLY >> " + reply.toString());

        // 显示图片
        String fileType = CommonUtils.decode(reply.filetype);
        if (fileType.startsWith("image")) {
            attachmentImageLayout.setVisibility(View.VISIBLE);

            // 尝试从缓存中读取图片，如果缓存中没有图片，则用户点击之后就能加载
            final String imageURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.attachment));
            Picasso.with(mContext)
                    .load(imageURL)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(attachmentImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Picasso >> 缓存图片成功");
                            clickToLoad.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Log.d(TAG, "Picasso >> 缓存图片失败");
                            clickToLoad.setText("点击加载图片");
                            clickToLoad.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "Picasso >> 点击文本，加载图片中");
                                    clickToLoad.setText("正在加载");
                                    Picasso.with(mContext).load(imageURL).into(attachmentImage, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "Picasso >> 加载成功");
                                            clickToLoad.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError() {
                                            Log.d(TAG, "Picasso >> 加载 " + imageURL + " 失败");
                                            clickToLoad.setText("加载失败，点击重试");
                                        }
                                    });
                                }
                            });
                        }
                    });
        }

        linearLayout.addView(itemView);
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
        public LinearLayout attachmentLayout;

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
                    Snackbar.make(mRecyclerView, mContext.getString(R.string.error_not_implement), Snackbar.LENGTH_LONG).show();
                }
            });

            message = (TextView) itemView.findViewById(R.id.thread_message);
            message.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
            message.setLineSpacing(6, 1.2f);

            useMobile = (ImageView) itemView.findViewById(R.id.thread_from_mobile);

            attachmentLayout = (LinearLayout) itemView.findViewById(R.id.thread_attachment_layout);
        }
    }
}
