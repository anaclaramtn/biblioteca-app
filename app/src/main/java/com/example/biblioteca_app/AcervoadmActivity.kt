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
        val listaNoticias = mutableListOf(
            Noticia("Título da notícia", "Descrição breve da notícia para o admin."),
            Noticia("Evento na Biblioteca", "Nova ala de estudos aberta."),
            Noticia("Manutenção", "Sistema ficará offline no domingo.")
        )
        val listaJogos = mutableListOf(
            Jogo("UNO", R.drawable.uno),
            Jogo("WAR", R.drawable.war),
            Jogo("Catan", R.drawable.uno)
        )
        val listaSalas = mutableListOf(
            Sala("SALA 01", 10),
            Sala("SALA 02", 10),
            Sala("AUDITÓRIO", 50),
            Sala("SALA 03", 8),
            Sala("SALA 04", 12),
            Sala("SALA 05", 10)
        )
        val listaPesquisas = mutableListOf(
            PesquisaAdm("Prof. Osvaldo", "Dúvida em relação à norma ABNT", "Dias disponíveis:\nSeg, Qua, Sex\nSalas Disponíveis:\nB01, B02, B05\nHorários:\n7h - 11h"),
            PesquisaAdm("Monitor Gabriel", "Dúvidas relacionadas à ideias de TCC", "Dias disponíveis:\nSeg, Qua, Sex\nSalas Disponíveis:\nB01, B02, B05\nHorários:\n11h - 14h")
        )
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
                        imagemUri = doc.getString("imagemUri") ?: "",
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
                item.imagemUri.isNotEmpty() -> {
                    try {
                        imgCapa.setImageURI(Uri.parse(item.imagemUri))
                    } catch (e: SecurityException) {
                        imgCapa.setImageResource(R.drawable.capadomquixote)
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
                            listaNoticias.removeAt(position)
                            recycler.adapter?.notifyItemRemoved(position)
                            recycler.adapter?.notifyItemRangeChanged(position, listaNoticias.size)
                            Toast.makeText(this, "Notícia '${item.titulo}' deletada!", Toast.LENGTH_SHORT).show()
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
            view.findViewById<ImageView>(R.id.imgJogo).setImageResource(item.imagemRes)
            view.findViewById<TextView>(R.id.txtNomeJogo).text = item.nome

            view.setOnClickListener { v ->
                val popup = PopupMenu(this, v)
                popup.menu.add("Editar")
                popup.menu.add("Deletar")

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Editar" -> {
                            startActivity(Intent(this, CadastroJogoActivity::class.java))
                            true
                        }
                        "Deletar" -> {
                            listaJogos.removeAt(position)
                            recycler.adapter?.notifyItemRemoved(position)
                            recycler.adapter?.notifyItemRangeChanged(position, listaJogos.size)
                            Toast.makeText(this, "Jogo '${item.nome}' deletado!", Toast.LENGTH_SHORT).show()
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
                            startActivity(Intent(this, CadastroSalaActivity::class.java))
                            true
                        }
                        "Deletar" -> {
                            listaSalas.removeAt(position)
                            recycler.adapter?.notifyItemRemoved(position)
                            recycler.adapter?.notifyItemRangeChanged(position, listaSalas.size)
                            Toast.makeText(this, "Sala '${item.nome}' deletada!", Toast.LENGTH_SHORT).show()
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
                                    recycler.adapter?.notifyItemRemoved(position)
                                    recycler.adapter?.notifyItemRangeChanged(position, listaPesquisas.size)
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
