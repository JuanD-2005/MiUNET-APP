package com.example.miunet01.ui.tramites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class AutoridadesAdapter(
    private var autoridades: List<Autoridad>,
    private val userRole: String,
    private val onEditarClick: (Autoridad) -> Unit,
    private val onEliminarClick: (Autoridad) -> Unit
) : RecyclerView.Adapter<AutoridadesAdapter.AutoridadViewHolder>() {

    class AutoridadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreDepto)
        val horario: TextView = itemView.findViewById(R.id.txtHorarioDepto)
        val ubicacion: TextView = itemView.findViewById(R.id.txtUbicacionDepto)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarDepto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoridadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departamento, parent, false)
        return AutoridadViewHolder(view)
    }

    override fun onBindViewHolder(holder: AutoridadViewHolder, position: Int) {
        val a = autoridades[position]
        holder.nombre.text = a.nombre
        holder.horario.text = "Horario: ${a.horario}"
        holder.ubicacion.text = "Ubicación: ${a.ubicacion}"

        if (userRole == "Admin" || userRole == "Profesor") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(a) }


            // Long click para eliminar
            holder.itemView.setOnLongClickListener {
                onEliminarClick(a)
                true
            }
        } else {
            holder.btnEditar?.visibility = View.GONE
        }
    }


    override fun getItemCount(): Int = autoridades.size
}
