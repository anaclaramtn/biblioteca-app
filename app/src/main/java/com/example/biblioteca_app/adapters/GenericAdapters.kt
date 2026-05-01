package com.example.biblioteca_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val layoutId: Int,
    items: List<T>,
    private val bind: (View, T, Int) -> Unit
) : RecyclerView.Adapter<GenericAdapter.ViewHolder>() {

    private val items = mutableListOf<T>()

    init {
        this.items.addAll(items)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bind(holder.itemView, items[position], position)
    }

    override fun getItemCount(): Int = items.size

    // 🔹 Atualizar lista inteira
    fun updateList(newList: List<T>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // 🔹 Remover item por posição
    fun removeAt(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // 🔹 Atualizar item específico
    fun updateItem(position: Int, item: T) {
        if (position in items.indices) {
            items[position] = item
            notifyItemChanged(position)
        }
    }

    // 🔹 Acesso à lista (se precisar)
    fun getItems(): List<T> = items
}