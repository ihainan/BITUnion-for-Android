package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bit.ihainan.me.bitunionforandroid.R;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_focus, container, false);

            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);

            // UI references
            mTabLayout = (TabLayout) mRootView.findViewById(R.id.tab_layout);
            mPager = (ViewPager) mRootView.findViewById(R.id.pager);
            mPager.setAdapter(new PagerAdapter(getFragmentManager(), mContext));
            mTabLayout.setupWithViewPager(mPager);
        }

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // private Fragment[] fragments = new Fragment[PAGE_COUNT];

        @Override
        public Fragment getItem(int position) {
            return new FavoriteListFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
