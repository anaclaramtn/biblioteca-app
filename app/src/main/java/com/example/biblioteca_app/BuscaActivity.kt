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
        val ids = listOf(R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6)

        btnLimpar.setOnClickListener {
            etPesquisa.text.clear()
        }

        etPesquisa.doAfterTextChanged { text ->
            val query = text.toString().lowercase()
            
            if (query.isEmpty()) {
                gridLivros.visibility = View.VISIBLE
                layoutSemResultados.visibility = View.GONE
                ids.forEach { findViewById<View>(it).visibility = View.VISIBLE }
            } else if (query.contains("dom") || query.contains("quixote") || query.contains("miguel") || query.contains("cervantes")) {
                gridLivros.visibility = View.VISIBLE
                layoutSemResultados.visibility = View.GONE
                // Mostra apenas o primeiro para simular resultado único encontrado
                ids.forEach { findViewById<View>(it).visibility = View.GONE }
                findViewById<View>(R.id.item1).visibility = View.VISIBLE
            } else {
                gridLivros.visibility = View.GONE
                layoutSemResultados.visibility = View.VISIBLE
                txtMensagemErro.text = "Nenhum resultado encontrado para\n\"$text\""
            }
        }

        // Configuração da Ordenação
        val btnOrdenar = findViewById<LinearLayout>(R.id.btnOrdenar)
        btnOrdenar.setOnClickListener {
            showOrderDialog()
        }

        // Configurar Livros (Todos Dom Quixote para demonstração)
        setupLivros(ids)

        // Configurar NavBar
        setupNavBar()
    }

    private fun setupLivros(ids: List<Int>) {
        val txtQtdLivros = findViewById<TextView>(R.id.txtQtdLivros)
        txtQtdLivros.text = "${ids.size} livro(s) registrado(s) no\nacervo"
        
        for (id in ids) {
            val view = findViewById<View>(id)
            view.findViewById<ImageView>(R.id.imgCapa).setImageResource(R.drawable.capadomquixote)
            view.findViewById<TextView>(R.id.txtTituloLivro).text = "Dom Quixote"
            view.findViewById<TextView>(R.id.txtAutor).text = "Miguel de Cervantes"
            
            view.setOnClickListener {
                // Ao clicar, vai para a tela do livro
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
                R.id.nav_menu -> {
                    startActivity(Intent(this, MenuActivity::class.java))
                    true
                }
                R.id.nav_busca -> true
                else -> false
            }
        }
    }
}