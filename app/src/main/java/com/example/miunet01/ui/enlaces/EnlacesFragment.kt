package com.example.miunet01.ui.enlaces

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.miunet01.R
import com.example.miunet01.databinding.FragmentEnlacesBinding

class EnlacesFragment : Fragment() {

    private var _binding: FragmentEnlacesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEnlacesBinding.inflate(inflater, container, false)

        setupFAQExpandable()
        setupButtonListeners()

        return binding.root
    }

    private fun setupFAQExpandable() {
        // FAQ 1
        setupFAQItem(
            container = binding.faq1,
            answer = binding.faq1Answer,
            arrow = binding.faq1Arrow
        )

        // FAQ 2
        setupFAQItem(
            container = binding.faq2,
            answer = binding.faq2Answer,
            arrow = binding.faq2Arrow
        )

        // FAQ 3 (si existe en tu layout)
        setupFAQItem(
            container = binding.faq3,
            answer = binding.faq3Answer,
            arrow = binding.faq3Arrow
        )
    }

    private fun setupFAQItem(
        container: View,
        answer: View,
        arrow: View
    ) {
        var isExpanded = false

        container.setOnClickListener {
            isExpanded = !isExpanded

            if (isExpanded) {
                // Expandir
                answer.visibility = View.VISIBLE
                arrow.rotation = 180f

                // Animación suave
                val animation = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in)
                answer.startAnimation(animation)
            } else {
                // Colapsar
                answer.visibility = View.GONE
                arrow.rotation = 0f
            }
        }
    }

    private fun setupButtonListeners() {
        binding.btnPortalUNET.setOnClickListener {
            openUrl("https://www.unet.edu.ve")
        }

        binding.btnCorreo.setOnClickListener {
            openUrl("https://correo.unet.edu.ve")
        }

        binding.btnControl.setOnClickListener {
            openUrl("https://control.unet.edu.ve/")
        }

        binding.btnCalcUNET.setOnClickListener {
            openPlayStore("com.dylan_roman.calculadora_unet")
        }

        binding.btnGeoGebra.setOnClickListener {
            openPlayStore("org.geogebra.android")
        }
    }

    private fun openUrl(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(Intent.createChooser(browserIntent, "Abrir con"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            // Si Play Store no está disponible, abrir en navegador
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
