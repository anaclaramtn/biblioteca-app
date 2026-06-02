package com.example.biblioteca_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class AcervoadmActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private val categorias = listOf("Livros", "Notícias", "Jogos", "Salas", "Pesquisa Científica")

    companion object {
        val listaLivros = mutableListOf<Livro>()
        val listaNoticias = mutableListOf<Noticia>()
        val listaJogos = mutableListOf<Jogo>()
        val listaSalas = mutableListOf<Sala>()
        val listaPesquisas = mutableListOf<PesquisaAdm>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_acervoadm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recycler = findViewById(R.id.recyclerAcervo)
        carregarLivros()
        carregarPesquisas()
        carregarNoticias()
        setupSpinner()
        setupNavBar()

        findViewById<View>(R.id.btnAdicionar).setOnClickListener {
            val categoria = findViewById<Spinner>(R.id.spinnerFiltro).selectedItem.toString()
            val intent = when (categoria) {
                "Livros" -> Intent(this, CadastroLivroActivity::class.java)
                "Notícias" -> Intent(this, CadastroNoticiaActivity::class.java)
                "Salas" -> Intent(this, CadastroSalaActivity::class.java)
                "Pesquisa Científica" -> Intent(this, CadastroPesquisaActivity::class.java)
                "Jogos" -> Intent(this, CadastroJogoActivity::class.java)
                else -> {
                    Toast.makeText(this, "Adicionar novo item em: $categoria", Toast.LENGTH_SHORT).show()
                    null
                }
            }
            intent?.let { startActivity(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        carregarLivros()
        carregarPesquisas()
        carregarJogos()
        carregarSalas()
        carregarNoticias()
        val spinner = findViewById<Spinner>(R.id.spinnerFiltro)
        atualizarConteudo(spinner.selectedItem.toString())
    }

    private fun carregarLivros() {

        FirebaseFirestore.getInstance()
            .collection("livros")
            .get()
            .addOnSuccessListener { documentos ->

                listaLivros.clear()

                for (doc in documentos) {

                    val livro = Livro(
                        id = doc.id,
                        titulo = doc.getString("titulo") ?: "",
                        autor = doc.getString("autor") ?: "",
                        descricao = doc.getString("sinopse") ?: "",
                        imagemBase64 = doc.getString("imagemBase64"),
                        disponivel = doc.getBoolean("disponivel") ?: true,
                        media = (doc.getDouble("media") ?: 0.0).toFloat(),
                        totalAvaliacoes = (doc.getLong("totalAvaliacoes") ?: 0).toInt()
                    )

                    listaLivros.add(livro)
                }

                setupLivros()
            }
    }

    private fun carregarPesquisas() {
        FirebaseFirestore.getInstance()
            .collection("pesquisaCientifica")
            .get()
            .addOnSuccessListener { documentos ->
                listaPesquisas.clear()
                for (doc in documentos) {
                    val pesquisa = PesquisaAdm(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        descricao = doc.getString("descricao") ?: "",
                        disponibilidade = doc.getString("disponibilidade") ?: ""
                    )
                    listaPesquisas.add(pesquisa)
                }
                if (findViewById<Spinner>(R.id.spinnerFiltro).selectedItem.toString() == "Pesquisa Científica") {
                    setupPesquisa()
                }
            }
    }

    private fun carregarNoticias() {
        FirebaseFirestore.getInstance()
            .collection("noticias")
            .get()
            .addOnSuccessListener { documentos ->
                listaNoticias.clear()
                for (doc in documentos) {
                    val noticia = Noticia(
                        id = doc.id,
                        titulo = doc.getString("nome") ?: "",
                        descricao = doc.getString("descricaoCurta") ?: "",
                        descricaoLonga = doc.getString("descricaoLonga") ?: "",
                        imagemBase64 = doc.getString("imagemBase64")
                    )
                    listaNoticias.add(noticia)
                }
                if (findViewById<Spinner>(R.id.spinnerFiltro).selectedItem.toString() == "Notícias") {
                    setupNoticias()
                }
            }
    }

    private fun carregarJogos() {
        FirebaseFirestore.getInstance()
            .collection("jogos")
            .get()
            .addOnSuccessListener { documentos ->
                listaJogos.clear()
                for (doc in documentos) {
                    val jogo = Jogo(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        imagemRes = (doc.getLong("imagemRes") ?: R.drawable.uno.toLong()).toInt(),
                        imagemBase64 = doc.getString("imagemBase64")
                    )
                    listaJogos.add(jogo)
                }
                if (findViewById<Spinner>(R.id.spinnerFiltro).selectedItem.toString() == "Jogos") {
                    setupJogos()
                }
            }
    }

    private fun carregarSalas() {
        FirebaseFirestore.getInstance()
            .collection("salas")
            .get()
            .addOnSuccessListener { documentos ->
                listaSalas.clear()
                for (doc in documentos) {
                    val sala = Sala(
                        id = doc.id,
                        nome = doc.getString("nome") ?: "",
                        capacidade = (doc.getLong("capacidade") ?: 0).toInt()
                    )
                    listaSalas.add(sala)
                }
                if (findViewById<Spinner>(R.id.spinnerFiltro).selectedItem.toString() == "Salas") {
                    setupSalas()
                }
            }
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerFiltro)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                atualizarConteudo(categorias[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun atualizarConteudo(categoria: String) {
        when (categoria) {
            "Livros" -> setupLivros()
            "Notícias" -> setupNoticias()
            "Jogos" -> setupJogos()
            "Salas" -> setupSalas()
            "Pesquisa Científica" -> setupPesquisa()
        }
    }

    private fun setupLivros() {
        recycler.layoutManager = GridLayoutManager(this, 3)

        recycler.adapter = GenericAdapter(
            R.layout.item_livro,
            listaLivros
        ) { view, item, position ->
            val imgCapa = view.findViewById<ImageView>(R.id.imgCapa)
            
            when {
                !item.imagemBase64.isNullOrEmpty() -> {
                    try {
                        val decodedBytes = android.util.Base64.decode(item.imagemBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imgCapa.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgCapa.setImageResource(R.drawable.capadomquixote)
                    }
                }
                item.imagemRes != null && item.imagemRes != 0 -> {
                    imgCapa.setImageResource(item.imagemRes!!)
                }
                else -> {
                    imgCapa.setImageResource(R.drawable.capadomquixote)
                }
            }

            view.findViewById<TextView>(R.id.txtTituloLivro).text = item.titulo
            view.findViewById<TextView>(R.id.txtAutor).text = item.autor

            view.setOnClickListener {
                val intent = Intent(this, AdminLivroActivity::class.java)

                intent.putExtra("LIVRO", item)
                intent.putExtra("LIVRO_POS", position)

                startActivity(intent)
            }
        }
    }

    private fun setupNoticias() {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = GenericAdapter(R.layout.item_noticia, listaNoticias) { view, item, position ->
            val imgNoticia = view.findViewById<ImageView>(R.id.imgNoticia)
            
            when {
                !item.imagemBase64.isNullOrEmpty() -> {
                    try {
                        val decodedBytes = android.util.Base64.decode(item.imagemBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imgNoticia.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgNoticia.setImageResource(R.drawable.logo)
                    }
                }
                item.imagemRes != null && item.imagemRes != 0 -> {
                    imgNoticia.setImageResource(item.imagemRes!!)
                }
                else -> {
                    imgNoticia.setImageResource(R.drawable.logo)
                }
            }

            view.findViewById<TextView>(R.id.txtTituloNoticia).text = item.titulo
            view.findViewById<TextView>(R.id.txtDescricaoNoticia).text = item.descricao

            view.setOnClickListener { v ->
                val popup = PopupMenu(this, v)
                popup.menu.add("Editar")
                popup.menu.add("Deletar")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> {
                            val intent = Intent(this, CadastroNoticiaActivity::class.java)
                            startActivity(intent)
                            true
                        }
                        "Deletar" -> {
                            FirebaseFirestore.getInstance().collection("noticias").document(item.id).delete()
                                .addOnSuccessListener {
                                    listaNoticias.removeAt(position)
                                    (recycler.adapter as? GenericAdapter<Noticia>)?.removeAt(position)
                                    Toast.makeText(this, "Notícia '${item.titulo}' deletada!", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private fun setupJogos() {
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = GenericAdapter(R.layout.item_jogo, listaJogos) { view, item, position ->
            val imgJogo = view.findViewById<ImageView>(R.id.imgJogo)
            
            when {
                !item.imagemBase64.isNullOrEmpty() -> {
                    try {
                        val decodedBytes = android.util.Base64.decode(item.imagemBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        imgJogo.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        imgJogo.setImageResource(R.drawable.uno)
                    }
                }
                item.imagemRes != null && item.imagemRes != 0 -> {
                    imgJogo.setImageResource(item.imagemRes)
                }
                else -> {
                    imgJogo.setImageResource(R.drawable.uno)
                }
            }

            view.findViewById<TextView>(R.id.txtNomeJogo).text = item.nome

            view.setOnClickListener { v ->
                val popup = PopupMenu(this, v)
                popup.menu.add("Editar")
                popup.menu.add("Deletar")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> {
                            val intent = Intent(this, CadastroJogoActivity::class.java)
                            intent.putExtra("JOGO", item)
                            startActivity(intent)
                            true
                        }
                        "Deletar" -> {
                            FirebaseFirestore.getInstance().collection("jogos").document(item.id).delete()
                                .addOnSuccessListener {
                                    listaJogos.removeAt(position)
                                    (recycler.adapter as? GenericAdapter<Jogo>)?.removeAt(position)
                                    Toast.makeText(this, "Jogo '${item.nome}' deletado!", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private fun setupSalas() {
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = GenericAdapter(R.layout.item_sala, listaSalas) { view, item, position ->
            view.findViewById<TextView>(R.id.txtNomeSala).text = item.nome
            view.findViewById<TextView>(R.id.txtCapacidade).text = "capacidade: ${item.capacidade} pessoas"

            view.setOnClickListener { v ->
                val popup = PopupMenu(this, v)
                popup.menu.add("Editar")
                popup.menu.add("Deletar")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> {
                            val intent = Intent(this, CadastroSalaActivity::class.java)
                            intent.putExtra("SALA", item)
                            startActivity(intent)
                            true
                        }
                        "Deletar" -> {
                            FirebaseFirestore.getInstance().collection("salas").document(item.id).delete()
                                .addOnSuccessListener {
                                    listaSalas.removeAt(position)
                                    (recycler.adapter as? GenericAdapter<Sala>)?.removeAt(position)
                                    Toast.makeText(this, "Sala '${item.nome}' deletada!", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private fun setupPesquisa() {
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = GenericAdapter(R.layout.item_pesquisa_adm, listaPesquisas) { view, item, position ->
            view.findViewById<TextView>(R.id.txtNomePesquisa).text = item.nome
            view.findViewById<TextView>(R.id.txtDescricaoPesquisa).text = item.descricao
            view.findViewById<TextView>(R.id.txtDisponibilidade).text = item.disponibilidade

            view.setOnClickListener { v ->
                val popup = PopupMenu(this, v)
                popup.menu.add("Editar")
                popup.menu.add("Deletar")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> {
                            val intent = Intent(this, CadastroPesquisaActivity::class.java)
                            intent.putExtra("PESQUISA", item)
                            startActivity(intent)
                            true
                        }
                        "Deletar" -> {
                            FirebaseFirestore.getInstance().collection("pesquisaCientifica").document(item.id).delete()
                                .addOnSuccessListener {
                                    listaPesquisas.removeAt(position)
                                    (recycler.adapter as? GenericAdapter<PesquisaAdm>)?.removeAt(position)
                                    Toast.makeText(this, "Pesquisa de '${item.nome}' deletada!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Erro ao deletar!", Toast.LENGTH_SHORT).show()
                                }
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_acervo

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_acervo -> true
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
