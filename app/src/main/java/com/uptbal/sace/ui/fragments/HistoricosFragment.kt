package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class HistoricosFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Históricos")
        cargar()
        setRecargar { cargar() }
    }

    private fun cargar() {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.historicos()
                if (res.success) {
                    val items = res.data.orEmpty()
                    if (items.isEmpty()) {
                        setContenido("No hay históricos registrados.")
                    } else {
                        setContenido(
                            items.joinToString("\n\n") { h ->
                                buildString {
                                    append(h.asignatura?.nombre ?: "Asignatura")
                                    append("\nPeriodo: ").append(h.periodo?.codigo ?: "-")
                                    append("  Sección: ").append(h.seccion ?: "-")
                                    append("\nCalificación: ").append(h.calificacion ?: "-")
                                }
                            }
                        )
                    }
                } else {
                    setContenido(res.error ?: "No se pudieron cargar los históricos.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudieron cargar los históricos.")
            } finally {
                finCargando()
            }
        }
    }
}
