package com.cse5236Group11.wheretoeat;

import com.cse5236Group11.wheretoeat.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Setting extends PreferenceActivity {
	
    public static String KEY_CURRENT_LOCATION = "PrefCurrentLocation"; // id of the place
    public static String KEY_STATE = "PrefState"; // name of the place
    public static String KEY_CITY = "PrefCity"; // address of the place
    public static String KEY_STREET = "PrefStreetAddress"; // address of the place
    
    private SharedPreferences SP;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		// if current location is chosen, disable user to edit own address
		OnPreferenceClickListener pref1_click = new OnPreferenceClickListener() {
		    public boolean onPreferenceClick(Preference preference) {
		    	SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		    	
                boolean PrefCurrentLocation = SP.getBoolean("KEY_CURRENT_LOCATION",true);
	            getPreferenceScreen().findPreference("KEY_STATE").setEnabled(!PrefCurrentLocation);
	            getPreferenceScreen().findPreference("KEY_CITY").setEnabled(!PrefCurrentLocation);
	            getPreferenceScreen().findPreference("KEY_STREET").setEnabled(!PrefCurrentLocation);
				return true;
		    }
		};
		
		getPreferenceScreen().findPreference("KEY_CURRENT_LOCATION").setOnPreferenceClickListener(pref1_click);
	}
	

}