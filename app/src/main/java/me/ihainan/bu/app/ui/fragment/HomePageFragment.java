package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.LatestThreadListAdapter;
import me.ihainan.bu.app.models.LatestThread;
import me.ihainan.bu.app.ui.SearchResultActivity;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * Home Page Fragment
 */
public class HomePageFragment extends Fragment {
    // Tags
    private final static String TAG = HomePageFragment.class.getSimpleName();

    // UI references
    private Context mContext;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mRootView;
    private Toolbar mToolbar;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Data
    private List<LatestThread> mLatestThreads = new ArrayList<LatestThread>();
    private LatestThreadListAdapter mAdapter;

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

            // Error Layout
            mErrorLayout = (RelativeLayout) mRootView.findViewById(R.id.error_layout);
            mErrorLayout.setVisibility(View.GONE);
            mTvErrorMessage = (TextView) mRootView.findViewById(R.id.error_message);
            mTvAction = (TextView) mRootView.findViewById(R.id.action_text);
            mTvAction.setVisibility(View.GONE);

            // RecyclerView
            final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
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
                refreshData();
            }
        });
    }

    /**
     * 更新列表数据
     */
    private void refreshData() {
        // 隐藏错误页，显示刷新界面
        mErrorLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);

        // 从接口拉取数据
        BUApi.getHomePage(mContext, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (!isAdded() || ((Activity) mContext).isFinishing()) return;
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

                            // Error
                            showErrorLayout(message);

                            // Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).show();
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
                        showErrorLayout(message);
                    }
                } catch (Exception e) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                    showErrorLayout(getString(R.string.error_parse_json));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                mSwipeRefreshLayout.setRefreshing(false);

                String message = getString(R.string.error_network);
                String debugMessage = "getHomePage >> " + message;
                CommonUtils.debugToast(mContext, debugMessage);
                showErrorLayout(message);
                Log.e(TAG, debugMessage, error);
            }
        });
    }

    private void showErrorLayout(String message) {
        mLatestThreads.clear();
        mAdapter.notifyDataSetChanged();
        mErrorLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText(message);
        mTvAction.setVisibility(View.VISIBLE);
        mTvAction.setText(getString(R.string.action_retry));
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshData();
            }
        });
    }

    /* 菜单 */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(mContext, SearchResultActivity.class);
                mContext.startActivity(intent);
                break;
        }

        return true;
    }
}
