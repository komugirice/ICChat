package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.data.firestore.model.Message
import com.komugirice.icchat.data.firestore.model.User
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
        private val usersMap = mutableMapOf<String, User>()

        fun refresh(list: List<Message>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun setUsers(list: List<User>) {
            val map = list.map{ it.userId to it}.toMap()
            usersMap.apply {
                clear()
                putAll(map)
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder
            if(viewType == VIEW_TYPE_LOGIN_USER) {
                holder = ChatMessageCellViewHolder(
                    ChatMessageCellBinding.inflate(
                        LayoutInflater.from(
                            context
                        ), parent, false
                    )
                )
                holder.binding.root.setOnClickListener(object: View.OnClickListener{
                    override fun onClick(v: View?) {
                        hideKeyboard(v)
                    }
                })
            } else {
                holder = ChatMessageOtheruserCellViewHolder(
                    ChatMessageOtheruserCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
                holder.binding.root.setOnClickListener(object: View.OnClickListener{
                    override fun onClick(v: View?) {
                        hideKeyboard(v)
                    }
                })
            }

            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChatMessageCellViewHolder)
                onBindLoginUserViewHolder(holder, position)

            else if (holder is ChatMessageOtheruserCellViewHolder)
                onBindOtherUserViewHolder(holder, position)
        }

        /**
         * ログインユーザ用
         *
         * @param holder
         * @param position
         */
        private fun onBindLoginUserViewHolder(holder: ChatMessageCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.message = data
        }

        /**
         * ログインユーザ以外用
         *
         * @param holder
         * @param position
         */
        private fun onBindOtherUserViewHolder(holder: ChatMessageOtheruserCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.message = data
            // TODO UserManagerのfriendsから取得
            holder.binding.user = usersMap[data.userId]
        }

        /**
         * hideKeyboard
         *
         * @param v: View?
         */
        private fun hideKeyboard(v: View?) {
            (v?.getContext()?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                v?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

    }
    class ChatMessageCellViewHolder(val binding: ChatMessageCellBinding) : RecyclerView.ViewHolder(binding.root)

    class ChatMessageOtheruserCellViewHolder(val binding: ChatMessageOtheruserCellBinding) : RecyclerView.ViewHolder(binding.root)


    companion object {
        private const val VIEW_TYPE_LOGIN_USER = 0
        private const val VIEW_TYPE_OTHER_USER = 1

    }
}