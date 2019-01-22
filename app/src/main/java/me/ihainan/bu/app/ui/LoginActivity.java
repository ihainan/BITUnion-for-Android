package me.ihainan.bu.app.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.IOException;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Session;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * A login screen that offers login via username / password.
 */
public class LoginActivity extends AppCompatActivity {
    private final static String TAG = LoginActivity.class.getSimpleName();
    private final Context mContext = this;

    // UI references.
    private AutoCompleteTextView mUsername;
    private EditText mPassword;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (BUApplication.userSession == null ||
                BUApplication.username == null || "".equals(BUApplication.username) ||
                BUApplication.password == null || "".equals(BUApplication.password)) {
            // New user
            init();
        } else {
            // Old user
            if (BUApplication.outHost != null) {
                // Just updated to the new version
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            } else {
                // Real old user
                init();
            }
        }
    }

    private void init() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // init UI references
        mUsername = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPassword = (EditText) findViewById(R.id.password);
        SwitchCompat mSwitchCompatOutNetwork = (SwitchCompat) findViewById(R.id.switch_compat_out_network);

        // 读取配置全局配置信息
        BUApplication.readConfig(mContext);

        // 自动填充，并设置最原始的登录节点
        if (BUApplication.username != null && BUApplication.networkType != null) {
            mUsername.setText(BUApplication.username);
            // mPassword.setText(BUApplication.password);
            if (mSwitchCompatOutNetwork != null) {
                if (BUApplication.networkType == BUApplication.NETWORK_TYPE.OUT_SCHOOL)
                    mSwitchCompatOutNetwork.setChecked(true);
                else mSwitchCompatOutNetwork.setChecked(false);
            }
        }

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if ((id == R.id.login || id == EditorInfo.IME_NULL
                        || id == EditorInfo.IME_ACTION_SEARCH)
                        && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        if (mSwitchCompatOutNetwork != null) {
            mSwitchCompatOutNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // BUApi.currentEndPoint = isChecked ? null : BUApi.IN_SCHOOL_ENDPOINT;
                    BUApplication.networkType = isChecked ? BUApplication.NETWORK_TYPE.OUT_SCHOOL : BUApplication.NETWORK_TYPE.IN_SCHOOL;
                    BUApplication.setCacheNetworkType(mContext);
                }
            });
        }

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        if (mUserSignInButton != null) {
            mUserSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mUsername.setError(null);
        mPassword.setError(null);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        // Store values at the time of the login attempt.
        final String username = mUsername.getText().toString();
        final String password = mPassword.getText().toString();

        // Check whether the username is empty
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            mUsername.requestFocus();
            return;
        }

        // Check whether the password is empty
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            mPassword.requestFocus();
            return;
        }

        // showProgress(true);
        mDialog = ProgressDialog.show(mContext, "",
                "正在登录…", false);
        mDialog.show();

        // getHost before logging in if out of school
        if (BUApplication.networkType == BUApplication.NETWORK_TYPE.IN_SCHOOL) {
            // 设置 API 地址
            BUApi.currentEndPoint = BUApi.IN_SCHOOL_ENDPOINT;

            // 配置 Picasso 的磁盘缓存（配合  OKHttp）
            BUApplication.setupPicasso(getApplicationContext());

            // 检查密码
            checkPassword(username, password);
        } else {
            BUApplication.outHost = null;
            BUApplication.setConfOutHost(getApplicationContext());
            BUApi.getHost(getApplicationContext(), new BUApi.HostListener() {
                @Override
                public void onSuccess() {
                    // 配置 Picasso 的磁盘缓存（配合  OKHttp）
                    BUApplication.setupPicasso(getApplicationContext());

                    // 检查密码
                    checkPassword(username, password);
                }

                @Override
                public void onError(Throwable exception) {
                    if (isFinishing()) return;
                    if (mDialog != null) mDialog.dismiss();
                    mUsername.setError(getString(R.string.error_network));
                    mUsername.requestFocus();
                    Log.e(TAG, getString(R.string.error_network), exception);
                }
            });
        }
    }

    /**
     * 连接服务器检查用户密码是否正确，若正确则跳转到首页，否则报错
     *
     * @param userName 用户名
     * @param password 密码
     */
    private void checkPassword(final String userName, final String password) {
        BUApi.tryLogin(mContext, userName, password,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (isFinishing()) return;
                        BUApplication.username = mUsername.getText().toString();
                        BUApplication.setCacheUserName(mContext);

                        if (mDialog != null) mDialog.dismiss();
                        try {
                            if (BUApi.checkStatus(response)) {
                                BUApplication.userSession = BUApi.MAPPER.readValue(response.toString(), Session.class);
                                BUApplication.setCacheSession(mContext);

                                if (BUApplication.userSession.credit < 0) {
                                    if (mDialog != null) mDialog.dismiss();
                                    mUsername.setError(getString(R.string.error_login_negative_credit));
                                    mUsername.requestFocus();
                                } else {
                                    BUApplication.password = mPassword.getText().toString();
                                    BUApplication.setCachePassword(mContext);

                                    Intent intent = new Intent(mContext, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                if (mDialog != null) mDialog.dismiss();
                                mPassword.setError(getString(R.string.error_wrong_password));
                                mPassword.requestFocus();
                            }
                        } catch (IOException e) {
                            mUsername.setError(getString(R.string.error_parse_json));
                            mUsername.requestFocus();
                            Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isFinishing()) return;
                        if (mDialog != null) mDialog.dismiss();
                        mUsername.setError(getString(R.string.error_network));
                        mUsername.requestFocus();
                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }
}
