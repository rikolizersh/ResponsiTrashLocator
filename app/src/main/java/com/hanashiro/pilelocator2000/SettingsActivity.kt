package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.hanashiro.pilelocator2000.BuildConfig

/**
 * Activity for displaying and managing app settings.
 */
class SettingsActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created. Initializes the UI, sets up listeners,
     * and configures the initial state of the settings.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set the background image based on the current theme (dark or light mode).
        val backgroundImage: ImageView = findViewById(R.id.background_image)
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            backgroundImage.setImageResource(R.drawable.nightmodetopo)
        } else {
            backgroundImage.setImageResource(R.drawable.lightmodetopo)
        }
        backgroundImage.alpha = 0.15f

        // Set up the toolbar with a title and back button.
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = "Settings"

        // Initialize the login-related UI.
        updateLoginSettingUI()

        // Set up the theme switch and its listener.
        val themeSwitch: SwitchMaterial = findViewById(R.id.theme_switch)
        val themePreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        themeSwitch.isChecked = themePreferences.getBoolean("is_dark_mode", false)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the theme preference and apply the new theme.
            val editor = themePreferences.edit()
            editor.putBoolean("is_dark_mode", isChecked)
            editor.apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            recreateWithAnimation()
        }

        // Set up the permissions setting link.
        val permissionsSetting: RelativeLayout = findViewById(R.id.permissions_setting)
        permissionsSetting.setOnClickListener {
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }

        // Set up the about setting link.
        val aboutSetting: RelativeLayout = findViewById(R.id.about_setting)
        aboutSetting.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        // Set up the clear report data setting.
        val clearReportDataSetting: RelativeLayout = findViewById(R.id.clear_report_data_setting)
        clearReportDataSetting.setOnClickListener {
            showClearReportDataDialog()
        }

        // Show debug options only in debug builds.
        val debugOptions: LinearLayout = findViewById(R.id.debug_options)
        if (BuildConfig.DEBUG) {
            debugOptions.visibility = View.VISIBLE
        }

        val debugMenuSetting: RelativeLayout = findViewById(R.id.debug_menu_setting)
        debugMenuSetting.setOnClickListener {
            val intent = Intent(this, DebugActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Called when the activity is resumed. Refreshes the login-dependent UI.
     */
    override fun onResume() {
        super.onResume()
        updateLoginSettingUI()
    }

    /**
     * Updates the UI related to the user's login status.
     */
    private fun updateLoginSettingUI() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("logged_in_user", null)
        val imageUriString = sharedPreferences.getString("profile_picture_uri", null)

        val loginSetting: CardView = findViewById(R.id.login_setting)
        val loginTitle: TextView = loginSetting.findViewById(R.id.login_title)
        val loginSubtitle: TextView = loginSetting.findViewById(R.id.login_subtitle)
        val profileImage: ImageView = loginSetting.findViewById(R.id.profile_image)

        setProfileImageWithFallback(profileImage, imageUriString, sharedPreferences)

        if (loggedInUser != null) {
            // If the user is logged in, show their username and a link to their profile.
            loginTitle.text = "Welcome, $loggedInUser"
            loginSubtitle.visibility = View.GONE
            loginSetting.setOnClickListener {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        } else {
            // If the user is not logged in, show a link to the login page.
            loginTitle.text = "Login"
            loginSubtitle.visibility = View.VISIBLE
            loginSetting.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * Sets the profile image, with a fallback to a default placeholder if the URI is invalid.
     */
    private fun setProfileImageWithFallback(imageView: ImageView, uriString: String?, sharedPreferences: SharedPreferences) {
        val isLoggedIn = sharedPreferences.getString("logged_in_user", null) != null
        val placeholder = if (isLoggedIn) R.drawable.prometheus else R.drawable.stelle

        if (uriString != null) {
            try {
                imageView.setImageURI(Uri.parse(uriString))
            } catch (e: SecurityException) {
                // If the URI is invalid, remove it from preferences and use the placeholder.
                sharedPreferences.edit().remove("profile_picture_uri").apply()
                imageView.setImageResource(placeholder)
            }
        } else {
            imageView.setImageResource(placeholder)
        }
    }

    /**
     * Handles menu item selection.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows a confirmation dialog before clearing all report data.
     */
    private fun showClearReportDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Report Data")
            .setMessage("Are you sure you want to delete all report data? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                val sharedPreferences = getSharedPreferences("reports", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                Toast.makeText(this, "Report data cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Recreates the activity with a fade animation.
     */
    private fun recreateWithAnimation() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}