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
import com.komugirice.icchat.databinding.FriendRequestCellBinding
import com.komugirice.icchat.databinding.TitleCellBinding
import com.komugirice.icchat.firestore.manager.RequestManager
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RequestStore
import com.komugirice.icchat.firestore.store.RoomStore
import com.komugirice.icchat.util.FireStorageUtil
import com.komugirice.icchat.util.FriendViewDialogUtil

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

            // databindingにRoomを使うタイプ
            if(viewType <= VIEW_TYPE_ITEM_DENY_GROUP) {
                return FriendCellViewHolder(
                    FriendCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )

            }
            // databindingにRequestを使うタイプ
            if ( viewType <= VIEW_TYPE_ITEM_DENY_FRIEND) {
                return RequestCellViewHolder(
                    FriendRequestCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            }
            // タイトル
            return TitleCellViewHolder(
                TitleCellBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is FriendCellViewHolder)
                onBindViewHolder(holder, position)
            else if(holder is RequestCellViewHolder)
                onBindViewHolder(holder, position)
            else if(holder is TitleCellViewHolder)
                onBindViewHolder(holder, position)

        }

        /**
         * databindingにRoomを使うタイプ
         * 友だち、グループ、招待グループ、拒否グループ
         * @param holder
         * @param position
         *
         */
        private fun onBindViewHolder(holder: FriendCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.room = data.room

            // タップ
            holder.binding.root.setOnClickListener {
                // 招待中のグループの場合
                if(data.viewType == VIEW_TYPE_ITEM_REQUEST_GROUP) {
                    FriendViewDialogUtil.confirmGroupRequestDialog(context, data.room)
                    return@setOnClickListener
                }
                // 拒否グループの場合
                if(data.viewType == VIEW_TYPE_ITEM_DENY_GROUP) {
                    FriendViewDialogUtil.cancelGroupDenyDialog(context, data.room)
                    return@setOnClickListener
                }
                // それ以外
                ChatActivity.start(context, data.room)

            }

            // 長押し
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
                                        FriendViewDialogUtil.confirmDeleteGroupDialog(context, data.room)
                                    }
                                    else -> return@listItems
                                }

                            })
                        }.show()
                    }
                    // 管理者ではないグループor友だち
                    if( data.viewType == VIEW_TYPE_ITEM_FRIEND || data.viewType == VIEW_TYPE_ITEM_GROUP) {

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
                    // 招待中のグループの場合
                    if(data.viewType == VIEW_TYPE_ITEM_REQUEST_GROUP) {
                        FriendViewDialogUtil.confirmGroupRequestDialog(context, data.room)
                    }
                    // 拒否グループの場合
                    if(data.viewType == VIEW_TYPE_ITEM_DENY_GROUP) {
                        FriendViewDialogUtil.cancelGroupDenyDialog(context, data.room)
                    }
                    return true
                }
            })
        }

        /**
         * databindingにRequestを使うタイプ
         * 申請ユーザ、拒否ユーザ
         *
         * @param holder
         * @param position
         *
         */
        private fun onBindViewHolder(holder: RequestCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.request = data.request

            // タップ
            holder.binding.root.setOnClickListener {
                // 友だち申請を受けているユーザの場合
                if (data.viewType == VIEW_TYPE_ITEM_REQUEST_FRIEND) {
                    FriendViewDialogUtil.confirmUserRequestDialog(
                        context,
                        data.request ?: Request()
                    )
                }
                // 友だち申請を拒否したユーザの場合
                if (data.viewType == VIEW_TYPE_ITEM_DENY_FRIEND) {
                    FriendViewDialogUtil.cancelUserDenyDialog(
                        context,
                        data.request ?: Request()
                    )
                }
            }

            // 長押し
            holder.binding.root.setOnLongClickListener(object: View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    // 友だち申請を受けているユーザの場合
                    if (data.viewType == VIEW_TYPE_ITEM_REQUEST_FRIEND) {
                        FriendViewDialogUtil.confirmUserRequestDialog(
                            context,
                            data.request ?: Request()
                        )
                    }
                    // 友だち申請を拒否したユーザの場合
                    if (data.viewType == VIEW_TYPE_ITEM_DENY_FRIEND) {
                        FriendViewDialogUtil.cancelUserDenyDialog(
                            context,
                            data.request ?: Request()
                        )
                    }
                    return true
                }
            })
        }

        /**
         * タイトル
         *
         * @param holder
         * @param position
         *
         */
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
                VIEW_TYPE_TITLE_REQUEST_GROUP -> {
                    holder.binding.title = context.getString(R.string.title_invite_group)
                }
                VIEW_TYPE_TITLE_DENY_GROUP -> {
                    holder.binding.title = context.getString(R.string.title_deny_group)
                }
                VIEW_TYPE_TITLE_REQUEST_FRIEND -> {
                    holder.binding.title = context.getString(R.string.title_request_friend)
                }
                VIEW_TYPE_TITLE_DENY_FRIEND -> {
                    holder.binding.title = context.getString(R.string.title_deny_friend)
                }
                else -> return
            }
        }
    }

    class FriendCellViewHolder(val binding: FriendCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class RequestCellViewHolder(val binding: FriendRequestCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class TitleCellViewHolder(val binding: TitleCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class FriendsViewData {
        var room: Room = Room()
        var request: Request? = null
        var viewType: Int

        constructor(room: Room, viewType: Int) {
            this.room = room
            this.viewType = viewType
        }
        constructor(request: Request, viewType: Int) {
            this.request = request
            this.viewType = viewType
        }
        constructor(viewType: Int) {
            this.viewType = viewType
        }
    }

    companion object {
        const val VIEW_TYPE_ITEM_GROUP = 0
        const val VIEW_TYPE_ITEM_FRIEND = 1
        const val VIEW_TYPE_ITEM_REQUEST_GROUP = 2
        const val VIEW_TYPE_ITEM_DENY_GROUP = 3
        const val VIEW_TYPE_ITEM_REQUEST_FRIEND = 4
        const val VIEW_TYPE_ITEM_DENY_FRIEND = 5
        const val VIEW_TYPE_TITLE_GROUP = 6
        const val VIEW_TYPE_TITLE_FRIEND = 7
        const val VIEW_TYPE_TITLE_REQUEST_GROUP = 8
        const val VIEW_TYPE_TITLE_DENY_GROUP = 9
        const val VIEW_TYPE_TITLE_REQUEST_FRIEND = 10
        const val VIEW_TYPE_TITLE_DENY_FRIEND = 11

    }
}