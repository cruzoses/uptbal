package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.R
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.InscripcionDto
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.launch

class InscripcionesFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargar()
    }

    override fun cargar() {
        mostrarCargando()
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.inscripciones()
                finCarga()
                if (!res.success) {
                    mostrarVacio(res.error ?: "No se pudieron cargar las inscripciones.")
                    return@launch
                }
                val items = res.data.orEmpty()
                if (items.isEmpty()) {
                    mostrarVacio("No hay inscripciones registradas.")
                    return@launch
                }
                vaciar()
                items.forEach { binding.contenedor.addView(boxInscripcion(it)) }
            } catch (t: Throwable) {
                finCarga()
                mensajeError(t)
                mostrarVacio("No se pudieron cargar las inscripciones.")
            }
        }
    }

    private fun estado(i: InscripcionDto): Pair<String, Int> {
        val ctx = requireContext()
        return when {
            i.culminado == 1 -> "Culminado" to android.graphics.Color.rgb(0x00, 0x5B, 0x96)
            i.congelado == 1 -> "Congelado" to android.graphics.Color.rgb(0xD4, 0x8A, 0x00)
            i.activo == 1 -> "Activo" to ctx.getColor(R.color.adminlte_success)
            else -> "Inactivo" to android.graphics.Color.rgb(0x88, 0x88, 0x88)
        }
    }

    private fun boxInscripcion(i: InscripcionDto): View {
        val ctx = requireContext()
        val (estado, color) = estado(i)
        return Boxes.tarjeta(ctx, i.programa?.nombre ?: i.programa?.codename ?: "Programa") { body ->
            body.addView(Boxes.filaSimple(ctx, "Carrera", i.carrera?.nombre ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Sede", i.sede ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Periodo", i.periodo?.codigo ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Cohorte", i.cohorte ?: "-"))
            body.addView(Boxes.filaSimple(ctx, "Fecha de egreso", i.fecha_egreso ?: "-"))
            body.addView(Boxes.linea(ctx))
            body.addView(
                Boxes.filaTabla(ctx, listOf("Estado", "ISA", "IRA"), header = true)
            )
            body.addView(
                Boxes.filaTabla(
                    ctx,
                    listOf(estado, "${i.isa}", "${i.ira}"),
                    colores = mapOf(0 to color)
                )
            )
        }
    }
}
