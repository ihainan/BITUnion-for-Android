package me.ihainan.bu.app.ui.fragment;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.TimelineAdapter;
import me.ihainan.bu.app.models.TimelineEvent;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;
import me.ihainan.bu.app.utils.ui.CustomOnClickListener;

/**
 * 时间轴 Fragment
 */

public class TimelineFragment extends BasicRecyclerViewFragment<TimelineEvent> {
    // Tags
    public final static String TIMELINE_ACTION_TAG = "TIMELINE_ACTION_TAG";

    // Data
    private String mUsername;
    private String mAction;
    public static boolean isSetToolbar = false;

    @Override
    protected String getNoNewDataMessage() {
        return mContext.getString(R.string.error_no_new_events);
    }

    @Override
    public String getFragmentTag() {
        return TimelineFragment.class.getSimpleName();
    }

    @Override
    protected List<TimelineEvent> processList(List<TimelineEvent> list) {
        return list;
    }

    @Override
    protected List<TimelineEvent> parseResponse(JSONObject response) throws Exception {
        return BUApi.MAPPER.readValue(response.get("data").toString(),
                new TypeReference<List<TimelineEvent>>() {
                });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return ExtraApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {
        mUsername = getArguments().getString(ProfileActivity.USER_NAME_TAG);
        if (mUsername == null) mUsername = BUApplication.userSession.username;
        mAction = getArguments().getString(TIMELINE_ACTION_TAG);
        if (mAction == null) mAction = "SPEC";
    }

    @Override
    protected void setupRecyclerView() {
        super.setupRecyclerView();
        if (!isSetToolbar) {
            isSetToolbar = !isSetToolbar;
            getActivity().findViewById(R.id.toolbar).setOnClickListener(CustomOnClickListener.doubleClickToListTop(mContext, mRecyclerView));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mContext != null && mRecyclerView != null) {
            getActivity().findViewById(R.id.toolbar).setOnClickListener(CustomOnClickListener.doubleClickToListTop(mContext, mRecyclerView));
        }
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new TimelineAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_TIMELINE_COUNT;
    }

    @Override
    protected void refreshData() {
        if (mAction.equals("SPEC"))
            ExtraApi.getSpecialUserTimeline(mContext, mUsername, from, to, listener, errorListener);
        else ExtraApi.getFocusTimeline(mContext, from, to, listener, errorListener);
    }
}
