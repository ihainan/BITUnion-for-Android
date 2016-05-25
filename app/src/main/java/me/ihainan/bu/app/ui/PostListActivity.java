package me.ihainan.bu.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Post;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.ui.fragment.PostListFragment;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;

public class PostListActivity extends SwipeActivity {
    // TAGS
    private final static String TAG = PostListActivity.class.getSimpleName();
    public final static int REQUEST_NEW_REPLY = 0;

    public final static String THREAD_ID_TAG = "THREAD_ID_TAG";
    public final static String THREAD_NAME_TAG = "THREAD_NAME_TAG";
    public final static String THREAD_AUTHOR_NAME_TAG = "THREAD_AUTHOR_NAME_TAG";
    public final static String THREAD_REPLY_COUNT_TAG = "THREAD_REPLY_COUNT_TAG";
    public final static String THREAD_JUMP_FLOOR = "THREAD_JUMP_FLOOR";

    // UI references
    private ViewPager mPager;
    private FloatingActionButton mNewPostFAB;
    private SmartTabLayout mTabLayout;
    private Toolbar mToolbar;
    private RelativeLayout mErrorLayout;
    private TextView mTvErrorMessage, mTvAction;

    // Data
    private Long mTid, mReplyCount;
    private String mThreadName, mAuthorName;
    private int mTotalPage;  // 总页数
    private Integer mJumpFloor = null;    // 跳转页面
    private Integer mJumpPage = 0, mJumpPageIndex = 0;   // 需要跳转的页数和页面内位置
    private PostListPageAdapter postListPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);

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

        // Error Layout
        mErrorLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mErrorLayout.setVisibility(View.GONE);
        mTvErrorMessage = (TextView) findViewById(R.id.error_message);
        mTvAction = (TextView) findViewById(R.id.action_text);
        mTvAction.setVisibility(View.GONE);

        // FAB
        mNewPostFAB = (FloatingActionButton) findViewById(R.id.fab);
        mNewPostFAB.setVisibility(View.INVISIBLE);
        mNewPostFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostListActivity.this, BetterPostActivity.class);
                intent.putExtra(BetterPostActivity.ACTION_TAG, BetterPostActivity.ACTION_NEW_POST);
                intent.putExtra(BetterPostActivity.NEW_POST_TID_TAG, mTid);
                intent.putExtra(BetterPostActivity.NEW_POST_MAX_FLOOR_TAG, mReplyCount + 1);
                startActivityForResult(intent, REQUEST_NEW_REPLY);
            }
        });

        // Tab Layout
        PostListFragment.isSetToolbar = false;  // So ugly....
        mTabLayout = (SmartTabLayout) findViewById(R.id.tab_layout);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(0);

        // Start fetch data and fill views
        startWork();

        // Swipe
        setSwipeAnyWhere(false);
    }

    private void startWork() {
        setTitle(mThreadName != null ? Html.fromHtml(CommonUtils.decode(mThreadName)) : "Loading...");

        if (mReplyCount == null || mReplyCount == 0 || mThreadName == null || mAuthorName == null)
            getBasicData();
        else fillViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_REPLY && resultCode == RESULT_OK) {
            CommonUtils.debugToast(this, "发表回复成功");
            Intent intent = getIntent();
            intent.removeExtra(THREAD_JUMP_FLOOR);  // 直接跳到最后
            finish();
            startActivity(intent);
        }
    }

    /**
     * 获取 Bundle 数据
     */
    private void getExtra() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mTid = bundle.getLong(THREAD_ID_TAG);
            mThreadName = bundle.getString(THREAD_NAME_TAG);
            mAuthorName = bundle.getString(THREAD_AUTHOR_NAME_TAG);
            mReplyCount = bundle.getLong(THREAD_REPLY_COUNT_TAG);
            mJumpFloor = bundle.getInt(THREAD_JUMP_FLOOR, -1);
            if (mJumpFloor != -1) mJumpFloor += 1;
        }

        if (BUApplication.debugMode && mTid == null) mTid = 10588072L; // For test
    }

    private static int calculateTotalPage(long floor) {
        if (floor % BUApplication.LOADING_POSTS_COUNT == 0) {
            return (int) (floor / BUApplication.LOADING_POSTS_COUNT);
        } else {
            return (int) (floor / BUApplication.LOADING_POSTS_COUNT) + 1;
        }
    }

    /**
     * Get basic data, including thread name, author name, replies count, etc.
     */
    private void getBasicData() {
        mErrorLayout.setVisibility(View.GONE);
        BUApi.getPostReplies(this, mTid, 0, 1, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (isFinishing()) return;
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
                    } else if (response.getString("msg").equals(BUApi.FORUM_NO_PERMISSION_MSG)) {
                        String message = getString(R.string.error_forum_no_permission);
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(PostListActivity.this, debugMessage);
                        showErrorLayout(message);
                    } else if (BUApi.THREAD_NO_PERMISSION_MSG.equals(response.getString("msg"))) {
                        String message = getString(R.string.error_thread_permission_need);
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(PostListActivity.this, debugMessage);
                        showErrorLayout(message);
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + response.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(PostListActivity.this, debugMessage);
                        showErrorLayout(message);
                    }
                } catch (Exception e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = message + " - " + response;
                    Log.e(TAG, debugMessage, e);
                    CommonUtils.debugToast(PostListActivity.this, debugMessage);
                    showErrorLayout(message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                String message = getString(R.string.error_network);
                String debugMessage = " getPostReplies >> " + message;
                Log.e(TAG, debugMessage, error);
                CommonUtils.debugToast(PostListActivity.this, debugMessage);
                showErrorLayout(message);
            }
        });
    }

    private void showErrorLayout(String message) {
        mErrorLayout.setVisibility(View.VISIBLE);
        mTvErrorMessage.setText(message);
        mTvAction.setVisibility(View.VISIBLE);
        mTvAction.setText(getString(R.string.action_retry));
        mTvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWork();
            }
        });
    }

    private void fillViews() {
        setTitle(Html.fromHtml(CommonUtils.decode(mThreadName)));    // 标题
        mNewPostFAB.setVisibility(View.VISIBLE);    // FAB

        // Jump Page & Index
        // 没有指定跳转楼层，那就跳转到最后一层
        if (mJumpFloor == null || mJumpFloor == -1) {
            mJumpFloor = mReplyCount.intValue();
        }

        if (mJumpFloor != null) {
            // 要求跳转到特定楼层
            mJumpPage = calculateTotalPage(mJumpFloor) - 1;
            mJumpPageIndex = mJumpFloor - (mJumpPage) * BUApplication.LOADING_POSTS_COUNT - 1;
        }

        // TabLayout
        mTotalPage = calculateTotalPage(mReplyCount);
        postListPageAdapter = new PostListPageAdapter(getFragmentManager(), this);
        mPager.setAdapter(postListPageAdapter);
        mTabLayout.setViewPager(mPager);

        if (!(mJumpPage == 0 && mJumpPageIndex == 0)) {
            mPager.setCurrentItem(mJumpPage);
        }

        getFavoriteStatus();    // 获取收藏状态
    }

    public class PostListPageAdapter extends FragmentPagerAdapter {
        private Context context;

        public PostListPageAdapter(FragmentManager fm, Context context) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.thread_detail_tab_menu, menu);
        mFavorItem = menu.findItem(R.id.action_favor);
        return true;
    }

    private void setFavIcon(boolean isFav) {
        if (isFav) {
            Drawable newIcon = getResources().getDrawable(R.drawable.ic_star_white_24dp);
            newIcon.mutate().setColorFilter(Color.argb(255, 255, 250, 76), PorterDuff.Mode.SRC_IN);
            mFavorItem.setIcon(newIcon);
            mFavorItem.setTitle("取消收藏");
        } else {
            mFavorItem.setTitle("添加收藏");
            mFavorItem.setIcon(R.drawable.ic_star_border_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favor:
                if (favorClickable) {
                    favorClickable = !favorClickable;   // 不允许重复点击
                    hasFavor = !hasFavor;
                    if (hasFavor) {
                        // 之前是删除，想要添加
                        setFavIcon(true);
                        addFavorite();
                    } else {
                        setFavIcon(false);
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

    /* 收藏相关接口 */
    private MenuItem mFavorItem;    // 收藏按钮
    private boolean favorClickable = false; // 收藏按钮是否可以被点击
    private boolean hasFavor = false;   // 收藏按钮状态
    private Response.Listener mFavorListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            favorClickable = !favorClickable;
            try {
                if (isFinishing()) return;
                if (response.getInt("code") == 0) {
                    // 成功添加 / 删除收藏，皆大欢喜
                    String message = hasFavor ? "添加收藏成功" : "删除收藏成功";
                    BUApplication.hasUpdateFavor = true;
                    Log.d(TAG, "mFavorListener >> " + message);
                    Toast.makeText(PostListActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    // Oh no!!!
                    String message = (hasFavor ? "添加" : "删除") + "收藏失败";
                    String debugMessage = message + " - " + response.get("message");
                    if (BUApplication.debugMode) {
                        CommonUtils.debugToast(PostListActivity.this, debugMessage);
                    } else {
                        Toast.makeText(PostListActivity.this, message, Toast.LENGTH_SHORT).show();
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
                if (BUApplication.debugMode) {
                    CommonUtils.debugToast(PostListActivity.this, debugMessage);
                } else {
                    Toast.makeText(PostListActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, debugMessage, e);

                hasFavor = !hasFavor;
                setFavIcon(hasFavor);
            }
        }
    };

    private Response.ErrorListener mFavorErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isFinishing()) return;
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

    private void getFavoriteStatus() {
        ExtraApi.getFavoriteStatus(this, mTid, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (isFinishing()) return;
                    // TODO: 等待一段时间
                    if (mFavorItem == null) getFavoriteStatus();
                    else {
                        if (response.getInt("code") == 0) {
                            hasFavor = response.getBoolean("data");
                            CommonUtils.debugToast(PostListActivity.this, "hasFavor = " + hasFavor);
                            favorClickable = true;
                            setFavIcon(hasFavor);
                        } else {
                            String message = "获取收藏状态失败，失败原因 " + response.getString("message");
                            if (BUApplication.debugMode) {
                                CommonUtils.debugToast(PostListActivity.this, message);
                            }
                            Log.w(TAG, message);
                        }
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
                String debugMessage = "getFavoriteStatus >> " + message;
                CommonUtils.debugToast(PostListActivity.this, debugMessage);
                Log.e(TAG, debugMessage, error);
            }
        });
    }

    private void addFavorite() {
        ExtraApi.addFavorite(this, mTid, mThreadName, mAuthorName, mFavorListener, mFavorErrorListener);
    }

    private void delFavorite() {
        ExtraApi.delFavorite(this, mTid, mFavorListener, mFavorErrorListener);
    }
}