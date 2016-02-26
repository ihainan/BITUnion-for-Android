package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.ReplyListFragment;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.Global;

public class ThreadDetailWithTabLayoutActivity extends SwipeActivity {
    private final static String TAG = ThreadDetailWithTabLayoutActivity.class.getSimpleName();

    // UI references
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    // Bundle tags
    public final static String THREAD_ID_TAG = "THREAD_ID_TAG";
    public final static String THREAD_NAME_TAG = "THREAD_NAME_TAG";
    public final static String THREAD_AUTHOR_NAME_TAG = "THREAD_AUTHOR_NAME_TAG";
    public final static String THREAD_REPLY_COUNT_TAG = "THREAD_REPLY_COUNT_TAG";

    // Data
    private int mTid, mReplyCount, mNumberOfPages;  // Thread ID
    private String mAuthorName;  // Thread name

    private void getExtra() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mTid = (int) bundle.getLong(THREAD_ID_TAG);
        mReplyCount = (int) bundle.getLong(THREAD_REPLY_COUNT_TAG);
        mAuthorName = bundle.getString(THREAD_AUTHOR_NAME_TAG);
        mNumberOfPages = mReplyCount % Global.LOADING_REPLIES_COUNT == 0
                ? mReplyCount / Global.LOADING_REPLIES_COUNT
                : mReplyCount / Global.LOADING_REPLIES_COUNT + 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_detail_with_tab_layout);

        // Get extra data
        getExtra();

        // Setup actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Setup TableLayout & ViewPager
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mViewPager.setAdapter(new ThreadPagerAdapter(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        setSwipeAnyWhere(false);
    }


    public class ThreadPagerAdapter extends FragmentStatePagerAdapter {
        private SparseArray<ReplyListFragment> registeredFragments = new SparseArray<>();

        public ThreadPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            ReplyListFragment replyListFragment = registeredFragments.get(position);
            if (replyListFragment == null) {
                replyListFragment = new ReplyListFragment();
                Bundle args = new Bundle();
                args.putInt(ReplyListFragment.THREAD_ID_TAG, mTid);
                args.putString(ReplyListFragment.THREAD_AUTHOR_NAME_TAG, mAuthorName);
                args.putInt(ReplyListFragment.THREAD_PAGE_POSITION, position);
                args.putInt(ReplyListFragment.THREAD_REPLY_COUNT, mReplyCount);
                replyListFragment.setArguments(args);
                registeredFragments.put(position, replyListFragment);
            }

            return replyListFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Page #" + position;
        }

        @Override
        public int getCount() {
            return mNumberOfPages;
        }
    }
}