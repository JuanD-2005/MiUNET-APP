package com.example.miunet01.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miunet01.databinding.FragmentChatbotBinding
import com.google.android.material.snackbar.Snackbar

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: ChatAdapter

    // 💬 Base de conocimiento avanzada (definida globalmente para usar también en sugerencias)
    private val baseConocimientoAvanzada = mapOf(
        // 🔹 SALUDOS Y PRESENTACIÓN
        listOf("hola", "holi", "hey", "buenas", "saludos", "qué tal", "hi", "hello") to listOf(
            "¡Hola! 👋 Soy tu asistente virtual de MiUNET. ¿En qué puedo ayudarte hoy?",
            "¡Hola! 😊 Estoy aquí para orientarte sobre la UNET. ¿Qué necesitas saber?",
            "¡Buenas! 🎓 Soy el chatbot de MiUNET. ¿Tienes alguna pregunta sobre la universidad?"
        ),

        listOf("quién eres", "qué eres", "tu nombre", "te llamas", "presentate", "identificate") to listOf(
            "Soy el asistente virtual de MiUNET 🤖, tu guía para información universitaria. Puedo ayudarte con ubicaciones, horarios, trámites y más.",
            "¡Soy tu compañero virtual de la UNET! 🎯 Estoy aquí para resolver tus dudas sobre departamentos, servicios y vida universitaria.",
            "Me llamo Asistente UNET 😄, tu aliado digital para navegar por la universidad. ¿En qué te ayudo?"
        ),

        // 🔹 DEPARTAMENTOS ACADÉMICOS
        listOf("ciencias de la salud", "salud", "medicina", "enfermería") to listOf(
            "El Departamento de Ciencias de la Salud 💊 está en el edificio B. Atiende de lunes a jueves de 8:00 a.m. a 12:00 p.m. Sigue el pasillo del teatro hacia el edificio B, puerta a mano izquierda.",
            "Para Ciencias de la Salud 🩺: edificio B, lunes a jueves 8:00 AM - 12:00 PM. ¡Es fácil de encontrar!",
            "¿Buscas Ciencias de la Salud? 🏥 Está en el edificio B, horario de 8:00 AM a 12:00 PM de lunes a jueves."
        ),

        listOf("psicología", "psicologo", "psicóloga", "departamento psicología") to listOf(
            "El Departamento de Psicología 🧠 tiene horario rotativo de 8:00 a.m. a 1:00 p.m. Lo encuentras frente al Laboratorio de Agroecología en el pasillo del Teatro B.",
            "Psicología 🧘‍♂️ atiende con horario rotativo (8:00 AM - 1:00 PM) frente al Laboratorio de Agroecología.",
            "Para Psicología: horario flexible de 8:00 AM a 1:00 PM, ubicado frente al Laboratorio de Agroecología."
        ),

        listOf("informatica", "computación", "sistemas", "programacion") to listOf(
            "El Departamento de Informática 💻 atiende de lunes a jueves de 8:30 a.m. a 12:30 p.m. En el pasillo derecho del edificio C, mano izquierda.",
            "Informática 🖥️: edificio C, pasillo derecho, lunes a jueves 8:30 AM - 12:30 PM.",
            "¿Buscas Informática? 💾 Está en el edificio C, horario de 8:30 AM a 12:30 PM de lunes a jueves."
        ),

        listOf("química", "laboratorio química", "departamento química") to listOf(
            "El Departamento de Química 🧪 abre martes y miércoles de 8:00 a.m. a 11:30 a.m. Arriba de la capilla (2 pisos), frente al Decanato de Investigación.",
            "Química 🔬: martes y miércoles 8:00 AM - 11:30 AM, arriba de la capilla, frente a Investigación.",
            "Para Química: horario martes y miércoles 8:00-11:30 AM, ubicación arriba de la capilla."
        ),

        listOf("electrónica", "ingeniería electrónica", "electronic", "circuitos") to listOf(
            "Ingeniería Electrónica ⚡ de lunes a jueves de 8:00 a.m. a 1:00 p.m. A mano izquierda después del Decanato de Investigación.",
            "Electrónica 🔌: lunes a jueves 8:00 AM - 1:00 PM, después del Decanato de Investigación.",
            "Departamento de Electrónica: horario extendido de 8:00 AM a 1:00 PM, cerca de Investigación."
        ),

        listOf("civil", "ingeniería civil", "estructuras", "construcción") to listOf(
            "Ingeniería Civil 🏗️ atiende de lunes a miércoles de 8:00 a.m. a 12:00 p.m. En el pasillo frente a la capilla, mano izquierda.",
            "Civil 🏢: lunes a miércoles 8:00 AM - 12:00 PM, pasillo frente a la capilla.",
            "Para Ingeniería Civil: horario de 8:00 AM a 12:00 PM, ubicación frente a la capilla."
        ),

        listOf("música", "licenciatura en música", "instrumentos", "arte") to listOf(
            "El Departamento de Música 🎵 abre de lunes a miércoles de 8:00 a.m. a 12:00 p.m. Al final del pasillo después de las fotocopias del B, antes de las escaleras al cafetín.",
            "Música 🎶: lunes a miércoles 8:00 AM - 12:00 PM, después de las fotocopias del edificio B.",
            "¿Buscas Música? 🎸 Está cerca del cafetín del B, horario de 8:00 AM a 12:00 PM."
        ),

        listOf("industrial", "ingeniería industrial", "procesos", "producción") to listOf(
            "Ingeniería Industrial 🏭 abre de lunes a jueves de 8:00 a.m. a 12:30 p.m. Parte posterior del B, subiendo tres pisos después del cafetín.",
            "Industrial ⚙️: lunes a jueves 8:00 AM - 12:30 PM, parte posterior del edificio B.",
            "Para Industrial: sube 3 pisos después del cafetín del B, horario 8:00 AM - 12:30 PM."
        ),

        listOf("agronómica", "ingeniería agronómica", "agro", "agricultura") to listOf(
            "Ingeniería Agronómica 🌱 atiende de lunes a viernes de 8:30 a.m. a 12:00 p.m. Parte posterior del B, cinco pisos arriba del cafetín.",
            "Agronómica 🚜: lunes a viernes 8:30 AM - 12:00 PM, 5 pisos arriba del cafetín B.",
            "Departamento de Agronómica: horario completo semana, ubicación alta en edificio B."
        ),

        listOf("matemática", "física", "matemática y física", "cálculo") to listOf(
            "Matemática y Física 📐 abre de lunes a jueves de 8:30 a.m. a 12:00 p.m. Parte posterior del B, cuatro pisos arriba del cafetín.",
            "Matemática/Física 📊: lunes a jueves 8:30 AM - 12:00 PM, 4 pisos arriba del cafetín B.",
            "Para Matemática y Física: sube 4 pisos del cafetín B, horario 8:30 AM - 12:00 PM."
        ),

        listOf("mecánica", "ingeniería mecánica", "mecanica", "talleres") to listOf(
            "Ingeniería Mecánica 🔧 atiende de lunes a jueves de 9:00 a.m. a 12:00 p.m. (jefe), y martes y miércoles 8:00 a.m. a 12:00 p.m. (secretaría). Último piso, pasillo del Departamento de Matemática.",
            "Mecánica 🛠️: horarios variados, consulta específicamente. Ubicado cerca de Matemática.",
            "Departamento de Mecánica: horarios diferenciados, ubicación en último piso del edificio."
        ),

        listOf("ambiental", "ingeniería ambiental", "medio ambiente", "ecología") to listOf(
            "Ingeniería Ambiental 🌍 abre martes y jueves de 9:00 a.m. a 12:30 p.m. Segunda puerta a la derecha del CETI, junto al laboratorio de catalización.",
            "Ambiental 🌿: martes y jueves 9:00 AM - 12:30 PM, cerca del CETI.",
            "Para Ambiental: ubicación específica junto al CETI, horario martes y jueves."
        ),

        listOf("arquitectura", "diseño", "construcción", "planos") to listOf(
            "Arquitectura 🏛️ atiende lunes, miércoles y jueves de 8:30 a.m. a 12:00 p.m. Arriba de la capilla (2 pisos), mano derecha, segunda puerta derecha.",
            "Arquitectura 📐: lunes, miércoles y jueves 8:30 AM - 12:00 PM, arriba de la capilla.",
            "Departamento de Arquitectura: horario específico 3 días semana, ubicación sobre capilla."
        ),

        listOf("deportivo", "tsu deportivo", "deporte", "educación física") to listOf(
            "El TSU Deportivo ⚽ funciona de lunes a viernes (según carga horaria). A mano derecha de la entrada principal, al lado de Admisión.",
            "Deportivo 🏃: horario variable según carga, cerca de la entrada principal.",
            "Para TSU Deportivo: ubicación estratégica entrada principal, horario flexible."
        ),

        // 🔹 DECANATOS Y SERVICIOS ADMINISTRATIVOS
        listOf("decanato de extensión", "extensión", "cursos", "talleres") to listOf(
            "El Decanato de Extensión 📚 trabaja de lunes a viernes de 8:00 a.m. a 12:00 p.m. Al lado de la Brigada, frente al estacionamiento del A.",
            "Extensión 🎨: lunes a viernes 8:00 AM - 12:00 PM, cerca del estacionamiento A.",
            "Decanato de Extensión: horario completo semana, ubicación conveniente estacionamiento."
        ),

        listOf("decanato de investigación", "investigación", "proyectos", "ciencia") to listOf(
            "El Decanato de Investigación 🔬 atiende de lunes a jueves de 8:00 a.m. a 12:00 p.m. Arriba de la capilla, frente al Departamento de Química.",
            "Investigación 🧪: lunes a jueves 8:00 AM - 12:00 PM, arriba de la capilla.",
            "Para Investigación: ubicación sobre capilla, horario de lunes a jueves."
        ),

        listOf("bienestar estudiantil", "bienestar", "apoyo", "consejería") to listOf(
            "Bienestar Estudiantil 💙 atiende todos los días de 8:00 a.m. a 11:30 a.m. (por turnos). En el pasillo detrás del CETI.",
            "Bienestar 🤗: horario rotativo 8:00 AM - 11:30 AM, detrás del CETI.",
            "Bienestar Estudiantil: apoyo continuo, ubicación trasera del CETI."
        ),

        listOf("formación permanente", "formación", "cursos libres", "educación continua") to listOf(
            "Formación Permanente 📖 trabaja de lunes a sábado de 8:00 a.m. a 2:00 p.m. En el Hall del A a mano izquierda.",
            "Formación Permanente 🎓: amplio horario incluyendo sábados, Hall del edificio A.",
            "Para Formación Permanente: horario extendido, ubicación principal Hall A."
        ),

        // 🔹 SERVICIOS GENERALES
        listOf("impresiones", "imprimir", "copias", "print") to listOf(
            "El servicio de Impresiones 🖨️ abre de lunes a viernes de 8:30 a.m. a 1:00 p.m. Afuera del A, a mano derecha del Hall.",
            "Impresiones 📄: lunes a viernes 8:30 AM - 1:00 PM, exterior edificio A.",
            "Para imprimir: servicio en exterior del A, horario matutino."
        ),

        listOf("fotocopias", "fotocopiado", "copiar", "xerox") to listOf(
            "Las fotocopias del B 📋 funcionan de lunes a viernes de 7:45 a.m. a 3:00 p.m. Subiendo la capilla en el edificio B.",
            "Fotocopias B 📊: amplio horario 7:45 AM - 3:00 PM, edificio B cerca capilla.",
            "Servicio de fotocopias: ubicación en B, horario casi completo día."
        ),

        listOf("biblioteca", "libros", "estudio", "lectura", "préstamo") to listOf(
            "La biblioteca 📚 abre lunes, martes y jueves de 7:30 a.m. a 12:00 p.m. En el edificio frente al B.",
            "Biblioteca 📖: lunes, martes y jueves 7:30 AM - 12:00 PM, frente al edificio B.",
            "Para la biblioteca: horario específico 3 días, ubicación frente al B."
        ),

        listOf("cafetín", "cafetería", "comida", "almuerzo", "refrigerio", "café") to listOf(
            "Tenemos varios cafetines ☕:\n• Cafetín A: Lunes a jueves 6:00 AM - 4:30 PM, Viernes y sábado 6:00 AM - 5:30 PM\n• Cafetín B: Lunes a viernes 7:00 AM - 12:00 PM\n• Cafetín C: Lunes, martes y jueves 7:30 AM - 12:00 PM",
            "Opciones de cafetín 🍽️:\n- A: Horario extendido\n- B: Horario matutino\n- C: Horario específico",
            "Servicios de alimentación 🥪:\n• Edificio A: Más horario\n• Edificio B: Solo mañanas\n• Edificio C: Días específicos"
        ),

        // 🔹 TRÁMITES ACADÉMICOS
        listOf("retirar materias", "retiro", "drop", "cancelar materia") to listOf(
            "Retirar materias 📝 no afecta tu índice académico. Debes hacerlo en Control de Estudios dentro del plazo establecido.",
            "El retiro de materias ✅ es sin penalización al índice. Consulta fechas en Control de Estudios.",
            "Para retirar materias: proceso sin afectar promedio, realiza en Control de Estudios."
        ),

        listOf("inscripciones", "inscribir", "matricular", "registro") to listOf(
            "Las inscripciones 🗓️ se realizan una semana antes del inicio del semestre, por orden de promedio. Consulta fechas exactas en Control de Estudios.",
            "Proceso de inscripción 📅: una semana antes del semestre, ordenado por promedio.",
            "Para inscribirte: una semana antes del inicio, sistema por promedio académico."
        ),

        listOf("constancia", "certificado", "documento", "record") to listOf(
            "Para constancias 📄 dirígete a Control de Estudios en el edificio A, planta baja. Horario de atención general.",
            "Constancias y certificados 📑: solicita en Control de Estudios, edificio A.",
            "Documentos académicos: Control de Estudios es tu lugar, edificio A planta baja."
        ),

        listOf("horario", "clases", "cursos", "asignaturas") to listOf(
            "Los horarios de clases 🕐 varían por carrera y semestre. Consulta con tu coordinador de programa o en Control de Estudios.",
            "Para horarios específicos 📆: contacta a tu departamento o Control de Estudios.",
            "Horarios académicos: información específica por carrera en cada departamento."
        ),

        // 🔹 INFORMACIÓN GENERAL
        listOf("ubicación", "dirección", "dónde queda", "cómo llegar") to listOf(
            "La UNET 🏛️ se encuentra en la Avenida Universidad, Sector Paramillo, San Cristóbal - Táchira. ¡Es fácil de encontrar!",
            "Ubicación UNET 🗺️: Avenida Universidad, Paramillo, San Cristóbal. Punto de referencia conocido.",
            "La universidad está en Av. Universidad, Sector Paramillo. ¡Todos conocen el lugar!"
        ),

        listOf("contacto", "teléfono", "email", "correo", "comunicarse") to listOf(
            "Para contactos específicos 📞, te recomiendo dirigirte al departamento correspondiente. La información general suele manejarse por cada unidad académica.",
            "Contactos 📧: varían por departamento. ¿De qué área necesitas información?",
            "Comunicación: cada departamento tiene sus propios canales. ¿Qué área te interesa?"
        ),

        // 🔹 AGRADECIMIENTOS Y DESPEDIDAS
        listOf("gracias", "thank you", "merci", "agradecido", "agradecida") to listOf(
            "¡De nada! 😊 ¿Necesitas algo más? Estoy aquí para ayudarte.",
            "¡No hay problema! 🎓 ¡Feliz de poder ayudarte! ¿Algo más en lo que te oriente?",
            "¡Con gusto! 🤗 Si tienes más preguntas, aquí estoy."
        ),

        listOf("adiós", "chao", "bye", "hasta luego", "nos vemos", "hasta pronto") to listOf(
            "¡Hasta luego! 👋 ¡Que tengas un excelente día en la UNET!",
            "¡Chao! 😄 ¡Éxito en tus actividades universitarias!",
            "¡Nos vemos! 🎯 ¡Buena suerte con tus trámites y estudios!"
        ),


        )


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)

        // 🔹 Inicializar RecyclerView
        adapter = ChatAdapter(mensajes)
        binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChat.adapter = adapter

        // 🔹 Mensaje inicial del bot
        agregarMensaje("¡Hola! 👋 Soy el asistente de MiUNET.\nPuedes preguntarme sobre los servicios, departamentos o trámites de la UNET.", false)

        // --- SUGERENCIAS DINÁMICAS PARA EL CHATBOT ---
        val sugerenciasBase = listOf(
            "¿Dónde queda el comedor?",
            "Horario del cafetín del A",
            "¿Dónde está Control de Estudios?",
            "Horario de la biblioteca",
            "¿Qué hace Bienestar Estudiantil?",
            "¿Dónde queda el Decanato de Investigación?",
            "¿Dónde puedo imprimir?",
            "Horario del Cafetín del B",
            "¿Cuándo son las inscripciones?",
            "¿Retirar materias afecta el índice?"
        )

        // 2️⃣ Combinamos sugerencias base + claves del conocimiento
        val sugerenciasTotales = (sugerenciasBase + baseConocimientoAvanzada.keys.flatten().map {
            "Información sobre ${it.replaceFirstChar { c -> c.uppercase() }}"
        }).distinct().sorted()

        // 3️⃣ Adaptador de autocompletado
        val adapterSugerencias = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerenciasTotales)

        // 4️⃣ Configuramos el AutoCompleteTextView
        val inputAuto = binding.inputMensaje as AutoCompleteTextView
        inputAuto.setAdapter(adapterSugerencias)
        inputAuto.threshold = 1 // Mostrar desde el primer carácter

        // 5️⃣ Mostrar sugerencias al enfocar
        inputAuto.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputAuto.showDropDown()
        }

        // 6️⃣ Refrescar sugerencias mientras el usuario escribe
        inputAuto.addTextChangedListener { editable ->
            val texto = editable?.toString()?.lowercase()?.trim() ?: ""
            val filtradas = sugerenciasTotales.filter { it.lowercase().contains(texto) }
            adapterSugerencias.clear()
            adapterSugerencias.addAll(filtradas)
            adapterSugerencias.notifyDataSetChanged()
            if (filtradas.isNotEmpty()) inputAuto.showDropDown()
        }

        // 🔹 Enviar mensaje
        binding.btnEnviar.setOnClickListener {
            val pregunta = binding.inputMensaje.text.toString().trim()
            if (pregunta.isNotEmpty()) {
                agregarMensaje(pregunta, true)
                binding.inputMensaje.text?.clear()
                responder(pregunta)
            } else {
                Snackbar.make(binding.root, "Escribe una pregunta antes de enviar ✍️", Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    // 🔹 Agregar mensaje
    private fun agregarMensaje(texto: String, esUsuario: Boolean) {
        mensajes.add(Mensaje(texto, esUsuario))
        adapter.notifyItemInserted(mensajes.size - 1)
        binding.recyclerChat.scrollToPosition(mensajes.size - 1)
    }

    // 🔹 Generar respuesta
    private fun responder(pregunta: String) {
        agregarMensaje("🤔 Pensando...", false)

        binding.recyclerChat.postDelayed({
            mensajes.removeAt(mensajes.size - 1)
            adapter.notifyItemRemoved(mensajes.size)

            val respuestaBase = obtenerRespuesta(pregunta)
            val introducciones = listOf(
                "Claro 😊, ", "Por supuesto 👍, ", "Te cuento 👇 ",
                "Sí, mira 👇 ", "Con gusto 💡, ", "Perfecto, aquí tienes 📘 ",
                "Te explico brevemente 💬 ", "Buena pregunta 👏, ",
                "Sin problema 😉, ", "Esto te puede ayudar 👉 "
            )
            val respuestaNatural = introducciones.random() + respuestaBase
            agregarMensaje(respuestaNatural, false)
        }, 900)
    }

    // 🔹 Buscar respuesta
    private fun obtenerRespuesta(pregunta: String): String {
        val p = pregunta.lowercase().trim()
        for ((claves, respuestas) in baseConocimientoAvanzada) {
            if (claves.any { p.contains(it) }) return respuestas.random()
        }
        return listOf(
            "Lo siento 😅, no encontré esa información. ¿Podrías reformular la pregunta?",
            "No tengo esa información aún, pero pronto sabré más.",
            "No estoy seguro de eso 🤔, intenta preguntar sobre trámites, biblioteca o comedor."
        ).random()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
