package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Notificacao
import com.google.android.material.bottomnavigation.BottomNavigationView

class NotificacoesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: GenericAdapter<Notificacao>
    private val lista = mutableListOf<Notificacao>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_notificacoes)

        setupRecycler()
        setupBotaoLimpar()
        setupNavBar()
    }

    private fun setupRecycler() {
        recycler = findViewById(R.id.recyclerNotificacoes)

        lista.add(
            Notificacao(
                "Administração - Lembrete",
                "O livro 'O Hobbit' deve ser devolvido em 3 dias",
                "24/04/2026",
                false
            )
        )
        lista.add(
            Notificacao(
                "Solicitação",
                "Seu aluguel foi aprovado",
                "23/04/2026",
                true
            )
        )

        adapter = GenericAdapter(
            R.layout.item_notificacao,
            lista
        ) { view, item, position ->

            val titulo = view.findViewById<TextView>(R.id.titulo1)
            val msg = view.findViewById<TextView>(R.id.msg1)
            val data = view.findViewById<TextView>(R.id.data1)
            val indicador = view.findViewById<View>(R.id.indicadorNaoLido)
            val card = view as? androidx.cardview.widget.CardView

            titulo.text = item.titulo
            msg.text = item.mensagem
            data.text = item.data

            // 🔵 bolinha azul
            indicador.visibility = if (item.lida) View.GONE else View.VISIBLE

            // ✅ clicar = marcar como lido (sumir bolinha)
            card?.setOnClickListener {
                item.lida = true
                indicador.visibility = View.GONE
                adapter.updateItem(position, item)
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun setupBotaoLimpar() {
        val btnLimpar = findViewById<TextView>(R.id.btnLimpar)

        btnLimpar.setOnClickListener {
            lista.forEach { it.lida = true }
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_notif

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
                R.id.nav_notif -> true
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