package com.example.miunet01.ui.tramites

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miunet01.R
import com.example.miunet01.databinding.FragmentTramitesBinding
import com.google.firebase.firestore.FirebaseFirestore

class TramitesFragment : Fragment() {

    private var _binding: FragmentTramitesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore

    private val listaDepartamentos = mutableListOf<Departamento>()
    private val listaServicios = mutableListOf<Servicio>()
    private val listaAutoridades = mutableListOf<Autoridad>()
    private val listaDecanatos = mutableListOf<Decanato>()
    private val listaInteres = mutableListOf<Interes>()

    private lateinit var adapterDepartamentos: DepartamentosAdapter
    private lateinit var adapterServicios: ServiciosAdapter
    private lateinit var adapterAutoridades: AutoridadesAdapter
    private lateinit var adapterDecanatos: DecanatosAdapter
    private lateinit var adapterInteres: InteresAdapter

    // Track de primera carga para carga diferida
    private var isDepartamentosLoaded = false
    private var isServiciosLoaded = false
    private var isAutoridadesLoaded = false
    private var isDecanatosLoaded = false
    private var isInteresLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTramitesBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        // 🔹 Obtener rol desde SharedPreferences
        val prefs = requireContext().getSharedPreferences("MiUNET_PREFS", AppCompatActivity.MODE_PRIVATE)
        val userRole = prefs.getString("USER_ROLE", "Estudiante") ?: "Estudiante"

        // 🔹 Inicializar adapters
        initializeAdapters(userRole)

        // 🔹 Configurar RecyclerViews
        setupRecyclerViews()

        // 🔹 Configurar secciones colapsables
        setupCollapsibleSections()

        // 🔹 Cargar datos iniciales (solo secciones expandidas por defecto)
        loadInitialData()


// En onCreateView, después de configurar el FAB
        if (userRole == "Admin") {
            binding.btnAgregar.visibility = View.VISIBLE
            binding.btnAgregar.setOnClickListener {
                mostrarDialogoAgregar()
            }
            Log.d("FAB_Debug", "FAB configurado para Admin - Visible")
        } else {
            binding.btnAgregar.visibility = View.GONE
            Log.d("FAB_Debug", "FAB oculto - Rol: $userRole")
        }

// También verifica si el FAB es null
        if (binding.btnAgregar == null) {
            Log.e("FAB_Debug", "FAB es null - Revisa el XML")
        } else {
            Log.d("FAB_Debug", "FAB encontrado en binding")
        }


