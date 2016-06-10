package me.ihainan.bu.app.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

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

    private SwitchPreference prefNetworkType, prefSaveDataMode, prefUploadData, prefDebugMode, prefEnableNotify, prefAdvancedEditor, prefEnableSilentMode;
    private Preference prefDeviceName, prefCheckUpdate, prefDisplaySetting, prefFeedback, prefHomePageClick, prefEnableNotifyType, prefDonate;

    private void loadDefaultValue() {
        BUApplication.readConfig(this);

        /* 网络相关 */
        prefNetworkType = (SwitchPreference) findPreference("pref_out_school");
        prefNetworkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.networkType = checked ? BUApplication.NETWORK_TYPE.OUT_SCHOOL : BUApplication.NETWORK_TYPE.IN_SCHOOL;
                BUApi.currentEndPoint = checked ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;
                BUApplication.setCacheNetworkType(SettingsActivity.this);
                return true;
            }
        });
        prefNetworkType.setChecked(BUApplication.networkType == BUApplication.NETWORK_TYPE.IN_SCHOOL ? false : true);

        prefSaveDataMode = (SwitchPreference) findPreference("pref_save_data");
        prefSaveDataMode.setChecked(BUApplication.saveDataMode);
        prefSaveDataMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.saveDataMode = checked;
                BUApplication.setCacheSaveDataMode(SettingsActivity.this);
                return true;
            }
        });

        /* 开发相关 */
        prefDebugMode = (SwitchPreference) findPreference("pref_enable_dev_mode");
        prefDebugMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.debugMode = checked;
                BUApplication.setCacheDebugMode(SettingsActivity.this);
                return true;
            }
        });

        prefDebugMode.setChecked(BUApplication.debugMode);

        prefUploadData = (SwitchPreference) findPreference("pref_upload_data");
        prefUploadData.setChecked(BUApplication.uploadData);
        prefUploadData.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.uploadData = checked;
                BUApplication.setUploadData(SettingsActivity.this);
                return true;
            }
        });

        /* 关于相关 */
        prefFeedback = findPreference("pref_feedback");
        prefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

        prefDeviceName = findPreference("pref_device_name");
        prefDeviceName.setSummary(CommonUtils.getDeviceName());

        prefCheckUpdate = findPreference("pref_check_update");
        prefCheckUpdate.setSummary("当前版本：" + BuildConfig.VERSION_NAME);
        prefCheckUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ProgressDialog dialog = ProgressDialog.show(SettingsActivity.this, "",
                        "正在检查更新", true);
                dialog.show();
                CommonUtils.updateVersion(SettingsActivity.this, false, dialog);
                return false;
            }
        });

        prefDonate = findPreference("pref_donate");
        prefDonate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setTitle("捐赠")
                        .setMessage("做这个应用纯属爱好，除了付出时间和精力之外，还承担着 Google 开发者帐号、域名和服务器的费用，当然，不算太多，个人承担倒也没什么问题。\n\n总之，感谢你的喜欢，感谢你的咖啡，我今天心情应该会很好。")
                        .setPositiveButton("复制支付宝用户名", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager clipboardManager = (ClipboardManager) SettingsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("alipay", CommonUtils.decode("ihainan72@163.com"));
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(SettingsActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        });

        /* 通知相关 */
        prefEnableNotify = (SwitchPreference) findPreference("pref_enable_notify");
        prefEnableSilentMode = (SwitchPreference) findPreference("pref_night_silent_mode");
        prefEnableNotifyType = findPreference("pref_enable_notify_type");

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

        prefEnableSilentMode.setChecked(BUApplication.enableSilentMode);
        prefEnableSilentMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableSilentMode = checked;
                BUApplication.setEnableSilentMode(SettingsActivity.this);
                return true;
            }
        });

        final String[] notifyTypes = new String[]{"回复通知", "引用通知", "@ 通知", "关注通知"};
        final boolean[] notifyIsEnable = new boolean[]{BUApplication.enableReplyNotify.booleanValue(), BUApplication.enableQuoteNotify.booleanValue(),
                BUApplication.enableAtNotify.booleanValue(), BUApplication.enableFollowingNotify.booleanValue()};
        prefEnableNotifyType.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("允许通知类型")
                        .setMultiChoiceItems(notifyTypes, notifyIsEnable, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean checked) {
                                if (indexSelected == 0) {
                                    BUApplication.enableReplyNotify = checked;
                                    BUApplication.setEnableReplyNotify(SettingsActivity.this);
                                } else if (indexSelected == 1) {
                                    BUApplication.enableQuoteNotify = checked;
                                    BUApplication.setEnableQuoteNotify(SettingsActivity.this);
                                } else if (indexSelected == 2) {
                                    BUApplication.enableAtNotify = checked;
                                    BUApplication.setEnableAtNotify(SettingsActivity.this);
                                } else if (indexSelected == 3) {
                                    BUApplication.enableFollowingNotify = checked;
                                    BUApplication.setEnableFollowNotify(SettingsActivity.this);
                                }
                            }
                        }).create().show();

                return true;
            }
        });

        /* 界面相关 */
        prefDisplaySetting = findPreference("pref_post_display");
        prefDisplaySetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, PostDisplaySettingActivity.class);
                startActivity(intent);
                return false;
            }
        });

        final String[] actions = new String[]{"查看回帖楼层", "查看主楼"};
        prefHomePageClick = findPreference("pref_home_page_click");
        prefHomePageClick.setSummary(actions[BUApplication.homePageClickEventType]);
        prefHomePageClick.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setTitle("首页帖子点击事件").setSingleChoiceItems(actions, BUApplication.homePageClickEventType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BUApplication.homePageClickEventType = which;
                        BUApplication.setHomePageClickEventType(SettingsActivity.this);
                        prefHomePageClick.setSummary(actions[BUApplication.homePageClickEventType]);
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        });

        prefAdvancedEditor = (SwitchPreference) findPreference("pref_advanced_editor");
        prefAdvancedEditor.setChecked(BUApplication.enableAdvancedEditor);
        prefAdvancedEditor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableAdvancedEditor = checked;
                BUApplication.setEnableAdvancedEditor(SettingsActivity.this);
                return true;
            }
        });
    }

    private void setupNotifySettings(boolean checked) {
        prefEnableNotifyType.setEnabled(checked);
        prefEnableSilentMode.setEnabled(checked);
    }
}
