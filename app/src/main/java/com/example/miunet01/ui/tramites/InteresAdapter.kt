package com.example.miunet01.ui.tramites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class InteresAdapter(
    private var zonasInteres: List<Interes>,
    private val userRole: String,
    private val onEditarClick: (Interes) -> Unit,
    private val onEliminarClick: (Interes) -> Unit
) : RecyclerView.Adapter<InteresAdapter.InteresViewHolder>() {

    class InteresViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreDepto)
        val horario: TextView = itemView.findViewById(R.id.txtHorarioDepto)
        val ubicacion: TextView = itemView.findViewById(R.id.txtUbicacionDepto)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarDepto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InteresViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departamento, parent, false)
        return InteresViewHolder(view)
    }

    override fun onBindViewHolder(holder: InteresViewHolder, position: Int) {
        val z = zonasInteres[position]
        holder.nombre.text = z.nombre
        holder.horario.text = "Horario: ${z.horario}"
        holder.ubicacion.text = "Ubicación: ${z.ubicacion}"

        if (userRole == "Admin" || userRole == "Profesor") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(z) }

            // Long click para eliminar
            holder.itemView.setOnLongClickListener {
                onEliminarClick(z)
                true
            }

        } else {
            holder.btnEditar?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = zonasInteres.size
}
