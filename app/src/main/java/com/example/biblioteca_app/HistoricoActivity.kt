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
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoricoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_historico)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        btnVoltar.setOnClickListener { finish() }

        setupHistorico()
        setupNavBar()
    }



    private fun setupHistorico() {
        // Item 1
        configurarItemHistorico(
            R.id.item1,
            "Dom Quixote",
            "Aluguel: 27/04/2026\nDevolução: 14/04/2026",
            "Valor da multa: R$ 4,00",
            R.drawable.capadomquixote
        )

        // Item 2
        configurarItemHistorico(
            R.id.item2,
            "Star Wars",
            "Aluguel: 10/01/2026\nDevolução: 25/01/2026",
            "Valor da multa: R$ 0,00",
            R.drawable.capa_star_wars
        )

        // Item 3
        configurarItemHistorico(
            R.id.item3,
            "Frankstein",
            "Aluguel: 05/12/2025\nDevolução: 20/12/2025",
            "Valor da multa: R$ 0,00",
            R.drawable.frankstein // Substituir pela capa correta se houver
        )
    }

    private fun configurarItemHistorico(id: Int, titulo: String, datas: String, multa: String, imagem: Int) {
        val itemView = findViewById<View>(id)
        itemView.findViewById<TextView>(R.id.txtTitulo).text = titulo
        itemView.findViewById<TextView>(R.id.txtDatas).text = datas
        itemView.findViewById<TextView>(R.id.txtMulta).text = multa
        itemView.findViewById<ImageView>(R.id.imgItem).setImageResource(imagem)
        
        itemView.setOnClickListener {
            val intent = Intent(this, LivroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_menu
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