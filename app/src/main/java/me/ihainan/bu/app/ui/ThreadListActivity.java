package me.ihainan.bu.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.ThreadListAdapter;
import me.ihainan.bu.app.models.ForumListGroup;
import me.ihainan.bu.app.models.Thread;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.BUApplication;

public class ThreadListActivity extends SwipeActivity {
    private final static String TAG = ThreadListActivity.class.getSimpleName();
    public final static int REQUEST_NEW_THREAD = 0;

    // UI references
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Bundle tags
    public final static String ACTION_TAG = "ACTION_TAG";
    public final static String MAIN_FORUM_TAG = "MAIN_FORUM_TAG";
    public final static String SUB_FORUM_TAG = "SUB_FORUM_TAG";
    public final static String FORUM_NAME_TAG = "FORUM_NAME_TAG";
    public final static String FORUM_FID_TAG = "FORUM_FID_TAG";

    private String mAction, mForumName;
    private ForumListGroup.SubForum mSubForum;
    private ForumListGroup.ForumList mMainForum;
    private Long mFid;
    private int mCurrentPosition = 0;

    private void getExtra() {
        // Get mAction and forum id
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mAction = bundle.getString(ACTION_TAG);
        mMainForum = (ForumListGroup.ForumList) bundle.getSerializable(MAIN_FORUM_TAG);
        mSubForum = (ForumListGroup.SubForum) bundle.getSerializable(SUB_FORUM_TAG);
        mForumName = bundle.getString(FORUM_NAME_TAG);
        mFid = bundle.getLong(FORUM_FID_TAG);

        if (mFid != null && mMainForum == null && mSubForum == null) {
            for (ForumListGroup forumListGroup : BUApplication.forumListGroupList) {
                for (ForumListGroup.ForumList forumList : forumListGroup.getChildItemList()) {
                    if (forumList.getForumId().equals(mFid)) {
                        mMainForum = forumList;
                        break;
                    } else {
                        for (ForumListGroup.SubForum subForum : forumList.getChildItemList()) {
                            if (subForum.getSubForumId().equals(mFid)) {
                                mMainForum = forumList;
                                mSubForum = subForum;
                                break;
                            }
                        }
                    }
                    if (mMainForum != null) break;
                }
                if (mMainForum != null) break;
            }
        } else if (mMainForum == null && mForumName != null) {
            for (ForumListGroup forumListGroup : BUApplication.forumListGroupList) {
                for (ForumListGroup.ForumList forumList : forumListGroup.getChildItemList()) {
                    if (forumList.getForumName().equals(mForumName)) {
                        mMainForum = forumList;
                        break;
                    } else {
                        for (ForumListGroup.SubForum subForum : forumList.getChildItemList()) {
                            if (subForum.getSubForumName().equals(mForumName)) {
                                mMainForum = forumList;
                                mSubForum = subForum;
                                break;
                            }
                        }
                    }
                    if (mMainForum != null) break;
                }
                if (mMainForum != null) break;
            }
        }

        if (mSubForum != null) {
            setTitle(mMainForum.getForumName() + " - " + mSubForum.getSubForumName());
            mFid = mSubForum.getSubForumId();
        } else {
            setTitle(mMainForum.getForumName() + " - 主板块");
            mFid = mMainForum.getForumId();
        }

        // Update most visited forums list
        BUApplication.updateForumsMap(this, mMainForum.getForumId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_list);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, null);
                finish();
            }
        });

        // Error Layout
        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mErrorLayout.setVisibility(View.GONE);
        mTvErrorMessage = (TextView) findViewById(R.id.error_message);
        mTvAction = (TextView) findViewById(R.id.action_text);
        mTvAction.setVisibility(View.GONE);

        // Get Extra information from intent
        getExtra();

        // UI references
        mRecyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.home_swipe_refresh_layout);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutManager == null || layoutManager.findFirstVisibleItemPosition() >= 10) {
                    mRecyclerView.scrollToPosition(0);
                } else {
                    mRecyclerView.smoothScrollToPosition(0);
                }
            }
        });
        setupRecyclerView();
        setupSwipeRefreshLayout();

        // FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ThreadListActivity.this, BetterPostActivity.class);
                intent.putExtra(BetterPostActivity.ACTION_TAG, BetterPostActivity.ACTION_NEW_THREAD);
                intent.putExtra(BetterPostActivity.NEW_THREAD_FID_TAG, mFid);
                startActivityForResult(intent, PostListActivity.REQUEST_NEW_REPLY);
            }
        });

        setSwipeAnyWhere(false);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK, null);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_THREAD && resultCode == RESULT_OK) {
            CommonUtils.debugToast(this, "发布主题成功");
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    private boolean mIsLoading = false;

    LinearLayoutManager layoutManager;

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(ThreadListActivity.this));
        mAdapter = new ThreadListAdapter(this, mSubForum == null ? mMainForum.getForumId() : mSubForum.getSubForumId(), mThreadList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                if (dy > 0 && lastVisibleItem >= mThreadList.size() - 4 && !mIsLoading) {
                    // 拉取数据，显示进度
                    Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
                    mThreadList.add(null);
                    mAdapter.notifyItemInserted(mThreadList.size() - 1);
                    refreshData(mCurrentPosition, mCurrentPosition + BUApplication.LOADING_THREADS_COUNT);
                    mIsLoading = true;
                }
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(BUApplication.SWIPE_LAYOUT_TRIGGER_DISTANCE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载
                reloadData();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        });
    }

    private void reloadData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mCurrentPosition = 0;
        refreshData(0, BUApplication.LOADING_THREADS_COUNT);
    }

    private List<Thread> mThreadList = new ArrayList<>();
    private ThreadListAdapter mAdapter;

    /**
     * 更新列表数据
     */
    private void refreshData(final int from, int to) {
        mErrorLayout.setVisibility(View.GONE);
        BUApi.getForumThreads(this, mFid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (isFinishing()) return;
                        try {
                            mSwipeRefreshLayout.setRefreshing(false);

                            if (BUApi.checkStatus(response)) {
                                JSONArray newListJson = response.getJSONArray("threadlist");
                                List<me.ihainan.bu.app.models.Thread> newThreads = BUApi.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<Thread>>() {
                                        });

                                // 重新加载
                                if (from == 0) {
                                    mThreadList.clear();
                                    mAdapter.notifyDataSetChanged();
                                }

                                // 删除 Loading Progress Bar
                                if (mThreadList.size() > 0) {
                                    mThreadList.remove(mThreadList.size() - 1);
                                    mAdapter.notifyItemRemoved(mThreadList.size());
                                }

                                // 更新 RecyclerView
                                if (from == 0) mThreadList.clear();
                                mCurrentPosition += BUApplication.LOADING_THREADS_COUNT;
                                mThreadList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();

                                // 更新标志
                                mIsLoading = false;
                            } else if ("forum+need+password".equals(response.getString("msg"))) {
                                String message = getString(R.string.error_forum_need_password);
                                String debugMessage = message + " - " + response;
                                Log.w(TAG, debugMessage);
                                CommonUtils.debugToast(ThreadListActivity.this, debugMessage);
                                showErrorLayout(message);
                            } else {
                                String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                                String debugMessage = message + " - " + response;
                                Log.w(TAG, debugMessage);
                                CommonUtils.debugToast(ThreadListActivity.this, debugMessage);
                                showErrorLayout(message);
                            }
                        } catch (Exception e) {
                            // 解析失败的话，说明到头了，移除标志，不允许再次更新（mIsLoading 始终为 true）
                            if (mThreadList.size() > 0) {
                                Log.d(TAG, "refreshData >> 到头了 " + response);
                                mThreadList.remove(mThreadList.size() - 1);
                                mAdapter.notifyItemRemoved(mThreadList.size());
                            }

                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isFinishing()) return;
                        mSwipeRefreshLayout.setRefreshing(false);

                        // 服务器请求失败，说明网络不好，移除标志，运行再次发送请求
                        if (mThreadList.size() > 0) {
                            mThreadList.remove(mThreadList.size() - 1);
                            mAdapter.notifyItemRemoved(mThreadList.size());
                        }

                        mIsLoading = false;

                        if (mThreadList.size() > 0) {
                            mThreadList.remove(mThreadList.size() - 1);
                            mAdapter.notifyItemRemoved(mThreadList.size());
                        }

                        String message = getString(R.string.error_network);
                        String debugMessage = "getForumThreads >> " + message;
                        CommonUtils.debugToast(ThreadListActivity.this, debugMessage);
                        showErrorLayout(message);
                        Log.e(TAG, debugMessage, error);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 友盟 SDK
        if (BUApplication.uploadData)
            MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        if (BUApplication.uploadData)
            MobclickAgent.onPause(this);
    }

    private void showErrorLayout(String message) {
        mErrorLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText(message);
        mTvAction.setVisibility(View.VISIBLE);
        mTvAction.setText(getString(R.string.action_retry));
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.FID_TAG, mFid);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
