package me.ihainan.bu.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.ui.fragment.FavoriteListFragment;
import me.ihainan.bu.app.ui.fragment.FollowingListFragment;
import me.ihainan.bu.app.ui.fragment.TimelineFragment;

public class FocusActivity extends SwipeActivity {
    private final static String TAG = FocusActivity.class.getSimpleName();

    // Data
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        // Swipe to back
        setSwipeAnyWhere(false);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setTitle(R.string.action_focus);

        // TabLayout & VierPager
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter mAdapter = new PagerAdapter(getFragmentManager(), mContext);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(1);
        mTabLayout.setupWithViewPager(mPager);
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private final String[] tabTitles = new String[]{"动态", "收藏"};
        private final Context context;

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
                TimelineFragment fragment = new TimelineFragment();
                Bundle args = new Bundle();
                args.putString(TimelineFragment.TIMELINE_ACTION_TAG, "FOCUS");
                fragment.setArguments(args);
                TimelineFragment.isSetToolbar = false;
                return fragment;
            } else {
                FavoriteListFragment fragment = new FavoriteListFragment();
                FavoriteListFragment.isSetToolbar = false;
                return fragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.focus_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.focus_list:
                // Intent intent = new Intent(this, FollowingListActivity.class);
                Intent intent = new Intent(mContext, ActivityWithFrameLayout.class);
                intent.putExtra(ActivityWithFrameLayout.TITLE_TAG, getString(R.string.title_activity_new_following_list));
                intent.putExtra(ActivityWithFrameLayout.FRAGMENT_TAG, FollowingListFragment.class.getSimpleName());
                startActivity(intent);
                break;
        }

        return true;
    }
}
