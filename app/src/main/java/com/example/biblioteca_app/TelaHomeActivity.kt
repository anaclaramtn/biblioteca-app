package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Livro
import com.example.biblioteca_app.models.Noticia
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TelaHomeActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_home)

        val header = findViewById<View>(R.id.header)

        val titulo = header.findViewById<TextView>(R.id.txtTitulo)
        val btnBack = header.findViewById<ImageView>(R.id.btnBack)

        val user = auth.currentUser

        user?.let{
            db.collection("usuarios")
                .document(it.uid)
                .get()
                .addOnSuccessListener { documento ->
                    val nome = documento.getString("nome")
                    titulo.text = "Olá, $nome"
                }
                .addOnFailureListener {
                    titulo.text = "Olá!"
                }
        }

        // Garante que NÃO aparece botão de voltar
        btnBack.visibility = View.GONE
        
        // Configuração das Notícias
        val rvNoticias = findViewById<RecyclerView>(R.id.rvNoticias)
        val layoutDots = findViewById<LinearLayout>(R.id.layoutDots)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevNoticia)
        val btnNext = findViewById<ImageButton>(R.id.btnNextNoticia)

        val listaNoticias = listOf(
            Noticia("Novo jogo chega à biblioteca!", "Venha conferir o mais novo jogo de tabuleiro que chegou na ludoteca da biblioteca.", R.drawable.war),
            Noticia("Professor Boba Fett", "Inacreditável! Um professor de Computação deu aula totalmente fantasiado de Boba Fett de Star Wars hoje.", R.drawable.logo)
        )

        rvNoticias.adapter = GenericAdapter(
            R.layout.item_noticia,
            listaNoticias
        ) { view, noticia, position ->

            view.findViewById<TextView>(R.id.txtTituloNoticia).text = noticia.titulo
            view.findViewById<TextView>(R.id.txtDescricaoNoticia).text = noticia.descricao

            noticia.imagemRes?.let {
                view.findViewById<ImageView>(R.id.imgNoticia).setImageResource(it)
            }

            view.findViewById<View>(R.id.btnSaibaMais).setOnClickListener {
                val intent = Intent(this, NoticiaCompletaActivity::class.java)
                intent.putExtra("TITULO", noticia.titulo)
                intent.putExtra("IMAGEM", noticia.imagemRes ?: R.drawable.logo)
                startActivity(intent)
            }
        }

        // SnapHelper para comportamento de carrossel (para parar na notícia centralizada)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvNoticias)

        // Configuração das Bolinhas (Indicadores)
        setupDots(layoutDots, listaNoticias.size)
        updateDots(layoutDots, 0)

        rvNoticias.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        updateDots(layoutDots, position)
                    }
                }
            }
        })

        // Botões de Navegação
        btnPrev.setOnClickListener {
            val currentPos = (rvNoticias.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (currentPos > 0) rvNoticias.smoothScrollToPosition(currentPos - 1)
        }

        btnNext.setOnClickListener {
            val currentPos = (rvNoticias.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (currentPos < listaNoticias.size - 1) rvNoticias.smoothScrollToPosition(currentPos + 1)
        }

        carregarLivrosPopulares()
        carregarLivrosBemAvaliados()

        setupNavBar()

        findViewById<View>(R.id.fabChatBot).setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java))
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

//    private fun setupLivrosCarrossel() {
//        // Populares
//        setupItemLivro(findViewById(R.id.livroPop1), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//        setupItemLivro(findViewById(R.id.livroPop2), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//        setupItemLivro(findViewById(R.id.livroPop3), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//
//        // Mais Bem Avaliados
//        setupItemLivro(findViewById(R.id.livroAval1), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//        setupItemLivro(findViewById(R.id.livroAval2), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//        setupItemLivro(findViewById(R.id.livroAval3), R.drawable.capadomquixote, "Dom Quixote", "Miguel de Cervantes")
//    }

