package me.ihainan.bu.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.ui.fragment.SearchPostResultFragment;
import me.ihainan.bu.app.ui.fragment.SearchUserResultFragment;


public class SearchActivity extends SwipeActivity {
    // TAGs
    private final static String TAG = SearchActivity.class.getSimpleName();
    public final static String FID_TAG = TAG + "_FID_TAG";

    // UI
    private FloatingSearchView mSearchView;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private PagerAdapter mPagerAdapter;

    // Data
    private String mSearchStr = "";
    private Long mFid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 获取额外数据
        getExtra();

        // UI
        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        // ViewPager & TabLayout
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(3);
        mTabLayout.setupWithViewPager(mPager);

        // SearchView
        mSearchView.focusSearch(View.FOCUS_RIGHT);
        mSearchView.requestFocus();
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                doSearch();
            }
        });

        mSearchView.setOnHomeActionClickListener(
                new FloatingSearchView.OnHomeActionClickListener() {
                    @Override
                    public void onHomeClicked() {
                        finish();
                    }
                });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                mSearchStr = newQuery;
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
                                            @Override
                                            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

                                            }

                                            @Override
                                            public void onSearchAction(String currentQuery) {
                                                mSearchStr = currentQuery;
                                                doSearch();
                                            }
                                        }
        );

        setSwipeAnyWhere(false);
    }

    /**
     * 获取额外数据
     */
    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mFid = bundle.getLong(FID_TAG, -1);
        } else mFid = -1L;
    }

    private void doSearch() {
        mPagerAdapter.reloadAll(mSearchStr);
        mSearchView.setSearchText(mSearchStr);
        mSearchView.setSearchHint(mSearchStr);
        mSearchView.clearSearchFocus();
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        private int page_count = 2;
        private String tabTitles[] = new String[]{"主题", "回帖", "用户"};
        private Context mContext;
        private Fragment fragments[];

        public void reloadAll(String keyword) {
            ((SearchPostResultFragment) fragments[0]).reloadData(keyword);
            ((SearchPostResultFragment) fragments[1]).reloadData(keyword);
            if (page_count >= 3) ((SearchUserResultFragment) fragments[2]).reloadData(keyword);
        }

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            page_count = (mFid == null || mFid == -1) ? 3 : 2;
            fragments = new Fragment[page_count];
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return page_count;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                Fragment fragment = new SearchPostResultFragment();
                fragments[position] = fragment;
                Bundle args = new Bundle();
                args.putString(SearchPostResultFragment.SEARCH_ACTION_TAG, SearchPostResultFragment.SEARCH_ACTION_THREAD);
                if (mFid != -1)
                    args.putLong(SearchPostResultFragment.SEARCH_FID_TAG, mFid);
                fragment.setArguments(args);
                return fragment;
            } else if (position == 1) {
                Fragment fragment = new SearchPostResultFragment();
                fragments[position] = fragment;
                Bundle args = new Bundle();
                args.putString(SearchPostResultFragment.SEARCH_ACTION_TAG, SearchPostResultFragment.SEARCH_ACTION_POST);
                if (mFid != -1)
                    args.putLong(SearchPostResultFragment.SEARCH_FID_TAG, mFid);
                fragment.setArguments(args);
                return fragment;
            } else if (position == 2) {
                Fragment fragment = new SearchUserResultFragment();
                fragments[position] = fragment;
                return fragment;
            }

            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
