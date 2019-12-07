package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.databinding.ChatMessageCellBinding


class ChatView  : RecyclerView {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        Adapter(
            context
        )
    }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }

    class Adapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {
        private val items = mutableListOf<Message>()

        fun refresh(list: List<Message>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun addItem(list: List<Message>) {
            items.apply {
                addAll(list)
            }
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ChatMessageCellViewHolder(ChatMessageCellBinding.inflate(LayoutInflater.from(context), parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChatMessageCellViewHolder)
                onBindViewHolder(holder, position)
        }

        private fun onBindViewHolder(holder: ChatMessageCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.message = data
        }

    }
    class ChatMessageCellViewHolder(val binding: ChatMessageCellBinding) : RecyclerView.ViewHolder(binding.root)
}