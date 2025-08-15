package com.example.foreverrusher

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.etEmail)
        val btnResetPassword: Button = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("ForgotPasswordActivity", "Sending password reset email to: $email")

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("ForgotPasswordActivity", "Reset email sent successfully")
                        Toast.makeText(this, "Reset link sent! Check your email", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Log.e("ForgotPasswordActivity", "Error: ${task.exception?.message}")
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
