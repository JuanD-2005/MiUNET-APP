package com.example.miunet01.ui.tramites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.miunet01.R

class TramitesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout de este fragmento
        return inflater.inflate(R.layout.fragment_tramites, container, false)
    }
}
