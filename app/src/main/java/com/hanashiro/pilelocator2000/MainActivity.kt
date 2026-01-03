package com.hanashiro.pilelocator2000

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.config.Configuration as OsmConfiguration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/**
 * Aktivitas utama aplikasi, yang menampilkan peta dan navigasi utama.
 */
class MainActivity : AppCompatActivity() {

    // Deklarasi variabel untuk komponen UI dan overlay peta.
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var reportButton: Button
    private lateinit var managerButton: Button
    private lateinit var gpsAccuracyText: TextView

    /**
     * Penangan untuk hasil permintaan izin. Jika izin diberikan, aktifkan fitur lokasi.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableLocation()
        }
    }

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI, peta, dan listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Mengaktifkan tampilan edge-to-edge untuk UI yang imersif.
        setContentView(R.layout.activity_main)
        // Menyesuaikan padding untuk memperhitungkan bar sistem (seperti status bar).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi komponen UI.
        reportButton = findViewById(R.id.report_button)
        managerButton = findViewById(R.id.manager_button)
        gpsAccuracyText = findViewById(R.id.gps_accuracy_text)
        gpsAccuracyText.text = "GPS Accuracy: Not available"

        // Perbarui UI berdasarkan status login.
        updateWelcomeText()
        updateButtonStates()

        // Muat konfigurasi untuk osmdroid.
        OsmConfiguration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        // Siapkan tampilan peta.
        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.setBuiltInZoomControls(true)

        // Terapkan tema yang benar ke peta (terang atau gelap).
        updateMapTheme()

        // Atur posisi awal dan zoom peta.
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val startPoint = GeoPoint(48.8583736, 2.2944813) // Menara Eiffel sebagai titik awal default.
        mapController.setCenter(startPoint)

        // Siapkan overlay untuk menampilkan lokasi pengguna saat ini.
        locationOverlay = AccuracyUpdatingMyLocationOverlay(GpsMyLocationProvider(this), mapView)
        mapView.overlays.add(locationOverlay)

        // Minta izin yang diperlukan.
        requestLocationPermission()

        // Atur listener untuk tombol navigasi.
        reportButton.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        managerButton.setOnClickListener {
            val intent = Intent(this, ManagerActivity::class.java)
            startActivity(intent)
        }

        val settingsButton: Button = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Muat laporan yang ada untuk ditampilkan di peta.
        loadReports()
    }

    /**
     * Perbarui teks selamat datang untuk menyapa pengguna yang masuk atau tamu.
     */
    private fun updateWelcomeText() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("logged_in_user", null)
        val welcomeText: TextView = findViewById(R.id.welcome_text)
        if (loggedInUser != null) {
            welcomeText.text = "Welcome, $loggedInUser"
        } else {
            welcomeText.text = "Welcome, Guest"
        }
    }

    /**
     * Aktifkan atau nonaktifkan tombol berdasarkan status login pengguna.
     */
    private fun updateButtonStates() {
        val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("logged_in_user", null)
        val isLoggedIn = loggedInUser != null

        reportButton.isEnabled = isLoggedIn
        managerButton.isEnabled = isLoggedIn
    }

    /**
     * Terapkan filter warna ke ubin peta untuk mencocokkan tema aplikasi (terang/gelap).
     */
    private fun updateMapTheme() {
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (isDarkMode) {
            val colorMatrix = ColorMatrix()
            // Balikkan warna untuk membuat peta tampak gelap.
            colorMatrix.set(floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            ))
            mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(colorMatrix))
        } else {
            // Hapus filter untuk kembali ke tampilan peta default.
            mapView.overlayManager.tilesOverlay.setColorFilter(null)
        }
    }

    /**
     * Periksa apakah izin lokasi telah diberikan; jika tidak, minta izin tersebut.
     */
    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    /**
     * Aktifkan overlay lokasi untuk menampilkan dan mengikuti lokasi pengguna.
     */
    private fun enableLocation() {
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
    }

    /**
     * Ambil data laporan dari SharedPreferences dan tampilkan setiap laporan sebagai penanda di peta.
     */
    private fun loadReports() {
        val sharedPreferences = getSharedPreferences("reports", Context.MODE_PRIVATE)
        val reportsJson = sharedPreferences.getString("report_list", null)
        val reportsType = object : TypeToken<List<Report>>() {}.type
        val reports: List<Report> = Gson().fromJson(reportsJson, reportsType) ?: emptyList()

        // Hapus penanda lama sebelum menambahkan yang baru untuk menghindari duplikat.
        mapView.overlays.removeAll { it is Marker }

        for (report in reports) {
            val marker = Marker(mapView)
            marker.position = GeoPoint(report.latitude, report.longitude)
            marker.title = report.description
            marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.outline_borg_24, null)
            mapView.overlays.add(marker)
        }
    }

    /**
     * Dipanggil saat aktivitas menjadi terlihat oleh pengguna. Perbarui UI dan mulai ulang layanan lokasi.
     */
    override fun onResume() {
        super.onResume()
        updateWelcomeText()
        updateButtonStates()
        mapView.onResume()
        loadReports()
        updateMapTheme()
        locationOverlay.enableMyLocation()
    }

    /**
     * Dipanggil saat aktivitas tidak lagi terlihat oleh pengguna. Jeda layanan lokasi untuk menghemat baterai.
     */
    override fun onPause() {
        super.onPause()
        locationOverlay.disableMyLocation()
        mapView.onPause()
    }

    /**
     * Kelas dalam kustom untuk MyLocationNewOverlay yang memperbarui TextView akurasi GPS.
     */
    inner class AccuracyUpdatingMyLocationOverlay(provider: IMyLocationProvider, mapView: MapView) : MyLocationNewOverlay(provider, mapView) {
        /**
         * Dipanggil setiap kali lokasi pengguna diperbarui. Perbarui teks akurasi.
         */
        override fun onLocationChanged(location: Location?, provider: IMyLocationProvider?) {
            super.onLocationChanged(location, provider)
            location?.let {
                gpsAccuracyText.text = "GPS Accuracy: ${it.accuracy}m"
            }
        }
    }
}
