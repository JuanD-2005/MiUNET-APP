package com.example.miunet01.ui.login

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.miunet01.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // ✅ Inicialización de Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailField = findViewById<EditText>(R.id.editEmail)
        val passField = findViewById<EditText>(R.id.editPassword)
        val roleSpinner = findViewById<Spinner>(R.id.spinnerRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passField.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, role)
        }
    }

    private fun registerUser(email: String, password: String, role: String) {
        if (!email.endsWith("@unet.edu.ve")) {
            Toast.makeText(this, "Usa tu correo institucional", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val user = hashMapOf(
                        "uid" to userId,
                        "email" to email,
                        "role" to role
                    )

                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                            Log.i("Register", "Usuario creado y guardado en Firestore: $email")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Register", "Error guardando usuario en Firestore", e)
                            Toast.makeText(this, "Error guardando rol: ${e.message}", Toast.LENGTH_LONG).show()
                        }

                } else {
                    val ex = task.exception
                    Log.e("Register", "Error creando usuario", ex)
                    Toast.makeText(this, "Error creando usuario: ${ex?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}

