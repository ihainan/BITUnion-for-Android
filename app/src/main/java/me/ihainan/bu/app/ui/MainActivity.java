package me.ihainan.bu.app.ui;

import android.app.Application;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.json.JSONObject;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.fragment.HomeFragment;
import me.ihainan.bu.app.ui.fragment.NotificationListFragment;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.ExtraApi;
import me.ihainan.bu.app.utils.network.SessionUpdateService;
import me.ihainan.bu.app.utils.ui.IconFontHelper;

public class MainActivity extends AppCompatActivity {
    // Tags
    private final static String TAG = MainActivity.class.getSimpleName();

    // UI elements
    private DrawerLayout mDrawerLayout;
    private ImageView mNavProfileView;
    private TextView mNavUsername;
    private NavigationView mNavigationView;
    private TextView mBadgeCount;

    // Data
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 防止某些 launcher 内部 bug 导致每次返回 activity 都会重启整个应用
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                Log.w(TAG, "Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;
            }
        }

        // setTheme(R.style.AppThemeDark_NoActionBar);

        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.action_home));

        // Navigation view
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            mNavigationView.getMenu().getItem(0).setChecked(true);
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        View mNavHead = mNavigationView.getHeaderView(0);
        mNavProfileView = (ImageView) mNavHead.findViewById(R.id.nav_profile_image);
        mNavUsername = (TextView) mNavHead.findViewById(R.id.nav_user_name);

        // Get user info
        BUApplication.readConfig(mContext);
        if (BUApplication.userSession == null ||
                BUApplication.username == null || "".equals(BUApplication.username) ||
                BUApplication.password == null || "".equals(BUApplication.password)) {
            Log.i(TAG, "MainActivity >> 尚未登录，返回登录界面");
            CommonUtils.debugToast(mContext, "尚未登录，即将返回登录界面");
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            initWork();
        }
    }

    private void initWork() {
        // 检查新版本更新并安装
        if (!BUApplication.IS_GOOGLE_PLAY_EDITION) {
            Log.i(TAG, "Is not Google Play Edition, will check for update");
            CommonUtils.updateVersion(mContext, true, null);
        } else {
            Log.i(TAG, "Google Play Edition, will not check for update");
        }

        // 登记用户
        if (BUApplication.username != null && !BUApplication.username.equals("")) {
            MobclickAgent.onProfileSignIn(CommonUtils.decode(BUApplication.username));
        }

        // 配置 Navigation Layout
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        // 获取用户信息用于设置头像
        getUserInfo();

        // 小米推送
        MiPushClient.setUserAccount(mContext, CommonUtils.decode(BUApplication.username), null);

        // 定期更新用户 Session
        Intent intent = new Intent(mContext, SessionUpdateService.class);
        // startService(intent);

        // Activity content
        if (mFragment == null) {
            mFragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.flContent, mFragment);
            fragmentTransaction.commit();
        }
    }

    private void getUserInfo() {
        mNavProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.USER_NAME_TAG, BUApplication.username);
                startActivity(intent);
            }
        });

        // 从缓存中获取用户头像
        CommonUtils.getAndCacheUserInfo(mContext, BUApplication.username,
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        mNavUsername.setText(CommonUtils.decode(member.username));
                        String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                        CommonUtils.setImageView(mContext, mNavProfileView,
                                avatarURL, R.drawable.default_avatar);
                    }
                });
    }

    private Fragment mFragment;

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();

                        // Switch between fragments
                        int menuId = menuItem.getItemId();
                        Intent intent = null;
                        switch (menuId) {
                            case R.id.nav_logout:
                                mDrawerLayout.closeDrawers();
                                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                builder.setTitle(getString(R.string.title_warning))
                                        .setMessage(getString(R.string.logout_info))
                                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                MiPushClient.unsetUserAccount(mContext, CommonUtils.decode(BUApplication.username), null);
                                                BUApplication.password = null;
                                                BUApplication.setCachePassword(mContext);
                                                Intent intent = new Intent(mContext, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                                break;
                            case R.id.nav_home:
                                break;
                            case R.id.nav_forum:
                                intent = new Intent(mContext, ForumListActivity.class);
                                break;
                            case R.id.nav_setting:
                                intent = new Intent(mContext, SettingsActivity.class);
                                break;
                            case R.id.nav_focus:
                                intent = new Intent(mContext, FocusActivity.class);
                                break;
                        }

                        if (intent != null) {
                            startActivity(intent);
                        }

                        return false;
                    }
                });
    }

    private boolean doubleBackToExitPressedOnce = false;

    /**
     * 按两次按钮退出应用
     */
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(mContext, getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, "mFragment", mFragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 友盟 SDK
        if (BUApplication.uploadData)
            MobclickAgent.onResume(mContext);

        if (mBadgeCount != null) {
            getUnreadCount();
        }
    }

    private void getUnreadCount() {
        if (mBadgeCount == null) return;
        ExtraApi.getUnreadCount(mContext, BUApplication.username, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (ExtraApi.checkStatus(response)) {
                        Log.d(TAG, "getUnreadCount >> " + response.toString());
                        int unreadCount = response.getInt("data");
                        if (unreadCount == 0) {
                            mBadgeCount.setVisibility(View.INVISIBLE);
                        } else {
                            String badgeStr = unreadCount > 9 ? "9+" : "" + unreadCount;
                            mBadgeCount.setVisibility(View.VISIBLE);
                            mBadgeCount.setText(badgeStr);
                        }
                    }
                } catch (Exception e) {
                    String message = mContext.getString(R.string.error_parse_json);
                    String debugMessage = TAG + " >> " + message;
                    CommonUtils.debugToast(mContext, debugMessage);
                    Log.e(TAG, debugMessage, e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = mContext.getString(R.string.error_network);
                String debugMessage = TAG + " >> " + message;
                CommonUtils.debugToast(mContext, debugMessage);
                Log.e(TAG, debugMessage, error);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        if (BUApplication.uploadData)
            MobclickAgent.onPause(mContext);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        // Notification
        MenuItem itemNotify = menu.findItem(R.id.action_notification);
        mBadgeCount = IconFontHelper.setupMenuIconWithBadge(mContext, itemNotify, getString(R.string.bell_icon), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ActivityWithFrameLayout.class);
                intent.putExtra(ActivityWithFrameLayout.FRAGMENT_TAG, NotificationListFragment.class.getSimpleName());
                intent.putExtra(ActivityWithFrameLayout.TITLE_TAG, getString(R.string.title_activity_notification_box));

                startActivity(intent);
            }
        });

        mBadgeCount.postDelayed(new Runnable() {
            @Override
            public void run() {
                getUnreadCount();
            }
        }, BUApplication.FETCH_UNREAD_COUNT_PERIOD * 1000);

        getUnreadCount();

        // Search
        MenuItem itemSearch = menu.findItem(R.id.action_search);
        IconFontHelper.setupMenuIcon(mContext, itemSearch, getString(R.string.search_icon), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchActivity.class);
                startActivity(intent);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}