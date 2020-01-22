package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.databinding.GroupMemberCellBinding
import com.komugirice.icchat.firebase.firestore.model.User

class GroupMemberView : RecyclerView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        MemberAdapter(
            context
        )
    }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }

    class MemberAdapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {
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
            holder = GroupMemberCellViewHolder(
                GroupMemberCellBinding.inflate(
                    LayoutInflater.from(
                        context
                    ), parent, false
                )
            )
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = items[position]

            if(holder is GroupMemberCellViewHolder) {
                holder.binding.user = data
            }
        }

    }

    class GroupMemberCellViewHolder(val binding: GroupMemberCellBinding) : RecyclerView.ViewHolder(binding.root)


}