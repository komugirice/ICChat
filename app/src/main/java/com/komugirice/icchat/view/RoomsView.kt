package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.databinding.RoomCellBinding
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room

class RoomsView : RecyclerView {
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

    class Adapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items = mutableListOf<Pair<Room, Message?>>()


        fun refresh(list: List<Pair<Room, Message?>>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun addItem(list: List<Pair<Room, Message?>>) {
            items.apply {
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
            RoomCellViewHolder(RoomCellBinding.inflate(LayoutInflater.from(context), parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is RoomCellViewHolder)
                onBindViewHolder(holder, position)
        }

        private fun onBindViewHolder(holder: RoomCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.room = data.first
            holder.binding.message = data.second

            holder.binding.root.setOnClickListener {
                ChatActivity.start(context, data.first)

            }
        }

    }

    class RoomCellViewHolder(val binding: RoomCellBinding) : RecyclerView.ViewHolder(binding.root)

}