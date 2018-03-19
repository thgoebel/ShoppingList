/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2018.  Wolfgang Popp
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

package com.woefe.shoppinglist.activity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.dialog.DirectoryChooserDialog;
import com.woefe.shoppinglist.shoppinglist.ShoppingListService;

/**
 * @author Wolfgang Popp.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        DirectoryChooserDialog.DirectoryChooserListener {

    public static final String KEY_DIRECTORY_LOCATION = "FILE_LOCATION";
    private static final int REQUEST_CODE_EXT_STORAGE = 32537;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Preference fileLocationPref = findPreference("FILE_LOCATION");

        fileLocationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                requestExternalStoragePermission();
                DirectoryChooserDialog.show(getActivity(), SettingsFragment.this, 0);
                return true;
            }
        });


        maybeRequestExternalStoragePermission();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreferences(p);
        }
    }

    private void updatePreferences(Preference p) {
        if (KEY_DIRECTORY_LOCATION.equals(p.getKey())) {
            String path = getSharedPreferences().getString(KEY_DIRECTORY_LOCATION, "");
            p.setSummary(path);
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }


    private void requestExternalStoragePermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_DENIED) {
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_EXT_STORAGE);
        }
    }

    private void maybeRequestExternalStoragePermission() {
        if (!getSharedPreferences().getString(KEY_DIRECTORY_LOCATION, "").equals("")) {
            requestExternalStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_EXT_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ShoppingListService.ShoppingListBinder binder = ((SettingsActivity) getActivity()).getBinder();
                if (binder != null) {
                    binder.onPermissionsGranted();
                }
            }
        }
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_DIRECTORY_LOCATION)) {
            Preference p = findPreference(key);
            updatePreferences(p);
            maybeRequestExternalStoragePermission();
        }
    }

    @Override
    public void onDirectorySelected(String path) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(KEY_DIRECTORY_LOCATION, path).apply();
    }
}
