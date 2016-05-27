package me.ihainan.bu.app.ui;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.dift.ui.SwipeToAction;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.NewFollowingListAdapter;
import me.ihainan.bu.app.models.Follow;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

public class FollowingListActivity extends SwipeActivity {
    private final static String TAG = FollowingListActivity.class.getSimpleName();

    // UI references
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Data
    private boolean mIsLoading = false;
    private List<Follow> mList = new ArrayList<>();
    private NewFollowingListAdapter mAdapter;
    private long mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_list);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Error Layout
        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mErrorLayout.setVisibility(View.GONE);
        mTvErrorMessage = (TextView) findViewById(R.id.error_message);
        mTvAction = (TextView) findViewById(R.id.action_text);
        mTvAction.setVisibility(View.GONE);

        // Setup RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setupRecyclerView();

        // Setup SwipeLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        setupSwipeRefreshLayout();

        // Swipe
        setSwipeAnyWhere(false);
    }


    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // Adapter
        mAdapter = new NewFollowingListAdapter(this, mList);
        mRecyclerView.setAdapter(mAdapter);

        // 自动加载
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
                if (dy > 0 && mLastVisibleItem >= mList.size() - BUApplication.LOADING_FOLLOWING_COUNT / 2 && !mIsLoading) {
                    loadMore(true);
                }
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(BUApplication.SWIPE_LAYOUT_TRIGGER_DISTANCE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载数据
                reloadData();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 第一次加载数据
                reloadData();
            }
        });
    }

    /**
     * 重新拉取数据
     */
    private void reloadData() {
        mIsLoading = true;
        mSwipeRefreshLayout.setRefreshing(true);
        mList.clear();
        mCurrentPosition = 0;
        loadMore(false);
    }

    private void loadMore(boolean isAddProgressBar) {
        // 拉取数据，显示进度
        Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
        if (isAddProgressBar) {
            mList.add(null);
            mAdapter.notifyItemInserted(mList.size() - 1);
            mIsLoading = true;
        }

        refreshData(mCurrentPosition, mCurrentPosition + BUApplication.LOADING_FOLLOWING_COUNT);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        mErrorLayout.setVisibility(View.GONE);
        ExtraApi.getFollowingList(this, from, to, listener, errorListener);
    }

    // 成功拉取数据事件监听器
    private Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (isFinishing()) return;
            mSwipeRefreshLayout.setRefreshing(false);

            if (ExtraApi.checkStatus(response)) {
                try {
                    List<Follow> newEvents = BUApi.MAPPER.readValue(response.get("data").toString(),
                            new TypeReference<List<Follow>>() {
                            });

                    // 成功拿到数据，删除 Loading Progress Bar
                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    // 更新 RecyclerView
                    if (newEvents.size() > 0) {
                        mCurrentPosition += BUApplication.LOADING_FOLLOWING_COUNT;
                        mList.addAll(newEvents);
                        mAdapter.notifyDataSetChanged();
                    } else if (mList.size() == 0 && newEvents.size() == 0) {
                        showErrorLayout(getString(R.string.error_no_following), getString(R.string.action_refresh));
                    }

                    // 判断是否到头
                    if (newEvents.size() == BUApplication.LOADING_FOLLOWING_COUNT) {
                        mIsLoading = false;
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = "FollowingListActivity >> " + message + " - " + response;
                    Log.e(TAG, debugMessage);
                    CommonUtils.debugToast(FollowingListActivity.this, debugMessage);

                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    showErrorLayout(getString(R.string.error_parse_json));
                }
            } else {
                String message = getString(R.string.error_unknown_json);
                String debugMessage = "FollowingListActivity >> " + message + " - " + response;
                Log.i(TAG, debugMessage);
                CommonUtils.debugToast(FollowingListActivity.this, debugMessage);

                if (mList.size() > 0) {
                    mList.remove(mList.size() - 1);
                    mAdapter.notifyItemRemoved(mList.size());
                }

                showErrorLayout(message);
            }
        }
    };

    // 拉取数据失败事件监听器
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isFinishing()) return;

            // 服务器请求失败，说明网络不好，只能通过 RETRY 来重新拉取数据
            if (mList.size() > 0) {
                mList.remove(mList.size() - 1);
                mAdapter.notifyItemRemoved(mList.size());
            }

            mSwipeRefreshLayout.setRefreshing(false);

            String message = getString(R.string.error_network);
            String debugMessage = "FollowingListActivity >> " + message;
            CommonUtils.debugToast(FollowingListActivity.this, debugMessage);
            Log.e(TAG, debugMessage, error);

            showErrorLayout(message);
        }
    };

    private void showErrorLayout(String message) {
        showErrorLayout(message, getString(R.string.action_retry));
    }

    private void showErrorLayout(String message, String actionStr) {
        if (actionStr == null) mTvAction.setVisibility(View.GONE);
        else {
            mTvAction.setText(actionStr);
            mTvAction.setVisibility(View.VISIBLE);
        }
        mList.clear();
        mAdapter.notifyDataSetChanged();
        mErrorLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText(message);
        mTvAction.setVisibility(View.VISIBLE);
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });
    }
}
