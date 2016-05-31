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
import me.ihainan.bu.app.ui.fragment.SearchResultFragment;


public class SearchActivity extends SwipeActivity {
    private final String TAG = SearchActivity.class.getSimpleName();

    // UI
    private FloatingSearchView mSearchView;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private PagerAdapter mPagerAdapter;

    // Data
    private String mSearchStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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


    private void doSearch() {
        mPagerAdapter.reloadAll(mSearchStr);
        mSearchView.setSearchText(mSearchStr);
        mSearchView.setSearchHint(mSearchStr);
        mSearchView.clearSearchFocus();
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
