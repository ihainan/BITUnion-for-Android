package me.ihainan.bu.app.ui.fragment;

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
 * 搜索结果 Fragment
 */
public class SearchResultFragment extends BasicRecyclerViewFragment<Post> {
    // Tags
    public final static String SEARCH_ACTION_TAG = "SEARCH_ACTION_TAG";     // 搜索动作，可选 SEARCH_ACTION_THREAD / SEARCH_ACTION_POST / SEARCH_ACTION_USER
    public final static String SEARCH_KEYWORD_TAG = "SEARCH_KEYWORD_TAG";   // 搜索关键词

    public final static String SEARCH_ACTION_THREAD = "SEARCH_ACTION_THREAD";     // 搜索动作 - 搜索主题
    public final static String SEARCH_ACTION_POST = "SEARCH_ACTION_POST";     // 搜索动作 - 搜索回帖
    public final static String SEARCH_ACTION_USER = "SEARCH_ACTION_USER";     // 搜索动作 - 搜索用户

    // Data
    private String mKeyword;
    private String mAction;

    @Override
    protected String getNoNewDataMessage() {
        return getString(R.string.error_no_search_result);
    }

    @Override
    protected String getFragmentTag() {
        return SearchResultFragment.class.getSimpleName();
    }

    /**
     * 更新列表数据
     */
    protected void refreshData() {
        mErrorLayout.setVisibility(View.GONE);
        if (mAction.equals(SEARCH_ACTION_THREAD))
            ExtraApi.searchThreads(mContext, mKeyword, from, to, listener, errorListener);
        else if (mAction.endsWith(SEARCH_ACTION_POST))
            ExtraApi.searchPosts(mContext, mKeyword, from, to, listener, errorListener);
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
        mAction = getArguments().getString(SEARCH_ACTION_TAG);
        mKeyword = getArguments().getString(SEARCH_KEYWORD_TAG);
        if (mKeyword == null) mKeyword = CommonUtils.decode(BUApplication.username);
        if (mAction == null) mAction = SEARCH_ACTION_USER;
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
