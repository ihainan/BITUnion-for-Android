package me.ihainan.bu.app.ui.fragment;

import android.support.v7.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.NewFollowingListAdapter;
import me.ihainan.bu.app.models.Follow;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 关注列表 Fragment
 */
public class FollowingListFragment extends BasicRecyclerViewFragment<Follow> {
    @Override
    protected String getNoNewDataMessage() {
        return getString(R.string.error_no_following);
    }

    @Override
    protected String getFragmentTag() {
        return FollowingListFragment.class.getSimpleName();
    }

    @Override
    protected List<Follow> processList(List<Follow> list) {
        return list;
    }

    @Override
    protected List<Follow> parseResponse(JSONObject response) throws Exception {
        return BUApi.MAPPER.readValue(response.get("data").toString(),
                new TypeReference<List<Follow>>() {
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
        return new NewFollowingListAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_FOLLOWING_COUNT;
    }

    @Override
    protected void refreshData() {
        ExtraApi.getFollowingList(mContext, from, to, listener, errorListener);
    }
}
