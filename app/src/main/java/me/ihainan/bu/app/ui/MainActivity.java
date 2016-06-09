package me.ihainan.bu.app.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.xiaomi.mipush.sdk.MiPushClient;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.fragment.HomeFragment;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.SessionUpdateService;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    // UI elements
    private DrawerLayout mDrawerLayout;
    private View mNavHead;
    private Toolbar mToolbar;
    private ImageView mNavProfileView;
    private TextView mNavUsername;
    private ImageButton mNavExit;
    private NavigationView mNavigationView;

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

        setContentView(R.layout.activity_main);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.action_home));

        // Navigation view
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavHead = mNavigationView.getHeaderView(0);
        mNavProfileView = (ImageView) mNavHead.findViewById(R.id.nav_profile_image);
        mNavUsername = (TextView) mNavHead.findViewById(R.id.nav_user_name);
        mNavExit = (ImageButton) mNavHead.findViewById(R.id.nav_logout);

        // Get user info
        BUApplication.readConfig(this);
        if (BUApplication.userSession == null || "".equals(BUApplication.username) || "".equals(BUApplication.password) || BUApplication.username == null || BUApplication.password == null) {
            Log.i(TAG, "MainActivity >> 尚未登录，返回登录界面");
            CommonUtils.debugToast(this, "尚未登录，即将返回登录界面");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else initWork();
    }

    private void initWork() {
        // 检查新版本更新并安装
        CommonUtils.updateVersion(this, true, null);

        // 配置 Navigation Layout
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

        // 用户用户信息
        getUserInfo();

        // 小米推送
        MiPushClient.setUserAccount(this, CommonUtils.decode(BUApplication.username), null);

        // 定期更新用户 Session
        Intent intent = new Intent(this, SessionUpdateService.class);
        startService(intent);

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
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.USER_NAME_TAG, BUApplication.username);
                startActivity(intent);
            }
        });

        // 从缓存中获取用户头像
        CommonUtils.getAndCacheUserInfo(this, BUApplication.username,
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        mNavUsername.setText(CommonUtils.decode(member.username));
                        String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                        CommonUtils.setImageView(MainActivity.this, mNavProfileView,
                                avatarURL, R.drawable.default_avatar);
                    }
                });
    }

    private Fragment mFragment;

    private void setupDrawerContent(final NavigationView navigationView) {

        mNavExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("")
                        .setMessage(getString(R.string.logout_info))
                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MiPushClient.unsetUserAccount(MainActivity.this, CommonUtils.decode(BUApplication.username), null);
                                BUApplication.password = null;
                                BUApplication.setCachePassword(MainActivity.this);
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("")
                                        .setMessage(getString(R.string.logout_info))
                                        .setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                MiPushClient.unsetUserAccount(MainActivity.this, CommonUtils.decode(BUApplication.username), null);
                                                BUApplication.password = null;
                                                BUApplication.setCachePassword(MainActivity.this);
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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
                                intent = new Intent(MainActivity.this, ForumListActivity.class);
                                break;
                            case R.id.nav_setting:
                                intent = new Intent(MainActivity.this, SettingsActivity.class);
                                break;
                            case R.id.nav_focus:
                                intent = new Intent(MainActivity.this, FocusActivity.class);
                                break;
                        }

                        if (intent != null) {
                            startActivity(intent);
                        }

                        navigationView.setCheckedItem(menuId);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.flContent, mFragment).commit();
                        menuItem.setChecked(true);

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

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show();

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
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
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
            MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        if (BUApplication.uploadData)
            MobclickAgent.onPause(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }
}