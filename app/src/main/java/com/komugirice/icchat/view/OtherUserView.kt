package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.databinding.DrawerUserCellBinding
import com.komugirice.icchat.firebase.firestore.model.User

class OtherUserView : RecyclerView {
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
        val items = mutableListOf<User>()

        fun refresh(list: List<User>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder
            holder = DrawerUserCellViewHolder(
                DrawerUserCellBinding.inflate(
                    LayoutInflater.from(
                        context
                    ), parent, false
                )
            )
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = items[position]

            if(holder is DrawerUserCellViewHolder) {
                holder.binding.user = data
            }
        }

    }

    class DrawerUserCellViewHolder(val binding: DrawerUserCellBinding) : RecyclerView.ViewHolder(binding.root)


}