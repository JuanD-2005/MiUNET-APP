package com.example.miunet01.ui.tramites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class ServiciosAdapter(
    private var servicios: MutableList<Servicio>,
    private val userRole: String,
    private val onEditarClick: (Servicio) -> Unit,
    private val onEliminarClick: (Servicio) -> Unit
) : RecyclerView.Adapter<ServiciosAdapter.ServicioViewHolder>() {

    class ServicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreServicio)
        val descripcion: TextView = itemView.findViewById(R.id.txtDescripcionServicio)
        val precio: TextView = itemView.findViewById(R.id.txtPrecioServicio)
        val horario: TextView = itemView.findViewById(R.id.txtHorarioServicio)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarServicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        return ServicioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicioViewHolder, position: Int) {
        val s = servicios[position]
        holder.nombre.text = s.nombre
        holder.descripcion.text = "Descripción: ${s.descripcion}"
        holder.precio.text = "Precio: ${s.precio}"
        holder.horario.text = "Horario: ${s.horario}"

        // Solo los roles con permisos ven los botones o acciones
        if (userRole == "Admin" || userRole == "Profesor") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(s) }

            // Pulsación larga para eliminar
            holder.itemView.setOnLongClickListener {
                onEliminarClick(s)
                true
            }
        } else {
            holder.btnEditar?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = servicios.size
}

