package com.hanashiro.pilelocator2000

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanashiro.pilelocator2000.Report
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.IOException
import java.text.DecimalFormat

/**
 * Aktivitas untuk membuat laporan baru.
 */
class ReportActivity : AppCompatActivity() {

    // Deklarasi untuk komponen UI dan data laporan.
    private lateinit var reportImage: ImageView
    private lateinit var reportDescription: TextInputEditText
    private lateinit var trashTypeAutoComplete: AutoCompleteTextView
    private lateinit var saveReportButton: MaterialButton
    private lateinit var mapView: MapView
    private lateinit var coordinatesText: TextView

    private var currentPhotoUri: Uri? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0
    private var locationMarker: Marker? = null

    /**
     * Penangan untuk hasil permintaan izin kamera. Jika diberikan, luncurkan intent kamera.
     */
    private val requestCameraPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Penangan untuk hasil permintaan izin lokasi. Jika diberikan, dapatkan lokasi saat ini.
     */
    private val requestLocationPermission = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getLocation()
            }
            else -> {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Penangan untuk hasil pengambilan gambar. Perbarui gambar pratinjau.
     */
    private val takePicture = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicture()) {
        if (it) {
            reportImage.setImageURI(currentPhotoUri)
            reportImage.scaleType = ImageView.ScaleType.CENTER_CROP
            reportImage.setPadding(0, 0, 0, 0)
        }
    }

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI, peta, dan listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        setContentView(R.layout.activity_report)

        // Siapkan toolbar dengan judul.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "New Report"

        // Inisialisasi komponen UI.
        reportImage = findViewById(R.id.report_image)
        reportDescription = findViewById(R.id.report_description)
        trashTypeAutoComplete = findViewById(R.id.trash_type_autocomplete)
        saveReportButton = findViewById(R.id.save_report_button)
        mapView = findViewById(R.id.map)
        coordinatesText = findViewById(R.id.coordinates_text)

        // Siapkan dropdown untuk jenis sampah.
        val trashTypes = resources.getStringArray(R.array.trash_types)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, trashTypes)
        trashTypeAutoComplete.setAdapter(adapter)

        // Atur on-click listener.
        reportImage.setOnClickListener { checkCameraPermission() }
        saveReportButton.setOnClickListener { saveReport() }

        // Siapkan peta dan minta izin lokasi.
        setupMap()
        checkLocationPermission()
    }

    /**
     * Siapkan peta dan tambahkan event listener untuk penanda.
     */
    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)
        mapView.setMultiTouchControls(true)

        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                updateMarkerPosition(p)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        mapView.overlays.add(0, mapEventsOverlay)
    }

    /**
     * Perbarui posisi penanda di peta dan perbarui teks koordinat.
     * @param geoPoint Titik geografis baru untuk penanda.
     */
    private fun updateMarkerPosition(geoPoint: GeoPoint) {
        currentLatitude = geoPoint.latitude
        currentLongitude = geoPoint.longitude

        val df = DecimalFormat("#.#####")
        coordinatesText.text = "Lat: ${df.format(currentLatitude)}, Lon: ${df.format(currentLongitude)}"

        if (locationMarker == null) {
            locationMarker = Marker(mapView)
            locationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(locationMarker)
        }
        locationMarker?.position = geoPoint
        mapView.invalidate()
    }

    /**
     * Periksa apakah izin kamera telah diberikan; jika tidak, minta izin tersebut.
     */
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispatchTakePictureIntent()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    /**
     * Luncurkan intent untuk mengambil gambar menggunakan aplikasi kamera.
     */
    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also { file ->
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "com.hanashiro.pilelocator2000.fileprovider",
                file
            )
            currentPhotoUri = photoUri
            takePicture.launch(photoUri)
        }
    }

    /**
     * Buat file gambar sementara untuk menyimpan foto.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    /**
     * Periksa apakah izin lokasi telah diberikan; jika tidak, minta izin tersebut.
     */
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getLocation()
            }
            else -> {
                requestLocationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    /**
     * Dapatkan lokasi terakhir yang diketahui dari penyedia lokasi GPS atau jaringan.
     */
    private fun getLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (location != null) {
                val startPoint = GeoPoint(location.latitude, location.longitude)
                mapView.controller.setCenter(startPoint)
                updateMarkerPosition(startPoint)
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Simpan laporan baru ke SharedPreferences.
     */
    private fun saveReport() {
        val description = reportDescription.text.toString()
        val trashType = trashTypeAutoComplete.text.toString()
        if (currentPhotoUri == null || description.isEmpty() || trashType.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("reports", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val reportsJson = sharedPreferences.getString("report_list", null)
        val reportsType = object : TypeToken<MutableList<Report>>() {}.type
        val reports: MutableList<Report> = Gson().fromJson(reportsJson, reportsType) ?: mutableListOf()

        val newReport = Report(System.currentTimeMillis(), currentLatitude, currentLongitude, currentPhotoUri.toString(), description, trashType)
        reports.add(newReport)

        editor.putString("report_list", Gson().toJson(reports))
        editor.apply()

        Toast.makeText(this, "Report saved!", Toast.LENGTH_SHORT).show()
        finish() // Tutup aktivitas setelah laporan disimpan.
    }

    /**
     * Dipanggil saat aktivitas dilanjutkan. Lanjutkan pembaruan peta.
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    /**
     * Dipanggil saat aktivitas dijeda. Jeda pembaruan peta untuk menghemat baterai.
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
