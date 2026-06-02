package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Notificacao
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class NotificacoesActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: GenericAdapter<Notificacao>
    private val lista = mutableListOf<Notificacao>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_notificacoes)

        setupRecycler()
        carregarNotificacoes()
        setupBotaoLimpar()
        setupNavBar()
    }

    private fun setupRecycler() {
        recycler = findViewById(R.id.recyclerNotificacoes)

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
                if (!item.lida) {
                    item.lida = true
                    indicador.visibility = View.GONE
                    adapter.updateItem(position, item)

                    // Persistir no Firestore
                    db.collection("solicitacoes").document(item.id)
                        .update("lidaUsuario", true)
                }
            }
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun carregarNotificacoes() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("solicitacoes")
            .whereEqualTo("idUsuario", uid)
            .whereIn("status", listOf("aceito", "recusado"))
            .get()
            .addOnSuccessListener { documents ->
                lista.clear()
                val tempLista = mutableListOf<Notificacao>()
                var processados = 0

                if (documents.isEmpty) {
                    adapter.updateList(emptyList())
                    return@addOnSuccessListener
                }

                for (doc in documents) {
                    val status = doc.getString("status")
                    val tipoObjeto = doc.getString("tipoObjeto")
                    val idObjeto = doc.getString("idObjeto")
                    val dataResposta = doc.getTimestamp("dataResposta")
                    val lida = doc.getBoolean("lidaUsuario") ?: false

                    if (dataResposta != null && tipoObjeto != null && idObjeto != null) {
                        val colecao = when (tipoObjeto) {
                            "livro" -> "livros"
                            "jogo" -> "jogos"
                            "sala" -> "salas"
                            else -> ""
                        }

                        if (colecao.isNotEmpty()) {
                            db.collection(colecao).document(idObjeto).get().addOnSuccessListener { objDoc ->
                                val nomeObjeto = if (tipoObjeto == "livro") objDoc.getString("titulo") else objDoc.getString("nome")
                                val acao = if (status == "aceito") "aprovado" else "recusado"

                                val titulo = "Administração - Solicitação"
                                val mensagem = "Solicitação de aluguel do $tipoObjeto\n\"$nomeObjeto\" foi $acao."

                                val sdfTime = SimpleDateFormat("hh:mm a", Locale.US)
                                val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                                val dataFormatada = "${sdfTime.format(dataResposta.toDate()).lowercase()}\n${sdfDate.format(dataResposta.toDate())}"

                                tempLista.add(Notificacao(
                                    id = doc.id,
                                    titulo = titulo,
                                    mensagem = mensagem,
                                    data = dataFormatada,
                                    lida = lida,
                                    timestamp = dataResposta.seconds
                                ))

                                processados++
                                if (processados == documents.size()) {
                                    tempLista.sortByDescending { it.timestamp }
                                    lista.clear()
                                    lista.addAll(tempLista)
                                    adapter.updateList(lista)
                                }
                            }.addOnFailureListener {
                                processados++
                                if (processados == documents.size()) {
                                    tempLista.sortByDescending { it.timestamp }
                                    lista.clear()
                                    lista.addAll(tempLista)
                                    adapter.updateList(lista)
                                }
                            }
                        } else {
                            processados++
                        }
                    } else {
                        processados++
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar notificações", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBotaoLimpar() {
        val btnLimpar = findViewById<TextView>(R.id.btnLimpar)

        btnLimpar.setOnClickListener {
            lista.forEach { notificacao ->
                if (!notificacao.lida) {
                    notificacao.lida = true
                    db.collection("solicitacoes").document(notificacao.id)
                        .update("lidaUsuario", true)
                }
            }
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