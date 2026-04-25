package com.example.miunet01.ui.unetinfo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

class UnetInfoFragment : Fragment() {

    private var _binding: FragmentUnetInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var userRole: String

    private val listaEventos = mutableListOf<Evento>()
    private lateinit var adapterEventos: EventosAdapter

    // Variables para el control del reloj en tiempo real
    private var snapshotListener: ListenerRegistration? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnetInfoBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        // 🔹 Recuperar el rol globalmente
        val prefs = requireContext().getSharedPreferences("MiUNET_PREFS", AppCompatActivity.MODE_PRIVATE)
        userRole = prefs.getString("USER_ROLE", "Estudiante") ?: "Estudiante"
        Log.d("ROLE_DEBUG", "Rol recibido en UnetInfoFragment (SharedPrefs): $userRole")

        // 🔹 Mostrar controles de Admin
        if (userRole == "Admin") {
            binding.btnAgregarEvento.visibility = View.VISIBLE

            // Truco UI: El Admin puede tocar el área del reloj para editarlo
            binding.imgSection.isClickable = true
            binding.imgSection.setOnClickListener {
                mostrarDialogoEditarSemestre()
            }
        } else {
            binding.btnAgregarEvento.visibility = View.GONE
            binding.imgSection.isClickable = false
        }

        // 🔹 Configurar RecyclerView (con tu optimización para NestedScrollView)
        adapterEventos = EventosAdapter(listaEventos, userRole) { evento ->
            mostrarDialogoEditarEvento(evento)
        }
        setupRecyclerView()

        // 🔹 Cargar datos desde Firestore
        cargarEventos()

        // 🔹 Iniciar escucha del reloj en tiempo real
        escucharConfiguracionSemestre()

        // 🔹 Acción del botón (solo visible para Admin)
        binding.btnAgregarEvento.setOnClickListener {
            mostrarDialogoAgregarEvento()
        }

        setupWelcomeMessage()

        return binding.root
    }

    // --- 🔹 NUEVO: Lógica del Reloj Dinámico con Firestore ---
    private fun escucharConfiguracionSemestre() {
        val docRef = db.collection("configuracion").document("semestre")

        snapshotListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null || _binding == null) {
                binding.txtFechaObjetivo.text = "Error al conectar 😅"
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val fechaInicio = snapshot.getTimestamp("fecha_inicio")?.toDate()
                val nombreSemestre = snapshot.getString("nombre_semestre") ?: ""

                if (fechaInicio != null) {
                    binding.txtTituloCuentaRegresiva.text = "⏳ Inicio del Semestre $nombreSemestre:"

                    val formatoFecha = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                    binding.txtFechaObjetivo.text = formatoFecha.format(fechaInicio)

                    iniciarContador(fechaInicio)
                }
            } else {
                binding.txtFechaObjetivo.text = "Fecha no configurada en BD"
            }
        }
    }

    private fun iniciarContador(fechaObjetivo: Date) {
        countDownTimer?.cancel()

        // Aseguramos que la vista siga existiendo antes de actualizar UI
        if (_binding == null) return

        val tiempoRestante = fechaObjetivo.time - System.currentTimeMillis()
        val textCuenta = binding.txtCuentaRegresiva

        if (tiempoRestante > 0) {
            countDownTimer = object : CountDownTimer(tiempoRestante, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (_binding == null) return // Prevenir crash si el fragment se cierra

                    val dias = millisUntilFinished / (1000 * 60 * 60 * 24)
                    val horas = (millisUntilFinished / (1000 * 60 * 60)) % 24
                    val minutos = (millisUntilFinished / 1000 / 60) % 60
                    val segundos = (millisUntilFinished / 1000) % 60

                    textCuenta.text = String.format(
                        Locale.getDefault(),
                        "%02dd %02dh %02dm %02ds",
                        dias, horas, minutos, segundos
                    )
                }

                override fun onFinish() {
                    if (_binding != null) {
                        textCuenta.text = "¡El semestre ha comenzado! 🎓"
                    }
                }
            }.start()
        } else {
            textCuenta.text = "¡El semestre ha comenzado! 🎓"
        }
    }

    // --- 🔹 Configuración de Vistas Original ---
    private fun setupRecyclerView() {
        binding.recyclerEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterEventos
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
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

    // --- 🔹 Operaciones de Base de Datos Originales ---
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
                if (_binding != null) {
                    Toast.makeText(requireContext(), "Error al cargar eventos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

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

    // --- 🔹 PANEL ADMIN: Editar Semestre ---
    private fun mostrarDialogoEditarSemestre() {
        val calendario = Calendar.getInstance()

        // 1. Mostrar Selector de Fecha
        DatePickerDialog(requireContext(), { _, anio, mes, dia ->
            calendario.set(Calendar.YEAR, anio)
            calendario.set(Calendar.MONTH, mes)
            calendario.set(Calendar.DAY_OF_MONTH, dia)

            // 2. Mostrar Selector de Hora inmediatamente después
            TimePickerDialog(requireContext(), { _, hora, minuto ->
                calendario.set(Calendar.HOUR_OF_DAY, hora)
                calendario.set(Calendar.MINUTE, minuto)
                calendario.set(Calendar.SECOND, 0)

                // 3. Pedir el nombre del semestre
                pedirNombreSemestre(calendario.time)

            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), false).show()

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pedirNombreSemestre(nuevaFecha: Date) {
        val input = EditText(requireContext()).apply {
            hint = "Ejemplo: 2026-I o B-2026"
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nombre del Semestre")
            .setMessage("Define cómo se llamará este nuevo periodo:")
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    guardarSemestreEnNube(nuevaFecha, nombre)
                } else {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun guardarSemestreEnNube(fecha: Date, nombre: String) {
        val datosSemestre = hashMapOf(
            "fecha_inicio" to com.google.firebase.Timestamp(fecha),
            "nombre_semestre" to nombre
        )

        // Hacemos update al documento único de configuración
        db.collection("configuracion").document("semestre")
            .set(datosSemestre)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "¡Reloj actualizado para toda la UNET!", Toast.LENGTH_LONG).show()
                // Nota: No necesitamos llamar a iniciarContador() porque el addSnapshotListener 
                // que creamos antes detectará este cambio en la BD y actualizará la UI automáticamente.
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        countDownTimer?.cancel()
        _binding = null
    }
}
