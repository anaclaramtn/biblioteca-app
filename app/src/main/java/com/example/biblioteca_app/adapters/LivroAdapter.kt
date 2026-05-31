package com.example.biblioteca_app.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.biblioteca_app.R
import com.example.biblioteca_app.models.Livro

class LivroAdapter(
    private var livros: List<Livro>,
    private val onClick: (Livro) -> Unit
) : RecyclerView.Adapter<LivroAdapter.LivroViewHolder>() {

    class LivroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCapa)
        val titulo: TextView = view.findViewById(R.id.txtTituloLivro)
        val autor: TextView = view.findViewById(R.id.txtAutor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LivroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_livro, parent, false)
        return LivroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LivroViewHolder, position: Int) {
        val livro = livros[position]

        holder.titulo.text = livro.titulo
        holder.autor.text = livro.autor

        when {
            !livro.imagemBase64.isNullOrEmpty() -> {
                val bytes = Base64.decode(livro.imagemBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                holder.img.setImageBitmap(bitmap)
            }

            livro.imagemRes != null && livro.imagemRes != 0 -> {
                holder.img.setImageResource(livro.imagemRes!!)
            }

            else -> {
                holder.img.setImageResource(R.drawable.capadomquixote)
            }
        }

        holder.itemView.setOnClickListener {
            onClick(livro)
        }
    }

    override fun getItemCount(): Int = livros.size

    fun updateList(novaLista: List<Livro>) {
        livros = novaLista
        notifyDataSetChanged()
    }
}