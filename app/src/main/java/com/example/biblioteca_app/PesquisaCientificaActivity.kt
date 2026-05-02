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

class PesquisaCientificaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_pesquisacientifica)

        val btnVoltar = findViewById<ImageView>(R.id.btnVoltar)
        val recycler = findViewById<RecyclerView>(R.id.recyclerPesquisas)

        btnVoltar.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // 🔹 Lista de dados (substitui seus cards fixos)
        val lista = listOf(
            PesquisaAdm(
                "Prof. Osvaldo",
                "Dúvida em relação à norma ABNT",
                "Seg, Qua, Sex\nSalas: D08, J10, M14\n13h - 17h"
            ),
            PesquisaAdm(
                "Monitor Gabriel",
                "Suporte relacionado à área de TCC",
                "Seg, Qua, Sex\nSalas: D08, K10, J14\n13h - 17h"
            )
        )

        // 🔹 Adapter genérico
        val adapter = GenericAdapter<PesquisaAdm>(
            R.layout.item_pesquisa,
            lista
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
    }
}