package me.ihainan.bu.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.ui.fragment.BasicInfoFragment;
import me.ihainan.bu.app.ui.fragment.TimelineFragment;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.ExtraApi;

public class ProfileActivity extends SwipeActivity {
    // TAGS
    private final static String TAG = ProfileActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";
    public final static String NOTIFY_ID_TAG = TAG + "_NOTIFY_ID_TAG";

    private CollapsingToolbarLayout mCollapsingToolbar;

    // Data
    private final Context mContext = this;
    private String mUsername;
    private Long mUid;
    private Integer mNotifyId;

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
            mNotifyId = bundle.getInt(NOTIFY_ID_TAG, -1);
        }

        if (mUsername == null && mUid == null) {
            mUsername = BUApplication.userSession.username;
        }

        if (mNotifyId != null && mNotifyId != -1) {
            ExtraApi.markAsRead(mContext, mNotifyId);
        }

        // Collasping Toolbar
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        mCollapsingToolbar.setTitle("");
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.TransparentText);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Follow icon
        ImageView mFollowIcon = (ImageView) findViewById(R.id.follow_icon);
        mFollowIcon.setVisibility(View.INVISIBLE);

        // Tab Layout
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new UserInfoPageAdapter(getFragmentManager(), mContext));
        mTabLayout.setupWithViewPager(mPager);
        ((TextView) findViewById(R.id.title)).setText(CommonUtils.decode(mUsername));

        // Get follow status
        getFollowStatus();

        // Swipe
        setSwipeAnyWhere(false);
    }

    private void setFollowIcon(boolean isFollow) {
        if (isFollow) {
            Drawable newIcon = getResources().getDrawable(R.drawable.ic_favorite_white_24dp);
            // newIcon.mutate().setColorFilter(Color.argb(255, 255, 76, 82), PorterDuff.Mode.SRC_IN);
            mFollowMenuItem.setIcon(newIcon);
            mFollowMenuItem.setTitle("取消关注");
        } else {
            mFollowMenuItem.setTitle("添加关注");
            mFollowMenuItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!CommonUtils.decode(BUApplication.userSession.username).equals(CommonUtils.decode(mUsername))) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
            mFollowMenuItem = menu.findItem(R.id.follow);
            return true;
        } else return false;
    }

    private boolean hasFollow;
    private MenuItem mFollowMenuItem;
    private boolean mFollowClickable = true;

    private void getFollowStatus() {
        ExtraApi.getFollowStatus(mContext, mUsername, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                try {
                    if (mFollowMenuItem == null) return;
                    if (response.getInt("code") == 0) {
                        hasFollow = response.getBoolean("data");
                        CommonUtils.debugToast(mContext, "hasFollow = " + hasFollow);
                        setFollowIcon(hasFollow);
                    } else {
                        String message = "获取关注状态失败，失败原因 " + response.getString("message");
                        if (BUApplication.debugMode) {
                            CommonUtils.debugToast(mContext, message);
                        }
                        Log.w(TAG, message);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, getString(R.string.error_parse_json) + ": " + response, e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                String message = getString(R.string.error_network);
                String debugMessage = "getFollowStatus >> " + message;
                CommonUtils.debugToast(mContext, debugMessage);
                Log.e(TAG, debugMessage, error);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.follow:
                if (mFollowClickable) {
                    mFollowClickable = !mFollowClickable;   // 不允许重复点击
                    hasFollow = !hasFollow;
                    setFollowIcon(hasFollow);
                    if (hasFollow) {
                        addFollow();
                    } else {
                        delFollow();
                    }
                }
                break;
        }
        return true;
    }

    private final Response.Listener mFollowListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            if (isFinishing()) return;
            mFollowClickable = !mFollowClickable;
            try {
                if (response.getInt("code") == 0) {
                    // 成功添加 / 删除收藏，皆大欢喜
                    String message = hasFollow ? "添加关注成功" : "取消关注成功";
                    BUApplication.hasUpdateFavor = true;
                    Log.d(TAG, "mFollowListener >> " + message);
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Oh no!!!
                    String message = (hasFollow ? "添加" : "取消") + "关注失败";
                    String debugMessage = message + " - " + response.get("message");
                    if (BUApplication.debugMode) {
                        CommonUtils.debugToast(mContext, debugMessage);
                    } else {
                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                    }

                    Log.w(TAG, debugMessage);

                    hasFollow = !hasFollow;
                    setFollowIcon(hasFollow);
                }
            } catch (JSONException e) {
                String message = (hasFollow ? "添加" : "删除") + "关注失败";
                String debugMessage = message + " - " + getString(R.string.error_parse_json) + " " + response;
                if (BUApplication.debugMode) {
                    CommonUtils.debugToast(mContext, debugMessage);
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, debugMessage, e);

                hasFollow = !hasFollow;
                setFollowIcon(hasFollow);
            }
        }
    };

    private final Response.ErrorListener mFollowErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isFinishing()) return;
            String message = (hasFollow ? "添加收藏失败，" : "取消收藏失败") + "无法连接到服务器";
            Snackbar.make(mCollapsingToolbar, message, Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasFollow) addFollow();
                    else delFollow();
                }
            }).show();
        }
    };

    private void addFollow() {
        ExtraApi.addFollow(mContext, mUsername, mFollowListener, mFollowErrorListener);
    }

    private void delFollow() {
        ExtraApi.delFollow(mContext, mUsername, mFollowListener, mFollowErrorListener);
    }

    public class UserInfoPageAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private final String[] tabTitles = new String[]{"信息", "动态"};
        private final Context mContext;

        public UserInfoPageAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = position == 0 ? new BasicInfoFragment() : new TimelineFragment();
            if (fragment instanceof TimelineFragment)
                TimelineFragment.isSetToolbar = false;
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
