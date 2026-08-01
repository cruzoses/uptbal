package com.uptbal.sace.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.uptbal.sace.R
import com.uptbal.sace.data.SessionManager
import com.uptbal.sace.data.api.ApiClient
import com.uptbal.sace.databinding.ActivityMainBinding
import com.uptbal.sace.ui.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.navHostFragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.situacionFragment,
                R.id.notasFragment,
                R.id.inscripcionesFragment,
                R.id.historicosFragment,
                R.id.noticiasFragment,
                R.id.perfilFragment
            ),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_salir -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    lifecycleScope.launch {
                        runCatching { ApiClient.service.logout() }
                        ApiClient.apiToken = null
                        session.clearSession()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finishAffinity()
                    }
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }

        lifecycleScope.launch {
            val user = session.user.first()
            val nombreCompleto = listOf(user?.nombres, user?.apellidos)
                .filterNotNull()
                .joinToString(" ")
                .ifBlank { user?.username ?: "" }
            binding.navView.getHeaderView(0)
                .findViewById<TextView>(R.id.txtHeaderUsuario)
                ?.text = nombreCompleto.ifBlank { getString(R.string.menu_drawer_header_subtitulo) }
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
