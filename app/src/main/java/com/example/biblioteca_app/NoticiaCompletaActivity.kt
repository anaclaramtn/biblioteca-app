package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biblioteca_app.models.Noticia
import com.google.android.material.bottomnavigation.BottomNavigationView

class NoticiaCompletaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_noticia_completa)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHeader()

        val imgNoticia = findViewById<ImageView>(R.id.imgNoticiaCompleta)
        val txtTitulo = findViewById<TextView>(R.id.txtTituloNoticiaCompleta)
        val txtConteudo = findViewById<TextView>(R.id.txtConteudoNoticia)

        // Receber objeto Noticia da Intent
        val noticia = intent.getSerializableExtra("NOTICIA") as? Noticia

        noticia?.let {
            txtTitulo.text = it.titulo
            txtConteudo.text = it.descricaoLonga

            if (!it.imagemBase64.isNullOrEmpty()) {
                try {
                    val bytes = android.util.Base64.decode(it.imagemBase64, android.util.Base64.DEFAULT)
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imgNoticia.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    imgNoticia.setImageResource(R.drawable.logo)
                }
            } else if (it.imagemRes != null && it.imagemRes != 0) {
                imgNoticia.setImageResource(it.imagemRes)
            } else {
                imgNoticia.setImageResource(R.drawable.logo)
            }
        }

        setupNavBar()
    }

    private fun setupHeader() {
        val header = findViewById<View>(R.id.header)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)
        val txtTituloHeader = header.findViewById<TextView>(R.id.txtTitulo)

        txtTituloHeader.text = "Notícia"
        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            finish()
        }
    }
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_busca -> {
                    startActivity(Intent(this, BuscaActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

}