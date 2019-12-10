package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.databinding.ChatMessageCellBinding
import com.komugirice.icchat.databinding.ChatMessageOtheruserCellBinding


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

        /**
         * itemsの数によってVIEW_TYPEを振り分け
         *
         * @param position
         * @return VIEW_TYPE: Int
         */

        override fun getItemViewType(position: Int): Int {
            return if (items[position].userId.equals(UserManager.myUserId) )
                        VIEW_TYPE_LOGIN_USER
                    else
                        VIEW_TYPE_OTHER_USER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            if(viewType == VIEW_TYPE_LOGIN_USER)
                ChatMessageCellViewHolder(ChatMessageCellBinding.inflate(LayoutInflater.from(context), parent, false))
            else
                ChatMessageOtheruserCellViewHolder(ChatMessageOtheruserCellBinding.inflate(LayoutInflater.from(context), parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChatMessageCellViewHolder)
                onBindLoginUserViewHolder(holder, position)

            else if (holder is ChatMessageOtheruserCellViewHolder)
                onBindOtherUserViewHolder(holder, position)
        }

        private fun onBindLoginUserViewHolder(holder: ChatMessageCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.message = data
        }

        private fun onBindOtherUserViewHolder(holder: ChatMessageOtheruserCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.message = data
            // TODO UserManagerのfriendsから取得
            holder.binding.userName = "テストユーザ"
        }

    }
    class ChatMessageCellViewHolder(val binding: ChatMessageCellBinding) : RecyclerView.ViewHolder(binding.root)

    class ChatMessageOtheruserCellViewHolder(val binding: ChatMessageOtheruserCellBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val VIEW_TYPE_LOGIN_USER = 0
        private const val VIEW_TYPE_OTHER_USER = 1

    }
}