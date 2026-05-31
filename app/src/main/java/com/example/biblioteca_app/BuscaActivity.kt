package com.example.biblioteca_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.LivroAdapter
import com.example.biblioteca_app.models.Livro
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class BuscaActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: LivroAdapter

    private val livros = mutableListOf<Livro>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_busca)

        val rvLivros = findViewById<RecyclerView>(R.id.rvLivros)
        val etPesquisa = findViewById<EditText>(R.id.etPesquisa)

        adapter = LivroAdapter(emptyList()) { livro ->
            val intent = Intent(this, LivroActivity::class.java)
            intent.putExtra("LIVRO", livro)
            startActivity(intent)
        }

        rvLivros.layoutManager = LinearLayoutManager(this)
        rvLivros.adapter = adapter

        carregarLivros()

        etPesquisa.doAfterTextChanged { text ->
            val query = text.toString().trim()

            val filtrados = if (query.isEmpty()) {
                livros
            } else {
                livros.filter {
                    it.titulo.contains(query, true) ||
                            it.autor.contains(query, true)
                }
            }

            adapter.updateList(filtrados)
        }
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun carregarLivros() {
        db.collection("livros")
            .get()
            .addOnSuccessListener { documents ->

                livros.clear()

                for (doc in documents) {
                    livros.add(
                        Livro(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            autor = doc.getString("autor") ?: "",
                            descricao = doc.getString("sinopse") ?: "",
                            imagemBase64 = doc.getString("imagemBase64"),
                            disponivel = doc.getBoolean("disponivel") ?: true,
                            media = doc.getDouble("media")?.toFloat() ?: 0f,
                            totalAvaliacoes = doc.getLong("totalAvaliacoes")?.toInt() ?: 0
                        )
                    )
                }

                adapter.updateList(livros)

                // atualizando a contagem de livros
                findViewById<TextView>(R.id.txtQtdLivros).text =
                    "${livros.size} livro(s) no acervo"
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