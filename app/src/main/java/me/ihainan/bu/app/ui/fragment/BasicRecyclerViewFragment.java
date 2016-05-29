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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * 列表 Fragment 抽象类.
 */
public abstract class BasicRecyclerViewFragment<T> extends Fragment {
    protected String TAG = getFragmentTag();
    protected Context mContext;

    // UI references
    protected View mRootView;
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected LinearLayoutManager mLayoutManager;
    protected RelativeLayout mErrorLayout;
    protected TextView mTvErrorMessage, mTvAction;

    // Data
    protected boolean mIsLoading = false;
    protected List<T> mList = new ArrayList<>();
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    protected long mCurrentPosition = 0;
    protected final int LOADING_COUNT = getLoadingCount();
    protected Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (!isAdded() || ((Activity) mContext).isFinishing()) return;
            mSwipeRefreshLayout.setRefreshing(false);

            if (checkStatus(response)) {
                try {
                    List<T> newItems = parseResponse(response);

                    // 成功拿到数据，删除 Loading Progress Bar
                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    // 预处理
                    newItems = processList(newItems);
                    CommonUtils.debugToast(mContext, "Loaded " + newItems.size() + " more item(s)");


                    // 更新 RecyclerView
                    if (newItems.size() > 0) {
                        mCurrentPosition += LOADING_COUNT;
                        mList.addAll(newItems);
                        mAdapter.notifyDataSetChanged();
                    } else if (mList.size() == 0 && newItems.size() == 0) {
                        showErrorLayout(getNoNewDataMessage(), getString(R.string.action_refresh));
                    }

                    // 判断是否到头
                    if (newItems.size() == LOADING_COUNT) {
                        mIsLoading = false;
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = TAG + " >> " + message + " - " + response;
                    Log.e(TAG, debugMessage);
                    CommonUtils.debugToast(mContext, debugMessage);

                    if (mList.size() > 0) {
                        mList.remove(mList.size() - 1);
                        mAdapter.notifyItemRemoved(mList.size());
                    }

                    showErrorLayout(getString(R.string.error_parse_json));
                }
            }
        }
    };


    protected Response.ErrorListener errorListener = new Response.ErrorListener() {
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
            String debugMessage = TAG + " >> " + message;
            CommonUtils.debugToast(mContext, debugMessage);
            Log.e(TAG, debugMessage, error);

            showErrorLayout(message);
        }
    };


    protected abstract String getNoNewDataMessage();

    /**
     * 获取类标签
     *
     * @return 类标签
     */
    protected abstract String getFragmentTag();

    /**
     * 预处理得到的列表对象
     *
     * @param list 列表对象
     * @return 处理之后的新列表对象
     */
    protected abstract List<T> processList(List<T> list);

    /**
     * 解析 JSON 数据，转换成列表对象
     *
     * @param response 服务器返回的 response
     * @return 对应的列表对象
     * @throws Exception 解析出现异常
     */
    protected abstract List<T> parseResponse(JSONObject response) throws Exception;

    /**
     * 检查 Response 是否表示正常状态
     *
     * @param response 返回的 Response
     * @return 正常返回 <code>true</code>，否则返回 <code>false</code>
     */
    protected abstract boolean checkStatus(JSONObject response);

    /**
     * 获取从外部传递过来的额外数据
     */
    protected abstract void getExtra();

    /**
     * 获取数据适配器
     *
     * @return 数据适配器
     */
    protected abstract RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter();

    /**
     * 获取每次 Loading 的条目个数
     *
     * @return 每次 Loading 的条目个数
     */
    protected abstract int getLoadingCount();

    /**
     * 从服务器获取数据
     *
     * @param from 数据在列表中的起点
     * @param to   数据在列表中的终点
     */
    protected abstract void refreshData(final long from, final long to);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            // Get extra data
            getExtra();

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

    protected void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = getAdapter();
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
                if (dy > 0 && mLastVisibleItem >= mList.size() - LOADING_COUNT / 2 && !mIsLoading) {
                    loadMore(true);
                }
            }
        });
    }

    protected void setupSwipeRefreshLayout() {
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
    protected void reloadData() {
        mIsLoading = true;
        mSwipeRefreshLayout.setRefreshing(true);
        mList.clear();
        mCurrentPosition = 0;
        loadMore(false);
    }

    protected void loadMore(boolean isAddProgressBar) {
        // 拉取数据，显示进度
        Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
        if (isAddProgressBar) {
            mList.add(null);
            mAdapter.notifyItemInserted(mList.size() - 1);
            mIsLoading = true;
        }

        mErrorLayout.setVisibility(View.GONE);
        refreshData(mCurrentPosition, mCurrentPosition + LOADING_COUNT);
    }

    protected void showErrorLayout(String message) {
        showErrorLayout(message, getString(R.string.action_retry));
    }

    protected void showErrorLayout(String message, String actionStr) {
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
