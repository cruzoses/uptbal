package com.uptbal.sace.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.data.api.LoginRequest
import com.uptbal.sace.databinding.ActivityLoginBinding
import com.uptbal.sace.ui.main.MainActivity
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

        binding.btnIngresar.setOnClickListener { login() }
        binding.btnRegistroEstudiante.setOnClickListener { avisarEnDesarrollo() }
        binding.btnRegistroDocente.setOnClickListener { avisarEnDesarrollo() }
        binding.btnOlvidoClave.setOnClickListener { avisarEnDesarrollo() }

        verificarSesionExistente()
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

    private fun avisarEnDesarrollo() {
        toast("Esta opción estará disponible próximamente.")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
