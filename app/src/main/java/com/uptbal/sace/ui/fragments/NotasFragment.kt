package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class NotasFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Notas de Lapso")
        cargar()
        setRecargar { cargar() }
    }

    private fun cargar() {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.notasLapso()
                if (res.success) {
                    val cursos = res.data.orEmpty()
                    if (cursos.isEmpty()) {
                        setContenido("No hay cursos inscritos en este lapso.")
                    } else {
                        setContenido(
                            cursos.joinToString("\n\n") { c ->
                                buildString {
                                    append(c.asignatura?.nombre ?: "Curso")
                                    append("\nCódigo: ").append(c.asignatura?.codigo ?: "-")
                                    append("  Sección: ").append(c.seccion ?: "-")
                                    append("\nPeriodo: ").append(c.periodo?.codigo ?: "-")
                                    append("\nDefinitiva: ").append(c.definitiva ?: c.calificacion ?: "-")
                                    append("\nEvaluaciones: ").append(c.evaluaciones.size)
                                }
                            }
                        )
                    }
                } else {
                    setContenido(res.error ?: "No se pudieron cargar las notas.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudieron cargar las notas.")
            } finally {
                finCargando()
            }
        }
    }
}
