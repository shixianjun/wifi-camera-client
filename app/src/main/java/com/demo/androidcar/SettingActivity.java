package com.demo.androidcar;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * 设置
 * @author
 *
 */
public class SettingActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.setting);
    }
}
