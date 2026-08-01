package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class InscripcionesFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Inscripciones")
        cargar()
        setRecargar { cargar() }
    }

    private fun cargar() {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.inscripciones()
                if (res.success) {
                    val items = res.data.orEmpty()
                    if (items.isEmpty()) {
                        setContenido("No hay inscripciones registradas.")
                    } else {
                        setContenido(
                            items.joinToString("\n\n") { i ->
                                buildString {
                                    append(i.programa?.nombre ?: i.programa?.codename ?: "Programa")
                                    append("\nCarrera: ").append(i.carrera?.nombre ?: "-")
                                    append("\nSede: ").append(i.sede ?: "-")
                                    append("\nPeriodo: ").append(i.periodo?.codigo ?: "-")
                                    val estado = when {
                                        i.culminado == 1 -> "Culminado"
                                        i.congelado == 1 -> "Congelado"
                                        i.activo == 1 -> "Activo"
                                        else -> "Inactivo"
                                    }
                                    append("\nEstado: ").append(estado)
                                }
                            }
                        )
                    }
                } else {
                    setContenido(res.error ?: "No se pudieron cargar las inscripciones.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudieron cargar las inscripciones.")
            } finally {
                finCargando()
            }
        }
    }
}
