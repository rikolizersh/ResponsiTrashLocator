package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hanashiro.pilelocator2000.Report

/**
 * Aktivitas untuk mengelola laporan yang ada.
 */
class ManagerActivity : AppCompatActivity() {

    // Deklarasi untuk komponen UI dan data laporan.
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var reportAdapter: ReportAdapter
    private var reports: MutableList<Report> = mutableListOf()
    private lateinit var emptyView: LinearLayout

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI, muat data, dan siapkan RecyclerView.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        // Siapkan toolbar dengan judul.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Manage Reports"

        // Inisialisasi komponen UI.
        reportsRecyclerView = findViewById(R.id.reports_recycler_view)
        emptyView = findViewById(R.id.empty_view)
        reportsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Muat laporan dan siapkan adapter untuk RecyclerView.
        loadReports()

        reportAdapter = ReportAdapter(this, reports) { report ->
            val intent = Intent(this, ReportDetailActivity::class.java)
            intent.putExtra("report_id", report.id)
            startActivity(intent)
        }
        reportsRecyclerView.adapter = reportAdapter

        // Tampilkan atau sembunyikan tampilan kosong berdasarkan apakah ada laporan.
        updateEmptyView()
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
     * Tampilkan atau sembunyikan tampilan kosong jika tidak ada laporan untuk ditampilkan.
     */
    private fun updateEmptyView() {
        if (reports.isEmpty()) {
            reportsRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            reportsRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }

    /**
     * Dipanggil saat aktivitas dilanjutkan. Muat ulang laporan dan perbarui UI.
     */
    override fun onResume() {
        super.onResume()
        loadReports()
        reportAdapter.notifyDataSetChanged()
        updateEmptyView()
    }
}
