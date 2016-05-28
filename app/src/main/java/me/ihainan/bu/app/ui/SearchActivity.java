package me.ihainan.bu.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.fragment.SearchResultFragment;


public class SearchActivity extends Activity {
    private final String TAG = SearchActivity.class.getSimpleName();

    // UI
    private FloatingSearchView mSearchView;
    private ViewGroup mParentView;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // UI
        mParentView = (ViewGroup) findViewById(R.id.parent_view);
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        // ViewPager & TabLayout
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mPager);

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                mPagerAdapter.reloadAll(mSearchView.getQuery());
                mSearchView.setSearchHint(mSearchView.getQuery());
                mSearchView.clearFocus();
            }
        });
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"主题", "回帖"};
        private Context mContext;
        private Fragment fragments[] = new Fragment[PAGE_COUNT];

        public void reloadAll(String keyword) {
            for (int i = 0; i < PAGE_COUNT; ++i) {
                ((SearchResultFragment) fragments[i]).reloadData(keyword);
            }
        }

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment fragment = new SearchResultFragment();
                fragments[position] = fragment;
                Bundle args = new Bundle();
                args.putString(SearchResultFragment.SEARCH_ACTION_TAG, SearchResultFragment.SEARCH_ACTION_THREAD);
                args.putString(SearchResultFragment.SEARCH_KEYWORD_TAG, SearchResultFragment.SEARCH_ACTION_THREAD);
                fragment.setArguments(args);
                return fragment;
            } else {
                Fragment fragment = new SearchResultFragment();
                fragments[position] = fragment;
                Bundle args = new Bundle();
                args.putString(SearchResultFragment.SEARCH_ACTION_TAG, SearchResultFragment.SEARCH_ACTION_POST);
                args.putString(SearchResultFragment.SEARCH_KEYWORD_TAG, SearchResultFragment.SEARCH_ACTION_THREAD);
                fragment.setArguments(args);
                return fragment;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

}
