package com.example.miunet01.ui.chatbot

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.miunet01.databinding.FragmentChatbotBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val mensajes = mutableListOf<Mensaje>()
    private lateinit var adapter: ChatAdapter

    // 🌐 Nuestro cliente HTTP para hablar con FastAPI
    private val client = OkHttpClient()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)

        // 🔹 Inicializar RecyclerView
        adapter = ChatAdapter(mensajes)
        binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChat.adapter = adapter

        // 🔹 Mensaje inicial del bot
        agregarMensaje("¡Hola! 👋 Soy el asistente inteligente de MiUNET.\nPuedes preguntarme sobre los servicios, departamentos o trámites de la UNET.", false)

        // --- SUGERENCIAS FRECUENTES PARA EL CHATBOT ---
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
        ).sorted()

        // Adaptador nativo de Android
        val adapterSugerencias = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sugerenciasBase)
        val inputAuto = binding.inputMensaje as AutoCompleteTextView

        inputAuto.setAdapter(adapterSugerencias)
        inputAuto.threshold = 1 // Mostrar desde la primera letra

        // Mostrar lista al tocar la caja
        inputAuto.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) inputAuto.showDropDown()
        }

        // Enviar la pregunta automáticamente al tocar la sugerencia
        inputAuto.setOnItemClickListener { parent, _, position, _ ->
            val preguntaSeleccionada = parent.getItemAtPosition(position).toString()
            agregarMensaje(preguntaSeleccionada, true)
            binding.inputMensaje.text?.clear()
            
            // Ocultar el teclado suavemente
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            
            responder(preguntaSeleccionada)
        }

        // 🔹 Enviar mensaje (Botón manual)
        binding.btnEnviar.setOnClickListener {
            val pregunta = binding.inputMensaje.text.toString().trim()
            if (pregunta.isNotEmpty()) {
                agregarMensaje(pregunta, true)
                binding.inputMensaje.text?.clear()

                // Ocultar el teclado suavemente
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.root.windowToken, 0)

                responder(pregunta)
            } else {
                Snackbar.make(binding.root, "Escribe una pregunta antes de enviar ✍️", Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    // 🔹 Agregar mensaje a la UI
    private fun agregarMensaje(texto: String, esUsuario: Boolean) {
        mensajes.add(Mensaje(texto, esUsuario))
        adapter.notifyItemInserted(mensajes.size - 1)
        binding.recyclerChat.smoothScrollToPosition(mensajes.size - 1)
    }

    // 🔹 Generar respuesta conectando con Gemini vía FastAPI
    private fun responder(pregunta: String) {
        // 1. Feedback visual de carga
        agregarMensaje("🤔 Pensando...", false)

        // 2. Lanzar corrutina en segundo plano (Dispatchers.IO) para no congelar la pantalla
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Preparamos el cuerpo de la petición (JSON)
                val jsonBody = JSONObject().apply {
                    put("pregunta", pregunta)
                }.toString()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toRequestBody(mediaType)

                // Apuntamos al servidor de producción en Render
                val request = Request.Builder()
                    .url("https://miunet-app.onrender.com/api/chat")
                    .post(requestBody)
                    .build()

                // Disparamos la petición
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    // Parseamos la respuesta de FastAPI
                    val jsonResponse = JSONObject(responseData)
                    val respuestaIA = jsonResponse.getString("respuesta")

                    // Volvemos al hilo principal (UI) para actualizar la pantalla
                    withContext(Dispatchers.Main) {
                        if (mensajes.isNotEmpty() && mensajes.last().texto == "🤔 Pensando...") {
                            mensajes.removeAt(mensajes.size - 1)
                            adapter.notifyItemRemoved(mensajes.size)
                        }
                        agregarMensaje(respuestaIA, false)
                    }
                } else {
                    throw Exception("Error del servidor: ${response.code}")
                }

            } catch (e: Exception) {
                e.printStackTrace() // Para ver el error exacto en tu Logcat de Android Studio

                // Volvemos al hilo principal para mostrar el error al usuario
                withContext(Dispatchers.Main) {
                    // Limpiamos el mensaje de "Pensando..."
                    if (mensajes.isNotEmpty() && mensajes.last().texto == "🤔 Pensando...") {
                        mensajes.removeAt(mensajes.size - 1)
                        adapter.notifyItemRemoved(mensajes.size)
                    }
                    agregarMensaje("Ups, hay un problema de conexión con la central. Intenta más tarde. 😅", false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
