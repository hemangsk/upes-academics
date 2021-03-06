/*
 * Copyright (c) 2013-2016 Shaleen Jain <shaleen.jain95@gmail.com>
 *
 * This file is part of UPES Academics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shalzz.attendance.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Severity;
import com.shalzz.attendance.R;
import com.shalzz.attendance.ui.base.BaseActivity;
import com.shalzz.attendance.ui.login.LoginActivity;
import com.shalzz.attendance.ui.main.MainActivity;
import com.shalzz.attendance.wrapper.MyPreferencesManager;

import javax.inject.Inject;

public class SplashActivity extends BaseActivity {

    @Inject
    MyPreferencesManager preferencesManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
		Bugsnag.setContext("SplashActivity");

                // Set all default values once for this application
        try {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_proxy, false);
        } catch (ClassCastException e) {
	        Bugsnag.notify(e, Severity.INFO);
            preferencesManager.removeDefaultSharedPreferences();
            PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
            PreferenceManager.setDefaultValues(this, R.xml.pref_proxy, true);
        }

		boolean loggedin = preferencesManager.getLoginStatus();

        Intent intent;
		if(!loggedin)
            intent = new Intent(SplashActivity.this, LoginActivity.class);
		else
            intent = new Intent(SplashActivity.this, MainActivity.class);

        startActivity(intent);
		finish();
	}
}
