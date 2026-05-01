package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.example.biblioteca_app.models.Emprestimo
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class DetalhesusuarioadmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_detalhesusuarioadm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.txtMultaTotal).setOnClickListener {
            Toast.makeText(this, "Abrir diálogo para editar multa", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
            showDeleteConfirmationDialog()
        }

        setupRecyclerViews()
        setupNavBar()
    }

    private fun setupRecyclerViews() {
        // Dados de exemplo (Molde)
        val listaAtivos = listOf(
            Emprestimo("WAR", "28/03/2026", null, 0.0, R.drawable.war, true),
            Emprestimo("O Hobbit", "28/03/2026", null, 0.0, R.drawable.hobbit, true)
        )

        val listaHistorico = listOf(
            Emprestimo("SALA 01", "25/03/2026", "25/03/2026", 0.0, null, false),
            Emprestimo("Dom Quixote", "27/03/2026", "10/04/2026", 0.0, R.drawable.capadomquixote, false)
        )

        // Configuração Recycler Ativos
        val rvAtivos = findViewById<RecyclerView>(R.id.recyclerAtivos)
        rvAtivos.layoutManager = LinearLayoutManager(this)
        rvAtivos.adapter = GenericAdapter(R.layout.item_emprestimo_ativo, listaAtivos) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtTitulo).text = item.titulo
            view.findViewById<TextView>(R.id.txtDatas).text = "Aluguel: ${item.dataEmprestimo}"
            view.findViewById<TextView>(R.id.txtMulta).text = String.format(Locale.getDefault(), "Valor da multa: R$ %.2f", item.valorMulta)
            
            val img = view.findViewById<ImageView>(R.id.imgItem)
            if (item.imagemRes != null) {
                img.setImageResource(item.imagemRes)
                img.visibility = View.VISIBLE
            } else {
                img.visibility = View.GONE
            }
        }

        // Configuração Recycler Histórico
        val rvHistorico = findViewById<RecyclerView>(R.id.recyclerHistorico)
        rvHistorico.layoutManager = LinearLayoutManager(this)
        rvHistorico.adapter = GenericAdapter(R.layout.item_emprestimo_historico, listaHistorico) { view, item, _ ->
            view.findViewById<TextView>(R.id.txtTitulo).text = item.titulo
            view.findViewById<TextView>(R.id.txtDatas).text = "Aluguel: ${item.dataEmprestimo}\nDevolução: ${item.dataDevolucao}"
            view.findViewById<TextView>(R.id.txtMulta).text = String.format(Locale.getDefault(), "Valor da multa: R$ %.2f", item.valorMulta)
            
            val img = view.findViewById<ImageView>(R.id.imgItem)
            if (item.imagemRes != null) {
                img.setImageResource(item.imagemRes)
                img.visibility = View.VISIBLE
            } else {
                img.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar exclusão")
            .setMessage("Deseja realmente remover este usuário?")
            .setPositiveButton("Sim") { _, _ ->
                Toast.makeText(this, "Usuário removido", Toast.LENGTH_SHORT).show()
                finish() // Volta para a tela de listagem
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_usuarios

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TelaHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_acervo -> {
                    Toast.makeText(this, "Acessando Acervo...", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_usuarios -> {
                    startActivity(Intent(this, UsuarioadmActivity::class.java))
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
