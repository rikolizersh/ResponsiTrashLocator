package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.hanashiro.pilelocator2000.Report

/**
 * Adapter untuk menampilkan daftar laporan dalam RecyclerView.
 * @param context Konteks dari aktivitas yang memanggil.
 * @param reports Daftar laporan yang akan ditampilkan.
 * @param onItemClick Lambda yang akan dieksekusi saat sebuah item diklik.
 */
class ReportAdapter(private val context: Context, private val reports: List<Report>, private val onItemClick: (Report) -> Unit) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    /**
     * Membuat ViewHolder baru saat RecyclerView membutuhkannya.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    /**
     * Mengikat data dari laporan ke ViewHolder pada posisi tertentu.
     */
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)
    }

    /**
     * Mengembalikan jumlah total item dalam daftar laporan.
     */
    override fun getItemCount() = reports.size

    /**
     * ViewHolder untuk setiap item laporan dalam RecyclerView.
     * @param itemView Tampilan item tunggal.
     */
    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Inisialisasi komponen UI dari layout item.
        private val cardView: MaterialCardView = itemView.findViewById(R.id.report_card)
        private val imageView: ImageView = itemView.findViewById(R.id.report_image)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.report_description)
        private val trashTypeTextView: TextView = itemView.findViewById(R.id.report_trash_type)
        private val statusTextView: TextView = itemView.findViewById(R.id.report_status)

        /**
         * Mengikat data dari objek Report ke komponen UI.
         * @param report Objek Report yang akan ditampilkan.
         */
        fun bind(report: Report) {
            // Atur data ke masing-masing view.
            imageView.setImageURI(Uri.parse(report.photoUri))
            descriptionTextView.text = report.description
            trashTypeTextView.text = report.trashType
            statusTextView.text = report.status

            // Periksa apakah mode gelap sedang aktif.
            val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

            // Redupkan item jika statusnya "Finished".
            if (report.status.equals("Finished", ignoreCase = true)) {
                cardView.alpha = 0.5f
            } else {
                cardView.alpha = 1.0f
            }

            // Sorot item jika jenis sampahnya "Hazardous".
            if (report.trashType.equals("Hazardous", ignoreCase = true)) {
                if (isDarkMode) {
                    cardView.setCardBackgroundColor(Color.argb(50, 255, 100, 100))
                } else {
                    cardView.setCardBackgroundColor(Color.argb(50, 255, 0, 0))
                }
            } else {
                // Atur warna latar belakang default berdasarkan tema.
                val defaultCardColor = if (isDarkMode) Color.DKGRAY else Color.WHITE
                cardView.setCardBackgroundColor(defaultCardColor)
            }

            // Atur on-click listener untuk item ini.
            itemView.setOnClickListener { onItemClick(report) }
        }
    }
}
