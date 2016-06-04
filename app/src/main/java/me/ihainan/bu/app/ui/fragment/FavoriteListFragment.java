package me.ihainan.bu.app.ui.fragment;

import android.support.v7.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.FavoriteListAdapter;
import me.ihainan.bu.app.models.Favorite;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 收藏列表 Fragment
 */
public class FavoriteListFragment extends BasicRecyclerViewFragment<Favorite> {
    @Override
    protected String getNoNewDataMessage() {
        return getString(R.string.error_no_favorites);
    }

    @Override
    protected String getFragmentTag() {
        return FavoriteListFragment.class.getSimpleName();
    }

    @Override
    protected List<Favorite> processList(List<Favorite> list) {
        return list;
    }

    @Override
    protected List<Favorite> parseResponse(JSONObject response) throws Exception {
        JSONArray newListJson = response.getJSONArray("data");
        return BUApi.MAPPER.readValue(newListJson.toString(), new TypeReference<List<Favorite>>() {
        });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return ExtraApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new FavoriteListAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_FAVORITES_COUNT;
    }

    @Override
    protected void refreshData(long from, long to) {
        ExtraApi.getFavoriteList(mContext, from, to, listener, errorListener);
    }
}
