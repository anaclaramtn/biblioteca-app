package com.example.biblioteca_app

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.ChatMessage
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatBotActivity : AppCompatActivity() {

    private lateinit var adapter: GenericAdapter<ChatMessage>
    private val messages = mutableListOf<ChatMessage>()

    // GEMINI
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyDiZlu-ahEDkL8qkfcyZh9u6h9NZMxI3Vs"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_chatbot)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootChatBot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (ime.bottom > systemBars.bottom) ime.bottom else systemBars.bottom
            )

            insets
        }

        setupHeader()
        setupChat()
        setupInput()

        if (messages.isEmpty()) {
            addBotMessage("Olá! Sou o assistente da biblioteca. Como posso ajudar?")
        }
    }

    private fun setupHeader() {

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.txtNomeChatBot).text = "IAurelio"
    }

    private fun setupChat() {

        val rvChat = findViewById<RecyclerView>(R.id.rvChat)

        adapter = GenericAdapter(R.layout.item_chat_bot, messages) { view, message, _ ->

            val layoutBot = view.findViewById<View>(R.id.layoutBot)
            val layoutUser = view.findViewById<View>(R.id.layoutUser)

            val txtBot = view.findViewById<TextView>(R.id.txtMensagemBot)
            val txtUser = view.findViewById<TextView>(R.id.txtMensagemUser)

            if (message.isFromUser) {

                layoutBot.visibility = View.GONE
                layoutUser.visibility = View.VISIBLE

                txtUser.text = message.text

            } else {

                layoutBot.visibility = View.VISIBLE
                layoutUser.visibility = View.GONE

                txtBot.text = message.text
            }
        }

        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        rvChat.adapter = adapter
    }

    private fun setupInput() {

        val edtMensagem = findViewById<EditText>(R.id.edtMensagem)
        val btnEnviar = findViewById<ImageButton>(R.id.btnEnviar)

        btnEnviar.setOnClickListener {

            val text = edtMensagem.text.toString().trim()

            if (text.isNotEmpty()) {

                addUserMessage(text)

                edtMensagem.text.clear()

                sendMessageToGemini(text)
            }
        }
    }

    private fun addUserMessage(text: String) {
        adapter.addItem(ChatMessage(text, true))
        scrollToBottom()
    }

    private fun addBotMessage(text: String) {
        adapter.addItem(ChatMessage(text, false))
        scrollToBottom()
    }

    private fun sendMessageToGemini(userMessage: String) {

        CoroutineScope(Dispatchers.IO).launch {

            try {

                // PERSONALIDADE DO BOT
                val prompt = """
                    Você é um assistente virtual de um aplicativo de biblioteca.
                    
                    Regras:
                    - Responda de forma que voce consiga se conectar com universitarios. Fale descontraidamente, mas sem perder a formalidade. Recomende tambem alguns livros baseados nas pesquisas dos usuarios(apenas quando for solicitado que o faca)
                    - Ajude usuários a encontrar livros.
                    - Tire duvidas sobre livros, mas sem dar spoiler.
                    - Explique funcionalidades do app.
                    - Nunca fale palavrão.
                    - Seu nome é IAurelio.
                    - Quando o assunto fugir do tema biblioteca, redirecione para tal.
                    
                    Pergunta do usuário:
                    $userMessage
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)

                val botResponse = response.text ?: "Não consegui responder."

                withContext(Dispatchers.Main) {

                    addBotMessage(botResponse)
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    addBotMessage("Erro ao conectar com a IA.")
                }
            }
        }
    }

    private fun scrollToBottom() {
        findViewById<RecyclerView>(R.id.rvChat)
            .scrollToPosition(adapter.itemCount - 1)
    }
}