package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

/**
 * Activity for displaying and managing the user's profile.
 */
class ProfileActivity : AppCompatActivity() {

    // UI component declarations.
    private lateinit var profileImage: ImageView
    private lateinit var usernameText: TextView

    /**
     * Handles the result of the image selection process. Updates the profile picture when an image is chosen.
     */
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Persist permission to read the URI.
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            profileImage.setImageURI(it)

            // Save the new profile picture URI to SharedPreferences.
            val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("profile_picture_uri", it.toString())
            editor.apply()
        }
    }

    /**
     * Called when the activity is first created. Initializes the UI, loads data, and sets up listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Set up the toolbar with a title and back button.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize UI components.
        profileImage = findViewById(R.id.profile_image)
        usernameText = findViewById(R.id.username_text)
        val addPfpButton: Button = findViewById(R.id.add_pfp_button)
        val changeInfoButton: Button = findViewById(R.id.change_info_button)
        val logoutButton: Button = findViewById(R.id.logout_button)
        val deleteAccountButton: Button = findViewById(R.id.delete_account_button)

        // Update the profile UI with the current user data.
        updateProfileUI()

        // Set up on-click listener for the add/change profile picture button.
        addPfpButton.setOnClickListener {
            showPfpDialog()
        }

        // Set up on-click listener for the change info button.
        changeInfoButton.setOnClickListener {
            showChangeInfoBottomSheet()
        }

        // Set up on-click listener for the logout button.
        logoutButton.setOnClickListener {
            // Clear the logged-in user and profile picture from SharedPreferences.
            val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.remove("logged_in_user")
            editor.remove("profile_picture_uri")
            editor.apply()

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Return to MainActivity after logout.
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finishAffinity()
        }

        // Set up on-click listener for the delete account button.
        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    /**
     * Called when the activity is resumed. Updates the profile UI to ensure data is fresh.
     */
    override fun onResume() {
        super.onResume()
        updateProfileUI()
    }

    /**
     * Handles the back button click in the toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Updates the profile picture and username in the UI.
     */
    private fun updateProfileUI() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val imageUriString = sharedPreferences.getString("profile_picture_uri", null)
        val username = sharedPreferences.getString("logged_in_user", "Username")

        if (imageUriString != null) {
            profileImage.setImageURI(Uri.parse(imageUriString))
        } else {
            profileImage.setImageResource(R.drawable.prometheus)
        }
        usernameText.text = username
    }

    /**
     * Displays a dialog to choose between changing or removing the profile picture.
     */
    private fun showPfpDialog() {
        val options = arrayOf("Change Picture", "Remove Picture")
        AlertDialog.Builder(this)
            .setTitle("Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> selectImage.launch("image/*")
                    1 -> removeProfilePicture()
                }
            }
            .show()
    }

    /**
     * Removes the user's profile picture from SharedPreferences and updates the UI.
     */
    private fun removeProfilePicture() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("profile_picture_uri").apply()
        profileImage.setImageResource(R.drawable.prometheus)
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
    }

    /**
     * Displays a bottom sheet for changing the username.
     */
    private fun showChangeInfoBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_change_info, null)
        bottomSheetDialog.setContentView(view)

        val usernameEditText = view.findViewById<TextInputEditText>(R.id.username_edit_text)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        usernameEditText.setText(sharedPreferences.getString("logged_in_user", ""))

        // Set up on-click listener for the save button in the bottom sheet.
        saveButton.setOnClickListener {
            val newUsername = usernameEditText.text.toString()
            if (newUsername.isNotEmpty()) {
                // Save the new username to SharedPreferences.
                val editor = sharedPreferences.edit()
                editor.putString("logged_in_user", newUsername)
                editor.apply()
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show()
                updateProfileUI() // Refresh the UI after the username is changed.
                bottomSheetDialog.dismiss()
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    /**
     * Displays a confirmation dialog before deleting the user's account.
     */
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes all user data from SharedPreferences and returns to the MainActivity.
     */
    private fun deleteAccount() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}
