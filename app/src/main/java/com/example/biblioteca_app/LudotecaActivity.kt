package com.example.biblioteca_app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Jogo
import com.example.biblioteca_app.models.Sala
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class LudotecaActivity : AppCompatActivity() {

    private lateinit var rvJogos: RecyclerView
    private lateinit var rvSalas: RecyclerView
    private val listaJogos = mutableListOf<Jogo>()
    private val listaSalas = mutableListOf<Sala>()
    private lateinit var adapterJogos: GenericAdapter<Jogo>
    private lateinit var adapterSalas: GenericAdapter<Sala>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_ludoteca)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val tabJogos = findViewById<TextView>(R.id.tabJogos)
        val tabSalas = findViewById<TextView>(R.id.tabSalas)
        rvJogos = findViewById(R.id.rvJogos)
        rvSalas = findViewById(R.id.rvSalas)

        btnVoltar.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        setupRecyclerViews()
        carregarDadosFirestore()

        tabJogos.setOnClickListener {
            tabJogos.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabJogos.setTextColor(Color.parseColor("#002D5E"))
            tabSalas.setBackgroundColor(Color.WHITE)
            tabSalas.setTextColor(Color.BLACK)
            rvJogos.visibility = View.VISIBLE
            rvSalas.visibility = View.GONE
        }

        tabSalas.setOnClickListener {
            tabSalas.setBackgroundColor(Color.parseColor("#E3F2FD"))
            tabSalas.setTextColor(Color.parseColor("#002D5E"))
            tabJogos.setBackgroundColor(Color.WHITE)
            tabJogos.setTextColor(Color.BLACK)
            rvJogos.visibility = View.GONE
            rvSalas.visibility = View.VISIBLE
        }
        setupNavBar()
    }

    private fun setupRecyclerViews() {
        adapterJogos = GenericAdapter(R.layout.item_jogo_ludoteca, listaJogos) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtNomeJogo).text = item.nome
            view.findViewById<ImageView>(R.id.imgJogo).setImageResource(item.imagemRes)
            val btnAlugar = view.findViewById<Button>(R.id.btnAlugar)
            btnAlugar.setOnClickListener {
                atualizarBotaoSolicitacao(btnAlugar, "Aluguel solicitado!")
            }
        }
        rvJogos.layoutManager = LinearLayoutManager(this)
        rvJogos.adapter = adapterJogos

        adapterSalas = GenericAdapter(R.layout.item_sala_ludoteca, listaSalas) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtNomeSala).text = item.nome
            view.findViewById<TextView>(R.id.txtStatusSala).text = "Disponível\nCapacidade: ${item.capacidade} pessoas"
            val btnReservar = view.findViewById<Button>(R.id.btnReservar)
            btnReservar.setOnClickListener {
                atualizarBotaoSolicitacao(btnReservar, "Reserva solicitada!")
            }
        }
        rvSalas.layoutManager = LinearLayoutManager(this)
        rvSalas.adapter = adapterSalas
    }

    private fun carregarDadosFirestore() {
        val db = FirebaseFirestore.getInstance()

        // Carregar Jogos
        db.collection("jogos").get().addOnSuccessListener { documentos ->
            listaJogos.clear()
            for (doc in documentos) {
                val jogo = Jogo(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    imagemRes = (doc.getLong("imagemRes") ?: R.drawable.uno.toLong()).toInt()
                )
                listaJogos.add(jogo)
            }
            adapterJogos.updateList(listaJogos)
        }

        // Carregar Salas
        db.collection("salas").get().addOnSuccessListener { documentos ->
            listaSalas.clear()
            for (doc in documentos) {
                val sala = Sala(
                    id = doc.id,
                    nome = doc.getString("nome") ?: "",
                    capacidade = (doc.getLong("capacidade") ?: 0).toInt()
                )
                listaSalas.add(sala)
            }
            adapterSalas.updateList(listaSalas)
        }
    }

    private fun atualizarBotaoSolicitacao(botao: Button, mensagem: String) {
        botao.text = "Solicitação Enviada"
        botao.isEnabled = false
        botao.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9E9E9E"))
        botao.setTextColor(Color.WHITE)
        if (botao is com.google.android.material.button.MaterialButton) {
            botao.strokeWidth = 0
        }
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
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
