package me.ihainan.bu.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.SearchUserResultAdapter;
import me.ihainan.bu.app.models.User;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 用户相关搜索结果 Fragment
 */
public class SearchUserResultFragment extends BasicRecyclerViewFragment<User> {
    public final static String TAG = SearchUserResultFragment.class.getSimpleName();
    public final static String SEARCH_KEYWORD_TAG = "SEARCH_KEYWORD_TAG";   // 搜索关键词

    // Data
    private String mKeyword;

    @Override
    protected String getNoNewDataMessage() {
        return mContext.getString(R.string.error_no_search_result);
    }

    protected void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setEnabled(false);
    }

    public void reloadData(String keyword) {
        mKeyword = keyword;
        super.reloadData();
    }

    @Override
    protected String getFragmentTag() {
        return TAG;
    }

    @Override
    protected List<User> processList(List<User> list) {
        if (mKeyword.equals("")) list.clear();
        return list;
    }

    @Override
    protected List<User> parseResponse(JSONObject response) throws Exception {
        return BUApi.MAPPER.readValue(response.get("data").toString(),
                new TypeReference<List<User>>() {
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
            mKeyword = bundle.getString(SEARCH_KEYWORD_TAG);
        }
        if (mKeyword == null) mKeyword = CommonUtils.decode(BUApplication.username);
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new SearchUserResultAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_SEARCH_RESULT_COUNT;
    }

    @Override
    protected void refreshData() {
        ExtraApi.searchUsers(mContext, mKeyword, from, to, listener, errorListener);
    }
}
