package com.example.miunet01.ui.unetinfo

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miunet01.R
import com.example.miunet01.databinding.FragmentUnetInfoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

class UnetInfoFragment : Fragment() {

    private var _binding: FragmentUnetInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var userRole: String  // 🔹 ahora es global

    private val listaEventos = mutableListOf<Evento>()
    private val listaHorarios = mutableListOf<Horario>()
    private lateinit var adapterEventos: EventosAdapter
    private lateinit var adapterHorarios: HorariosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnetInfoBinding.inflate(inflater, container, false)
        val view = binding.root

        db = FirebaseFirestore.getInstance()

        // 🔹 Recuperar el rol globalmente
        val prefs = requireContext().getSharedPreferences("MiUNET_PREFS", AppCompatActivity.MODE_PRIVATE)
        userRole = prefs.getString("USER_ROLE", "Estudiante") ?: "Estudiante"
        Log.d("ROLE_DEBUG", "Rol recibido en UnetInfoFragment (SharedPrefs): $userRole")

        // 🔹 Mostrar el botón solo si es Admin
        if (userRole == "Admin") {
            binding.btnAgregarEvento.visibility = View.VISIBLE
        } else {
            binding.btnAgregarEvento.visibility = View.GONE
        }

        // 🔹 Configurar RecyclerViews
        adapterEventos = EventosAdapter(listaEventos, userRole) { evento ->
            mostrarDialogoEditarEvento(evento)
        }
        adapterHorarios = HorariosAdapter(listaHorarios)

        binding.recyclerEventos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerEventos.adapter = adapterEventos



        // 🔹 Cargar datos desde Firestore
        cargarEventos()

        // 🔹 Acción del botón (solo visible para Admin)
        binding.btnAgregarEvento.setOnClickListener {
            mostrarDialogoAgregarEvento()
        }


        // Definimos la fecha objetivo: 10 de noviembre de 2025, 00:00:00
        val fechaObjetivo = Calendar.getInstance().apply {
            set(2025, Calendar.NOVEMBER, 10, 0, 0, 0)
        }.timeInMillis

        val textCuenta = binding.txtCuentaRegresiva
        val handler = android.os.Handler()

        val runnable = object : Runnable {
            override fun run() {
                val ahora = System.currentTimeMillis()
                val diferencia = fechaObjetivo - ahora

                if (diferencia > 0) {
                    val dias = diferencia / (1000 * 60 * 60 * 24)
                    val horas = (diferencia / (1000 * 60 * 60)) % 24
                    val minutos = (diferencia / (1000 * 60)) % 60
                    val segundos = (diferencia / 1000) % 60

                    val texto = String.format(
                        Locale.getDefault(),
                        "%02dd %02dh %02dm %02ds",
                        dias, horas, minutos, segundos
                    )

                    textCuenta.text = texto
                    handler.postDelayed(this, 1000)
                } else {
                    textCuenta.text = "¡El semestre ha comenzado! 🎓"
                }
            }
        }

// Iniciar el contador
        handler.post(runnable)

        setupWelcomeMessage()

        return view
    }

    // En tu Activity o Fragment
    private fun setupRecyclerView() {
        binding.recyclerEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterEventos
            // Esto ayuda con el rendimiento en NestedScrollView
            setHasFixedSize(false)
            isNestedScrollingEnabled = false  // 👈 Importante para smooth scrolling
        }
    }



    private fun setupWelcomeMessage() {
        val saludo = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..11 -> "¡Buenos días Inge! ☀️"
            in 12..18 -> "¡Buenas tardes Inge! 🌞"
            else -> "¡Buenas noches Inge! 🌙"
        }
        binding.tvBienvenida.text = saludo
    }


    // --- 🔹 Diálogo para agregar eventos ---
    private fun mostrarDialogoAgregarEvento() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_evento, null)
        val titulo = dialogView.findViewById<EditText>(R.id.editTitulo)
        val fecha = dialogView.findViewById<EditText>(R.id.editFecha)
        val lugar = dialogView.findViewById<EditText>(R.id.editLugar)
        val descripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar Evento")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoEvento = hashMapOf(
                    "titulo" to titulo.text.toString(),
                    "fecha" to fecha.text.toString(),
                    "lugar" to lugar.text.toString(),
                    "descripcion" to descripcion.text.toString()
                )

                db.collection("eventos")
                    .add(nuevoEvento)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Evento agregado correctamente", Toast.LENGTH_SHORT).show()
                        cargarEventos()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // --- 🔹 Diálogo para editar eventos ---
    private fun mostrarDialogoEditarEvento(evento: Evento) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_evento, null)
        val titulo = dialogView.findViewById<EditText>(R.id.editTitulo)
        val fecha = dialogView.findViewById<EditText>(R.id.editFecha)
        val lugar = dialogView.findViewById<EditText>(R.id.editLugar)
        val descripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)

        titulo.setText(evento.titulo)
        fecha.setText(evento.fecha)
        lugar.setText(evento.lugar)
        descripcion.setText(evento.descripcion)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Evento")
            .setView(dialogView)
            .setPositiveButton("Guardar Cambios") { _, _ ->
                val nuevosDatos = mapOf(
                    "titulo" to titulo.text.toString(),
                    "fecha" to fecha.text.toString(),
                    "lugar" to lugar.text.toString(),
                    "descripcion" to descripcion.text.toString()
                )

                db.collection("eventos").document(evento.id!!)
                    .update(nuevosDatos)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Evento actualizado correctamente", Toast.LENGTH_SHORT).show()
                        cargarEventos()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            .setNeutralButton("Eliminar") { _, _ ->
                confirmarEliminacionEvento(evento)
            }

            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun confirmarEliminacionEvento(evento: Evento) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Evento")
            .setMessage("¿Seguro que deseas eliminar \"${evento.titulo}\"?")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                db.collection("eventos").document(evento.id!!)
                    .delete()
                    .addOnSuccessListener {
                        val index = listaEventos.indexOfFirst { it.id == evento.id }
                        if (index != -1) {
                            listaEventos.removeAt(index)
                            adapterEventos.notifyItemRemoved(index)

                            // 🔹 Animación de salida
                            binding.recyclerEventos.layoutAnimation =
                                android.view.animation.AnimationUtils.loadLayoutAnimation(
                                    requireContext(),
                                    R.anim.layout_fade_in
                                )
                            binding.recyclerEventos.scheduleLayoutAnimation()
                        }

                        Toast.makeText(requireContext(), "Evento eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }




    // --- 🔹 Cargar eventos desde Firebase ---
    private fun cargarEventos() {
        db.collection("eventos")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                listaEventos.clear()
                for (document in result) {
                    val evento = document.toObject(Evento::class.java)
                    evento.id = document.id
                    listaEventos.add(evento)
                }

                adapterEventos.actualizarLista(listaEventos)
            }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(requireContext(), "Error al cargar eventos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




