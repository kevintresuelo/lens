package com.kevintresuelo.lens

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.kevintresuelo.lens.databinding.ActivityMainBinding
import com.kevintresuelo.novus.UpdateChecker

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var updateChecker: UpdateChecker
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        /**
         * Initializes the Toolbar to hook with Activity actions
         */
        val toolbar: Toolbar = findViewById(R.id.am_tb_toolbar)
        setSupportActionBar(toolbar)

        /**
         * Initializes the Navigation Controller with the Toolbar
         */
        val navController = findNavController(R.id.am_f_nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        updateChecker = UpdateChecker(this, findViewById(android.R.id.content), false)
        updateChecker.checkForUpdates()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        when (sharedPreferences.getString(getString(R.string.prefs_general_theme_key), getString(R.string.prefs_general_theme_default_value))) {
            getString(R.string.prefs_general_theme_option_dark_value) -> {
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
            }
            getString(R.string.prefs_general_theme_option_light_value) -> {
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
            }
            else -> {
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateChecker.checkForStalledUpdates()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.am_f_nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UpdateChecker.assessActivityResult(requestCode, resultCode, findViewById(android.R.id.content))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            getString(R.string.prefs_general_theme_key) -> recreate()
        }
    }
}
