package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.SituacionPrograma
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.launch

class SituacionFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargar()
    }

    override fun cargar() {
        mostrarCargando()
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.situacion()
                finCarga()
                if (!res.success) {
                    mostrarVacio(res.error ?: "No se pudo cargar la situación académica.")
                    return@launch
                }
                val programas = res.data.orEmpty()
                if (programas.isEmpty()) {
                    mostrarVacio("No hay situación académica registrada.")
                    return@launch
                }
                vaciar()
                programas.forEach { binding.contenedor.addView(boxPrograma(it)) }
            } catch (t: Throwable) {
                finCarga()
                mensajeError(t)
                mostrarVacio("No se pudo cargar la situación académica.")
            }
        }
    }

    private fun boxPrograma(p: SituacionPrograma): View {
        val ctx = requireContext()
        return Boxes.tarjeta(ctx, p.programa?.nombre ?: p.programa?.codename ?: "Programa") { body ->
            body.addView(Boxes.filaSimple(ctx, "Carrera", p.carrera?.nombre ?: "-"))
            val r = p.resumen
            body.addView(Boxes.linea(ctx))
            body.addView(
                Boxes.filaTabla(
                    ctx,
                    listOf("Créditos", "Aprobadas", "% Aprobado", "ISA", "IRA"),
                    header = true
                )
            )
            body.addView(
                Boxes.filaTabla(
                    ctx,
                    listOf(
                        "${r?.creditos_aprobados ?: 0} / ${r?.creditos_programa ?: 0}",
                        "${r?.asignaturas_aprobadas ?: 0} / ${r?.total_asignaturas ?: 0}",
                        "${r?.porcentaje_aprobado ?: 0.0}%",
                        "${r?.isa ?: 0.0}",
                        "${r?.ira ?: 0.0}"
                    )
                )
            )
            body.addView(Boxes.linea(ctx))
            body.addView(
                Boxes.filaTabla(ctx, listOf("Asignatura", "Tray.", "Nota", "Pdo."), header = true)
            )
            p.asignaturas.forEach { a ->
                val aprobada = a.aprobada == 1
                body.addView(
                    Boxes.filaTabla(
                        ctx,
                        listOf(
                            a.asignatura_nombre ?: "-",
                            a.trayecto ?: "-",
                            a.calificacion ?: "-",
                            a.periodo ?: "-"
                        ),
                        colores = mapOf(2 to Boxes.colorNota(ctx, a.calificacion, aprobada))
                    )
                )
            }
        }
    }
}
