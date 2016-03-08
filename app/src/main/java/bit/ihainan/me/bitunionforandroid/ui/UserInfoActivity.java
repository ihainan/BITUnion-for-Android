package bit.ihainan.me.bitunionforandroid.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;


import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.ui.fragment.BasicInfoFragment;

public class UserInfoActivity extends SwipeActivity {
    // UI references
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbar;

    // TAGS
    private final static String TAG = UserInfoActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTitle("");
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new UserInfoPageAdapter(getFragmentManager(), this));
        mTabLayout.setupWithViewPager(mPager);
        // mToolbar.setTitle("Name");

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mCollapsingToolbar.setTitle("");
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbar.setCollapsedTitleTextAppearance(R.style.ExpandedAppBar);


        setSwipeAnyWhere(false);
    }

    public class UserInfoPageAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[]{"信息", "发帖", "回帖"};
        private Context context;

        public UserInfoPageAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return new BasicInfoFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