        return binding.root
    }

    private fun mostrarDialogoAgregar() {
        // Determinar qué sección está visible
        val seccionActiva = when {
            binding.cardDepartamentos.visibility == View.VISIBLE -> "Departamentos"
            binding.cardServicios.visibility == View.VISIBLE -> "servicios"
            binding.cardAutoridades.visibility == View.VISIBLE -> "autoridades"
            binding.cardDecanatos.visibility == View.VISIBLE -> "decanatos"
            binding.cardInteres.visibility == View.VISIBLE -> "interes"
            else -> null
        }

        if (seccionActiva == null) {
            Toast.makeText(requireContext(), "Primero expande una sección para agregar contenido", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarDialogoAgregarEntidad(seccionActiva)
    }

    private fun mostrarDialogoAgregarEntidad(coleccion: String) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_info, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val editHorario = dialogView.findViewById<EditText>(R.id.editHorario)
        val editUbicacion = dialogView.findViewById<EditText>(R.id.editUbicacion)
        val editDescripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)

        // Configurar hint específicos según la colección
        when (coleccion) {
            "servicios" -> {
                editHorario.hint = "Ej: Lunes a Viernes 8:00-16:00"
                editUbicacion.hint = "Ej: Edificio A, Planta Baja"
                editDescripcion.hint = "Ej: Trámite de constancias"
            }
            "autoridades" -> {
                editHorario.hint = "Ej: Horario de atención"
                editUbicacion.hint = "Ej: Despacho del Rectorado"
                editDescripcion.hint = "Ej: Rector de la UNET"
            }
            // Agrega más casos según necesites
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar a ${coleccion.replaceFirstChar { it.uppercase() }}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val campos = hashMapOf<String, Any>(
                    "nombre" to editNombre.text.toString().trim(),
                    "horario" to editHorario.text.toString().trim(),
                    "ubicacion" to editUbicacion.text.toString().trim(),
                    "descripcion" to editDescripcion.text.toString().trim()
                )

                val nombre = campos["nombre"] as String

                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Validar que no exista ya un documento con ese nombre
                db.collection(coleccion)
                    .document(nombre)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            Toast.makeText(requireContext(), "Ya existe un elemento con ese nombre", Toast.LENGTH_SHORT).show()
                        } else {
                            // Guardar el nuevo elemento
                            db.collection(coleccion)
                                .document(nombre)
                                .set(campos)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "✅ Elemento agregado", Toast.LENGTH_SHORT).show()
                                    recargarSeccion(coleccion)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun recargarSeccion(coleccion: String) {
        when (coleccion) {
            "Departamentos" -> cargarDepartamentos()
            "servicios" -> cargarServicios()
            "autoridades" -> cargarAutoridades()
            "decanatos" -> cargarDecanatos()
            "interes" -> cargarInteres()
        }
    }


    private fun initializeAdapters(userRole: String) {
        adapterDepartamentos = DepartamentosAdapter(
            listaDepartamentos,
            userRole,
            onEditarClick = { dep ->
                val campos = mutableMapOf<String, Any>(
                    "nombre" to dep.nombre,
                    "horario" to dep.horario,
                    "ubicacion" to dep.ubicacion,
                    "descripcion" to dep.descripcion
                )
                mostrarDialogoEditarEntidad("Departamentos", dep.nombre, campos) {
                    cargarDepartamentos()
                }
            },
            onEliminarClick = { dep ->
                mostrarDialogoEliminarEntidad("Departamentos", dep)
            }
        )

        adapterServicios = ServiciosAdapter(
            listaServicios,
            userRole,
            onEditarClick = { serv ->
                val campos = mutableMapOf<String, Any>(
                    "nombre" to serv.nombre,
                    "descripcion" to serv.descripcion,
                    "precio" to serv.precio,
                    "horario" to serv.horario
                )
                mostrarDialogoEditarEntidad("servicios", serv.nombre, campos) {
                    cargarServicios()
                }
            },
            onEliminarClick = { serv ->
                mostrarDialogoEliminarEntidad("servicios", serv)
            }
        )

        adapterAutoridades = AutoridadesAdapter(
            listaAutoridades,
            userRole,
            onEditarClick = { autoridad ->
                val campos = mutableMapOf<String, Any>(
                    "nombre" to autoridad.nombre,
                    "horario" to autoridad.horario,
                    "ubicacion" to autoridad.ubicacion,
                    "descripcion" to autoridad.descripcion
                )
                mostrarDialogoEditarEntidad("autoridades", autoridad.nombre, campos) {
                    cargarAutoridades()
                }
            },
            onEliminarClick = { autoridad ->
                mostrarDialogoEliminarEntidad("autoridades", autoridad)
            }
        )

        adapterDecanatos = DecanatosAdapter(
            listaDecanatos,
            userRole,
            onEditarClick = { decanato ->
                val campos = mutableMapOf<String, Any>(
                    "nombre" to decanato.nombre,
                    "horario" to decanato.horario,
                    "ubicacion" to decanato.ubicacion,
                    "descripcion" to decanato.descripcion
                )
                mostrarDialogoEditarEntidad("decanatos", decanato.nombre, campos) {
                    cargarDecanatos()
                }
            },
            onEliminarClick = { decanato ->
                mostrarDialogoEliminarEntidad("decanatos", decanato)
            }
        )

        adapterInteres = InteresAdapter(
            listaInteres,
            userRole,
            onEditarClick = { zona ->
                val campos = mutableMapOf<String, Any>(
                    "nombre" to zona.nombre,
                    "horario" to zona.horario,
                    "ubicacion" to zona.ubicacion,
                    "descripcion" to zona.descripcion
                )
                mostrarDialogoEditarEntidad("interes", zona.nombre, campos) {
                    cargarInteres()
                }
            },
            onEliminarClick = { zona ->
                mostrarDialogoEliminarEntidad("interes", zona)
            }
        )
    }
    private fun setupRecyclerViews() {
        binding.recyclerDepartamentos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDepartamentos.adapter = adapterDepartamentos

        binding.recyclerServicios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerServicios.adapter = adapterServicios

        binding.recyclerAutoridades.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAutoridades.adapter = adapterAutoridades

        binding.recyclerDecanatos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDecanatos.adapter = adapterDecanatos

        binding.recyclerInteres.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInteres.adapter = adapterInteres
    }

    private fun setupCollapsibleSections() {
        // Configurar estado inicial explícitamente
        setSectionInitialState(
            header = binding.headerDepartamentos,
            arrow = binding.arrowDepartamentos,
            card = binding.cardDepartamentos,
            isExpanded = true
        )

        setSectionInitialState(
            header = binding.headerServicios,
            arrow = binding.arrowServicios,
            card = binding.cardServicios,
            isExpanded = true
        )

        setSectionInitialState(
            header = binding.headerAutoridades,
            arrow = binding.arrowAutoridades,
            card = binding.cardAutoridades,
            isExpanded = false
        )

        setSectionInitialState(
            header = binding.headerDecanatos,
            arrow = binding.arrowDecanatos,
            card = binding.cardDecanatos,
            isExpanded = false
        )

        setSectionInitialState(
            header = binding.headerInteres,
            arrow = binding.arrowInteres,
            card = binding.cardInteres,
            isExpanded = false
        )

        // Configurar listeners
        setupSectionClickListener(
            header = binding.headerDepartamentos,
            arrow = binding.arrowDepartamentos,
            card = binding.cardDepartamentos,
            sectionType = "departamentos"
        )

        setupSectionClickListener(
            header = binding.headerServicios,
            arrow = binding.arrowServicios,
            card = binding.cardServicios,
            sectionType = "servicios"
        )

        setupSectionClickListener(
            header = binding.headerAutoridades,
            arrow = binding.arrowAutoridades,
            card = binding.cardAutoridades,
            sectionType = "autoridades"
        )

        setupSectionClickListener(
            header = binding.headerDecanatos,
            arrow = binding.arrowDecanatos,
            card = binding.cardDecanatos,
            sectionType = "decanatos"
        )

        setupSectionClickListener(
            header = binding.headerInteres,
            arrow = binding.arrowInteres,
            card = binding.cardInteres,
            sectionType = "interes"
        )
    }

    private fun setSectionInitialState(
        header: android.widget.LinearLayout,
        arrow: android.widget.ImageView,
        card: androidx.cardview.widget.CardView,
        isExpanded: Boolean
    ) {
        if (isExpanded) {
            card.visibility = View.VISIBLE
            arrow.setImageResource(R.drawable.ic_arrow_down)
        } else {
            card.visibility = View.GONE
            arrow.setImageResource(R.drawable.ic_arrow_up)
        }
    }

    private fun setupSectionClickListener(
        header: android.widget.LinearLayout,
        arrow: android.widget.ImageView,
        card: androidx.cardview.widget.CardView,
        sectionType: String
    ) {
        header.setOnClickListener {
            val isExpanded = card.visibility == View.VISIBLE

            if (isExpanded) {
                // Colapsar
                card.visibility = View.GONE
                arrow.setImageResource(R.drawable.ic_arrow_up)

                // Animación de colapso
                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                card.startAnimation(animation)
            } else {
                // Expandir
                card.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.ic_arrow_down)

                // Cargar datos si es necesario
                loadSectionData(sectionType)

                // Animación de expansión
                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
                card.startAnimation(animation)
            }
        }
    }

    private fun loadInitialData() {
        // Cargar solo las secciones que están expandidas por defecto
        if (binding.cardDepartamentos.visibility == View.VISIBLE) {
            loadSectionData("departamentos")
        }
        if (binding.cardServicios.visibility == View.VISIBLE) {
            loadSectionData("servicios")
        }
        // Las demás secciones se cargarán cuando se expandan
    }

    private fun loadSectionData(sectionType: String) {
        when (sectionType) {
            "departamentos" -> {
                if (!isDepartamentosLoaded) {
                    cargarDepartamentos()
                    isDepartamentosLoaded = true
                }
            }
            "servicios" -> {
                if (!isServiciosLoaded) {
                    cargarServicios()
                    isServiciosLoaded = true
                }
            }
            "autoridades" -> {
                if (!isAutoridadesLoaded) {
                    cargarAutoridades()
                    isAutoridadesLoaded = true
                }
            }
            "decanatos" -> {
                if (!isDecanatosLoaded) {
                    cargarDecanatos()
                    isDecanatosLoaded = true
                }
            }
            "interes" -> {
                if (!isInteresLoaded) {
                    cargarInteres()
                    isInteresLoaded = true
                }
            }
        }
    }

    // 🔹 Diálogo genérico para editar cualquier entidad
    private fun mostrarDialogoEditarEntidad(
        coleccion: String,
        nombreEntidad: String,
        campos: MutableMap<String, Any>,
        onSuccess: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_editar_info, null)

        val editNombre = dialogView.findViewById<EditText>(R.id.editNombre)
        val editHorario = dialogView.findViewById<EditText>(R.id.editHorario)
        val editUbicacion = dialogView.findViewById<EditText>(R.id.editUbicacion)
        val editDescripcion = dialogView.findViewById<EditText>(R.id.editDescripcion)

        editNombre.setText(campos["nombre"]?.toString() ?: "")
        editHorario.setText(campos["horario"]?.toString() ?: "")
        editUbicacion.setText(campos["ubicacion"]?.toString() ?: "")
        editDescripcion.setText(campos["descripcion"]?.toString() ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar información")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                campos["nombre"] = editNombre.text.toString()
                campos["horario"] = editHorario.text.toString()
                campos["ubicacion"] = editUbicacion.text.toString()
                campos["descripcion"] = editDescripcion.text.toString()

                db.collection(coleccion)
                    .document(nombreEntidad)
                    .set(campos)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Información actualizada", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔹 Carga de datos de cada colección
    private fun cargarDepartamentos() {
        db.collection("Departamentos").get().addOnSuccessListener { result ->
            listaDepartamentos.clear()
            for (doc in result) listaDepartamentos.add(doc.toObject(Departamento::class.java))
            adapterDepartamentos.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar departamentos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarServicios() {
        db.collection("servicios").get().addOnSuccessListener { result ->
            listaServicios.clear()
            for (doc in result) listaServicios.add(doc.toObject(Servicio::class.java))
            adapterServicios.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar servicios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarAutoridades() {
        db.collection("autoridades").get().addOnSuccessListener { result ->
            listaAutoridades.clear()
            for (doc in result) listaAutoridades.add(doc.toObject(Autoridad::class.java))
            adapterAutoridades.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar autoridades", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDecanatos() {
        db.collection("decanatos").get().addOnSuccessListener { result ->
            listaDecanatos.clear()
            for (doc in result) listaDecanatos.add(doc.toObject(Decanato::class.java))
            adapterDecanatos.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar decanatos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarInteres() {
        db.collection("interes").get().addOnSuccessListener { result ->
            listaInteres.clear()
            for (doc in result) listaInteres.add(doc.toObject(Interes::class.java))
            adapterInteres.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error al cargar zonas de interés", Toast.LENGTH_SHORT).show()
        }
    }


    private fun <T> mostrarDialogoEliminarEntidad(coleccion: String, entidad: T) {
        val nombre = when (entidad) {
            is Departamento -> entidad.nombre
            is Servicio -> entidad.nombre
            is Autoridad -> entidad.nombre
            is Decanato -> entidad.nombre
            is Interes -> entidad.nombre
            else -> return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar registro")
            .setMessage("¿Deseas eliminar '$nombre' de $coleccion?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarEntidad(coleccion, nombre)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarEntidad(coleccion: String, nombre: String) {
        db.collection(coleccion)
            .document(nombre)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "✅ Eliminado correctamente", Toast.LENGTH_SHORT).show()
                // Recargar la sección correspondiente
                when (coleccion) {
                    "Departamentos" -> {
                        listaDepartamentos.removeAll { it.nombre == nombre }
                        adapterDepartamentos.notifyDataSetChanged()
                    }
                    "servicios" -> {
                        listaServicios.removeAll { it.nombre == nombre }
                        adapterServicios.notifyDataSetChanged()
                    }
                    "autoridades" -> {
                        listaAutoridades.removeAll { it.nombre == nombre }
                        adapterAutoridades.notifyDataSetChanged()
                    }
                    "decanatos" -> {
                        listaDecanatos.removeAll { it.nombre == nombre }
                        adapterDecanatos.notifyDataSetChanged()
                    }
                    "interes" -> {
                        listaInteres.removeAll { it.nombre == nombre }
                        adapterInteres.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}