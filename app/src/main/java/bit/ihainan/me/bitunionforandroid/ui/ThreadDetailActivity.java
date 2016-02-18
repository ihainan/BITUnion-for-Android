package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.ReplyListAdapter;
import bit.ihainan.me.bitunionforandroid.models.*;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.Api;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.HtmlUtil;

public class ThreadDetailActivity extends AppCompatActivity {
    private final static String TAG = ThreadDetailActivity.class.getSimpleName();

    // UI references
    private TextView mToolbarTitle, mBigTitle;
    private AppBarLayout mAppbar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageButton mChangeOrder;

    // Bundle tags
    public final static String THREAD_ID_TAG = "THREAD_ID_TAG";
    public final static String THREAD_NAME_TAG = "THREAD_NAME_TAG";
    public final static String THREAD_AUTHOR_NAME_TAG = "THREAD_AUTHOR_NAME_TAG";
    public final static String THREAD_REPLY_COUNT_TAG = "THREAD_REPLY_COUNT_TAG";


    // Data
    private Long mTid, mReplyCount;  // Thread ID
    private String mThreadName, mAuthorName;  // Thread name
    private long mCurrentPosition = 0;
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_detail);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get thread name and id
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mTid = bundle.getLong(THREAD_ID_TAG);
        mThreadName = bundle.getString(THREAD_NAME_TAG);
        mReplyCount = bundle.getLong(THREAD_REPLY_COUNT_TAG);
        mAuthorName = bundle.getString(THREAD_AUTHOR_NAME_TAG);
        if (mTid == null) {
            Global.readConfig(this);
            mTid = 10609296l;
        }

        // Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("");

        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mBigTitle = (TextView) findViewById(R.id.big_title);
        mToolbarTitle.setText(CommonUtils.truncateString(mThreadName, 8));
        mBigTitle.setText(mThreadName == null ? "" : mThreadName);
        mAppbar = (AppBarLayout) findViewById(R.id.app_bar);
        mAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset <= -toolbar.getHeight()) {
                    mToolbarTitle.setVisibility(View.VISIBLE);
                } else {
                    mToolbarTitle.setVisibility(View.INVISIBLE);
                }
            }
        });

        // RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.home_swipe_refresh_layout);

        // Order Button
        mChangeOrder = (ImageButton) findViewById(R.id.order_button);
        mChangeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "mChangeOrder >> 变换顺序 " + Global.increaseOrder + " -> " + !Global.increaseOrder);
                Global.increaseOrder = !Global.increaseOrder;
                Global.saveConfig(ThreadDetailActivity.this);

                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        reloadData(true);
                    }
                });
            }
        });


        setupRecyclerView();
        setupSwipeRefreshLayout();
    }

    private List<bit.ihainan.me.bitunionforandroid.models.ThreadReply> mThreadPostList = new ArrayList<>();
    private ReplyListAdapter mAdapter;

    private void setupRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(ThreadDetailActivity.this));

        mAdapter = new ReplyListAdapter(this, mThreadPostList, mAuthorName, mReplyCount);
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

                // 由于不能拿到论坛帖子的总数，因此只能无限加载
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                // Log.d(TAG, "onScrolled >> lastVisibleItem = " + lastVisibleItem + " mThreadPostList.size() = " + mThreadPostList.size());
                if (dy > 0 && lastVisibleItem >= mThreadPostList.size() - 2 && !mIsLoading) {
                    // 拉取数据，显示进度
                    Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
                    mThreadPostList.add(null);
                    mAdapter.notifyItemInserted(mThreadPostList.size() - 1);

                    if (!Global.increaseOrder) {
                        refreshData(mCurrentPosition < 0 ? 0 : mCurrentPosition, mCurrentPosition + Global.LOADING_REPLIES_COUNT);
                    } else {
                        refreshData(mCurrentPosition, mCurrentPosition + Global.LOADING_REPLIES_COUNT);
                    }

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
                reloadData(false);
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 第一次加载数据
                reloadData(false);
            }
        });
    }

    private void reloadData(boolean notifyChange) {
        mSwipeRefreshLayout.setRefreshing(true);
        mThreadPostList.clear();
        if (notifyChange) mAdapter.notifyDataSetChanged();
        if (Global.increaseOrder) {
            Log.d(TAG, "reloadData => 0 - " + Global.LOADING_REPLIES_COUNT);
            mCurrentPosition = 0;
            refreshData(0, Global.LOADING_REPLIES_COUNT);
        } else {
            mCurrentPosition = mReplyCount - Global.LOADING_REPLIES_COUNT + 1; // 起始的 mCurrentPosition
            Log.d(TAG, "reloadData => " + (mCurrentPosition < 0 ? 0 : mCurrentPosition) + " - " + (mCurrentPosition + Global.LOADING_REPLIES_COUNT));
            refreshData(mCurrentPosition < 0 ? 0 : mCurrentPosition, mCurrentPosition + Global.LOADING_REPLIES_COUNT);
        }
    }

    // TODO: 上滑，自动加载更多数据
    private void loadAfterData(long from, long to) {
        // 防止越界
        if (from < 0) from = 0;
        if (to > mReplyCount) to = mReplyCount;

        refreshData(from, to);
    }

    private void loadBeforeData() {
        // 下滑，自动加载更多数据
    }

    /**
     * 获取上一次浏览的位置
     *
     * @return 上一次浏览的位置
     */
    private static int getStartPosition() {
        // 第一次浏览的时候，StartPosition = 0，每浏览 Global.LOADING_REPLIES_COUNT 时候，记录一次位置，载入该位置

        return 0;
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, long to) {
        Log.d(TAG, "refreshData >> FROM " + from + " TO " + to);
        Api.getPostReplies(this, mTid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (Api.checkStatus(response)) {
                            try {
                                JSONArray newListJson = response.getJSONArray("postlist");
                                List<bit.ihainan.me.bitunionforandroid.models.ThreadReply> newThreads = Api.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<ThreadReply>>() {
                                        });

                                // 成功拿到数据，删除 Loading Progress Bar
                                if (mThreadPostList.size() > 0) {
                                    mThreadPostList.remove(mThreadPostList.size() - 1);
                                    mAdapter.notifyItemRemoved(mThreadPostList.size());
                                }

                                for (ThreadReply reply : newThreads) {
                                    String body = CommonUtils.decode(reply.message);
                                    reply.useMobile = body.contains("From BIT-Union Open API Project");
                                    HtmlUtil htmlUtil = new HtmlUtil(CommonUtils.decode(reply.message));
                                    reply.message = htmlUtil.makeAll();
                                }

                                // 更新 RecyclerView
                                if (!Global.increaseOrder) Collections.reverse(newThreads); // 倒序
                                mCurrentPosition += (Global.increaseOrder ? Global.LOADING_REPLIES_COUNT : -Global.LOADING_REPLIES_COUNT);
                                mThreadPostList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();

                                // 更新标志
                                // 正序的情况下直接变成 false，倒序的情况下需要在 from > 0 的时候才变，== 0 说明已经没有更多数据了
                                if (Global.increaseOrder || from > 0)
                                    mIsLoading = false;
                            } catch (Exception e) {
                                // 解析失败的话，说明到头了，移除标志，不允许再次更新（mIsLoading 始终为 true）
                                if (mThreadPostList.size() > 0) {
                                    Log.d(TAG, "refreshData >> 到头了 " + response);
                                    mThreadPostList.remove(mThreadPostList.size() - 1);
                                    mAdapter.notifyItemRemoved(mThreadPostList.size());
                                }

                                e.printStackTrace();
                                Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                            }
                        } else {
                            Log.i(TAG, "refreshData >> 服务器返回错误信息 " + response);
                            if (mThreadPostList.size() > 0) {
                                Log.d(TAG, "refreshData >> 到头了 " + response);
                                mThreadPostList.remove(mThreadPostList.size() - 1);
                                mAdapter.notifyItemRemoved(mThreadPostList.size());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 服务器请求失败，说明网络不好，移除标志，不允许再次发送请求（待定）
                        if (mThreadPostList.size() > 0) {
                            mThreadPostList.remove(mThreadPostList.size() - 1);
                            mAdapter.notifyItemRemoved(mThreadPostList.size());
                        }

                        mSwipeRefreshLayout.setRefreshing(false);
                        CommonUtils.showDialog(ThreadDetailActivity.this,
                                getString(R.string.error_title),
                                getString(R.string.error_network));
                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }
}
