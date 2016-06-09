package me.ihainan.bu.app.ui.fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.PersonalThreadAndPostAdapter;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 用户回帖或者主题列表
 */
public class PersonalThreadAndPostFragment extends BasicRecyclerViewFragment<Post> {
    // TAGs
    public final static String TAG = PersonalThreadAndPostFragment.class.getSimpleName();
    public final static String USERNAME_TAG = TAG + "USERNAME_TAG";
    public final static String ACTION_TAG = TAG + "ACTION_TAG";
    public final static String ACTION_THREAD = "ACTION_THREAD";
    public final static String ACTION_POST = "ACTION_POST";

    // Data
    private String mUsername;
    private String mAction;

    @Override
    protected String getNoNewDataMessage() {
        if (ACTION_THREAD.equals(mAction)) {
            return mContext.getString(R.string.error_no_new_threads);
        } else if (ACTION_THREAD.equals(ACTION_POST)) {
            return mContext.getString(R.string.error_no_new_posts);
        } else return mContext.getString(R.string.error_unknown);
    }

    @Override
    protected String getFragmentTag() {
        return PersonalThreadAndPostFragment.class.getSimpleName();
    }

    @Override
    protected List<Post> processList(List<Post> list) {
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
            mUsername = bundle.getString(USERNAME_TAG);
            if (mUsername == null) mUsername = BUApplication.username;
            mAction = bundle.getString(ACTION_TAG);
            if (mAction == null) mAction = ACTION_THREAD;
        }
    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new PersonalThreadAndPostAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_TIMELINE_COUNT;
    }

    @Override
    protected void refreshData() {
        if (mAction.equals(ACTION_THREAD))
            ExtraApi.getUserThreadList(mContext, mUsername, from, to, listener, errorListener);
        else ExtraApi.getUserPostList(mContext, mUsername, from, to, listener, errorListener);
    }
}
