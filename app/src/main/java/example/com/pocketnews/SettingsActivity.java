package example.com.pocketnews;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    //custom PreferenceFragment
    public static class NewsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //add preference widgets defined in xml file
            addPreferencesFromResource(R.xml.settings_main);

            //get reference to order_by preference
            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            //update summary value for order_by
            bindPreferenceSummaryToValue(orderBy);

            //get reference to search_for preference
            Preference searchFor = findPreference(getString(R.string.settings_search_for_key));
            //update summary value for search_for
            bindPreferenceSummaryToValue(searchFor);
        }

        /**
         * displays value of preference as a summary under preference
         *
         * @param preference reference to preference widget
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            //set preference change listener to fetch the updated preference value when changed
            preference.setOnPreferenceChangeListener(this);
            //get the stored values from SharedPreferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = sharedPrefs.getString(preference.getKey(), "");
            //invoke onPreferenceChange to update the summary with the new value
            onPreferenceChange(preference, preferenceString);
        }

        /**
         * fetches and updates summary value for preferences when changes
         *
         * @param preference reference to preference widget
         * @param newValue   newly updated preference value
         * @return boolean flag indicating preference change has been handled
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //identify if the preference widget is for order_by list or search_for edittext
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                //fetch the index of the order_by value
                int prefIndex = listPreference.findIndexOfValue(newValue.toString());
                //update the summary value of preference value is changed
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                //set the summary to be the value of edittext for the EditText preference widget
                preference.setSummary(newValue.toString());
            }
            return true;
        }
    }
}
