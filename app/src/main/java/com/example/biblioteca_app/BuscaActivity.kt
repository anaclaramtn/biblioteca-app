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
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.GridLayoutManager
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
        val btnLimparPesquisa = findViewById<ImageView>(R.id.btnLimparPesquisa) // Mapeado aqui

        val txtQtd = findViewById<TextView>(R.id.txtQtdLivros)
        val layoutSemResultados = findViewById<LinearLayout>(R.id.layoutSemResultados)
        val btnOrdenar = findViewById<LinearLayout>(R.id.btnOrdenar)

        adapter = LivroAdapter(emptyList()) { livro ->
            val intent = Intent(this, LivroActivity::class.java)
            intent.putExtra("LIVRO", livro)
            startActivity(intent)
        }

        rvLivros.layoutManager = GridLayoutManager(this, 3)
        rvLivros.adapter = adapter

        carregarLivros(txtQtd, layoutSemResultados)

        // Começa escondido porque o campo inicia vazio
        btnLimparPesquisa.visibility = View.GONE

        // Ação para limpar o texto ao clicar no "X"
        btnLimparPesquisa.setOnClickListener {
            etPesquisa.text.clear() // Isso dispara automaticamente o doAfterTextChanged abaixo
        }

        btnOrdenar.setOnClickListener {
            mostrarMenuOrdenacao(it, etPesquisa, txtQtd, layoutSemResultados)
        }

        etPesquisa.doAfterTextChanged { text ->
            val query = text.toString().trim()

            // Controla a visibilidade do botão de limpar dinamicamente
            btnLimparPesquisa.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE

            filtrarEAtualizar(etPesquisa, txtQtd, layoutSemResultados)
        }

        setupNavBar()
    }

    private fun carregarLivros(
        txtQtd: TextView,
        layoutSemResultados: LinearLayout
    ) {
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

                atualizarLista(livros, txtQtd, layoutSemResultados)
            }
    }

    private fun atualizarLista(
        lista: List<Livro>,
        txtQtd: TextView,
        layoutSemResultados: LinearLayout
    ) {
        adapter.updateList(lista)

        txtQtd.text = "${lista.size} livro(s) no acervo"

        if (lista.isEmpty()) {
            layoutSemResultados.visibility = View.VISIBLE
            findViewById<RecyclerView>(R.id.rvLivros).visibility = View.GONE
        } else {
            layoutSemResultados.visibility = View.GONE
            findViewById<RecyclerView>(R.id.rvLivros).visibility = View.VISIBLE
        }
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

    fun base64ToBitmap(base64: String): Bitmap {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun filtrarEAtualizar(
        etPesquisa: EditText,
        txtQtd: TextView,
        layoutSemResultados: LinearLayout
    ) {
        val query = etPesquisa.text.toString().trim()

        val filtrados = if (query.isEmpty()) {
            livros
        } else {
            livros.filter {
                it.titulo.contains(query, true) ||
                        it.autor.contains(query, true)
            }
        }

        atualizarLista(filtrados, txtQtd, layoutSemResultados)
    }

    private fun mostrarMenuOrdenacao(
        view: View,
        etPesquisa: EditText,
        txtQtd: TextView,
        layoutSemResultados: LinearLayout
    ) {
        val popup = PopupMenu(this, view)
        popup.menu.add("A - Z por título do Livro")
        popup.menu.add("Z - A por título do Livro")
        popup.menu.add("A - Z por título do Autor")
        popup.menu.add("Z - A por título do Autor")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "A - Z por título do Livro" -> livros.sortBy { it.titulo }
                "Z - A por título do Livro" -> livros.sortByDescending { it.titulo }
                "A - Z por título do Autor" -> livros.sortBy { it.autor }
                "Z - A por título do Autor" -> livros.sortByDescending { it.autor }
            }
            filtrarEAtualizar(etPesquisa, txtQtd, layoutSemResultados)
            true
        }
        popup.show()
    }
}