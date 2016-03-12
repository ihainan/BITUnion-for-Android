package bit.ihainan.me.bitunionforandroid.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;


import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.ui.PicassoImageGetter;

public class OldUserInfoActivity extends SwipeActivity {

    // UI references
    private TextView mUserId, mStatus, mCredit, mBday, mEmail, mWebsite, mThreadCount, mPostCount, mToolbarTitle, mRegDate, mLastVisit;
    private TextView mSignature;
    private ImageView mAvatar;
    private LinearLayout mContactLayout, mSignatureLayout, mBdayLayout;
    private RelativeLayout mEmailLayout, mWebsiteLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private NestedScrollView mProfileLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // TAGS
    public static final String TAG = OldUserInfoActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";

    private String mUsername;
    private Long mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_user_info);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get USER_NAME or USER ID
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mUsername = bundle.getString(USER_NAME_TAG);
            mUid = bundle.getLong(USER_ID_TAG);
        }

        if (mUsername == null && mUid == null) {
            mUsername = Global.username;
        }

        // UI references
        mSignature = (TextView) findViewById(R.id.profile_signature);
        mAvatar = (ImageView) findViewById(R.id.profile_image);
        mUserId = (TextView) findViewById(R.id.profile_user_id);
        mStatus = (TextView) findViewById(R.id.profile_status);
        mCredit = (TextView) findViewById(R.id.profile_credit);
        mBday = (TextView) findViewById(R.id.profile_bday);
        mEmail = (TextView) findViewById(R.id.profile_email);
        mWebsite = (TextView) findViewById(R.id.profile_web);
        mThreadCount = (TextView) findViewById(R.id.profile_thread_sum);
        mPostCount = (TextView) findViewById(R.id.profile_post_sum);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mRegDate = (TextView) findViewById(R.id.profile_regdate);
        mLastVisit = (TextView) findViewById(R.id.profile_lastvisit);
        mContactLayout = (LinearLayout) findViewById(R.id.profile_contact_layout);
        mEmailLayout = (RelativeLayout) findViewById(R.id.profile_email_layout);
        mWebsiteLayout = (RelativeLayout) findViewById(R.id.profile_website_layout);

        mSignatureLayout = (LinearLayout) findViewById(R.id.profile_signature_layout);
        mBdayLayout = (LinearLayout) findViewById(R.id.profile_bday_layout);

        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        mToolbarTitle.setText(CommonUtils.decode(mUsername));
        mCollapsingToolbar.setTitle(CommonUtils.decode(mUsername));

        mProfileLayout = (NestedScrollView) findViewById(R.id.profile_layout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        setupSwipeRefreshLayout();

        setSwipeAnyWhere(false);
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载数据
                Global.getCache(OldUserInfoActivity.this)
                        .remove(Global.CACHE_USER_INFO + mUsername);
                getUserInfo();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 第一次加载数据
                mProfileLayout.setVisibility(View.INVISIBLE);
                getUserInfo();
            }
        });
    }

    private Member mMember;

    private void getUserInfo() {
        mSwipeRefreshLayout.setRefreshing(true);

        mMember = (Member) Global.getCache(this)
                .getAsObject(Global.CACHE_USER_INFO + mUsername);

        // 从缓存中获取用户头像
        CommonUtils.getAndCacheUserInfo(this,
                CommonUtils.decode(mUsername), new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        mMember = member;
                        fillViews();
                    }
                });
    }

    private void fillViews() {
        // 头像
        final String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(mMember.avatar));
        CommonUtils.setImageView(OldUserInfoActivity.this, mAvatar,
                avatarURL, R.drawable.default_avatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(avatarURL));
                startActivity(intent);
            }
        });

        // 签名
        if (mMember.signature == null
                || "".equals(mMember.signature)) mSignatureLayout.setVisibility(View.GONE);
        else {
            mSignature.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
            mSignature.setLineSpacing(6, 1.2f);
            SpannableString spannableString = new SpannableString(
                    Html.fromHtml(
                            CommonUtils.decode(mMember.signature),
                            new PicassoImageGetter(this, mSignature),
                            null));
            CustomSpan.replaceQuoteSpans(this, spannableString);
            CustomSpan.replaceClickableSpan(this, spannableString);
            mSignature.setText(spannableString);
        }

        mUserId.setText(CommonUtils.decode("" + mMember.uid));
        mStatus.setText(CommonUtils.decode("" + mMember.status));
        mCredit.setText(CommonUtils.decode("" + mMember.credit));

        // 生日
        if (mMember.bday == null || "".equals(mMember.bday)
                || CommonUtils.decode(mMember.bday).equals("0000-00-00")) {
            mBdayLayout.setVisibility(View.INVISIBLE);
        } else mBday.setText(CommonUtils.decode(mMember.bday));

        // 联系方式
        if ((mMember.email == null || "".equals(mMember.email)) && (mMember.site == null || "".equals(mMember.site))) {
            mContactLayout.setVisibility(View.GONE);
        } else {
            if (mMember.email == null || "".equals(mMember.email))
                mEmailLayout.setVisibility(View.GONE);
            else mEmail.setText(CommonUtils.decode(mMember.email));

            if (mMember.site == null || "".equals(mMember.site))
                mWebsiteLayout.setVisibility(View.GONE);
            else mWebsite.setText(CommonUtils.decode("" + mMember.site));
        }

        mThreadCount.setText(CommonUtils.decode("" + mMember.threadnum));
        mPostCount.setText(CommonUtils.decode("" + mMember.postnum));
        mRegDate.setText(CommonUtils.formatDateTimeToDay(CommonUtils.unixTimeStampToDate(mMember.regdate)));
        mLastVisit.setText(CommonUtils.formatDateTimeToDay(CommonUtils.unixTimeStampToDate(mMember.lastvisit)));

        registerForContextMenu(mEmailLayout);
        registerForContextMenu(mWebsiteLayout);

        addOnClickListener();

        mProfileLayout.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * 添加弹出菜单事件
     */
    private void addOnClickListener() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.showContextMenu();
            }
        };

        mEmailLayout.setOnClickListener(onClickListener);
        mWebsiteLayout.setOnClickListener(onClickListener);
    }

    // 菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // super.onCreateContextMenu(menu, v, menuInfo);
        if (v.equals(mEmailLayout)) {
            menu.add(0, 1, Menu.NONE, "发送邮件…");
            menu.add(0, 2, Menu.NONE, "复制地址");
        } else if (v.equals(mWebsiteLayout)) {
            menu.add(1, 1, Menu.NONE, "打开链接…");
            menu.add(1, 2, Menu.NONE, "复制地址");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent i;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (item.getGroupId() == 0) {
            // Email
            switch (item.getItemId()) {
                case 1:
                    i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{CommonUtils.decode(mMember.email)});
                    startActivity(Intent.createChooser(i, "Send mail..."));
                    break;
                case 2:
                    ClipData clipData = ClipData.newPlainText("Email", CommonUtils.decode(mMember.email));
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            switch (item.getItemId()) {
                case 1:
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(CommonUtils.decode(mMember.site)));
                    startActivity(i);
                    break;
                case 2:
                    ClipData clipData = ClipData.newPlainText("Website", CommonUtils.decode(mMember.site));
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        return true;
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
