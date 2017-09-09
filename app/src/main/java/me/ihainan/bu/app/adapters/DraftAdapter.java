package me.ihainan.bu.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Draft;
import me.ihainan.bu.app.ui.NewPostActivity;
import me.ihainan.bu.app.ui.viewholders.DraftViewHolder;
import me.ihainan.bu.app.ui.viewholders.LoadingViewHolder;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.DraftUtil;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 通知适配器
 */
public class DraftAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = DraftAdapter.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private final List<Draft> mList;

    public DraftAdapter(Context context, List<Draft> draftList) {
        mList = draftList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    private final int VIEW_TYPE_ITEM = 0;

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_LOADING = 1;
        return mList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ITEM) {
            view = mLayoutInflater.inflate(R.layout.item_drafts_item, parent, false);
            return new DraftViewHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.listview_progress_bar, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DraftViewHolder) {
            // Do nothing here
            final Draft draft = mList.get(position);
            final DraftViewHolder viewHolder = (DraftViewHolder) holder;

            if (draft.subject != null && !draft.subject.equals(""))
                viewHolder.title.setText(draft.subject);
            else viewHolder.title.setText("无主题");

            if (draft.attachmentURI != null && !"".equals(draft.attachmentURI))
                viewHolder.attachment.setVisibility(View.VISIBLE);
            else viewHolder.attachment.setVisibility(View.GONE);

            Date date = CommonUtils.unixTimeStampToDate(draft.timestamp);
            String dateStr = CommonUtils.getRelativeTimeSpanString(date);
            viewHolder.date.setText(dateStr);

            viewHolder.content.setText(draft.content);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 从 List 中删除
                    Intent intent = new Intent();
                    try {
                        DraftUtil.removeDraft(mContext, draft);
                        String draftStr = BUApi.MAPPER.writeValueAsString(draft);
                        intent.putExtra(NewPostActivity.DRAFT_TAG, draftStr);
                        ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    } catch (IOException e) {
                        String message = mContext.getString(R.string.error_fetch_draft_list);
                        Log.e(TAG, message, e);
                        ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    }

                    ((Activity) mContext).finish();
                }
            };

            viewHolder.rootLayout.setOnClickListener(onClickListener);
        } else {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}
