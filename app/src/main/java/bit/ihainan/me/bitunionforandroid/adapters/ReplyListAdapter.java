package bit.ihainan.me.bitunionforandroid.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import bit.ihainan.me.bitunionforandroid.ui.assist.LoadingViewHolder;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Post List Adapter
 */
public class ReplyListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = ReplyListAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final String mAuthorName;
    private final long mReplyCount;
    private final Context mContext;
    private List<ThreadReply> mList;
    private ImageView mCover;   // 标题图片

    public ReplyListAdapter(Context context, List<ThreadReply> mList, String authorName, long replyCount) {
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_thread_detail, parent, false);
            return new ThreadReplyViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ThreadReplyViewHolder) {
            final ThreadReply reply = mList.get(position);
            final ThreadReplyViewHolder viewHolder = (ThreadReplyViewHolder) holder;

            final long currentLevel = Global.increaseOrder ? (position + 1) : (mReplyCount - position);
            viewHolder.number.setText("# " + currentLevel);

            LatestThreadListAdapter.setUserClickListener(mContext, viewHolder.avatar, -1, CommonUtils.decode(reply.author));

            if (reply.useMobile) viewHolder.useMobile.setVisibility(View.VISIBLE);
            else viewHolder.useMobile.setVisibility(View.INVISIBLE);

            if (CommonUtils.decode(reply.subject).equals("")) {
                viewHolder.subject.setVisibility(View.GONE);
            } else {
                viewHolder.subject.setVisibility(View.VISIBLE);
            }

            if (CommonUtils.decode(reply.author).equals(mAuthorName)) {
                viewHolder.author.setTextColor(Color.RED);
                viewHolder.author.setText(CommonUtils.decode(reply.author) + "（楼主）");
            } else {
                viewHolder.author.setTextColor(Color.BLACK);
                viewHolder.author.setText(CommonUtils.decode(reply.author));
            }

            viewHolder.subject.setText(CommonUtils.decode(reply.subject));
            viewHolder.message.loadDataWithBaseURL("file:///android_asset/", reply.message.replaceAll("\\+", " "), "text/html", "utf-8", null);

            if (reply.attachment == null || reply.attachment.equals("")) {
                viewHolder.attachmentLayout.setVisibility(View.GONE);
            } else {
                viewHolder.attachmentLayout.setVisibility(View.VISIBLE);
                String fileName = CommonUtils.decode(reply.filename);
                fileName = fileName.length() >= 6 ? fileName.substring(0, 5) + ".." : fileName;
                viewHolder.attachmentName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = Global.getBaseURL() + CommonUtils.decode(reply.attachment);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        mContext.startActivity(i);
                    }
                });
                viewHolder.attachmentName.setText(fileName + "（" + Integer.valueOf(reply.filesize) / 1000.0 + " KB）");

                Log.d(TAG, "REPLY >> " + reply.toString());

                String fileType = CommonUtils.decode(reply.filetype);
                if (fileType.startsWith("image")) {
                    viewHolder.attachmentImageLayout.setVisibility(View.VISIBLE);

                    // 尝试从缓存中读取图片，如果缓存中没有图片，则用户点击之后就能加载
                    final String imageURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.attachment));
                    Picasso.with(mContext)
                            .load(imageURL)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(viewHolder.attachmentImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Picasso >> 缓存图片成功");
                                    viewHolder.clickToLoad.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "Picasso >> 缓存图片失败");
                                    viewHolder.clickToLoad.setText("点击加载图片");
                                    viewHolder.clickToLoad.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Log.d(TAG, "Picasso >> 点击文本，加载图片中");
                                            viewHolder.clickToLoad.setText("正在加载");
                                            Picasso.with(mContext).load(imageURL).into(viewHolder.attachmentImage, new Callback() {
                                                @Override
                                                public void onSuccess() {
                                                    Log.d(TAG, "Picasso >> 加载成功");
                                                    viewHolder.clickToLoad.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onError() {
                                                    Log.d(TAG, "Picasso >> 加载 " + imageURL + " 失败");
                                                    viewHolder.clickToLoad.setText("加载失败，点击重试");
                                                }
                                            });
                                        }
                                    });
                                }
                            });

//                    Picasso.with(mContext).load(imageURL)
//                            .into(((ThreadReplyViewHolder) holder).attachmentImage);
                } else {
                    viewHolder.attachmentImageLayout.setVisibility(View.GONE);
                }
            }

            viewHolder.date.setText(CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate(reply.dateline)));

            String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(reply.avatar == null ? "" : reply.avatar));
            Picasso.with(mContext).load(avatarURL)
                    .error(R.drawable.default_avatar)
                    .into(viewHolder.avatar);
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ThreadReplyViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar, attachmentImage, reply, useMobile;
        public TextView author, subject, date, number, attachmentName, clickToLoad;
        public LinearLayout attachmentLayout;
        public RelativeLayout attachmentImageLayout;
        public WebView message;

        public ThreadReplyViewHolder(View itemView) {
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

            message = (WebView) itemView.findViewById(R.id.thread_message);
            message.setScrollbarFadingEnabled(false);
            message.setBackgroundColor(Color.TRANSPARENT);
            message.setWebViewClient(new MyWebViewClient());

            attachmentLayout = (LinearLayout) itemView.findViewById(R.id.thread_attachment);
            attachmentImage = (ImageView) itemView.findViewById(R.id.thread_attachment_image);
            attachmentName = (TextView) itemView.findViewById(R.id.thread_attachment_name);
            clickToLoad = (TextView) itemView.findViewById(R.id.load_image_text);
            attachmentImageLayout = (RelativeLayout) itemView.findViewById(R.id.thread_attachment_image_layout);

            useMobile = (ImageView) itemView.findViewById(R.id.thread_from_mobile);
        }
    }

    class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(intent);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onLoadResource(WebView view, String url) {

            super.onLoadResource(view, url);

        }
    }
}
