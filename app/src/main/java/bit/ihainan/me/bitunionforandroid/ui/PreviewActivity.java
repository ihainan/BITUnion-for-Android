package bit.ihainan.me.bitunionforandroid.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;
import bit.ihainan.me.bitunionforandroid.utils.ui.PicassoImageGetter;

public class PreviewActivity extends SwipeActivity {
    // TAG
    private final static String TAG = PreviewActivity.class.getSimpleName();
    public final static String MESSAGE_CONTENT = "MESSAGE_CONTENT";

    // UI References
    private TextView mMessageView, mSubjectView, mFloorView, mPostDateView;
    private Button mSubmitBtn;
    private CardView mCardView;

    // Data
    private String mSubject, mMessageContent, mMessageHtmlContent, mAction, mActionStr;
    private Long mFloor, mFid, mTid;
    private byte[] mAttachmentByteArray;

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
        mActionStr = "发表" + (mAction.endsWith(NewPostActivity.ACTION_POST) ? "回复" : "主题");
        mAttachmentByteArray = bundle.getByteArray(NewPostActivity.NEW_POST_ATTACHMENT_TAG);
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

        // CardView
        mCardView = (CardView) findViewById(R.id.card_view);

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
        mSubmitBtn.setOnClickListener(submitListener);
        if (NewPostActivity.ACTION_POST.equals(mAction)) {
            mSubmitBtn.setText("发表回复");
        } else {
            mSubmitBtn.setText("发布主题");
        }

        mSubjectView = (TextView) findViewById(R.id.thread_subject);
        if ("".equals(mSubject)) {
            mSubjectView.setVisibility(View.GONE);
        } else {
            mSubjectView.setVisibility(View.VISIBLE);
            mSubjectView.setText(mSubject);
        }
    }

    private View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
            builder.setTitle("提醒")
                    .setMessage("确认" + mActionStr + "?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            postNew();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    };

    ProgressDialog dialog;

    private void postNew() {
        try {
            if (NewPostActivity.ACTION_POST.equals(mAction)) {
                dialog = ProgressDialog.show(PreviewActivity.this, "",
                        "正在" + mActionStr, true);
                BUApi.postNewPost(this, mTid, mMessageContent, mAttachmentByteArray, listener, errorListener);
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 成功拉取数据事件监听器
    private Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>() {
        @Override
        public void onResponse(NetworkResponse response) {
            if (dialog != null && !isFinishing()) {
                dialog.dismiss();
            }
            if (response.statusCode == 200) {
                String jsonStr = new String(response.data).trim();
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    if ("success".equals(jsonObject.getString("result"))) {
                        String message = mActionStr + "成功";
                        String debugMessage = message + " - " + jsonStr;
                        CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                        Log.d(TAG, debugMessage);

                        // Finish
                        Intent resultData = new Intent();
                        setResult(Activity.RESULT_OK, resultData);
                        finish();
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + jsonObject.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                        showSnackbar(message);
                    }
                } catch (JSONException e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = message + " - " + jsonStr;
                    Log.e(TAG, debugMessage, e);
                    CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                    showSnackbar(message);
                }
            } else {
                String message = mActionStr + "失败，请重试";
                String debugMessage = message + " - " + response.statusCode;
                Log.w(TAG, debugMessage);
                CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                showSnackbar(message);
            }
        }
    };

    // 拉取数据失败事件监听器
    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (dialog != null && !isFinishing()) {
                dialog.dismiss();
            }
            String message = getString(R.string.error_network);
            String debugMessage = "PreviewActivity >> " + message;
            CommonUtils.debugToast(PreviewActivity.this, debugMessage);
            showSnackbar(message);
            Log.e(TAG, debugMessage, error);
        }
    };

    private void showSnackbar(String message) {
        Snackbar.make(mCardView, message, Snackbar.LENGTH_LONG)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postNew();
                    }
                }).show();
    }
}
