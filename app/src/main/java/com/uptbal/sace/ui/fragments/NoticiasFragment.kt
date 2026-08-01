package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.NoticiaDto
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.launch

class NoticiasFragment : BaseListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargar()
    }

    override fun cargar() {
        mostrarCargando()
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.noticias()
                finCarga()
                if (!res.success) {
                    mostrarVacio(res.error ?: "No se pudieron cargar las noticias.")
                    return@launch
                }
                val noticias = res.data.orEmpty()
                if (noticias.isEmpty()) {
                    mostrarVacio("No hay noticias publicadas.")
                    return@launch
                }
                vaciar()
                noticias.forEach { binding.contenedor.addView(boxNoticia(it)) }
            } catch (t: Throwable) {
                finCarga()
                mensajeError(t)
                mostrarVacio("No se pudieron cargar las noticias.")
            }
        }
    }

    private fun boxNoticia(n: NoticiaDto): View {
        val ctx = requireContext()
        val card = Boxes.tarjeta(ctx) { body ->
            body.addView(Boxes.texto(ctx, n.titulo ?: "Sin título", titulo = true))
            body.addView(
                Boxes.texto(
                    ctx,
                    "Fecha: ${n.fecha ?: "-"}  |  Autor: ${n.autor ?: "-"}"
                )
            )
            body.addView(Boxes.linea(ctx))
            body.addView(Boxes.texto(ctx, (n.contenido ?: "").take(160)))
        }
        card.isClickable = true
        card.isFocusable = true
        card.setOnClickListener {
            findNavController().navigate(
                NoticiasFragmentDirections.actionNoticiasFragmentToNoticiaDetalleFragment(n.id)
            )
        }
        return card
    }
}
