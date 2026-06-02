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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class UsuarioadmActivity : AppCompatActivity() {

    private lateinit var adapter: GenericAdapter<UsuarioDisplay>
    private val listaUsuariosOriginal = mutableListOf<UsuarioDisplay>()
    private val db = FirebaseFirestore.getInstance()

    data class UsuarioDisplay(
        val usuario: Usuario,
        val qtdLivros: Int,
        val multa: Double
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
        carregarUsuarios()
    }

    private fun carregarUsuarios() {
        db.collection("usuarios")
            .whereEqualTo("isAdmin", false) // Filtrar apenas usuários comuns
            .get()
            .addOnSuccessListener { snapshots ->
                listaUsuariosOriginal.clear()
                val usuarios = snapshots.mapNotNull { it.toObject(Usuario::class.java).copy(uid = it.id) }
                
                var carregados = 0
                if (usuarios.isEmpty()) {
                    adapter.updateList(emptyList())
                    return@addOnSuccessListener
                }

                for (user in usuarios) {
                    // Buscar histórico para cada usuário
                    db.collection("historico")
                        .whereEqualTo("idUsuario", user.uid)
                        .whereEqualTo("dataSaida", null) // Livros não devolvidos
                        .get()
                        .addOnSuccessListener { histSnap ->
                            val qtdLivros = histSnap.size()
                            var multaTotal = 0.0

                            for (doc in histSnap) {
                                val dataPrazo = doc.getTimestamp("dataPrazo")
                                if (dataPrazo != null) {
                                    val agora = Timestamp.now().toDate().time
                                    val prazo = dataPrazo.toDate().time
                                    if (agora > prazo) {
                                        val diff = agora - prazo
                                        val diasAtraso = (diff / (1000 * 60 * 60 * 24)).toInt()
                                        multaTotal += diasAtraso * 0.50
                                    }
                                }
                            }

                            listaUsuariosOriginal.add(UsuarioDisplay(user, qtdLivros, multaTotal))
                            carregados++

                            if (carregados == usuarios.size) {
                                adapter.updateList(listaUsuariosOriginal)
                            }
                        }
                        .addOnFailureListener {
                            carregados++
                            listaUsuariosOriginal.add(UsuarioDisplay(user, 0, 0.0))
                            if (carregados == usuarios.size) {
                                adapter.updateList(listaUsuariosOriginal)
                            }
                        }
                }
            }
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerUsuarios)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = GenericAdapter(
            R.layout.item_usuario,
            listaUsuariosOriginal
        ) { view, item, _ ->
            val usuario = item.usuario
            view.findViewById<TextView>(R.id.txtNomeUsuario).text = usuario.nome
            view.findViewById<TextView>(R.id.txtEmailUsuario).text = usuario.email
            view.findViewById<TextView>(R.id.txtQtdLivros).text = item.qtdLivros.toString()
            view.findViewById<TextView>(R.id.txtMulta).text = String.format(Locale.getDefault(), "R$: %.2f", item.multa)

            // Facilitando a alteração pelo Admin
            view.setOnClickListener {
                val intent = Intent(this@UsuarioadmActivity, DetalhesusuarioadmActivity::class.java)
                intent.putExtra("USUARIO_ID", usuario.uid)
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
                it.usuario.nome.contains(texto, ignoreCase = true) || 
                it.usuario.email.contains(texto, ignoreCase = true)
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