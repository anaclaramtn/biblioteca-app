package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_admin_home)

        setupHeader()
        setupAtalhos()
        setupNavBar()
    }

    private fun setupHeader() {
        val header = findViewById<View>(R.id.header)
        val titulo = header.findViewById<TextView>(R.id.txtTitulo)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)

        titulo.text = "Olá, Admin"
        btnBack.visibility = View.GONE
    }

    private fun setupAtalhos() {
        findViewById<LinearLayout>(R.id.btnCadastroNoticias).setOnClickListener {
            startActivity(Intent(this, CadastroNoticiaActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnCadastroLivros).setOnClickListener {
            startActivity(Intent(this, CadastroLivroActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnCadastroJogos).setOnClickListener {
            startActivity(Intent(this, CadastroJogoActivity::class.java))
        }
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuarioadmActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_notif -> {
                    startActivity(Intent(this, AdminNotificacoesActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_menu -> {
                    startActivity(Intent(this, AdminMenuActivity::class.java))
                    finish()
                    true
                }

                else -> false
            }
        }
    }
}