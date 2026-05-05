package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminEdicaoActivity : AppCompatActivity() {

    private var livroPos: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_edicao)

        livroPos = intent.getIntExtra("LIVRO_POS", -1)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        preencherDados()
        setupBotoes()
        setupNavBar()
    }

    private fun preencherDados() {
        if (livroPos != -1 && livroPos < AcervoadmActivity.listaLivros.size) {
            val livro = AcervoadmActivity.listaLivros[livroPos]
            findViewById<EditText>(R.id.edtTitulo).setText(livro.titulo)
            findViewById<EditText>(R.id.edtAutor).setText(livro.autor)
            findViewById<EditText>(R.id.edtDescricao).setText(livro.descricao)
            findViewById<ImageView>(R.id.imgCapa).setImageResource(livro.imagemRes)
        }
    }

    private fun setupBotoes() {
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            confirmarSaida()
        }

        findViewById<Button>(R.id.btnEnviar).setOnClickListener {
            salvarAlteracoes()
        }
    }

    private fun salvarAlteracoes() {
        val novoTitulo = findViewById<EditText>(R.id.edtTitulo).text.toString()
        val novoAutor = findViewById<EditText>(R.id.edtAutor).text.toString()
        val novaDesc = findViewById<EditText>(R.id.edtDescricao).text.toString()

        if (novoTitulo.isBlank() || novoAutor.isBlank() || novaDesc.isBlank()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        if (livroPos != -1 && livroPos < AcervoadmActivity.listaLivros.size) {
            val livroOriginal = AcervoadmActivity.listaLivros[livroPos]
            val livroEditado = livroOriginal.copy(
                titulo = novoTitulo,
                autor = novoAutor,
                descricao = novaDesc
            )
            AcervoadmActivity.listaLivros[livroPos] = livroEditado
            Toast.makeText(this, "Acervo Atualizado!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(this)
            .setTitle("Certeza que deseja voltar?")
            .setMessage("Suas informações não serão salvas.")
            .setPositiveButton("Sim") { _, _ -> finish() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.navbar)
        bottomNav.selectedItemId = R.id.nav_acervo

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }
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
