package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import bit.ihainan.me.bitunionforandroid.BuildConfig;
import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * 设置页面
 */
public class SettingFragment extends PreferenceFragment {
    public final static String TAG = SettingFragment.class.getSimpleName();
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getActivity();

        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_all);

        loadDefaultValue();
    }

    private SwitchPreference networkType, saveDataMode, uploadData, debugMode;
    private Preference version, deviceName, checkUpdate;

    private void loadDefaultValue() {
        Global.readConfig(mContext);

        networkType = (SwitchPreference) findPreference("pref_out_school");
        networkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                Global.networkType = checked ? Global.NETWORK_TYPE.OUT_SCHOOL : Global.NETWORK_TYPE.IN_SCHOOL;
                BUApi.currentEndPoint = checked ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;
                Global.saveConfig(mContext);
                return true;
            }
        });
        networkType.setChecked(Global.networkType == Global.NETWORK_TYPE.IN_SCHOOL ? false : true);

        saveDataMode = (SwitchPreference) findPreference("pref_save_data");
        saveDataMode.setChecked(Global.saveDataMode);
        saveDataMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                Global.saveDataMode = checked;
                Global.saveConfig(mContext);
                return true;
            }
        });

        uploadData = (SwitchPreference) findPreference("pref_upload_data");
        uploadData.setChecked(Global.uploadData);
        uploadData.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                Global.uploadData = checked;
                Global.saveConfig(mContext);
                return true;
            }
        });

        debugMode = (SwitchPreference) findPreference("pref_enable_dev_mode");
        debugMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                Global.debugMode = checked;
                Global.saveConfig(mContext);
                return true;
            }
        });
        debugMode.setChecked(Global.debugMode);

        version = findPreference("pref_version");
        version.setSummary(BuildConfig.VERSION_NAME);

        deviceName = findPreference("pref_device_name");
        deviceName.setSummary(CommonUtils.getDeviceName());

        checkUpdate = findPreference("pref_check_update");
        checkUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ProgressDialog dialog = ProgressDialog.show(mContext, "",
                        "正在检查更新", true);
                dialog.show();
                CommonUtils.updateVersion(mContext, false, dialog);
                return false;
            }
        });
    }
}
