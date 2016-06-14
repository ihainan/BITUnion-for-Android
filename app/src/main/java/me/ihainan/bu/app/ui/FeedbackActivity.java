package me.ihainan.bu.app.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.Date;

import me.ihainan.bu.app.BuildConfig;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Feedback;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.ExtraApi;

public class FeedbackActivity extends SwipeActivity {
    private final static String TAG = FeedbackActivity.class.getSimpleName();

    private AutoCompleteTextView tvEmail, tvContent;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolbar.setTitle(getString(R.string.title_activity_feedback));

        // UI
        tvEmail = (AutoCompleteTextView) findViewById(R.id.email);
        tvContent = (AutoCompleteTextView) findViewById(R.id.content);

        // 自动填充
        if (BUApplication.cachedFeedbackEmail != null) {
            tvEmail.setText(BUApplication.cachedFeedbackEmail);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feedback_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                if (tvEmail.getText().toString().trim().equals("")) {
                    tvEmail.setError("不准不写邮箱地址，哼！");
                    tvContent.setError(null);
                    tvEmail.requestFocus();
                } else if (tvContent.getText().toString().trim().equals("")) {
                    tvEmail.setError(null);
                    tvContent.setError("不准不写反馈内容，哼！");
                    tvContent.requestFocus();
                } else if (!CommonUtils.isValidEmailAddress(tvEmail.getText().toString())) {
                    tvEmail.setError("填写正确的邮箱地址好么，不要闹……");
                    tvContent.setError(null);
                    tvEmail.requestFocus();
                } else {
                    BUApplication.cachedFeedbackEmail = tvEmail.getText().toString();
                    BUApplication.setCachedFeedbackEmail(this);

                    tvContent.setError(null);
                    tvEmail.setError(null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(getString(R.string.title_warning)).setMessage("确认发生反馈信息给作者？")
                            .setNegativeButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Feedback feedback = new Feedback();
                                    feedback.email = tvEmail.getText().toString();
                                    feedback.content = tvContent.getText().toString();
                                    if (BUApplication.username != null) {
                                        feedback.username = CommonUtils.decode(BUApplication.username);
                                    } else {
                                        feedback.username = "Unknown";
                                    }
                                    feedback.deviceName = CommonUtils.getDeviceName();
                                    feedback.version = BuildConfig.VERSION_NAME;
                                    feedback.application = getString(R.string.app_name);
                                    feedback.versionCode = BuildConfig.VERSION_CODE;
                                    feedback.dtCreated = CommonUtils.formatDateTime(new Date());
                                    submitFeedback(feedback);
                                    dialog.dismiss();
                                }
                            }).setPositiveButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void submitFeedback(Feedback feedback) {
        mProgressDialog = ProgressDialog.show(this, "", "正在提交反馈…", true);
        mProgressDialog.show();
        ExtraApi.addFeedback(this, feedback, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                if (ExtraApi.checkStatus(response)) {
                    String message = getString(R.string.message_success_add_feedback);
                    Toast.makeText(FeedbackActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    String message = getString(R.string.message_error_add_feedback);
                    Toast.makeText(FeedbackActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
                String message = getString(R.string.error_network);
                Toast.makeText(FeedbackActivity.this, message, Toast.LENGTH_LONG).show();

                String debugMessage = TAG + " >> " + message;
                CommonUtils.debugToast(FeedbackActivity.this, debugMessage);
                Log.e(TAG, debugMessage, error);
            }
        });
    }
}
