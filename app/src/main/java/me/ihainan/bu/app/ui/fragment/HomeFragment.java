package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Response;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.LatestThreadListAdapter;
import me.ihainan.bu.app.models.LatestThread;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * Home Page Fragment
 */
public class HomeFragment extends BasicRecyclerViewFragment<LatestThread> {
    @Override
    protected String getNoNewDataMessage() {
        return getString(R.string.error_unknown_msg);
    }

    @Override
    protected String getFragmentTag() {
        return HomeFragment.class.getSimpleName();
    }

    @Override
    protected List<LatestThread> processList(List<LatestThread> list) {
        return list;
    }

    @Override
    protected List<LatestThread> parseResponse(JSONObject response) throws Exception {
        return BUApi.MAPPER.readValue(response.getJSONArray("newlist").toString(),
                new TypeReference<List<LatestThread>>() {
                });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return BUApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {

    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new LatestThreadListAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_HOME_PAGE_COUNT;
    }

    @Override
    protected void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void refreshData() {
        BUApi.getHomePage(mContext, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                try {
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (BUApi.checkStatus(response)) {
                        List<LatestThread> newItems = parseResponse(response);

                        // 重新加载
                        if (from == 0) {
                            mList.clear();
                            mAdapter.notifyDataSetChanged();
                        }

                        if (newItems == null || newItems.size() == 0) {
                            String message = getString(R.string.error_negative_credit);
                            String debugMessage = message + " - " + response;
                            Log.w(TAG, debugMessage);
                            CommonUtils.debugToast(mContext, debugMessage);

                            // Error
                            showErrorLayout(message);
                        } else {
                            mList.clear();
                            mList.addAll(newItems);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(mContext, debugMessage);
                        showErrorLayout(message);
                    }
                } catch (Exception e) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                    showErrorLayout(getString(R.string.error_parse_json));
                }
            }
        }, errorListener);
    }
}
