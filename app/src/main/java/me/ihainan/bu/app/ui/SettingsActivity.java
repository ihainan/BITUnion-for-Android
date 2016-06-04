package me.ihainan.bu.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.lb.material_preferences_library.PreferenceActivity;

import me.ihainan.bu.app.BuildConfig;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;

public class SettingsActivity extends PreferenceActivity {
    public final static String TAG = SettingsActivity.class.getSimpleName();

    // UI references
    private Toolbar mToolbar;

    @Override
    protected int getPreferencesXmlId() {
        return R.xml.pref_all;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

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
        setTitle(R.string.action_settings);

        // Load
        loadDefaultValue();
    }

    private SwitchPreference networkType, saveDataMode, uploadData, debugMode, prefEnableNotify;
    private Preference deviceName, checkUpdate, displaySetting, feedback;
    private CheckBoxPreference prefEnableReplyNotify, prefEnableQuoteNotify, prefEnableAtNotify, prefEnableFollowNotify;

    private void loadDefaultValue() {
        BUApplication.readConfig(this);

        networkType = (SwitchPreference) findPreference("pref_out_school");
        networkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.networkType = checked ? BUApplication.NETWORK_TYPE.OUT_SCHOOL : BUApplication.NETWORK_TYPE.IN_SCHOOL;
                BUApi.currentEndPoint = checked ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;
                BUApplication.setCacheNetworkType(SettingsActivity.this);
                return true;
            }
        });
        networkType.setChecked(BUApplication.networkType == BUApplication.NETWORK_TYPE.IN_SCHOOL ? false : true);

        saveDataMode = (SwitchPreference) findPreference("pref_save_data");
        saveDataMode.setChecked(BUApplication.saveDataMode);
        saveDataMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.saveDataMode = checked;
                BUApplication.setCacheSaveDataMode(SettingsActivity.this);
                return true;
            }
        });

        uploadData = (SwitchPreference) findPreference("pref_upload_data");
        uploadData.setChecked(BUApplication.uploadData);
        uploadData.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.uploadData = checked;
                BUApplication.setUploadData(SettingsActivity.this);
                return true;
            }
        });

        debugMode = (SwitchPreference) findPreference("pref_enable_dev_mode");
        debugMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.debugMode = checked;
                BUApplication.setCacheDebugMode(SettingsActivity.this);
                return true;
            }
        });
        debugMode.setChecked(BUApplication.debugMode);

        feedback = findPreference("pref_feedback");
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
                feedbackIntent.setType("message/rfc822");
                feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ihainan72@gmail.com"});
                feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "联盟安卓客户端意见反馈");
                feedbackIntent.putExtra(Intent.EXTRA_TEXT, "\n---\n当前版本：" + BuildConfig.VERSION_NAME);
                startActivity(Intent.createChooser(feedbackIntent, "发送邮件..."));
                return false;
            }
        });

        deviceName = findPreference("pref_device_name");
        deviceName.setSummary(CommonUtils.getDeviceName());

        checkUpdate = findPreference("pref_check_update");
        checkUpdate.setSummary("当前版本：" + BuildConfig.VERSION_NAME);
        checkUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ProgressDialog dialog = ProgressDialog.show(SettingsActivity.this, "",
                        "正在检查更新", true);
                dialog.show();
                CommonUtils.updateVersion(SettingsActivity.this, false, dialog);
                return false;
            }
        });

        displaySetting = findPreference("pref_post_display");
        displaySetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, PostDisplaySettingActivity.class);
                startActivity(intent);
                return false;
            }
        });

        prefEnableNotify = (SwitchPreference) findPreference("pref_enable_notify");
        prefEnableReplyNotify = (CheckBoxPreference) findPreference("pref_enable_reply_notify");
        prefEnableQuoteNotify = (CheckBoxPreference) findPreference("pref_enable_quote_notify");
        prefEnableAtNotify = (CheckBoxPreference) findPreference("pref_enable_at_notify");
        prefEnableFollowNotify = (CheckBoxPreference) findPreference("pref_enable_follow_notify");
        prefEnableNotify.setChecked(BUApplication.enableNotify);

        setupNotifySettings(BUApplication.enableNotify);
        prefEnableNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableNotify = checked;
                BUApplication.setEnableNotify(SettingsActivity.this);
                setupNotifySettings(checked);
                return true;
            }
        });

        prefEnableReplyNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableReplyNotify = checked;
                BUApplication.setEnableReplyNotify(SettingsActivity.this);
                return true;
            }
        });

        prefEnableQuoteNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableQuoteNotify = checked;
                BUApplication.setEnableQuoteNotify(SettingsActivity.this);
                return true;
            }
        });

        prefEnableAtNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableAtNotify = checked;
                BUApplication.setEnableAtNotify(SettingsActivity.this);
                return true;
            }
        });

        prefEnableFollowNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableFollowingNotify = checked;
                BUApplication.setEnableFollowNotify(SettingsActivity.this);
                return true;
            }
        });
    }

    private void setupNotifySettings(boolean checked) {
        prefEnableReplyNotify.setEnabled(checked);
        prefEnableQuoteNotify.setEnabled(checked);
        prefEnableAtNotify.setEnabled(checked);
        prefEnableFollowNotify.setEnabled(checked);
    }
}
