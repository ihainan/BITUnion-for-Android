package me.ihainan.bu.app.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.NewFollowingListActivity;

/**
 * 关注列表
 */
public class FocusFragment extends Fragment {
    private final static String TAG = FocusFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private View mRootView;
    private Toolbar mToolbar;

    // Data
    private PagerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (mAdapter == null)
            mAdapter = new PagerAdapter(getChildFragmentManager(), mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_focus, container, false);

            // Toolbar
            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);

            // TabLayout
            mTabLayout = (TabLayout) mRootView.findViewById(R.id.tab_layout);
            mPager = (ViewPager) mRootView.findViewById(R.id.pager);
        }


        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mPager);

        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.focus_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.focus_list:
                Intent intent = new Intent(mContext, NewFollowingListActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mToolbar.setTitle(R.string.action_focus);
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"动态", "收藏"};
        private Context context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment fragment = new TimelineFragment();
                Bundle args = new Bundle();
                args.putString(TimelineFragment.TIMELINE_ACTION_TAG, "FOCUS");
                fragment.setArguments(args);
                return fragment;
            } else return new FavoriteListFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }
}
