package com.kamron.pogoiv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.kamron.pogoiv.updater.AppUpdate;
import com.kamron.pogoiv.updater.AppUpdateUtil;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_page_title));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        mContext = SettingsActivity.this;
        LocalBroadcastManager.getInstance(this).registerReceiver(showUpdateDialog, new IntentFilter(MainActivity.ACTION_SHOW_UPDATE_DIALOG));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(showUpdateDialog);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        GoIVSettings.saveSettings(this, new GoIVSettings(
                sharedPreferences.getBoolean(GoIVSettings.LAUNCH_POKEMON_GO, true),
                sharedPreferences.getBoolean(GoIVSettings.SHOW_CONFIRMATION_DIALOG, true),
                sharedPreferences.getBoolean(GoIVSettings.MANUAL_SCREENSHOT_MODE, false),
                sharedPreferences.getBoolean(GoIVSettings.DELETE_SCREENSHOTS, true),
                sharedPreferences.getBoolean(GoIVSettings.COPY_TO_CLIPBOARD, false),
                sharedPreferences.getBoolean(GoIVSettings.SEND_CRASH_REPORTS, true),
                sharedPreferences.getBoolean(GoIVSettings.AUTO_UPDATE_ENABLED, true)));
    }

    private final BroadcastReceiver showUpdateDialog = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AppUpdate update = intent.getParcelableExtra("update");
            AlertDialog updateDialog = AppUpdateUtil.getAppUpdateDialog(mContext, update);
            updateDialog.show();
        }
    };

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            PreferenceScreen preferenceScreen = getPreferenceScreen();

            if (BuildConfig.isInternetAvailable) {
                Preference checkForUpdatePreference = getPreferenceManager().findPreference("checkForUpdate");
                checkForUpdatePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Toast.makeText(getActivity(), "Checking for update... ", Toast.LENGTH_SHORT).show();
                        AppUpdateUtil.checkForUpdate(getActivity());
                        return true;
                    }
                });
            } else {
                //Hide update and crash report related settings
                Preference crashReportsPreference = getPreferenceManager().findPreference(GoIVSettings.SEND_CRASH_REPORTS);
                Preference autoUpdatePreference = getPreferenceManager().findPreference(GoIVSettings.AUTO_UPDATE_ENABLED);
                Preference checkForUpdatePreference = getPreferenceManager().findPreference("checkForUpdate");


                preferenceScreen.removePreference(crashReportsPreference);
                preferenceScreen.removePreference(autoUpdatePreference);
                preferenceScreen.removePreference(checkForUpdatePreference);
            }

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                SwitchPreference manualScreenshotModePreference = (SwitchPreference) getPreferenceManager().findPreference(GoIVSettings.MANUAL_SCREENSHOT_MODE);
                manualScreenshotModePreference.setDefaultValue(true);
                manualScreenshotModePreference.setChecked(true);
                manualScreenshotModePreference.setEnabled(false);
            }
        }
    }
}
