package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Inicio")
        val session = SessionManager(requireContext())

        lifecycleScope.launch {
            val user = session.user.first()
            val nombre = listOf(user?.nombres, user?.apellidos)
                .filterNotNull()
                .joinToString(" ")
            setContenido(
                if (nombre.isNotBlank()) {
                    "Bienvenido(a), $nombre.\n\nMódulos del portal del estudiante disponibles en el menú lateral."
                } else {
                    "Bienvenido(a) al SACE UPTBAL.\n\nMódulos del portal del estudiante disponibles en el menú lateral."
                }
            )
        }

        setRecargar {
            lifecycleScope.launch {
                setCargando()
                try {
                    val res = ApiClient.service.meEstudiante()
                    if (res.success && res.data != null) {
                        val e = res.data
                        val nombre = "${e.nombres ?: ""} ${e.apellidos ?: ""}".trim()
                        setContenido(
                            "Estudiante: $nombre\n" +
                                "Cédula: ${e.cedula}\n" +
                                "Expediente: ${e.expediente ?: "-"}\n" +
                                "Email: ${e.email ?: "-"}"
                        )
                    } else {
                        setContenido(res.error ?: "No se encontró el registro del estudiante.")
                    }
                } catch (t: Throwable) {
                    mensajeError(t)
                    setContenido("No se pudo cargar la información del estudiante.")
                } finally {
                    finCargando()
                }
            }
        }
    }
}
