package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.uptbal.sace.databinding.FragmentListBinding
import com.uptbal.sace.util.Boxes
import retrofit2.HttpException
import java.io.IOException

abstract class BaseListFragment : Fragment() {

    protected lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        binding.swipeRefresh.setOnRefreshListener { cargar() }
        return binding.root
    }

    abstract fun cargar()

    protected fun vaciar() {
        binding.contenedor.removeAllViews()
    }

    protected fun mostrarCargando() {
        vaciar()
        binding.contenedor.addView(Boxes.texto(requireContext(), "Cargando…"))
    }

    protected fun mostrarVacio(mensaje: String) {
        vaciar()
        binding.contenedor.addView(Boxes.texto(requireContext(), mensaje))
    }

    protected fun finCarga() {
        binding.swipeRefresh.isRefreshing = false
    }

    protected fun mensajeError(t: Throwable) {
        when (t) {
            is HttpException -> {
                if (t.code() == 401) {
                    toast("Su sesión expiró. Vuelva a iniciar sesión.")
                } else {
                    toast("Error del servidor (${t.code()})")
                }
            }
            is IOException -> toast("Sin conexión con el servidor. Verifique su red.")
            else -> toast(t.message ?: "Error inesperado")
        }
    }

    protected fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}
