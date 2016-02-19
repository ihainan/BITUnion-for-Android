package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.ThreadListAdapter;
import bit.ihainan.me.bitunionforandroid.models.ForumListGroup;
import bit.ihainan.me.bitunionforandroid.models.Thread;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.Api;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

public class ThreadListActivity extends AppCompatActivity {
    private final static String TAG = ThreadListActivity.class.getSimpleName();

    // UI references
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Bundle tags
    public final static String ACTION_TAG = "ACTION_TAG";
    public final static String MAIN_FORUM_TAG = "MAIN_FORUM_TAG";
    public final static String SUB_FORUM_TAG = "SUB_FORUM_TAG";
    public final static String FORUM_NAME_TAG = "FORUM_NAME_TAG";

    private String mAction, mForumName;
    private ForumListGroup.SubForum mSubForum;
    private ForumListGroup.ForumList mMainForum;
    private Long mFid, mRepliesCount;
    private int mCurrentPosition = 0;

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
                finish();
            }
        });

        // Get mAction and forum id
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mAction = bundle.getString(ACTION_TAG);
        mMainForum = (ForumListGroup.ForumList) bundle.getSerializable(MAIN_FORUM_TAG);
        mSubForum = (ForumListGroup.SubForum) bundle.getSerializable(SUB_FORUM_TAG);
        mForumName = bundle.getString(FORUM_NAME_TAG);

        if (mMainForum == null && mForumName != null) {
            for (ForumListGroup forumListGroup : Global.forumListGroupList) {
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

        // UI references
        mRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.home_swipe_refresh_layout);

        // Initialize views
        setupRecyclerView();
        setupSwipeRefreshLayout();

        // FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "开发者拼死拼活实现该功能中 T T", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
    }

    private boolean mIsLoading = false;

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(ThreadListActivity.this));

        mAdapter = new ThreadListAdapter(this, mThreadList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 由于不能拿到论坛帖子的总数，因此只能无限加载
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                // Log.d(TAG, "onScrolled >> lastVisibleItem = " + lastVisibleItem + " mThreadList.size() = " + mThreadList.size());
                // Log.d(TAG, "onScrolled >> " + (dy > 0) + " " + (lastVisibleItem >= mThreadList.size() - 4) + " " + !mIsLoading);
                if (dy > 0 && lastVisibleItem >= mThreadList.size() - 4 && !mIsLoading) {
                    // 拉取数据，显示进度
                    Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
                    mThreadList.add(null);
                    mAdapter.notifyItemInserted(mThreadList.size() - 1);
                    refreshData(mCurrentPosition, mCurrentPosition + Global.LOADING_COUNT);
                    mIsLoading = true;
                }
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载
                mThreadList.clear();
                mCurrentPosition = 0;
                refreshData(0, Global.LOADING_COUNT);
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                refreshData(mCurrentPosition, Global.LOADING_COUNT);
            }
        });
    }

    private List<Thread> mThreadList = new ArrayList<Thread>();
    private ThreadListAdapter mAdapter;

    /**
     * 更新列表数据
     */
    private void refreshData(final int from, int to) {
        Api.getForumThreads(this, mFid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (Api.checkStatus(response)) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            try {
                                JSONArray newListJson = response.getJSONArray("threadlist");
                                List<bit.ihainan.me.bitunionforandroid.models.Thread> newThreads = Api.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<Thread>>() {
                                        });

                                // 删除 Loading Progress Bar
                                if (mThreadList.size() > 0) {
                                    mThreadList.remove(mThreadList.size() - 1);
                                    mAdapter.notifyItemRemoved(mThreadList.size());
                                }

                                // 更新 RecyclerView
                                if (from == 0) mThreadList.clear();
                                mCurrentPosition += Global.LOADING_COUNT;
                                mThreadList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();

                                // 更新标志
                                mIsLoading = false;
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
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                        mSwipeRefreshLayout.setRefreshing(false);
                        CommonUtils.showDialog(ThreadListActivity.this,
                                getString(R.string.error_title),
                                getString(R.string.error_network));
                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 友盟 SDK
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        MobclickAgent.onPause(this);
    }
}
