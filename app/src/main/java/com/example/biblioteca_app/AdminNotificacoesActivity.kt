package com.example.biblioteca_app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminNotificacoesActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val itensNotificacao = mutableListOf<Pair<View, TipoNotificacao>>()
    private lateinit var botoesFiltro: List<Button>

    // Enum para definir qual molde usar e para filtragem
    enum class TipoNotificacao {
        LIVROS, JOGOS, SALAS, DEVOLUCOES, DENUNCIAS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_admin_notificacoes)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        container = findViewById(R.id.containerSolicitacoes)

        setupFiltros()
        carregarExemplos()
        setupNavBar()
    }


    private fun setupFiltros() {
        val btnTodas = findViewById<Button>(R.id.btnFiltroTodas)
        val btnLivros = findViewById<Button>(R.id.btnFiltroLivros)
        val btnJogos = findViewById<Button>(R.id.btnFiltroJogos)
        val btnSalas = findViewById<Button>(R.id.btnFiltroSalas)
        val btnDevolucoes = findViewById<Button>(R.id.btnFiltroDevolucoes)
        val btnDenuncias = findViewById<Button>(R.id.btnFiltroDenuncias)

        botoesFiltro = listOf(btnTodas, btnLivros, btnJogos, btnSalas, btnDevolucoes, btnDenuncias)

        btnTodas.setOnClickListener { aplicarFiltro(null, btnTodas) }
        btnLivros.setOnClickListener { aplicarFiltro(TipoNotificacao.LIVROS, btnLivros) }
        btnJogos.setOnClickListener { aplicarFiltro(TipoNotificacao.JOGOS, btnJogos) }
        btnSalas.setOnClickListener { aplicarFiltro(TipoNotificacao.SALAS, btnSalas) }
        btnDevolucoes.setOnClickListener { aplicarFiltro(TipoNotificacao.DEVOLUCOES, btnDevolucoes) }
        btnDenuncias.setOnClickListener { aplicarFiltro(TipoNotificacao.DENUNCIAS, btnDenuncias) }
    }

    private fun aplicarFiltro(tipo: TipoNotificacao?, botaoSelecionado: Button) {
        // Filtrar visualmente os itens
        itensNotificacao.forEach { (view, t) ->
            if (tipo == null || t == tipo) {
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }
        }

        // Atualizar estilo dos botões
        botoesFiltro.forEach { btn ->
            if (btn == botaoSelecionado) {
                btn.setBackgroundColor(android.graphics.Color.parseColor("#D1E8F3"))
            } else {
                btn.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }

    private fun carregarExemplos() {
        // Exemplo: Livro
        adicionarItemNotificacao(
            TipoNotificacao.LIVROS,
            "João Victor",
            "jotave@gmail.com",
            "Requerimento de aluguel do Livro\n'O Hobbit'"
        )

        // Exemplo: Sala
        adicionarItemNotificacao(
            TipoNotificacao.SALAS,
            "Fernanda Souza",
            "nanda@gmail.com",
            "Reserva da Sala de Estudos 02\nHorário: 14:00 - 16:00"
        )

        // Exemplo: Devolução
        adicionarItemNotificacao(
            TipoNotificacao.DEVOLUCOES,
            "Ygor Costa",
            "ygor@gmail.com",
            "Devolução do livro 'Corra'\nMulta: R$ 6,00",
            "Data Empréstimo: 27/03/2026\nPrazo de Entrega: 10/04/2026"
        )

        // Exemplo: Jogo
        adicionarItemNotificacao(
            TipoNotificacao.JOGOS,
            "Lucas Lima",
            "lucas@gmail.com",
            "Requerimento de aluguel do Jogo\n'Catan'"
        )

        // Exemplo: Denúncia
        adicionarItemNotificacao(
            TipoNotificacao.DENUNCIAS,
            "Thiago Narak",
            "narak@unifor.com",
            "Denúncia de comentário no livro\n'Harry Potter'\n- Conteúdo Inadequado"
        )
    }

    /**
     * Função Mestre: Adiciona qualquer tipo de notificação ao container
     */
    private fun adicionarItemNotificacao(
        tipo: TipoNotificacao,
        nome: String,
        email: String,
        descricao: String,
        extraInfo: String? = null // Usado para datas na devolução
    ) {
        val layoutRes = when (tipo) {
            TipoNotificacao.LIVROS, TipoNotificacao.JOGOS, TipoNotificacao.SALAS -> R.layout.item_solicitacao_aluguel
            TipoNotificacao.DEVOLUCOES -> R.layout.item_devolucao_admin
            TipoNotificacao.DENUNCIAS -> R.layout.item_denuncia_admin
        }

        val itemView = LayoutInflater.from(this).inflate(layoutRes, container, false)

        // Preencher campos comuns
        itemView.findViewById<TextView>(R.id.tvNomeUsuario).text = nome
        itemView.findViewById<TextView>(R.id.tvEmailUsuario).text = email
        itemView.findViewById<TextView>(R.id.tvDescricao).text = descricao

        // Configurar ações específicas
        when (tipo) {
            TipoNotificacao.LIVROS, TipoNotificacao.JOGOS, TipoNotificacao.SALAS -> {
                itemView.findViewById<Button>(R.id.btnAprovar).setOnClickListener {
                    confirmarAcao("Aprovar solicitação de $nome?") {
                        Toast.makeText(this, "Aprovado!", Toast.LENGTH_SHORT).show()
                        removerItem(itemView)
                    }
                }
                itemView.findViewById<Button>(R.id.btnRecusar).setOnClickListener {
                    confirmarAcao("Recusar solicitação de $nome?") {
                        Toast.makeText(this, "Recusado!", Toast.LENGTH_SHORT).show()
                        removerItem(itemView)
                    }
                }
            }
            TipoNotificacao.DEVOLUCOES -> {
                itemView.findViewById<TextView>(R.id.tvDatas).text = extraInfo
                itemView.findViewById<Button>(R.id.btnConfirmarDevolucao).setOnClickListener {
                    confirmarAcao("Confirmar devolução de $nome?") {
                        Toast.makeText(this, "Devolução confirmada!", Toast.LENGTH_SHORT).show()
                        removerItem(itemView)
                    }
                }
            }
            TipoNotificacao.DENUNCIAS -> {
                itemView.findViewById<Button>(R.id.btnVerificar).setOnClickListener {
                    startActivity(Intent(this, AdminMaisAvaliacoesActivity::class.java))
                }
            }
        }
        container.addView(itemView)
        itensNotificacao.add(Pair(itemView, tipo))
    }

    private fun removerItem(view: View) {
        container.removeView(view)
        itensNotificacao.removeAll { it.first == view }
    }

    private fun confirmarAcao(mensagem: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Confirmação")
            .setMessage(mensagem)
            .setPositiveButton("Sim") { _, _ -> onConfirm() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun setupNavBar() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavAdmin)
        bottomNav.selectedItemId = R.id.nav_notif

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