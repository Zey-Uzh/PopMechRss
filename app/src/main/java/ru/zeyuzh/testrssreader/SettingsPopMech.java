package ru.zeyuzh.testrssreader;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class SettingsPopMech extends PreferenceActivity {

    CheckBoxPreference notif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);

/*
        PreferenceScreen rootScreen = getPreferenceScreen();

        ListPreference list = new ListPreference(this);
        list.setKey("list");
        list.setTitle("List");
        list.setSummary("Description of list");
        list.setEntries(R.array.entries);
        list.setEntryValues(R.array.entry_values);

        rootScreen.addPreference(list);
*/

        //notif = (CheckBoxPreference) findPreference("notif");

    }
}
