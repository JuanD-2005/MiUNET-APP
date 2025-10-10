package com.example.miunet01.ui.usuarios

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miunet01.databinding.FragmentUsuariosBinding
import com.example.miunet01.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsuariosFragment : Fragment() {

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsuariosBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val prefs = requireContext().getSharedPreferences("MiUNET_PREFS", 0)
        val userRole = prefs.getString("USER_ROLE", "Estudiante")

        binding.tvUserEmail.text = "Correo: ${user?.email ?: "No disponible"}"
        binding.tvUserRole.text = "Rol: $userRole"

        // 🔹 Cambiar contraseña
        binding.btnCambiarContrasena.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(
                android.R.layout.simple_list_item_1, null
            )
            val input = EditText(requireContext())
            input.hint = "Nueva contraseña"
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

            AlertDialog.Builder(requireContext())
                .setTitle("Cambiar Contraseña")
                .setView(input)
                .setPositiveButton("Actualizar") { _, _ ->
                    val nuevaContrasena = input.text.toString().trim()
                    if (nuevaContrasena.length < 6) {
                        Toast.makeText(requireContext(), "Debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    } else {
                        user?.updatePassword(nuevaContrasena)
                            ?.addOnSuccessListener {
                                Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // 🔹 Cerrar sesión
        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        binding.btnEliminarCuenta.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val user = FirebaseAuth.getInstance().currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        // 1️⃣ Eliminar de Firestore
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .delete()
                            .addOnSuccessListener {
                                // 2️⃣ Eliminar de FirebaseAuth
                                user.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Error eliminando usuario: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error eliminando datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }


        binding.btnAyuda.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Ayuda y Soporte")
                .setMessage(
                    "Si tienes problemas con tu cuenta o necesitas asistencia técnica, puedes escribir a:\n\n" +
                            "📧 jdpgparedes@gmail.com\n\n" +
                            "Horario de atención: Lunes a Viernes, 8:00 a.m. - 4:00 p.m."
                )
                .setPositiveButton("Aceptar", null)
                .show()
        }






        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
