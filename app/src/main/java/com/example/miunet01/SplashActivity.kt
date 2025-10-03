package com.example.miunet01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.miunet01.ui.login.LoginActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Usuario logueado → va a Main
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No logueado → va a Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
