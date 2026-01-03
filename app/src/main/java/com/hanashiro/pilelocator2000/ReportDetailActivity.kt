package com.hanashiro.pilelocator2000

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanashiro.pilelocator2000.Report
import java.text.DecimalFormat

/**
 * Aktivitas untuk menampilkan detail laporan tunggal.
 */
class ReportDetailActivity : AppCompatActivity() {

    // Deklarasi untuk komponen UI dan data laporan.
    private lateinit var reportImage: ImageView
    private lateinit var reportDescription: TextView
    private lateinit var reportTrashType: TextView
    private lateinit var reportCoordinates: TextView
    private lateinit var reportStatus: TextView
    private lateinit var markFinishedButton: Button
    private lateinit var deleteReportButton: Button

    private var reportId: Long = -1
    private var reports: MutableList<Report> = mutableListOf()

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI, muat data, dan atur listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_detail)

        // Siapkan toolbar dengan judul dan tombol kembali.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Report Detail"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inisialisasi komponen UI.
        reportImage = findViewById(R.id.report_image)
        reportDescription = findViewById(R.id.report_description)
        reportTrashType = findViewById(R.id.report_trash_type)
        reportCoordinates = findViewById(R.id.report_coordinates)
        reportStatus = findViewById(R.id.report_status)
        markFinishedButton = findViewById(R.id.mark_finished_button)
        deleteReportButton = findViewById(R.id.delete_report_button)

        // Dapatkan ID laporan dari intent.
        reportId = intent.getLongExtra("report_id", -1)

        // Muat semua laporan dan temukan yang cocok dengan ID.
        loadReports()
        val report = reports.find { it.id == reportId }

        // Jika laporan ditemukan, isi UI dengan datanya.
        if (report != null) {
            reportImage.setImageURI(Uri.parse(report.photoUri))
            reportDescription.text = report.description
            reportTrashType.text = report.trashType
            val df = DecimalFormat("#.#####")
            reportCoordinates.text = "Lat: ${df.format(report.latitude)}, Lon: ${df.format(report.longitude)}"
            reportStatus.text = report.status

            // Atur on-click listener untuk tombol "Mark as Finished".
            markFinishedButton.setOnClickListener {
                report.status = "Finished"
                saveReports()
                reportStatus.text = report.status
                Toast.makeText(this, "Report marked as finished", Toast.LENGTH_SHORT).show()
            }

            // Atur on-click listener untuk tombol "Delete Report".
            deleteReportButton.setOnClickListener {
                reports.remove(report)
                saveReports()
                Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show()
                finish() // Tutup aktivitas setelah laporan dihapus.
            }
        } else {
            // Jika laporan tidak ditemukan, tampilkan pesan error dan tutup aktivitas.
            Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show()
            finish()
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
     * Muat daftar laporan dari SharedPreferences.
     */
    private fun loadReports() {
        val sharedPreferences = getSharedPreferences("reports", Context.MODE_PRIVATE)
        val reportsJson = sharedPreferences.getString("report_list", null)
        val reportsType = object : TypeToken<List<Report>>() {}.type
        reports.clear()
        reports.addAll(Gson().fromJson(reportsJson, reportsType) ?: emptyList())
    }

    /**
     * Simpan daftar laporan saat ini ke SharedPreferences.
     */
    private fun saveReports() {
        val sharedPreferences = getSharedPreferences("reports", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("report_list", Gson().toJson(reports))
        editor.apply()
    }
}
