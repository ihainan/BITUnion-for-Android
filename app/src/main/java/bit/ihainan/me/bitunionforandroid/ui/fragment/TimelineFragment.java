package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.TimelineAdapter;
import bit.ihainan.me.bitunionforandroid.models.TimelineEvent;
import bit.ihainan.me.bitunionforandroid.ui.ProfileActivity;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.network.ExtraApi;

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
            if (mUsername == null) mUsername = Global.userSession.username;
            mAction = getArguments().getString(TIMELINE_ACTION_TAG);
            if (mAction == null) mAction = "SPEC";

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
                if (dy > 0 && mLastVisibleItem >= mList.size() - Global.LOADING_TIMELINE_COUNT / 2 && !mIsLoading) {
                    loadMore(true);
                }
            }
        });
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);

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

        refreshData(mCurrentPosition, mCurrentPosition + Global.LOADING_TIMELINE_COUNT);
    }

    // 成功拉取数据事件监听器
    private Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (!isAdded() || ((Activity) mContext).isFinishing()) return;
            mSwipeRefreshLayout.setRefreshing(false);

            if (ExtraApi.checkStatus(response)) {
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
                        mCurrentPosition += Global.LOADING_TIMELINE_COUNT;
                        mList.addAll(newEventsFiltered);
                        mAdapter.notifyDataSetChanged();
                    }

                    // 判断是否到头
                    if (newEvents.size() == Global.LOADING_TIMELINE_COUNT) {
                        mIsLoading = false;
                    }
                } catch (Exception e) {
                    Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);

                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    Snackbar.make(mRecyclerView, getString(R.string.error_parse_json),
                            Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadMore(true);
                        }
                    }).show();
                }
            } else {
                Log.i(TAG, "refreshData >> " + getString(R.string.error_unknown_json) + "" + response);

                if (mList.size() > 0) {
                    mList.remove(mList.size() - 1);
                    mAdapter.notifyItemRemoved(mList.size());
                }

                Snackbar.make(mRecyclerView, getString(R.string.error_unknown_json), Snackbar.LENGTH_LONG).show();
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

            Snackbar.make(mRecyclerView, message, Snackbar.LENGTH_LONG).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMore(true);
                }
            }).show();
        }
    };


    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        if (mAction.equals("SPEC"))
            ExtraApi.getSpecialUserTimeline(mContext, mUsername, from, to, listener, errorListener);
        else ExtraApi.getFocusTimeline(mContext, from, to, listener, errorListener);
    }
}
