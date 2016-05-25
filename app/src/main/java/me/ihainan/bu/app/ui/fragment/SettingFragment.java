package me.ihainan.bu.app.ui.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.ihainan.bu.app.BuildConfig;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 设置页面
 */
public class SettingFragment extends PreferenceFragment {
    public final static String TAG = SettingFragment.class.getSimpleName();
    // UI references
    private Context mContext;
    private View mRootView;
    private Toolbar mToolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragmemt_setting_main, container, false);

            // UI references
            mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_all);
            loadDefaultValue();
        }

        return mRootView;
    }

    private SwitchPreference networkType, saveDataMode, uploadData, debugMode;
    private Preference version, deviceName, checkUpdate;

    private void loadDefaultValue() {
        BUApplication.readConfig(mContext);

        networkType = (SwitchPreference) findPreference("pref_out_school");
        networkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.networkType = checked ? BUApplication.NETWORK_TYPE.OUT_SCHOOL : BUApplication.NETWORK_TYPE.IN_SCHOOL;
                BUApi.currentEndPoint = checked ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;
                BUApplication.saveConfig(mContext);
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
                BUApplication.saveConfig(mContext);
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
                BUApplication.saveConfig(mContext);
                return true;
            }
        });

        debugMode = (SwitchPreference) findPreference("pref_enable_dev_mode");
        debugMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.debugMode = checked;
                BUApplication.saveConfig(mContext);
                return true;
            }
        });
        debugMode.setChecked(BUApplication.debugMode);

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
