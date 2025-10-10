package com.example.miunet01

import android.os.Bundle
import android.view.Menu
import android.util.Log // Para Kotlin
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.miunet01.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var userRole: String = "Estudiante"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        supportActionBar?.hide() // 🔹 Oculta la ActionBar por si aún aparece

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener rol del Intent
        userRole = intent.getStringExtra("USER_ROLE") ?: "Estudiante"


        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)


// 🔹 Pasar USER_ROLE al fragmento actual cada vez que cambia la navegación
        navController.addOnDestinationChangedListener { _, _, _ ->
            val currentFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main)
                ?.childFragmentManager
                ?.fragments
                ?.firstOrNull()

            currentFragment?.arguments = Bundle().apply {
                putString("USER_ROLE", userRole)
            }
        }


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_unet_info,
                R.id.navigation_chatbot,
                R.id.navigation_tramites,
                R.id.navigation_enlaces,
                R.id.navigation_usuario
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }


}

