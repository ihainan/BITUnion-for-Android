package bit.ihainan.me.bitunionforandroid.ui.assist;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.SuperParentAdapter;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * Home Page Fragment
 */
public class ForumFragment extends Fragment {
    private final static String TAG = HomePageFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private ExpandableListView mExpandableListView;
    private View mRootView;
    private AppBarLayout mAppBarLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragement_forum, container, false);

            Global.makeForumGroupList(mContext);

            // UI references
            mExpandableListView = (ExpandableListView) mRootView.findViewById(R.id.forum_system_admin_lv);

            SuperParentAdapter adapter = new SuperParentAdapter(mContext, Global.forumListGroupList);
            mExpandableListView.setAdapter(adapter);
            mExpandableListView.setDivider(null);

            mExpandableListView.expandGroup(4);
        }

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((Activity) mContext).getWindow().getDecorView().findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableListView.smoothScrollToPosition(0);
            }
        });
    }
}
