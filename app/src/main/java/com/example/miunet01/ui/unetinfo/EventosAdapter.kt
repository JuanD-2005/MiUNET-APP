package com.example.miunet01.ui.unetinfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class EventosAdapter(
    private var eventos: List<Evento>,
    private val userRole: String, // 🔹 Recibimos el rol del usuario
    private val onEditarClick: (Evento) -> Unit // 🔹 Callback para editar
) : RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.txt_titulo)
        val fecha: TextView = itemView.findViewById(R.id.txt_fecha)
        val lugar: TextView = itemView.findViewById(R.id.txt_lugar)
        val descripcion: TextView = itemView.findViewById(R.id.txt_descripcion)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarEvento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.titulo.text = evento.titulo
        holder.fecha.text = evento.fecha
        holder.lugar.text = evento.lugar
        holder.descripcion.text = evento.descripcion

        // 🔹 Mostrar botón solo si el usuario es Admin
        if (userRole == "Admin") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(evento) }
        } else {
            holder.btnEditar?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = eventos.size

    fun actualizarLista(nuevaLista: List<Evento>) {
        eventos = nuevaLista
        notifyDataSetChanged()
    }
}

