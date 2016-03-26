package me.ihainan.bu.app.ui.fragment;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.SpannableString;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.FullscreenPhotoViewerActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.assist.CustomSpan;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.ui.PicassoImageGetter;

/**
 * 用户基本资料
 */
public class BasicInfoFragment extends Fragment {
    private final static String TAG = BasicInfoFragment.class.getSimpleName();
    private Context mContext;

    // UI references
    private View mRootView;
    private TextView mUserId, mStatus, mCredit, mBday, mEmail, mWebsite, mThreadCount, mPostCount, mRegDate, mLastVisit;
    private TextView mSignature;
    private ImageView mAvatar;
    private LinearLayout mContactLayout, mSignatureLayout, mBdayLayout;
    private RelativeLayout mEmailLayout, mWebsiteLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private NestedScrollView mProfileLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Data
    private String mUsername;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_basic_info, container, false);

            // Username
            mUsername = getArguments().getString(ProfileActivity.USER_NAME_TAG);
            if (mUsername == null) mUsername = Global.userSession.username;

            // UI references
            mSignature = (TextView) mRootView.findViewById(R.id.profile_signature);
            mAvatar = (ImageView) getActivity().findViewById(R.id.profile_image);
            mUserId = (TextView) mRootView.findViewById(R.id.profile_user_id);
            mStatus = (TextView) mRootView.findViewById(R.id.profile_status);
            mCredit = (TextView) mRootView.findViewById(R.id.profile_credit);
            mBday = (TextView) mRootView.findViewById(R.id.profile_bday);
            mEmail = (TextView) mRootView.findViewById(R.id.profile_email);
            mWebsite = (TextView) mRootView.findViewById(R.id.profile_web);
            mThreadCount = (TextView) mRootView.findViewById(R.id.profile_thread_sum);
            mPostCount = (TextView) mRootView.findViewById(R.id.profile_post_sum);
            mRegDate = (TextView) mRootView.findViewById(R.id.profile_regdate);
            mLastVisit = (TextView) mRootView.findViewById(R.id.profile_lastvisit);
            mContactLayout = (LinearLayout) mRootView.findViewById(R.id.profile_contact_layout);
            mEmailLayout = (RelativeLayout) mRootView.findViewById(R.id.profile_email_layout);
            mWebsiteLayout = (RelativeLayout) mRootView.findViewById(R.id.profile_website_layout);

            mSignatureLayout = (LinearLayout) mRootView.findViewById(R.id.profile_signature_layout);
            mBdayLayout = (LinearLayout) mRootView.findViewById(R.id.profile_bday_layout);

            mProfileLayout = (NestedScrollView) mRootView.findViewById(R.id.profile_layout);
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);

            setupSwipeRefreshLayout();
        }

        return mRootView;
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载数据
                Global.getCache(mContext)
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

        mMember = (Member) Global.getCache(mContext)
                .getAsObject(Global.CACHE_USER_INFO + mUsername);

        // 从缓存中获取用户头像
        CommonUtils.getAndCacheUserInfo(mContext, mUsername,
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        mMember = member;
                        fillViews();
                    }
                });
    }

    private void fillViews() {
        // 头像
        final String avatarURL = CommonUtils.getRealImageURL(mMember.avatar);
        CommonUtils.setImageView(mContext, mAvatar,
                avatarURL, R.drawable.default_avatar);
        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FullscreenPhotoViewerActivity.class);
                intent.putExtra(FullscreenPhotoViewerActivity.IMAGE_URL_TAG, CommonUtils.getRealImageURL(avatarURL));
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
                            new PicassoImageGetter(mContext, mSignature),
                            null));
            CustomSpan.replaceQuoteSpans(mContext, spannableString);
            CustomSpan.replaceClickableSpan(mContext, spannableString);
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
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
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
                    Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        return true;
    }
}
