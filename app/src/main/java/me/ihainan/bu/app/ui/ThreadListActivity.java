package me.ihainan.bu.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.CustomOnClickListener;
import me.ihainan.bu.app.utils.ui.IconFontHelper;

public class ThreadListActivity extends SwipeActivity {
    private final static String TAG = ThreadListActivity.class.getSimpleName();
    private final static int REQUEST_NEW_THREAD = 0;

    // UI references
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Bundle tags
    public final static String ACTION_TAG = "ACTION_TAG";
    public final static String MAIN_FORUM_TAG = "MAIN_FORUM_TAG";
    public final static String SUB_FORUM_TAG = "SUB_FORUM_TAG";
    private final static String FORUM_NAME_TAG = "FORUM_NAME_TAG";
    public final static String FORUM_FID_TAG = "FORUM_FID_TAG";

    private ForumListGroup.SubForum mSubForum;
    private ForumListGroup.ForumList mMainForum;
    private Long mFid;
    private int mCurrentPosition = 0;
    private boolean firstFavoriteStatus = false;

    private void getExtra() {
        // Get mAction and forum id
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mMainForum = (ForumListGroup.ForumList) bundle.getSerializable(MAIN_FORUM_TAG);
        mSubForum = (ForumListGroup.SubForum) bundle.getSerializable(SUB_FORUM_TAG);
        String mForumName = bundle.getString(FORUM_NAME_TAG);
        mFid = bundle.getLong(FORUM_FID_TAG, -1);

        if (mMainForum == null && mSubForum == null) {
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
        } else if (mMainForum == null && mFid != -1) {
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

        if (mMainForum != null) {
            if (mSubForum != null) {
                setTitle(mMainForum.getForumName() + " - " + mSubForum.getSubForumName());
                mFid = mSubForum.getSubForumId();
            } else {
                setTitle(mMainForum.getForumName() + " - 主板块");
                mFid = mMainForum.getForumId();
            }
        }

    }

    @Override
    public void finish() {
        if (mMainForum == null) {
            setResult(Activity.RESULT_CANCELED, null);
        } else {
            boolean status = BUApplication.getForumFavorStatus(ThreadListActivity.this,
                    mMainForum.getForumId()) == firstFavoriteStatus;
            setResult(status ? Activity.RESULT_CANCELED : Activity.RESULT_OK, null);
        }

        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_list);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Error Layout
        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        if (mErrorLayout != null) {
            mErrorLayout.setVisibility(View.GONE);
        }
        mTvErrorMessage = (TextView) findViewById(R.id.error_message);
        mTvAction = (TextView) findViewById(R.id.action_text);
        if (mTvAction != null) {
            mTvAction.setVisibility(View.GONE);
        }

        // Get Extra information from intent
        getExtra();

        // UI references
        mRecyclerView = (RecyclerView) findViewById(R.id.detail_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.home_swipe_refresh_layout);
        if (toolbar != null) {
            toolbar.setOnClickListener(CustomOnClickListener.doubleClickToListTop(this, mRecyclerView));
        }
        setupRecyclerView();
        setupSwipeRefreshLayout();

        // FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ThreadListActivity.this, NewPostActivity.class);
                    intent.putExtra(NewPostActivity.ACTION_TAG, NewPostActivity.ACTION_NEW_THREAD);
                    intent.putExtra(NewPostActivity.NEW_THREAD_FID_TAG, mFid);
                    startActivityForResult(intent, PostListActivity.REQUEST_NEW_REPLY);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_THREAD && resultCode == RESULT_OK) {
            CommonUtils.debugToast(this, "发布主题成功");
            Intent intent = getIntent();
            super.finish();
            startActivity(intent);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    private boolean mIsLoading = false;

    private LinearLayoutManager layoutManager;

    private void setupRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(ThreadListActivity.this));
        mAdapter = new ThreadListAdapter(this, mSubForum == null ? mMainForum.getForumId() : mSubForum.getSubForumId(), mThreadList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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

    private final List<Thread> mThreadList = new ArrayList<>();
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

    private MenuItem mFavorItem;    // 收藏按钮

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.thread_list_menu, menu);

        // Search
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        IconFontHelper.setupMenuIcon(this, itemSearch, getString(R.string.search_icon), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThreadListActivity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.FID_TAG, mFid);
                startActivity(intent);
            }
        });

        mFavorItem = menu.findItem(R.id.action_forum_favor);
        firstFavoriteStatus = BUApplication.getForumFavorStatus(this,
                mMainForum.getForumId());
        mFavorItem.setIcon(firstFavoriteStatus ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);

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
            case R.id.action_forum_favor:
                if (mMainForum != null && mFavorItem != null) {
                    BUApplication.addOrRemoveForumFavor(this, mMainForum.getForumId(),
                            !BUApplication.getForumFavorStatus(this, mMainForum.getForumId()));
                    mFavorItem.setIcon(BUApplication.getForumFavorStatus(this,
                            mMainForum.getForumId()) ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp);
                }
        }
        return super.onOptionsItemSelected(item);
    }
}
