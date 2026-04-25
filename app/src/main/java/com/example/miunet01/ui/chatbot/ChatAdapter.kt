package com.example.miunet01.ui.chatbot

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class ChatAdapter(private val mensajes: List<Mensaje>) :
    RecyclerView.Adapter<ChatAdapter.MensajeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mensaje, parent, false)
        return MensajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val mensaje = mensajes[position]
        holder.bind(mensaje)
    }

    override fun getItemCount(): Int = mensajes.size

    inner class MensajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtMensaje: TextView = view.findViewById(R.id.txtMensaje)
        private val messageContainer: LinearLayout = view.findViewById(R.id.messageContainer)

        fun bind(mensaje: Mensaje) {
            txtMensaje.text = mensaje.texto

            if (mensaje.esUsuario) {
                // 🔹 MENSAJE DEL ESTUDIANTE (Derecha, Azul, Texto Blanco)
                messageContainer.gravity = Gravity.END
                txtMensaje.setBackgroundResource(R.drawable.bg_mensaje_usuario)
                txtMensaje.setTextColor(android.graphics.Color.WHITE)
            } else {
                // 🔹 MENSAJE DE LA IA (Izquierda, Gris/Blanco, Texto Negro)
                messageContainer.gravity = Gravity.START
                txtMensaje.setBackgroundResource(R.drawable.bg_mensaje_bot)
                txtMensaje.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}
