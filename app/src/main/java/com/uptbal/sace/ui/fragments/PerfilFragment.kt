package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View

class PerfilFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Mi Perfil")
        setContenido("Edición de foto y redes sociales.\n\nDisponible próximamente.")
    }
}
