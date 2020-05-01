package com.kevintresuelo.lens.screens

import android.content.Context
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
        setPreferencesFromResource(R.xml.settings, rootKey)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)
        billingViewModel.proVersionLiveData.observe(this, Observer {
            it?.apply {
                hasProVersion = entitled
                enableProVersionPreference(!entitled)
            }
        })

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

        proVersionPreference = findPreference(getString(R.string.prefs_general_pro_version_key))
        proVersionPreference?.setOnPreferenceClickListener {
            if (hasProVersion) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.pro_version_entitled_title)
                    .setMessage(R.string.pro_version_entitled_description)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } else {
                ProVersionDialogFragment(billingViewModel).show(parentFragmentManager, ProVersionDialogFragment.TAG)
            }
            true
        }

        val prefsFileKey = "pro_version"
        val hasPurchasedKey = "has_purchased"

        val sharedProVersionPreferences = context?.getSharedPreferences(prefsFileKey, Context.MODE_PRIVATE)
        val hasPurchased = sharedProVersionPreferences?.getBoolean(hasPurchasedKey, false) ?: false
    }

    private fun enableProVersionPreference(state: Boolean) {
        if (!state) {
            proVersionPreference?.summary = getString(R.string.prefs_general_pro_version_entitled_summary)
        }
    }
}
