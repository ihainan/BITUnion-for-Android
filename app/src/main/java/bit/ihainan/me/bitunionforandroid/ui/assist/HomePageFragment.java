package bit.ihainan.me.bitunionforandroid.ui.assist;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.LatestThreadListAdapter;
import bit.ihainan.me.bitunionforandroid.models.LatestThread;
import bit.ihainan.me.bitunionforandroid.utils.Api;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Home Page Fragment
 */
public class HomePageFragment extends Fragment {
    private final static String TAG = HomePageFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_home_page, container, false);

            // UI references
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.home_recycler_view);

            final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

            mAdapter = new LatestThreadListAdapter(mContext, mLatestThreads);
            mRecyclerView.setAdapter(mAdapter);

            // Click

            // Swipe Refresh Layout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.home_swipe_refresh_layout);
            setupSwipeRefreshLayout();
        }

        return mRootView;
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                refreshData();
            }
        });
    }

    private List<LatestThread> mLatestThreads = new ArrayList<LatestThread>();
    private LatestThreadListAdapter mAdapter;

    /**
     * 更新列表数据
     */
    private void refreshData() {
        Api.getHomePage(mContext, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (Api.checkStatus(response)) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    try {
                        JSONArray newListJson = response.getJSONArray("newlist");
                        List<LatestThread> newThreads = Api.MAPPER.readValue(newListJson.toString(),
                                new TypeReference<List<LatestThread>>() {
                                });
                        mLatestThreads.clear();
                        mLatestThreads.addAll(newThreads);
                        mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        CommonUtils.showDialog(mContext,
                                getString(R.string.error_title),
                                getString(R.string.error_parse_json));
                        Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                CommonUtils.showDialog(mContext,
                        getString(R.string.error_title),
                        getString(R.string.error_network));
                Log.e(TAG, getString(R.string.error_network), error);
            }
        });
    }
}
