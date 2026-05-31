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

import com.example.biblioteca_app.models.Noticia

class CadastroNoticiaActivity : AppCompatActivity() {

    // Guarda a URI da imagem selecionada
    private var imageUri: Uri? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_cadastro_noticia)

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

                val imageView = findViewById<ImageView>(R.id.imgNoticia)
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
            val titulo = findViewById<EditText>(R.id.edtTituloNoticia).text.toString()
            val desc = findViewById<EditText>(R.id.edtDescricaoCurta).text.toString()
            val corpo = findViewById<EditText>(R.id.edtCorpoNoticia).text.toString()

            // Valida se todos os campos e a imagem foram preenchidos
            if (titulo.isBlank() || desc.isBlank() || corpo.isBlank() || imageUri == null) {
                exibirAviso()
            } else {
                // Prepara os dados da notícia para o Firestore
                val dadosNoticia = hashMapOf(
                    "nome" to titulo,
                    "descricaoCurta" to desc,
                    "descricaoLonga" to corpo,
                    "imagemCapa" to imageUri.toString()
                )

                // Salva a notícia no banco de dados (Firestore)
                db.collection("noticias")
                    .add(dadosNoticia)
                    .addOnSuccessListener {
                        // Também adiciona na lista local para feedback imediato (opcional)
                        AcervoadmActivity.listaNoticias.add(0, Noticia(titulo, desc))
                        
                        Toast.makeText(this, "Notícia cadastrada com sucesso!", Toast.LENGTH_SHORT).show()
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
