package com.uptbal.sace.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.uptbal.sace.databinding.FragmentPlaceholderBinding
import retrofit2.HttpException
import java.io.IOException

abstract class BaseFragment : Fragment() {

    protected lateinit var binding: FragmentPlaceholderBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setTitulo(titulo: String) {
        binding.txtTitulo.text = titulo
    }

    protected fun setContenido(texto: String) {
        binding.txtContenido.text = texto
    }

    protected fun setRecargar(accion: () -> Unit) {
        binding.btnRecargar.setOnClickListener { accion() }
    }

    protected fun setCargando() {
        binding.txtContenido.text = "Cargando…"
        binding.btnRecargar.isEnabled = false
    }

    protected fun finCargando() {
        binding.btnRecargar.isEnabled = true
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
