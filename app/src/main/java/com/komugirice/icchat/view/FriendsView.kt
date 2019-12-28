package com.komugirice.icchat.view

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.google.firebase.storage.FirebaseStorage
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.GroupSettingActivity
import com.komugirice.icchat.LoginActivity
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.FriendCellBinding
import com.komugirice.icchat.databinding.TitleCellBinding
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RoomStore
import com.komugirice.icchat.util.FireStorageUtil

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

            if(viewType <= VIEW_TYPE_ITEM_DENY) {
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
                // 招待中のグループの場合
                if(data.viewType == VIEW_TYPE_ITEM_INVITE) {
                    AlertDialog.Builder(context)
                        .setMessage("招待中のグループを承認しますか？")
                        .setPositiveButton("承認", object: DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                RoomStore.acceptGroupMember(data.room, UserManager.myUserId){
                                    Toast.makeText(
                                        context,
                                        "承認しました",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                        .setNegativeButton("拒否", object: DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                RoomStore.denyGroup(data.room, UserManager.myUserId){
                                    Toast.makeText(
                                        context,
                                        "拒否しました",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                        .setNeutralButton("キャンセル", object: DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                            }
                        }).show()
                    return@setOnClickListener
                }
                // 拒否グループの場合
                if(data.viewType == VIEW_TYPE_ITEM_DENY) {
                    AlertDialog.Builder(context)
                        .setMessage("グループの拒否を取り消しますか？")
                        .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                RoomStore.cancelDenyGroup(data.room, UserManager.myUserId){
                                    Toast.makeText(
                                        context,
                                        "拒否を取り消しました",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .show()
                    return@setOnClickListener
                }
                // それ以外
                ChatActivity.start(context, data.room)

            }

            holder.binding.root.setOnLongClickListener(object: View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {

                    val menuList = listOf(
                        Pair(0, R.string.chat_activity),
                        Pair(1, R.string.group_setting),
                        Pair(2, R.string.group_delete)
                    )


                    if(data.viewType == VIEW_TYPE_ITEM_GROUP && data.room.ownerId.equals(UserManager.myUserId) ) {
                        // 管理者であるグループ
                        MaterialDialog(context).apply {
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
                                        AlertDialog.Builder(context)
                                            .setMessage(context.getString(R.string.confirm_group_delete))
                                            .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                    RoomStore.deleteRoom(data.room.documentId) {
                                                        FireStorageUtil.deleteGroupIconImage(data.room.documentId) {
                                                            Toast.makeText(
                                                                context,
                                                                context.getString(R.string.success_group_delete),
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            })
                                            .setNegativeButton("キャンセル", null)
                                            .show()
                                    }
                                    else -> return@listItems
                                }

                            })
                        }.show()
                    } else if( data.viewType == VIEW_TYPE_ITEM_FRIEND || data.viewType == VIEW_TYPE_ITEM_GROUP) {
                        // グループだが管理者ではないor友だち
                        MaterialDialog(context).apply {
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
                                }
                            )
                        }.show()
                    }
                    return true
                }
            })
        }


        private fun onBindViewHolder(holder: TitleCellViewHolder, position: Int) {
            val data = items[position]
            when(data.viewType ) {
                VIEW_TYPE_TITLE_GROUP -> {
                    val size = items.filter {it.viewType == VIEW_TYPE_ITEM_GROUP}.size
                    holder.binding.title = context.getString(R.string.title_group) + " ${size}"
                }
                VIEW_TYPE_TITLE_FRIEND -> {
                    val size = items.filter {it.viewType == VIEW_TYPE_ITEM_FRIEND}.size
                    holder.binding.title = context.getString(R.string.title_friend) + " ${size}"
                }
                VIEW_TYPE_TITLE_INVITE -> {
                    val size = items.filter {it.viewType == VIEW_TYPE_ITEM_INVITE}.size
                    holder.binding.title = context.getString(R.string.title_invite_group) + " ${size}"
                    // 招待中0件の場合、非表示
                    if(size == 0) holder.binding.root.visibility = View.GONE

                }
                VIEW_TYPE_TITLE_DENY -> {
                    val size = items.filter {it.viewType == VIEW_TYPE_ITEM_DENY}.size
                    holder.binding.title = context.getString(R.string.title_deny_group)
                    // 拒否0件の場合、非表示
                    if(size == 0) holder.binding.root.visibility = View.GONE

                }
                else -> return
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
        const val VIEW_TYPE_ITEM_GROUP = 0
        const val VIEW_TYPE_ITEM_FRIEND = 1
        const val VIEW_TYPE_ITEM_INVITE = 2
        const val VIEW_TYPE_ITEM_DENY = 3
        const val VIEW_TYPE_TITLE_GROUP = 4
        const val VIEW_TYPE_TITLE_FRIEND = 5
        const val VIEW_TYPE_TITLE_INVITE = 6
        const val VIEW_TYPE_TITLE_DENY = 7
    }
}