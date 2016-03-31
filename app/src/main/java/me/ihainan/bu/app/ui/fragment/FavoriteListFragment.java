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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.FavoriteListAdapter;
import me.ihainan.bu.app.models.Favorite;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

public class FavoriteListFragment extends Fragment {
    private final static String TAG = FavoriteListFragment.class.getSimpleName();
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
    private List<Favorite> mList = new ArrayList<>();
    private FavoriteListAdapter mAdapter;
    private long mCurrentPosition = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

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
        mAdapter = new FavoriteListAdapter(mContext, mList);
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
                if (dy > 0 && mLastVisibleItem >= mList.size() - Global.LOADING_FAVORITES_COUNT / 2 && !mIsLoading) {
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

        refreshData(mCurrentPosition, mCurrentPosition + Global.LOADING_POSTS_COUNT);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        mErrorLayout.setVisibility(View.GONE);
        ExtraApi.getFavoriteList(mContext, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (ExtraApi.checkStatus(response)) {
                            try {
                                JSONArray newListJson = response.getJSONArray("data");
                                List<Favorite> newFavorites = BUApi.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<Favorite>>() {
                                        });

                                // 成功拿到数据，删除 Loading Progress Bar
                                if (mList.size() > 0) {
                                    mList.remove(mList.size() - 1);
                                    mAdapter.notifyItemRemoved(mList.size());
                                }

                                CommonUtils.debugToast(mContext, "Loaded " + newFavorites.size() + " more item(s)");

                                // 更新 RecyclerView
                                if (newFavorites.size() > 0) {
                                    mCurrentPosition += Global.LOADING_POSTS_COUNT;
                                    mList.addAll(newFavorites);
                                    mAdapter.notifyDataSetChanged();
                                    Global.hasUpdateFavor = false;
                                } else if (newFavorites.size() == 0) {
                                    showErrorLayout(getString(R.string.error_no_favorites), getString(R.string.action_refresh));
                                }

                                // 判断是否到头
                                if (newFavorites.size() == Global.LOADING_FAVORITES_COUNT) {
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

                                showErrorLayout(message);
                            }
                        } else {
                            Log.i(TAG, "refreshData >> " + getString(R.string.error_unknown_json) + "" + response);

                            if (mList.size() > 0) {
                                mList.remove(mList.size() - 1);
                                mAdapter.notifyItemRemoved(mList.size());
                            }

                            showErrorLayout(getString(R.string.error_unknown_json));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded() || ((Activity) mContext).isFinishing()) return;

                        // 服务器请求失败，说明网络不好，只能通过 RETRY 来重新拉取数据
                        if (mList.size() > 0) {
                            mList.remove(mList.size() - 1);
                            mAdapter.notifyItemRemoved(mList.size());
                        }

                        mSwipeRefreshLayout.setRefreshing(false);

                        String message = getString(R.string.error_network);
                        String debugMessage = "FavoriteListFragment >> " + message;
                        CommonUtils.debugToast(mContext, debugMessage);
                        showErrorLayout(message);
                        Log.e(TAG, debugMessage, error);
                    }
                });
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
        mTvAction.setText(getString(R.string.action_retry));
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });
    }
}
