package com.hanashiro.pilelocator2000

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Aktivitas untuk mendaftarkan pengguna baru.
 */
class RegisterActivity : AppCompatActivity() {

    /**
     * Dipanggil saat aktivitas pertama kali dibuat. Inisialisasi UI dan listener.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi komponen UI.
        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirm_password)
        val registerButton: Button = findViewById(R.id.register_button)

        // Atur on-click listener untuk tombol register.
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // Periksa apakah input valid.
            if (username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Simpan kredensial pengguna baru ke SharedPreferences.
                    val sharedPreferences = getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString(username, password)
                    editor.apply()

                    // Tampilkan pesan sukses dan kembali ke layar sebelumnya.
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Tampilkan pesan error jika kata sandi tidak cocok.
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Tampilkan pesan error jika ada bidang yang kosong.
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
