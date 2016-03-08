package bit.ihainan.me.bitunionforandroid.ui.fragment;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 *
 */
public class FocusListFragment extends Fragment {
    private final static String TAG = FocusListFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            Global.makeForumGroupList(mContext);

            // UI references
        }

        return mRootView;
    }
}
