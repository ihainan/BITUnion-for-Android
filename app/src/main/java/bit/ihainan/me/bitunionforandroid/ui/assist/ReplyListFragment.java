package bit.ihainan.me.bitunionforandroid.ui.assist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import bit.ihainan.me.bitunionforandroid.adapters.PostListAdapter;
import bit.ihainan.me.bitunionforandroid.models.ThreadReply;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;

public class ReplyListFragment extends Fragment {
    private final static String TAG = ReplyListFragment.class.getSimpleName();
    private Context mContext;


    // UI references
    private RecyclerView mRecyclerView;
    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Bundle tags
    public final static String THREAD_ID_TAG = "THREAD_ID_TAG";
    public final static String THREAD_AUTHOR_NAME_TAG = "THREAD_AUTHOR_NAME_TAG";
    public final static String THREAD_PAGE_POSITION = "THREAD_PAGE_POSITION";
    public final static String THREAD_REPLY_COUNT = "THREAD_REPLY_COUNT";


    // Data
    private int mTid, mPagePosition, mReplyCount;
    private String mAuthorName;
    private List<ThreadReply> mThreadPostList = new ArrayList<>();
    private PostListAdapter mAdapter;

    private void getExtra(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

        } else {
            mTid = getArguments().getInt(THREAD_ID_TAG);
            mAuthorName = getArguments().getString(THREAD_AUTHOR_NAME_TAG);
            mPagePosition = getArguments().getInt(THREAD_PAGE_POSITION) + 1;
            mReplyCount = getArguments().getInt(THREAD_REPLY_COUNT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            // super.onCreate(savedInstanceState);
            getExtra(savedInstanceState);

            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.reply_list_fragment, container, false);

            // UI references
            final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

            // Setup RecyclerView
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.detail_recycler_view);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
            setupRecyclerView();

            // Setup Swipe Refresh Layout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
            setupSwipeRefreshLayout();
        }

        return mRootView;
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
        if (notifyChange) mAdapter.notifyDataSetChanged();
        refreshData((mPagePosition - 1) * Global.LOADING_REPLIES_COUNT, (mPagePosition) * Global.LOADING_REPLIES_COUNT);
    }

    private LinearLayoutManager mLayoutManager;

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));

        mAdapter = new PostListAdapter(mContext, mThreadPostList, mAuthorName, mReplyCount);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, long to) {
        if (to > mReplyCount) to = mReplyCount - 1;
        Log.d(TAG, "refreshData >> FROM " + from + " TO " + to);

        BUApi.getPostReplies(mContext, mTid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (BUApi.checkStatus(response)) {
                            try {
                                JSONArray newListJson = response.getJSONArray("postlist");
                                List<bit.ihainan.me.bitunionforandroid.models.ThreadReply> newThreads = BUApi.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<ThreadReply>>() {
                                        });

                                // 处理数据
                                for (ThreadReply reply : newThreads) {
                                    String body = CommonUtils.decode(reply.message);
                                    reply.useMobile = body.contains("From BIT-Union Open API Project");
                                    HtmlUtil htmlUtil = new HtmlUtil(CommonUtils.decode(reply.message));
                                    reply.message = htmlUtil.makeAll();
                                }

                                // 更新 RecyclerView
                                mThreadPostList.clear();
                                mThreadPostList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                            }
                        } else {
                            Log.i(TAG, "refreshData >> 服务器返回错误信息 " + response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }
}
