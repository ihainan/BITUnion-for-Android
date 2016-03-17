package bit.ihainan.me.bitunionforandroid.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Post;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.ui.fragment.PostListFragment;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.network.ExtraApi;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class ThreadDetailNewActivity extends SwipeActivity {
    // TAGS
    private final static String TAG = ThreadDetailNewActivity.class.getSimpleName();
    public final static String THREAD_ID_TAG = "THREAD_ID_TAG";
    public final static String THREAD_NAME_TAG = "THREAD_NAME_TAG";
    public final static String THREAD_AUTHOR_NAME_TAG = "THREAD_AUTHOR_NAME_TAG";
    public final static String THREAD_REPLY_COUNT_TAG = "THREAD_REPLY_COUNT_TAG";
    public final static String THREAD_JUMP_FLOOR = "THREAD_JUMP_FLOOR";

    // UI references
    private ViewPager mPager;
    private SmartTabLayout mTabLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbar;

    // Data
    private Long mTid, mReplyCount;
    private String mThreadName, mAuthorName;
    private int mTotalPage;  // 总页数
    private Integer mJumpFloor = null;    // 跳转页面
    private Integer mJumpPage = 0, mJumpPageIndex = 0;   // 需要跳转的页数和页面内位置

    private void getExtra() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mTid = bundle.getLong(THREAD_ID_TAG);
            mThreadName = bundle.getString(THREAD_NAME_TAG);
            mAuthorName = bundle.getString(THREAD_AUTHOR_NAME_TAG);
            mReplyCount = bundle.getLong(THREAD_REPLY_COUNT_TAG);
            mJumpFloor = bundle.getInt(THREAD_JUMP_FLOOR, -1);
            Integer cacheViewPosition = (Integer) Global.getCache(this).getAsObject(Global.CACHE_VIEW_POSITION + "_" + mTid);
            if (mJumpFloor == -1 && cacheViewPosition != null) {
                mJumpFloor = cacheViewPosition;
            }

            if (mJumpFloor != null) mJumpFloor += 1;
        }

        if (mTid == null) mTid = 10588072L; // For test
        // Jump Page & Index
        if (mJumpFloor != null) {
            // 要求跳转到特定楼层
            mJumpPage = calculateTotalPage(mJumpFloor) - 1;  // 19 - 1, 21 - 2
            mJumpPageIndex = (int) (mJumpFloor - (mJumpPage) * Global.LOADING_POSTS_COUNT) - 1;
        }
    }

    private static int calculateTotalPage(long floor) {
        if (floor % Global.LOADING_POSTS_COUNT == 0) {
            return (int) (floor / Global.LOADING_POSTS_COUNT);
        } else {
            return (int) (floor / Global.LOADING_POSTS_COUNT) + 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_detail_new);

        // Get bundle data
        getExtra();

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbar.setTitle("Loading");

        // Tab Layout
        mTabLayout = (SmartTabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);

        if (mThreadName != null) {
            mCollapsingToolbar.setTitle(Html.fromHtml(CommonUtils.decode(mThreadName)));
        }

        if (mReplyCount == null || mReplyCount == 0 || mThreadName == null || mAuthorName == null)
            getBasicData();
        else fillViews();

        // Swipe
        setSwipeAnyWhere(false);
    }

    /**
     * Get basic data, including thread name, author name, replies count, etc.
     */
    private void getBasicData() {
        BUApi.getPostReplies(this, mTid, 0, 1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (BUApi.checkStatus(response)) {
                        JSONArray newListJson = response.getJSONArray("postlist");
                        mReplyCount = (long) response.getInt("total_reply_count") + 1;

                        List<Post> threads = BUApi.MAPPER.readValue(newListJson.toString(),
                                new TypeReference<List<Post>>() {
                                });

                        if (threads.size() > 0) {
                            Post firstReply = threads.get(0);
                            mThreadName = firstReply.subject;
                            mAuthorName = firstReply.author;
                            fillViews();
                        }
                    } else if ("thread_nopermission".equals(response.getString("msg"))) {
                        String message = getString(R.string.error_thread_permission_need);
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(ThreadDetailNewActivity.this, debugMessage);
                        Snackbar.make(mPager, message, Snackbar.LENGTH_LONG).show();
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(ThreadDetailNewActivity.this, debugMessage);
                        Snackbar.make(mPager, message, Snackbar.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = message + " - " + response;
                    Log.e(TAG, debugMessage, e);
                    CommonUtils.debugToast(ThreadDetailNewActivity.this, debugMessage);
                    Snackbar.make(mPager, message, Snackbar.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = getString(R.string.error_network);
                Log.e(TAG, message, error);
                CommonUtils.debugToast(ThreadDetailNewActivity.this, message);
            }
        });
    }

    private void fillViews() {
        getFavoriteStatus();    // 收藏装填

        mCollapsingToolbar.setTitle(Html.fromHtml(CommonUtils.decode(mThreadName)));    // 标题

        // TabLayout
        mTotalPage = calculateTotalPage(mReplyCount);
        mPager.setAdapter(new ThreadPageAdapter(getFragmentManager(), this));
        mTabLayout.setViewPager(mPager);
        mPager.setOffscreenPageLimit(1);
        if (!(mJumpPage == 0 && mJumpPageIndex == 0)) {
            mPager.setCurrentItem(mJumpPage);
        }
    }

    public boolean hasFavor = false;

    private void getFavoriteStatus() {
        ExtraApi.getFavoriteStatus(this, mTid, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("code") == 0) {
                        hasFavor = response.getBoolean("data");
                        CommonUtils.debugToast(ThreadDetailNewActivity.this, "hasFavor = " + hasFavor);
                        favorClickable = true;
                        if (hasFavor) {
                            mFavorItem.setTitle("取消收藏");
                            mFavorItem.setIcon(R.drawable.ic_favorite_white_24dp);
                        } else {
                            mFavorItem.setTitle("添加收藏");
                            mFavorItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                        }
                    } else {
                        String message = "获取收藏状态失败，失败原因 " + response.getString("message");
                        if (Global.debugMode) {
                            CommonUtils.debugToast(ThreadDetailNewActivity.this, message);
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
                Log.e(TAG, "getFavoriteStatus >> " + getString(R.string.error_network), error);
            }
        });
    }

    private MenuItem mFavorItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_detail_tab_menu, menu);
        mFavorItem = menu.findItem(R.id.action_favor);
        return true;
    }

    public class ThreadPageAdapter extends FragmentPagerAdapter {
        private Context context;

        public ThreadPageAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return mTotalPage;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putLong(THREAD_ID_TAG, mTid);
            args.putInt(PostListFragment.PAGE_POSITION_TAG, position);
            args.putString(THREAD_AUTHOR_NAME_TAG, mAuthorName);
            args.putLong(THREAD_REPLY_COUNT_TAG, mReplyCount);

            if (!(mJumpPage == 0 && mJumpPageIndex == 0) && mJumpPage == position) {
                args.putInt(PostListFragment.PAGE_INDEX_TAG, mJumpPageIndex);
            }

            Fragment fragment = new PostListFragment();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return "#" + (position + 1);
        }
    }

    private boolean favorClickable = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favor:
                if (favorClickable) {
                    favorClickable = !favorClickable;   // 不允许重复点击
                    hasFavor = !hasFavor;
                    if (hasFavor) {
                        // 之前是删除，想要添加
                        mFavorItem.setIcon(R.drawable.ic_favorite_white_24dp);
                        mFavorItem.setTitle("取消收藏");
                        addFavorite();
                    } else {
                        mFavorItem.setTitle("添加收藏");
                        mFavorItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                        delFavorite();
                    }
                }
                break;
            case R.id.jump_to_page:
                final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(this)
                        .minValue(1)
                        .maxValue(mTotalPage)
                        .defaultValue(mPager.getCurrentItem() + 1)
                        .backgroundColor(Color.WHITE)
                        .separatorColor(Color.TRANSPARENT)
                        .textColor(R.color.primary_dark)
                        .textSize(20)
                        .enableFocusability(false)
                        .wrapSelectorWheel(true)
                        .build();
                new AlertDialog.Builder(this)
                        .setTitle("跳转到页面")
                        .setView(numberPicker)
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Math.abs(numberPicker.getValue() - mPager.getCurrentItem()) <= 5)
                                    mPager.setCurrentItem(numberPicker.getValue() - 1, true);
                                else mPager.setCurrentItem(numberPicker.getValue() - 1);
                            }
                        })
                        .show();
                break;
            case R.id.action_jump_to_head:
                if (mPager.getCurrentItem() <= 5) mPager.setCurrentItem(0, true);
                else mPager.setCurrentItem(0);
                break;
            case R.id.action_jump_to_tail:
                if (mTotalPage - mPager.getCurrentItem() <= 5)
                    mPager.setCurrentItem(mTotalPage - 1, true);
                else mPager.setCurrentItem(mTotalPage - 1);
                break;
        }

        return true;
    }

    private Response.Listener mFavorListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            favorClickable = !favorClickable;
            try {
                if (response.getInt("code") == 0) {
                    // 成功添加 / 删除收藏，皆大欢喜
                    String message = hasFavor ? "添加收藏成功" : "删除收藏成功";
                    Global.hasUpdateFavor = true;
                    Log.d(TAG, "mFavorListener >> " + message);
                    Toast.makeText(ThreadDetailNewActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Oh no!!!
                    String message = (hasFavor ? "添加" : "删除") + "收藏失败";
                    String debugMessage = message + " - " + response.get("message");
                    if (Global.debugMode) {
                        CommonUtils.debugToast(ThreadDetailNewActivity.this, debugMessage);
                    } else {
                        Toast.makeText(ThreadDetailNewActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                    Log.w(TAG, debugMessage);

                    hasFavor = !hasFavor;
                    if (hasFavor) {
                        mFavorItem.setIcon(R.drawable.ic_favorite_white_24dp);
                        mFavorItem.setTitle("取消收藏");
                    } else {
                        mFavorItem.setTitle("添加收藏");
                        mFavorItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                    }
                }
            } catch (JSONException e) {
                String message = (hasFavor ? "添加" : "删除") + "收藏失败";
                String debugMessage = message + " - " + getString(R.string.error_parse_json) + " " + response;
                if (Global.debugMode) {
                    CommonUtils.debugToast(ThreadDetailNewActivity.this, debugMessage);
                } else {
                    Toast.makeText(ThreadDetailNewActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, debugMessage, e);

                hasFavor = !hasFavor;
                if (hasFavor) {
                    mFavorItem.setIcon(R.drawable.ic_favorite_white_24dp);
                    mFavorItem.setTitle("取消收藏");
                } else {
                    mFavorItem.setTitle("添加收藏");
                    mFavorItem.setIcon(R.drawable.ic_favorite_border_white_24dp);
                }
            }
        }
    };

    private Response.ErrorListener mFavorErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            String message = (hasFavor ? "取消收藏失败，" : "添加收藏失败") + "无法连接到服务器";
            Snackbar.make(mPager, message, Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasFavor) addFavorite();
                    else delFavorite();
                }
            }).show();
        }
    };

    private void addFavorite() {
        ExtraApi.addFavorite(this, mTid, mThreadName, mAuthorName, mFavorListener, mFavorErrorListener);
    }

    private void delFavorite() {
        ExtraApi.delFavorite(this, mTid, mFavorListener, mFavorErrorListener);
    }
}