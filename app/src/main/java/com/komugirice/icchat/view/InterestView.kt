package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.databinding.InterestCellBinding
import com.komugirice.icchat.databinding.RoomCellBinding
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room

class InterestView : RecyclerView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        InterestView.Adapter(
            context
        )
    }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }
    class Adapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items = mutableListOf<Interest>()

        fun refresh(list: List<Interest>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun clear() {
            items.clear()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            InterestCellViewHolder(
                InterestCellBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is InterestCellViewHolder)
                onBindViewHolder(holder, position)
        }

        private fun onBindViewHolder(holder: InterestCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.interest = data
            holder.binding.isLeft = position % 2 == 0

            holder.binding.root.setOnClickListener {

            }
        }
    }

    class InterestCellViewHolder(val binding: InterestCellBinding) : RecyclerView.ViewHolder(binding.root)
}