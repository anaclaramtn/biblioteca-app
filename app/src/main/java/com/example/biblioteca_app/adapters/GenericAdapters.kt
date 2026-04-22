package com.example.biblioteca_app.adapters

import android.view.*
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val layoutId: Int,
    private val items: List<T>,
    private val bind: (View, T) -> Unit
) : RecyclerView.Adapter<GenericAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(layoutId, p, false))
    override fun onBindViewHolder(h: ViewHolder, p: Int) = bind(h.itemView, items[p])
    override fun getItemCount() = items.size
}