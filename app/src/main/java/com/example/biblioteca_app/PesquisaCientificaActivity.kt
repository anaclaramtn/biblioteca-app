package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.PesquisaAdm
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.firestore.FirebaseFirestore

class PesquisaCientificaActivity : AppCompatActivity() {

    private lateinit var adapter: GenericAdapter<PesquisaAdm>
    private val listaPesquisas = mutableListOf<PesquisaAdm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_pesquisacientifica)

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val recycler = findViewById<RecyclerView>(R.id.recyclerPesquisas)
        val btnOrdenar = findViewById<android.view.View>(R.id.btnOrdenar)

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        btnOrdenar.setOnClickListener {
            mostrarDialogOrdenacao()
        }

        // 🔹 Adapter genérico
        adapter = GenericAdapter<PesquisaAdm>(
            R.layout.item_pesquisa,
            listaPesquisas
        ) { view, item, _ ->

            val nome = view.findViewById<TextView>(R.id.txtNomePesquisa)
            val descricao = view.findViewById<TextView>(R.id.txtDescricaoPesquisa)
            val disponibilidade = view.findViewById<TextView>(R.id.txtDisponibilidade)

            nome.text = item.nome
            descricao.text = item.descricao
            disponibilidade.text = item.disponibilidade
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        
        carregarDadosBanco()
        setupNavBar()
    }

    private fun carregarDadosBanco() {
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
                adapter.updateList(listaPesquisas)
            }
            .addOnFailureListener {
                android.widget.Toast.makeText(this, "Erro ao carregar dados", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogOrdenacao() {
        val opcoes = arrayOf("Mais próximos", "Alfabética (A-Z)", "Alfabética (Z-A)")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setItems(opcoes) { _, which ->
                val opcaoSelecionada = opcoes[which]
                android.widget.Toast.makeText(this, "Ordenando por: $opcaoSelecionada", android.widget.Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_menu

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