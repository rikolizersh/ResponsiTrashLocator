package com.hanashiro.pilelocator2000

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar

/**
 * Aktivitas untuk mengelola dan menampilkan status izin aplikasi.
 */
class PermissionsActivity : AppCompatActivity() {

    // Deklarasi untuk komponen UI.
    private lateinit var locationStatus: TextView
    private lateinit var cameraStatus: TextView
    private lateinit var grantLocationButton: Button
    private lateinit var grantCameraButton: Button

    /**
     * Penangan untuk hasil permintaan izin lokasi. Memperbarui UI setelah izin diberikan atau ditolak.
     */
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionStatus()
    }

    /**
     * Penangan untuk hasil permintaan izin kamera. Memperbarui UI setelah izin diberikan atau ditolak.
     */
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionStatus()
    }

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI, atur listener, dan perbarui status izin.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        // Siapkan toolbar dengan judul dan tombol kembali.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Permissions"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inisialisasi komponen UI.
        locationStatus = findViewById(R.id.location_status)
        cameraStatus = findViewById(R.id.camera_status)
        grantLocationButton = findViewById(R.id.grant_location_button)
        grantCameraButton = findViewById(R.id.grant_camera_button)

        // Perbarui status izin saat aktivitas dimulai.
        updatePermissionStatus()

        // Atur on-click listener untuk tombol izin lokasi.
        grantLocationButton.setOnClickListener {
            // Jika pengguna telah menolak izin sebelumnya, buka pengaturan aplikasi.
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                openAppSettings()
            }
        }

        // Atur on-click listener untuk tombol izin kamera.
        grantCameraButton.setOnClickListener {
            // Jika pengguna telah menolak izin sebelumnya, buka pengaturan aplikasi.
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            } else {
                openAppSettings()
            }
        }
    }

    /**
     * Tangani acara klik tombol kembali di toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    /**
     * Perbarui UI untuk menampilkan status izin saat ini (Diberikan/Ditolak).
     */
    private fun updatePermissionStatus() {
        val locationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        locationStatus.text = if (locationPermission) "Granted" else "Denied"
        cameraStatus.text = if (cameraPermission) "Granted" else "Denied"

        // Sembunyikan tombol "Grant" jika izin sudah diberikan.
        grantLocationButton.visibility = if (locationPermission) View.GONE else View.VISIBLE
        grantCameraButton.visibility = if (cameraPermission) View.GONE else View.VISIBLE
    }

    /**
     * Buka layar pengaturan aplikasi untuk aplikasi ini, sehingga pengguna dapat memberikan izin secara manual.
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}
