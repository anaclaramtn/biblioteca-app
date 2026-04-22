package com.example.biblioteca_app

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.adapters.GenericAdapter
import com.example.biblioteca_app.models.Noticia

class TelaHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tela_home)
        
        // Configuração das Notícias
        val rvNoticias = findViewById<RecyclerView>(R.id.rvNoticias)
        val layoutDots = findViewById<LinearLayout>(R.id.layoutDots)
        val btnPrev = findViewById<ImageButton>(R.id.btnPrevNoticia)
        val btnNext = findViewById<ImageButton>(R.id.btnNextNoticia)

        val listaNoticias = listOf(
            Noticia("Novo Acervo de Fantasia", "Chegaram mais de 50 novos títulos de fantasia épica esta semana!"),
            Noticia("Evento de Leitura", "Participe do nosso círculo de leitura no próximo sábado às 14h.")
        )

        rvNoticias.adapter = GenericAdapter(R.layout.item_noticia, listaNoticias) { view, noticia ->
            view.findViewById<TextView>(R.id.txtTituloNoticia).text = noticia.titulo
            view.findViewById<TextView>(R.id.txtDescricaoNoticia).text = noticia.descricao
        }

        // SnapHelper para comportamento de carrossel (para parar na notícia centralizada)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvNoticias)

        // Configuração das Bolinhas (Indicadores)
        setupDots(layoutDots, listaNoticias.size)
        updateDots(layoutDots, 0)

        rvNoticias.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        updateDots(layoutDots, position)
                    }
                }
            }
        })

        // Botões de Navegação
        btnPrev.setOnClickListener {
            val currentPos = (rvNoticias.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (currentPos > 0) rvNoticias.smoothScrollToPosition(currentPos - 1)
        }

        btnNext.setOnClickListener {
            val currentPos = (rvNoticias.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (currentPos < listaNoticias.size - 1) rvNoticias.smoothScrollToPosition(currentPos + 1)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupDots(container: LinearLayout, count: Int) {
        container.removeAllViews()
        for (i in 0 until count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            dot.setBackgroundResource(android.R.drawable.presence_invisible) // Círculo cinza simples
            dot.alpha = 0.3f
            container.addView(dot)
        }
    }

    private fun updateDots(container: LinearLayout, activeIndex: Int) {
        for (i in 0 until container.childCount) {
            val dot = container.getChildAt(i)
            dot.alpha = if (i == activeIndex) 1.0f else 0.3f
        }
    }
}