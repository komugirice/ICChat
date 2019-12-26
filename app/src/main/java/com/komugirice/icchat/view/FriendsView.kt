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
import com.komugirice.icchat.databinding.TitleCellBinding
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
        private val items = mutableListOf<FriendsViewData>()

        fun refresh(list: List<FriendsViewData>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun addItem(list: List<FriendsViewData>) {
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

        /**
         * itemsの数によってVIEW_TYPEを振り分け
         *
         * @param position
         * @return VIEW_TYPE: Int
         */

        override fun getItemViewType(position: Int): Int {
            return items[position].viewType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            if(viewType == VIEW_TYPE_ITEM) {
                return FriendCellViewHolder(
                        FriendCellBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                    )
                )
            } else {
                return TitleCellViewHolder(
                    TitleCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FriendCellViewHolder)
                onBindViewHolder(holder, position)
            else if(holder is TitleCellViewHolder)
                onBindViewHolder(holder, position)

        }

        private fun onBindViewHolder(holder: FriendCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.room = data.room

            holder.binding.root.setOnClickListener {
                ChatActivity.start(context, data.room)

            }

            holder.binding.root.setOnLongClickListener(object: View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {

                    val menuList = listOf(
                        Pair(0, R.string.chat_activity),
                        Pair(1, R.string.group_settings),
                        Pair(2, R.string.group_delete)
                    )

                    MaterialDialog(context).apply {
                        if(data.room.isGroup == true && data.room.ownerId.equals(UserManager.myUserId) ) {
                            // 管理者であるグループ
                            listItems(items = listOf(
                                context.getString(menuList.get(0).second),
                                context.getString(menuList.get(1).second),
                                context.getString(menuList.get(2).second)
                            ),
                            selection = { dialog, index, text ->
                                when (index) {
                                    menuList.get(0).first -> {
                                        ChatActivity.start(context, data.room)
                                    }
                                    menuList.get(1).first -> {
                                        GroupSettingActivity.update(context, data.room)
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
                                        ChatActivity.start(context, data.room)
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


        private fun onBindViewHolder(holder: TitleCellViewHolder, position: Int) {
            val data = items[position]
            if(data.viewType == VIEW_TYPE_TITLE_GROUP) {
                val size = items.filter {it.room.isGroup == true && it.viewType == VIEW_TYPE_ITEM}.size
                holder.binding.title = context.getString(R.string.title_group) + " ${size}"
            } else {
                val size = items.filter {it.room.isGroup == false && it.viewType == VIEW_TYPE_ITEM}.size
                holder.binding.title = context.getString(R.string.title_friend) + " ${size}"
            }
        }

    }

    class FriendCellViewHolder(val binding: FriendCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TitleCellViewHolder(val binding: TitleCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FriendsViewData {
        var room: Room
        var viewType: Int

        constructor(room: Room, viewType: Int) {
            this.room = room
            this.viewType = viewType
        }
    }

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_TITLE_GROUP = 1
        const val VIEW_TYPE_TITLE_FRIEND = 2
    }
}