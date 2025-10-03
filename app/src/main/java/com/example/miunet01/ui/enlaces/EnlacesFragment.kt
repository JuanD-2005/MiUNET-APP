package com.example.miunet01.ui.enlaces

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.miunet01.R
import android.widget.Toast

class EnlacesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_enlaces, container, false)

        // Referencias a los botones
        val btnWebUnet: Button = root.findViewById(R.id.btn_web_unet)
        val btnMoodle: Button = root.findViewById(R.id.btn_web_moodle)
        val btnCorreo: Button = root.findViewById(R.id.btn_web_correo)

        // Abrir página oficial de la UNET
        btnWebUnet.setOnClickListener {
            abrirEnlace("https://www.unet.edu.ve/")
        }

        // Abrir Moodle UNET
        btnMoodle.setOnClickListener {
            abrirEnlace("https://moodle.unet.edu.ve/")
        }

        // Abrir Correo Institucional
        btnCorreo.setOnClickListener {
            abrirEnlace("https://correo.unet.edu.ve/")
        }

        return root
    }

    // Función auxiliar para abrir URLs
    private fun abrirEnlace(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(Intent.createChooser(browserIntent, "Abrir con"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

}
