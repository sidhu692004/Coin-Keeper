package com.example.foreverrusher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()

        val googleSignInButton: Button = findViewById(R.id.btnGoogleSignIn)
        val emailSignInButton: Button = findViewById(R.id.btnEmailSignIn)
        val registerButton: Button = findViewById(R.id.btnRegister)
        val forgotPasswordButton: Button = findViewById(R.id.btnForgotPassword)
        val emailEditText: EditText = findViewById(R.id.etEmail)
        val passwordEditText: EditText = findViewById(R.id.etPassword)

        googleSignInButton.setOnClickListener { signInWithGoogle() }
        emailSignInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            signInWithEmail(email, password)
        }

        registerButton.setOnClickListener {
            Log.d("AuthActivity", "Register Button Clicked")
            Toast.makeText(this, "Opening Register Screen", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordButton.setOnClickListener {
            Log.d("AuthActivity", "Forgot Password Button Clicked")
            Toast.makeText(this, "Opening Forgot Password Screen", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        setupGoogleSignIn()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("692114982129-vbkaskf12u3543ddimirbe1725o0jt7f.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("AuthActivity", "Google sign-in failed", e)
                }
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToGame()
                } else {
                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToGame()
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    Toast.makeText(this, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
                    Log.e("AuthActivity", "Email login failed", task.exception)
                }
            }

    }

    private fun navigateToGame() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }
}
