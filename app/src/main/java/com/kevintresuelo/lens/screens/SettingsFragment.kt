/**
 *
 *  Lens Transposition - Flat Transposition Calculator
 *  Copyright (C) 2020  KevinT.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.html.
 *
 */

package com.kevintresuelo.lens.screens

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevintresuelo.lens.R
import com.kevintresuelo.lens.billing.payments.ProVersionDialogFragment
import com.kevintresuelo.lens.billing.viewmodels.BillingViewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private var sharedPreferences: SharedPreferences? = null
    private var hasProVersion: Boolean = false
    private var proVersionPreference: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        /**
         * Retrieves and displays set of preferences from xml resource.
         */
        setPreferencesFromResource(R.xml.settings, rootKey)

        /**
         * Retrieves the default SharedPreferences based on the context provided.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        /**
         * Checks whether the user has Pro version and applies the appropriate
         * action.
         */
        val billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)
        billingViewModel.proVersionLiveData.observe(this, Observer {
            it?.apply {
                hasProVersion = entitled
                enableProVersionPreference(!entitled)
            }
        })

        /**
         * Prevents the user from selecting themes other than the default theme
         * if the user isn't entitled to the Pro version, otherwise allow changes
         * to be saved.
         */
        val themePreference: ListPreference? = findPreference(getString(R.string.prefs_general_theme_key))
        themePreference?.setOnPreferenceChangeListener { preference, newValue ->
            if (!hasProVersion) {
                when (newValue) {
                    getString(R.string.prefs_general_theme_option_light_value), getString(R.string.prefs_general_theme_option_dark_value) -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.prefs_error_no_pro_version_title)
                            .setMessage(R.string.prefs_error_no_pro_version_message)
                            .setPositiveButton(R.string.prefs_error_no_pro_version_positive_action) { dialogInterface: DialogInterface, i: Int ->
                                ProVersionDialogFragment(billingViewModel).show(parentFragmentManager, ProVersionDialogFragment.TAG)
                            }
                            .setNegativeButton(R.string.prefs_error_no_pro_version_negative_action, null)
                            .show()
                        false
                    }
                    else -> true
                }
            } else {
                true
            }
        }

        /**
         * Displays the state of the Pro version.
         */
        proVersionPreference = findPreference(getString(R.string.prefs_general_pro_version_key))
        proVersionPreference?.setOnPreferenceClickListener {
            if (hasProVersion) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pro_version_entitled_title)
                    .setMessage(R.string.pro_version_description)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                ProVersionDialogFragment(billingViewModel).show(parentFragmentManager, ProVersionDialogFragment.TAG)
            }
            true
        }
    }

    /**
     * Changes the state of Pro version if the user is entitled to it.
     */
    private fun enableProVersionPreference(state: Boolean) {
        if (!state) {
            proVersionPreference?.summary = getString(R.string.prefs_general_pro_version_entitled_summary)
        }
    }
}