//    private fun carregarLivros() {
//
//        db.collection("livros")
//            .get()
//            .addOnSuccessListener { documentos ->
//
//                val livros = mutableListOf<Livro>()
//
//                for (documento in documentos) {
//
//                    val livro = Livro(
//                        id = documento.id,
//                        titulo = documento.getString("titulo") ?: "",
//                        autor = documento.getString("autor") ?: "",
//                        descricao = documento.getString("sinopse") ?: "",
//                        disponivel = documento.getBoolean("disponivel") ?: true,
//                        media = documento.getDouble("media")?.toFloat() ?: 0f,
//                        totalAvaliacoes = documento.getLong("totalAvaliacoes")?.toInt() ?: 0,
//
//                        imagemRes = R.drawable.capadomquixote
//                    )
//
//                    livros.add(livro)
//                }
//
//                mostrarLivros(livros)
//            }
//    }
    private fun carregarLivrosBemAvaliados() {

        db.collection("livros")
            .orderBy("media", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documentos ->

                val livros = mutableListOf<Livro>()

                for (documento in documentos) {

                    val livro = Livro(
                        id = documento.id,
                        titulo = documento.getString("titulo") ?: "",
                        autor = documento.getString("autor") ?: "",
                        descricao = documento.getString("sinopse") ?: "",
                        imagemRes = R.drawable.capadomquixote,
                        disponivel = documento.getBoolean("disponivel") ?: true,
                        media = documento.getDouble("media")?.toFloat() ?: 0f,
                        totalAvaliacoes = documento.getLong("totalAvaliacoes")?.toInt() ?: 0
                    )

                    livros.add(livro)
                }

                mostrarLivrosBemAvaliados(livros)
            }
    }

    private fun mostrarLivrosPopulares(
        livros: List<Livro>
    ) {

        val views = listOf(
            findViewById<View>(R.id.livroPop1),
            findViewById<View>(R.id.livroPop2),
            findViewById<View>(R.id.livroPop3)
        )

        livros.forEachIndexed { index, livro ->

            if (index < views.size) {

                setupItemLivro(
                    views[index],
                    livro
                )
            }
        }
    }
    private fun mostrarLivrosBemAvaliados(
        livros: List<Livro>
    ) {

        val views = listOf(
            findViewById<View>(R.id.livroAval1),
            findViewById<View>(R.id.livroAval2),
            findViewById<View>(R.id.livroAval3)
        )

        livros.forEachIndexed { index, livro ->

            if (index < views.size) {

                setupItemLivro(
                    views[index],
                    livro
                )
            }
        }
    }

    private fun carregarLivrosPopulares() {

        db.collection("livros")
            .orderBy("totalAvaliacoes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { documentos ->

                val livros = mutableListOf<Livro>()

                for (documento in documentos) {

                    val livro = Livro(
                        id = documento.id,
                        titulo = documento.getString("titulo") ?: "",
                        autor = documento.getString("autor") ?: "",
                        descricao = documento.getString("sinopse") ?: "",
                        imagemRes = R.drawable.capadomquixote,
                        disponivel = documento.getBoolean("disponivel") ?: true,
                        media = documento.getDouble("media")?.toFloat() ?: 0f,
                        totalAvaliacoes = documento.getLong("totalAvaliacoes")?.toInt() ?: 0
                    )

                    livros.add(livro)
                }

                mostrarLivrosPopulares(livros)
            }
    }

    private fun setupItemLivro(
        view: View,
        livro: Livro
    ) {

        view.findViewById<TextView>(
            R.id.txtTituloLivro
        ).text = livro.titulo

        view.findViewById<TextView>(
            R.id.txtAutor
        ).text = livro.autor

        view.findViewById<ImageView>(
            R.id.imgCapa
        ).setImageResource(
            R.drawable.capadomquixote
        )

        view.setOnClickListener {

            val intent = Intent(
                this,
                LivroActivity::class.java
            )

            intent.putExtra(
                "LIVRO",
                livro
            )

            startActivity(intent)
        }
    }

    private fun setupDots(container: LinearLayout, count: Int) {
        container.removeAllViews()
        for (i in 0 until count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setBackgroundResource(android.R.drawable.presence_invisible) // Círculo cinza simples
            dot.alpha = 0.3f
            container.addView(dot)
        }
    }

    private fun updateDots(container: LinearLayout, activeIndex: Int) {
        for (i in 0 until container.childCount) {
            val dot = container.getChildAt(i)
            dot.alpha = if (i == activeIndex) 1.0f else 0.3f
        }
    }
}