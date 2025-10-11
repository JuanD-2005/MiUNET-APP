package com.example.miunet01.ui.tramites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class DecanatosAdapter(
    private var decanatos: List<Decanato>,
    private val userRole: String,
    private val onEditarClick: (Decanato) -> Unit,
    private val onEliminarClick: (Decanato) -> Unit
) : RecyclerView.Adapter<DecanatosAdapter.DecanatoViewHolder>() {

    class DecanatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreDepto)
        val horario: TextView = itemView.findViewById(R.id.txtHorarioDepto)
        val ubicacion: TextView = itemView.findViewById(R.id.txtUbicacionDepto)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarDepto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DecanatoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departamento, parent, false)
        return DecanatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DecanatoViewHolder, position: Int) {
        val d = decanatos[position]
        holder.nombre.text = d.nombre
        holder.horario.text = "Horario: ${d.horario}"
        holder.ubicacion.text = "Ubicación: ${d.ubicacion}"

        if (userRole == "Admin" || userRole == "Profesor") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(d) }

            // Long click para eliminar
            holder.itemView.setOnLongClickListener {
                onEliminarClick(d)
                true
            }

        } else {
            holder.btnEditar?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = decanatos.size
}
