package com.example.miunet01.ui.tramites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class DepartamentosAdapter(
    private var departamentos: List<Departamento>,
    private val userRole: String,
    private val onEditarClick: (Departamento) -> Unit,
    private val onEliminarClick: (Departamento) -> Unit
) : RecyclerView.Adapter<DepartamentosAdapter.DepartamentoViewHolder>() {

    class DepartamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.txtNombreDepto)
        val horario: TextView = itemView.findViewById(R.id.txtHorarioDepto)
        val ubicacion: TextView = itemView.findViewById(R.id.txtUbicacionDepto)
        val btnEditar: ImageButton? = itemView.findViewById(R.id.btnEditarDepto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_departamento, parent, false)
        return DepartamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepartamentoViewHolder, position: Int) {
        val d = departamentos[position]
        holder.nombre.text = d.nombre
        holder.horario.text = "Horario: ${d.horario}"
        holder.ubicacion.text = "Ubicación: ${d.ubicacion}"

        if (userRole == "Admin" || userRole == "Profesor") {
            holder.btnEditar?.visibility = View.VISIBLE
            holder.btnEditar?.setOnClickListener { onEditarClick(d) }
        } else {
            holder.btnEditar?.visibility = View.GONE
        }

        if (userRole == "Admin" || userRole == "Profesor") {
            holder.itemView.setOnLongClickListener {
                onEliminarClick(d)
                true
            }
        }


    }

    override fun getItemCount(): Int = departamentos.size
}
