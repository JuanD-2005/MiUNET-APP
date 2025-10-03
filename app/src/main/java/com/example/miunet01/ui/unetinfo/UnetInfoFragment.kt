package com.example.miunet01.ui.unetinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miunet01.databinding.FragmentUnetInfoBinding

class UnetInfoFragment : Fragment() {

    private var _binding: FragmentUnetInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnetInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Ejemplo de contenido dinámico (texto)
        binding.tvTitulo.text = "Eventos y Horarios UNET"
        binding.tvDescripcion.text = "Consulta los próximos eventos y horarios de atención de cada departamento."

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
