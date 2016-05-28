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
import me.ihainan.bu.app.adapters.SearchThreadOrPostResultAdapter;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 搜索结果 Fragment
 */
public class SearchResultFragment extends Fragment {
    private final static String TAG = SearchResultFragment.class.getSimpleName();
    public final static String SEARCH_ACTION_TAG = "SEARCH_ACTION_TAG";     // 搜索动作，可选 SEARCH_ACTION_THREAD / SEARCH_ACTION_POST / SEARCH_ACTION_USER
    public final static String SEARCH_KEYWORD_TAG = "SEARCH_KEYWORD_TAG";   // 搜索关键词

    public final static String SEARCH_ACTION_THREAD = "SEARCH_ACTION_THREAD";     // 搜索动作 - 搜索主题
    public final static String SEARCH_ACTION_POST = "SEARCH_ACTION_POST";     // 搜索动作 - 搜索回帖
    public final static String SEARCH_ACTION_USER = "SEARCH_ACTION_USER";     // 搜索动作 - 搜索用户

    private Context mContext;

    // UI references
    private View mRootView;
    private LinearLayoutManager mLayoutManager;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Data
    private boolean mIsLoading = false;
    private List<Post> mPostList = new ArrayList<>();
    private RecyclerView.Adapter<RecyclerView.ViewHolder> mAdapter;
    private long mCurrentPosition = 0;
    private String mKeyword;
    private String mAction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            // Action & Username
            mAction = getArguments().getString(SEARCH_ACTION_TAG);
            mKeyword = getArguments().getString(SEARCH_KEYWORD_TAG);
            if (mKeyword == null) mKeyword = CommonUtils.decode(BUApplication.username);
            if (mAction == null) mAction = SEARCH_ACTION_USER;

            // Error Layout
            mErrorLayout = (RelativeLayout) mRootView.findViewById(R.id.error_layout);
            mErrorLayout.setVisibility(View.GONE);
            mTvErrorMessage = (TextView) mRootView.findViewById(R.id.error_message);
            mTvAction = (TextView) mRootView.findViewById(R.id.action_text);
            mTvAction.setVisibility(View.GONE);

            // Setup RecyclerView
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
            setupRecyclerView();

            // Setup SwipeRefreshLayout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setEnabled(false);
        }

        return mRootView;
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = new SearchThreadOrPostResultAdapter(mContext, mPostList, mAction.equals(SEARCH_ACTION_THREAD));
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
                if (dy > 0 && mLastVisibleItem >= mPostList.size() - BUApplication.LOADING_SEARCH_RESULT_COUNT / 2 && !mIsLoading) {
                    loadMore(true);
                }
            }
        });
    }

    /**
     * 重新拉取数据
     */
    public void reloadData(String keyword) {
        mKeyword = keyword;
        mIsLoading = true;
        mPostList.clear();
        mAdapter.notifyDataSetChanged();
        mCurrentPosition = 0;
        loadMore(true);
    }

    private void loadMore(boolean isAddProgressBar) {
        // 拉取数据，显示进度
        Log.i(TAG, "onScrolled >> 即将到底，准备请求新数据");
        if (isAddProgressBar) {
            mPostList.add(null);
            mAdapter.notifyItemInserted(mPostList.size() - 1);
            mIsLoading = true;
        }

        refreshData(mCurrentPosition, mCurrentPosition + BUApplication.LOADING_SEARCH_RESULT_COUNT);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        mErrorLayout.setVisibility(View.GONE);
        if (mAction.equals(SEARCH_ACTION_THREAD))
            ExtraApi.searchThreads(mContext, mKeyword, from, to, listener, errorListener);
        else if (mAction.endsWith(SEARCH_ACTION_POST))
            ExtraApi.searchPosts(mContext, mKeyword, from, to, listener, errorListener);
        else ExtraApi.searchPosts(mContext, mKeyword, from, to, listener, errorListener);
    }

    // 成功拉取数据事件监听器
    private Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (!isAdded() || ((Activity) mContext).isFinishing()) return;

            if (ExtraApi.checkStatus(response)) {
                try {
                    List<Post> newPosts = BUApi.MAPPER.readValue(response.get("data").toString(),
                            new TypeReference<List<Post>>() {
                            });

                    // 成功拿到数据，删除 Loading Progress Bar
                    if (mPostList.size() > 0) {
                        mPostList.remove(mPostList.size() - 1);
                        mAdapter.notifyItemRemoved(mPostList.size());
                    }

                    if (mKeyword.equals("")) newPosts.clear();

                    // 更新 RecyclerView
                    if (newPosts.size() > 0) {
                        mCurrentPosition += BUApplication.LOADING_SEARCH_RESULT_COUNT;
                        mPostList.addAll(newPosts);
                        mAdapter.notifyDataSetChanged();
                    } else if (mPostList.size() == 0 && newPosts.size() == 0) {
                        showErrorLayout(getString(R.string.error_no_search_result), getString(R.string.action_refresh));
                    }

                    // 判断是否到头
                    if (newPosts.size() == BUApplication.LOADING_SEARCH_RESULT_COUNT) {
                        mIsLoading = false;
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = "SearchResultFragment >> " + message + " - " + response;
                    Log.e(TAG, debugMessage);
                    CommonUtils.debugToast(mContext, debugMessage);

                    if (mPostList.size() > 0) {
                        mPostList.remove(mPostList.size() - 1);
                        mAdapter.notifyItemRemoved(mPostList.size());
                    }

                    showErrorLayout(getString(R.string.error_parse_json));
                }
            } else {
                String message = getString(R.string.error_unknown_json);
                String debugMessage = "SearchResultFragment >> " + message + " - " + response;
                Log.i(TAG, debugMessage);
                CommonUtils.debugToast(mContext, debugMessage);

                if (mPostList.size() > 0) {
                    mPostList.remove(mPostList.size() - 1);
                    mAdapter.notifyItemRemoved(mPostList.size());
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
            if (mPostList.size() > 0) {
                mPostList.remove(mPostList.size() - 1);
                mAdapter.notifyItemRemoved(mPostList.size());
            }

            String message = getString(R.string.error_network);
            String debugMessage = "SearchResultFragment >> " + message;
            CommonUtils.debugToast(mContext, debugMessage);
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
        mPostList.clear();
        mAdapter.notifyDataSetChanged();
        mErrorLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText(message);
        mTvAction.setVisibility(View.VISIBLE);
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData(mKeyword);
            }
        });
    }
}
