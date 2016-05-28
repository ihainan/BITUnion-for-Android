package me.ihainan.bu.app.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.SuperParentAdapter;
import me.ihainan.bu.app.utils.BUApplication;

/**
 * 板块列表
 */
public class ForumFragment extends Fragment {
    private final static String TAG = ForumFragment.class.getSimpleName();
    private Context mContext;
    public final static int REQUEST_CODE = 1;

    // UI references
    private ExpandableListView mExpandableListView;
    private View mRootView;
    private Toolbar mToolbar;

    // Data
    private SuperParentAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragement_forum, container, false);

            BUApplication.makeForumGroupList(mContext);

            // UI references
            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);

            mExpandableListView = (ExpandableListView) mRootView.findViewById(R.id.forum_system_admin_lv);

            mAdapter = new SuperParentAdapter(mContext, this, BUApplication.forumListGroupList);
            mExpandableListView.setAdapter(mAdapter);
            mExpandableListView.setDivider(null);

            mExpandableListView.expandGroup(0);
        }

        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(false);
        mToolbar.setTitle(R.string.action_forum);
    }

    @Override
    public void onResume() {
        super.onResume();

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableListView.smoothScrollToPosition(0);
            }
        });
    }
}
