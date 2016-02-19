package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.utils.BUWebViewClient;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.HtmlUtil;

public class UserInfoActivity extends AppCompatActivity {

    // UI references
    private TextView mUserId, mStatus, mCredit, mBday, mEmail, mWebsite, mThreadCount, mPostCount, mToolbarTitle, mRegDate, mLastVisit;
    private WebView mSignature;
    private ImageView mAvatar;

    // TAGS
    public static final String TAG = UserInfoActivity.class.getSimpleName();
    public final static String USER_NAME_TAG = "USER_NAME_TAG";
    public final static String USER_ID_TAG = "USER_ID_TAG";

    private String mUsername;
    private Long mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

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
            mUsername = Global.userName;
        }

        // UI references
        mSignature = (WebView) findViewById(R.id.profile_signature);
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


        mToolbarTitle.setText(mUsername);

        getUserInfo();
    }

    private Member mMember;

    private void getUserInfo() {
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
        String avatarURL = CommonUtils.getRealImageURL(CommonUtils.decode(mMember.avatar));
        Picasso.with(this).load(avatarURL)
                .error(R.drawable.default_avatar)
                .into(mAvatar);

        mSignature.setScrollbarFadingEnabled(false);
        mSignature.setBackgroundColor(Color.TRANSPARENT);
        mSignature.setWebViewClient(new BUWebViewClient(this));
        mSignature.loadDataWithBaseURL("file:///android_asset/", new HtmlUtil(CommonUtils.decode(mMember.signature)).makeAll(), "text/html", "utf-8", null);

        mUserId.setText(CommonUtils.decode("" + mMember.uid));
        mStatus.setText(CommonUtils.decode("" + mMember.status));
        mCredit.setText(CommonUtils.decode("" + mMember.credit));
        mBday.setText(CommonUtils.decode("" + mMember.bday).equals("0000-00-00") ? "UNKNOWN" : CommonUtils.decode("" + mMember.bday));
        mEmail.setText(CommonUtils.decode(CommonUtils.decode("" + mMember.email).equals("") ? "UNKNOWN" : CommonUtils.decode("" + mMember.email)));
        mWebsite.setText(CommonUtils.decode("" + mMember.site).equals("") ? "UNKNOWN" : CommonUtils.decode("" + mMember.site));
        mThreadCount.setText(CommonUtils.decode("" + mMember.threadnum));
        mPostCount.setText(CommonUtils.decode("" + mMember.postnum));
        mRegDate.setText(CommonUtils.formatDateTimeToDay(CommonUtils.unixTimeStampToDate(mMember.regdate)));
        mLastVisit.setText(CommonUtils.formatDateTimeToDay(CommonUtils.unixTimeStampToDate(mMember.lastvisit)));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 友盟 SDK
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 友盟 SDK
        MobclickAgent.onPause(this);
    }
}
