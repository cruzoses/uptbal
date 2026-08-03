package com.uptbal.sace.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uptbal.sace.R
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.LoginRequest
import com.uptbal.sace.data.api.RecuperarClaveRequest
import com.uptbal.sace.databinding.ActivityLoginBinding
import com.uptbal.sace.ui.main.MainActivity
import com.uptbal.sace.ui.registro.RegistroActivity
import com.uptbal.sace.ui.registro.RegistroRapidoActivity
import com.uptbal.sace.util.Boxes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        binding.btnBienvenido.setOnClickListener {
            binding.flipperLogin.showNext()
            binding.btnBienvenido.isEnabled = false
        }
        binding.btnIngresar.setOnClickListener { login() }
        binding.btnRegistroEstudiante.setOnClickListener { abrirRegistro(RegistroActivity.MODO_ESTUDIANTE) }
        binding.btnRegistroDocente.setOnClickListener { abrirRegistro(RegistroActivity.MODO_DOCENTE) }
        binding.btnSolicitarClave.setOnClickListener {
            startActivity(Intent(this, RegistroRapidoActivity::class.java))
        }
        binding.btnOlvidoClave.setOnClickListener { recuperarClave() }

        verificarSesionExistente()
    }

    private fun abrirRegistro(modo: String) {
        startActivity(Intent(this, RegistroActivity::class.java).putExtra(RegistroActivity.EXTRA_MODO, modo))
    }

    private fun verificarSesionExistente() {
        lifecycleScope.launch {
            val token = session.getToken()
            if (token != null) {
                ApiClient.apiToken = token
                val user = session.user.first()
                if (user != null) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun login() {
        val username = binding.inputUsuario.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnIngresar.isEnabled = false
        binding.progressLogin.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val res = ApiClient.service.login(LoginRequest(username, password))
                if (res.success && res.token != null && res.user != null) {
                    ApiClient.apiToken = res.token
                    session.saveSession(res.token, res.user)
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    toast(res.error ?: "Usuario o contraseña incorrectos")
                }
            } catch (e: HttpException) {
                toast(if (e.code() == 401) "Usuario o contraseña incorrectos" else "Error del servidor (${e.code()})")
            } catch (e: IOException) {
                toast("Sin conexión con el servidor. Verifique su red.")
            } catch (e: Exception) {
                toast(e.message ?: "Error inesperado")
            } finally {
                binding.btnIngresar.isEnabled = true
                binding.progressLogin.visibility = View.GONE
            }
        }
    }

    private fun recuperarClave() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            hint = getString(R.string.olvido_email_hint)
            setPadding(Boxes.dp(this@LoginActivity, 24), 0, Boxes.dp(this@LoginActivity, 24), 0)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.olvido_titulo)
            .setMessage(R.string.olvido_mensaje)
            .setView(input)
            .setPositiveButton(R.string.olvido_enviar) { _, _ ->
                val email = input.text?.toString()?.trim().orEmpty()
                if (email.isEmpty()) {
                    toast(getString(R.string.olvido_email_hint))
                } else {
                    enviarRecuperacion(email)
                }
            }
            .setNegativeButton(R.string.olvido_cancelar, null)
            .show()
    }

    private fun enviarRecuperacion(email: String) {
        lifecycleScope.launch {
            try {
                val res = ApiClient.service.recuperarClave(RecuperarClaveRequest(email))
                toast(res.message ?: res.error ?: "Solicitud procesada.")
            } catch (e: IOException) {
                toast("Sin conexión con el servidor. Verifique su red.")
            } catch (e: Exception) {
                toast(e.message ?: "Error inesperado")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
