package com.hanashiro.pilelocator2000

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    // Fungsi ini dipanggil saat aktivitas dibuat.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi elemen UI.
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)
        val registerButton: Button = findViewById(R.id.register_button)

        // On-click listener untuk tombol login.
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Dapatkan kredensial yang disimpan dari SharedPreferences.
            val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
            val savedPassword = sharedPreferences.getString(username, null)

            // Periksa apakah kredensial valid.
            if (savedPassword != null && savedPassword == password) {
                // Simpan pengguna yang login.
                val editor = sharedPreferences.edit()
                editor.putString("logged_in_user", username)
                editor.apply()

                // Arahkan ke MainActivity.
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Tampilkan pesan error jika kredensial tidak valid.
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // On-click listener untuk tombol register.
        registerButton.setOnClickListener {
            // Arahkan ke RegisterActivity.
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
