package bit.ihainan.me.bitunionforandroid.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.ui.fragment.BasicInfoFragment;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

public class ProfileActivity extends SwipeActivity {
    // TAGS
    private final static String TAG = ProfileActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";

    // UI references
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbar;

    // Data
    private String mUsername;
    private Long mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Username / User ID
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUsername = bundle.getString(USER_NAME_TAG);
            mUid = bundle.getLong(USER_ID_TAG);
        }

        if (mUsername == null && mUid == null) {
            mUsername = Global.userSession.username;
        }

        // Collasping Toolbar
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        mCollapsingToolbar.setTitle("");
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.TransparentText);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Tab Layout
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new UserInfoPageAdapter(getFragmentManager(), this));
        mTabLayout.setupWithViewPager(mPager);
        ((TextView) findViewById(R.id.title)).setText(CommonUtils.decode(mUsername));

        // Swipe
        setSwipeAnyWhere(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    public class UserInfoPageAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"信息", "动态"};
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
            Fragment fragment = new BasicInfoFragment();
            Bundle args = new Bundle();
            args.putString(ProfileActivity.USER_NAME_TAG, mUsername);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }
}
