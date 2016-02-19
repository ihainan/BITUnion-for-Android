package bit.ihainan.me.bitunionforandroid.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Session;
import bit.ihainan.me.bitunionforandroid.utils.ACache;
import bit.ihainan.me.bitunionforandroid.utils.Api;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * A login screen that offers login via username / password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    public final static String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private AutoCompleteTextView mUsername;
    private EditText mPassword;
    private SwitchCompat mSwitchCompatOutNetwork;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init UI references
        mUsername = (AutoCompleteTextView) findViewById(R.id.user_name);
        mPassword = (EditText) findViewById(R.id.password);
        mSwitchCompatOutNetwork = (SwitchCompat) findViewById(R.id.switch_compat_out_network);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // 读取配置全局配置信息
        Global.readConfig(this);
        if (Global.userName != null && Global.networkType != null) {
            mUsername.setText(Global.userName);
            mPassword.setText(Global.password);
            if (Global.networkType == Global.NETWORK_TYPE.OUT_SCHOOL)
                mSwitchCompatOutNetwork.setChecked(true);
            else mSwitchCompatOutNetwork.setChecked(false);
        } else {
            Global.currentEndPoint =
                    mSwitchCompatOutNetwork.isChecked() ?
                            Global.OUT_SCHOOL_ENDPOINT :
                            Global.IN_SCHOOL_ENDPOINT;
        }

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSwitchCompatOutNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Global.currentEndPoint = isChecked ? Global.OUT_SCHOOL_ENDPOINT : Global.IN_SCHOOL_ENDPOINT;
                Global.networkType = isChecked ? Global.NETWORK_TYPE.OUT_SCHOOL : Global.NETWORK_TYPE.IN_SCHOOL;
                Global.saveConfig(LoginActivity.this);
            }
        });

        Button mUserSignInButton = (Button) findViewById(R.id.user_sign_in_button);
        mUserSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
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

        // Store values at the time of the login attempt.
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check whether the password is empty
        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_field_required));
            focusView = mPassword;
            cancel = true;
        }

        // Check whether the username is empty
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            checkPassword(username, password);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * 连接服务器检查用户密码是否正确，若正确则跳转到首页，否则报错
     *
     * @param userName 用户名
     * @param password 密码
     */
    private void checkPassword(final String userName, final String password) {
        Api.tryLogin(this, userName, password,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Global.userName = mUsername.getText().toString();
                        showProgress(false);
                        if (Api.checkStatus(response)) {
                            try {
                                Global.userSession = Api.MAPPER.readValue(response.toString(), Session.class);
                                Global.password = mPassword.getText().toString();
                                Global.saveConfig(LoginActivity.this);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } catch (IOException e) {
                                mUsername.setError(getString(R.string.error_parse_json));
                                Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);
                                return;
                            }
                        } else {
                            Global.saveConfig(LoginActivity.this);
                            mUsername.setError(getString(R.string.error_wrong_password));
                            return;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false);
                        mUsername.setError(getString(R.string.error_network));
                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        // Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mUsername.setAdapter(adapter);
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
