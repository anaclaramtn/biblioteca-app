package com.example.biblioteca_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

import com.example.biblioteca_app.models.Jogo

class CadastroJogoActivity : AppCompatActivity() {

    // Guarda a URI da imagem selecionada
    private var imageUri: Uri? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_cadastro_jogo)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Configura o seletor de galeria
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                imageUri = uri

                // Solicita permissão persistente para a URI
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val imageView = findViewById<ImageView>(R.id.imgCapaJogo)
                val textView = findViewById<TextView>(R.id.txtMensagemImagem)

                imageView.setImageURI(uri)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.imageTintList = null
                textView.visibility = View.GONE
            }
        }

        // Clique para abrir a galeria
        findViewById<LinearLayout>(R.id.containerSelecaoImagem).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            confirmarSaida()
        }

        findViewById<AppCompatButton>(R.id.btnEnviar).setOnClickListener {
            val titulo = findViewById<EditText>(R.id.edtTituloJogo).text.toString()

            // Valida se o título e a imagem foram preenchidos
            if (titulo.isBlank() || imageUri == null) {
                exibirAviso()
            } else {
                // Prepara os dados do jogo para o Firestore
                val dadosJogo = hashMapOf(
                    "nome" to titulo,
                    "imagemCapa" to imageUri.toString(),
                    "isDisponivel" to true
                )

                // Salva o jogo no banco de dados (Firestore)
                db.collection("jogos")
                    .add(dadosJogo)
                    .addOnSuccessListener {
                        // Também adiciona na lista local para feedback imediato (opcional)
                        AcervoadmActivity.listaJogos.add(0, Jogo(titulo, R.drawable.uno))
                        
                        Toast.makeText(this, "Jogo cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar no banco: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        setupNavBar()
    }

    private fun confirmarSaida() {
        AlertDialog.Builder(this)
            .setTitle("Certeza que deseja voltar?")
            .setMessage("Suas informações não serão salvas.")
            .setPositiveButton("Sim") { _, _ -> finish() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun exibirAviso() {
        AlertDialog.Builder(this)
            .setTitle("Aviso")
            .setMessage("Todos os campos precisam ser preenchidos")
            .setPositiveButton("OK", null)
            .show()
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
                R.id.nav_acervo -> {
                    startActivity(Intent(this, AcervoadmActivity::class.java))
                    finish()
                    true
                }
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
