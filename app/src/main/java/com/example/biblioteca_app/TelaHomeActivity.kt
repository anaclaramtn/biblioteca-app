package com.example.biblioteca_app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
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

        val adapterNoticias = GenericAdapter(
            R.layout.item_noticia,
            emptyList<Noticia>()
        ) { view, noticia, position ->

            view.findViewById<TextView>(R.id.txtTituloNoticia).text = noticia.titulo
            view.findViewById<TextView>(R.id.txtDescricaoNoticia).text = noticia.descricao

            val img = view.findViewById<ImageView>(R.id.imgNoticia)
            when {
                !noticia.imagemBase64.isNullOrEmpty() -> {
                    try {
                        img.setImageBitmap(base64ToBitmap(noticia.imagemBase64))
                    } catch (e: Exception) {
                        img.setImageResource(R.drawable.logo)
                    }
                }
                noticia.imagemRes != null && noticia.imagemRes != 0 -> {
                    img.setImageResource(noticia.imagemRes)
                }
                else -> {
                    img.setImageResource(R.drawable.logo)
                }
            }

            view.findViewById<View>(R.id.btnSaibaMais).setOnClickListener {
                val intent = Intent(this, NoticiaCompletaActivity::class.java)
                intent.putExtra("NOTICIA", noticia)
                startActivity(intent)
            }
        }
        rvNoticias.adapter = adapterNoticias

        db.collection("noticias")
            .get()
            .addOnSuccessListener { documents ->
                val noticias = documents.map { doc ->
                    Noticia(
                        id = doc.id,
                        titulo = doc.getString("nome") ?: "",
                        descricao = doc.getString("descricaoCurta") ?: "",
                        descricaoLonga = doc.getString("descricaoLonga") ?: "",
                        imagemBase64 = doc.getString("imagemBase64")
                    )
                }
                if (noticias.isNotEmpty()) {
                    adapterNoticias.updateList(noticias)
                    setupDots(layoutDots, noticias.size)
                    updateDots(layoutDots, 0)
                }
            }
            .addOnFailureListener {
                // Silently fail or show empty
            }

        // SnapHelper para comportamento de carrossel (para parar na notícia centralizada)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvNoticias)

        // Configuração das Bolinhas (Indicadores)
        // Removido setupDots inicial pois agora é feito no callback do Firestore

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
            val total = adapterNoticias.itemCount
            if (total > 0 && currentPos != RecyclerView.NO_POSITION) {
                val targetPos = if (currentPos == 0) total - 1 else currentPos - 1
                rvNoticias.smoothScrollToPosition(targetPos)
            }
        }

        btnNext.setOnClickListener {
            val currentPos = (rvNoticias.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            val total = adapterNoticias.itemCount
            if (total > 0 && currentPos != RecyclerView.NO_POSITION) {
                val targetPos = if (currentPos == total - 1) 0 else currentPos + 1
                rvNoticias.smoothScrollToPosition(targetPos)
            }
        }

        carregarLivrosPopulares()
        carregarLivrosMaisCurtidos()
        carregarLivrosBemAvaliados()

        // Inicializa com dados exemplares (Mock) para garantir que os 6 apareçam sempre
        setupLivrosExemplares()

        setupNavBar()

        findViewById<View>(R.id.fabChatBot).setOnClickListener {
            startActivity(Intent(this, ChatBotActivity::class.java))
        }
    }

    fun base64ToBitmap(base64: String): Bitmap {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
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

    private fun setupLivrosExemplares() {
        val livroExemplo = Livro(
            titulo = "Dom Quixote",
            autor = "Miguel de Cervantes",
            descricao = "A história de um cavaleiro errante...",
            imagemRes = R.drawable.capadomquixote,
            disponivel = true,
            media = 4.5f,
            totalAvaliacoes = 50
        )

        // Preenche Populares
        val viewsPop = listOf(R.id.livroPop1, R.id.livroPop2, R.id.livroPop3, R.id.livroPop4, R.id.livroPop5, R.id.livroPop6)
        viewsPop.forEach { id -> setupItemLivro(findViewById(id), livroExemplo) }

        // Preenche Curtidos
        val viewsCurt = listOf(R.id.livroCurt1, R.id.livroCurt2, R.id.livroCurt3, R.id.livroCurt4, R.id.livroCurt5, R.id.livroCurt6)
        viewsCurt.forEach { id -> setupItemLivro(findViewById(id), livroExemplo) }

        // Preenche Bem Avaliados
        val viewsAval = listOf(R.id.livroAval1, R.id.livroAval2, R.id.livroAval3, R.id.livroAval4, R.id.livroAval5, R.id.livroAval6)
        viewsAval.forEach { id -> setupItemLivro(findViewById(id), livroExemplo) }
    }

    private fun carregarLivrosBemAvaliados() {
        db.collection("livros").get().addOnSuccessListener { livrosDocs ->
            val todosLivros = livrosDocs.map { doc ->
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
            }

            db.collection("avaliacoes").get().addOnSuccessListener { avalDocs ->
                val grupos = avalDocs.groupBy { it.get("idLivro")?.toString() ?: "" }
                val livrosComMedia = todosLivros.map { livro ->
                    val notas = grupos[livro.id]?.mapNotNull { it.getDouble("nota") } ?: emptyList()
                    val mediaCalculada = if (notas.isNotEmpty()) notas.average().toFloat() else 0f
                    livro.copy(media = mediaCalculada, totalAvaliacoes = notas.size)
                }

                val top6 = livrosComMedia.sortedByDescending { it.media }.take(6)
                mostrarLivrosBemAvaliados(top6)
            }
        }
    }

    private fun carregarLivrosPopulares() {
        db.collection("livros")
            .orderBy("totalAvaliacoes", Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { documentos ->
                val livros = mutableListOf<Livro>()
                for (documento in documentos) {
                    val livro = Livro(
                        id = documento.id,
                        titulo = documento.getString("titulo") ?: "",
                        autor = documento.getString("autor") ?: "",
                        descricao = documento.getString("sinopse") ?: "",
                        imagemBase64 = documento.getString("imagemBase64"),
                        disponivel = documento.getBoolean("disponivel") ?: true,
                        media = documento.getDouble("media")?.toFloat() ?: 0f,
                        totalAvaliacoes = documento.getLong("totalAvaliacoes")?.toInt() ?: 0
                    )
                    livros.add(livro)
                }
                mostrarLivrosPopulares(livros)
            }
    }

    private fun carregarLivrosMaisCurtidos() {
        db.collection("livros").get().addOnSuccessListener { livrosDocs ->
            val todosLivros = livrosDocs.map { doc ->
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
            }

            db.collection("livrosCurtidos").get().addOnSuccessListener { curtidasDocs ->
                val contagemCurtidas = curtidasDocs.groupBy { it.get("idLivro")?.toString() ?: "" }
                    .mapValues { it.value.size }

                val top6 = todosLivros.sortedByDescending { contagemCurtidas[it.id] ?: 0 }.take(6)
                mostrarLivrosMaisCurtidos(top6)
            }
        }
    }

    private fun mostrarLivrosPopulares(livros: List<Livro>) {
        val views = listOf(
            findViewById<View>(R.id.livroPop1),
            findViewById<View>(R.id.livroPop2),
            findViewById<View>(R.id.livroPop3),
            findViewById<View>(R.id.livroPop4),
            findViewById<View>(R.id.livroPop5),
            findViewById<View>(R.id.livroPop6)
        )
        livros.forEachIndexed { index, livro ->
            if (index < views.size) {
                setupItemLivro(views[index], livro)
            }
        }
    }

    private fun mostrarLivrosMaisCurtidos(livros: List<Livro>) {
        val views = listOf(
            findViewById<View>(R.id.livroCurt1),
            findViewById<View>(R.id.livroCurt2),
            findViewById<View>(R.id.livroCurt3),
            findViewById<View>(R.id.livroCurt4),
            findViewById<View>(R.id.livroCurt5),
            findViewById<View>(R.id.livroCurt6)
        )
        livros.forEachIndexed { index, livro ->
            if (index < views.size) {
                setupItemLivro(views[index], livro)
            }
        }
    }

    private fun mostrarLivrosBemAvaliados(livros: List<Livro>) {
        val views = listOf(
            findViewById<View>(R.id.livroAval1),
            findViewById<View>(R.id.livroAval2),
            findViewById<View>(R.id.livroAval3),
            findViewById<View>(R.id.livroAval4),
            findViewById<View>(R.id.livroAval5),
            findViewById<View>(R.id.livroAval6)
        )
        livros.forEachIndexed { index, livro ->
            if (index < views.size) {
                setupItemLivro(views[index], livro)
            }
        }
    }

    private fun setupItemLivro(view: View, livro: Livro) {

        view.findViewById<TextView>(R.id.txtTituloLivro).text = livro.titulo
        view.findViewById<TextView>(R.id.txtAutor).text = livro.autor

        val img = view.findViewById<ImageView>(R.id.imgCapa)

        when {
            !livro.imagemBase64.isNullOrEmpty() -> {
                try {
                    val bitmap = base64ToBitmap(livro.imagemBase64!!)
                    img.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    img.setImageResource(R.drawable.capadomquixote)
                }
            }

            livro.imagemRes != null && livro.imagemRes != 0 -> {
                img.setImageResource(livro.imagemRes!!)
            }

            else -> {
                img.setImageResource(R.drawable.capadomquixote)
            }
        }

        view.setOnClickListener {
            val intent = Intent(this, LivroActivity::class.java)
            intent.putExtra("LIVRO", livro)
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