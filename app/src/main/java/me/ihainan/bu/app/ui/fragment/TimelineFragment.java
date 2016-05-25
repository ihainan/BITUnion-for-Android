package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.TimelineAdapter;
import me.ihainan.bu.app.models.TimelineEvent;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 时间轴 Fragment
 */
public class TimelineFragment extends Fragment {
    private final static String TAG = TimelineFragment.class.getSimpleName();
    public final static String TIMELINE_ACTION_TAG = "TIMELINE_ACTION_TAG";
    private Context mContext;

    // UI references
    private View mRootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Data
    private boolean mIsLoading = false;
    private List<TimelineEvent> mList = new ArrayList<>();
    private TimelineAdapter mAdapter;
    private long mCurrentPosition = 0;
    private String mUsername;
    private String mAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            // Username
            mUsername = getArguments().getString(ProfileActivity.USER_NAME_TAG);
            if (mUsername == null) mUsername = BUApplication.userSession.username;
            mAction = getArguments().getString(TIMELINE_ACTION_TAG);
            if (mAction == null) mAction = "SPEC";

            // Error Layout
            mErrorLayout = (RelativeLayout) mRootView.findViewById(R.id.error_layout);
            mErrorLayout.setVisibility(View.GONE);
            mTvErrorMessage = (TextView) mRootView.findViewById(R.id.error_message);
            mTvAction = (TextView) mRootView.findViewById(R.id.action_text);
            mTvAction.setVisibility(View.GONE);

            // Setup RecyclerView
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
            setupRecyclerView();

            // Setup SwipeLayout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
            setupSwipeRefreshLayout();
        }

        return mRootView;
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = new TimelineAdapter(mContext, mList);
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
                if (dy > 0 && mLastVisibleItem >= mList.size() - BUApplication.LOADING_TIMELINE_COUNT / 2 && !mIsLoading) {
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

        refreshData(mCurrentPosition, mCurrentPosition + BUApplication.LOADING_TIMELINE_COUNT);
    }

    // 成功拉取数据事件监听器
    private Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (!isAdded() || ((Activity) mContext).isFinishing()) return;
            mSwipeRefreshLayout.setRefreshing(false);

            if (ExtraApi.checkStatus(response)) {
                // TODO: 服务器端错误信息处理
                try {
                    List<TimelineEvent> newEvents = BUApi.MAPPER.readValue(response.get("data").toString(),
                            new TypeReference<List<TimelineEvent>>() {
                            });

                    // 成功拿到数据，删除 Loading Progress Bar
                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    // 过滤
                    List<TimelineEvent> newEventsFiltered = new ArrayList<>();
                    for (TimelineEvent event : newEvents) {
                        if (event.type != 0) newEventsFiltered.add(event);
                    }
                    CommonUtils.debugToast(mContext, "Loaded " + newEventsFiltered.size() + " more item(s)");


                    // 更新 RecyclerView
                    if (newEventsFiltered.size() > 0) {
                        mCurrentPosition += BUApplication.LOADING_TIMELINE_COUNT;
                        mList.addAll(newEventsFiltered);
                        mAdapter.notifyDataSetChanged();
                    } else if (newEventsFiltered.size() == 0) {
                        showErrorLayout(getString(R.string.error_no_new_events), getString(R.string.action_refresh));
                    }

                    // 判断是否到头
                    if (newEvents.size() == BUApplication.LOADING_TIMELINE_COUNT) {
                        mIsLoading = false;
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = "TimelineFragment >> " + message + " - " + response;
                    Log.e(TAG, debugMessage);
                    CommonUtils.debugToast(mContext, debugMessage);

                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    showErrorLayout(getString(R.string.error_parse_json));
                }
            } else {
                String message = getString(R.string.error_unknown_json);
                String debugMessage = "TimelineFragment >> " + message + " - " + response;
                Log.i(TAG, debugMessage);
                CommonUtils.debugToast(mContext, debugMessage);

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
            if (((Activity) mContext).isFinishing()) return;

            // 服务器请求失败，说明网络不好，只能通过 RETRY 来重新拉取数据
            if (mList.size() > 0) {
                mList.remove(mList.size() - 1);
                mAdapter.notifyItemRemoved(mList.size());
            }

            mSwipeRefreshLayout.setRefreshing(false);

            String message = getString(R.string.error_network);
            String debugMessage = "TimelineFragment >> " + message;
            CommonUtils.debugToast(mContext, debugMessage);
            Log.e(TAG, debugMessage, error);

            showErrorLayout(message);
        }
    };

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        mErrorLayout.setVisibility(View.GONE);
        if (mAction.equals("SPEC"))
            ExtraApi.getSpecialUserTimeline(mContext, mUsername, from, to, listener, errorListener);
        else ExtraApi.getFocusTimeline(mContext, from, to, listener, errorListener);
    }

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
