package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.HistoricoDto
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.launch

class HistoricosFragment : BaseListFragment() {

    private val periodoActual = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargar()
    }

    override fun cargar() {
        mostrarCargando()
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.historicos()
                finCarga()
                if (!res.success) {
                    mostrarVacio(res.error ?: "No se pudieron cargar los históricos.")
                    return@launch
                }
                val items = res.data.orEmpty()
                if (items.isEmpty()) {
                    mostrarVacio("No hay históricos registrados.")
                    return@launch
                }
                vaciar()
                periodoActual.clear()
                items.forEach { h ->
                    val periodo = h.periodo?.codigo ?: "Sin periodo"
                    if (periodo !in periodoActual) {
                        periodoActual.add(periodo)
                        binding.contenedor.addView(Boxes.texto(requireContext(), "Periodo $periodo", titulo = true))
                        binding.contenedor.addView(
                            Boxes.filaTabla(requireContext(), listOf("Asignatura", "Sec.", "Nota", "Resp."), header = true)
                        )
                    }
                    binding.contenedor.addView(filaHistorico(h))
                }
            } catch (t: Throwable) {
                finCarga()
                mensajeError(t)
                mostrarVacio("No se pudieron cargar los históricos.")
            }
        }
    }

    private fun aprobada(nota: String?): Boolean {
        val v = nota?.trim() ?: return false
        if (v.equals("A", ignoreCase = true)) return true
        return runCatching { v.toDouble() >= 12.0 }.getOrDefault(false)
    }

    private fun filaHistorico(h: HistoricoDto): View {
        val ctx = requireContext()
        return Boxes.filaTabla(
            ctx,
            listOf(
                h.asignatura?.nombre ?: "-",
                h.seccion ?: "-",
                h.calificacion ?: "-",
                h.responsable ?: "-"
            ),
            colores = mapOf(2 to Boxes.colorNota(ctx, h.calificacion, aprobada(h.calificacion)))
        )
    }
}
