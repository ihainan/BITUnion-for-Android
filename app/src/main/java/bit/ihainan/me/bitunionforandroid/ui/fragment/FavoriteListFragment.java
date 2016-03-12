package bit.ihainan.me.bitunionforandroid.ui.fragment;


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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.FavoriteListAdapter;
import bit.ihainan.me.bitunionforandroid.models.Favorite;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.network.ExtraApi;

public class FavoriteListFragment extends Fragment {
    private final static String TAG = FavoriteListFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private View mRootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;

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

            Global.makeForumGroupList(mContext);

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

        refreshData(mCurrentPosition, mCurrentPosition + Global.LOADING_REPLIES_COUNT - 1);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        ExtraApi.getFavoriteList(mContext, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                                    mCurrentPosition += Global.LOADING_REPLIES_COUNT;
                                    mList.addAll(newFavorites);
                                    mAdapter.notifyDataSetChanged();
                                    Global.hasUpdateFavor = false;
                                }

                                // 判断是否到头
                                if (newFavorites.size() == Global.LOADING_FAVORITES_COUNT) {
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
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 服务器请求失败，说明网络不好，只能通过 RETRY 来重新拉取数据
                        if (mList.size() > 0) {
                            mList.remove(mList.size() - 1);
                            mAdapter.notifyItemRemoved(mList.size());
                        }

                        mSwipeRefreshLayout.setRefreshing(false);

                        Snackbar.make(mRecyclerView, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadMore(true);
                            }
                        }).show();

                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }
}
