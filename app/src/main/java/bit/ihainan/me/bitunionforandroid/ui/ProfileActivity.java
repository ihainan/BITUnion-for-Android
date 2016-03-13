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

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.ui.fragment.BasicInfoFragment;
import bit.ihainan.me.bitunionforandroid.ui.fragment.TimelineFragment;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.ExtraApi;

public class ProfileActivity extends SwipeActivity {
    // TAGS
    private final static String TAG = ProfileActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";

    // UI references
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private Toolbar mToolbar;
    private ImageView mFollowIcon;
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

        // Follow icon
        mFollowIcon = (ImageView) findViewById(R.id.follow_icon);
        mFollowIcon.setVisibility(View.INVISIBLE);

        // Tab Layout
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new UserInfoPageAdapter(getFragmentManager(), this));
        mTabLayout.setupWithViewPager(mPager);
        ((TextView) findViewById(R.id.title)).setText(CommonUtils.decode(mUsername));

        // Get follow status
        getFollowStatus();

        // Swipe
        setSwipeAnyWhere(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!Global.userSession.username.equals(mUsername)) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
            mFollowMenuItem = menu.findItem(R.id.follow);
            return true;
        } else return false;
    }

    private boolean hasFollow;
    private MenuItem mFollowMenuItem;
    private boolean mFollowClickable = true;

    private void getFollowStatus() {
        if (mFollowMenuItem == null) return;
        ExtraApi.getFollowStatus(this, mUsername, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("code") == 0) {
                        hasFollow = response.getBoolean("data");
                        CommonUtils.debugToast(ProfileActivity.this, "hasFollow = " + hasFollow);
                        if (hasFollow) {
                            mFollowMenuItem.setTitle("取消关注");
                            mFollowIcon.setVisibility(View.VISIBLE);
                        } else {
                            mFollowMenuItem.setTitle("添加关注");
                            mFollowIcon.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        String message = "获取关注状态失败，失败原因 " + response.getString("message");
                        if (Global.debugMode) {
                            CommonUtils.debugToast(ProfileActivity.this, message);
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
                Log.e(TAG, "getFollowStatus >> " + getString(R.string.error_network), error);
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
                    if (hasFollow) {
                        // 之前是删除，想要添加
                        mFollowMenuItem.setTitle("取消关注");
                        mFollowIcon.setVisibility(View.VISIBLE);
                        addFollow();
                    } else {
                        mFollowMenuItem.setTitle("添加关注");
                        mFollowIcon.setVisibility(View.INVISIBLE);
                        delFollow();
                    }
                }
                break;
        }
        return true;
    }

    private Response.Listener mFollowListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mFollowClickable = !mFollowClickable;
            try {
                if (response.getInt("code") == 0) {
                    // 成功添加 / 删除收藏，皆大欢喜
                    String message = hasFollow ? "添加关注成功" : "取消关注成功";
                    Global.hasUpdateFavor = true;
                    Log.d(TAG, "mFollowListener >> " + message);
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Oh no!!!
                    String message = (hasFollow ? "添加" : "取消") + "关注失败";
                    String debugMessage = message + " - " + response.get("message");
                    if (Global.debugMode) {
                        CommonUtils.debugToast(ProfileActivity.this, debugMessage);
                    } else {
                        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                    Log.w(TAG, debugMessage);

                    hasFollow = !hasFollow;
                    if (hasFollow) {
                        mFollowMenuItem.setTitle("取消关注");
                        mFollowIcon.setVisibility(View.VISIBLE);
                    } else {
                        mFollowMenuItem.setTitle("添加关注");
                        mFollowIcon.setVisibility(View.INVISIBLE);
                    }
                }
            } catch (JSONException e) {
                String message = (hasFollow ? "添加" : "删除") + "关注失败";
                String debugMessage = message + " - " + getString(R.string.error_parse_json) + " " + response;
                if (Global.debugMode) {
                    CommonUtils.debugToast(ProfileActivity.this, debugMessage);
                } else {
                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, debugMessage, e);

                hasFollow = !hasFollow;
                if (hasFollow) {
                    mFollowMenuItem.setTitle("取消关注");
                    mFollowIcon.setVisibility(View.VISIBLE);
                } else {
                    mFollowMenuItem.setTitle("添加关注");
                    mFollowIcon.setVisibility(View.INVISIBLE);
                }
            }
        }
    };

    private Response.ErrorListener mFollowErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            String message = (hasFollow ? "取消收藏失败，" : "添加收藏失败") + "无法连接到服务器";
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
        ExtraApi.addFollow(this, mUsername, mFollowListener, mFollowErrorListener);
    }

    private void delFollow() {
        ExtraApi.delFollow(this, mUsername, mFollowListener, mFollowErrorListener);
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
            Fragment fragment = position == 0 ? new BasicInfoFragment() : new TimelineFragment();
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
