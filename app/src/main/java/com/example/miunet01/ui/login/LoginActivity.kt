package com.example.miunet01.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miunet01.MainActivity
import com.example.miunet01.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.miunet01.R

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()  // 🔹 Oculta la barra superior
        setContentView(R.layout.activity_login) // o activity_register

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.btnRegister.setOnClickListener {
            openRegistration()
        }

        binding.txtForgotPassword.setOnClickListener {
            openForgotPassword()
        }
    }

    private fun setupTextWatchers() {
        // Validación en tiempo real para email
        binding.editEmailLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail(s.toString())
            }
        })

        // Validación en tiempo real para contraseña
        binding.editPasswordLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePassword(s.toString())
            }
        })
    }

    private fun attemptLogin() {
        val email = binding.editEmailLogin.text.toString().trim()
        val password = binding.editPasswordLogin.text.toString().trim()

        if (isValidForm(email, password)) {
            performLogin(email, password)
        } else {
            // Mostrar mensaje específico según los errores
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Por favor, ingresa un email válido")
            } else if (password.length < 6) {
                showToast("La contraseña debe tener al menos 6 caracteres")
            }
        }
    }

    private fun isValidForm(email: String, password: String): Boolean {
        var isValid = true

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmailLogin.error = "Email inválido"
            isValid = false
        } else {
            binding.editEmailLogin.error = null
        }

        if (password.length < 6) {
            binding.editPasswordLogin.error = "Mínimo 6 caracteres"
            isValid = false
        } else {
            binding.editPasswordLogin.error = null
        }

        return isValid
    }

    private fun validateEmail(email: String) {
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmailLogin.error = "Email inválido"
        } else {
            binding.editEmailLogin.error = null
        }
    }

    private fun validatePassword(password: String) {
        if (password.isNotEmpty() && password.length < 6) {
            binding.editPasswordLogin.error = "Mínimo 6 caracteres"
        } else {
            binding.editPasswordLogin.error = null
        }
    }

    private fun performLogin(email: String, password: String) {
        // Deshabilitar el botón y mostrar estado de carga
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Iniciando sesión..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 🔹 AUTENTICACIÓN EXITOSA
                    val userId = auth.currentUser?.uid ?: run {
                        restoreLoginButton()
                        showToast("Error: Usuario no encontrado")
                        return@addOnCompleteListener
                    }

                    // 2. Obtener el Documento del Usuario en Firestore
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role") ?: "Estudiante"

                                // 3. Redirigir y pasar el rol a MainActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("USER_ROLE", role)

                                // Guardar el rol globalmente
                                val prefs = getSharedPreferences("MiUNET_PREFS", MODE_PRIVATE)
                                prefs.edit().putString("USER_ROLE", role).apply()

                                showToast("¡Bienvenido!")
                                startActivity(intent)
                                finish() // Cierra la actividad de Login

                            } else {
                                restoreLoginButton()
                                showToast("Usuario sin rol asignado")
                            }
                        }
                        .addOnFailureListener { e ->
                            restoreLoginButton()
                            showToast("Error obteniendo rol: ${e.message}")
                        }

                } else {
                    // 🔹 ERROR DE AUTENTICACIÓN
                    restoreLoginButton()
                    showToast("Error: ${task.exception?.message}")
                }
            }
    }

    private fun restoreLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Iniciar Sesión"
    }

    private fun openRegistration() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun openForgotPassword() {
        showToast("Comunícate con juan.paredes@unet.edu.ve")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
