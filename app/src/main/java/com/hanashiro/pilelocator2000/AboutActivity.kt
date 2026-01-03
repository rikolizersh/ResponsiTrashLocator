package com.hanashiro.pilelocator2000

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

/**
 * Aktivitas untuk menampilkan informasi tentang aplikasi.
 */
class AboutActivity : AppCompatActivity() {
    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI dan latar belakang.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // Siapkan toolbar dengan judul dan tombol kembali.
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Atur gambar latar belakang berdasarkan tema aplikasi (terang/gelap).
        val backgroundImage: ImageView = findViewById(R.id.background_image)
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            backgroundImage.setImageResource(R.drawable.nightmodetopo)
        } else {
            backgroundImage.setImageResource(R.drawable.lightmodetopo)
        }
        backgroundImage.alpha = 0.15f // Atur opasitas latar belakang.
    }

    /**
     * Tangani acara klik tombol kembali di toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
