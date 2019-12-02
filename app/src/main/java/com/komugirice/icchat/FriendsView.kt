package com.komugirice.icchat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.data.firestore.User

class FriendsView  : RecyclerView {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy { Adapter(context) }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }

    class Adapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {
        private val items = mutableListOf<User>()

        fun refresh(list: List<User>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun addItem(list: List<User>) {
            items.apply {
                addAll(list)
            }
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ItemViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.friend_cell,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemViewHolder)
                onBindViewHolder(holder, position)
        }

        private fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val data = items[position]
            holder.nameTextView.text = data.name
            holder.rootView.setOnClickListener {

            }
        }

        class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val rootView: ConstraintLayout = view.findViewById(R.id.rootView)
            val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        }

    }
}