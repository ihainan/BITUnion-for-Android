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

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;
import bit.ihainan.me.bitunionforandroid.utils.ui.PicassoImageGetter;

public class PreviewActivity extends SwipeActivity {
    // TAG
    private final static String TAG = PreviewActivity.class.getSimpleName();
    public final static String MESSAGE_CONTENT = "MESSAGE_CONTENT";

    // UI References
    private TextView mMessage, mSubject;
    private Button mSubmit;

    // Data
    private String mMessageContent, mMessageHtmlContent;

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        mMessageContent = bundle.getString(MESSAGE_CONTENT);
        if (mMessageContent != null) {
            // mMessageContent += "\n\n\n[b]发自 " + CommonUtils.getDeviceName() + " @BITUnion for Android[/b]";
            mMessageHtmlContent = new HtmlUtil(HtmlUtil.ubbToHtml(mMessageContent)).makeAll();
        }
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mMessage = (TextView) findViewById(R.id.thread_message);
        mMessage.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
        mMessage.setLineSpacing(6, 1.2f);
        SpannableString spannableString = new SpannableString(
                Html.fromHtml(
                        mMessageHtmlContent,
                        new PicassoImageGetter(this, mMessage),
                        null));
        CustomSpan.setUpAllSpans(this, spannableString);
        mMessage.setText(spannableString);


        mSubmit = (Button) findViewById(R.id.submit);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PreviewActivity.this, mMessageHtmlContent, Toast.LENGTH_LONG).show();
            }
        });

        mSubject = (TextView) findViewById(R.id.thread_subject);
        mSubject.setVisibility(View.GONE);
    }

}
