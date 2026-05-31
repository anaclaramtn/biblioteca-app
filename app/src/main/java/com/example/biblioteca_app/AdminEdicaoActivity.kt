package com.example.biblioteca_app

import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore

class AdminEdicaoActivity : AppCompatActivity() {
    private var livroPos: Int = -1
    private var livroAtual: Livro? = null

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_edicao)

        livroPos = intent.getIntExtra("LIVRO_POS", -1)
        livroAtual = intent.getSerializableExtra("LIVRO") as? Livro

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
        livroAtual?.let { livro ->

            findViewById<EditText>(R.id.edtTitulo)
                .setText(livro.titulo)

            findViewById<EditText>(R.id.edtAutor)
                .setText(livro.autor)

            findViewById<EditText>(R.id.edtDescricao)
                .setText(livro.descricao)

            findViewById<ImageView>(R.id.imgCapa)
                .setImageURI(android.net.Uri.parse(livro.imagemUri))
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

        val livro = livroAtual ?: return

        val dadosAtualizados = hashMapOf(
            "titulo" to novoTitulo,
            "autor" to novoAutor,
            "sinopse" to novaDesc
        )

        db.collection("livros")
            .document(livro.id)
            .update(dadosAtualizados as Map<String, Any>)
            .addOnSuccessListener {

                val index = AcervoadmActivity.listaLivros.indexOfFirst {
                    it.id == livro.id
                }

                if (index != -1) {
                    AcervoadmActivity.listaLivros[index] =
                        AcervoadmActivity.listaLivros[index].copy(
                            titulo = novoTitulo,
                            autor = novoAutor,
                            descricao = novaDesc
                        )
                }

                Toast.makeText(
                    this,
                    "Livro atualizado com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erro ao atualizar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
