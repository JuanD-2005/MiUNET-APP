package com.example.miunet01.ui.unetinfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.miunet01.R

class HorariosAdapter(private var horarios: List<Horario>) :
    RecyclerView.Adapter<HorariosAdapter.HorarioViewHolder>() {

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val departamento: TextView = itemView.findViewById(R.id.txt_departamento)
        val dia: TextView = itemView.findViewById(R.id.txt_dia)
        val horario: TextView = itemView.findViewById(R.id.txt_horario)
        val ubicacion: TextView = itemView.findViewById(R.id.txt_ubicacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_horario, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val h = horarios[position]
        holder.departamento.text = h.departamento
        holder.dia.text = "Día: ${h.dia}"
        holder.horario.text = "Horario: ${h.hora_inicio} - ${h.hora_fin}"
        holder.ubicacion.text = "Ubicación: ${h.ubicacion}"
    }

    override fun getItemCount(): Int = horarios.size

    fun actualizarLista(nuevaLista: List<Horario>) {
        horarios = nuevaLista
        notifyDataSetChanged()
    }
}
