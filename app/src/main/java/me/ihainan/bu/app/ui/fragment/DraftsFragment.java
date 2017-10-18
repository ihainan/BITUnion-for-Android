package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.DraftAdapter;
import me.ihainan.bu.app.models.Draft;
import me.ihainan.bu.app.models.Notification;
import me.ihainan.bu.app.ui.NewPostActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.DraftUtil;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;
import me.ihainan.bu.app.utils.ui.CustomOnClickListener;

/**
 * 通知 Fragment
 */
public class DraftsFragment extends BasicRecyclerViewFragment<Draft> {
    // Data
    public static boolean isSetToolbar = false;

    public DraftsFragment() {
        super();
        isSetToolbar = false;
    }

    // TAGs
    private final static String TAG = DraftsFragment.class.getSimpleName();

    @Override
    protected String getNoNewDataMessage() {
        return mContext.getString(R.string.error_no_drafts);
    }

    @Override
    protected String getFragmentTag() {
        return TAG;
    }

    @Override
    protected List<Draft> processList(List<Draft> list) {
        return list;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mContext != null && mRecyclerView != null) {
            getActivity().findViewById(R.id.toolbar).setOnClickListener(CustomOnClickListener.doubleClickToListTop(mContext, mRecyclerView));
        }
    }


    @Override
    protected void setupRecyclerView() {
        super.setupRecyclerView();
        if (!isSetToolbar) {
            isSetToolbar = true;
            getActivity().findViewById(R.id.toolbar).setOnClickListener(CustomOnClickListener.doubleClickToListTop(mContext, mRecyclerView));
        }
    }

    @Override
    protected List<Draft> parseResponse(JSONObject response) throws Exception {
        JSONArray newListJson = response.getJSONArray("data");
        return BUApi.MAPPER.readValue(newListJson.toString(), new TypeReference<List<Notification>>() {
        });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return ExtraApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {

    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new DraftAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_NOTIFICATION_COUNT;
    }

    public void clearDrafts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.title_warning))
                .setMessage(mContext.getString(R.string.message_clear_drafts))
                .setPositiveButton(mContext.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DraftUtil.removeDraft(mContext);
                        } catch (IOException e) {
                            String message = getString(R.string.error_clear_drafts);
                            CommonUtils.toast(mContext, message);
                            Log.e(TAG, message, e);
                        }

                        refreshData();
                    }
                }).setNegativeButton(mContext.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (!((Activity) mContext).isFinishing()) {
            builder.show();
        }
    }

    @Override
    protected void refreshData() {
        // get data
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(false);

        try {
            String action = getArguments().getString(NewPostActivity.DRAFT_ACTION_TAG);
            if (NewPostActivity.ACTION_NEW_POST.equals(action)) {
                ((Activity) mContext).setTitle(getString(R.string.title_activity_draft_list_new_post));
            } else {
                ((Activity) mContext).setTitle(getString(R.string.title_activity_draft_list_new_thread));
            }
            mList.clear();
            mList.addAll(DraftUtil.getDraftList(mContext, getArguments().getString(NewPostActivity.DRAFT_ACTION_TAG)));
        } catch (IOException e) {
            String message = getString(R.string.error_fetch_draft_list);
            Log.e(TAG, message, e);
            CommonUtils.toast(mContext, message);
        }

        mAdapter.notifyDataSetChanged();
    }

}
