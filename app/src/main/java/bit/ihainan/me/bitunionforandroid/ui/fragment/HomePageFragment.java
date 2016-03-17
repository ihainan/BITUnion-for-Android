package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.LatestThreadListAdapter;
import bit.ihainan.me.bitunionforandroid.models.LatestThread;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;

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
    private Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_home_page, container, false);

            // UI references
            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);

            final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.detail_recycler_view);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

            mAdapter = new LatestThreadListAdapter(mContext, mLatestThreads);
            mRecyclerView.setAdapter(mAdapter);

            // Swipe Refresh Layout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.home_swipe_refresh_layout);
            setupSwipeRefreshLayout();

            setHasOptionsMenu(true);
        }

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar.setTitle(R.string.action_home);
    }

    @Override
    public void onResume() {
        super.onResume();

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });
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
        BUApi.getHomePage(mContext, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (BUApi.checkStatus(response)) {
                        JSONArray newListJson = response.getJSONArray("newlist");
                        List<LatestThread> newThreads = BUApi.MAPPER.readValue(newListJson.toString(),
                                new TypeReference<List<LatestThread>>() {
                                });

                        if (newThreads == null || newThreads.size() == 0) {
                            String message = getString(R.string.error_negative_credit);
                            String debugMessage = message + " - " + response;
                            Log.w(TAG, debugMessage);
                            CommonUtils.debugToast(mContext, debugMessage);
                            Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
                        } else {
                            mLatestThreads.clear();
                            mLatestThreads.addAll(newThreads);
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(mContext, debugMessage);
                        Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showSnackbar(getString(R.string.error_parse_json));
                    Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefreshLayout.setRefreshing(false);
                showSnackbar(getString(R.string.error_network));
                Log.e(TAG, getString(R.string.error_network), error);
            }
        });
    }

    private void showSnackbar(String showMessage) {
        Snackbar.make(mRecyclerView, showMessage, Snackbar.LENGTH_LONG)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSwipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(true);
                                refreshData();
                            }
                        });
                    }
                }).show();
    }
}
