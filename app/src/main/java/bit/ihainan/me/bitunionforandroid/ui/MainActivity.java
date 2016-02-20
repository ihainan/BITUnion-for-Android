package bit.ihainan.me.bitunionforandroid.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import java.io.UnsupportedEncodingException;

import bit.ihainan.me.bitunionforandroid.BuildConfig;
import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.ui.assist.AboutFragment;
import bit.ihainan.me.bitunionforandroid.ui.assist.ForumFragment;
import bit.ihainan.me.bitunionforandroid.ui.assist.HomePageFragment;
import bit.ihainan.me.bitunionforandroid.ui.assist.SettingFragment;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    // UI elements
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private View mNavHead;
    private ImageView mNavProfileView;
    private TextView mNavUsername;
    private ImageButton mNavExit;
    private AppBarLayout mAppBarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                Log.w(TAG, "Main Activity is not the root.  Finishing Main Activity instead of launching.");
                finish();
                return;
            }
        }

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Navigation view setup
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavHead = navigationView.getHeaderView(0);
        mNavProfileView = (ImageView) mNavHead.findViewById(R.id.nav_profile_image);
        mNavUsername = (TextView) mNavHead.findViewById(R.id.nav_user_name);
        mNavExit = (ImageButton) mNavHead.findViewById(R.id.nav_logout);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        ab.setDisplayHomeAsUpEnabled(true);

        // Get user info
        Global.readConfig(this);
        if (Global.userName == null || Global.password == null) {
            Log.i(TAG, "MainActivity >> 尚未登录，返回登录界面");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // 检查新版本更新并安装
            CommonUtils.updateVersion(this, true, null);

            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }

            try {
                getUserInfo();
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, getString(R.string.error_parse_json), e);
            }

            // Activity content
            if (mFragment == null) {
                mFragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.flContent, mFragment).commit();
            }
        }
    }

    // TODO: 退出 activity 时候保存当前的 Fragment，返回时候再恢复该 Fragment

    @Override
    protected void onStop() {
        super.onStop();
    }

    private Fragment mFragment;
    private Fragment homeFragment;

    private Fragment getHomeFragment() {
        setTitle(getString(R.string.action_home));
        if (homeFragment == null) homeFragment = new HomePageFragment();
        return homeFragment;
    }

    private Fragment forumFragment;

    private Fragment getForumFragment() {
        setTitle(getString(R.string.action_forum));
        mAppBarLayout.setExpanded(true);
        if (forumFragment == null) forumFragment = new ForumFragment();
        return forumFragment;
    }

    private Fragment settingFragment;

    public Fragment getSettingFragment() {
        setTitle(R.string.action_settings);
        if (settingFragment == null) settingFragment = new SettingFragment();
        return settingFragment;
    }

    private Fragment aboutFragment;

    private Fragment getAboutFragment() {
        setTitle(getString(R.string.action_about));
        mAppBarLayout.setExpanded(true);
        if (aboutFragment == null) aboutFragment = new AboutFragment();
        return aboutFragment;
    }


    private void getUserInfo() throws UnsupportedEncodingException {
        mNavProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.USER_NAME_TAG, Global.userName);
                startActivity(intent);
            }
        });

        // 从缓存中获取用户头像
        CommonUtils.getAndCacheUserInfo(this,
                CommonUtils.decode(Global.userSession.username),
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        mNavUsername.setText(CommonUtils.decode(member.username));
                        String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(member.avatar));
                        Picasso.with(MainActivity.this).load(avatarURL)
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .into(mNavProfileView);
                    }
                });
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        mNavExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Switch between fragments
                        int menuId = menuItem.getItemId();
                        boolean setTitle = true;
                        switch (menuId) {
                            case R.id.nav_home:
                                setTitle = true;
                                mFragment = getHomeFragment();
                                break;
                            case R.id.nav_forum:
                                setTitle = true;
                                mFragment = getForumFragment();
                                break;
                            case R.id.nav_feedback:
                                setTitle = false;
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("message/rfc822");
                                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"ihainan72@gmail.com"});
                                i.putExtra(Intent.EXTRA_SUBJECT, "联盟客户端意见反馈 - 问题概述");
                                i.putExtra(Intent.EXTRA_TEXT, "\n---\n当前版本：" + BuildConfig.VERSION_NAME);
                                startActivity(Intent.createChooser(i, "Send mail..."));
                                break;
                            case R.id.nav_about:
                                setTitle = true;
                                mFragment = getAboutFragment();
                                break;
                            case R.id.nav_setting:
                                setTitle = true;
                                mFragment = getSettingFragment();
                                break;
                            default:
                                // fragment = new ForumFragment();
                                break;
                        }

                        if (setTitle) {
                            navigationView.setCheckedItem(menuId);
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.flContent, mFragment).commit();
                            mToolbar.setTitle(menuItem.getTitle());
                            menuItem.setChecked(true);
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
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
    protected void onResume() {
        super.onResume();

        // 友盟 SDK
        if (Global.uploadData)
            MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        if (Global.uploadData)
            MobclickAgent.onPause(this);
    }
}
