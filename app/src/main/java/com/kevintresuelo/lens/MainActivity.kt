package com.kevintresuelo.lens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.kevintresuelo.lens.databinding.ActivityMainBinding
import com.kevintresuelo.novus.UpdateChecker

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var updateChecker: UpdateChecker

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
    }

    override fun onResume() {
        super.onResume()

        updateChecker.checkForStalledUpdates()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.am_f_nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UpdateChecker.assessActivityResult(requestCode, resultCode, findViewById(android.R.id.content))
    }
}
