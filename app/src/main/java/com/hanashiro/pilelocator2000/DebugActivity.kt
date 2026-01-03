package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val exportDataButton: Button = findViewById(R.id.export_data_button)
        exportDataButton.setOnClickListener {
            exportData()
        }

        val importDataButton: Button = findViewById(R.id.import_data_button)
        importDataButton.setOnClickListener {
            importData()
        }

        val checkCredentialsButton: Button = findViewById(R.id.check_credentials_button)
        checkCredentialsButton.setOnClickListener {
            checkCredentials()
        }
    }

    private fun exportData() {
        val reportPrefs = getSharedPreferences("reports", Context.MODE_PRIVATE)
        val userPrefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val data = mapOf("reports" to reportPrefs.all, "user_credentials" to userPrefs.all)
        val json = Gson().toJson(data)

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "pilelocator_backup.json")
        }
        startActivityForResult(intent, EXPORT_REQUEST_CODE)
    }

    private fun importData() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    private fun checkCredentials() {
        val userPrefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val credentials = userPrefs.all.toString()
        AlertDialog.Builder(this)
            .setTitle("Saved Credentials")
            .setMessage(credentials)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                EXPORT_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        try {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                val reportPrefs = getSharedPreferences("reports", Context.MODE_PRIVATE)
                                val userPrefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                                val data = mapOf("reports" to reportPrefs.all, "user_credentials" to userPrefs.all)
                                val json = Gson().toJson(data)
                                outputStream.write(json.toByteArray())
                                Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                IMPORT_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        try {
                            contentResolver.openInputStream(uri)?.use { inputStream ->
                                val json = inputStream.reader().readText()
                                val data = Gson().fromJson(json, Map::class.java) as Map<String, Map<String, Any>>

                                val reportPrefs = getSharedPreferences("reports", Context.MODE_PRIVATE).edit()
                                reportPrefs.clear().apply()
                                val reports = data["reports"] ?: emptyMap()
                                for ((key, value) in reports) {
                                    reportPrefs.putString(key, value.toString())
                                }
                                reportPrefs.apply()

                                val userPrefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE).edit()
                                userPrefs.clear().apply()
                                val credentials = data["user_credentials"] ?: emptyMap()
                                for ((key, value) in credentials) {
                                    userPrefs.putString(key, value.toString())
                                }
                                userPrefs.apply()

                                Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show()
                                recreate()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to import data", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXPORT_REQUEST_CODE = 1001
        private const val IMPORT_REQUEST_CODE = 1002
    }
}