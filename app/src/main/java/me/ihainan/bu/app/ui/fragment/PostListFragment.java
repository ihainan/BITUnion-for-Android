package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.PostListAdapter;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.assist.SimpleDividerItemDecoration;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * 帖子列表 Fragment.
 */
public class PostListFragment extends BasicRecyclerViewFragment<Post> {
    // Tags
    public final static String PAGE_POSITION_TAG = "PAGE_POSITION_TAG";
    public final static String PAGE_INDEX_TAG = "PAGE_INDEX_TAG";

    // Data
    private Long mTid, mReplyCount;
    private Integer mPagePosition, mPageIndex;
    private String mAuthorName;
    private boolean shouldJump = true;  // 是否需要跳到指定楼层
    public static boolean isSetToolbar = false;

    {
        listener = new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (!isAdded() || ((Activity) mContext).isFinishing()) return;
                mSwipeRefreshLayout.setRefreshing(false);
                try {
                    if (checkStatus(response)) {
                        List<Post> newItems = parseResponse(response);

                        // 处理数据
                        newItems = processList(newItems);

                        // 更新 RecyclerView
                        mList.addAll(newItems);
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
        };
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
    protected void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);

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
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), 0, mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
    }

    @Override
    protected String getNoNewDataMessage() {
        return null;
    }

    @Override
    protected String getFragmentTag() {
        return PostListFragment.class.getSimpleName();
    }


    @Override
    protected void reloadData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mList.clear();
        refreshData(mPagePosition * LOADING_COUNT, (mPagePosition + 1) * LOADING_COUNT);
    }

    @Override
    protected List<Post> processList(List<Post> list) {
        for (Post reply : list) {
            // 处理正文
            reply.useMobile = CommonUtils.decode(reply.message).contains("From BIT-Union Open API Project");
            reply.message = HtmlUtil.formatHtml(CommonUtils.decode(reply.message));

            // 楼层
            reply.floor = mPagePosition * LOADING_COUNT + list.indexOf(reply) + 1;

            // 获取设备信息
            getDeviceName(reply);
        }

        return list;
    }

    @Override
    protected List<Post> parseResponse(JSONObject response) throws Exception {
        JSONArray newListJson = response.getJSONArray("postlist");
        return BUApi.MAPPER.readValue(newListJson.toString(),
                new TypeReference<List<Post>>() {
                });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return BUApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {
        mTid = getArguments().getLong(PostListActivity.THREAD_ID_TAG);
        mAuthorName = getArguments().getString(PostListActivity.THREAD_AUTHOR_NAME_TAG);
        mReplyCount = getArguments().getLong(PostListActivity.THREAD_REPLY_COUNT_TAG);
        mPagePosition = getArguments().getInt(PAGE_POSITION_TAG);
        mPageIndex = getArguments().getInt(PAGE_INDEX_TAG);
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new PostListAdapter(mContext, mList, mAuthorName, mReplyCount);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_POSTS_COUNT;
    }

    @Override
    protected void refreshData(long from, long to) {
        BUApi.getPostReplies(mContext, mTid, from, to, listener, errorListener);
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
