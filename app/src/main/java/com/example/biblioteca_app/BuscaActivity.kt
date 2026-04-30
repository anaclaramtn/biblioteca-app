package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.widget.doAfterTextChanged
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class BuscaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_busca)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuração da Pesquisa
        val etPesquisa = findViewById<EditText>(R.id.etPesquisa)
        val btnLimpar = findViewById<ImageView>(R.id.btnLimparPesquisa)
        val gridLivros = findViewById<View>(R.id.gridLivros)
        val layoutSemResultados = findViewById<View>(R.id.layoutSemResultados)
        val txtMensagemErro = findViewById<TextView>(R.id.txtMensagemErro)
        val ids = listOf(R.id.item1, R.id.item2, R.id.item3, R.id.item4)

        btnLimpar.setOnClickListener {
            etPesquisa.text.clear()
        }

        etPesquisa.doAfterTextChanged { text ->
            val query = text.toString().lowercase()
            
            if (query.isEmpty()) {
                gridLivros.visibility = View.VISIBLE
                layoutSemResultados.visibility = View.GONE
                ids.forEach { findViewById<View>(it).visibility = View.VISIBLE }
            } else {
                val matches = mutableListOf<Int>()
                
                // Simulação de pesquisa para os 4 livros específicos
                if ("star wars".contains(query) || "george lucas".contains(query)) matches.add(R.id.item1)
                if ("frankenstein".contains(query) || "mary shelley".contains(query)) matches.add(R.id.item2)
                if ("the hobbit".contains(query) || "j.r.r. tolkien".contains(query)) matches.add(R.id.item3)
                if ("it".contains(query) || "stephen king".contains(query)) matches.add(R.id.item4)

                if (matches.isNotEmpty()) {
                    gridLivros.visibility = View.VISIBLE
                    layoutSemResultados.visibility = View.GONE
                    ids.forEach { id -> 
                        findViewById<View>(id).visibility = if (matches.contains(id)) View.VISIBLE else View.GONE 
                    }
                } else {
                    gridLivros.visibility = View.GONE
                    layoutSemResultados.visibility = View.VISIBLE
                    txtMensagemErro.text = "Nenhum resultado encontrado para\n\"$text\""
                }
            }
        }

        // Configuração da Ordenação
        val btnOrdenar = findViewById<LinearLayout>(R.id.btnOrdenar)
        btnOrdenar.setOnClickListener {
            showOrderDialog()
        }

        // Configurar Livros (Star Wars no primeiro, demais Dom Quixote)
        setupLivros(ids)

        // Configurar NavBar
        setupNavBar()
    }

    private fun setupLivros(ids: List<Int>) {
        val txtQtdLivros = findViewById<TextView>(R.id.txtQtdLivros)
        txtQtdLivros.text = "${ids.size} livro(s) registrado(s) no\nacervo"
        
        for (i in ids.indices) {
            val id = ids[i]
            val view = findViewById<View>(id)
            
            when (i) {
                0 -> {
                    view.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.capa_star_wars)
                    view.findViewById<TextView>(R.id.txtTituloLivro).text = "Star Wars"
                    view.findViewById<TextView>(R.id.txtAutor).text = "George Lucas"
                }
                1 -> {
                    view.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.frankstein)
                    view.findViewById<TextView>(R.id.txtTituloLivro).text = "Frankenstein"
                    view.findViewById<TextView>(R.id.txtAutor).text = "Mary Shelley"
                }
                2 -> {
                    view.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.hobbit)
                    view.findViewById<TextView>(R.id.txtTituloLivro).text = "The Hobbit"
                    view.findViewById<TextView>(R.id.txtAutor).text = "J.R.R. Tolkien"
                }
                3 -> {
                    view.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.it)
                    view.findViewById<TextView>(R.id.txtTituloLivro).text = "It"
                    view.findViewById<TextView>(R.id.txtAutor).text = "Stephen King"
                }
            }
            
            view.setOnClickListener {
                val intent = Intent(this, LivroActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showOrderDialog() {
        val options = arrayOf("A - Z (Título)", "Z - A (Título)", "A - Z (Autor)", "Z - A (Autor)")
        
        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setItems(options) { _, which ->
                Toast.makeText(this, "Ordenando por: ${options[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_busca
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_busca -> true
                R.id.nav_notif -> {
                    startActivity(Intent(this, NotificacoesActivity::class.java))
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}