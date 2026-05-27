package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Usuario
import com.google.android.material.bottomnavigation.BottomNavigationView

class UsuarioadmActivity : AppCompatActivity() {

    private lateinit var adapter: GenericAdapter<Usuario>
    private val listaUsuariosOriginal = listOf(
        Usuario(nome = "Bruno Facó", email = "bruno@fuja.com"),
        Usuario(nome = "Ygor Costa", email = "ygor@gmail.com"),
        Usuario(nome = "Breno Faca", email = "breno@fuja.com"),
        Usuario(nome = "Igor Frente", email = "igor@gmail.com"),
        Usuario(nome = "Maria Clara", email = "maria@fuja.com"),
        Usuario(nome = "Joao Pedro", email = "jp@gmail.com")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_usuarioadm)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupBusca()
        setupNavBar()
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = GenericAdapter(
            R.layout.item_usuario,
            listaUsuariosOriginal
        ) { view, usuario, _ ->
            view.findViewById<TextView>(R.id.txtNomeUsuario).text = usuario.nome
            view.findViewById<TextView>(R.id.txtEmailUsuario).text = usuario.email
            view.findViewById<TextView>(R.id.txtQtdLivros).text = "-"
            view.findViewById<TextView>(R.id.txtMulta).text = "R$: 0,00"

            // Facilitando a alteração pelo Admin
            view.setOnClickListener {
                val intent = Intent(this, DetalhesusuarioadmActivity::class.java)
                startActivity(intent)
            }
        }

        recycler.adapter = adapter
    }

    private fun setupBusca() {
        val editBusca = findViewById<EditText>(R.id.editBusca)
        val btnClear = findViewById<ImageView>(R.id.btnClearBusca)

        editBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarUsuarios(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClear.setOnClickListener {
            editBusca.text.clear()
        }
    }

    private fun filtrarUsuarios(texto: String) {
        val listaFiltrada = if (texto.isEmpty()) {
            listaUsuariosOriginal
        } else {
            listaUsuariosOriginal.filter {
                it.nome.contains(texto, ignoreCase = true) || 
                it.email.contains(texto, ignoreCase = true)
            }
        }

        val txtSemResultados = findViewById<TextView>(R.id.txtSemResultados)
        if (listaFiltrada.isEmpty()) {
            txtSemResultados.visibility = View.VISIBLE
        } else {
            txtSemResultados.visibility = View.GONE
        }

        adapter.updateList(listaFiltrada)
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_usuarios
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, AdminHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_usuarios -> {
                    // Já estamos nesta tela
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