package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * 用户基本资料
 */
public class BasicInfoFragment extends Fragment {
    private final static String TAG = BasicInfoFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private View mRootView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.user_info_content, container, false);

            Global.makeForumGroupList(mContext);

            // Setup SwipeLayout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
        }

        return mRootView;
    }
}
