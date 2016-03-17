package bit.ihainan.me.bitunionforandroid.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import javax.security.auth.Subject;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;
import bit.ihainan.me.bitunionforandroid.utils.ui.PicassoImageGetter;

public class PreviewActivity extends SwipeActivity {
    // TAG
    private final static String TAG = PreviewActivity.class.getSimpleName();
    public final static String MESSAGE_CONTENT = "MESSAGE_CONTENT";

    // UI References
    private TextView mMessageView, mSubjectView, mFloorView, mPostDateView;
    private Button mSubmitBtn;

    // Data
    private String mSubject, mMessageContent, mMessageHtmlContent, mAction;
    private Long mFloor, mFid, mTid;

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        mMessageContent = bundle.getString(MESSAGE_CONTENT);
        if (mMessageContent != null) {
            // mMessageContent += "\n\n\n[b]发自 " + CommonUtils.getDeviceName() + " @BITUnion for Android[/b]";
            mMessageHtmlContent = new HtmlUtil(HtmlUtil.ubbToHtml(mMessageContent)).makeAll();
        }

        mFloor = bundle.getLong(NewPostActivity.NEW_POST_FLOOR_TAG);
        mAction = bundle.getString(NewPostActivity.NEW_POST_ACTION_TAG);
        mSubject = bundle.getString(NewPostActivity.NEW_POST_SUBJECT_TAG, "").trim();
        mTid = bundle.getLong(NewPostActivity.NEW_POST_TID_TAG);
        mFid = bundle.getLong(NewPostActivity.NEW_POST_FID_TAG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getExtra();
        setSwipeAnyWhere(false);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("预览");

        // Message
        mMessageView = (TextView) findViewById(R.id.thread_message);
        mMessageView.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
        mMessageView.setLineSpacing(6, 1.2f);
        SpannableString spannableString = new SpannableString(
                Html.fromHtml(
                        mMessageHtmlContent,
                        new PicassoImageGetter(this, mMessageView),
                        null));
        CustomSpan.setUpAllSpans(this, spannableString);
        mMessageView.setText(spannableString);

        // Floor
        mFloorView = (TextView) findViewById(R.id.post_floor);
        mFloorView.setText("#" + mFloor);

        // Date
        mPostDateView = (TextView) findViewById(R.id.post_date);
        mPostDateView.setText(CommonUtils.formatDateTime(new Date()));

        // Submit
        mSubmitBtn = (Button) findViewById(R.id.submit);
        if (NewPostActivity.ACTION_POST.equals(mSubject)) {
            mSubmitBtn.setText("发表回复");
        } else {
            mSubmitBtn.setText("发布主题");
        }
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PreviewActivity.this, mMessageHtmlContent, Toast.LENGTH_LONG).show();
            }
        });

        mSubjectView = (TextView) findViewById(R.id.thread_subject);
        if ("".equals(mSubject)) {
            mSubjectView.setVisibility(View.GONE);
        } else {
            mSubjectView.setVisibility(View.VISIBLE);
            mSubjectView.setText(mSubject);
        }

    }

}
