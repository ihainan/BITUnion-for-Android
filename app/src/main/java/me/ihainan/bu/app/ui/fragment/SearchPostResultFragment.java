package me.ihainan.bu.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.SearchThreadOrPostResultAdapter;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 帖子相关搜索结果 Fragment
 */
public class SearchPostResultFragment extends BasicRecyclerViewFragment<Post> {
    // Tags
    private final static String TAG = SearchPostResultFragment.class.getSimpleName();
    public final static String SEARCH_ACTION_TAG = "SEARCH_ACTION_TAG";     // 搜索动作，可选 SEARCH_ACTION_THREAD / SEARCH_ACTION_POST / SEARCH_ACTION_USER
    public final static String SEARCH_KEYWORD_TAG = "SEARCH_KEYWORD_TAG";   // 搜索关键词
    public final static String SEARCH_FID_TAG = TAG + "_FID_TAG";

    public final static String SEARCH_ACTION_THREAD = "SEARCH_ACTION_THREAD";     // 搜索动作 - 搜索主题
    public final static String SEARCH_ACTION_POST = "SEARCH_ACTION_POST";     // 搜索动作 - 搜索回帖

    // Data
    private String mKeyword;
    private String mAction;
    private Long mFid;

    @Override
    protected String getNoNewDataMessage() {
        return mContext.getString(R.string.error_no_search_result);
    }

    @Override
    protected String getFragmentTag() {
        return SearchPostResultFragment.class.getSimpleName();
    }

    /**
     * 更新列表数据
     */
    protected void refreshData() {
        mErrorLayout.setVisibility(View.GONE);
        if (mAction.equals(SEARCH_ACTION_THREAD)) {
            if (mFid != -1)
                ExtraApi.searchThreadsInForum(mContext, mKeyword, mFid, from, to, listener, errorListener);
            else ExtraApi.searchThreads(mContext, mKeyword, from, to, listener, errorListener);
        } else if (mAction.endsWith(SEARCH_ACTION_POST)) {
            if (mFid != -1)
                ExtraApi.searchPostsInForum(mContext, mKeyword, mFid, from, to, listener, errorListener);
            else ExtraApi.searchPosts(mContext, mKeyword, from, to, listener, errorListener);
        }
    }

    public void reloadData(String keyword) {
        mKeyword = keyword;
        ((SearchThreadOrPostResultAdapter) mAdapter).setKeyword(mKeyword);
        super.reloadData();
    }

    protected void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    @Override
    protected List<Post> processList(List<Post> list) {
        if (mKeyword.equals("")) list.clear();
        return list;
    }

    @Override
    protected List<Post> parseResponse(JSONObject response) throws Exception {
        return BUApi.MAPPER.readValue(response.get("data").toString(),
                new TypeReference<List<Post>>() {
                });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return ExtraApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAction = bundle.getString(SEARCH_ACTION_TAG);
            mKeyword = bundle.getString(SEARCH_KEYWORD_TAG);
            mFid = bundle.getLong(SEARCH_FID_TAG, -1);
        }

        if (mKeyword == null) mKeyword = CommonUtils.decode(BUApplication.username);
        if (mAction == null) mAction = SEARCH_ACTION_POST;
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new SearchThreadOrPostResultAdapter(mContext, mKeyword, mList, mAction.equals(SEARCH_ACTION_THREAD));
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_SEARCH_RESULT_COUNT;
    }
}
