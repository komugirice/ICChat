package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.GroupSettingActivity
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.FriendCellBinding
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room

class FriendsView : RecyclerView {

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
        private val items = mutableListOf<Room>()

        fun refresh(list: List<Room>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun addItem(list: List<Room>) {
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
            FriendCellViewHolder(
                FriendCellBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FriendCellViewHolder)
                onBindViewHolder(holder, position)
        }

        private fun onBindViewHolder(holder: FriendCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.room = data

            holder.binding.root.setOnClickListener {
                ChatActivity.start(context, data)
            }

            holder.binding.root.setOnLongClickListener(object: View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {

                    val menuList = listOf(
                        Pair(0, R.string.chat_activity),
                        Pair(1, R.string.group_settings),
                        Pair(2, R.string.group_delete)
                    )

                    MaterialDialog(context).apply {
                        if(data.isGroup == true && data.ownerId.equals(UserManager.myUserId) ) {
                            // 管理者であるグループ
                            listItems(items = listOf(
                                context.getString(menuList.get(0).second),
                                context.getString(menuList.get(1).second),
                                context.getString(menuList.get(2).second)
                            ),
                            selection = { dialog, index, text ->
                                when (index) {
                                    menuList.get(0).first -> {
                                        ChatActivity.start(context, data)
                                    }
                                    menuList.get(1).first -> {
                                        GroupSettingActivity.update(context, data)
                                    }
                                    menuList.get(2).first -> {
                                    }
                                    else -> return@listItems
                                }

                            })
                        } else {
                            // それ以外
                            listItems(items = listOf(
                                context.getString(menuList.get(0).second)
                            ),
                            selection = { dialog, index, text ->
                                when (index) {
                                    menuList.get(0).first -> {
                                        ChatActivity.start(context, data)
                                    }
                                    else -> return@listItems
                                }

                            })
                        }
                    }.show()
                    return true
                }
            })
        }

    }

    class FriendCellViewHolder(val binding: FriendCellBinding) :
        RecyclerView.ViewHolder(binding.root)
}