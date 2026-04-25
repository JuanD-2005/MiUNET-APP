package com.example.miunet01.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.miunet01.R
import com.example.miunet01.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.activity_register)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        // Configurar el AutoCompleteTextView para roles desde strings.xml
        val roles = resources.getStringArray(R.array.roles_array)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinnerRole.setAdapter(adapter)

        // Establecer un valor por defecto
        binding.spinnerRole.setText("Estudiante", false)

        // 1. Mostrar/Ocultar el campo de PIN dinámicamente
        binding.spinnerRole.setOnItemClickListener { parent, _, position, _ ->
            val rolSeleccionado = parent.getItemAtPosition(position).toString()

            if (rolSeleccionado == "Profesor") {
                // Animación suave de entrada
                binding.layoutPinProfesor.alpha = 0f
                binding.layoutPinProfesor.visibility = View.VISIBLE
                binding.layoutPinProfesor.animate().alpha(1f).setDuration(300).start()
            } else {
                binding.layoutPinProfesor.visibility = View.GONE
                binding.editPinProfesor.text?.clear() // Limpiamos si se arrepiente y vuelve a Estudiante
                binding.layoutPinProfesor.error = null
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            attemptRegistration()
        }

        binding.btnBackToLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun attemptRegistration() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val role = binding.spinnerRole.text.toString().trim()
        val pin = binding.editPinProfesor.text.toString().trim()

        if (isValidForm(email, password, role, pin)) {
            registerUser(email, password, role)
        }
    }

    private fun isValidForm(email: String, password: String, role: String, pin: String): Boolean {
        var isValid = true

        // Validar email
        if (email.isEmpty()) {
            binding.editEmail.error = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Email inválido"
            isValid = false
        } else if (!email.endsWith("@unet.edu.ve")) {
            binding.editEmail.error = "Debe usar correo institucional (@unet.edu.ve)"
            isValid = false
        } else {
            binding.editEmail.error = null
        }

        // Validar contraseña
        if (password.isEmpty()) {
            binding.editPassword.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.editPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else {
            binding.editPassword.error = null
        }

        // Validar rol
        val rolesArray = resources.getStringArray(R.array.roles_array)
        if (role.isEmpty()) {
            binding.spinnerRole.error = "Seleccione un rol"
            isValid = false
        } else if (!rolesArray.contains(role)) {
            binding.spinnerRole.error = "Rol inválido"
            isValid = false
        } else {
            binding.spinnerRole.error = null
        }

        // 🛡️ EL BLINDAJE DEL PIN (Solo si eligió Profesor)
        if (role == "Profesor") {
            val PIN_SECRETO = "UNET-2026-PROFE"

            if (pin != PIN_SECRETO) {
                binding.layoutPinProfesor.error = "PIN inválido o vacío"
                binding.editPinProfesor.requestFocus()
                showToast("Acceso denegado. No eres el profesor Oak.")
                isValid = false
            } else {
                binding.layoutPinProfesor.error = null // Limpiar error si está bien
            }
        }

        return isValid
    }

    private fun registerUser(email: String, password: String, role: String) {
        // Mostrar estado de carga
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registrando..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: run {
                        restoreRegisterButton()
                        showToast("Error: No se pudo obtener el ID del usuario")
                        return@addOnCompleteListener
                    }

                    val user = hashMapOf(
                        "uid" to userId,
                        "email" to email,
                        "role" to role,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("users").document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Log.i("Register", "Usuario creado y guardado en Firestore: $email")
                            showToast("¡Usuario registrado correctamente!")

                            // Regresa al login automáticamente
                            navigateToLogin()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Register", "Error guardando usuario en Firestore", e)
                            restoreRegisterButton()
                            showToast("Error guardando datos: ${e.message}")
                        }

                } else {
                    val ex = task.exception
                    Log.e("Register", "Error creando usuario", ex)
                    restoreRegisterButton()
                    showToast("Error creando usuario: ${ex?.message}")
                }
            }
    }

    private fun restoreRegisterButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Registrarse"
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
