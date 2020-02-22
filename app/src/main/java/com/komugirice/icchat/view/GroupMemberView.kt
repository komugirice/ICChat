package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.GroupMemberCellBinding
import com.komugirice.icchat.databinding.TitleCellBinding
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

        override fun getItemViewType(position: Int): Int {
            // 0件対策
            return if(items.isEmpty()) EMPTY else EXIST
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder
            if(viewType == EXIST) {
                // データあり
                holder = GroupMemberCellViewHolder(
                    GroupMemberCellBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            } else {
                // 0件
                holder = EmptyCellViewHolder(
                    TitleCellBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
            }
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if(holder is GroupMemberCellViewHolder) {
                // データあり
                onBindViewHolder(holder, position)
            } else if(holder is EmptyCellViewHolder) {
                // 0件
                onBindViewHolder(holder, position)
            }
        }

        private fun onBindViewHolder(holder: GroupMemberCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.user = data
        }

        private fun onBindViewHolder(holder: EmptyCellViewHolder, position: Int) {
            holder.binding.title = context.getString(R.string.none)
        }

    }

    class GroupMemberCellViewHolder(val binding: GroupMemberCellBinding) : RecyclerView.ViewHolder(binding.root)
    class EmptyCellViewHolder(val binding: TitleCellBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val EMPTY = 0
        const val EXIST = 1
    }
}