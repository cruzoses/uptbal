package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.NotaLapsoDto
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.launch
import java.util.Locale

class NotasFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargar()
    }

    override fun cargar() {
        mostrarCargando()
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.notasLapso()
                finCarga()
                if (!res.success) {
                    mostrarVacio(res.error ?: "No se pudieron cargar las notas.")
                    return@launch
                }
                val cursos = res.data.orEmpty()
                if (cursos.isEmpty()) {
                    mostrarVacio("No hay cursos inscritos en este lapso.")
                    return@launch
                }
                vaciar()
                cursos.forEach { binding.contenedor.addView(boxCurso(it)) }
            } catch (t: Throwable) {
                finCarga()
                mensajeError(t)
                mostrarVacio("No se pudieron cargar las notas.")
            }
        }
    }

    private fun aprobada(definitiva: String?): Boolean {
        val v = definitiva?.trim() ?: return false
        if (v.equals("A", ignoreCase = true)) return true
        return runCatching { v.toDouble() >= 12.0 }.getOrDefault(false)
    }

    private fun boxCurso(c: NotaLapsoDto): View {
        val ctx = requireContext()
        val definitiva = c.definitiva ?: c.calificacion
        return Boxes.tarjeta(
            ctx,
            "${c.asignatura?.nombre ?: "Curso"}  ·  Sec. ${c.seccion ?: "-"}"
        ) { body ->
            body.addView(Boxes.filaSimple(ctx, "Código", c.asignatura?.codigo ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Periodo", c.periodo?.codigo ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Docente", c.docente ?: "-"))
            body.addView(Boxes.linea(ctx))
            body.addView(
                Boxes.filaTabla(ctx, listOf("Definitiva", "Recuperación", "Observación"), header = true)
            )
            body.addView(
                Boxes.filaTabla(
                    ctx,
                    listOf(
                        definitiva ?: "-",
                        c.recuperacion ?: "-",
                        c.observacion ?: "-"
                    ),
                    colores = mapOf(0 to Boxes.colorNota(ctx, definitiva, aprobada(definitiva)))
                )
            )
            body.addView(Boxes.linea(ctx))
            if (c.evaluaciones.isEmpty()) {
                body.addView(Boxes.texto(ctx, "Sin evaluaciones registradas."))
            } else {
                body.addView(
                    Boxes.filaTabla(ctx, listOf("Evaluación", "Fecha", "Pond.", "Nota"), header = true)
                )
                c.evaluaciones.forEach { e ->
                    body.addView(
                        Boxes.filaTabla(
                            ctx,
                            listOf(
                                e.descripcion ?: e.indicador ?: "-",
                                e.fecha ?: "-",
                                String.format(Locale.US, "%.0f%%", e.ponderacion),
                                e.nota ?: "-"
                            )
                        )
                    )
                }
            }
        }
    }
}
