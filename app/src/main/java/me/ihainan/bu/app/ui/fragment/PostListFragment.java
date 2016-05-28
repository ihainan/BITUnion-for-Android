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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.PostListAdapter;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * Post List Fragment
 */
public class PostListFragment extends Fragment {
    // Tags
    private final static String TAG = PostListFragment.class.getSimpleName();
    public final static String PAGE_POSITION_TAG = "PAGE_POSITION_TAG";
    public final static String PAGE_INDEX_TAG = "PAGE_INDEX_TAG";

    private Context mContext;

    // UI references
    private View mRootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Data
    private Long mTid, mReplyCount;
    private Integer mPagePosition, mPageIndex;
    private String mAuthorName;
    private List<Post> mList = new ArrayList<>();
    private PostListAdapter mAdapter;
    private boolean shouldJump = true;  // 是否需要跳到指定楼层
    public static boolean isSetToolbar = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            // Extra tid & page position
            mTid = getArguments().getLong(PostListActivity.THREAD_ID_TAG);
            mAuthorName = getArguments().getString(PostListActivity.THREAD_AUTHOR_NAME_TAG);
            mReplyCount = getArguments().getLong(PostListActivity.THREAD_REPLY_COUNT_TAG);
            mPagePosition = getArguments().getInt(PAGE_POSITION_TAG);
            mPageIndex = getArguments().getInt(PAGE_INDEX_TAG);

            // Error Layout
            mErrorLayout = (RelativeLayout) mRootView.findViewById(R.id.error_layout);
            mErrorLayout.setVisibility(View.GONE);
            mTvErrorMessage = (TextView) mRootView.findViewById(R.id.error_message);
            mTvAction = (TextView) mRootView.findViewById(R.id.action_text);
            mTvAction.setVisibility(View.GONE);

            // Setup RecyclerView
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
            setupRecyclerView();
            if (!isSetToolbar) {
                isSetToolbar = !isSetToolbar;
                Log.d(TAG, "onCreateView >> " + mPagePosition);
                getActivity().findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
                    }
                });
            }

            // Setup SwipeLayout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
            setupSwipeRefreshLayout();
        }

        return mRootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mContext != null && mRecyclerView != null) {
            getActivity().findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, 0);
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), 0, mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = new PostListAdapter(mContext, mList, mAuthorName, mReplyCount);
        mRecyclerView.setAdapter(mAdapter);
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

    private void showErrorLayout(String message) {
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

    /**
     * 重新拉取数据
     */
    private void reloadData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mList.clear();
        refreshData(mPagePosition * BUApplication.LOADING_POSTS_COUNT, (mPagePosition + 1) * BUApplication.LOADING_POSTS_COUNT);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        mErrorLayout.setVisibility(View.GONE);

        BUApi.getPostReplies(mContext, mTid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                        mSwipeRefreshLayout.setRefreshing(false);
                        try {
                            if (BUApi.checkStatus(response)) {
                                JSONArray newListJson = response.getJSONArray("postlist");
                                List<Post> newThreads = BUApi.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<Post>>() {
                                        });

                                // 处理数据
                                for (Post reply : newThreads) {
                                    // 处理正文
                                    reply.useMobile = CommonUtils.decode(reply.message).contains("From BIT-Union Open API Project");
                                    reply.message = HtmlUtil.formatHtml(CommonUtils.decode(reply.message));

                                    // 楼层
                                    reply.floor = mPagePosition * BUApplication.LOADING_POSTS_COUNT + newThreads.indexOf(reply) + 1;

                                    // 获取设备信息
                                    getDeviceName(reply);
                                }

                                // 更新 RecyclerView
                                mList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();

                                // 跳转到指定的位置，仅跳转一次
                                if (shouldJump && mPageIndex != null) {
                                    shouldJump = false;
                                    mLayoutManager.scrollToPositionWithOffset(mPageIndex, 0);
                                }
                            } else if (response.getString("msg").equals(BUApi.FORUM_NO_PERMISSION_MSG)) {
                                String message = getString(R.string.error_forum_no_permission) + ": " + response.getString("msg");
                                String debugMessage = message + " - " + response;
                                Log.w(TAG, debugMessage);
                                CommonUtils.debugToast(mContext, debugMessage);
                                showErrorLayout(message);
                            } else {
                                String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                                String debugMessage = message + " - " + response;
                                Log.w(TAG, debugMessage);
                                CommonUtils.debugToast(mContext, debugMessage);
                                showErrorLayout(message);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                            showErrorLayout(getString(R.string.error_parse_json));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                        mSwipeRefreshLayout.setRefreshing(false);

                        String message = getString(R.string.error_network);
                        String debugMessage = "getPostReplies >> " + message;
                        CommonUtils.debugToast(mContext, debugMessage);

                        showErrorLayout(message);
                        Log.e(TAG, debugMessage, error);
                    }
                });
    }

    private void getDeviceName(Post reply) {
        if (reply.message.contains("客户端") || reply.message.contains("发自"))
            Log.d(TAG, "getDeviceName >> " + reply.message);
        for (String regex : HtmlUtil.REGEX_DEVICE_ARRAY) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(reply.message);
            while (matcher.find()) {
                // 找到啦！
                reply.deviceName = matcher.group(1);    // 可能出现多次，替换所有，提取最后一个
                if (reply.deviceName.equals("WindowsPhone8"))
                    reply.deviceName = "Windows Phone 8";
                if (reply.deviceName.equals("联盟iOS客户端"))
                    reply.deviceName = "iPhone";
                Log.d(TAG, "deviceName >> " + reply.deviceName);
                reply.message = reply.message.replace(matcher.group(0), "");
                reply.message = HtmlUtil.replaceOther(reply.message);
            }
        }
    }
}
