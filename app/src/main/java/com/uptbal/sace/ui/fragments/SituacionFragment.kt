package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import kotlinx.coroutines.launch

class SituacionFragment : BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTitulo("Situación Académica")
        cargar()
        setRecargar { cargar() }
    }

    private fun cargar() {
        lifecycleScope.launch {
            setCargando()
            try {
                val res = ApiClient.service.situacion()
                if (res.success) {
                    val programas = res.data.orEmpty()
                    if (programas.isEmpty()) {
                        setContenido("No hay situación académica registrada.")
                    } else {
                        setContenido(
                            programas.joinToString("\n\n") { p ->
                                val r = p.resumen
                                buildString {
                                    append(p.programa?.nombre ?: p.programa?.codename ?: "Programa")
                                    append("\nCarrera: ").append(p.carrera?.nombre ?: "-")
                                    append("\nCréditos: ").append(r?.creditos_aprobados ?: 0).append(" / ").append(r?.creditos_programa ?: 0)
                                    append("\nAprobadas: ").append(r?.asignaturas_aprobadas ?: 0).append(" / ").append(r?.total_asignaturas ?: 0)
                                    append("\n% Aprobado: ").append(r?.porcentaje_aprobado ?: 0.0)
                                    append("\nISA: ").append(r?.isa ?: 0.0).append("  IRA: ").append(r?.ira ?: 0.0)
                                }
                            }
                        )
                    }
                } else {
                    setContenido(res.error ?: "No se pudo cargar la situación académica.")
                }
            } catch (t: Throwable) {
                mensajeError(t)
                setContenido("No se pudo cargar la situación académica.")
            } finally {
                finCargando()
            }
        }
    }
}
